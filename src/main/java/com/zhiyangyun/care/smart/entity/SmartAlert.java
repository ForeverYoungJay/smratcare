package com.zhiyangyun.care.smart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("smart_alert")
public class SmartAlert {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private Long deviceId;
  private Long sourceEventId;
  private String alertNo;
  private String alertType;
  private String level;
  private String title;
  private String content;
  private String location;
  /** 联动影像引用（视频厂商适配层预留，如片段URL/ID）。 */
  private String mediaRef;
  /** 联动定位引用（定位厂商适配层预留，如轨迹ID/坐标）。 */
  private String locationRef;
  private String status;
  private LocalDateTime firstTriggeredAt;
  private LocalDateTime latestTriggeredAt;
  private Long acknowledgedBy;
  private LocalDateTime acknowledgedAt;
  private Long resolvedBy;
  private LocalDateTime resolvedAt;
  private String resolutionNote;
  private Integer escalationCount;
  private Integer notifyFamily;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
