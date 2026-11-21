package com.zhiyangyun.care.crm.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.crm.constant.CrmReservationRules;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmCallbackPlanMapper;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.model.CrmContractResponse;
import com.zhiyangyun.care.crm.service.ContractSignedLinkageService;
import com.zhiyangyun.care.crm.service.CrmTraceService;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmission;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import com.zhiyangyun.care.elder.model.BedStatus;
import com.zhiyangyun.care.finance.entity.AdmissionFeeAudit;
import com.zhiyangyun.care.finance.mapper.AdmissionFeeAuditMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrmReservationGuardTest {
  private static final LocalDate TODAY = LocalDate.of(2026, 11, 16);
  private CrmLeadMapper leadMapper;
  private CrmContractMapper contractMapper;
  private BedMapper bedMapper;
  private CrmLeadServiceImpl service;

  @BeforeEach
  void setUp() {
    leadMapper = mock(CrmLeadMapper.class);
    contractMapper = mock(CrmContractMapper.class);
    bedMapper = mock(BedMapper.class);
    service = new CrmLeadServiceImpl(leadMapper, mock(CrmCallbackPlanMapper.class), contractMapper, mock(ElderMapper.class),
        mock(ElderAdmissionMapper.class), mock(StaffMapper.class), mock(AuditLogService.class), mock(CrmTraceService.class));
    service.setBedMapper(bedMapper);
    when(bedMapper.selectOne(any())).thenReturn(bed(null, BedStatus.AVAILABLE));
    when(leadMapper.selectList(any())).thenReturn(List.of());
    when(contractMapper.selectCount(any())).thenReturn(0L);
  }

  @Test
  void expiredRefundedOrSignedReservationsNoLongerHoldTheBed() {
    assertTrue(CrmReservationRules.isActiveReservation(reservation(TODAY), TODAY));
    assertFalse(CrmReservationRules.isActiveReservation(reservation(TODAY.minusDays(1)), TODAY));
    CrmLead refunded = reservation(TODAY);
    refunded.setRefunded(1);
    assertFalse(CrmReservationRules.isActiveReservation(refunded, TODAY));
    CrmLead signed = reservation(TODAY);
    signed.setContractSignedFlag(1);
    assertFalse(CrmReservationRules.isActiveReservation(signed, TODAY));
  }

  @Test
  void rejectsBedHeldByAnotherActiveReservation() {
    when(leadMapper.selectList(any())).thenReturn(List.of(reservation(LocalDate.now())));
    IllegalStateException error = assertThrows(IllegalStateException.class,
        () -> service.rejectReservationConflict(1L, 5L, 10L, null));
    assertEquals("所选床位已被其他客户预订或签约占用，请重新选择", error.getMessage());
  }

  @Test
  void rejectsOccupiedBedAndAllowsFreeOrUnchangedBed() {
    when(bedMapper.selectOne(any())).thenReturn(bed(99L, BedStatus.OCCUPIED));
    assertThrows(IllegalStateException.class, () -> service.rejectReservationConflict(1L, 5L, 10L, null));

    when(bedMapper.selectOne(any())).thenReturn(bed(null, BedStatus.AVAILABLE));
    assertDoesNotThrow(() -> service.rejectReservationConflict(1L, 5L, 10L, null));

    // 沿用原床位时不再校验（即使床态已变），避免已有预订因编辑别的字段被拦
    when(bedMapper.selectOne(any())).thenReturn(bed(99L, BedStatus.OCCUPIED));
    assertDoesNotThrow(() -> service.rejectReservationConflict(1L, 6L, 10L, 6L));
  }

  @Test
  void contractPageShowsLatestFinanceConclusionOnly() {
    AdmissionFeeAuditMapper auditMapper = mock(AdmissionFeeAuditMapper.class);
    AdmissionFeeAudit latest = new AdmissionFeeAudit();
    latest.setElderId(42L);
    latest.setStatus("APPROVED");
    AdmissionFeeAudit older = new AdmissionFeeAudit();
    older.setElderId(42L);
    older.setStatus("REJECTED");
    when(auditMapper.selectList(any())).thenReturn(List.of(latest, older));
    CrmContractServiceImpl contracts = new CrmContractServiceImpl(contractMapper, leadMapper, mock(ElderAdmissionMapper.class),
        mock(AssessmentRecordMapper.class), mock(ContractSignedLinkageService.class), mock(AuditLogService.class), mock(CrmTraceService.class));
    contracts.setFeeAuditMapper(auditMapper);
    CrmContractResponse withElder = new CrmContractResponse();
    withElder.setElderId(42L);
    CrmContractResponse withoutElder = new CrmContractResponse();

    contracts.fillFinanceAuditStatus(1L, List.of(withElder, withoutElder));

    assertEquals("APPROVED", withElder.getFinanceAuditStatus());
    assertNull(withoutElder.getFinanceAuditStatus());
  }

  @Test
  void sharedCheckCountsPendingContractsAndActiveLeadsOnly() {
    // 入住办理选床与线索预订共用：本人线索被排除后，只剩已过期的预订 → 不冲突
    when(leadMapper.selectList(any())).thenReturn(List.of(reservation(TODAY.minusDays(3))));
    assertFalse(CrmReservationRules.bedHeldByOthers(leadMapper, contractMapper, 1L, 5L, 10L, 30L, TODAY));
    // 另一份办理中合同占着该床 → 冲突
    when(contractMapper.selectCount(any())).thenReturn(1L);
    assertTrue(CrmReservationRules.bedHeldByOthers(leadMapper, contractMapper, 1L, 5L, 10L, 30L, TODAY));
  }

  @Test
  void contractPageMarksAdmittedByContractNoOrElder() {
    ElderAdmissionMapper admissionMapper = mock(ElderAdmissionMapper.class);
    ElderAdmission byContract = new ElderAdmission();
    byContract.setContractNo("HT-1");
    byContract.setAdmissionDate(TODAY);
    ElderAdmission byElder = new ElderAdmission();
    byElder.setElderId(42L);
    byElder.setAdmissionDate(TODAY.minusDays(2));
    when(admissionMapper.selectList(any())).thenReturn(List.of(byContract, byElder));
    CrmContractServiceImpl contracts = new CrmContractServiceImpl(contractMapper, leadMapper, admissionMapper,
        mock(AssessmentRecordMapper.class), mock(ContractSignedLinkageService.class), mock(AuditLogService.class), mock(CrmTraceService.class));
    CrmContractResponse first = new CrmContractResponse();
    first.setContractNo("HT-1");
    CrmContractResponse second = new CrmContractResponse();
    second.setContractNo("HT-2");
    second.setElderId(42L);
    CrmContractResponse waiting = new CrmContractResponse();
    waiting.setContractNo("HT-3");

    contracts.fillAdmittedDate(1L, List.of(first, second, waiting));

    assertEquals(TODAY, first.getAdmittedDate());
    assertEquals(TODAY.minusDays(2), second.getAdmittedDate());
    assertNull(waiting.getAdmittedDate());
  }

  @Test
  void contractTypeAcceptsKnownCodesOnly() {
    CrmContractServiceImpl contracts = new CrmContractServiceImpl(contractMapper, leadMapper, mock(ElderAdmissionMapper.class),
        mock(AssessmentRecordMapper.class), mock(ContractSignedLinkageService.class), mock(AuditLogService.class), mock(CrmTraceService.class));
    assertEquals("SHORT_TERM", contracts.normalizeContractType(" short_term "));
    assertNull(contracts.normalizeContractType(""));
    assertThrows(IllegalArgumentException.class, () -> contracts.normalizeContractType("VIP"));
  }

  private static CrmLead reservation(LocalDate lockUntil) {
    CrmLead lead = new CrmLead();
    lead.setId(20L);
    lead.setReservationBedId(5L);
    lead.setStatus(2);
    lead.setNextFollowDate(lockUntil);
    return lead;
  }

  private static Bed bed(Long elderId, int status) {
    Bed bed = new Bed();
    bed.setId(5L);
    bed.setElderId(elderId);
    bed.setStatus(status);
    bed.setIsDeleted(0);
    return bed;
  }
}
