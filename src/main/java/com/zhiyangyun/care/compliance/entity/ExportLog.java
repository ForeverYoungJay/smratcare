package com.zhiyangyun.care.compliance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 导出留痕：导出二次确认签发一次性凭证（exportTicket），导出完成后回填行数。
 */
@Data
@TableName("compliance_export_log")
public class ExportLog {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long actorId;
  private String actorName;
  private String actorRole;
  private String module;
  private String scope;
  private String purpose;
  private Integer rowCount;
  private String exportTicket;
  /** PENDING / USED / EXPIRED */
  private String status;
  private LocalDateTime expiresAt;
  private LocalDateTime usedAt;
  private String ip;
  private String userAgent;
  private String requestId;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private Integer isDeleted;
}
