package com.zhiyangyun.care.bill.service;

import com.zhiyangyun.care.bill.model.BillDetailResponse;
import com.zhiyangyun.care.bill.model.BillGenerateResponse;

public interface BillService {
  BillGenerateResponse generateMonthlyBills(String billMonth);

  BillDetailResponse ensureMonthlyBillForElder(
      Long orgId,
      Long elderId,
      String billMonth,
      Long contractId,
      String contractNo);

  BillDetailResponse getBillDetail(Long elderId, String billMonth);

  BillDetailResponse getBillDetailById(Long billId);
}
