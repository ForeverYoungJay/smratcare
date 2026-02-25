package com.zhiyangyun.care.life.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dining_delivery_record")
public class DiningDeliveryRecord {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private Long mealOrderId;

  private String orderNo;

  private Long deliveryAreaId;

  private String deliveryAreaName;

  private Long deliveredBy;

  private String deliveredByName;

  private LocalDateTime deliveredAt;

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
