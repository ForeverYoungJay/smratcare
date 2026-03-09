package com.zhiyangyun.care.family.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("family_notify_log")
public class FamilyNotifyLog {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private Long familyUserId;

  private Long elderId;

  private String eventType;

  private String level;

  private String title;

  private String content;

  private String templateId;

  private String touserOpenId;

  private String pagePath;

  private String payloadJson;

  private String responseJson;

  private String status;

  private Integer retryCount;

  private Integer maxRetry;

  private LocalDateTime nextRetryAt;

  private String lastError;

  private Long createdBy;

  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;

  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
