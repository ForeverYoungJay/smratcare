package com.zhiyangyun.care.store.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("inventory_adjustment")
public class InventoryAdjustment {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long productId;
  private Long batchId;
  private String inventoryType;
  private String adjustType;
  private Integer adjustQty;
  private String reason;
  private Long operatorStaffId;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
