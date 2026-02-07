package com.zhiyangyun.care.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("care_task_execute_log")
public class CareTaskExecuteLog {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long orgId;

  private Long taskDailyId;

  private Long elderId;

  private Long bedId;

  private Long staffId;

  private LocalDateTime executeTime;

  private String bedQrCode;

  private Integer resultStatus;

  private Integer suspiciousFlag;

  private String remark;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
