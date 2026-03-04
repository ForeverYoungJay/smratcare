package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FinanceInvoiceReceiptItem {
  private Long paymentId;
  private Long billId;
  private Long elderId;
  private String elderName;
  private BigDecimal amount;
  private String payMethod;
  private String payMethodLabel;
  private String invoiceStatus;
  private String invoiceStatusLabel;
  private String receiptNo;
  private String remark;
  private LocalDateTime paidAt;
}
