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
@TableName("material_purchase_order")
public class MaterialPurchaseOrder {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long orgId;

  private String orderNo;

  private Long warehouseId;

  private Long supplierId;

  private LocalDate orderDate;

  private String status;

  private BigDecimal totalAmount;

  private String remark;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
