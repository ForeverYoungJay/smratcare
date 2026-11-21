package com.zhiyangyun.care.hr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("hr_attendance_period")
public class HrAttendancePeriod {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private String period;
  private String status;
  private String lockedByName;
  private LocalDateTime lockedAt;
}
