package com.zhiyangyun.care.hr.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("staff_reward_punishment")
public class StaffRewardPunishment {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long orgId;

  private Long staffId;

  private String staffName;

  private String type;

  private String level;

  private String title;

  private String reason;

  private BigDecimal amount;

  private Integer points;

  private LocalDate occurredDate;

  private String status;

  private String remark;

  private Long createdBy;

  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;

  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
