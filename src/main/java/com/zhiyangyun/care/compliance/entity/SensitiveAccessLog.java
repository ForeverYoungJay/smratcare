package com.zhiyangyun.care.compliance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 敏感数据访问/导出审计日志。
 */
@Data
@TableName("compliance_sensitive_access_log")
public class SensitiveAccessLog {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long actorId;
  private String actorName;
  private String actorRole;
  private String action;
  private String dataCategory;
  private String targetType;
  private Long targetId;
  private String targetName;
  private String fields;
  private String purpose;
  private String result;
  private String ip;
  private String userAgent;
  private String requestId;
  private LocalDateTime createTime;
  private Integer isDeleted;
}
