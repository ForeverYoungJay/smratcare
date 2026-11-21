package com.zhiyangyun.care.crm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.service.ContractSignedLinkageService;
import com.zhiyangyun.care.crm.service.CrmTraceService;
import com.zhiyangyun.care.crm.service.impl.CrmContractServiceImpl;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mockito.ArgumentCaptor;

class CrmContractExpiryReminderQueryTest {

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  void expiryReminderFiltersActiveUnrenewedContractsUpToDate() {
    TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), CrmContract.class);
    CrmContractMapper contractMapper = mock(CrmContractMapper.class);
    when(contractMapper.selectPage(any(), any())).thenReturn(new Page<>(1, 20));
    CrmContractServiceImpl service = new CrmContractServiceImpl(
        contractMapper,
        mock(CrmLeadMapper.class),
        mock(ElderAdmissionMapper.class),
        mock(AssessmentRecordMapper.class),
        mock(ContractSignedLinkageService.class),
        mock(AuditLogService.class),
        mock(CrmTraceService.class));
    LocalDate to = LocalDate.of(2026, 12, 16);

    service.page(1L, 1, 20, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, to);

    ArgumentCaptor<Wrapper> captor = ArgumentCaptor.forClass(Wrapper.class);
    verify(contractMapper).selectPage(any(), captor.capture());
    Wrapper<?> wrapper = captor.getValue();
    String sql = wrapper.getCustomSqlSegment();
    assertThat(sql).contains("status IN", "effective_to IS NOT NULL", "effective_to <=", "NOT EXISTS",
        "ORDER BY effective_to ASC");
    assertThat(((AbstractWrapper<?, ?, ?>) wrapper).getParamNameValuePairs().values())
        .contains("SIGNED", "EFFECTIVE", to);
  }
}
