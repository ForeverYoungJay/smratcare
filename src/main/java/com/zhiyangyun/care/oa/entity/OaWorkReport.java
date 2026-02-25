package com.zhiyangyun.care.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("oa_work_report")
public class OaWorkReport {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private String title;

  private String reportType;

  private LocalDate reportDate;

  @TableField("period_start_date")
  private LocalDate periodStartDate;

  @TableField("period_end_date")
  private LocalDate periodEndDate;

  private String contentSummary;

  private String completedWork;

  private String riskIssue;

  private String nextPlan;

  private String status;

  private Long reporterId;

  private String reporterName;

  private Long createdBy;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
