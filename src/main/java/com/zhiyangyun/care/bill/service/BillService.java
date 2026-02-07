package com.zhiyangyun.care.bill.service;

import com.zhiyangyun.care.bill.model.BillDetailResponse;
import com.zhiyangyun.care.bill.model.BillGenerateResponse;

public interface BillService {
  BillGenerateResponse generateMonthlyBills(String billMonth);

  BillDetailResponse getBillDetail(Long elderId, String billMonth);
}
