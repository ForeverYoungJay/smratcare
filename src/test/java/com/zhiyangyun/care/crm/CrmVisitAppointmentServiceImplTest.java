package com.zhiyangyun.care.crm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.entity.CrmVisitAppointment;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.mapper.CrmVisitAppointmentMapper;
import com.zhiyangyun.care.crm.model.visit.CrmVisitAppointmentCompleteRequest;
import com.zhiyangyun.care.crm.model.visit.CrmVisitAppointmentRequest;
import com.zhiyangyun.care.crm.service.impl.CrmVisitAppointmentServiceImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CrmVisitAppointmentServiceImplTest {
  private CrmVisitAppointmentMapper appointmentMapper;
  private CrmLeadMapper leadMapper;
  private CrmVisitAppointmentServiceImpl service;

  @BeforeEach
  void setUp() {
    appointmentMapper = mock(CrmVisitAppointmentMapper.class);
    leadMapper = mock(CrmLeadMapper.class);
    service = new CrmVisitAppointmentServiceImpl(appointmentMapper, leadMapper);
  }

  @Test
  void createAppointmentAlsoMarksLeadForFollowup() {
    CrmLead lead = lead(10L, 7L);
    when(leadMapper.selectById(10L)).thenReturn(lead);
    when(appointmentMapper.selectCount(any())).thenReturn(0L);
    CrmVisitAppointmentRequest request = new CrmVisitAppointmentRequest();
    request.setLeadId(10L);
    request.setAppointmentTime("2026-11-06 10:30:00");
    request.setVisitorName("王女士");

    service.create(1L, 1L, 7L, false, request);

    assertEquals("已预约参观", lead.getFollowupStatus());
    assertEquals(LocalDate.of(2026, 11, 6), lead.getNextFollowDate());
    verify(leadMapper).updateById(lead);
  }

  @Test
  void completingArrivalPersistsResultAndNextFollowDate() {
    CrmLead lead = lead(10L, 7L);
    CrmVisitAppointment appointment = new CrmVisitAppointment();
    appointment.setId(20L);
    appointment.setTenantId(1L);
    appointment.setLeadId(10L);
    appointment.setOwnerStaffId(7L);
    appointment.setCreatedBy(7L);
    appointment.setIsDeleted(0);
    appointment.setStatus("ARRIVED");
    appointment.setAppointmentTime(LocalDateTime.of(2026, 11, 6, 10, 30));
    when(appointmentMapper.selectById(20L)).thenReturn(appointment);
    when(leadMapper.selectById(10L)).thenReturn(lead);
    CrmVisitAppointmentCompleteRequest request = new CrmVisitAppointmentCompleteRequest();
    request.setVisitResult("有入住意向，等待家属确认");
    request.setNextFollowDate("2026-11-08");

    service.complete(1L, 7L, false, 20L, request);

    ArgumentCaptor<CrmVisitAppointment> captor = ArgumentCaptor.forClass(CrmVisitAppointment.class);
    verify(appointmentMapper).updateById(captor.capture());
    assertEquals("COMPLETED", captor.getValue().getStatus());
    assertEquals(LocalDate.of(2026, 11, 8), lead.getNextFollowDate());
    assertEquals("参观完成", lead.getFollowupStatus());
  }

  private CrmLead lead(Long id, Long ownerStaffId) {
    CrmLead lead = new CrmLead();
    lead.setId(id);
    lead.setTenantId(1L);
    lead.setOwnerStaffId(ownerStaffId);
    lead.setCreatedBy(ownerStaffId);
    lead.setIsDeleted(0);
    lead.setName("测试客户");
    return lead;
  }
}
