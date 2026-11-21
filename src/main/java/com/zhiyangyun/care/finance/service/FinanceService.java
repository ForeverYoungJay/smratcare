package com.zhiyangyun.care.finance.service;

import com.zhiyangyun.care.finance.model.BillDeductionPreviewResponse;
import com.zhiyangyun.care.finance.model.PaymentRequest;
import com.zhiyangyun.care.finance.model.PaymentResponse;
import com.zhiyangyun.care.finance.model.ReconcileResponse;
import java.time.LocalDate;

public interface FinanceService {
  PaymentResponse pay(Long billId, PaymentRequest request, Long operatorStaffId);

  PaymentResponse updatePaymentRecord(Long paymentRecordId, PaymentRequest request, Long operatorStaffId);

  void invalidateBill(Long billId, Long operatorStaffId);

  ReconcileResponse reconcile(Long orgId, LocalDate date);

  /** 收款登记前的抵扣可用额度预览。 */
  BillDeductionPreviewResponse deductionPreview(Long billId);
}
