package com.zhiyangyun.care.medicalcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 巡诊记录（逐长者查体所见与处理意见）。 */
@Data
@TableName("medical_rounds_record")
public class MedicalRoundsRecord {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long planId;
  private Long elderId;
  private String elderName;
  private LocalDateTime roundTime;
  /** 查体所见。 */
  private String findings;
  /** 处理意见。 */
  private String handleOpinion;
  /** NORMAL 正常 / ATTENTION 需关注 / URGENT 需紧急处理。 */
  private String resultLevel;
  /** 关联生成的病程记录（emr_record.id）。 */
  private Long emrRecordId;
  /** 关联生成的医嘱（medical_order.id）。 */
  private Long medicalOrderId;
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
