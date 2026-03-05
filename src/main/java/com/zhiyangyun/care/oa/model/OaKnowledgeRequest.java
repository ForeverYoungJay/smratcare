package com.zhiyangyun.care.oa.model;

import lombok.Data;

@Data
public class OaKnowledgeRequest {
  private String title;

  private String category;

  private String tags;

  private String content;

  private String attachmentName;

  private String attachmentUrl;

  private String attachmentType;

  private Long attachmentSize;

  private Long authorId;

  private String authorName;

  private String status;

  private java.time.LocalDateTime expiredAt;

  private String remark;
}
