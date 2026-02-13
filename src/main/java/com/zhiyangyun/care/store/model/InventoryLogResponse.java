package com.zhiyangyun.care.store.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class InventoryLogResponse {
  private Long id;
  private Long productId;
  private String productName;
  private Long batchId;
  private String batchNo;
  private String changeType;
  private Integer changeQty;
  private Long refOrderId;
  private Long refAdjustmentId;
  private String outType;
  private String remark;
  private LocalDateTime createTime;
}
