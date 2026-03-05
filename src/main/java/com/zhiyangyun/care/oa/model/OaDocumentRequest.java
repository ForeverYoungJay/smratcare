package com.zhiyangyun.care.oa.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OaDocumentRequest {
  private String name;

  private String folder;

  private Long folderId;

  private String url;

  private Long sizeBytes;

  private Long uploaderId;

  private String uploaderName;

  private LocalDateTime uploadedAt;

  private String remark;
}
