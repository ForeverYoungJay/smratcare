package com.zhiyangyun.care.hr.model;

import lombok.Data;

@Data
public class HrProfileDocumentRequest {
  private String name;
  private String folder;
  private String url;
  private Long sizeBytes;
  private String uploaderName;
  private String uploadedAt;
  private String remark;
}
