package com.zhiyangyun.care.medins.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("medins_channel_config")
public class MedinsChannelConfig {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String channel;
  private String regionCode;
  private String endpoint;
  private String appId;
  /** 密钥引用（密钥库键名），不落明文密钥。 */
  private String secretRef;
  private Integer enabled;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
