package com.zhiyangyun.care.finance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.service.ElderOccupancyReadService;
import com.zhiyangyun.care.finance.entity.FinanceElectricityPrice;
import com.zhiyangyun.care.finance.entity.FinanceElectricityReading;
import com.zhiyangyun.care.finance.mapper.FinanceElectricityPriceMapper;
import com.zhiyangyun.care.finance.mapper.FinanceElectricityReadingMapper;
import com.zhiyangyun.care.finance.model.ElectricityMonthSummary;
import com.zhiyangyun.care.finance.model.ElectricityPriceRequest;
import com.zhiyangyun.care.finance.model.ElectricityReadingRequest;
import com.zhiyangyun.care.finance.model.ElectricityReadingView;
import com.zhiyangyun.care.finance.service.ElectricityFeeService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElectricityFeeServiceImpl implements ElectricityFeeService {
  private static final String SHARE_ROOM = "ROOM";
  private static final String SHARE_PER_RESIDENT = "PER_RESIDENT";
  private static final String PAY_UNPAID = "UNPAID";
  private static final String PAY_PAID = "PAID";

  private final FinanceElectricityPriceMapper priceMapper;
  private final FinanceElectricityReadingMapper readingMapper;
  private final RoomMapper roomMapper;
  private final BedMapper bedMapper;
  private final ElderOccupancyReadService elderOccupancyReadService;

  public ElectricityFeeServiceImpl(FinanceElectricityPriceMapper priceMapper,
      FinanceElectricityReadingMapper readingMapper,
      RoomMapper roomMapper,
      BedMapper bedMapper,
      ElderOccupancyReadService elderOccupancyReadService) {
    this.priceMapper = priceMapper;
    this.readingMapper = readingMapper;
    this.roomMapper = roomMapper;
    this.bedMapper = bedMapper;
    this.elderOccupancyReadService = elderOccupancyReadService;
  }

  @Override
  public List<FinanceElectricityPrice> priceList() {
    Long orgId = AuthContext.getOrgId();
    return priceMapper.selectList(
        Wrappers.lambdaQuery(FinanceElectricityPrice.class)
            .eq(FinanceElectricityPrice::getIsDeleted, 0)
            .eq(orgId != null, FinanceElectricityPrice::getOrgId, orgId)
            .orderByDesc(FinanceElectricityPrice::getEffectiveFrom)
            .orderByDesc(FinanceElectricityPrice::getId));
  }

  @Override
  public BigDecimal resolveUnitPrice(String billMonth) {
    Long orgId = AuthContext.getOrgId();
    YearMonth month = parseMonth(billMonth);
    // 取生效日期不晚于账期最后一天的最近一条电价
    FinanceElectricityPrice price = priceMapper.selectOne(
        Wrappers.lambdaQuery(FinanceElectricityPrice.class)
            .eq(FinanceElectricityPrice::getIsDeleted, 0)
            .eq(orgId != null, FinanceElectricityPrice::getOrgId, orgId)
            .le(FinanceElectricityPrice::getEffectiveFrom, month.atEndOfMonth())
            .orderByDesc(FinanceElectricityPrice::getEffectiveFrom)
            .orderByDesc(FinanceElectricityPrice::getId)
            .last("LIMIT 1"));
    return price == null ? null : price.getUnitPrice();
  }

  @Override
  @Transactional
  public FinanceElectricityPrice savePrice(ElectricityPriceRequest request, Long operatorStaffId) {
    Long orgId = AuthContext.getOrgId();
    if (orgId == null) {
      throw new IllegalStateException("当前登录用户缺少机构信息，无法配置电价");
    }
    FinanceElectricityPrice existing = priceMapper.selectOne(
        Wrappers.lambdaQuery(FinanceElectricityPrice.class)
            .eq(FinanceElectricityPrice::getIsDeleted, 0)
            .eq(FinanceElectricityPrice::getOrgId, orgId)
            .eq(FinanceElectricityPrice::getEffectiveFrom, request.getEffectiveFrom())
            .last("LIMIT 1"));
    FinanceElectricityPrice price = existing == null ? new FinanceElectricityPrice() : existing;
    price.setTenantId(orgId);
    price.setOrgId(orgId);
    price.setUnitPrice(request.getUnitPrice().setScale(4, RoundingMode.HALF_UP));
    price.setEffectiveFrom(request.getEffectiveFrom());
    price.setRemark(trimToNull(request.getRemark()));
    if (price.getId() == null) {
      price.setCreatedBy(operatorStaffId);
      priceMapper.insert(price);
    } else {
      priceMapper.updateById(price);
    }
    return price;
  }

  @Override
  public IPage<ElectricityReadingView> page(
      long pageNo,
      long pageSize,
      String billMonth,
      String building,
      String roomNo,
      String payStatus) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(FinanceElectricityReading.class)
        .eq(FinanceElectricityReading::getIsDeleted, 0)
        .eq(orgId != null, FinanceElectricityReading::getOrgId, orgId);
    String month = trimToNull(billMonth);
    if (month != null) {
      wrapper.eq(FinanceElectricityReading::getBillMonth, month);
    }
    String normalizedBuilding = trimToNull(building);
    if (normalizedBuilding != null) {
      wrapper.eq(FinanceElectricityReading::getBuilding, normalizedBuilding);
    }
    String normalizedRoomNo = trimToNull(roomNo);
    if (normalizedRoomNo != null) {
      wrapper.like(FinanceElectricityReading::getRoomNo, normalizedRoomNo);
    }
    String normalizedPayStatus = normalizeUpper(payStatus);
    if (PAY_PAID.equals(normalizedPayStatus) || PAY_UNPAID.equals(normalizedPayStatus)) {
      wrapper.eq(FinanceElectricityReading::getPayStatus, normalizedPayStatus);
    }
    wrapper.orderByAsc(FinanceElectricityReading::getBuilding)
        .orderByAsc(FinanceElectricityReading::getFloorNo)
        .orderByAsc(FinanceElectricityReading::getRoomNo);

    IPage<FinanceElectricityReading> page =
        readingMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    IPage<ElectricityReadingView> viewPage = new Page<>(pageNo, pageSize);
    viewPage.setTotal(page.getTotal());
    viewPage.setRecords(page.getRecords().stream().map(this::toView).toList());
    return viewPage;
  }

  @Override
  @Transactional
  public ElectricityReadingView saveReading(ElectricityReadingRequest request, Long operatorStaffId) {
    Long orgId = AuthContext.getOrgId();
    String month = parseMonth(request.getBillMonth()).toString();
    Room room = roomMapper.selectById(request.getRoomId());
    if (room == null || Integer.valueOf(1).equals(room.getIsDeleted())) {
      throw new IllegalArgumentException("房间不存在");
    }
    ensureOrgAccess(room.getOrgId());

    BigDecimal previous = request.getPreviousReading() == null
        ? suggestPreviousReading(month, room.getId())
        : scale2(request.getPreviousReading());
    BigDecimal current = scale2(request.getCurrentReading());
    if (previous.compareTo(BigDecimal.ZERO) < 0 || current.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("读数不能为负数");
    }
    if (current.compareTo(previous) < 0) {
      throw new IllegalArgumentException("本期读数不能小于上期读数，请核对抄表数据");
    }
    BigDecimal unitPrice = resolveUnitPrice(month);
    if (unitPrice == null) {
      throw new IllegalStateException("尚未配置电价，请先在电费管理里设置元/度单价");
    }

    BigDecimal usage = current.subtract(previous);
    BigDecimal fee = usage.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    String shareMode = SHARE_PER_RESIDENT.equals(normalizeUpper(request.getShareMode()))
        ? SHARE_PER_RESIDENT
        : SHARE_ROOM;
    int residentCount = countResidents(room);
    BigDecimal perResident = BigDecimal.ZERO;
    if (SHARE_PER_RESIDENT.equals(shareMode)) {
      if (residentCount <= 0) {
        throw new IllegalStateException("该房间当前没有在住长者，无法按人数均摊，请改用整间计费");
      }
      perResident = fee.divide(BigDecimal.valueOf(residentCount), 2, RoundingMode.HALF_UP);
    }

    FinanceElectricityReading existing = readingMapper.selectOne(
        Wrappers.lambdaQuery(FinanceElectricityReading.class)
            .eq(FinanceElectricityReading::getIsDeleted, 0)
            .eq(orgId != null, FinanceElectricityReading::getOrgId, orgId)
            .eq(FinanceElectricityReading::getBillMonth, month)
            .eq(FinanceElectricityReading::getRoomId, room.getId())
            .last("LIMIT 1"));
    FinanceElectricityReading reading = existing == null ? new FinanceElectricityReading() : existing;
    if (existing != null && PAY_PAID.equals(existing.getPayStatus())) {
      throw new IllegalStateException("该房间本月电费已标记缴费，请先改回未缴再修改读数");
    }
    reading.setTenantId(room.getOrgId());
    reading.setOrgId(room.getOrgId());
    reading.setBillMonth(month);
    reading.setRoomId(room.getId());
    reading.setBuilding(room.getBuilding());
    reading.setFloorNo(room.getFloorNo());
    reading.setRoomNo(room.getRoomNo());
    reading.setPreviousReading(previous);
    reading.setCurrentReading(current);
    reading.setUsageAmount(usage);
    reading.setUnitPrice(unitPrice.setScale(4, RoundingMode.HALF_UP));
    reading.setFeeAmount(fee);
    reading.setShareMode(shareMode);
    reading.setResidentCount(residentCount);
    reading.setPerResidentAmount(perResident);
    reading.setRemark(trimToNull(request.getRemark()));
    if (reading.getId() == null) {
      reading.setPayStatus(PAY_UNPAID);
      reading.setCreatedBy(operatorStaffId);
      readingMapper.insert(reading);
    } else {
      readingMapper.updateById(reading);
    }
    return toView(reading);
  }

  @Override
  public BigDecimal suggestPreviousReading(String billMonth, Long roomId) {
    if (roomId == null) {
      return BigDecimal.ZERO;
    }
    Long orgId = AuthContext.getOrgId();
    String previousMonth = parseMonth(billMonth).minusMonths(1).toString();
    FinanceElectricityReading last = readingMapper.selectOne(
        Wrappers.lambdaQuery(FinanceElectricityReading.class)
            .eq(FinanceElectricityReading::getIsDeleted, 0)
            .eq(orgId != null, FinanceElectricityReading::getOrgId, orgId)
            .eq(FinanceElectricityReading::getRoomId, roomId)
            .eq(FinanceElectricityReading::getBillMonth, previousMonth)
            .last("LIMIT 1"));
    return last == null ? BigDecimal.ZERO : scale2(last.getCurrentReading());
  }

  @Override
  @Transactional
  public ElectricityReadingView updatePayStatus(
      Long readingId, boolean paid, String payRemark, Long operatorStaffId) {
    FinanceElectricityReading reading = readingId == null ? null : readingMapper.selectOne(
        Wrappers.lambdaQuery(FinanceElectricityReading.class)
            .eq(FinanceElectricityReading::getId, readingId)
            .eq(FinanceElectricityReading::getIsDeleted, 0)
            .last("LIMIT 1 FOR UPDATE"));
    if (reading == null) {
      throw new IllegalArgumentException("电费记录不存在");
    }
    ensureOrgAccess(reading.getOrgId());
    reading.setPayStatus(paid ? PAY_PAID : PAY_UNPAID);
    reading.setPaidAt(paid ? LocalDateTime.now() : null);
    reading.setPaidBy(paid ? operatorStaffId : null);
    reading.setPayRemark(trimToNull(payRemark));
    readingMapper.updateById(reading);
    return toView(reading);
  }

  @Override
  public ElectricityMonthSummary summary(String billMonth) {
    Long orgId = AuthContext.getOrgId();
    String month = parseMonth(billMonth).toString();
    ElectricityMonthSummary summary = new ElectricityMonthSummary();
    summary.setBillMonth(month);
    BigDecimal unitPrice = resolveUnitPrice(month);
    summary.setUnitPrice(unitPrice == null ? BigDecimal.ZERO : unitPrice);

    long roomCount = roomMapper.selectCount(
        Wrappers.lambdaQuery(Room.class)
            .eq(Room::getIsDeleted, 0)
            .eq(orgId != null, Room::getOrgId, orgId));
    summary.setRoomCount((int) roomCount);

    List<FinanceElectricityReading> rows = readingMapper.selectList(
        Wrappers.lambdaQuery(FinanceElectricityReading.class)
            .eq(FinanceElectricityReading::getIsDeleted, 0)
            .eq(orgId != null, FinanceElectricityReading::getOrgId, orgId)
            .eq(FinanceElectricityReading::getBillMonth, month));
    summary.setRecordedRoomCount(rows.size());
    summary.setUnrecordedRoomCount(Math.max(0, (int) roomCount - rows.size()));
    BigDecimal totalUsage = BigDecimal.ZERO;
    BigDecimal totalFee = BigDecimal.ZERO;
    BigDecimal unpaidFee = BigDecimal.ZERO;
    int unpaidRooms = 0;
    for (FinanceElectricityReading row : rows) {
      totalUsage = totalUsage.add(scale2(row.getUsageAmount()));
      totalFee = totalFee.add(scale2(row.getFeeAmount()));
      if (!PAY_PAID.equals(row.getPayStatus())) {
        unpaidRooms += 1;
        unpaidFee = unpaidFee.add(scale2(row.getFeeAmount()));
      }
    }
    summary.setTotalUsage(totalUsage);
    summary.setTotalFee(totalFee);
    summary.setUnpaidFee(unpaidFee);
    summary.setUnpaidRoomCount(unpaidRooms);
    return summary;
  }

  private int countResidents(Room room) {
    Long orgId = room.getOrgId();
    List<Bed> beds = bedMapper.selectList(
        Wrappers.lambdaQuery(Bed.class)
            .eq(Bed::getIsDeleted, 0)
            .eq(orgId != null, Bed::getOrgId, orgId)
            .eq(Bed::getRoomId, room.getId()));
    if (beds.isEmpty()) {
      return 0;
    }
    return elderOccupancyReadService.buildOccupiedBedMapByElderId(orgId, beds).size();
  }

  private ElectricityReadingView toView(FinanceElectricityReading reading) {
    ElectricityReadingView view = new ElectricityReadingView();
    view.setId(reading.getId());
    view.setBillMonth(reading.getBillMonth());
    view.setRoomId(reading.getRoomId());
    view.setBuilding(reading.getBuilding());
    view.setFloorNo(reading.getFloorNo());
    view.setRoomNo(reading.getRoomNo());
    view.setPreviousReading(scale2(reading.getPreviousReading()));
    view.setCurrentReading(scale2(reading.getCurrentReading()));
    view.setUsageAmount(scale2(reading.getUsageAmount()));
    view.setUnitPrice(reading.getUnitPrice());
    view.setFeeAmount(scale2(reading.getFeeAmount()));
    view.setShareMode(reading.getShareMode());
    view.setShareModeText(SHARE_PER_RESIDENT.equals(reading.getShareMode()) ? "按在住人数均摊" : "整间计费");
    view.setResidentCount(reading.getResidentCount());
    view.setPerResidentAmount(scale2(reading.getPerResidentAmount()));
    view.setPayStatus(reading.getPayStatus());
    view.setPayStatusText(PAY_PAID.equals(reading.getPayStatus()) ? "已缴" : "未缴");
    view.setPaidAt(reading.getPaidAt());
    view.setPayRemark(reading.getPayRemark());
    view.setRemark(reading.getRemark());
    return view;
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

  private static YearMonth parseMonth(String billMonth) {
    if (billMonth == null || billMonth.isBlank()) {
      return YearMonth.now();
    }
    try {
      return YearMonth.parse(billMonth.trim());
    } catch (Exception ignored) {
      throw new IllegalArgumentException("账期格式应为 yyyy-MM");
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
