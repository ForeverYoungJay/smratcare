package com.zhiyangyun.care.life.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("incident_report")
public class IncidentReport {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private Long elderId;

  private String elderName;

  private String reporterName;

  private LocalDateTime incidentTime;

  private String incidentType;

  private String level;

  private String location;

  private String scanText;

  private String attachmentUrls;

  private String voiceUrl;

  private Integer voiceDurationSec;

  private String description;

  private String actionTaken;

  private String emergencyPlan;

  private String onsiteHandling;

  private String familyNotification;

  private String rectificationMeasure;

  private String reviewConclusion;

  private String regulatoryReport;

  private String status;

  private Long createdBy;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
