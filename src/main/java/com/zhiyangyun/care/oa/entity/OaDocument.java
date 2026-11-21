package com.zhiyangyun.care.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("oa_document")
public class OaDocument {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private String name;

  private String folder;

  private Long folderId;

  private String url;

  private Long sizeBytes;

  private Long uploaderId;

  private String uploaderName;

  private LocalDateTime uploadedAt;

  private String remark;
  private Integer versionNo;
  private Long prevVersionId;
  /** 1 = 当前生效版本；新版本审核通过前为 0 */
  private Integer isCurrent;
  /** PENDING 待审核 / APPROVED 已审核 / REJECTED 已退回 */
  private String reviewStatus;
  private String reviewerName;
  private LocalDateTime reviewedAt;
  private String reviewNote;
  /** 下次复核日期 */
  private java.time.LocalDate reviewDueDate;

  private Long createdBy;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
