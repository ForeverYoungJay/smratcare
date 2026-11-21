package com.zhiyangyun.care.elder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmissionProgress;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischargeApply;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderMedicalOutingRecord;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderOutingRecord;
import com.zhiyangyun.care.elder.model.lifecycle.ElderWorkbenchTodoResponse;
import com.zhiyangyun.care.elder.model.lifecycle.ElderWorkbenchTodoResponse.Item;
import com.zhiyangyun.care.elder.service.ElderWorkbenchTodoService.Sources;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ElderWorkbenchTodoServiceTest {
  private static final Long ME = 7L;
  private final LocalDateTime now = LocalDateTime.of(2026, 11, 16, 12, 0);

  private ElderOutingRecord outing(long id, String status, LocalDateTime expected, Long createdBy) {
    ElderOutingRecord r = new ElderOutingRecord();
    r.setId(id);
    r.setElderId(id);
    r.setElderName("外" + id);
    r.setOutingDate(LocalDate.of(2026, 11, 17));
    r.setStatus(status);
    r.setExpectedReturnTime(expected);
    r.setCreatedBy(createdBy);
    return r;
  }

  private ElderDischargeApply apply(long id, String status, LocalDateTime created, LocalDate planned) {
    ElderDischargeApply r = new ElderDischargeApply();
    r.setId(id);
    r.setElderId(id);
    r.setElderName("退" + id);
    r.setStatus(status);
    r.setCreateTime(created);
    r.setPlannedDischargeDate(planned);
    return r;
  }

  private CrmContract contract(long id, LocalDate effectiveTo, Long leadId) {
    CrmContract c = new CrmContract();
    c.setId(id);
    c.setElderId(id);
    c.setElderName("合" + id);
    c.setContractNo("C" + id);
    c.setEffectiveTo(effectiveTo);
    c.setLeadId(leadId);
    return c;
  }

  private Map<String, Item> build() {
    ElderDischargeApply settling = apply(23, "APPROVED", now.minusDays(9), LocalDate.of(2026, 11, 15));
    settling.setCareHandoverTime(now.minusDays(2));
    settling.setItemHandoverTime(now.minusDays(1));
    settling.setAutoDischargeStatus("PENDING_SETTLEMENT");
    // 交接功能上线前已结算、没有交接记录的旧申请：不应算办理超时
    ElderDischargeApply legacy = apply(24, "APPROVED", now.minusDays(30), LocalDate.of(2026, 11, 1));
    legacy.setAutoDischargeStatus("SUCCESS");
    ElderMedicalOutingRecord medical = new ElderMedicalOutingRecord();
    medical.setId(4L);
    medical.setElderId(4L);
    medical.setStatus("OUT");
    medical.setExpectedReturnTime(now.minusHours(1));
    ElderAdmissionProgress returned = new ElderAdmissionProgress();
    returned.setId(50L);
    returned.setElderId(50L);
    returned.setCurrentStep(3);
    returned.setReturnReason("缺体检报告");
    CrmLead lead = new CrmLead();
    lead.setId(900L);
    lead.setOwnerStaffId(ME);
    ElderProfile elder50 = new ElderProfile();
    elder50.setId(50L);
    elder50.setFullName("档案名");
    elder50.setResponsibleStaffId(ME);

    Sources sources = new Sources(
        List.of(outing(1, "OUT", now.minusHours(2), ME), outing(2, "OUT", now.plusHours(2), null), outing(3, "APPLIED", null, null)),
        List.of(medical),
        List.of(
            apply(20, "PENDING", now.minusDays(1), LocalDate.of(2026, 11, 30)),
            apply(21, "PENDING", now.minusDays(4), LocalDate.of(2026, 11, 30)),
            apply(22, "APPROVED", now.minusDays(9), LocalDate.of(2026, 11, 16)),
            settling,
            legacy),
        List.of(contract(30, null, null)),
        List.of(contract(31, LocalDate.of(2026, 12, 1), 900L), contract(32, LocalDate.of(2026, 11, 10), null),
            contract(33, LocalDate.of(2027, 1, 1), null)),
        List.of(returned));
    return ElderWorkbenchTodoService.buildItems(sources, Map.of(50L, elder50), Map.of(900L, lead), ME, now).stream()
        .collect(java.util.stream.Collectors.toMap(Item::getId, i -> i));
  }

  @Test
  void classifiesEachSourceWithLevelDepartmentAndTarget() {
    Map<String, Item> items = build();
    assertEquals(List.of(
            "ADMISSION_RETURNED-50", "CONTRACT_EXPIRED-32", "CONTRACT_EXPIRING-31", "CONTRACT_PENDING_BED-30",
            "DISCHARGE_PENDING-20", "DISCHARGE_PROCESS_OVERDUE-23", "DISCHARGE_REVIEW_OVERDUE-21",
            "MEDICAL_OUTING_OVERDUE-4", "OUTING_OVERDUE-1", "OUTING_PENDING-3"),
        items.keySet().stream().sorted().toList());

    Item outing = items.get("OUTING_OVERDUE-1");
    assertEquals("urgent", outing.getLevel());
    assertEquals("NURSING", outing.getDepartment());
    assertEquals("/elder/bed-panorama?view=records&recordType=OUTING&elderId=1", outing.getPath());
    assertEquals("/elder/bed-panorama?view=records&recordType=MEDICAL_OUTING&elderId=4",
        items.get("MEDICAL_OUTING_OVERDUE-4").getPath());

    assertEquals("high", items.get("DISCHARGE_PENDING-20").getLevel());
    assertEquals(now.minusDays(1).plusDays(3), items.get("DISCHARGE_PENDING-20").getDueTime());
    assertEquals("urgent", items.get("DISCHARGE_REVIEW_OVERDUE-21").getLevel());
    assertEquals("/elder/status-change/discharge-settlement?tab=discharge&elderId=21",
        items.get("DISCHARGE_REVIEW_OVERDUE-21").getPath());
    Item settling = items.get("DISCHARGE_PROCESS_OVERDUE-23");
    assertEquals("FINANCE", settling.getDepartment());
    assertEquals("财务部", settling.getDepartmentName());

    assertEquals("urgent", items.get("CONTRACT_EXPIRED-32").getLevel());
    assertEquals("high", items.get("CONTRACT_EXPIRING-31").getLevel());
    assertEquals("MARKETING", items.get("CONTRACT_EXPIRING-31").getDepartment());
    assertEquals("/elder/detail/31?tab=contract", items.get("CONTRACT_EXPIRING-31").getPath());
    assertEquals("/elder/admission-processing?contractNo=C30", items.get("CONTRACT_PENDING_BED-30").getPath());
    assertEquals("档案名", items.get("ADMISSION_RETURNED-50").getElderName());
  }

  @Test
  void mineMeansResponsibleElderOrCreatorOrLeadOwner() {
    Map<String, Item> items = build();
    assertTrue(items.get("OUTING_OVERDUE-1").isMine());
    assertEquals(ME, items.get("OUTING_OVERDUE-1").getOwnerStaffId());
    assertTrue(items.get("CONTRACT_EXPIRING-31").isMine());
    assertTrue(items.get("ADMISSION_RETURNED-50").isMine());
    assertFalse(items.get("OUTING_PENDING-3").isMine());
    assertFalse(items.get("CONTRACT_EXPIRED-32").isMine());
  }

  @Test
  void summarizeFiltersByScopeAndSortsByLevelThenDue() {
    List<Item> all = List.copyOf(build().values());
    ElderWorkbenchTodoResponse mine = ElderWorkbenchTodoService.summarize(all, "mine", now);
    assertEquals(3, mine.getTotal());
    assertEquals(3, mine.getMineCount());
    assertEquals(10, mine.getAllCount());

    ElderWorkbenchTodoResponse everything = ElderWorkbenchTodoService.summarize(all, "all", now);
    assertEquals(10, everything.getTotal());
    assertEquals(5, everything.getUrgentCount());
    // urgent 中截止最早的是 11-10 到期的合同
    assertEquals("CONTRACT_EXPIRED-32", everything.getItems().get(0).getId());
    assertEquals("normal", everything.getItems().get(everything.getItems().size() - 1).getLevel());
  }
}
