package com.zhiyangyun.care.digitaltwin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dt_spatial_asset")
public class DtSpatialAsset {
  @TableId(type = IdType.AUTO)
  private Long id;
  private Long orgId;
  private String assetCode;
  private String name;
  private String assetType;
  private String parentCode;
  private String modelNodeKey;
  private Long modelVersionId;
  private String positionJson;
  private String verificationStatus;
  private String sourceReference;
  private Long verifiedBy;
  private LocalDateTime verifiedAt;
  private Integer sortNo;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
