package com.zhiyangyun.care.finance.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 财务提醒：dedupeKey 保证同类型同周期同对象只生成一条。 */
@Data
@TableName("finance_reminder")
public class FinanceReminder {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String reminderType;
  private String dedupeKey;
  private String title;
  private String content;
  /** INFO / WARNING / DANGER。 */
  private String severity;
  private String bizMonth;
  private Long elderId;
  private Long roomId;
  private BigDecimal amount;
  private String actionPath;
  /** PENDING / HANDLED。 */
  private String status;
  private LocalDateTime handledAt;
  private Long handledBy;
  private String handleRemark;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
