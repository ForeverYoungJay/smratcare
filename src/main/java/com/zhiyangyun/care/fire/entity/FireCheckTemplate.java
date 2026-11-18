package com.zhiyangyun.care.fire.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 消防检查模板 */
@Data
@TableName("fire_check_template")
public class FireCheckTemplate {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private String recordType;
  private String templateName;
  private String items;
  private Integer version;
  private Integer enabled;
  private String updatedByName;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private Integer isDeleted;
}
