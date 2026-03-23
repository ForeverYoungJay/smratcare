package com.zhiyangyun.care.oa.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("oa_activity_plan")
public class OaActivityPlan {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private String title;

  private String activityType;

  private LocalDate planDate;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private String location;

  private String organizer;

  private String participantTarget;

  private String participantTags;

  private String participantElderIds;

  private Integer estimatedCount;

  private Boolean needMedicalStaff;

  private String riskLevel;

  private Boolean needBudget;

  private BigDecimal budgetAmount;

  private String budgetDescription;

  private String status;

  private String content;

  private String schemeAttachmentName;

  private String schemeAttachmentUrl;

  private String approvalLogsJson;

  private Integer currentApprovalStep;

  private String currentApproverRole;

  private Long currentApproverId;

  private String currentApproverName;

  private LocalDateTime submittedAt;

  private LocalDateTime approvedAt;

  private LocalDateTime startedAt;

  private LocalDateTime completedAt;

  private String signInRecordsJson;

  private Integer actualCount;

  private String photoUrlsJson;

  private String executionRemark;

  private BigDecimal actualExpense;

  private String effectEvaluation;

  private String elderFeedback;

  private String riskSituation;

  private String reportContent;

  private String remark;

  private Long createdBy;

  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;

  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
