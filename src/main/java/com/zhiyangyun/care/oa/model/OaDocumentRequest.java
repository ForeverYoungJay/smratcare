package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OaDocumentRequest {
  @NotBlank
  private String name;

  private String folder;

  private String url;

  private Long sizeBytes;

  private Long uploaderId;

  private String uploaderName;

  private LocalDateTime uploadedAt;

  private String remark;
}
