package com.zhiyangyun.care.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("care_task_template")
public class CareTaskTemplate {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long orgId;

  private String taskName;

  private Integer frequencyPerDay;

  private String careLevelRequired;

  private Integer enabled;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
