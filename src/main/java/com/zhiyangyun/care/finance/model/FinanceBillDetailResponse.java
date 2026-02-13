package com.zhiyangyun.care.finance.model;

import com.zhiyangyun.care.bill.model.BillItemDetail;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceBillDetailResponse {
  private Long billId;
  private Long elderId;
  private String elderName;
  private String billMonth;
  private BigDecimal totalAmount;
  private BigDecimal paidAmount;
  private BigDecimal outstandingAmount;
  private Integer status;
  private List<BillItemDetail> items = new ArrayList<>();
  private List<PaymentRecordItem> payments = new ArrayList<>();
  private List<StoreOrderSummary> storeOrders = new ArrayList<>();
}
