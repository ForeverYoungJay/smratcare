package com.zhiyangyun.care.store.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("inventory_outbound_sheet_item")
public class InventoryOutboundSheetItem {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long orgId;

  private Long sheetId;

  private Long productId;

  private Long warehouseId;

  private Long batchId;

  private Integer quantity;

  private String reason;

  private Integer itemSort;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
