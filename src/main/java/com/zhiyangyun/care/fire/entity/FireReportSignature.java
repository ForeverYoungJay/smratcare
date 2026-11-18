package com.zhiyangyun.care.fire.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 维保报告签署 */
@Data
@TableName("fire_report_signature")
public class FireReportSignature {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long recordId;
  private String signType;
  private Long signerId;
  private String signerName;
  private String fileUrl;
  private String fileSha256;
  private String comment;
  private LocalDateTime signedAt;
  private LocalDateTime createTime;
  private Integer isDeleted;
}
