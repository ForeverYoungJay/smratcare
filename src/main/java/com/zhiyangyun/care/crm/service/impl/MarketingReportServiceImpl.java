package com.zhiyangyun.care.crm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.crm.entity.CrmCallbackPlan;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.entity.CrmMarketingPlan;
import com.zhiyangyun.care.crm.mapper.CrmCallbackPlanMapper;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.mapper.CrmMarketingPlanMapper;
import com.zhiyangyun.care.crm.model.report.MarketingCallbackItem;
import com.zhiyangyun.care.crm.model.report.MarketingCallbackReportResponse;
import com.zhiyangyun.care.crm.model.report.MarketingChannelReportItem;
import com.zhiyangyun.care.crm.model.report.MarketingConsultationTrendItem;
import com.zhiyangyun.care.crm.model.report.MarketingConversionReportResponse;
import com.zhiyangyun.care.crm.model.report.MarketingDataQualityResponse;
import com.zhiyangyun.care.crm.model.report.MarketingFollowupReportResponse;
import com.zhiyangyun.care.crm.model.report.MarketingLeadEntrySummaryResponse;
import com.zhiyangyun.care.crm.model.report.MarketingWorkbenchSummaryResponse;
import com.zhiyangyun.care.crm.service.CrmTraceService;
import com.zhiyangyun.care.crm.service.MarketingReportService;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketingReportServiceImpl implements MarketingReportService {
  private static final Set<String> STANDARD_SOURCES = new LinkedHashSet<>(
      Arrays.asList("自然到访", "线上咨询", "抖音", "微信", "转介绍", "社区活动", "其他"));
  private static final String FLOW_PENDING_ASSESSMENT = "PENDING_ASSESSMENT";
  private static final String FLOW_PENDING_BED_SELECT = "PENDING_BED_SELECT";
  private static final String FLOW_PENDING_SIGN = "PENDING_SIGN";
  private static final String FLOW_SIGNED = "SIGNED";
  private static final String CHANGE_PENDING_APPROVAL = "PENDING_APPROVAL";

  private final CrmLeadMapper crmLeadMapper;
  private final CrmCallbackPlanMapper callbackPlanMapper;
  private final CrmContractMapper crmContractMapper;
  private final BedMapper bedMapper;
  private final CrmMarketingPlanMapper marketingPlanMapper;
  private final CrmTraceService crmTraceService;

  public MarketingReportServiceImpl(
      CrmLeadMapper crmLeadMapper,
      CrmCallbackPlanMapper callbackPlanMapper,
      CrmContractMapper crmContractMapper,
      BedMapper bedMapper,
      CrmMarketingPlanMapper marketingPlanMapper,
      CrmTraceService crmTraceService) {
    this.crmLeadMapper = crmLeadMapper;
    this.callbackPlanMapper = callbackPlanMapper;
    this.crmContractMapper = crmContractMapper;
    this.bedMapper = bedMapper;
    this.marketingPlanMapper = marketingPlanMapper;
    this.crmTraceService = crmTraceService;
  }

  @Override
  public MarketingConversionReportResponse conversion(
      Long tenantId, String dateFrom, String dateTo, String source, Long staffId) {
    List<CrmLead> leads = crmLeadMapper.selectList(leadWrapperByCreateTime(tenantId, dateFrom, dateTo, source, staffId));
    long total = leads.size();
    long consult = leads.stream().filter(this::isConsultLead).count();
    long intent = leads.stream().filter(this::isIntentLead).count();
    long reserve = leads.stream().filter(this::hasReservationEvidence).count();
    long invalid = leads.stream().filter(this::isInvalidLead).count();
    long contractCount = leads.stream().filter(this::isSignedLead).count();

    MarketingConversionReportResponse response = new MarketingConversionReportResponse();
    response.setTotalLeads(total);
    response.setConsultCount(consult);
    response.setIntentCount(intent);
    response.setReservationCount(reserve);
    response.setInvalidCount(invalid);
    response.setContractCount(contractCount);
    response.setIntentRate(rate(intent, total));
    response.setReservationRate(rate(reserve, total));
    response.setContractRate(rate(contractCount, total));
    crmTraceService.saveSnapshot(
        tenantId,
        tenantId,
        "MARKETING_CONVERSION",
        parseDateOrNull(dateFrom),
        parseDateOrNull(dateTo),
        buildReportSnapshotKey(source, staffId, null),
        response);
    return response;
  }

  @Override
  public List<MarketingConsultationTrendItem> consultationTrend(
      Long tenantId, int days, String dateFrom, String dateTo, String source, Long staffId) {
    LocalDate from = parseDateOrNull(dateFrom);
    LocalDate to = parseDateOrNull(dateTo);
    if (from == null || to == null) {
      int safeDays = Math.max(1, Math.min(days, 90));
      to = LocalDate.now();
      from = to.minusDays(safeDays - 1L);
    }
    if (from.isAfter(to)) {
      throw new IllegalArgumentException("dateFrom cannot be after dateTo");
    }

    LambdaQueryWrapper<CrmLead> wrapper = Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId)
        .eq(source != null && !source.isBlank(), CrmLead::getSource, normalizeSource(source));
    applyLeadStaffScope(wrapper, staffId);
    List<CrmLead> leads = crmLeadMapper.selectList(wrapper);

    Map<String, MarketingConsultationTrendItem> map = new LinkedHashMap<>();
    LocalDate cursor = from;
    while (!cursor.isAfter(to)) {
      MarketingConsultationTrendItem item = new MarketingConsultationTrendItem();
      item.setDate(cursor.toString());
      map.put(item.getDate(), item);
      cursor = cursor.plusDays(1);
    }

    for (CrmLead lead : leads) {
      LocalDate bucketDate = lead.getConsultDate();
      if (bucketDate == null && lead.getCreateTime() != null) {
        bucketDate = lead.getCreateTime().toLocalDate();
      }
      if (bucketDate == null || bucketDate.isBefore(from) || bucketDate.isAfter(to)) {
        continue;
      }
      MarketingConsultationTrendItem day = map.get(bucketDate.toString());
      if (day == null) {
        continue;
      }
      day.setTotal(day.getTotal() + 1);
      if (isConsultLead(lead)) {
        day.setConsultCount(day.getConsultCount() + 1);
      }
      if (isIntentLead(lead)) {
        day.setIntentCount(day.getIntentCount() + 1);
      }
    }

    List<MarketingConsultationTrendItem> response = new ArrayList<>(map.values());
    crmTraceService.saveSnapshot(
        tenantId,
        tenantId,
        "MARKETING_CONSULTATION",
        from,
        to,
        buildReportSnapshotKey(source, staffId, "days=" + days),
        response);
    return response;
  }

  @Override
  public List<MarketingChannelReportItem> channel(Long tenantId, String dateFrom, String dateTo, Long staffId) {
    List<CrmLead> leads = crmLeadMapper.selectList(
        leadWrapperByCreateTime(tenantId, dateFrom, dateTo, null, staffId));
    Map<String, MarketingChannelReportItem> map = new LinkedHashMap<>();
    for (CrmLead lead : leads) {
      String source = normalizeSource(lead.getSource());
      MarketingChannelReportItem item = map.computeIfAbsent(source, key -> {
        MarketingChannelReportItem created = new MarketingChannelReportItem();
        created.setSource(source);
        return created;
      });
      item.setLeadCount(item.getLeadCount() + 1);
      if (hasReservationEvidence(lead)) {
        item.setReservationCount(item.getReservationCount() + 1);
      }
      if (isSignedLead(lead)) {
        item.setContractCount(item.getContractCount() + 1);
      }
    }
    List<MarketingChannelReportItem> response = new ArrayList<>(map.values());
    crmTraceService.saveSnapshot(
        tenantId,
        tenantId,
        "MARKETING_CHANNEL",
        parseDateOrNull(dateFrom),
        parseDateOrNull(dateTo),
        buildReportSnapshotKey(null, staffId, null),
        response);
    return response;
  }

  @Override
  public MarketingFollowupReportResponse followup(
      Long tenantId, String dateFrom, String dateTo, String source, Long staffId) {
    List<CrmLead> leads = crmLeadMapper.selectList(leadWrapperByCreateTime(tenantId, dateFrom, dateTo, source, staffId));
    long total = leads.size();
    long consult = leads.stream().filter(this::isConsultLead).count();
    long intent = leads.stream().filter(this::isIntentLead).count();
    long reserve = leads.stream().filter(this::hasReservationEvidence).count();
    long invalid = leads.stream().filter(this::isInvalidLead).count();

    LocalDate today = LocalDate.now();
    LambdaQueryWrapper<CrmLead> overdueWrapper = Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId)
        .eq(source != null && !source.isBlank(), CrmLead::getSource, normalizeSource(source))
        .isNotNull(CrmLead::getNextFollowDate)
        .lt(CrmLead::getNextFollowDate, today)
        .ne(CrmLead::getContractSignedFlag, 1)
        .ne(CrmLead::getStatus, 3);
    applyLeadStaffScope(overdueWrapper, staffId);
    long overdue = crmLeadMapper.selectCount(overdueWrapper);

    MarketingFollowupReportResponse response = new MarketingFollowupReportResponse();
    response.setTotalLeads(total);
    response.setConsultCount(consult);
    response.setIntentCount(intent);
    response.setReservationCount(reserve);
    response.setInvalidCount(invalid);
    response.setOverdueCount(overdue);
    response.setStages(buildStages(total, consult, intent, reserve, invalid));
    crmTraceService.saveSnapshot(
        tenantId,
        tenantId,
        "MARKETING_FOLLOWUP",
        parseDateOrNull(dateFrom),
        parseDateOrNull(dateTo),
        buildReportSnapshotKey(source, staffId, null),
        response);
    return response;
  }

  @Override
  public MarketingCallbackReportResponse callback(
      Long tenantId, long pageNo, long pageSize, String dateFrom, String dateTo, String source, Long staffId, String type) {
    LocalDate from = parseDateOrNull(dateFrom);
    LocalDate to = parseDateOrNull(dateTo);
    LocalDate today = LocalDate.now();
    if (from == null) {
      from = today.minusDays(30);
    }
    if (to == null) {
      to = today;
    }
    if (from.isAfter(to)) {
      throw new IllegalArgumentException("dateFrom cannot be after dateTo");
    }

    String normalizedType = normalizeCallbackType(type);
    if ("SCORE".equals(normalizedType)) {
      MarketingCallbackReportResponse scoreResponse =
          buildScoreCallbackResponse(tenantId, pageNo, pageSize, from, to, source, staffId);
      crmTraceService.saveSnapshot(
          tenantId,
          tenantId,
          "MARKETING_CALLBACK",
          from,
          to,
          buildReportSnapshotKey(source, staffId, normalizedType),
          buildCallbackSnapshotPayload(scoreResponse));
      return scoreResponse;
    }
    List<CrmLead> dueLeads = crmLeadMapper.selectList(baseCallbackLeadWrapper(tenantId, source, staffId)
        .isNotNull(CrmLead::getNextFollowDate)
        .between(CrmLead::getNextFollowDate, from, to)
        .ne(CrmLead::getContractSignedFlag, 1)
        .ne(CrmLead::getStatus, 3)
        .le(CrmLead::getNextFollowDate, today)
        .orderByAsc(CrmLead::getNextFollowDate)
        .orderByAsc(CrmLead::getId));
    List<CrmLead> todayDueLeads = crmLeadMapper.selectList(baseCallbackLeadWrapper(tenantId, source, staffId)
        .eq(CrmLead::getNextFollowDate, today)
        .ne(CrmLead::getContractSignedFlag, 1)
        .ne(CrmLead::getStatus, 3));
    List<CrmLead> overdueLeads = crmLeadMapper.selectList(baseCallbackLeadWrapper(tenantId, source, staffId)
        .isNotNull(CrmLead::getNextFollowDate)
        .lt(CrmLead::getNextFollowDate, today)
        .ne(CrmLead::getContractSignedFlag, 1)
        .ne(CrmLead::getStatus, 3));

    Map<Long, List<CrmCallbackPlan>> planMap = loadPlansByLeadIds(tenantId, collectLeadIds(dueLeads, todayDueLeads, overdueLeads));
    List<CrmLead> filteredDueLeads = filterLeadsByCallbackType(dueLeads, planMap, normalizedType);
    List<CrmLead> filteredTodayDueLeads = filterLeadsByCallbackType(todayDueLeads, planMap, normalizedType);
    List<CrmLead> filteredOverdueLeads = filterLeadsByCallbackType(overdueLeads, planMap, normalizedType);

    int safePageNo = (int) Math.max(1, pageNo);
    int safePageSize = (int) Math.max(1, pageSize);
    int startIndex = Math.max(0, (safePageNo - 1) * safePageSize);
    int endIndex = Math.min(filteredDueLeads.size(), startIndex + safePageSize);
    List<CrmLead> pageRecords = startIndex >= filteredDueLeads.size()
        ? List.of()
        : filteredDueLeads.subList(startIndex, endIndex);
    List<MarketingCallbackItem> records = pageRecords.stream().map(item -> {
      MarketingCallbackItem result = new MarketingCallbackItem();
      result.setId(item.getId());
      result.setName(item.getName());
      result.setPhone(item.getPhone());
      result.setSource(item.getSource());
      result.setNextFollowDate(item.getNextFollowDate() == null ? null : item.getNextFollowDate().toString());
      result.setCallbackType(toClientCallbackType(resolveCallbackType(item, planMap.get(item.getId()))));
      result.setScore(resolveCallbackScore(planMap.get(item.getId())));
      result.setRemark(resolveCallbackRemark(item, planMap.get(item.getId())));
      return result;
    }).toList();

    MarketingCallbackReportResponse response = new MarketingCallbackReportResponse();
    response.setTodayDue(filteredTodayDueLeads.size());
    response.setOverdue(filteredOverdueLeads.size());
    response.setCompleted(countCompletedCallbacks(tenantId, from, to, source, staffId, normalizedType));
    response.setTotal(filteredDueLeads.size());
    response.setRecords(records);
    crmTraceService.saveSnapshot(
        tenantId,
        tenantId,
        "MARKETING_CALLBACK",
        from,
        to,
        buildReportSnapshotKey(source, staffId, normalizedType),
        buildCallbackSnapshotPayload(response));
    return response;
  }

  private MarketingCallbackReportResponse buildScoreCallbackResponse(
      Long tenantId,
      long pageNo,
      long pageSize,
      LocalDate from,
      LocalDate to,
      String source,
      Long staffId) {
    List<CrmLead> candidateLeads = crmLeadMapper.selectList(baseCallbackLeadWrapper(tenantId, source, staffId));
    MarketingCallbackReportResponse response = new MarketingCallbackReportResponse();
    if (candidateLeads.isEmpty()) {
      response.setTodayDue(0);
      response.setOverdue(0);
      response.setCompleted(0);
      response.setTotal(0);
      response.setRecords(List.of());
      return response;
    }
    Map<Long, CrmLead> leadMap = candidateLeads.stream()
        .filter(item -> item.getId() != null)
        .collect(Collectors.toMap(CrmLead::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    LocalDateTime executedFrom = from.atStartOfDay();
    LocalDateTime executedTo = to.plusDays(1L).atStartOfDay();
    List<CrmCallbackPlan> scoredPlans = callbackPlanMapper.selectList(Wrappers.lambdaQuery(CrmCallbackPlan.class)
            .eq(CrmCallbackPlan::getIsDeleted, 0)
            .eq(tenantId != null, CrmCallbackPlan::getTenantId, tenantId)
            .eq(CrmCallbackPlan::getStatus, "DONE")
            .isNotNull(CrmCallbackPlan::getExecutedTime)
            .ge(CrmCallbackPlan::getExecutedTime, executedFrom)
            .lt(CrmCallbackPlan::getExecutedTime, executedTo)
            .in(CrmCallbackPlan::getLeadId, leadMap.keySet())
            .orderByDesc(CrmCallbackPlan::getExecutedTime)
            .orderByDesc(CrmCallbackPlan::getCreateTime))
        .stream()
        .filter(plan -> resolvePlanScore(plan) != null)
        .toList();

    int safePageNo = (int) Math.max(1, pageNo);
    int safePageSize = (int) Math.max(1, pageSize);
    int startIndex = Math.max(0, (safePageNo - 1) * safePageSize);
    int endIndex = Math.min(scoredPlans.size(), startIndex + safePageSize);
    List<CrmCallbackPlan> pagePlans = startIndex >= scoredPlans.size()
        ? List.of()
        : scoredPlans.subList(startIndex, endIndex);
    List<MarketingCallbackItem> records = pagePlans.stream()
        .map(plan -> buildScoreCallbackItem(plan, leadMap.get(plan.getLeadId())))
        .toList();

    response.setTodayDue(0);
    response.setOverdue(0);
    response.setCompleted(scoredPlans.size());
    response.setTotal(scoredPlans.size());
    response.setRecords(records);
    return response;
  }

  private LambdaQueryWrapper<CrmLead> baseCallbackLeadWrapper(Long tenantId, String source, Long staffId) {
    LambdaQueryWrapper<CrmLead> wrapper = Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId)
        .eq(source != null && !source.isBlank(), CrmLead::getSource, normalizeSource(source));
    applyLeadStaffScope(wrapper, staffId);
    return wrapper;
  }

  private Set<Long> collectLeadIds(List<CrmLead>... leadGroups) {
    Set<Long> leadIds = new LinkedHashSet<>();
    for (List<CrmLead> group : leadGroups) {
      if (group == null) {
        continue;
      }
      for (CrmLead lead : group) {
        if (lead != null && lead.getId() != null) {
          leadIds.add(lead.getId());
        }
      }
    }
    return leadIds;
  }

  private Map<Long, List<CrmCallbackPlan>> loadPlansByLeadIds(Long tenantId, Set<Long> leadIds) {
    if (leadIds == null || leadIds.isEmpty()) {
      return Map.of();
    }
    return callbackPlanMapper.selectList(Wrappers.lambdaQuery(CrmCallbackPlan.class)
            .eq(CrmCallbackPlan::getIsDeleted, 0)
            .eq(tenantId != null, CrmCallbackPlan::getTenantId, tenantId)
            .in(CrmCallbackPlan::getLeadId, leadIds)
            .orderByDesc(CrmCallbackPlan::getPlanExecuteTime)
            .orderByDesc(CrmCallbackPlan::getCreateTime))
        .stream()
        .collect(Collectors.groupingBy(CrmCallbackPlan::getLeadId, LinkedHashMap::new, Collectors.toList()));
  }

  private List<CrmLead> filterLeadsByCallbackType(
      List<CrmLead> leads,
      Map<Long, List<CrmCallbackPlan>> planMap,
      String normalizedType) {
    if (normalizedType == null || normalizedType.isBlank()) {
      return leads;
    }
    List<CrmLead> result = new ArrayList<>();
    for (CrmLead lead : leads) {
      if (matchesCallbackType(lead, planMap.get(lead.getId()), normalizedType)) {
        result.add(lead);
      }
    }
    return result;
  }

  private long countCompletedCallbacks(
      Long tenantId,
      LocalDate from,
      LocalDate to,
      String source,
      Long staffId,
      String normalizedType) {
    List<CrmLead> candidateLeads = crmLeadMapper.selectList(baseCallbackLeadWrapper(tenantId, source, staffId));
    if (candidateLeads.isEmpty()) {
      return 0;
    }
    Map<Long, CrmLead> leadMap = candidateLeads.stream()
        .filter(item -> item.getId() != null)
        .collect(Collectors.toMap(CrmLead::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    LocalDateTime executedFrom = from.atStartOfDay();
    LocalDateTime executedTo = to.plusDays(1L).atStartOfDay();
    return callbackPlanMapper.selectList(Wrappers.lambdaQuery(CrmCallbackPlan.class)
            .eq(CrmCallbackPlan::getIsDeleted, 0)
            .eq(tenantId != null, CrmCallbackPlan::getTenantId, tenantId)
            .eq(CrmCallbackPlan::getStatus, "DONE")
            .isNotNull(CrmCallbackPlan::getExecutedTime)
            .ge(CrmCallbackPlan::getExecutedTime, executedFrom)
            .lt(CrmCallbackPlan::getExecutedTime, executedTo)
            .in(CrmCallbackPlan::getLeadId, leadMap.keySet()))
        .stream()
        .filter(item -> matchesCallbackType(leadMap.get(item.getLeadId()), List.of(item), normalizedType))
        .count();
  }

  private boolean matchesCallbackType(CrmLead lead, List<CrmCallbackPlan> plans, String normalizedType) {
    if (normalizedType == null || normalizedType.isBlank()) {
      return true;
    }
    if ("SCORE".equals(normalizedType)) {
      return hasScoreEvidence(lead, plans);
    }
    return normalizedType.equals(resolveCallbackType(lead, plans));
  }

  private MarketingCallbackItem buildScoreCallbackItem(CrmCallbackPlan plan, CrmLead lead) {
    MarketingCallbackItem item = new MarketingCallbackItem();
    item.setId(plan.getId());
    item.setName(lead == null ? null : firstNonBlank(blankToNull(lead.getName()), blankToNull(lead.getElderName())));
    item.setPhone(lead == null ? null : firstNonBlank(blankToNull(lead.getPhone()), blankToNull(lead.getElderPhone())));
    item.setSource(lead == null ? null : lead.getSource());
    item.setNextFollowDate(plan.getExecutedTime() == null ? null : plan.getExecutedTime().toLocalDate().toString());
    item.setCallbackType(toClientCallbackType(resolveCallbackType(lead, List.of(plan))));
    item.setScore(resolvePlanScore(plan));
    item.setRemark(resolveCallbackRemark(lead, List.of(plan)));
    return item;
  }

  private String resolveCallbackType(CrmLead lead, List<CrmCallbackPlan> plans) {
    if (plans != null) {
      for (CrmCallbackPlan plan : plans) {
        String normalized = normalizeCallbackType(plan.getCallbackType());
        if (normalized != null) {
          return normalized;
        }
      }
    }
    return inferCallbackType(lead, plans);
  }

  private String resolveCallbackRemark(CrmLead lead, List<CrmCallbackPlan> plans) {
    if (plans != null) {
      for (CrmCallbackPlan plan : plans) {
        String remark = firstNonBlank(
            plan.getFollowupResult(),
            plan.getExecuteNote(),
            plan.getFollowupContent(),
            plan.getTitle());
        if (remark != null) {
          return remark;
        }
      }
    }
    return blankToNull(lead == null ? null : lead.getRemark());
  }

  private boolean hasScoreEvidence(CrmLead lead, List<CrmCallbackPlan> plans) {
    if (resolveCallbackScore(plans) != null) {
      return true;
    }
    if (containsScoreEvidence(lead == null ? null : lead.getRemark())) {
      return true;
    }
    if (plans == null) {
      return false;
    }
    for (CrmCallbackPlan plan : plans) {
      if (containsScoreEvidence(plan.getFollowupResult())
          || containsScoreEvidence(plan.getExecuteNote())
          || containsScoreEvidence(plan.getFollowupContent())
          || containsScoreEvidence(plan.getTitle())) {
        return true;
      }
    }
    return false;
  }

  private Double resolveCallbackScore(List<CrmCallbackPlan> plans) {
    if (plans == null) {
      return null;
    }
    for (CrmCallbackPlan plan : plans) {
      Double score = resolvePlanScore(plan);
      if (score != null) {
        return score;
      }
    }
    return null;
  }

  private Double resolvePlanScore(CrmCallbackPlan plan) {
    if (plan == null) {
      return null;
    }
    if (plan.getScore() != null) {
      return plan.getScore().doubleValue();
    }
    return extractScore(plan.getFollowupResult(), plan.getExecuteNote(), plan.getFollowupContent(), plan.getTitle());
  }

  private Double extractScore(String... values) {
    if (values == null) {
      return null;
    }
    for (String value : values) {
      String normalized = blankToNull(value);
      if (normalized == null) {
        continue;
      }
      java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("([1-5](?:\\.\\d)?)\\s*(分|星)").matcher(normalized);
      if (matcher.find()) {
        try {
          return Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException ignored) {
          return null;
        }
      }
    }
    return null;
  }

  private boolean containsScoreEvidence(String value) {
    String normalized = blankToNull(value);
    if (normalized == null) {
      return false;
    }
    String text = normalized.toLowerCase(Locale.ROOT);
    return text.contains("评分")
        || text.contains("满意度")
        || text.contains("星级")
        || text.contains("分数")
        || text.matches(".*[1-5](?:\\.\\d)?\\s*(分|星).*");
  }

  private String firstNonBlank(String... values) {
    if (values == null) {
      return null;
    }
    for (String value : values) {
      String normalized = blankToNull(value);
      if (normalized != null) {
        return normalized;
      }
    }
    return null;
  }

  private String inferCallbackType(CrmLead lead, List<CrmCallbackPlan> plans) {
    String text = buildCallbackInferenceText(lead, plans);
    if (text.contains("试住")) {
      return "TRIAL";
    }
    if (text.contains("退住") || text.contains("离院") || text.contains("出院")) {
      return "DISCHARGE";
    }
    if (text.contains("入住") || text.contains("签约") || text.contains("入院")) {
      return "CHECKIN";
    }
    return "CHECKIN";
  }

  private String buildCallbackInferenceText(CrmLead lead, List<CrmCallbackPlan> plans) {
    StringBuilder builder = new StringBuilder();
    if (lead != null) {
      appendInferenceText(builder, lead.getSource());
      appendInferenceText(builder, lead.getRemark());
      appendInferenceText(builder, lead.getContractStatus());
      appendInferenceText(builder, lead.getFollowupStatus());
    }
    if (plans != null) {
      for (CrmCallbackPlan plan : plans) {
        appendInferenceText(builder, plan.getTitle());
        appendInferenceText(builder, plan.getFollowupContent());
        appendInferenceText(builder, plan.getFollowupResult());
        appendInferenceText(builder, plan.getExecuteNote());
      }
    }
    return builder.toString();
  }

  private void appendInferenceText(StringBuilder builder, String value) {
    if (value == null || value.isBlank()) {
      return;
    }
    if (builder.length() > 0) {
      builder.append(' ');
    }
    builder.append(value.trim().toLowerCase(Locale.ROOT));
  }

  private String normalizeCallbackType(String type) {
    if (type == null || type.isBlank()) {
      return null;
    }
    String normalized = type.trim().toUpperCase(Locale.ROOT);
    if ("CHECKIN".equals(normalized) || "TRIAL".equals(normalized) || "DISCHARGE".equals(normalized) || "SCORE".equals(normalized)) {
      return normalized;
    }
    return null;
  }

  private String toClientCallbackType(String normalizedType) {
    return normalizedType == null ? null : normalizedType.toLowerCase(Locale.ROOT);
  }

  private Map<String, Object> buildCallbackSnapshotPayload(MarketingCallbackReportResponse response) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("todayDue", response == null ? 0 : response.getTodayDue());
    payload.put("overdue", response == null ? 0 : response.getOverdue());
    payload.put("completed", response == null ? 0 : response.getCompleted());
    payload.put("total", response == null ? 0 : response.getTotal());
    payload.put("recordCount", response == null || response.getRecords() == null ? 0 : response.getRecords().size());
    return payload;
  }

  private String buildReportSnapshotKey(String source, Long staffId, String extra) {
    List<String> segments = new ArrayList<>();
    String normalizedSource = blankToNull(source);
    if (normalizedSource != null) {
      segments.add("source=" + normalizeSource(normalizedSource));
    }
    if (staffId != null) {
      segments.add("staffId=" + staffId);
    }
    String normalizedExtra = blankToNull(extra);
    if (normalizedExtra != null) {
      segments.add(normalizedExtra);
    }
    return segments.isEmpty() ? null : String.join("|", segments);
  }

  @Override
  public MarketingDataQualityResponse dataQuality(Long tenantId) {
    long missingSource = crmLeadMapper.selectCount(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId)
        .and(w -> w.isNull(CrmLead::getSource).or().eq(CrmLead::getSource, "")));

    long missingNextFollowDate = crmLeadMapper.selectCount(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId)
        .ne(CrmLead::getContractSignedFlag, 1)
        .ne(CrmLead::getStatus, 3)
        .isNull(CrmLead::getNextFollowDate));

    List<CrmLead> sourceLeads = crmLeadMapper.selectList(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId)
        .isNotNull(CrmLead::getSource));
    long nonStandard = sourceLeads.stream()
        .map(CrmLead::getSource)
        .filter(value -> value != null && !value.isBlank())
        .filter(value -> !STANDARD_SOURCES.contains(normalizeSource(value)))
        .count();

    MarketingDataQualityResponse response = new MarketingDataQualityResponse();
    response.setMissingSourceCount(missingSource);
    response.setMissingNextFollowDateCount(missingNextFollowDate);
    response.setNonStandardSourceCount(nonStandard);
    crmTraceService.saveSnapshot(
        tenantId,
        tenantId,
        "MARKETING_DATA_QUALITY",
        LocalDate.now(),
        LocalDate.now(),
        null,
        response);
    return response;
  }

  @Override
  public MarketingWorkbenchSummaryResponse workbenchSummary(Long tenantId, String dateFrom, String dateTo) {
    LocalDate from = parseDateOrNull(dateFrom);
    LocalDate to = parseDateOrNull(dateTo);
    if (from == null || to == null) {
      to = LocalDate.now();
      from = to.withDayOfMonth(1);
    }
    if (from.isAfter(to)) {
      throw new IllegalArgumentException("dateFrom cannot be after dateTo");
    }

    String fromText = from.toString();
    String toText = to.toString();
    MarketingConversionReportResponse conversion = conversion(tenantId, fromText, toText, null, null);
    MarketingFollowupReportResponse followupReport = followup(tenantId, fromText, toText, null, null);
    MarketingCallbackReportResponse callbackReport = callback(tenantId, 1, 50, fromText, toText, null, null, null);
    List<MarketingChannelReportItem> channelReport = channel(tenantId, fromText, toText, null);
    List<CrmLead> leads = crmLeadMapper.selectList(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId));
    List<CrmContract> contracts = crmContractMapper.selectList(Wrappers.lambdaQuery(CrmContract.class)
        .eq(CrmContract::getIsDeleted, 0)
        .eq(tenantId != null, CrmContract::getTenantId, tenantId));
    List<Bed> beds = bedMapper.selectList(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(tenantId != null, Bed::getTenantId, tenantId));
    List<CrmMarketingPlan> plans = marketingPlanMapper.selectList(Wrappers.lambdaQuery(CrmMarketingPlan.class)
        .eq(CrmMarketingPlan::getIsDeleted, 0)
        .eq(tenantId != null, CrmMarketingPlan::getOrgId, tenantId));
    LocalDate today = LocalDate.now();

    MarketingWorkbenchSummaryResponse response = new MarketingWorkbenchSummaryResponse();
    response.setDateFrom(fromText);
    response.setDateTo(toText);
    response.setGeneratedAt(LocalDateTime.now());

    response.getFunnel().setTodayConsultCount(conversion.getConsultCount());
    response.getFunnel().setEvaluationCount(countContractsByStage(contracts, FLOW_PENDING_ASSESSMENT));
    response.getFunnel().setPendingAdmissionCount(countContractsByStage(contracts, FLOW_PENDING_BED_SELECT));
    response.getFunnel().setPendingSignCount(countContractsByStage(contracts, FLOW_PENDING_SIGN));
    response.getFunnel().setMonthDealCount(conversion.getContractCount());
    response.getFunnel().setMonthConversionRate(conversion.getContractRate());

    response.getFollowup().setTodayDue(callbackReport.getTodayDue());
    response.getFollowup().setOverdue(callbackReport.getOverdue());
    response.getFollowup().setHighIntentCount(leads.stream().filter(this::isIntentLead).count());
    response.getFollowup().setLockExpiringCount(leads.stream()
        .filter(this::hasReservationEvidence)
        .filter(lead -> lead.getNextFollowDate() != null)
        .count());

    response.getBedSales().setEmptyCount(beds.stream()
        .filter(this::isSellableEmptyBed)
        .count());
    response.getBedSales().setLockCount(beds.stream()
        .filter(bed -> "RESERVATION".equalsIgnoreCase(blankToNull(bed.getOccupancySource())))
        .count());
    response.getBedSales().setReservedUnsignedCount(contracts.stream()
        .filter(contract -> FLOW_PENDING_BED_SELECT.equals(blankToNull(contract.getFlowStage()))
            || FLOW_PENDING_SIGN.equals(blankToNull(contract.getFlowStage())))
        .count());
    response.getBedSales().setPremiumEmptyCount(beds.stream()
        .filter(this::isSellableEmptyBed)
        .filter(bed -> {
          String bedType = blankToNull(bed.getBedType());
          return bedType != null && (bedType.contains("高") || bedType.toLowerCase(Locale.ROOT).contains("premium") || bedType.toLowerCase(Locale.ROOT).contains("vip"));
        })
        .count());

    response.getContract().setPendingSignCount(countContractsByStage(contracts, FLOW_PENDING_SIGN));
    response.getContract().setRenewalDueCount(contracts.stream().filter(contract -> isSignedContract(contract)
        && contract.getEffectiveTo() != null
        && !contract.getEffectiveTo().isBefore(today)
        && contract.getEffectiveTo().isBefore(today.plusDays(31))).count());
    response.getContract().setChangePendingCount(contracts.stream()
        .filter(contract -> CHANGE_PENDING_APPROVAL.equalsIgnoreCase(blankToNull(contract.getChangeWorkflowStatus())))
        .count());

    response.getCallback().setCheckinCount(countCallbackPlansByType(tenantId, from, to, "CHECKIN"));
    response.getCallback().setTrialCount(countCallbackPlansByType(tenantId, from, to, "TRIAL"));
    response.getCallback().setDischargeCount(countCallbackPlansByType(tenantId, from, to, "DISCHARGE"));
    response.getCallback().setScore(resolveAverageCallbackScore(tenantId, from, to));

    Map<String, long[]> marketerStats = new LinkedHashMap<>();
    contracts.stream()
        .filter(this::isSignedContract)
        .forEach(contract -> marketerStats.computeIfAbsent(firstNonBlank(blankToNull(contract.getMarketerName()), "未分配"), key -> new long[1])[0]++);
    long bestDealCount = marketerStats.values().stream().mapToLong(item -> item[0]).max().orElse(0L);
    response.getPerformance().setMonthDealCount(bestDealCount);
    response.getPerformance().setRankNo(marketerStats.size());
    response.getPerformance().setTimelyRate(ratePercentage(
        Math.max((followupReport.getTotalLeads() - followupReport.getOverdueCount()), 0),
        Math.max(followupReport.getTotalLeads(), 1)));

    List<CrmLead> medicalLeads = leads.stream()
        .filter(this::isMedicalTransferredLead)
        .toList();
    response.getMedical().setTodayCount(medicalLeads.stream()
        .filter(item -> item.getCreateTime() != null && today.equals(item.getCreateTime().toLocalDate()))
        .count());
    response.getMedical().setReferCount(medicalLeads.size());
    response.getMedical().setUnassignedCount(medicalLeads.stream().filter(item -> item.getOwnerStaffId() == null).count());

    List<CrmMarketingPlan> speechPlans = plans.stream()
        .filter(item -> "SPEECH".equalsIgnoreCase(blankToNull(item.getModuleType())))
        .toList();
    List<CrmMarketingPlan> policyPlans = plans.stream()
        .filter(item -> "POLICY".equalsIgnoreCase(blankToNull(item.getModuleType())))
        .toList();
    response.getPlan().setSpeechCount(speechPlans.size());
    response.getPlan().setPolicyCount(policyPlans.size());
    response.getPlan().setPendingApprovalCount(plans.stream().filter(item -> "PENDING_APPROVAL".equalsIgnoreCase(blankToNull(item.getStatus()))).count());
    response.getPlan().setRejectedCount(plans.stream().filter(item -> "REJECTED".equalsIgnoreCase(blankToNull(item.getStatus()))).count());
    response.getPlan().setTotalBudgetAmount(plans.stream()
        .map(CrmMarketingPlan::getBudgetAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add));
    response.getPlan().setTotalActualLeadCount(plans.stream().mapToLong(item -> calculatePlanActualLeadCount(item, leads)).sum());
    response.getPlan().setTotalActualContractCount(plans.stream().mapToLong(item -> calculatePlanActualContractCount(item, leads)).sum());

    response.getRisk().setOverdueFollowupCount(callbackReport.getOverdue());
    response.getRisk().setLockUnsignedCount(leads.stream().filter(this::hasReservationEvidence).count());
    response.getRisk().setHighIntentNoEvalCount(leads.stream()
        .filter(this::isIntentLead)
        .filter(item -> blankToNull(item.getFollowupStatus()) == null)
        .count());
    response.getRisk().setChannelDropCount(channelReport.stream()
        .filter(item -> item.getLeadCount() > 0)
        .filter(item -> rate(item.getContractCount(), item.getLeadCount()) < 10D)
        .count());

    List<MarketingChannelReportItem> topChannels = new ArrayList<>(channelReport);
    topChannels.sort((left, right) -> Long.compare(right.getContractCount(), left.getContractCount()));
    response.setChannelTop5(topChannels.stream().limit(5).map(item -> {
      MarketingWorkbenchSummaryResponse.ChannelTop top = new MarketingWorkbenchSummaryResponse.ChannelTop();
      top.setSource(firstNonBlank(blankToNull(item.getSource()), "未标记渠道"));
      top.setLeadCount(item.getLeadCount());
      top.setContractRate(item.getLeadCount() <= 0 ? "0%" : String.format(Locale.ROOT, "%.1f%%", rate(item.getContractCount(), item.getLeadCount())));
      return top;
    }).toList());
    response.setChannelUnknownCount(channelReport.stream()
        .filter(item -> blankToNull(item.getSource()) == null || String.valueOf(item.getSource()).contains("不明"))
        .mapToLong(MarketingChannelReportItem::getLeadCount)
        .sum());
    response.setChannelMonthDeals(channelReport.stream().mapToLong(MarketingChannelReportItem::getContractCount).sum());

    crmTraceService.saveSnapshot(tenantId, tenantId, "MARKETING_WORKBENCH", from, to, fromText + ":" + toText, response);
    return response;
  }

  @Override
  @Transactional
  public int normalizeSources(Long tenantId) {
    List<CrmLead> leads = crmLeadMapper.selectList(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId));
    int updated = 0;
    for (CrmLead lead : leads) {
      String normalized = normalizeSource(lead.getSource());
      String original = lead.getSource();
      boolean changed = (original == null && normalized != null)
          || (original != null && !original.equals(normalized));
      if (!changed) {
        continue;
      }
      lead.setSource(normalized);
      crmLeadMapper.updateById(lead);
      updated++;
    }
    return updated;
  }

  @Override
  public MarketingLeadEntrySummaryResponse leadEntrySummary(
      Long tenantId, String mode, String keyword, String consultantName, String consultantPhone,
      String elderName, String elderPhone, String consultDateFrom, String consultDateTo, String consultType,
      String mediaChannel, String infoSource, String customerTag, String marketerName) {
    String normalizedMode = normalizeMode(mode);
    LocalDate today = LocalDate.now();

    long total = crmLeadMapper.selectCount(buildLeadEntryWrapper(
        tenantId, keyword, null, consultantName, consultantPhone, elderName, elderPhone,
        consultDateFrom, consultDateTo, consultType, mediaChannel, infoSource, customerTag, marketerName));
    List<CrmLead> leads = crmLeadMapper.selectList(buildLeadEntryWrapper(
        tenantId, keyword, null, consultantName, consultantPhone, elderName, elderPhone,
        consultDateFrom, consultDateTo, consultType, mediaChannel, infoSource, customerTag, marketerName));
    long consultCount = leads.stream().filter(this::isConsultLead).count();
    long intentCount = leads.stream().filter(this::isIntentLead).count();
    long reservationCount = leads.stream().filter(this::hasReservationEvidence).count();
    long invalidCount = leads.stream().filter(this::isInvalidLead).count();

    long modeCount = switch (normalizedMode) {
      case "CONSULTATION" -> consultCount;
      case "INTENT" -> intentCount;
      case "RESERVATION" -> reservationCount;
      case "INVALID" -> invalidCount;
      case "CALLBACK" -> leads.stream()
          .filter(lead -> !isInvalidLead(lead) && !isSignedLead(lead))
          .filter(lead -> lead.getNextFollowDate() != null && !lead.getNextFollowDate().isAfter(today))
          .count();
      default -> total;
    };

    long signedContractCount = leads.stream().filter(this::isSignedLead).count();
    long unsignedReservationCount = leads.stream().filter(this::hasReservationEvidence).filter(lead -> !isSignedLead(lead)).count();
    long refundedReservationCount = leads.stream().filter(this::hasReservationEvidence).filter(lead -> Integer.valueOf(1).equals(lead.getRefunded())).count();

    long callbackDueTodayCount = leads.stream()
        .filter(lead -> !isInvalidLead(lead) && !isSignedLead(lead))
        .filter(lead -> today.equals(lead.getNextFollowDate()))
        .count();
    long callbackOverdueCount = leads.stream()
        .filter(lead -> !isInvalidLead(lead) && !isSignedLead(lead))
        .filter(lead -> lead.getNextFollowDate() != null && lead.getNextFollowDate().isBefore(today))
        .count();

    long missingSourceCount = crmLeadMapper.selectCount(buildLeadEntryWrapper(
        tenantId, keyword, null, consultantName, consultantPhone, elderName, elderPhone,
        consultDateFrom, consultDateTo, consultType, mediaChannel, infoSource, customerTag, marketerName)
        .and(w -> w.isNull(CrmLead::getSource).or().eq(CrmLead::getSource, "")));
    long missingNextFollowDateCount = crmLeadMapper.selectCount(buildLeadEntryWrapper(
        tenantId, keyword, null, consultantName, consultantPhone, elderName, elderPhone,
        consultDateFrom, consultDateTo, consultType, mediaChannel, infoSource, customerTag, marketerName)
        .ne(CrmLead::getContractSignedFlag, 1)
        .ne(CrmLead::getStatus, 3)
        .isNull(CrmLead::getNextFollowDate));

    long nonStandardSourceCount = leads.stream()
        .map(CrmLead::getSource)
        .filter(value -> value != null && !value.isBlank())
        .filter(value -> !STANDARD_SOURCES.contains(normalizeSource(value)))
        .count();

    MarketingLeadEntrySummaryResponse response = new MarketingLeadEntrySummaryResponse();
    response.setTotalCount(total);
    response.setModeCount(modeCount);
    response.setConsultCount(consultCount);
    response.setIntentCount(intentCount);
    response.setReservationCount(reservationCount);
    response.setInvalidCount(invalidCount);
    response.setSignedContractCount(signedContractCount);
    response.setUnsignedReservationCount(unsignedReservationCount);
    response.setRefundedReservationCount(refundedReservationCount);
    response.setCallbackDueTodayCount(callbackDueTodayCount);
    response.setCallbackOverdueCount(callbackOverdueCount);
    response.setCallbackPendingCount(callbackDueTodayCount + callbackOverdueCount);
    response.setMissingSourceCount(missingSourceCount);
    response.setMissingNextFollowDateCount(missingNextFollowDateCount);
    response.setNonStandardSourceCount(nonStandardSourceCount);
    return response;
  }

  private long countByStatus(
      Long tenantId, String dateFrom, String dateTo, String source, Long staffId, int status) {
    return crmLeadMapper.selectCount(
        leadWrapperByCreateTime(tenantId, dateFrom, dateTo, source, staffId).eq(CrmLead::getStatus, status));
  }

  private long countContracts(
      Long tenantId, String dateFrom, String dateTo, String source, Long staffId) {
    return crmLeadMapper.selectCount(leadWrapperByCreateTime(tenantId, dateFrom, dateTo, source, staffId)
        .eq(CrmLead::getContractSignedFlag, 1));
  }

  private boolean isSignedLead(CrmLead lead) {
    if (lead == null) {
      return false;
    }
    return Integer.valueOf(1).equals(lead.getContractSignedFlag())
        || "SIGNED".equalsIgnoreCase(blankToNull(lead.getFlowStage()));
  }

  private boolean isInvalidLead(CrmLead lead) {
    return lead != null && Integer.valueOf(3).equals(lead.getStatus());
  }

  private boolean hasReservationEvidence(CrmLead lead) {
    if (lead == null || isInvalidLead(lead) || isSignedLead(lead)) {
      return false;
    }
    if (lead.getReservationBedId() != null || blankToNull(lead.getReservationRoomNo()) != null) {
      return true;
    }
    if (lead.getReservationAmount() != null && lead.getReservationAmount().compareTo(BigDecimal.ZERO) > 0) {
      return true;
    }
    String reservationStatus = blankToNull(lead.getReservationStatus());
    if (reservationStatus != null) {
      String normalized = reservationStatus.toLowerCase(Locale.ROOT);
      if (normalized.contains("预") || normalized.contains("锁") || normalized.contains("reserve") || normalized.contains("lock")) {
        return true;
      }
    }
    String flowStage = blankToNull(lead.getFlowStage());
    return "PENDING_BED_SELECT".equalsIgnoreCase(flowStage) || "PENDING_SIGN".equalsIgnoreCase(flowStage);
  }

  private boolean isIntentLead(CrmLead lead) {
    return lead != null && !isInvalidLead(lead) && !isSignedLead(lead) && !hasReservationEvidence(lead)
        && Integer.valueOf(1).equals(lead.getStatus());
  }

  private boolean isConsultLead(CrmLead lead) {
    return lead != null && !isInvalidLead(lead) && !isSignedLead(lead) && !hasReservationEvidence(lead)
        && !Integer.valueOf(1).equals(lead.getStatus());
  }

  private long countContractsByStage(List<CrmContract> contracts, String flowStage) {
    return contracts.stream()
        .filter(item -> flowStage.equalsIgnoreCase(blankToNull(item.getFlowStage())))
        .count();
  }

  private boolean isSignedContract(CrmContract contract) {
    if (contract == null) {
      return false;
    }
    String status = blankToNull(contract.getStatus());
    String flowStage = blankToNull(contract.getFlowStage());
    return "SIGNED".equalsIgnoreCase(status)
        || "EFFECTIVE".equalsIgnoreCase(status)
        || FLOW_SIGNED.equalsIgnoreCase(flowStage);
  }

  private boolean isSellableEmptyBed(Bed bed) {
    if (bed == null || Integer.valueOf(1).equals(bed.getIsDeleted())) {
      return false;
    }
    if (bed.getElderId() != null) {
      return false;
    }
    if ("RESERVATION".equalsIgnoreCase(blankToNull(bed.getOccupancySource()))
        || "MAINTENANCE".equalsIgnoreCase(blankToNull(bed.getOccupancySource()))
        || "FROZEN".equalsIgnoreCase(blankToNull(bed.getOccupancySource()))) {
      return false;
    }
    return Integer.valueOf(1).equals(bed.getStatus());
  }

  private long countCallbackPlansByType(Long tenantId, LocalDate from, LocalDate to, String type) {
    LocalDateTime fromTime = from.atStartOfDay();
    LocalDateTime toTime = to.plusDays(1L).atStartOfDay();
    return callbackPlanMapper.selectCount(Wrappers.lambdaQuery(CrmCallbackPlan.class)
        .eq(CrmCallbackPlan::getIsDeleted, 0)
        .eq(tenantId != null, CrmCallbackPlan::getTenantId, tenantId)
        .eq(type != null, CrmCallbackPlan::getCallbackType, type)
        .isNotNull(CrmCallbackPlan::getPlanExecuteTime)
        .ge(CrmCallbackPlan::getPlanExecuteTime, fromTime)
        .lt(CrmCallbackPlan::getPlanExecuteTime, toTime));
  }

  private double resolveAverageCallbackScore(Long tenantId, LocalDate from, LocalDate to) {
    LocalDateTime fromTime = from.atStartOfDay();
    LocalDateTime toTime = to.plusDays(1L).atStartOfDay();
    List<CrmCallbackPlan> plans = callbackPlanMapper.selectList(Wrappers.lambdaQuery(CrmCallbackPlan.class)
        .eq(CrmCallbackPlan::getIsDeleted, 0)
        .eq(tenantId != null, CrmCallbackPlan::getTenantId, tenantId)
        .eq(CrmCallbackPlan::getStatus, "DONE")
        .isNotNull(CrmCallbackPlan::getExecutedTime)
        .ge(CrmCallbackPlan::getExecutedTime, fromTime)
        .lt(CrmCallbackPlan::getExecutedTime, toTime));
    List<Double> scores = plans.stream()
        .map(this::resolvePlanScore)
        .filter(Objects::nonNull)
        .toList();
    if (scores.isEmpty()) {
      return 0D;
    }
    double sum = scores.stream().mapToDouble(Double::doubleValue).sum();
    return Double.parseDouble(String.format(Locale.ROOT, "%.1f", sum / scores.size()));
  }

  private boolean isMedicalTransferredLead(CrmLead lead) {
    if (lead == null) {
      return false;
    }
    String text = String.join(" ",
        safeText(lead.getSource()),
        safeText(lead.getInfoSource()),
        safeText(lead.getMediaChannel()));
    return text.contains("医");
  }

  private long calculatePlanActualLeadCount(CrmMarketingPlan plan, List<CrmLead> leads) {
    return leads.stream().filter(lead -> matchesPlanLead(plan, lead)).count();
  }

  private long calculatePlanActualContractCount(CrmMarketingPlan plan, List<CrmLead> leads) {
    return leads.stream().filter(lead -> matchesPlanLead(plan, lead)).filter(this::isSignedLead).count();
  }

  private boolean matchesPlanLead(CrmMarketingPlan plan, CrmLead lead) {
    if (plan == null || lead == null) {
      return false;
    }
    LocalDate effectiveDate = plan.getEffectiveDate();
    if (effectiveDate != null) {
      LocalDate consultDate = lead.getConsultDate();
      if (consultDate == null && lead.getCreateTime() != null) {
        consultDate = lead.getCreateTime().toLocalDate();
      }
      if (consultDate == null || consultDate.isBefore(effectiveDate.minusDays(30)) || consultDate.isAfter(effectiveDate.plusDays(120))) {
        return false;
      }
    }
    String sourceTag = normalizePlanTag(plan.getSourceTag());
    String campaignCode = normalizePlanTag(plan.getCampaignCode());
    if (sourceTag == null && campaignCode == null) {
      return false;
    }
    String leadText = normalizePlanTag(String.join(" ",
        safeText(lead.getSource()),
        safeText(lead.getInfoSource()),
        safeText(lead.getMediaChannel()),
        safeText(lead.getCustomerTag()),
        safeText(lead.getReferralChannel()),
        safeText(lead.getReservationChannel()),
        safeText(lead.getRemark())));
    if (leadText == null) {
      return false;
    }
    boolean sourceMatched = sourceTag == null || leadText.contains(sourceTag);
    boolean campaignMatched = campaignCode == null || leadText.contains(campaignCode);
    return sourceMatched && campaignMatched;
  }

  private String normalizePlanTag(String value) {
    String text = blankToNull(value);
    if (text == null) {
      return null;
    }
    return text.replace("　", " ").replace(" ", "").toLowerCase(Locale.ROOT);
  }

  private double ratePercentage(long numerator, long denominator) {
    return Double.parseDouble(String.format(Locale.ROOT, "%.1f", rate(numerator, denominator)));
  }

  private String safeText(String value) {
    return value == null ? "" : value;
  }

  private LambdaQueryWrapper<CrmLead> leadWrapperByCreateTime(
      Long tenantId, String dateFrom, String dateTo, String source, Long staffId) {
    LocalDate from = parseDateOrNull(dateFrom);
    LocalDate to = parseDateOrNull(dateTo);
    LocalDateTime fromTime = from == null ? null : from.atStartOfDay();
    LocalDateTime toTime = to == null ? null : to.plusDays(1).atStartOfDay();

    LambdaQueryWrapper<CrmLead> wrapper = Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId)
        .eq(source != null && !source.isBlank(), CrmLead::getSource, normalizeSource(source))
        .ge(fromTime != null, CrmLead::getCreateTime, fromTime)
        .lt(toTime != null, CrmLead::getCreateTime, toTime);
    applyLeadStaffScope(wrapper, staffId);
    return wrapper;
  }

  private void applyLeadStaffScope(LambdaQueryWrapper<CrmLead> wrapper, Long staffId) {
    if (wrapper == null || staffId == null) {
      return;
    }
    wrapper.and(w -> w.eq(CrmLead::getOwnerStaffId, staffId)
        .or(x -> x.isNull(CrmLead::getOwnerStaffId).eq(CrmLead::getCreatedBy, staffId)));
  }

  private LambdaQueryWrapper<CrmLead> buildLeadEntryWrapper(
      Long tenantId, String keyword, Integer status, String consultantName, String consultantPhone,
      String elderName, String elderPhone, String consultDateFrom, String consultDateTo, String consultType,
      String mediaChannel, String infoSource, String customerTag, String marketerName) {
    LambdaQueryWrapper<CrmLead> wrapper = Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId)
        .eq(status != null, CrmLead::getStatus, status)
        .like(consultantName != null && !consultantName.isBlank(), CrmLead::getConsultantName, consultantName)
        .like(consultantPhone != null && !consultantPhone.isBlank(), CrmLead::getConsultantPhone, consultantPhone)
        .like(elderName != null && !elderName.isBlank(), CrmLead::getElderName, elderName)
        .like(elderPhone != null && !elderPhone.isBlank(), CrmLead::getElderPhone, elderPhone)
        .eq(consultType != null && !consultType.isBlank(), CrmLead::getConsultType, consultType)
        .eq(mediaChannel != null && !mediaChannel.isBlank(), CrmLead::getMediaChannel, mediaChannel)
        .eq(infoSource != null && !infoSource.isBlank(), CrmLead::getInfoSource, infoSource)
        .eq(customerTag != null && !customerTag.isBlank(), CrmLead::getCustomerTag, customerTag);
    if (marketerName != null && !marketerName.isBlank()) {
      wrapper.and(w -> w.like(CrmLead::getOwnerStaffName, marketerName)
          .or()
          .like(CrmLead::getMarketerName, marketerName));
    }
    LocalDate consultFrom = parseDateOrNull(consultDateFrom);
    LocalDate consultTo = parseDateOrNull(consultDateTo);
    wrapper.ge(consultFrom != null, CrmLead::getConsultDate, consultFrom);
    wrapper.le(consultTo != null, CrmLead::getConsultDate, consultTo);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(CrmLead::getName, keyword)
          .or().like(CrmLead::getPhone, keyword)
          .or().like(CrmLead::getConsultantName, keyword)
          .or().like(CrmLead::getConsultantPhone, keyword)
          .or().like(CrmLead::getElderName, keyword)
          .or().like(CrmLead::getElderPhone, keyword));
    }
    return wrapper;
  }

  private String normalizeMode(String mode) {
    if (mode == null || mode.isBlank()) {
      return "ALL";
    }
    String value = mode.trim().toUpperCase(Locale.ROOT);
    return switch (value) {
      case "CONSULTATION", "INTENT", "RESERVATION", "INVALID", "CALLBACK" -> value;
      default -> "ALL";
    };
  }

  private List<MarketingFollowupReportResponse.StageItem> buildStages(
      long total, long consult, long intent, long reserve, long invalid) {
    List<MarketingFollowupReportResponse.StageItem> stages = new ArrayList<>();
    stages.add(stage("咨询管理", consult, total));
    stages.add(stage("意向客户", intent, total));
    stages.add(stage("预订管理", reserve, total));
    stages.add(stage("失效用户", invalid, total));
    return stages;
  }

  private MarketingFollowupReportResponse.StageItem stage(String name, long count, long total) {
    MarketingFollowupReportResponse.StageItem item = new MarketingFollowupReportResponse.StageItem();
    item.setName(name);
    item.setCount(count);
    item.setRate(rate(count, total));
    return item;
  }

  private double rate(long count, long total) {
    if (total <= 0) {
      return 0;
    }
    return BigDecimal.valueOf(count)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP)
        .doubleValue();
  }

  private LocalDate parseDateOrNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return LocalDate.parse(value);
  }

  private String blankToNull(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private String normalizeSource(String source) {
    if (source == null || source.isBlank()) {
      return null;
    }
    String value = source.trim().replace("　", " ").replace(" ", "");
    String lower = value.toLowerCase(Locale.ROOT);
    if (lower.contains("抖音") || lower.contains("douyin") || lower.contains("dy")) {
      return "抖音";
    }
    if (lower.contains("微信") || lower.contains("weixin") || lower.contains("wx")) {
      return "微信";
    }
    if (lower.contains("转介绍") || lower.contains("介绍")) {
      return "转介绍";
    }
    if (lower.contains("自然到访") || lower.contains("到访")) {
      return "自然到访";
    }
    if (lower.contains("线上") || lower.contains("官网") || lower.contains("小程序")) {
      return "线上咨询";
    }
    if (lower.contains("社区")) {
      return "社区活动";
    }
    if ("其他".equals(value) || "other".equals(lower)) {
      return "其他";
    }
    return value;
  }
}
