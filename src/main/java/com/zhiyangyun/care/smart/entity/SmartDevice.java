package com.zhiyangyun.care.smart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("smart_device")
public class SmartDevice {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private String deviceCode;
  private String deviceName;
  private String deviceType;
  private String vendor;
  private String model;
  private String location;
  private String protocol;
  private String bindStatus;
  private String onlineStatus;
  private LocalDateTime lastHeartbeatAt;
  private LocalDateTime lastEventAt;
  private Integer enabled;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
