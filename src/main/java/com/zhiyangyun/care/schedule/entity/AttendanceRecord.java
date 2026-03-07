package com.zhiyangyun.care.schedule.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("attendance_record")
public class AttendanceRecord {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long staffId;
  private LocalDate workDate;
  private LocalDateTime checkInTime;
  private LocalDateTime checkOutTime;
  private LocalDateTime outingStartTime;
  private LocalDateTime outingEndTime;
  private LocalDateTime lunchBreakStartTime;
  private LocalDateTime lunchBreakEndTime;
  private Integer outingMinutes;
  private Integer reviewed;
  private Long reviewedBy;
  private LocalDateTime reviewedAt;
  private String reviewRemark;
  private String note;
  private String status;
  private Long createdBy;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
