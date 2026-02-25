package com.zhiyangyun.care.oa.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("oa_knowledge")
public class OaKnowledge {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private String title;

  private String category;

  private String tags;

  private String content;

  private Long authorId;

  private String authorName;

  private String status;

  private LocalDateTime publishedAt;

  private String remark;

  private Long createdBy;

  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;

  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
