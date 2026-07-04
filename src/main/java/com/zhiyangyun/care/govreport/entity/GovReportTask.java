package com.zhiyangyun.care.govreport.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("gov_report_task")
public class GovReportTask {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String reportType;
  private String channel;
  private String period;
  private String taskStatus;
  private String triggerType;
  private Integer recordCount;
  private String lastError;
  private Integer retryCount;
  private LocalDateTime sentAt;
  private LocalDateTime ackedAt;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
