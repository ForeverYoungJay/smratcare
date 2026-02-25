package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.model.report.MarketingCallbackReportResponse;
import com.zhiyangyun.care.crm.model.report.MarketingConsultationTrendItem;
import com.zhiyangyun.care.crm.model.report.MarketingConversionReportResponse;
import com.zhiyangyun.care.crm.model.report.MarketingDataQualityResponse;
import com.zhiyangyun.care.crm.model.report.MarketingFollowupReportResponse;
import com.zhiyangyun.care.crm.service.impl.MarketingReportServiceImpl;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarketingReportServiceTest {

  @Mock
  private CrmLeadMapper crmLeadMapper;
  @Mock
  private ElderAdmissionMapper admissionMapper;

  private MarketingReportServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new MarketingReportServiceImpl(crmLeadMapper, admissionMapper);
  }

  @Test
  void conversionShouldReturnExpectedCountsAndRates() {
    when(crmLeadMapper.selectCount(any())).thenReturn(10L, 4L, 3L, 2L, 1L);
    when(admissionMapper.selectCount(any())).thenReturn(2L);

    MarketingConversionReportResponse response =
        service.conversion(1L, "2026-01-01", "2026-01-31", null, null);

    assertEquals(10L, response.getTotalLeads());
    assertEquals(4L, response.getConsultCount());
    assertEquals(3L, response.getIntentCount());
    assertEquals(2L, response.getReservationCount());
    assertEquals(1L, response.getInvalidCount());
    assertEquals(2L, response.getContractCount());
    assertEquals(30.0, response.getIntentRate());
  }

  @Test
  void consultationTrendShouldAggregateByDate() {
    CrmLead a = new CrmLead();
    a.setCreateTime(LocalDateTime.of(2026, 2, 1, 10, 0));
    a.setStatus(0);
    CrmLead b = new CrmLead();
    b.setCreateTime(LocalDateTime.of(2026, 2, 1, 11, 0));
    b.setStatus(1);
    CrmLead c = new CrmLead();
    c.setCreateTime(LocalDateTime.of(2026, 2, 2, 9, 0));
    c.setStatus(0);
    when(crmLeadMapper.selectList(any())).thenReturn(List.of(a, b, c));

    List<MarketingConsultationTrendItem> items =
        service.consultationTrend(1L, 14, "2026-02-01", "2026-02-02", null, null);

    assertEquals(2, items.size());
    assertEquals(2L, items.get(0).getTotal());
    assertEquals(1L, items.get(0).getConsultCount());
    assertEquals(1L, items.get(0).getIntentCount());
    assertEquals(1L, items.get(1).getTotal());
  }

  @Test
  void followupShouldReturnStagesAndOverdue() {
    when(crmLeadMapper.selectCount(any())).thenReturn(20L, 8L, 5L, 4L, 3L, 2L);

    MarketingFollowupReportResponse response =
        service.followup(1L, "2026-02-01", "2026-02-28", null, null);

    assertEquals(20L, response.getTotalLeads());
    assertEquals(2L, response.getOverdueCount());
    assertNotNull(response.getStages());
    assertEquals(4, response.getStages().size());
  }

  @Test
  void callbackShouldSupportPaging() {
    CrmLead lead = new CrmLead();
    lead.setId(100L);
    lead.setName("张三");
    lead.setPhone("13800000000");
    lead.setSource("抖音");
    lead.setNextFollowDate(LocalDate.now().minusDays(1));
    lead.setRemark("待回访");

    IPage<CrmLead> page = new Page<>(1, 10, 1);
    page.setRecords(List.of(lead));
    when(crmLeadMapper.selectPage(any(), any())).thenReturn(page);
    when(crmLeadMapper.selectCount(any())).thenReturn(1L, 0L, 1L, 3L);

    MarketingCallbackReportResponse response =
        service.callback(1L, 1, 10, null, null, null, null);

    assertEquals(1L, response.getTotal());
    assertEquals(1, response.getRecords().size());
    assertEquals("张三", response.getRecords().get(0).getName());
  }

  @Test
  void dataQualityShouldReturnCounts() {
    when(crmLeadMapper.selectCount(any())).thenReturn(2L, 5L);
    CrmLead a = new CrmLead();
    a.setSource("unknown_channel");
    CrmLead b = new CrmLead();
    b.setSource("抖音");
    when(crmLeadMapper.selectList(any())).thenReturn(List.of(a, b));

    MarketingDataQualityResponse response = service.dataQuality(1L);
    assertEquals(2L, response.getMissingSourceCount());
    assertEquals(5L, response.getMissingNextFollowDateCount());
    assertEquals(1L, response.getNonStandardSourceCount());
  }
}
