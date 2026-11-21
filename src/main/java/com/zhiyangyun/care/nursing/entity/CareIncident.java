package com.zhiyangyun.care.nursing.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("care_incident")
public class CareIncident {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String incidentNo;
  private Long elderId;
  private String elderName;
  private String location;
  private String incidentType;
  private String severity;
  private LocalDateTime occurredAt;
  private Long reporterId;
  private String reporterName;
  private String description;
  private String measures;
  private String notifyTargets;
  private Long ownerId;
  private String ownerName;
  private String status;
  private String escalateTo;
  private Long relatedTaskId;
  private Long reviewerId;
  private String reviewerName;
  private String reviewNote;
  private LocalDateTime closedAt;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
