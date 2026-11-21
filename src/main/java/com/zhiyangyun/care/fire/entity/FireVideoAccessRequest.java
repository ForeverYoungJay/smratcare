package com.zhiyangyun.care.fire.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 录像调阅申请 */
@Data
@TableName("fire_video_access_request")
public class FireVideoAccessRequest {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long cameraId;
  private String cameraName;
  private LocalDateTime rangeStart;
  private LocalDateTime rangeEnd;
  private String reason;
  private Long applicantId;
  private String applicantName;
  private String status;
  private Long approverId;
  private String approverName;
  private LocalDateTime decidedAt;
  private String decisionNote;
  private LocalDateTime expiresAt;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private Integer isDeleted;
}
