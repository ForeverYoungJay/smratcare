package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FinanceVoucherView {
  private Long id;
  private Long elderId;
  private String elderName;
  private String voucherNo;
  private String voucherName;
  private BigDecimal faceAmount;
  private BigDecimal balanceAmount;
  private BigDecimal minBillAmount;
  private Boolean allowSplit;
  private LocalDate validFrom;
  private LocalDate validTo;
  private String status;
  private String statusText;
  private String source;
  private LocalDateTime issuedAt;
  private String revokeReason;
  private String remark;
}
