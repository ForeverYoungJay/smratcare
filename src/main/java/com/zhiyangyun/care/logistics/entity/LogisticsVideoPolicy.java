package com.zhiyangyun.care.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
@TableName("logistics_video_policy")
public class LogisticsVideoPolicy {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private String provider;
  private Integer retentionDays;
  private Boolean autoCleanup;
  private String basis;
  private java.time.LocalDateTime updateTime;
}
