package com.zhiyangyun.care.store.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("inventory_batch")
public class InventoryBatch {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long orgId;

  private Long productId;

  private Long warehouseId;

  private String batchNo;

  private Integer quantity;

  private BigDecimal costPrice;

  private LocalDate expireDate;

  private String warehouseLocation;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
