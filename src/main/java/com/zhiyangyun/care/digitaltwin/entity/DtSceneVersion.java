package com.zhiyangyun.care.digitaltwin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dt_scene_version")
public class DtSceneVersion {
  @TableId(type = IdType.AUTO)
  private Long id;
  private Long orgId;
  private String version;
  private String modelUrl;
  private String checksum;
  private String sourceType;
  private String verificationStatus;
  private String status;
  private String coordinateNote;
  private LocalDateTime publishedAt;
  private Long replacedBy;
  private LocalDateTime createTime;
}
