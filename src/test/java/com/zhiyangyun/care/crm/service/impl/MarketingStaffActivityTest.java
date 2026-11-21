package com.zhiyangyun.care.crm.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.entity.CrmVisitAppointment;
import com.zhiyangyun.care.crm.model.report.MarketingStaffActivityResponse;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class MarketingStaffActivityTest {
  private static final LocalDate TODAY = LocalDate.of(2026, 11, 16);

  @Test
  void groupsLeadsVisitsAndSigningsByOwnerAndCountsOverdue() {
    CrmLead overdue = lead("李经理", 1, TODAY.minusDays(1), "待回访");
    CrmLead done = lead("李经理", 1, TODAY.minusDays(3), "已回访");
    CrmLead invalid = lead("李经理", 3, TODAY.minusDays(3), "待回访");
    CrmLead unassigned = lead(null, 0, null, null);
    CrmVisitAppointment v1 = visit(1L, "王经理");
    CrmVisitAppointment v2 = visit(1L, "王经理");
    CrmContract signed = new CrmContract();
    signed.setMarketerName("李经理");

    MarketingStaffActivityResponse result = MarketingReportServiceImpl.aggregateStaffActivity(
        List.of(overdue, done, invalid, unassigned), List.of(v1, v2), List.of(signed), TODAY);

    assertEquals(2, result.getArrivedVisitCount());
    assertEquals(1, result.getArrivedLeadCount());
    MarketingStaffActivityResponse.StaffRow li = result.getStaff().get(0);
    assertEquals("李经理", li.getName());
    assertEquals(2, li.getLeads());
    assertEquals(1, li.getOverdue());
    assertEquals(1, li.getContracts());
    assertEquals(List.of("李经理", "未分配", "王经理"), result.getStaff().stream().map(MarketingStaffActivityResponse.StaffRow::getName).toList());
  }

  private static CrmLead lead(String owner, int status, LocalDate next, String followupStatus) {
    CrmLead lead = new CrmLead();
    lead.setOwnerStaffName(owner);
    lead.setStatus(status);
    lead.setNextFollowDate(next);
    lead.setFollowupStatus(followupStatus);
    return lead;
  }

  private static CrmVisitAppointment visit(Long leadId, String owner) {
    CrmVisitAppointment visit = new CrmVisitAppointment();
    visit.setLeadId(leadId);
    visit.setOwnerStaffName(owner);
    return visit;
  }
}
