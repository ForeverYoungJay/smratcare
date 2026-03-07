package com.zhiyangyun.care.store.model;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class InventoryOutboundSheetResponse {
  private Long id;
  private String outboundNo;
  private String receiverName;
  private Long elderId;
  private String contractNo;
  private String applyDept;
  private Long operatorStaffId;
  private String operatorName;
  private String status;
  private String remark;
  private Long confirmStaffId;
  private LocalDateTime confirmTime;
  private LocalDateTime createTime;
  private List<InventoryOutboundSheetItemResponse> items;
}
