package com.zhiyangyun.care.store.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class OrderPreviewResponse {
  private String status;
  private boolean allowed;
  private Long elderId;
  private Long productId;
  private String productName;
  private Integer qty;
  private Integer pointsRequired;
  private List<ForbiddenReason> reasons = new ArrayList<>();
  private String message;
}
