package com.zhiyangyun.care.crm.service;

import com.zhiyangyun.care.crm.model.report.MarketingCallbackReportResponse;
import com.zhiyangyun.care.crm.model.report.MarketingChannelReportItem;
import com.zhiyangyun.care.crm.model.report.MarketingConsultationTrendItem;
import com.zhiyangyun.care.crm.model.report.MarketingConversionReportResponse;
import com.zhiyangyun.care.crm.model.report.MarketingFollowupReportResponse;
import com.zhiyangyun.care.crm.model.report.MarketingDataQualityResponse;
import java.util.List;

public interface MarketingReportService {
  MarketingConversionReportResponse conversion(
      Long tenantId, String dateFrom, String dateTo, String source, Long staffId);

  List<MarketingConsultationTrendItem> consultationTrend(
      Long tenantId, int days, String dateFrom, String dateTo, String source, Long staffId);

  List<MarketingChannelReportItem> channel(Long tenantId, String dateFrom, String dateTo, Long staffId);

  MarketingFollowupReportResponse followup(
      Long tenantId, String dateFrom, String dateTo, String source, Long staffId);

  MarketingCallbackReportResponse callback(
      Long tenantId, long pageNo, long pageSize, String dateFrom, String dateTo, String source, Long staffId);

  MarketingDataQualityResponse dataQuality(Long tenantId);

  int normalizeSources(Long tenantId);
}
