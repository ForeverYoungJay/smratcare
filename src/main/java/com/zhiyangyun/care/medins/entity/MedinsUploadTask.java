package com.zhiyangyun.care.medins.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 医保上报任务：报文快照 + 回执内容 + 失败重试计数。 */
@Data
@TableName("medins_upload_task")
public class MedinsUploadTask {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long sheetId;
  private String channel;
  private String bizType;
  private String taskStatus;
  private String payloadJson;
  private String payloadHash;
  private String receiptCode;
  private String receiptJson;
  private String lastError;
  private Integer retryCount;
  private LocalDateTime sentAt;
  private LocalDateTime ackedAt;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
