package com.zhiyangyun.care.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("staff")
public class StaffAccount {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long departmentId;
  private String staffNo;
  private String username;
  private String passwordHash;
  private String realName;
  private String phone;
  private String email;
  private Integer gender;
  private Integer status;
  private LocalDateTime lastLoginTime;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
