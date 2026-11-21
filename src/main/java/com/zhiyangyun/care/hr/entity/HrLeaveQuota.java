package com.zhiyangyun.care.hr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("hr_leave_quota")
public class HrLeaveQuota {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long staffId;
  private Integer quotaYear;
  private String leaveType;
  private java.math.BigDecimal quotaDays;
  private String updatedByName;
  private LocalDateTime updateTime;
}
