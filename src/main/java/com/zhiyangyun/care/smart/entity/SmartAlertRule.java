package com.zhiyangyun.care.smart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("smart_alert_rule")
public class SmartAlertRule {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String ruleCode;
  private String ruleName;
  private String eventType;
  private String deviceType;
  private String metricKey;
  /** PRESENT / GT / LT / GE / LE / EQ / RANGE_OUT。 */
  private String operator;
  private BigDecimal threshold;
  private BigDecimal threshold2;
  private Integer durationSec;
  private String level;
  private String disabilityLevelScope;
  private Integer autoDispatch;
  private Integer notifyFamily;
  private Integer priority;
  private Integer enabled;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
