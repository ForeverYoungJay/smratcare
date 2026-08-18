package com.zhiyangyun.care.finance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.finance.entity.FinanceDepositAccount;
import com.zhiyangyun.care.finance.entity.FinanceDepositStandard;
import com.zhiyangyun.care.finance.entity.FinanceDepositTransaction;
import com.zhiyangyun.care.finance.mapper.FinanceDepositAccountMapper;
import com.zhiyangyun.care.finance.mapper.FinanceDepositStandardMapper;
import com.zhiyangyun.care.finance.mapper.FinanceDepositTransactionMapper;
import com.zhiyangyun.care.finance.model.DepositAccountView;
import com.zhiyangyun.care.finance.model.DepositStandardRequest;
import com.zhiyangyun.care.finance.model.DepositSummary;
import com.zhiyangyun.care.finance.model.DepositTransactionRequest;
import com.zhiyangyun.care.finance.model.DepositTransactionView;
import com.zhiyangyun.care.finance.service.DepositService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositServiceImpl implements DepositService {
  private static final String TXN_PAY = "PAY";
  private static final String TXN_DEDUCT = "DEDUCT";
  private static final String TXN_REFUND = "REFUND";
  private static final String STATUS_UNPAID = "UNPAID";
  private static final String STATUS_PARTIAL = "PARTIAL";
  private static final String STATUS_PAID = "PAID";
  private static final String STATUS_CLOSED = "CLOSED";

  private final FinanceDepositStandardMapper standardMapper;
  private final FinanceDepositAccountMapper accountMapper;
  private final FinanceDepositTransactionMapper transactionMapper;
  private final ElderMapper elderMapper;
  private final BedMapper bedMapper;
  private final RoomMapper roomMapper;

  public DepositServiceImpl(FinanceDepositStandardMapper standardMapper,
      FinanceDepositAccountMapper accountMapper,
      FinanceDepositTransactionMapper transactionMapper,
      ElderMapper elderMapper,
      BedMapper bedMapper,
      RoomMapper roomMapper) {
    this.standardMapper = standardMapper;
    this.accountMapper = accountMapper;
    this.transactionMapper = transactionMapper;
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.roomMapper = roomMapper;
  }

  @Override
  public List<FinanceDepositStandard> standardList() {
    Long orgId = AuthContext.getOrgId();
    return standardMapper.selectList(
        Wrappers.lambdaQuery(FinanceDepositStandard.class)
            .eq(FinanceDepositStandard::getIsDeleted, 0)
            .eq(orgId != null, FinanceDepositStandard::getOrgId, orgId)
            .orderByAsc(FinanceDepositStandard::getCareLevel)
            .orderByDesc(FinanceDepositStandard::getEffectiveFrom));
  }

  @Override
  @Transactional
  public FinanceDepositStandard saveStandard(DepositStandardRequest request, Long operatorStaffId) {
    Long orgId = AuthContext.getOrgId();
    if (orgId == null) {
      throw new IllegalStateException("当前登录用户缺少机构信息，无法配置押金标准");
    }
    FinanceDepositStandard standard = request.getId() == null
        ? new FinanceDepositStandard()
        : standardMapper.selectById(request.getId());
    if (standard == null) {
      throw new IllegalArgumentException("押金标准不存在");
    }
    if (standard.getId() != null) {
      ensureOrgAccess(standard.getOrgId());
    }
    standard.setTenantId(orgId);
    standard.setOrgId(orgId);
    standard.setStandardName(request.getStandardName().trim());
    standard.setCareLevel(trimToNull(request.getCareLevel()));
    standard.setAmount(scale2(request.getAmount()));
    standard.setEffectiveFrom(request.getEffectiveFrom());
    standard.setRemark(trimToNull(request.getRemark()));
    if (standard.getId() == null) {
      standard.setCreatedBy(operatorStaffId);
      standardMapper.insert(standard);
    } else {
      standardMapper.updateById(standard);
    }
    return standard;
  }

  @Override
  @Transactional
  public void deleteStandard(Long standardId) {
    FinanceDepositStandard standard = standardId == null ? null : standardMapper.selectById(standardId);
    if (standard == null) {
      return;
    }
    ensureOrgAccess(standard.getOrgId());
    standardMapper.deleteById(standardId);
  }

  @Override
  public BigDecimal resolveStandardAmount(String careLevel) {
    Long orgId = AuthContext.getOrgId();
    LocalDate today = LocalDate.now();
    String normalizedLevel = trimToNull(careLevel);
    if (normalizedLevel != null) {
      FinanceDepositStandard matched = standardMapper.selectOne(
          Wrappers.lambdaQuery(FinanceDepositStandard.class)
              .eq(FinanceDepositStandard::getIsDeleted, 0)
              .eq(orgId != null, FinanceDepositStandard::getOrgId, orgId)
              .eq(FinanceDepositStandard::getCareLevel, normalizedLevel)
              .le(FinanceDepositStandard::getEffectiveFrom, today)
              .orderByDesc(FinanceDepositStandard::getEffectiveFrom)
              .orderByDesc(FinanceDepositStandard::getId)
              .last("LIMIT 1"));
      if (matched != null) {
        return scale2(matched.getAmount());
      }
    }
    // 回落到通用默认标准
    FinanceDepositStandard fallback = standardMapper.selectOne(
        Wrappers.lambdaQuery(FinanceDepositStandard.class)
            .eq(FinanceDepositStandard::getIsDeleted, 0)
            .eq(orgId != null, FinanceDepositStandard::getOrgId, orgId)
            .isNull(FinanceDepositStandard::getCareLevel)
            .le(FinanceDepositStandard::getEffectiveFrom, today)
            .orderByDesc(FinanceDepositStandard::getEffectiveFrom)
            .orderByDesc(FinanceDepositStandard::getId)
            .last("LIMIT 1"));
    return fallback == null ? BigDecimal.ZERO : scale2(fallback.getAmount());
  }

  @Override
  public IPage<DepositAccountView> page(long pageNo, long pageSize, String status, String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(FinanceDepositAccount.class)
        .eq(FinanceDepositAccount::getIsDeleted, 0)
        .eq(orgId != null, FinanceDepositAccount::getOrgId, orgId);
    String normalizedStatus = normalizeUpper(status);
    if (Set.of(STATUS_UNPAID, STATUS_PARTIAL, STATUS_PAID, STATUS_CLOSED).contains(normalizedStatus)) {
      wrapper.eq(FinanceDepositAccount::getStatus, normalizedStatus);
    }
    String normalizedKeyword = trimToNull(keyword);
    if (normalizedKeyword != null) {
      List<Long> elderIds = elderMapper.selectList(
              Wrappers.lambdaQuery(ElderProfile.class)
                  .eq(ElderProfile::getIsDeleted, 0)
                  .eq(orgId != null, ElderProfile::getOrgId, orgId)
                  .like(ElderProfile::getFullName, normalizedKeyword))
          .stream().map(ElderProfile::getId).toList();
      if (elderIds.isEmpty()) {
        return new Page<>(pageNo, pageSize, 0);
      }
      wrapper.in(FinanceDepositAccount::getElderId, elderIds);
    }
    wrapper.orderByDesc(FinanceDepositAccount::getUpdateTime).orderByDesc(FinanceDepositAccount::getId);

    IPage<FinanceDepositAccount> page = accountMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, ElderProfile> elderMap = loadElders(page.getRecords().stream()
        .map(FinanceDepositAccount::getElderId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet()));
    Map<Long, String[]> bedRoomMap = loadBedRoom(elderMap.values());

    IPage<DepositAccountView> viewPage = new Page<>(pageNo, pageSize);
    viewPage.setTotal(page.getTotal());
    viewPage.setRecords(page.getRecords().stream()
        .map(item -> toView(item, elderMap.get(item.getElderId()), bedRoomMap))
        .toList());
    return viewPage;
  }

  @Override
  public DepositAccountView detail(Long elderId) {
    FinanceDepositAccount account = findAccount(elderId);
    if (account == null) {
      return null;
    }
    Map<Long, ElderProfile> elderMap = loadElders(Set.of(elderId));
    return toView(account, elderMap.get(elderId), loadBedRoom(elderMap.values()));
  }

  @Override
  public List<DepositTransactionView> transactions(Long elderId) {
    Long orgId = AuthContext.getOrgId();
    if (elderId == null) {
      return List.of();
    }
    List<FinanceDepositTransaction> rows = transactionMapper.selectList(
        Wrappers.lambdaQuery(FinanceDepositTransaction.class)
            .eq(FinanceDepositTransaction::getIsDeleted, 0)
            .eq(orgId != null, FinanceDepositTransaction::getOrgId, orgId)
            .eq(FinanceDepositTransaction::getElderId, elderId)
            .orderByDesc(FinanceDepositTransaction::getOccurredAt)
            .orderByDesc(FinanceDepositTransaction::getId));
    Map<Long, ElderProfile> elderMap = loadElders(Set.of(elderId));
    ElderProfile elder = elderMap.get(elderId);
    return rows.stream().map(row -> {
      DepositTransactionView view = new DepositTransactionView();
      view.setId(row.getId());
      view.setElderId(row.getElderId());
      view.setElderName(elder == null ? null : elder.getFullName());
      view.setTxnType(row.getTxnType());
      view.setTxnTypeText(txnTypeText(row.getTxnType()));
      view.setAmount(scale2(row.getAmount()));
      view.setBalanceAfter(scale2(row.getBalanceAfter()));
      view.setPayMethod(row.getPayMethod());
      view.setOccurredAt(row.getOccurredAt());
      view.setReason(row.getReason());
      view.setRemark(row.getRemark());
      return view;
    }).toList();
  }

  @Override
  @Transactional
  public DepositAccountView registerPay(DepositTransactionRequest request, Long operatorStaffId) {
    return register(request, TXN_PAY, operatorStaffId);
  }

  @Override
  @Transactional
  public DepositAccountView registerDeduct(DepositTransactionRequest request, Long operatorStaffId) {
    return register(request, TXN_DEDUCT, operatorStaffId);
  }

  @Override
  @Transactional
  public DepositAccountView registerRefund(DepositTransactionRequest request, Long operatorStaffId) {
    return register(request, TXN_REFUND, operatorStaffId);
  }

  @Override
  @Transactional
  public DepositAccountView refreshStandard(Long elderId) {
    ElderProfile elder = loadElderForWrite(elderId);
    FinanceDepositAccount account = ensureAccount(elder);
    account.setStandardAmount(resolveStandardAmount(elder.getCareLevel()));
    recalculate(account);
    accountMapper.updateById(account);
    return toView(account, elder, loadBedRoom(List.of(elder)));
  }

  @Override
  public DepositSummary summary() {
    Long orgId = AuthContext.getOrgId();
    List<FinanceDepositAccount> rows = accountMapper.selectList(
        Wrappers.lambdaQuery(FinanceDepositAccount.class)
            .eq(FinanceDepositAccount::getIsDeleted, 0)
            .eq(orgId != null, FinanceDepositAccount::getOrgId, orgId));
    DepositSummary summary = new DepositSummary();
    summary.setAccountCount(rows.size());
    BigDecimal shortfall = BigDecimal.ZERO;
    BigDecimal balance = BigDecimal.ZERO;
    BigDecimal paid = BigDecimal.ZERO;
    BigDecimal deducted = BigDecimal.ZERO;
    BigDecimal refunded = BigDecimal.ZERO;
    int shortfallCount = 0;
    for (FinanceDepositAccount row : rows) {
      BigDecimal gap = scale2(row.getStandardAmount()).subtract(scale2(row.getPaidAmount()));
      if (gap.compareTo(BigDecimal.ZERO) > 0 && !STATUS_CLOSED.equals(row.getStatus())) {
        shortfallCount += 1;
        shortfall = shortfall.add(gap);
      }
      balance = balance.add(scale2(row.getBalanceAmount()));
      paid = paid.add(scale2(row.getPaidAmount()));
      deducted = deducted.add(scale2(row.getDeductedAmount()));
      refunded = refunded.add(scale2(row.getRefundedAmount()));
    }
    summary.setShortfallCount(shortfallCount);
    summary.setShortfallAmount(shortfall);
    summary.setTotalBalance(balance);
    summary.setTotalPaid(paid);
    summary.setTotalDeducted(deducted);
    summary.setTotalRefunded(refunded);
    return summary;
  }

  private DepositAccountView register(DepositTransactionRequest request, String txnType, Long operatorStaffId) {
    BigDecimal amount = scale2(request.getAmount());
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("金额必须大于 0");
    }
    if (!TXN_PAY.equals(txnType) && trimToNull(request.getReason()) == null) {
      throw new IllegalArgumentException(TXN_DEDUCT.equals(txnType) ? "扣款必须填写原因" : "退还必须填写原因");
    }
    ElderProfile elder = loadElderForWrite(request.getElderId());
    FinanceDepositAccount account = ensureAccount(elder);

    BigDecimal balance = scale2(account.getBalanceAmount());
    if (TXN_PAY.equals(txnType)) {
      account.setPaidAmount(scale2(account.getPaidAmount()).add(amount));
    } else {
      if (amount.compareTo(balance) > 0) {
        throw new IllegalStateException(
            (TXN_DEDUCT.equals(txnType) ? "扣款" : "退还") + "金额超过在押余额 " + balance + " 元");
      }
      if (TXN_DEDUCT.equals(txnType)) {
        account.setDeductedAmount(scale2(account.getDeductedAmount()).add(amount));
      } else {
        account.setRefundedAmount(scale2(account.getRefundedAmount()).add(amount));
      }
    }
    recalculate(account);
    account.setLastOpAt(LocalDateTime.now());
    accountMapper.updateById(account);

    FinanceDepositTransaction txn = new FinanceDepositTransaction();
    txn.setTenantId(account.getOrgId());
    txn.setOrgId(account.getOrgId());
    txn.setDepositAccountId(account.getId());
    txn.setElderId(elder.getId());
    txn.setTxnType(txnType);
    txn.setAmount(amount);
    txn.setBalanceAfter(scale2(account.getBalanceAmount()));
    txn.setPayMethod(trimToNull(request.getPayMethod()));
    txn.setOccurredAt(request.getOccurredAt() == null ? LocalDateTime.now() : request.getOccurredAt());
    txn.setReason(trimToNull(request.getReason()));
    txn.setRemark(trimToNull(request.getRemark()));
    txn.setOperatorStaffId(operatorStaffId);
    transactionMapper.insert(txn);

    return toView(account, elder, loadBedRoom(List.of(elder)));
  }

  private FinanceDepositAccount ensureAccount(ElderProfile elder) {
    FinanceDepositAccount account = accountMapper.selectOne(
        Wrappers.lambdaQuery(FinanceDepositAccount.class)
            .eq(FinanceDepositAccount::getIsDeleted, 0)
            .eq(FinanceDepositAccount::getElderId, elder.getId())
            .last("LIMIT 1 FOR UPDATE"));
    if (account != null) {
      ensureOrgAccess(account.getOrgId());
      return account;
    }
    account = new FinanceDepositAccount();
    account.setTenantId(elder.getOrgId());
    account.setOrgId(elder.getOrgId());
    account.setElderId(elder.getId());
    account.setStandardAmount(resolveStandardAmount(elder.getCareLevel()));
    account.setPaidAmount(BigDecimal.ZERO);
    account.setDeductedAmount(BigDecimal.ZERO);
    account.setRefundedAmount(BigDecimal.ZERO);
    account.setBalanceAmount(BigDecimal.ZERO);
    account.setStatus(STATUS_UNPAID);
    accountMapper.insert(account);
    return account;
  }

  private void recalculate(FinanceDepositAccount account) {
    BigDecimal paid = scale2(account.getPaidAmount());
    BigDecimal deducted = scale2(account.getDeductedAmount());
    BigDecimal refunded = scale2(account.getRefundedAmount());
    BigDecimal standard = scale2(account.getStandardAmount());
    BigDecimal balance = paid.subtract(deducted).subtract(refunded);
    account.setPaidAmount(paid);
    account.setDeductedAmount(deducted);
    account.setRefundedAmount(refunded);
    account.setBalanceAmount(balance);
    if (paid.compareTo(BigDecimal.ZERO) <= 0) {
      account.setStatus(STATUS_UNPAID);
      return;
    }
    // 缴过押金但已退/扣干净，视为结清退出
    if (balance.compareTo(BigDecimal.ZERO) <= 0
        && refunded.add(deducted).compareTo(BigDecimal.ZERO) > 0) {
      account.setStatus(STATUS_CLOSED);
      return;
    }
    account.setStatus(paid.compareTo(standard) >= 0 ? STATUS_PAID : STATUS_PARTIAL);
  }

  private ElderProfile loadElderForWrite(Long elderId) {
    ElderProfile elder = elderId == null ? null : elderMapper.selectById(elderId);
    if (elder == null || Integer.valueOf(1).equals(elder.getIsDeleted())) {
      throw new IllegalArgumentException("长者不存在");
    }
    ensureOrgAccess(elder.getOrgId());
    return elder;
  }

  private FinanceDepositAccount findAccount(Long elderId) {
    if (elderId == null) {
      return null;
    }
    Long orgId = AuthContext.getOrgId();
    return accountMapper.selectOne(
        Wrappers.lambdaQuery(FinanceDepositAccount.class)
            .eq(FinanceDepositAccount::getIsDeleted, 0)
            .eq(orgId != null, FinanceDepositAccount::getOrgId, orgId)
            .eq(FinanceDepositAccount::getElderId, elderId)
            .last("LIMIT 1"));
  }

  private Map<Long, ElderProfile> loadElders(Set<Long> elderIds) {
    if (elderIds == null || elderIds.isEmpty()) {
      return Map.of();
    }
    return elderMapper.selectList(
            Wrappers.lambdaQuery(ElderProfile.class).in(ElderProfile::getId, elderIds))
        .stream()
        .collect(Collectors.toMap(ElderProfile::getId, Function.identity(), (a, b) -> a));
  }

  /** elderId -> [roomNo, bedNo]。 */
  private Map<Long, String[]> loadBedRoom(java.util.Collection<ElderProfile> elders) {
    Map<Long, String[]> result = new HashMap<>();
    if (elders == null || elders.isEmpty()) {
      return result;
    }
    List<Long> bedIds = elders.stream()
        .map(ElderProfile::getBedId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    if (bedIds.isEmpty()) {
      return result;
    }
    Map<Long, Bed> bedMap = bedMapper.selectList(
            Wrappers.lambdaQuery(Bed.class).in(Bed::getId, bedIds))
        .stream()
        .collect(Collectors.toMap(Bed::getId, Function.identity(), (a, b) -> a));
    List<Long> roomIds = bedMap.values().stream()
        .map(Bed::getRoomId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Room> roomMap = roomIds.isEmpty()
        ? Map.of()
        : roomMapper.selectList(Wrappers.lambdaQuery(Room.class).in(Room::getId, roomIds))
            .stream()
            .collect(Collectors.toMap(Room::getId, Function.identity(), (a, b) -> a));
    for (ElderProfile elder : elders) {
      Bed bed = elder.getBedId() == null ? null : bedMap.get(elder.getBedId());
      Room room = bed == null || bed.getRoomId() == null ? null : roomMap.get(bed.getRoomId());
      result.put(elder.getId(), new String[] {
          room == null ? null : room.getRoomNo(),
          bed == null ? null : bed.getBedNo()
      });
    }
    return result;
  }

  private DepositAccountView toView(
      FinanceDepositAccount account, ElderProfile elder, Map<Long, String[]> bedRoomMap) {
    DepositAccountView view = new DepositAccountView();
    view.setId(account.getId());
    view.setElderId(account.getElderId());
    view.setElderName(elder == null ? null : elder.getFullName());
    view.setCareLevel(elder == null ? null : elder.getCareLevel());
    String[] bedRoom = bedRoomMap == null ? null : bedRoomMap.get(account.getElderId());
    view.setRoomNo(bedRoom == null ? null : bedRoom[0]);
    view.setBedNo(bedRoom == null ? null : bedRoom[1]);
    view.setStandardAmount(scale2(account.getStandardAmount()));
    view.setPaidAmount(scale2(account.getPaidAmount()));
    view.setDeductedAmount(scale2(account.getDeductedAmount()));
    view.setRefundedAmount(scale2(account.getRefundedAmount()));
    view.setBalanceAmount(scale2(account.getBalanceAmount()));
    BigDecimal gap = scale2(account.getStandardAmount()).subtract(scale2(account.getPaidAmount()));
    view.setShortfallAmount(gap.max(BigDecimal.ZERO));
    view.setStatus(account.getStatus());
    view.setStatusText(statusText(account.getStatus()));
    view.setLastOpAt(account.getLastOpAt());
    view.setRemark(account.getRemark());
    return view;
  }

  private static String statusText(String status) {
    if (status == null) {
      return "-";
    }
    return switch (status) {
      case STATUS_UNPAID -> "未缴";
      case STATUS_PARTIAL -> "部分缴";
      case STATUS_PAID -> "已缴清";
      case STATUS_CLOSED -> "已结清";
      default -> status;
    };
  }

  private static String txnTypeText(String txnType) {
    if (txnType == null) {
      return "-";
    }
    return switch (txnType) {
      case TXN_PAY -> "缴纳";
      case TXN_DEDUCT -> "扣款";
      case TXN_REFUND -> "退还";
      default -> txnType;
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
