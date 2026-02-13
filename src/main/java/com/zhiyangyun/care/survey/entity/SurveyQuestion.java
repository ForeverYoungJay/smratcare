package com.zhiyangyun.care.survey.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("survey_question_bank")
public class SurveyQuestion {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private String questionCode;

  private String title;

  private String questionType;

  private String optionsJson;

  private Integer requiredFlag;

  private Integer scoreEnabled;

  private Integer maxScore;

  private Integer status;

  private Integer sortNo;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
