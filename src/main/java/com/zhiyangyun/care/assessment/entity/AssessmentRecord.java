package com.zhiyangyun.care.assessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("assessment_record")
public class AssessmentRecord {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private Long elderId;

  private String elderName;

  private String assessmentType;

  private Long templateId;

  private String levelCode;

  private BigDecimal score;

  private LocalDate assessmentDate;

  private LocalDate nextAssessmentDate;

  private Long assessorId;

  private String assessorName;

  private String status;

  private String resultSummary;

  private String suggestion;

  private String detailJson;

  private Integer scoreAuto;

  private String archiveNo;

  private String source;

  private Long createdBy;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
