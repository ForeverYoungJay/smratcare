package com.zhiyangyun.care.cockpit.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.Org;
import com.zhiyangyun.care.auth.mapper.OrgMapper;
import com.zhiyangyun.care.bill.entity.BillItem;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillItemMapper;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.cockpit.entity.StatsDailyCare;
import com.zhiyangyun.care.cockpit.entity.StatsDailyFinance;
import com.zhiyangyun.care.cockpit.entity.StatsDailyOperation;
import com.zhiyangyun.care.cockpit.mapper.StatsDailyCareMapper;
import com.zhiyangyun.care.cockpit.mapper.StatsDailyFinanceMapper;
import com.zhiyangyun.care.cockpit.mapper.StatsDailyOperationMapper;
import com.zhiyangyun.care.cockpit.service.CockpitDailyStatsService;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.model.ElderLifecycleStatus;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.model.TaskStatus;
import com.zhiyangyun.care.schedule.entity.StaffSchedule;
import com.zhiyangyun.care.schedule.mapper.StaffScheduleMapper;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.mapper.SmartAlertMapper;
import com.zhiyangyun.care.survey.entity.SurveySubmission;
import com.zhiyangyun.care.survey.mapper.SurveySubmissionMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CockpitDailyStatsServiceImpl implements CockpitDailyStatsService {

  private static final Logger log = LoggerFactory.getLogger(CockpitDailyStatsServiceImpl.class);

  private final StatsDailyOperationMapper operationMapper;
  private final StatsDailyFinanceMapper financeMapper;
  private final StatsDailyCareMapper careMapper;
  private final BedMapper bedMapper;
  private final ElderMapper elderMapper;
  private final BillMonthlyMapper billMonthlyMapper;
  private final BillItemMapper billItemMapper;
  private final PaymentRecordMapper paymentRecordMapper;
  private final CareTaskDailyMapper careTaskDailyMapper;
  private final StaffScheduleMapper staffScheduleMapper;
  private final SmartAlertMapper smartAlertMapper;
  private final SurveySubmissionMapper surveySubmissionMapper;
  private final OrgMapper orgMapper;
  private final ObjectMapper objectMapper;

  public CockpitDailyStatsServiceImpl(StatsDailyOperationMapper operationMapper,
      StatsDailyFinanceMapper financeMapper, StatsDailyCareMapper careMapper,
      BedMapper bedMapper, ElderMapper elderMapper, BillMonthlyMapper billMonthlyMapper,
      BillItemMapper billItemMapper, PaymentRecordMapper paymentRecordMapper,
      CareTaskDailyMapper careTaskDailyMapper, StaffScheduleMapper staffScheduleMapper,
      SmartAlertMapper smartAlertMapper, SurveySubmissionMapper surveySubmissionMapper,
      OrgMapper orgMapper, ObjectMapper objectMapper) {
    this.operationMapper = operationMapper;
    this.financeMapper = financeMapper;
    this.careMapper = careMapper;
    this.bedMapper = bedMapper;
    this.elderMapper = elderMapper;
    this.billMonthlyMapper = billMonthlyMapper;
    this.billItemMapper = billItemMapper;
    this.paymentRecordMapper = paymentRecordMapper;
    this.careTaskDailyMapper = careTaskDailyMapper;
    this.staffScheduleMapper = staffScheduleMapper;
    this.smartAlertMapper = smartAlertMapper;
    this.surveySubmissionMapper = surveySubmissionMapper;
    this.orgMapper = orgMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional
  public void aggregate(Long orgId, LocalDate statDate) {
    if (orgId == null) {
      return;
    }
    LocalDate date = statDate == null ? LocalDate.now().minusDays(1) : statDate;
    aggregateOperation(orgId, date);
    aggregateFinance(orgId, date);
    aggregateCare(orgId, date);
  }

  @Override
  public int aggregateAllOrgs(LocalDate statDate) {
    List<Org> orgs = orgMapper.selectList(Wrappers.lambdaQuery(Org.class)
        .eq(Org::getIsDeleted, 0));
    int count = 0;
    for (Org org : orgs) {
      if (org.getId() == null) {
        continue;
      }
      try {
        aggregate(org.getId(), statDate);
        count++;
      } catch (Exception ex) {
        log.error("[CockpitBI] 机构 {} 日汇总聚合失败, date={}", org.getId(), statDate, ex);
      }
    }
    return count;
  }

  // ==================== 运营汇总 ====================

  private void aggregateOperation(Long orgId, LocalDate date) {
    List<Bed> beds = bedMapper.selectList(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(Bed::getOrgId, orgId));
    int totalBeds = beds.size();
    int occupiedBeds = (int) beds.stream().filter(b -> b.getElderId() != null).count();

    // 在住长者（口径：status=1），一次取出用于人数/等级分布/平均在住时长
    List<ElderProfile> residents = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getStatus, 1));
    int residentCount = residents.size();

    int admissions = Math.toIntExact(elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getAdmissionDate, date)));

    LocalDateTime dayStart = date.atStartOfDay();
    LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
    int discharges = Math.toIntExact(elderMapper.selectCount(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(ElderProfile::getOrgId, orgId)
        .in(ElderProfile::getLifecycleStatus, ElderLifecycleStatus.DISCHARGED, ElderLifecycleStatus.DECEASED)
        .ge(ElderProfile::getLifecycleUpdatedAt, dayStart)
        .lt(ElderProfile::getLifecycleUpdatedAt, dayEnd)));

    // 平均在住时长（天）：在住长者自入住日至统计日
    long staySum = 0;
    int stayCount = 0;
    for (ElderProfile elder : residents) {
      if (elder.getAdmissionDate() != null && !elder.getAdmissionDate().isAfter(date)) {
        staySum += ChronoUnit.DAYS.between(elder.getAdmissionDate(), date);
        stayCount++;
      }
    }
    BigDecimal avgStayDays = stayCount == 0 ? BigDecimal.ZERO
        : BigDecimal.valueOf(staySum).divide(BigDecimal.valueOf(stayCount), 1, RoundingMode.HALF_UP);

    // 护理等级分布
    Map<String, Integer> levelDist = new LinkedHashMap<>();
    for (ElderProfile elder : residents) {
      String level = elder.getCareLevel() == null || elder.getCareLevel().isBlank()
          ? "未评定" : elder.getCareLevel();
      levelDist.merge(level, 1, Integer::sum);
    }

    StatsDailyOperation row = operationMapper.selectOne(Wrappers.lambdaQuery(StatsDailyOperation.class)
        .eq(StatsDailyOperation::getIsDeleted, 0)
        .eq(StatsDailyOperation::getOrgId, orgId)
        .eq(StatsDailyOperation::getStatDate, date));
    boolean insert = row == null;
    if (insert) {
      row = new StatsDailyOperation();
      row.setOrgId(orgId);
      row.setStatDate(date);
      row.setIsDeleted(0);
    }
    row.setResidentCount(residentCount);
    row.setTotalBeds(totalBeds);
    row.setOccupiedBeds(occupiedBeds);
    row.setOccupancyRate(rate(occupiedBeds, totalBeds));
    row.setBedTurnoverRate(rate(admissions + discharges, totalBeds));
    row.setAdmissions(admissions);
    row.setDischarges(discharges);
    row.setAvgStayDays(avgStayDays);
    row.setCareLevelJson(toJson(levelDist));
    if (insert) {
      operationMapper.insert(row);
    } else {
      operationMapper.updateById(row);
    }
  }

  // ==================== 财务汇总 ====================

  private void aggregateFinance(Long orgId, LocalDate date) {
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

    // 当日回款：收款流水按支付时间
    LocalDateTime dayStart = date.atStartOfDay();
    LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
    BigDecimal paidDaily = paymentRecordMapper.selectList(Wrappers.lambdaQuery(PaymentRecord.class)
            .eq(PaymentRecord::getIsDeleted, 0)
            .eq(PaymentRecord::getOrgId, orgId)
            .ge(PaymentRecord::getPaidAt, dayStart)
            .lt(PaymentRecord::getPaidAt, dayEnd))
        .stream()
        .map(p -> nvl(p.getAmount()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // 费用科目结构：当月账单明细按 item_type 汇总
    Map<String, BigDecimal> costStructure = new LinkedHashMap<>();
    List<Long> billIds = bills.stream().map(BillMonthly::getId).filter(Objects::nonNull).toList();
    if (!billIds.isEmpty()) {
      List<BillItem> items = billItemMapper.selectList(Wrappers.lambdaQuery(BillItem.class)
          .eq(BillItem::getIsDeleted, 0)
          .eq(BillItem::getOrgId, orgId)
          .in(BillItem::getBillMonthlyId, billIds));
      for (BillItem item : items) {
        String type = item.getItemType() == null || item.getItemType().isBlank()
            ? "OTHER" : item.getItemType();
        costStructure.merge(type, nvl(item.getAmount()), BigDecimal::add);
      }
    }

    StatsDailyFinance row = financeMapper.selectOne(Wrappers.lambdaQuery(StatsDailyFinance.class)
        .eq(StatsDailyFinance::getIsDeleted, 0)
        .eq(StatsDailyFinance::getOrgId, orgId)
        .eq(StatsDailyFinance::getStatDate, date));
    boolean insert = row == null;
    if (insert) {
      row = new StatsDailyFinance();
      row.setOrgId(orgId);
      row.setStatDate(date);
      row.setIsDeleted(0);
    }
    row.setRevenueAmount(revenue);
    row.setPaidAmount(paid);
    row.setPaidDailyAmount(paidDaily);
    row.setOutstandingAmount(outstanding);
    row.setCollectionRate(rate(paid, revenue));
    row.setCostStructureJson(toJson(costStructure));
    if (insert) {
      financeMapper.insert(row);
    } else {
      financeMapper.updateById(row);
    }
  }

  // ==================== 护理与安全汇总 ====================

  private void aggregateCare(Long orgId, LocalDate date) {
    LocalDateTime dayStart = date.atStartOfDay();
    LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

    // 护理工单完成率
    List<CareTaskDaily> tasks = careTaskDailyMapper.selectList(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(CareTaskDaily::getOrgId, orgId)
        .eq(CareTaskDaily::getTaskDate, date));
    int taskTotal = tasks.size();
    int taskDone = (int) tasks.stream()
        .filter(t -> TaskStatus.DONE.name().equals(t.getStatus())).count();

    // 人效工时：当日排班工时（结束-开始），按排班员工数摊薄
    List<StaffSchedule> schedules = staffScheduleMapper.selectList(Wrappers.lambdaQuery(StaffSchedule.class)
        .eq(StaffSchedule::getIsDeleted, 0)
        .eq(StaffSchedule::getOrgId, orgId)
        .eq(StaffSchedule::getDutyDate, date));
    long scheduledStaffCount = schedules.stream()
        .map(StaffSchedule::getStaffId).filter(Objects::nonNull).distinct().count();
    long totalMinutes = 0;
    for (StaffSchedule schedule : schedules) {
      if (schedule.getStartTime() != null && schedule.getEndTime() != null
          && schedule.getEndTime().isAfter(schedule.getStartTime())) {
        totalMinutes += Duration.between(schedule.getStartTime(), schedule.getEndTime()).toMinutes();
      }
    }
    BigDecimal scheduledHours = BigDecimal.valueOf(totalMinutes)
        .divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);
    BigDecimal hoursPerStaff = scheduledStaffCount == 0 ? BigDecimal.ZERO
        : scheduledHours.divide(BigDecimal.valueOf(scheduledStaffCount), 1, RoundingMode.HALF_UP);

    // 告警响应达标率：当日触发的告警中，已响应（非 OPEN）且未超时升级（escalation_count=0）
    List<SmartAlert> alerts = smartAlertMapper.selectList(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(SmartAlert::getOrgId, orgId)
        .ge(SmartAlert::getFirstTriggeredAt, dayStart)
        .lt(SmartAlert::getFirstTriggeredAt, dayEnd));
    int alertTotal = alerts.size();
    int alertOnTime = (int) alerts.stream()
        .filter(a -> !"OPEN".equalsIgnoreCase(a.getStatus()))
        .filter(a -> a.getEscalationCount() == null || a.getEscalationCount() == 0)
        .count();

    // 家属满意度：当日提交问卷有效得分平均值
    List<SurveySubmission> submissions = surveySubmissionMapper.selectList(
        Wrappers.lambdaQuery(SurveySubmission.class)
            .eq(SurveySubmission::getIsDeleted, 0)
            .eq(SurveySubmission::getOrgId, orgId)
            .ge(SurveySubmission::getCreateTime, dayStart)
            .lt(SurveySubmission::getCreateTime, dayEnd)
            .isNotNull(SurveySubmission::getScoreEffective));
    int satisfactionCount = submissions.size();
    BigDecimal satisfactionScore = BigDecimal.ZERO;
    if (satisfactionCount > 0) {
      long scoreSum = submissions.stream()
          .mapToLong(s -> s.getScoreEffective() == null ? 0 : s.getScoreEffective()).sum();
      satisfactionScore = BigDecimal.valueOf(scoreSum)
          .divide(BigDecimal.valueOf(satisfactionCount), 1, RoundingMode.HALF_UP);
    }

    StatsDailyCare row = careMapper.selectOne(Wrappers.lambdaQuery(StatsDailyCare.class)
        .eq(StatsDailyCare::getIsDeleted, 0)
        .eq(StatsDailyCare::getOrgId, orgId)
        .eq(StatsDailyCare::getStatDate, date));
    boolean insert = row == null;
    if (insert) {
      row = new StatsDailyCare();
      row.setOrgId(orgId);
      row.setStatDate(date);
      row.setIsDeleted(0);
    }
    row.setCareTaskTotal(taskTotal);
    row.setCareTaskDone(taskDone);
    row.setCareTaskCompletionRate(rate(taskDone, taskTotal));
    row.setScheduledStaffCount((int) scheduledStaffCount);
    row.setScheduledHours(scheduledHours);
    row.setHoursPerStaff(hoursPerStaff);
    row.setAlertTotal(alertTotal);
    row.setAlertOnTime(alertOnTime);
    row.setAlertOnTimeRate(rate(alertOnTime, alertTotal));
    row.setSatisfactionScore(satisfactionScore);
    row.setSatisfactionCount(satisfactionCount);
    if (insert) {
      careMapper.insert(row);
    } else {
      careMapper.updateById(row);
    }
  }

  private String toJson(Map<String, ?> map) {
    try {
      return objectMapper.writeValueAsString(map);
    } catch (JsonProcessingException ex) {
      log.warn("[CockpitBI] 序列化分布 JSON 失败", ex);
      return "{}";
    }
  }

  private static BigDecimal nvl(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
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
