package com.zhiyangyun.care.hr.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("hr_staff_monthly_fee_bill")
public class StaffMonthlyFeeBill {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long staffId;
  private String staffNo;
  private String staffName;
  private Long departmentId;
  private String departmentName;
  private String feeMonth;
  private String feeType;
  private String title;
  private BigDecimal quantity;
  private BigDecimal unitPrice;
  private BigDecimal amount;
  private String status;
  private String financeSyncStatus;
  private Long financeSyncId;
  private LocalDateTime financeSyncAt;
  private Long financeSyncBy;
  private String dormitoryBuilding;
  private String dormitoryRoomNo;
  private String dormitoryBedNo;
  private String meterNo;
  private String detailJson;
  private String remark;
  private Long createdBy;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
