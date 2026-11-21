package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.assessment.entity.AssessmentRecord;
import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.service.CrmTraceService;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.model.lifecycle.AdmissionRequest;
import com.zhiyangyun.care.elder.service.lifecycle.ElderLifecycleService;
import com.zhiyangyun.care.finance.entity.AdmissionFeeAudit;
import com.zhiyangyun.care.finance.mapper.AdmissionFeeAuditMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

/** 入住办理 admit 的前置条件：入住评估、费用审核已确认、床位可用；历史补录（无合同记录）不受限。 */
@SpringBootTest
@ActiveProfiles("test")
class ElderAdmissionPreconditionTest {
  private static final Long ORG_ID = 9871L;

  @Autowired private ElderLifecycleService lifecycleService;
  @Autowired private ElderMapper elderMapper;
  @Autowired private CrmContractMapper contractMapper;
  @Autowired private AssessmentRecordMapper assessmentRecordMapper;
  @Autowired private AdmissionFeeAuditMapper feeAuditMapper;
  @Autowired private RoomMapper roomMapper;
  @Autowired private BedMapper bedMapper;
  // H2 测试库缺合同流程日志表、线索表字段落后；入住后的 CRM 同步与本测试无关
  @MockBean private CrmTraceService crmTraceService;
  @MockBean private CrmLeadMapper crmLeadMapper;

  @Test
  void rejects_when_admission_assessment_missing() {
    Long elderId = createElder("评估缺失长者");
    AdmissionRequest request = request(elderId, createContract(elderId, null), createBed(null));
    approveFee(elderId);
    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> lifecycleService.admit(request));
    assertTrue(ex.getMessage().contains("入住评估"), ex.getMessage());
  }

  @Test
  void submitted_assessment_record_satisfies_assessment_gate() {
    Long elderId = createElder("评估已提交长者");
    AssessmentRecord record = new AssessmentRecord();
    record.setTenantId(ORG_ID);
    record.setOrgId(ORG_ID);
    record.setElderId(elderId);
    record.setElderName("评估已提交长者");
    record.setAssessmentType("ADMISSION");
    record.setAssessmentDate(LocalDate.now());
    record.setStatus("COMPLETED");
    record.setIsDeleted(0);
    assessmentRecordMapper.insert(record);
    AdmissionRequest request = request(elderId, createContract(elderId, null), createBed(null));
    // 评估已满足，下一项缺失的是费用
    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> lifecycleService.admit(request));
    assertTrue(ex.getMessage().contains("入住费用"), ex.getMessage());
  }

  @Test
  void rejects_when_fee_audit_not_approved() {
    Long elderId = createElder("费用未审长者");
    AdmissionFeeAudit pending = feeAudit(elderId, "PENDING");
    feeAuditMapper.insert(pending);
    AdmissionRequest request = request(elderId, createContract(elderId, "PENDING_BED_SELECT"), createBed(null));
    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> lifecycleService.admit(request));
    assertTrue(ex.getMessage().contains("入住费用"), ex.getMessage());
  }

  @Test
  void rejects_when_bed_occupied_by_other_elder() {
    Long elderId = createElder("床位冲突长者");
    Long otherId = createElder("床位占用长者");
    approveFee(elderId);
    AdmissionRequest request = request(elderId, createContract(elderId, "PENDING_BED_SELECT"), createBed(otherId));
    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> lifecycleService.admit(request));
    assertTrue(ex.getMessage().contains("床位不可用"), ex.getMessage());
  }

  @Test
  void rejects_bed_held_by_another_pending_contract() {
    Long elderId = createElder("选床冲突长者");
    approveFee(elderId);
    Long bedId = createBed(null);
    // 另一位客户的合同（无线索号）已选这张床、待签署
    CrmContract other = new CrmContract();
    other.setTenantId(ORG_ID);
    other.setOrgId(ORG_ID);
    other.setContractNo("ADM-OTHER-" + System.nanoTime());
    other.setStatus("PENDING");
    other.setFlowStage("PENDING_SIGN");
    other.setReservationBedId(bedId);
    other.setIsDeleted(0);
    contractMapper.insert(other);
    AdmissionRequest request = request(elderId, createContract(elderId, "PENDING_BED_SELECT"), bedId);
    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> lifecycleService.admit(request));
    assertTrue(ex.getMessage().contains("已被其他客户预订"), ex.getMessage());
  }

  @Test
  void contract_type_fee_and_expected_admission_round_trip() {
    CrmContract contract = new CrmContract();
    contract.setTenantId(ORG_ID);
    contract.setOrgId(ORG_ID);
    contract.setContractNo("ADM-FIELDS-" + System.nanoTime());
    contract.setStatus("PENDING");
    contract.setContractType("TRIAL");
    contract.setReferenceMonthlyFee(new BigDecimal("4580.50"));
    contract.setExpectedAdmissionDate(LocalDate.of(2026, 12, 1));
    contract.setIsDeleted(0);
    contractMapper.insert(contract);
    CrmContract saved = contractMapper.selectById(contract.getId());
    assertEquals("TRIAL", saved.getContractType());
    assertEquals(0, new BigDecimal("4580.50").compareTo(saved.getReferenceMonthlyFee()));
    assertEquals(LocalDate.of(2026, 12, 1), saved.getExpectedAdmissionDate());
  }

  @Test
  void rejects_when_no_bed_selected_and_elder_has_none() {
    Long elderId = createElder("未选床位长者");
    approveFee(elderId);
    AdmissionRequest request = request(elderId, createContract(elderId, "PENDING_BED_SELECT"), null);
    IllegalStateException ex = assertThrows(IllegalStateException.class, () -> lifecycleService.admit(request));
    assertTrue(ex.getMessage().contains("缺少床位"), ex.getMessage());
  }

  @Test
  void admits_when_all_preconditions_met() {
    Long elderId = createElder("条件齐全长者");
    approveFee(elderId);
    Long bedId = createBed(null);
    AdmissionRequest request = request(elderId, createContract(elderId, "PENDING_BED_SELECT"), bedId);
    assertNotNull(lifecycleService.admit(request).getId());
    assertEquals(elderId, bedMapper.selectById(bedId).getElderId());
  }

  @Test
  void historical_import_without_contract_record_bypasses_flow_gates() {
    Long elderId = createElder("历史补录长者");
    AdmissionRequest request = request(elderId, "HIST-" + System.nanoTime(), null);
    request.setAllowMissingContractRecord(true);
    assertNotNull(lifecycleService.admit(request).getId());
  }

  private AdmissionRequest request(Long elderId, String contractNo, Long bedId) {
    AdmissionRequest request = new AdmissionRequest();
    request.setTenantId(ORG_ID);
    request.setOrgId(ORG_ID);
    request.setElderId(elderId);
    request.setAdmissionDate(LocalDate.now());
    request.setContractNo(contractNo);
    request.setBedId(bedId);
    request.setCreatedBy(1L);
    return request;
  }

  private Long createElder(String name) {
    ElderProfile elder = new ElderProfile();
    elder.setTenantId(ORG_ID);
    elder.setOrgId(ORG_ID);
    elder.setElderCode("ELD-" + System.nanoTime());
    elder.setElderQrCode("QR-" + System.nanoTime());
    elder.setFullName(name + System.nanoTime());
    elderMapper.insert(elder);
    return elder.getId();
  }

  private String createContract(Long elderId, String flowStage) {
    CrmContract contract = new CrmContract();
    contract.setTenantId(ORG_ID);
    contract.setOrgId(ORG_ID);
    contract.setElderId(elderId);
    contract.setContractNo("ADM-PRE-" + System.nanoTime());
    contract.setStatus("PENDING");
    contract.setFlowStage(flowStage);
    contract.setIsDeleted(0);
    contractMapper.insert(contract);
    return contract.getContractNo();
  }

  private Long createBed(Long occupantElderId) {
    Room room = new Room();
    room.setTenantId(ORG_ID);
    room.setOrgId(ORG_ID);
    room.setBuilding("P栋");
    room.setFloorNo("1层");
    room.setRoomNo("P" + System.nanoTime());
    room.setCapacity(2);
    room.setStatus(1);
    roomMapper.insert(room);
    Bed bed = new Bed();
    bed.setTenantId(ORG_ID);
    bed.setOrgId(ORG_ID);
    bed.setRoomId(room.getId());
    bed.setBedNo("01");
    bed.setBedQrCode("QR-PRE-" + System.nanoTime());
    bed.setStatus(occupantElderId == null ? 1 : 2);
    bed.setElderId(occupantElderId);
    bedMapper.insert(bed);
    return bed.getId();
  }

  private void approveFee(Long elderId) {
    feeAuditMapper.insert(feeAudit(elderId, "APPROVED"));
  }

  private AdmissionFeeAudit feeAudit(Long elderId, String status) {
    AdmissionFeeAudit audit = new AdmissionFeeAudit();
    audit.setTenantId(ORG_ID);
    audit.setOrgId(ORG_ID);
    audit.setElderId(elderId);
    audit.setTotalAmount(BigDecimal.valueOf(3000));
    audit.setDepositAmount(BigDecimal.ZERO);
    audit.setStatus(status);
    audit.setIsDeleted(0);
    return audit;
  }
}
