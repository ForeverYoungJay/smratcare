package com.zhiyangyun.care.life.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dining_meal_order")
public class DiningMealOrder {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private String orderNo;

  private Long elderId;

  private String elderName;

  private LocalDate orderDate;

  private String mealType;

  private String dishIds;

  private String dishNames;

  private BigDecimal totalAmount;

  private Long prepZoneId;

  private String prepZoneName;

  private Long deliveryAreaId;

  private String deliveryAreaName;

  private Long overrideId;

  private String status;

  private String remark;

  private Long createdBy;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
