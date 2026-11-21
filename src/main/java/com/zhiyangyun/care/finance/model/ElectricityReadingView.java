package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ElectricityReadingView {
  private Long id;
  private String billMonth;
  private Long roomId;
  private String building;
  private String floorNo;
  private String roomNo;
  private BigDecimal previousReading;
  private BigDecimal currentReading;
  private BigDecimal usageAmount;
  private BigDecimal unitPrice;
  private BigDecimal feeAmount;
  private String shareMode;
  private String shareModeText;
  private Integer residentCount;
  private BigDecimal perResidentAmount;
  private String payStatus;
  private String payStatusText;
  private LocalDateTime paidAt;
  private String payRemark;
  private String remark;
}
