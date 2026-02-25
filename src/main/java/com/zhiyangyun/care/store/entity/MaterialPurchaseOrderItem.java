package com.zhiyangyun.care.store.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("material_purchase_order_item")
public class MaterialPurchaseOrderItem {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long orgId;

  private Long orderId;

  private Long productId;

  private String productNameSnapshot;

  private Integer quantity;

  private BigDecimal unitPrice;

  private BigDecimal amount;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
