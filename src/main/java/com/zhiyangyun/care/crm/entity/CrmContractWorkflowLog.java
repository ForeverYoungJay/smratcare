package com.zhiyangyun.care.crm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("crm_contract_workflow_log")
public class CrmContractWorkflowLog {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long contractId;
  private Long leadId;
  private String actionType;
  private String beforeStatus;
  private String afterStatus;
  private String beforeFlowStage;
  private String afterFlowStage;
  private String beforeChangeWorkflow;
  private String afterChangeWorkflow;
  private String remark;
  private String snapshotJson;
  private Long operatedBy;
  private String operatedByName;
  private LocalDateTime operatedAt;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
