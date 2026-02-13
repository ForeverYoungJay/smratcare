package com.zhiyangyun.care.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("survey_submission")
public class SurveySubmission {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private Long templateId;

  private String targetType;

  private Long targetId;

  private Long relatedStaffId;

  private Long submitterId;

  private String submitterName;

  private String submitterRole;

  private Integer anonymousFlag;

  private Integer scoreTotal;

  private Integer scoreEffective;

  private Integer status;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
