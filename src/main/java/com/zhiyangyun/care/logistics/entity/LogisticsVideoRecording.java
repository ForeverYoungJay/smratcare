package com.zhiyangyun.care.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
@TableName("logistics_video_recording")
public class LogisticsVideoRecording {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long cameraId;
  private String ingestKey;
  private String provider;
  @JsonIgnore
  private String objectKey;
  private java.time.LocalDateTime startedAt;
  private java.time.LocalDateTime endedAt;
  private java.time.LocalDateTime expiresAt;
  private Long sizeBytes;
  private String sha256;
  private Boolean held;
  private String holdReason;
  private String status;
  private java.time.LocalDateTime createTime;
  private java.time.LocalDateTime updateTime;
}
