package com.zhiyangyun.care.fire.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 录像调阅审计 */
@Data
@TableName("fire_video_access_log")
public class FireVideoAccessLog {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long requestId;
  private Long recordingId;
  private String action;
  private Long staffId;
  private String staffName;
  private LocalDateTime createTime;
}
