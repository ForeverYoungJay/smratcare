package com.zhiyangyun.care.cockpit.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.Org;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.OrgMapper;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.cockpit.entity.CockpitMetricSnapshot;
import com.zhiyangyun.care.cockpit.mapper.CockpitMetricSnapshotMapper;
import com.zhiyangyun.care.cockpit.model.CockpitOverviewResponse;
import com.zhiyangyun.care.cockpit.model.CockpitTrendItem;
import com.zhiyangyun.care.cockpit.service.CockpitMetricService;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.model.ElderLifecycleStatus;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.mapper.SmartAlertMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CockpitMetricServiceImpl implements CockpitMetricService {

  private final CockpitMetricSnapshotMapper snapshotMapper;
  private final BedMapper bedMapper;
  private final ElderMapper elderMapper;
  private final BillMonthlyMapper billMonthlyMapper;
  private final SmartAlertMapper smartAlertMapper;
  private final OrgMapper orgMapper;
  private final StaffMapper staffAccountMapper;

  public CockpitMetricServiceImpl(CockpitMetricSnapshotMapper snapshotMapper,
      BedMapper bedMapper, ElderMapper elderMapper, BillMonthlyMapper billMonthlyMapper,
      SmartAlertMapper smartAlertMapper, OrgMapper orgMapper, StaffMapper staffAccountMapper) {
    this.snapshotMapper = snapshotMapper;
    this.bedMapper = bedMapper;
    this.elderMapper = elderMapper;
    this.billMonthlyMapper = billMonthlyMapper;
    this.smartAlertMapper = smartAlertMapper;
    this.orgMapper = orgMapper;
    this.staffAccountMapper = staffAccountMapper;
  }

  @Override
  @Transactional
  public CockpitMetricSnapshot snapshot(Long orgId, LocalDate statDate) {
    if (orgId == null) {
      return null;
    }
    LocalDate date = statDate == null ? LocalDate.now() : statDate;
    LocalDateTime dayStart = date.atStartOfDay();
    LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

    // 床位与入住
    List<Bed> beds = bedMapper.selectList(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(Bed::getOrgId, orgId));
    int totalBeds = beds.size();
    int occupiedBeds = (int) beds.stream().filter(b -> b.getElderId() != null).count();
    int availableBeds = Math.max(0, totalBeds - occupiedBeds);
    BigDecimal occupancyRate = rate(occupiedBeds, totalBeds);

    int residentCount = Math.toIntExact(elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getStatus, 1)));

    int admissions = Math.toIntExact(elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getAdmissionDate, date)));

    int discharges = Math.toIntExact(elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(ElderProfile::getOrgId, orgId)
        .in(ElderProfile::getLifecycleStatus, ElderLifecycleStatus.DISCHARGED, ElderLifecycleStatus.DECEASED)
        .ge(ElderProfile::getLifecycleUpdatedAt, dayStart)
        .lt(ElderProfile::getLifecycleUpdatedAt, dayEnd)));

    // 营收与回款（按当日所属自然月账单口径）
    String billMonth = YearMonth.from(date).toString();
    List<BillMonthly> bills = billMonthlyMapper.selectList(Wrappers.lambdaQuery(BillMonthly.class)
        .eq(BillMonthly::getIsDeleted, 0)
        .eq(BillMonthly::getOrgId, orgId)
        .eq(BillMonthly::getBillMonth, billMonth));
    BigDecimal revenue = BigDecimal.ZERO;
    BigDecimal paid = BigDecimal.ZERO;
    BigDecimal outstanding = BigDecimal.ZERO;
    for (BillMonthly bill : bills) {
      revenue = revenue.add(nvl(bill.getTotalAmount()));
      paid = paid.add(nvl(bill.getPaidAmount()));
      outstanding = outstanding.add(nvl(bill.getOutstandingAmount()));
    }
    BigDecimal collectionRate = rate(paid, revenue);

    // 安全告警（当日触发）
    List<SmartAlert> alerts = smartAlertMapper.selectList(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(SmartAlert::getOrgId, orgId)
        .ge(SmartAlert::getFirstTriggeredAt, dayStart)
        .lt(SmartAlert::getFirstTriggeredAt, dayEnd));
    int alertTotal = alerts.size();
    int alertResolved = (int) alerts.stream().filter(a -> "RESOLVED".equalsIgnoreCase(a.getStatus())).count();
    int alertResponded = (int) alerts.stream().filter(a -> !"OPEN".equalsIgnoreCase(a.getStatus())).count();
    int alertTimeout = (int) alerts.stream()
        .filter(a -> a.getEscalationCount() != null && a.getEscalationCount() > 0).count();
    BigDecimal alertRespRate = rate(alertResponded, alertTotal);

    CockpitMetricSnapshot existing = snapshotMapper.selectOne(Wrappers.lambdaQuery(CockpitMetricSnapshot.class)
        .eq(CockpitMetricSnapshot::getIsDeleted, 0)
        .eq(CockpitMetricSnapshot::getOrgId, orgId)
        .eq(CockpitMetricSnapshot::getStatDate, date));
    CockpitMetricSnapshot snap = existing == null ? new CockpitMetricSnapshot() : existing;
    snap.setOrgId(orgId);
    snap.setStatDate(date);
    snap.setTotalBeds(totalBeds);
    snap.setOccupiedBeds(occupiedBeds);
    snap.setAvailableBeds(availableBeds);
    snap.setOccupancyRate(occupancyRate);
    snap.setResidentCount(residentCount);
    snap.setAdmissions(admissions);
    snap.setDischarges(discharges);
    snap.setNetChange(admissions - discharges);
    snap.setRevenueAmount(revenue);
    snap.setPaidAmount(paid);
    snap.setOutstandingAmount(outstanding);
    snap.setCollectionRate(collectionRate);
    snap.setAlertTotal(alertTotal);
    snap.setAlertResolved(alertResolved);
    snap.setAlertTimeout(alertTimeout);
    snap.setAlertRespRate(alertRespRate);
    if (existing == null) {
      snap.setIsDeleted(0);
      snapshotMapper.insert(snap);
    } else {
      snapshotMapper.updateById(snap);
    }
    return snap;
  }

  @Override
  public int snapshotAllOrgsToday() {
    List<Org> orgs = orgMapper.selectList(Wrappers.lambdaQuery(Org.class)
        .eq(Org::getIsDeleted, 0));
    LocalDate today = LocalDate.now();
    int count = 0;
    for (Org org : orgs) {
      if (org.getId() == null) {
        continue;
      }
      snapshot(org.getId(), today);
      count++;
    }
    return count;
  }

  @Override
  public CockpitOverviewResponse overview(Long orgId, int trendDays) {
    int days = trendDays <= 0 ? 30 : Math.min(trendDays, 180);
    LocalDate today = LocalDate.now();
    // 保证当日快照存在（实时补数）
    CockpitMetricSnapshot latest = snapshot(orgId, today);

    List<CockpitMetricSnapshot> history = snapshotMapper.selectList(Wrappers.lambdaQuery(CockpitMetricSnapshot.class)
        .eq(CockpitMetricSnapshot::getIsDeleted, 0)
        .eq(CockpitMetricSnapshot::getOrgId, orgId)
        .ge(CockpitMetricSnapshot::getStatDate, today.minusDays(days))
        .orderByAsc(CockpitMetricSnapshot::getStatDate));

    CockpitOverviewResponse resp = new CockpitOverviewResponse();
    resp.setStatDate(today);
    resp.setTotalBeds(latest.getTotalBeds());
    resp.setOccupiedBeds(latest.getOccupiedBeds());
    resp.setAvailableBeds(latest.getAvailableBeds());
    resp.setOccupancyRate(latest.getOccupancyRate());
    resp.setResidentCount(latest.getResidentCount());
    resp.setAdmissions(latest.getAdmissions());
    resp.setDischarges(latest.getDischarges());
    resp.setNetChange(latest.getNetChange());
    resp.setRevenueAmount(latest.getRevenueAmount());
    resp.setPaidAmount(latest.getPaidAmount());
    resp.setOutstandingAmount(latest.getOutstandingAmount());
    resp.setCollectionRate(latest.getCollectionRate());
    resp.setAlertTotal(latest.getAlertTotal());
    resp.setAlertResolved(latest.getAlertResolved());
    resp.setAlertTimeout(latest.getAlertTimeout());
    resp.setAlertRespRate(latest.getAlertRespRate());

    // 床位周转率：近 days 日累计入住 / 期末在住
    int admissionsSum = history.stream().mapToInt(s -> nvlInt(s.getAdmissions())).sum();
    resp.setTurnoverRate(rate(admissionsSum, nvlInt(latest.getResidentCount())));

    // 人效：在住长者 / 在职员工
    int staffCount = Math.toIntExact(staffAccountMapper.selectCount(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(StaffAccount::getOrgId, orgId)
        .eq(StaffAccount::getStatus, 1)));
    resp.setStaffCount(staffCount);
    resp.setResidentPerStaff(staffCount == 0 ? BigDecimal.ZERO
        : BigDecimal.valueOf(nvlInt(latest.getResidentCount()))
            .divide(BigDecimal.valueOf(staffCount), 2, RoundingMode.HALF_UP));

    List<CockpitTrendItem> trend = new ArrayList<>();
    for (CockpitMetricSnapshot s : history) {
      CockpitTrendItem item = new CockpitTrendItem();
      item.setStatDate(s.getStatDate());
      item.setOccupancyRate(s.getOccupancyRate());
      item.setResidentCount(s.getResidentCount());
      item.setRevenueAmount(s.getRevenueAmount());
      item.setCollectionRate(s.getCollectionRate());
      item.setAlertRespRate(s.getAlertRespRate());
      trend.add(item);
    }
    trend.sort(Comparator.comparing(CockpitTrendItem::getStatDate));
    resp.setTrend(trend);
    return resp;
  }

  private static BigDecimal nvl(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
  }

  private static int nvlInt(Integer v) {
    return v == null ? 0 : v;
  }

  private static BigDecimal rate(long numerator, long denominator) {
    if (denominator <= 0) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(numerator)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
  }

  private static BigDecimal rate(BigDecimal numerator, BigDecimal denominator) {
    if (denominator == null || denominator.signum() <= 0) {
      return BigDecimal.ZERO;
    }
    return nvl(numerator).multiply(BigDecimal.valueOf(100))
        .divide(denominator, 2, RoundingMode.HALF_UP);
  }
}
