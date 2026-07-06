package com.zhiyangyun.care.ai.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ai_risk_model")
public class AiRiskModel {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  /** FALL/PRESSURE_ULCER/READMISSION */
  private String riskType;
  private String modelName;
  /** 规则JSON：{"factors":[{code,name,weight,bands:[{gte,lte,score}]}],"levels":[{level,gte}]} */
  private String ruleJson;
  private Integer enabled;
  private String remark;
  private Long createdBy;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
