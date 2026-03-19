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
@TableName("hr_staff_service_plan")
public class StaffServicePlan {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long staffId;
  private String staffNo;
  private String staffName;
  private Long departmentId;
  private String departmentName;
  private Integer breakfastEnabled;
  private BigDecimal breakfastUnitPrice;
  private Integer breakfastDaysPerMonth;
  private Integer lunchEnabled;
  private BigDecimal lunchUnitPrice;
  private Integer lunchDaysPerMonth;
  private Integer dinnerEnabled;
  private BigDecimal dinnerUnitPrice;
  private Integer dinnerDaysPerMonth;
  private Integer liveInDormitory;
  private String dormitoryBuilding;
  private String dormitoryRoomNo;
  private String dormitoryBedNo;
  private String meterNo;
  private String status;
  private String remark;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
