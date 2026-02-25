package com.zhiyangyun.care.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.finance.entity.AdmissionFeeAudit;
import com.zhiyangyun.care.finance.entity.ConsumptionRecord;
import com.zhiyangyun.care.finance.entity.DischargeFeeAudit;
import com.zhiyangyun.care.finance.entity.DischargeSettlement;
import com.zhiyangyun.care.finance.entity.MonthlyAllocation;
import com.zhiyangyun.care.finance.model.AdmissionFeeAuditCreateRequest;
import com.zhiyangyun.care.finance.model.ConsumptionRecordCreateRequest;
import com.zhiyangyun.care.finance.model.DischargeFeeAuditCreateRequest;
import com.zhiyangyun.care.finance.model.DischargeSettlementConfirmRequest;
import com.zhiyangyun.care.finance.model.DischargeSettlementCreateRequest;
import com.zhiyangyun.care.finance.model.FeeAuditReviewRequest;
import com.zhiyangyun.care.finance.model.MonthlyAllocationCreateRequest;

public interface FeeManagementService {
  IPage<AdmissionFeeAudit> admissionAuditPage(Long orgId, long pageNo, long pageSize, Long elderId, String status);

  AdmissionFeeAudit createAdmissionAudit(Long orgId, Long operatorId, AdmissionFeeAuditCreateRequest request);

  AdmissionFeeAudit reviewAdmissionAudit(Long orgId, Long operatorId, Long id, FeeAuditReviewRequest request);

  IPage<DischargeFeeAudit> dischargeAuditPage(Long orgId, long pageNo, long pageSize, Long elderId, String status);

  DischargeFeeAudit createDischargeAudit(Long orgId, Long operatorId, DischargeFeeAuditCreateRequest request);

  DischargeFeeAudit reviewDischargeAudit(Long orgId, Long operatorId, Long id, FeeAuditReviewRequest request);

  IPage<DischargeSettlement> dischargeSettlementPage(Long orgId, long pageNo, long pageSize, Long elderId, String status);

  DischargeSettlement createDischargeSettlement(Long orgId, Long operatorId, DischargeSettlementCreateRequest request);

  DischargeSettlement confirmDischargeSettlement(Long orgId, Long operatorId, Long id,
      DischargeSettlementConfirmRequest request);

  IPage<ConsumptionRecord> consumptionPage(Long orgId, long pageNo, long pageSize, Long elderId,
      String from, String to, String category);

  ConsumptionRecord createConsumption(Long orgId, Long operatorId, ConsumptionRecordCreateRequest request);

  IPage<MonthlyAllocation> monthlyAllocationPage(Long orgId, long pageNo, long pageSize, String month, String status);

  MonthlyAllocation createMonthlyAllocation(Long orgId, Long operatorId, MonthlyAllocationCreateRequest request);
}
