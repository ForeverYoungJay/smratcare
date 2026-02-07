package com.zhiyangyun.care.elder.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("elder")
public class ElderProfile {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long orgId;

  private String elderCode;

  private String elderQrCode;

  private String fullName;

  private String idCardNo;

  private Integer gender;

  private LocalDate birthDate;

  private String phone;

  private LocalDate admissionDate;

  private Integer status;

  private Long bedId;

  private String careLevel;

  private String remark;

  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;

  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
