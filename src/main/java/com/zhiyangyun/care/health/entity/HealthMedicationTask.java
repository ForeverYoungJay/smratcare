package com.zhiyangyun.care.health.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("health_medication_task")
public class HealthMedicationTask {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long settingId;
  private Long elderId;
  private String elderName;
  private Long drugId;
  private String drugName;
  private LocalDateTime plannedTime;
  private LocalDate taskDate;
  private String status;
  private Long registrationId;
  private LocalDateTime doneTime;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
