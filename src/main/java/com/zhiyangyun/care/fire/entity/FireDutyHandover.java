package com.zhiyangyun.care.fire.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 控制室交接班 */
@Data
@TableName("fire_duty_handover")
public class FireDutyHandover {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private LocalDate shiftDate;
  private String shiftName;
  private Long handoverStaffId;
  private String handoverName;
  private Long takeoverStaffId;
  private String takeoverName;
  private String checklist;
  private String openIssues;
  private String status;
  private LocalDateTime handoverAt;
  private LocalDateTime takeoverAt;
  private String takeoverNote;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private Integer isDeleted;
}
