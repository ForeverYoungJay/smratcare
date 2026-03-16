package com.zhiyangyun.care.finance.service;

import com.zhiyangyun.care.finance.model.FinanceCrossPeriodApprovalRequest;
import com.zhiyangyun.care.finance.model.FinanceCrossPeriodApprovalResponse;
import com.zhiyangyun.care.finance.model.FinanceMonthLockStatusResponse;
import com.zhiyangyun.care.finance.model.FinanceMonthUnlockRequest;
import java.time.YearMonth;

public interface FinanceMonthLockService {
  FinanceMonthLockStatusResponse getMonthLockStatus(Long orgId, YearMonth month);

  void assertMonthEditable(Long orgId, YearMonth month, String actionLabel);

  FinanceMonthLockStatusResponse unlockMonth(Long orgId, FinanceMonthUnlockRequest request);

  FinanceCrossPeriodApprovalResponse requestCrossPeriodApproval(Long orgId, FinanceCrossPeriodApprovalRequest request);
}
