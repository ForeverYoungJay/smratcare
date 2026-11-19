package com.zhiyangyun.care.elder.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("bed")
public class Bed {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long roomId;
  private String bedNo;
  private String bedType;
  private String bedQrCode;
  private Integer status;
  // 占用字段释放床位时要写成 NULL；默认 updateById 会跳过 null，导致「空床」上仍挂着长者
  @TableField(updateStrategy = FieldStrategy.IGNORED)
  private Long elderId;
  @TableField(updateStrategy = FieldStrategy.IGNORED)
  private String occupancySource;
  @TableField(updateStrategy = FieldStrategy.IGNORED)
  private String occupancyRefType;
  @TableField(updateStrategy = FieldStrategy.IGNORED)
  private Long occupancyRefId;
  @TableField(updateStrategy = FieldStrategy.IGNORED)
  private LocalDateTime lockExpiresAt;
  @TableField(updateStrategy = FieldStrategy.IGNORED)
  private String occupancyNote;
  private String lastReleaseReason;
  private LocalDateTime lastReleasedAt;
  private Long createdBy;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
