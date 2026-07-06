package com.zhiyangyun.care.cockpit.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.Org;
import com.zhiyangyun.care.auth.mapper.OrgMapper;
import com.zhiyangyun.care.cockpit.entity.StatsDailyCare;
import com.zhiyangyun.care.cockpit.entity.StatsDailyFinance;
import com.zhiyangyun.care.cockpit.entity.StatsDailyOperation;
import com.zhiyangyun.care.cockpit.entity.StatsMetricDefinition;
import com.zhiyangyun.care.cockpit.mapper.StatsDailyCareMapper;
import com.zhiyangyun.care.cockpit.mapper.StatsDailyFinanceMapper;
import com.zhiyangyun.care.cockpit.mapper.StatsDailyOperationMapper;
import com.zhiyangyun.care.cockpit.mapper.StatsMetricDefinitionMapper;
import com.zhiyangyun.care.cockpit.model.CockpitBiDistributionResponse;
import com.zhiyangyun.care.cockpit.model.CockpitBiMetricCard;
import com.zhiyangyun.care.cockpit.model.CockpitBiOrgCompareItem;
import com.zhiyangyun.care.cockpit.model.CockpitBiSummaryResponse;
import com.zhiyangyun.care.cockpit.model.CockpitBiTrendItem;
import com.zhiyangyun.care.cockpit.service.CockpitBiService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CockpitBiServiceImpl implements CockpitBiService {

  private static final Logger log = LoggerFactory.getLogger(CockpitBiServiceImpl.class);

  private final StatsDailyOperationMapper operationMapper;
  private final StatsDailyFinanceMapper financeMapper;
  private final StatsDailyCareMapper careMapper;
  private final StatsMetricDefinitionMapper metricDefinitionMapper;
  private final OrgMapper orgMapper;
  private final ObjectMapper objectMapper;

  public CockpitBiServiceImpl(StatsDailyOperationMapper operationMapper,
      StatsDailyFinanceMapper financeMapper, StatsDailyCareMapper careMapper,
      StatsMetricDefinitionMapper metricDefinitionMapper, OrgMapper orgMapper,
      ObjectMapper objectMapper) {
    this.operationMapper = operationMapper;
    this.financeMapper = financeMapper;
    this.careMapper = careMapper;
    this.metricDefinitionMapper = metricDefinitionMapper;
    this.orgMapper = orgMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public CockpitBiSummaryResponse summary(Long orgId) {
    YearMonth month = YearMonth.now();
    MonthAgg cur = loadMonth(orgId, month);
    MonthAgg prev = loadMonth(orgId, month.minusMonths(1));

    CockpitBiSummaryResponse resp = new CockpitBiSummaryResponse();
    resp.setOrgId(orgId);
    resp.setMonth(month.toString());
    resp.setLatestStatDate(cur.latestDate);

    List<CockpitBiMetricCard> cards = new ArrayList<>();
    cards.add(card("OCCUPANCY_RATE", "入住率", "%", cur.occupancyRate, prev.occupancyRate));
    cards.add(card("RESIDENT_COUNT", "在住人数", "人", cur.residentCount, prev.residentCount));
    cards.add(card("ADMISSIONS", "本月入住", "人", cur.admissions, prev.admissions));
    cards.add(card("DISCHARGES", "本月退住", "人", cur.discharges, prev.discharges));
    cards.add(card("AVG_STAY_DAYS", "平均在住时长", "天", cur.avgStayDays, prev.avgStayDays));
    cards.add(card("REVENUE_AMOUNT", "本月营收", "元", cur.revenueAmount, prev.revenueAmount));
    cards.add(card("PAID_AMOUNT", "本月回款", "元", cur.paidAmount, prev.paidAmount));
    cards.add(card("OUTSTANDING_AMOUNT", "本月欠费", "元", cur.outstandingAmount, prev.outstandingAmount));
    cards.add(card("COLLECTION_RATE", "回款率", "%", cur.collectionRate, prev.collectionRate));
    cards.add(card("CARE_TASK_COMPLETION_RATE", "护理工单完成率", "%",
        cur.careTaskCompletionRate, prev.careTaskCompletionRate));
    cards.add(card("HOURS_PER_STAFF", "人效工时", "小时", cur.hoursPerStaff, prev.hoursPerStaff));
    cards.add(card("ALERT_ON_TIME_RATE", "告警响应达标率", "%", cur.alertOnTimeRate, prev.alertOnTimeRate));
    cards.add(card("SATISFACTION_SCORE", "家属满意度", "分", cur.satisfactionScore, prev.satisfactionScore));
    resp.setCards(cards);
    return resp;
  }

  @Override
  public List<CockpitBiTrendItem> trend(Long orgId, int days) {
    int span = days <= 0 ? 30 : Math.min(days, 180);
    LocalDate from = LocalDate.now().minusDays(span);

    Map<LocalDate, CockpitBiTrendItem> byDate = new TreeMap<>();
    Function<LocalDate, CockpitBiTrendItem> itemOf = date -> byDate.computeIfAbsent(date, d -> {
      CockpitBiTrendItem item = new CockpitBiTrendItem();
      item.setStatDate(d);
      return item;
    });

    List<StatsDailyOperation> operations = operationMapper.selectList(
        Wrappers.lambdaQuery(StatsDailyOperation.class)
            .eq(StatsDailyOperation::getIsDeleted, 0)
            .eq(StatsDailyOperation::getOrgId, orgId)
            .ge(StatsDailyOperation::getStatDate, from)
            .orderByAsc(StatsDailyOperation::getStatDate));
    for (StatsDailyOperation op : operations) {
      CockpitBiTrendItem item = itemOf.apply(op.getStatDate());
      item.setOccupancyRate(op.getOccupancyRate());
      item.setResidentCount(op.getResidentCount());
      item.setAdmissions(op.getAdmissions());
      item.setDischarges(op.getDischarges());
    }

    List<StatsDailyFinance> finances = financeMapper.selectList(
        Wrappers.lambdaQuery(StatsDailyFinance.class)
            .eq(StatsDailyFinance::getIsDeleted, 0)
            .eq(StatsDailyFinance::getOrgId, orgId)
            .ge(StatsDailyFinance::getStatDate, from)
            .orderByAsc(StatsDailyFinance::getStatDate));
    for (StatsDailyFinance fin : finances) {
      CockpitBiTrendItem item = itemOf.apply(fin.getStatDate());
      item.setRevenueAmount(fin.getRevenueAmount());
      item.setPaidDailyAmount(fin.getPaidDailyAmount());
      item.setCollectionRate(fin.getCollectionRate());
    }

    List<StatsDailyCare> cares = careMapper.selectList(
        Wrappers.lambdaQuery(StatsDailyCare.class)
            .eq(StatsDailyCare::getIsDeleted, 0)
            .eq(StatsDailyCare::getOrgId, orgId)
            .ge(StatsDailyCare::getStatDate, from)
            .orderByAsc(StatsDailyCare::getStatDate));
    for (StatsDailyCare care : cares) {
      CockpitBiTrendItem item = itemOf.apply(care.getStatDate());
      item.setCareTaskCompletionRate(care.getCareTaskCompletionRate());
      item.setAlertOnTimeRate(care.getAlertOnTimeRate());
      item.setSatisfactionScore(care.getSatisfactionScore());
    }

    return new ArrayList<>(byDate.values());
  }

  @Override
  public CockpitBiDistributionResponse distribution(Long orgId) {
    CockpitBiDistributionResponse resp = new CockpitBiDistributionResponse();
    resp.setOrgId(orgId);
    resp.setCareLevelDist(new LinkedHashMap<>());
    resp.setCostStructure(new LinkedHashMap<>());

    StatsDailyOperation latestOp = operationMapper.selectOne(
        Wrappers.lambdaQuery(StatsDailyOperation.class)
            .eq(StatsDailyOperation::getIsDeleted, 0)
            .eq(StatsDailyOperation::getOrgId, orgId)
            .orderByDesc(StatsDailyOperation::getStatDate)
            .last("LIMIT 1"));
    if (latestOp != null) {
      resp.setStatDate(latestOp.getStatDate());
      resp.setCareLevelDist(parseJson(latestOp.getCareLevelJson(),
          new TypeReference<LinkedHashMap<String, Integer>>() {}));
    }

    StatsDailyFinance latestFin = financeMapper.selectOne(
        Wrappers.lambdaQuery(StatsDailyFinance.class)
            .eq(StatsDailyFinance::getIsDeleted, 0)
            .eq(StatsDailyFinance::getOrgId, orgId)
            .orderByDesc(StatsDailyFinance::getStatDate)
            .last("LIMIT 1"));
    if (latestFin != null) {
      resp.setCostStructure(parseJson(latestFin.getCostStructureJson(),
          new TypeReference<LinkedHashMap<String, BigDecimal>>() {}));
    }
    return resp;
  }

  @Override
  public List<CockpitBiOrgCompareItem> orgCompare(YearMonth month) {
    YearMonth targetMonth = month == null ? YearMonth.now() : month;
    List<Org> orgs = orgMapper.selectList(Wrappers.lambdaQuery(Org.class)
        .eq(Org::getIsDeleted, 0));
    List<CockpitBiOrgCompareItem> items = new ArrayList<>();
    for (Org org : orgs) {
      if (org.getId() == null) {
        continue;
      }
      MonthAgg agg = loadMonth(org.getId(), targetMonth);
      CockpitBiOrgCompareItem item = new CockpitBiOrgCompareItem();
      item.setOrgId(org.getId());
      item.setOrgName(org.getOrgName());
      item.setOccupancyRate(agg.occupancyRate);
      item.setResidentCount(agg.residentCount == null ? null : agg.residentCount.intValue());
      item.setRevenueAmount(agg.revenueAmount);
      item.setCollectionRate(agg.collectionRate);
      item.setCareTaskCompletionRate(agg.careTaskCompletionRate);
      item.setAlertOnTimeRate(agg.alertOnTimeRate);
      item.setSatisfactionScore(agg.satisfactionScore);
      items.add(item);
    }
    items.sort(Comparator.comparing(CockpitBiOrgCompareItem::getOccupancyRate,
        Comparator.nullsLast(Comparator.reverseOrder())));
    return items;
  }

  @Override
  public List<StatsMetricDefinition> metricDefinitions() {
    return metricDefinitionMapper.selectList(Wrappers.lambdaQuery(StatsMetricDefinition.class)
        .eq(StatsMetricDefinition::getIsDeleted, 0)
        .eq(StatsMetricDefinition::getStatus, 1)
        .orderByAsc(StatsMetricDefinition::getMetricCategory)
        .orderByAsc(StatsMetricDefinition::getId));
  }

  // ==================== 月度汇总（从每日汇总二次聚合） ====================

  /** 单月聚合结果（供总览环比与机构对比复用）。 */
  private static class MonthAgg {
    LocalDate latestDate;
    BigDecimal occupancyRate;
    BigDecimal residentCount;
    BigDecimal admissions;
    BigDecimal discharges;
    BigDecimal avgStayDays;
    BigDecimal revenueAmount;
    BigDecimal paidAmount;
    BigDecimal outstandingAmount;
    BigDecimal collectionRate;
    BigDecimal careTaskCompletionRate;
    BigDecimal hoursPerStaff;
    BigDecimal alertOnTimeRate;
    BigDecimal satisfactionScore;
  }

  private MonthAgg loadMonth(Long orgId, YearMonth month) {
    LocalDate from = month.atDay(1);
    LocalDate to = month.atEndOfMonth();
    MonthAgg agg = new MonthAgg();

    List<StatsDailyOperation> operations = operationMapper.selectList(
        Wrappers.lambdaQuery(StatsDailyOperation.class)
            .eq(StatsDailyOperation::getIsDeleted, 0)
            .eq(StatsDailyOperation::getOrgId, orgId)
            .between(StatsDailyOperation::getStatDate, from, to)
            .orderByAsc(StatsDailyOperation::getStatDate));
    if (!operations.isEmpty()) {
      StatsDailyOperation latest = operations.get(operations.size() - 1);
      agg.latestDate = latest.getStatDate();
      agg.residentCount = toDecimal(latest.getResidentCount());
      agg.avgStayDays = latest.getAvgStayDays();
      agg.occupancyRate = avg(operations.stream().map(StatsDailyOperation::getOccupancyRate).toList());
      agg.admissions = sumInt(operations.stream().map(StatsDailyOperation::getAdmissions).toList());
      agg.discharges = sumInt(operations.stream().map(StatsDailyOperation::getDischarges).toList());
    }

    // 财务为月累计口径，取月内最新一日
    StatsDailyFinance latestFin = financeMapper.selectOne(
        Wrappers.lambdaQuery(StatsDailyFinance.class)
            .eq(StatsDailyFinance::getIsDeleted, 0)
            .eq(StatsDailyFinance::getOrgId, orgId)
            .between(StatsDailyFinance::getStatDate, from, to)
            .orderByDesc(StatsDailyFinance::getStatDate)
            .last("LIMIT 1"));
    if (latestFin != null) {
      agg.revenueAmount = latestFin.getRevenueAmount();
      agg.paidAmount = latestFin.getPaidAmount();
      agg.outstandingAmount = latestFin.getOutstandingAmount();
      agg.collectionRate = latestFin.getCollectionRate();
    }

    List<StatsDailyCare> cares = careMapper.selectList(
        Wrappers.lambdaQuery(StatsDailyCare.class)
            .eq(StatsDailyCare::getIsDeleted, 0)
            .eq(StatsDailyCare::getOrgId, orgId)
            .between(StatsDailyCare::getStatDate, from, to));
    if (!cares.isEmpty()) {
      long taskTotal = cares.stream().mapToLong(c -> nvlInt(c.getCareTaskTotal())).sum();
      long taskDone = cares.stream().mapToLong(c -> nvlInt(c.getCareTaskDone())).sum();
      agg.careTaskCompletionRate = rate(taskDone, taskTotal);
      long alertTotal = cares.stream().mapToLong(c -> nvlInt(c.getAlertTotal())).sum();
      long alertOnTime = cares.stream().mapToLong(c -> nvlInt(c.getAlertOnTime())).sum();
      agg.alertOnTimeRate = rate(alertOnTime, alertTotal);
      agg.hoursPerStaff = avg(cares.stream().map(StatsDailyCare::getHoursPerStaff).toList());
      // 满意度按提交数加权
      long satCount = cares.stream().mapToLong(c -> nvlInt(c.getSatisfactionCount())).sum();
      if (satCount > 0) {
        BigDecimal weighted = cares.stream()
            .filter(c -> c.getSatisfactionScore() != null && nvlInt(c.getSatisfactionCount()) > 0)
            .map(c -> c.getSatisfactionScore().multiply(BigDecimal.valueOf(c.getSatisfactionCount())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        agg.satisfactionScore = weighted.divide(BigDecimal.valueOf(satCount), 1, RoundingMode.HALF_UP);
      }
    }
    return agg;
  }

  private static CockpitBiMetricCard card(String code, String name, String unit,
      BigDecimal value, BigDecimal prevValue) {
    CockpitBiMetricCard card = new CockpitBiMetricCard();
    card.setMetricCode(code);
    card.setMetricName(name);
    card.setUnit(unit);
    card.setValue(value);
    card.setPrevValue(prevValue);
    if (value != null && prevValue != null && prevValue.signum() != 0) {
      card.setMomRate(value.subtract(prevValue)
          .multiply(BigDecimal.valueOf(100))
          .divide(prevValue.abs(), 2, RoundingMode.HALF_UP));
    }
    return card;
  }

  private <T> Map<String, T> parseJson(String json, TypeReference<LinkedHashMap<String, T>> type) {
    if (json == null || json.isBlank()) {
      return new LinkedHashMap<>();
    }
    try {
      return objectMapper.readValue(json, type);
    } catch (Exception ex) {
      log.warn("[CockpitBI] 解析分布 JSON 失败: {}", json, ex);
      return new LinkedHashMap<>();
    }
  }

  private static BigDecimal toDecimal(Integer v) {
    return v == null ? null : BigDecimal.valueOf(v);
  }

  private static int nvlInt(Integer v) {
    return v == null ? 0 : v;
  }

  private static BigDecimal sumInt(List<Integer> values) {
    long sum = values.stream().mapToLong(v -> v == null ? 0 : v).sum();
    return BigDecimal.valueOf(sum);
  }

  private static BigDecimal avg(List<BigDecimal> values) {
    List<BigDecimal> present = values.stream().filter(v -> v != null).toList();
    if (present.isEmpty()) {
      return null;
    }
    BigDecimal sum = present.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    return sum.divide(BigDecimal.valueOf(present.size()), 2, RoundingMode.HALF_UP);
  }

  private static BigDecimal rate(long numerator, long denominator) {
    if (denominator <= 0) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(numerator)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
  }
}
