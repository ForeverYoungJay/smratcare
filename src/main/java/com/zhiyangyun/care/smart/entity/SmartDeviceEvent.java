package com.zhiyangyun.care.smart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("smart_device_event")
public class SmartDeviceEvent {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long deviceId;
  private Long elderId;
  private String deviceCode;
  private String eventType;
  private String eventLevel;
  private LocalDateTime eventTime;
  private String payloadJson;
  private String location;
  private Integer handled;
  private Long generatedAlertId;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
