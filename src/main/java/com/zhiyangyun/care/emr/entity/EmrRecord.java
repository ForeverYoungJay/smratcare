package com.zhiyangyun.care.emr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("emr_record")
public class EmrRecord {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  /** ADMISSION 入院 / PROGRESS 病程 / DIAGNOSIS 诊断 / EXAM 检查。 */
  private String recordType;
  private LocalDate visitDate;
  private String chiefComplaint;
  private String presentIllness;
  private String pastHistory;
  private String physicalExam;
  private String diagnosis;
  private String treatmentPlan;
  private Long doctorId;
  private String doctorName;
  private String status;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
