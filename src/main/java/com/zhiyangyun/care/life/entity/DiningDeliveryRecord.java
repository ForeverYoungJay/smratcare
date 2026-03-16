package com.zhiyangyun.care.life.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.util.List;
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

  private LocalDateTime signedAt;

  @JsonIgnore
  @TableField("signoff_image_urls")
  private String signoffImageUrlsText;

  @TableField(exist = false)
  private List<String> signoffImageUrls;

  private LocalDateTime qrScanAt;

  private String status;

  private String failureReason;

  private String redispatchStatus;

  private LocalDateTime redispatchAt;

  private String redispatchByName;

  private String redispatchRemark;

  private String remark;

  private Long createdBy;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
