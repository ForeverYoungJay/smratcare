package com.zhiyangyun.care.store.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("product")
public class Product {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long orgId;

  private String productCode;

  private String productName;

  private String category;

  private String unit;

  private BigDecimal price;

  private Integer pointsPrice;

  private Integer safetyStock;

  private Integer status;

  private String tagIds;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
