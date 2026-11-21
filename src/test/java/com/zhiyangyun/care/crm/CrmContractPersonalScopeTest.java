package com.zhiyangyun.care.crm;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.service.ContractSignedLinkageService;
import com.zhiyangyun.care.crm.service.CrmTraceService;
import com.zhiyangyun.care.crm.service.impl.CrmContractServiceImpl;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class CrmContractPersonalScopeTest {
  @AfterEach
  void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void ordinaryMarketingEmployeeCannotReadAnotherEmployeesContract() {
    authenticateEmployee(7L);
    CrmContractMapper contractMapper = mock(CrmContractMapper.class);
    CrmLeadMapper leadMapper = mock(CrmLeadMapper.class);
    CrmContract contract = new CrmContract();
    contract.setId(20L);
    contract.setTenantId(1L);
    contract.setLeadId(10L);
    contract.setCreatedBy(8L);
    contract.setIsDeleted(0);
    CrmLead lead = new CrmLead();
    lead.setId(10L);
    lead.setTenantId(1L);
    lead.setOwnerStaffId(8L);
    lead.setCreatedBy(8L);
    lead.setIsDeleted(0);
    when(contractMapper.selectById(20L)).thenReturn(contract);
    when(leadMapper.selectById(10L)).thenReturn(lead);
    CrmContractServiceImpl service = service(contractMapper, leadMapper);

    assertNull(service.get(1L, 20L));
    lead.setOwnerStaffId(7L);
    assertNotNull(service.get(1L, 20L));
  }

  private CrmContractServiceImpl service(CrmContractMapper contractMapper, CrmLeadMapper leadMapper) {
    return new CrmContractServiceImpl(
        contractMapper,
        leadMapper,
        mock(ElderAdmissionMapper.class),
        mock(AssessmentRecordMapper.class),
        mock(ContractSignedLinkageService.class),
        mock(AuditLogService.class),
        mock(CrmTraceService.class));
  }

  private void authenticateEmployee(Long staffId) {
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(String.valueOf(staffId), null, List.of());
    authentication.setDetails(Map.of("orgId", 1L, "roleCodes", List.of("MARKETING_EMPLOYEE")));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
