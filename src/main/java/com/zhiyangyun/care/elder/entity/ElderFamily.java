package com.zhiyangyun.care.elder.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("elder_family")
public class ElderFamily {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long elderId;
  private Long familyUserId;
  private String relation;
  private Integer isPrimary;
  private String remark;
  /** HEALTH,BILLING,PHOTO,PROXY,EMERGENCY；null = 历史绑定，沿用全量可见 */
  private String authScopes;
  private java.time.LocalDate validUntil;
  private String authorizedByName;
  private LocalDateTime authorizedAt;
  private String revokedByName;
  private LocalDateTime revokedAt;
  private String revokeReason;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
