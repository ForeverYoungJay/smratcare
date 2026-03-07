package com.zhiyangyun.care.store.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class InventoryOutboundSheetCreateRequest {
  private Long orgId;

  private String outboundNo;

  @NotBlank
  private String receiverName;

  private Long elderId;

  private String contractNo;

  private String applyDept;

  private String remark;

  @Valid
  @NotEmpty
  private List<InventoryOutboundSheetItemRequest> items;
}
