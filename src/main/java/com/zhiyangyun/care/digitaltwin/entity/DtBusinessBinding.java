package com.zhiyangyun.care.digitaltwin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dt_business_binding")
public class DtBusinessBinding {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private String assetCode;
  private String sourceModule;
  private String entityType;
  private Long externalEntityId;
  private String bindingStatus;
  private Long createdBy;
  private Long verifiedBy;
  private LocalDateTime verifiedAt;
  private LocalDateTime retiredAt;
  private String remark;
  private LocalDateTime createTime;
}
