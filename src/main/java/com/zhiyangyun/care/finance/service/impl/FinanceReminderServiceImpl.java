package com.zhiyangyun.care.finance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.finance.entity.FinanceDepositAccount;
import com.zhiyangyun.care.finance.entity.FinanceElectricityReading;
import com.zhiyangyun.care.finance.entity.FinanceReminder;
import com.zhiyangyun.care.finance.mapper.FinanceDepositAccountMapper;
import com.zhiyangyun.care.finance.mapper.FinanceElectricityReadingMapper;
import com.zhiyangyun.care.finance.mapper.FinanceReminderMapper;
import com.zhiyangyun.care.finance.model.FinanceReminderSummary;
import com.zhiyangyun.care.finance.model.FinanceReminderView;
import com.zhiyangyun.care.finance.service.FinanceReminderService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceReminderServiceImpl implements FinanceReminderService {
  private static final String TYPE_CARE_FEE = "NEXT_MONTH_CARE_FEE";
  private static final String TYPE_DEPOSIT = "DEPOSIT_SHORTFALL";
  private static final String TYPE_ELEC_UNRECORDED = "ELECTRICITY_UNRECORDED";
  private static final String TYPE_ELEC_UNPAID = "ELECTRICITY_UNPAID";
  private static final String STATUS_PENDING = "PENDING";
  private static final String STATUS_HANDLED = "HANDLED";

  private final FinanceReminderMapper reminderMapper;
  private final FinanceDepositAccountMapper depositAccountMapper;
  private final FinanceElectricityReadingMapper electricityReadingMapper;
  private final BillMonthlyMapper billMonthlyMapper;
  private final ElderMapper elderMapper;
  private final RoomMapper roomMapper;

  public FinanceReminderServiceImpl(FinanceReminderMapper reminderMapper,
      FinanceDepositAccountMapper depositAccountMapper,
      FinanceElectricityReadingMapper electricityReadingMapper,
      BillMonthlyMapper billMonthlyMapper,
      ElderMapper elderMapper,
      RoomMapper roomMapper) {
    this.reminderMapper = reminderMapper;
    this.depositAccountMapper = depositAccountMapper;
    this.electricityReadingMapper = electricityReadingMapper;
    this.billMonthlyMapper = billMonthlyMapper;
    this.elderMapper = elderMapper;
    this.roomMapper = roomMapper;
  }

  @Override
  @Transactional
  public int generateNextMonthCareFeeReminder(Long orgId, LocalDate today) {
    if (orgId == null) {
      return 0;
    }
    YearMonth nextMonth = YearMonth.from(today).plusMonths(1);
    YearMonth currentMonth = YearMonth.from(today);
    long residentCount = elderMapper.selectCount(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(ElderProfile::getOrgId, orgId)
            .eq(ElderProfile::getStatus, 1));
    BigDecimal currentMonthTotal = billMonthlyMapper.selectList(
            Wrappers.lambdaQuery(BillMonthly.class)
                .eq(BillMonthly::getIsDeleted, 0)
                .eq(BillMonthly::getOrgId, orgId)
                .eq(BillMonthly::getBillMonth, currentMonth.toString()))
        .stream()
        .map(item -> scale2(item.getTotalAmount()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    FinanceReminder reminder = new FinanceReminder();
    reminder.setOrgId(orgId);
    reminder.setReminderType(TYPE_CARE_FEE);
    reminder.setDedupeKey(TYPE_CARE_FEE + ":" + nextMonth);
    reminder.setTitle(nextMonth + " 代养费即将生成，请提前核对");
    reminder.setContent("当前在住 " + residentCount + " 人，" + currentMonth + " 账单合计 "
        + currentMonthTotal + " 元。请在生成前核对护理等级、折扣与长护险抵扣口径。");
    reminder.setSeverity("WARNING");
    reminder.setBizMonth(nextMonth.toString());
    reminder.setAmount(currentMonthTotal);
    reminder.setActionPath("/finance/bills/detail-query");
    return insertIfAbsent(reminder) ? 1 : 0;
  }

  @Override
  @Transactional
  public int generateDepositShortfallReminders(Long orgId, LocalDate today) {
    if (orgId == null) {
      return 0;
    }
    YearMonth month = YearMonth.from(today);
    List<FinanceDepositAccount> accounts = depositAccountMapper.selectList(
        Wrappers.lambdaQuery(FinanceDepositAccount.class)
            .eq(FinanceDepositAccount::getIsDeleted, 0)
            .eq(FinanceDepositAccount::getOrgId, orgId)
            .ne(FinanceDepositAccount::getStatus, "CLOSED"));
    if (accounts.isEmpty()) {
      return 0;
    }
    Map<Long, ElderProfile> elderMap = loadElders(accounts.stream()
        .map(FinanceDepositAccount::getElderId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet()));
    int created = 0;
    for (FinanceDepositAccount account : accounts) {
      BigDecimal gap = scale2(account.getStandardAmount()).subtract(scale2(account.getPaidAmount()));
      if (gap.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      ElderProfile elder = elderMap.get(account.getElderId());
      FinanceReminder reminder = new FinanceReminder();
      reminder.setOrgId(orgId);
      reminder.setReminderType(TYPE_DEPOSIT);
      reminder.setDedupeKey(TYPE_DEPOSIT + ":" + month + ":" + account.getElderId());
      reminder.setTitle("押金未缴清：" + (elder == null ? "长者#" + account.getElderId() : elder.getFullName()));
      reminder.setContent("应缴 " + scale2(account.getStandardAmount()) + " 元，已缴 "
          + scale2(account.getPaidAmount()) + " 元，还差 " + gap + " 元。");
      reminder.setSeverity("WARNING");
      reminder.setBizMonth(month.toString());
      reminder.setElderId(account.getElderId());
      reminder.setAmount(gap);
      reminder.setActionPath("/finance/deposit-management");
      if (insertIfAbsent(reminder)) {
        created += 1;
      }
    }
    return created;
  }

  @Override
  @Transactional
  public int generateElectricityReminders(Long orgId, LocalDate today) {
    if (orgId == null) {
      return 0;
    }
    YearMonth month = YearMonth.from(today);
    List<Room> rooms = roomMapper.selectList(
        Wrappers.lambdaQuery(Room.class)
            .eq(Room::getIsDeleted, 0)
            .eq(Room::getOrgId, orgId));
    List<FinanceElectricityReading> readings = electricityReadingMapper.selectList(
        Wrappers.lambdaQuery(FinanceElectricityReading.class)
            .eq(FinanceElectricityReading::getIsDeleted, 0)
            .eq(FinanceElectricityReading::getOrgId, orgId)
            .eq(FinanceElectricityReading::getBillMonth, month.toString()));
    Set<Long> recordedRoomIds = readings.stream()
        .map(FinanceElectricityReading::getRoomId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    int created = 0;
    long unrecorded = rooms.stream()
        .map(Room::getId)
        .filter(Objects::nonNull)
        .filter(id -> !recordedRoomIds.contains(id))
        .count();
    if (unrecorded > 0) {
      FinanceReminder reminder = new FinanceReminder();
      reminder.setOrgId(orgId);
      reminder.setReminderType(TYPE_ELEC_UNRECORDED);
      reminder.setDedupeKey(TYPE_ELEC_UNRECORDED + ":" + month);
      reminder.setTitle(month + " 还有 " + unrecorded + " 间房未登记电表读数");
      reminder.setContent("共 " + rooms.size() + " 间房，已登记 " + recordedRoomIds.size()
          + " 间，未登记 " + unrecorded + " 间。未登记的房间无法核算电费。");
      reminder.setSeverity("INFO");
      reminder.setBizMonth(month.toString());
      reminder.setAmount(BigDecimal.valueOf(unrecorded));
      reminder.setActionPath("/finance/electricity-fee");
      if (insertIfAbsent(reminder)) {
        created += 1;
      }
    }

    for (FinanceElectricityReading reading : readings) {
      if ("PAID".equals(reading.getPayStatus())) {
        continue;
      }
      FinanceReminder reminder = new FinanceReminder();
      reminder.setOrgId(orgId);
      reminder.setReminderType(TYPE_ELEC_UNPAID);
      reminder.setDedupeKey(TYPE_ELEC_UNPAID + ":" + month + ":" + reading.getRoomId());
      reminder.setTitle("电费未缴：" + reading.getRoomNo() + " 房");
      reminder.setContent(month + " 用电 " + scale2(reading.getUsageAmount()) + " 度，电费 "
          + scale2(reading.getFeeAmount()) + " 元，尚未收取。");
      reminder.setSeverity("WARNING");
      reminder.setBizMonth(month.toString());
      reminder.setRoomId(reading.getRoomId());
      reminder.setAmount(scale2(reading.getFeeAmount()));
      reminder.setActionPath("/finance/electricity-fee");
      if (insertIfAbsent(reminder)) {
        created += 1;
      }
    }
    return created;
  }

  @Override
  public IPage<FinanceReminderView> page(long pageNo, long pageSize, String status, String reminderType) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(FinanceReminder.class)
        .eq(FinanceReminder::getIsDeleted, 0)
        .eq(orgId != null, FinanceReminder::getOrgId, orgId);
    String normalizedStatus = normalizeUpper(status);
    if (STATUS_PENDING.equals(normalizedStatus) || STATUS_HANDLED.equals(normalizedStatus)) {
      wrapper.eq(FinanceReminder::getStatus, normalizedStatus);
    }
    String normalizedType = normalizeUpper(reminderType);
    if (!normalizedType.isEmpty()) {
      wrapper.eq(FinanceReminder::getReminderType, normalizedType);
    }
    wrapper.orderByAsc(FinanceReminder::getStatus)
        .orderByDesc(FinanceReminder::getCreateTime)
        .orderByDesc(FinanceReminder::getId);

    IPage<FinanceReminder> page = reminderMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, ElderProfile> elderMap = loadElders(page.getRecords().stream()
        .map(FinanceReminder::getElderId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet()));
    IPage<FinanceReminderView> viewPage = new Page<>(pageNo, pageSize);
    viewPage.setTotal(page.getTotal());
    viewPage.setRecords(page.getRecords().stream()
        .map(item -> toView(item, item.getElderId() == null ? null : elderMap.get(item.getElderId())))
        .toList());
    return viewPage;
  }

  @Override
  public FinanceReminderSummary summary() {
    Long orgId = AuthContext.getOrgId();
    List<FinanceReminder> rows = reminderMapper.selectList(
        Wrappers.lambdaQuery(FinanceReminder.class)
            .eq(FinanceReminder::getIsDeleted, 0)
            .eq(orgId != null, FinanceReminder::getOrgId, orgId));
    FinanceReminderSummary summary = new FinanceReminderSummary();
    int pending = 0;
    int handled = 0;
    for (FinanceReminder row : rows) {
      if (STATUS_HANDLED.equals(row.getStatus())) {
        handled += 1;
        continue;
      }
      pending += 1;
      summary.getPendingByType().merge(row.getReminderType(), 1, Integer::sum);
    }
    summary.setPendingCount(pending);
    summary.setHandledCount(handled);
    return summary;
  }

  @Override
  @Transactional
  public FinanceReminderView handle(Long reminderId, String remark) {
    FinanceReminder reminder = reminderId == null ? null : reminderMapper.selectById(reminderId);
    if (reminder == null) {
      throw new IllegalArgumentException("提醒不存在");
    }
    ensureOrgAccess(reminder.getOrgId());
    if (STATUS_HANDLED.equals(reminder.getStatus())) {
      return toView(reminder, null);
    }
    reminder.setStatus(STATUS_HANDLED);
    reminder.setHandledAt(LocalDateTime.now());
    reminder.setHandledBy(AuthContext.getStaffId());
    reminder.setHandleRemark(trimToNull(remark));
    reminderMapper.updateById(reminder);
    return toView(reminder, null);
  }

  @Override
  @Transactional
  public int handleAll(String reminderType, String remark) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(FinanceReminder.class)
        .eq(FinanceReminder::getIsDeleted, 0)
        .eq(orgId != null, FinanceReminder::getOrgId, orgId)
        .eq(FinanceReminder::getStatus, STATUS_PENDING);
    String normalizedType = normalizeUpper(reminderType);
    if (!normalizedType.isEmpty()) {
      wrapper.eq(FinanceReminder::getReminderType, normalizedType);
    }
    List<FinanceReminder> pending = reminderMapper.selectList(wrapper);
    LocalDateTime now = LocalDateTime.now();
    Long staffId = AuthContext.getStaffId();
    for (FinanceReminder reminder : pending) {
      reminder.setStatus(STATUS_HANDLED);
      reminder.setHandledAt(now);
      reminder.setHandledBy(staffId);
      reminder.setHandleRemark(trimToNull(remark));
      reminderMapper.updateById(reminder);
    }
    return pending.size();
  }

  /** 依赖 (org_id, dedupe_key, is_deleted) 唯一键做幂等，已存在则跳过。 */
  private boolean insertIfAbsent(FinanceReminder reminder) {
    reminder.setTenantId(reminder.getOrgId());
    reminder.setStatus(STATUS_PENDING);
    Long existingCount = Long.valueOf(reminderMapper.selectCount(
        Wrappers.lambdaQuery(FinanceReminder.class)
            .eq(FinanceReminder::getIsDeleted, 0)
            .eq(FinanceReminder::getOrgId, reminder.getOrgId())
            .eq(FinanceReminder::getDedupeKey, reminder.getDedupeKey())));
    if (existingCount > 0) {
      return false;
    }
    try {
      reminderMapper.insert(reminder);
      return true;
    } catch (DuplicateKeyException ignored) {
      // 并发下由唯一键兜底
      return false;
    }
  }

  /** 返回可变 HashMap：Map.of() 的 get(null) 会抛 NPE，而机构级提醒的 elderId 本就为空。 */
  private Map<Long, ElderProfile> loadElders(Set<Long> elderIds) {
    if (elderIds == null || elderIds.isEmpty()) {
      return new java.util.HashMap<>();
    }
    return elderMapper.selectList(
            Wrappers.lambdaQuery(ElderProfile.class).in(ElderProfile::getId, elderIds))
        .stream()
        .collect(Collectors.toMap(
            ElderProfile::getId, Function.identity(), (a, b) -> a, java.util.HashMap::new));
  }

  private FinanceReminderView toView(FinanceReminder reminder, ElderProfile elder) {
    FinanceReminderView view = new FinanceReminderView();
    view.setId(reminder.getId());
    view.setReminderType(reminder.getReminderType());
    view.setReminderTypeText(typeText(reminder.getReminderType()));
    view.setTitle(reminder.getTitle());
    view.setContent(reminder.getContent());
    view.setSeverity(reminder.getSeverity());
    view.setBizMonth(reminder.getBizMonth());
    view.setElderId(reminder.getElderId());
    view.setElderName(elder == null ? null : elder.getFullName());
    view.setRoomId(reminder.getRoomId());
    view.setAmount(reminder.getAmount());
    view.setActionPath(reminder.getActionPath());
    view.setStatus(reminder.getStatus());
    view.setStatusText(STATUS_HANDLED.equals(reminder.getStatus()) ? "已处理" : "未处理");
    view.setHandledAt(reminder.getHandledAt());
    view.setHandleRemark(reminder.getHandleRemark());
    view.setCreateTime(reminder.getCreateTime());
    return view;
  }

  private static String typeText(String type) {
    if (type == null) {
      return "-";
    }
    return switch (type) {
      case TYPE_CARE_FEE -> "下月代养费";
      case TYPE_DEPOSIT -> "押金未缴清";
      case TYPE_ELEC_UNRECORDED -> "电费未登记";
      case TYPE_ELEC_UNPAID -> "电费未缴";
      default -> type;
    };
  }

  private void ensureOrgAccess(Long targetOrgId) {
    if (targetOrgId == null) {
      return;
    }
    Long currentOrgId = AuthContext.getOrgId();
    if (currentOrgId == null || AuthContext.isAdmin()) {
      return;
    }
    if (!Objects.equals(currentOrgId, targetOrgId)) {
      throw new AccessDeniedException("No permission to access another organization");
    }
  }

  private static BigDecimal scale2(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
  }

  private static String normalizeUpper(String value) {
    return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
  }

  private static String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
