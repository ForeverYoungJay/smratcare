package com.zhiyangyun.care.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("survey_template")
public class SurveyTemplate {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private String templateCode;

  private String templateName;

  private String description;

  private String targetType;

  private Integer status;

  private LocalDate startDate;

  private LocalDate endDate;

  private Integer anonymousFlag;

  private Integer scoreEnabled;

  private Integer totalScore;

  private Long createdBy;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
