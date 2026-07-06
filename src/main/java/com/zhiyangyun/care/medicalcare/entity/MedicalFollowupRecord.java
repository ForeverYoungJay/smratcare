package com.zhiyangyun.care.medicalcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 慢病随访记录。 */
@Data
@TableName("medical_followup_record")
public class MedicalFollowupRecord {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long planId;
  private Long elderId;
  private String elderName;
  private LocalDate followupDate;
  /** 本次体征值 JSON，如 {"sbp":135,"dbp":85,"fbg":6.2}。 */
  private String vitalJson;
  /** 关联 health 模块体征记录（health_data_record.id）。 */
  private Long healthDataId;
  /** GOOD 良好 / PARTIAL 部分依从 / POOR 差。 */
  private String medicationCompliance;
  /** CONTROLLED 达标 / UNSTABLE 未达标 / WORSENED 恶化。 */
  private String assessmentLevel;
  /** 评估结论。 */
  private String assessment;
  /** 下次随访日期（按计划频次自动排程）。 */
  private LocalDate nextFollowupDate;
  private Long doctorId;
  private String doctorName;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
