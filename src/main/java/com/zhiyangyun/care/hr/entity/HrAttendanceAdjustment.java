package com.zhiyangyun.care.hr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("hr_attendance_adjustment")
public class HrAttendanceAdjustment {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long recordId;
  private Long staffId;
  private java.time.LocalDate workDate;
  private LocalDateTime newCheckIn;
  private LocalDateTime newCheckOut;
  private String newStatus;
  private String reason;
  private String status;
  private Long requestedBy;
  private String requestedByName;
  private String decidedByName;
  private LocalDateTime decidedAt;
  private String decisionNote;
  private LocalDateTime createTime;
}
