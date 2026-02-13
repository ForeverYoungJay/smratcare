package com.zhiyangyun.care.store.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class OrderDetailResponse {
  private Long id;
  private String orderNo;
  private Long elderId;
  private String elderName;
  private BigDecimal totalAmount;
  private BigDecimal payableAmount;
  private Integer pointsUsed;
  private Integer orderStatus;
  private Integer payStatus;
  private LocalDateTime payTime;
  private LocalDateTime createTime;

  private List<OrderLineItem> items;
  private List<RiskReason> riskReasons;
  private List<FifoLogItem> fifoLogs;

  @Data
  public static class OrderLineItem {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
  }

  @Data
  public static class RiskReason {
    private String diseaseName;
    private String tagCode;
    private String tagName;
  }

  @Data
  public static class FifoLogItem {
    private String batchNo;
    private Integer quantity;
    private String expireDate;
  }
}
