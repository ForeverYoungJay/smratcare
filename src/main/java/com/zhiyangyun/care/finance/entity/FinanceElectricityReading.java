package com.zhiyangyun.care.finance.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/** 房间月度电费：用电量与电费由读数和电价推导，不接受前端直接写入。 */
@Data
@TableName("finance_electricity_reading")
public class FinanceElectricityReading {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String billMonth;
  private Long roomId;
  private String building;
  private String floorNo;
  private String roomNo;
  private BigDecimal previousReading;
  private BigDecimal currentReading;
  private BigDecimal usageAmount;
  private BigDecimal unitPrice;
  private BigDecimal feeAmount;
  /** ROOM 整间计费 / PER_RESIDENT 按在住人数均摊。 */
  private String shareMode;
  private Integer residentCount;
  private BigDecimal perResidentAmount;
  /** UNPAID / PAID。 */
  private String payStatus;
  private LocalDateTime paidAt;
  private Long paidBy;
  private String payRemark;
  private String remark;
  private Long createdBy;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
