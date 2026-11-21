package com.zhiyangyun.care.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
@TableName("logistics_camera")
public class LogisticsCamera {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private String cameraCode;
  private String cameraName;
  private String location;
  private String channelRef;
  private String protocol;
  private String responsibleName;
  private Integer enabled;
  private java.math.BigDecimal bitrateMbps;
  private java.time.LocalDateTime createTime;
  private java.time.LocalDateTime updateTime;
}
