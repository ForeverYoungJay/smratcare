package com.zhiyangyun.care.crm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.crm.entity.CrmCallbackPlan;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmCallbackPlanMapper;
import com.zhiyangyun.care.crm.mapper.CrmContractAttachmentMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.mapper.CrmSmsTaskMapper;
import com.zhiyangyun.care.crm.model.action.CrmLeadFollowupRequest;
import com.zhiyangyun.care.crm.service.CrmTraceService;
import com.zhiyangyun.care.crm.service.impl.CrmLeadActionServiceImpl;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class CrmLeadFollowupRecordTest {
  private CrmLeadMapper leadMapper;
  private CrmCallbackPlanMapper planMapper;
  private CrmLeadActionServiceImpl service;

  @BeforeEach
  void setUp() {
    leadMapper = mock(CrmLeadMapper.class);
    planMapper = mock(CrmCallbackPlanMapper.class);
    service = new CrmLeadActionServiceImpl(leadMapper, planMapper, mock(CrmContractAttachmentMapper.class),
        mock(CrmSmsTaskMapper.class), mock(AuditLogService.class), mock(CrmTraceService.class));
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
        "7", null, List.of(new SimpleGrantedAuthority("ROLE_MARKETING_EMPLOYEE"))));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void recordsDoneFollowupAndSchedulesNextReminder() {
    CrmLead lead = lead();
    when(leadMapper.selectById(10L)).thenReturn(lead);
    CrmLeadFollowupRequest request = new CrmLeadFollowupRequest();
    request.setMethod("电话回访");
    request.setFollowupResult("意向持续，需继续跟进");
    request.setFollowupContent("家属关注护理费用");
    request.setNextFollowTime("2026-11-17 10:00:00");

    service.recordFollowup(1L, 1L, 7L, 10L, request);

    ArgumentCaptor<CrmCallbackPlan> captor = ArgumentCaptor.forClass(CrmCallbackPlan.class);
    verify(planMapper, times(2)).insert(captor.capture());
    assertEquals("DONE", captor.getAllValues().get(0).getStatus());
    assertEquals("电话回访", captor.getAllValues().get(0).getTitle());
    assertEquals("PENDING", captor.getAllValues().get(1).getStatus());
    assertEquals(LocalDate.of(2026, 11, 17), lead.getNextFollowDate());
    assertEquals("待回访", lead.getFollowupStatus());
  }

  @Test
  void withoutNextTimeOnlyRecordsAndClearsReminder() {
    CrmLead lead = lead();
    lead.setNextFollowDate(LocalDate.of(2026, 11, 1));
    when(leadMapper.selectById(10L)).thenReturn(lead);
    CrmLeadFollowupRequest request = new CrmLeadFollowupRequest();
    request.setMethod("微信沟通");

    service.recordFollowup(1L, 1L, 7L, 10L, request);

    verify(planMapper, times(1)).insert(org.mockito.ArgumentMatchers.any(CrmCallbackPlan.class));
    assertNull(lead.getNextFollowDate());
    assertEquals("已回访", lead.getFollowupStatus());
  }

  @Test
  void rejectsLeadOwnedByAnotherEmployee() {
    CrmLead lead = lead();
    lead.setOwnerStaffId(99L);
    when(leadMapper.selectById(10L)).thenReturn(lead);
    assertNull(service.recordFollowup(1L, 1L, 7L, 10L, new CrmLeadFollowupRequest()));
    verify(planMapper, times(0)).insert(org.mockito.ArgumentMatchers.any(CrmCallbackPlan.class));
  }

  private CrmLead lead() {
    CrmLead lead = new CrmLead();
    lead.setId(10L);
    lead.setTenantId(1L);
    lead.setOrgId(1L);
    lead.setOwnerStaffId(7L);
    lead.setIsDeleted(0);
    lead.setStatus(1);
    return lead;
  }
}
