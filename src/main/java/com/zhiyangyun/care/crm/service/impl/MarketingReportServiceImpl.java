package com.zhiyangyun.care.crm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.model.report.MarketingCallbackItem;
import com.zhiyangyun.care.crm.model.report.MarketingCallbackReportResponse;
import com.zhiyangyun.care.crm.model.report.MarketingChannelReportItem;
import com.zhiyangyun.care.crm.model.report.MarketingConsultationTrendItem;
import com.zhiyangyun.care.crm.model.report.MarketingConversionReportResponse;
import com.zhiyangyun.care.crm.model.report.MarketingDataQualityResponse;
import com.zhiyangyun.care.crm.model.report.MarketingFollowupReportResponse;
import com.zhiyangyun.care.crm.service.MarketingReportService;
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
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketingReportServiceImpl implements MarketingReportService {
  private static final Set<String> STANDARD_SOURCES = new LinkedHashSet<>(
      Arrays.asList("自然到访", "线上咨询", "抖音", "微信", "转介绍", "社区活动", "其他"));

  private final CrmLeadMapper crmLeadMapper;

  public MarketingReportServiceImpl(CrmLeadMapper crmLeadMapper) {
    this.crmLeadMapper = crmLeadMapper;
  }

  @Override
  public MarketingConversionReportResponse conversion(
      Long tenantId, String dateFrom, String dateTo, String source, Long staffId) {
    LambdaQueryWrapper<CrmLead> baseWrapper = leadWrapperByCreateTime(tenantId, dateFrom, dateTo, source, staffId);
    long total = crmLeadMapper.selectCount(baseWrapper);
    long consult = countByStatus(tenantId, dateFrom, dateTo, source, staffId, 0);
    long intent = countByStatus(tenantId, dateFrom, dateTo, source, staffId, 1);
    long reserve = countByStatus(tenantId, dateFrom, dateTo, source, staffId, 2);
    long invalid = countByStatus(tenantId, dateFrom, dateTo, source, staffId, 3);
    long contractCount = countContracts(tenantId, dateFrom, dateTo, source, staffId);

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

    LambdaQueryWrapper<CrmLead> wrapper = leadWrapperByCreateTime(tenantId, from.toString(), to.toString(), source, staffId);
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
      if (lead.getCreateTime() == null) {
        continue;
      }
      MarketingConsultationTrendItem day = map.get(lead.getCreateTime().toLocalDate().toString());
      if (day == null) {
        continue;
      }
      day.setTotal(day.getTotal() + 1);
      if (Integer.valueOf(0).equals(lead.getStatus())) {
        day.setConsultCount(day.getConsultCount() + 1);
      }
      if (Integer.valueOf(1).equals(lead.getStatus())) {
        day.setIntentCount(day.getIntentCount() + 1);
      }
    }

    return new ArrayList<>(map.values());
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
      if (Integer.valueOf(2).equals(lead.getStatus()) || Integer.valueOf(1).equals(lead.getContractSignedFlag())) {
        item.setReservationCount(item.getReservationCount() + 1);
      }
      if (Integer.valueOf(1).equals(lead.getContractSignedFlag())) {
        item.setContractCount(item.getContractCount() + 1);
      }
    }
    return new ArrayList<>(map.values());
  }

  @Override
  public MarketingFollowupReportResponse followup(
      Long tenantId, String dateFrom, String dateTo, String source, Long staffId) {
    long total = crmLeadMapper.selectCount(leadWrapperByCreateTime(tenantId, dateFrom, dateTo, source, staffId));
    long consult = countByStatus(tenantId, dateFrom, dateTo, source, staffId, 0);
    long intent = countByStatus(tenantId, dateFrom, dateTo, source, staffId, 1);
    long reserve = countByStatus(tenantId, dateFrom, dateTo, source, staffId, 2);
    long invalid = countByStatus(tenantId, dateFrom, dateTo, source, staffId, 3);

    LocalDate today = LocalDate.now();
    long overdue = crmLeadMapper.selectCount(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId)
        .eq(source != null && !source.isBlank(), CrmLead::getSource, normalizeSource(source))
        .eq(staffId != null, CrmLead::getCreatedBy, staffId)
        .isNotNull(CrmLead::getNextFollowDate)
        .lt(CrmLead::getNextFollowDate, today)
        .notIn(CrmLead::getStatus, List.of(2, 3)));

    MarketingFollowupReportResponse response = new MarketingFollowupReportResponse();
    response.setTotalLeads(total);
    response.setConsultCount(consult);
    response.setIntentCount(intent);
    response.setReservationCount(reserve);
    response.setInvalidCount(invalid);
    response.setOverdueCount(overdue);
    response.setStages(buildStages(total, consult, intent, reserve, invalid));
    return response;
  }

  @Override
  public MarketingCallbackReportResponse callback(
      Long tenantId, long pageNo, long pageSize, String dateFrom, String dateTo, String source, Long staffId) {
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

    LambdaQueryWrapper<CrmLead> dueWrapper = Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId)
        .eq(source != null && !source.isBlank(), CrmLead::getSource, normalizeSource(source))
        .eq(staffId != null, CrmLead::getCreatedBy, staffId)
        .isNotNull(CrmLead::getNextFollowDate)
        .between(CrmLead::getNextFollowDate, from, to)
        .notIn(CrmLead::getStatus, List.of(2, 3))
        .le(CrmLead::getNextFollowDate, today)
        .orderByAsc(CrmLead::getNextFollowDate)
        .orderByAsc(CrmLead::getId);

    long total = crmLeadMapper.selectCount(dueWrapper);
    IPage<CrmLead> page = crmLeadMapper.selectPage(new Page<>(Math.max(1, pageNo), Math.max(1, pageSize)), dueWrapper);
    List<MarketingCallbackItem> records = page.getRecords().stream().map(item -> {
      MarketingCallbackItem result = new MarketingCallbackItem();
      result.setId(item.getId());
      result.setName(item.getName());
      result.setPhone(item.getPhone());
      result.setSource(item.getSource());
      result.setNextFollowDate(item.getNextFollowDate() == null ? null : item.getNextFollowDate().toString());
      result.setRemark(item.getRemark());
      return result;
    }).toList();

    long todayDue = crmLeadMapper.selectCount(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId)
        .eq(source != null && !source.isBlank(), CrmLead::getSource, normalizeSource(source))
        .eq(staffId != null, CrmLead::getCreatedBy, staffId)
        .eq(CrmLead::getNextFollowDate, today)
        .notIn(CrmLead::getStatus, List.of(2, 3)));
    long overdue = crmLeadMapper.selectCount(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId)
        .eq(source != null && !source.isBlank(), CrmLead::getSource, normalizeSource(source))
        .eq(staffId != null, CrmLead::getCreatedBy, staffId)
        .isNotNull(CrmLead::getNextFollowDate)
        .lt(CrmLead::getNextFollowDate, today)
        .notIn(CrmLead::getStatus, List.of(2, 3)));
    long completed = crmLeadMapper.selectCount(leadWrapperByCreateTime(tenantId, dateFrom, dateTo, source, staffId)
        .eq(CrmLead::getStatus, 2));

    MarketingCallbackReportResponse response = new MarketingCallbackReportResponse();
    response.setTodayDue(todayDue);
    response.setOverdue(overdue);
    response.setCompleted(completed);
    response.setTotal(total);
    response.setRecords(records);
    return response;
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
        .in(CrmLead::getStatus, List.of(0, 1))
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

  private LambdaQueryWrapper<CrmLead> leadWrapperByCreateTime(
      Long tenantId, String dateFrom, String dateTo, String source, Long staffId) {
    LocalDate from = parseDateOrNull(dateFrom);
    LocalDate to = parseDateOrNull(dateTo);
    LocalDateTime fromTime = from == null ? null : from.atStartOfDay();
    LocalDateTime toTime = to == null ? null : to.plusDays(1).atStartOfDay();

    return Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getIsDeleted, 0)
        .eq(tenantId != null, CrmLead::getTenantId, tenantId)
        .eq(source != null && !source.isBlank(), CrmLead::getSource, normalizeSource(source))
        .eq(staffId != null, CrmLead::getCreatedBy, staffId)
        .ge(fromTime != null, CrmLead::getCreateTime, fromTime)
        .lt(toTime != null, CrmLead::getCreateTime, toTime);
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
