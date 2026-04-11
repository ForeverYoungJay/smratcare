package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.zhiyangyun.care.crm.mapper.CrmCallbackPlanMapper;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.mapper.CrmMarketingPlanMapper;
import com.zhiyangyun.care.crm.model.report.MarketingCallbackReportResponse;
import com.zhiyangyun.care.crm.model.report.MarketingConsultationTrendItem;
import com.zhiyangyun.care.crm.model.report.MarketingConversionReportResponse;
import com.zhiyangyun.care.crm.model.report.MarketingDataQualityResponse;
import com.zhiyangyun.care.crm.model.report.MarketingFollowupReportResponse;
import com.zhiyangyun.care.crm.service.CrmTraceService;
import com.zhiyangyun.care.crm.service.impl.MarketingReportServiceImpl;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import java.math.BigDecimal;
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
  private CrmCallbackPlanMapper callbackPlanMapper;

  @Mock
  private CrmContractMapper crmContractMapper;

  @Mock
  private BedMapper bedMapper;

  @Mock
  private CrmMarketingPlanMapper crmMarketingPlanMapper;

  @Mock
  private CrmTraceService crmTraceService;

  private MarketingReportServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new MarketingReportServiceImpl(
        crmLeadMapper,
        callbackPlanMapper,
        crmContractMapper,
        bedMapper,
        crmMarketingPlanMapper,
        crmTraceService);
  }

  @Test
  void conversionShouldReturnExpectedCountsAndRates() {
    CrmLead consult = new CrmLead();
    consult.setStatus(0);
    CrmLead intentA = new CrmLead();
    intentA.setStatus(1);
    CrmLead intentB = new CrmLead();
    intentB.setStatus(1);
    CrmLead reservation = new CrmLead();
    reservation.setStatus(1);
    reservation.setReservationStatus("预定");
    reservation.setReservationAmount(BigDecimal.valueOf(1000));
    CrmLead invalid = new CrmLead();
    invalid.setStatus(3);
    CrmLead signed = new CrmLead();
    signed.setStatus(2);
    signed.setContractSignedFlag(1);
    CrmLead consultB = new CrmLead();
    consultB.setStatus(0);
    CrmLead consultC = new CrmLead();
    consultC.setStatus(0);
    CrmLead consultD = new CrmLead();
    consultD.setStatus(0);
    CrmLead consultE = new CrmLead();
    consultE.setStatus(0);
    when(crmLeadMapper.selectList(any())).thenReturn(List.of(
        consult, intentA, intentB, reservation, invalid, signed, consultB, consultC, consultD, consultE));

    MarketingConversionReportResponse response =
        service.conversion(1L, "2026-01-01", "2026-01-31", null, null);

    assertEquals(10L, response.getTotalLeads());
    assertEquals(5L, response.getConsultCount());
    assertEquals(2L, response.getIntentCount());
    assertEquals(1L, response.getReservationCount());
    assertEquals(1L, response.getInvalidCount());
    assertEquals(1L, response.getContractCount());
    assertEquals(20.0, response.getIntentRate());
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
    CrmLead consult = new CrmLead();
    consult.setStatus(0);
    consult.setNextFollowDate(LocalDate.now().minusDays(1));
    CrmLead intent = new CrmLead();
    intent.setStatus(1);
    intent.setNextFollowDate(LocalDate.now().minusDays(2));
    CrmLead reservation = new CrmLead();
    reservation.setStatus(1);
    reservation.setReservationStatus("锁床");
    reservation.setNextFollowDate(LocalDate.now().plusDays(2));
    CrmLead invalid = new CrmLead();
    invalid.setStatus(3);
    List<CrmLead> leads = List.of(consult, intent, reservation, invalid);
    when(crmLeadMapper.selectList(any())).thenReturn(leads);
    when(crmLeadMapper.selectCount(any())).thenReturn(2L);

    MarketingFollowupReportResponse response =
        service.followup(1L, "2026-02-01", "2026-02-28", null, null);

    assertEquals(4L, response.getTotalLeads());
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

    when(crmLeadMapper.selectList(any()))
        .thenReturn(List.of(lead), List.of(), List.of(lead), List.of(lead));
    when(callbackPlanMapper.selectList(any())).thenReturn(List.of(), List.of());

    MarketingCallbackReportResponse response =
        service.callback(1L, 1, 10, null, null, null, null, null);

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
