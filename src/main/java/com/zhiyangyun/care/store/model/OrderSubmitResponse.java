package com.zhiyangyun.care.store.model;

import lombok.Data;

@Data
public class OrderSubmitResponse {
  private String status;
  private boolean allowed;
  private Long orderId;
  private String orderNo;
  private Integer pointsDeducted;
  private Integer balanceAfter;
  private OrderPreviewResponse preview;
  private String message;
}
