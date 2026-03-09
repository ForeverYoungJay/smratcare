package com.zhiyangyun.care.family.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("family_sms_code_log")
public class FamilySmsCodeLog {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private Long familyUserId;

  private String phone;

  private String scene;

  private String clientIp;

  private String bizNo;

  private String codeHash;

  private String status;

  private String provider;

  private String providerMessageId;

  private String providerRequest;

  private String providerResponse;

  private Integer verifyAttempts;

  private Integer maxAttempts;

  private LocalDateTime expiresAt;

  private LocalDateTime verifiedAt;

  private LocalDateTime usedAt;

  private String lastError;

  private Long createdBy;

  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;

  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
