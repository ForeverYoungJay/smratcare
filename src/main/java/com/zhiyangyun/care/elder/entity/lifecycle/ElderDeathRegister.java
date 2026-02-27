package com.zhiyangyun.care.elder.entity.lifecycle;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("elder_death_register")
public class ElderDeathRegister {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private String elderName;
  private LocalDate deathDate;
  private LocalDateTime deathTime;
  private String place;
  private String cause;
  private String certificateNo;
  private String reportedBy;
  private LocalDateTime reportedTime;
  private Integer beforeStatus;
  private String status;
  private String remark;
  private Long createdBy;
  private Long updatedBy;
  private Long cancelledBy;
  private LocalDateTime cancelledTime;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
