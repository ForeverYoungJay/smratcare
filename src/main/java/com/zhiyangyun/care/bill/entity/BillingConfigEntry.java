package com.zhiyangyun.care.bill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("billing_config")
public class BillingConfigEntry {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long orgId;

  private String configKey;

  private BigDecimal configValue;

  private String effectiveMonth;

  private Integer status;

  private String remark;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
