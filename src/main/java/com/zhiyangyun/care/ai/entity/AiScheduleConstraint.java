package com.zhiyangyun.care.ai.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ai_schedule_constraint")
public class AiScheduleConstraint {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private BigDecimal maxWeeklyHours;
  private Integer maxConsecutiveDays;
  /** 夜班后强制休一天 */
  private Integer nightRestEnabled;
  /** 读取请假审批，请假日不排班 */
  private Integer respectLeave;
  /** 班次资质要求 JSON：{"shiftCode":["ROLE_CODE",...]} */
  private String qualificationJson;
  private BigDecimal workloadBalanceWeight;
  private BigDecimal nightFairnessWeight;
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
