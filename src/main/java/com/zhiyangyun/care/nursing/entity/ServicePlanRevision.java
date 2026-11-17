package com.zhiyangyun.care.nursing.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("service_plan_revision")
public class ServicePlanRevision {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long planId;
  private Integer revision;
  private String snapshotJson;
  private String changeReason;
  private Long changedBy;
  private String changedByName;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
}
