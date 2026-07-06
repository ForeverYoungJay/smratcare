package com.zhiyangyun.care.medins.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 长者医保电子凭证绑定登记与校验记录。 */
@Data
@TableName("medins_evoucher")
public class MedinsEvoucher {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private String insuredNo;
  private String idCard;
  /** 电子凭证授权令牌引用，不落敏感原文。 */
  private String ecardToken;
  private String channel;
  /** BOUND 已绑定 / UNBOUND 已解绑。 */
  private String bindStatus;
  /** PENDING 待校验 / PASSED 校验通过 / FAILED 校验失败。 */
  private String verifyStatus;
  private String verifyMessage;
  private LocalDateTime lastVerifyAt;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
