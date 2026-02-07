package com.zhiyangyun.care.finance.service;

import com.zhiyangyun.care.finance.model.PaymentRequest;
import com.zhiyangyun.care.finance.model.PaymentResponse;
import com.zhiyangyun.care.finance.model.ReconcileResponse;
import java.time.LocalDate;

public interface FinanceService {
  PaymentResponse pay(Long billId, PaymentRequest request, Long operatorStaffId);

  ReconcileResponse reconcile(Long orgId, LocalDate date);
}
