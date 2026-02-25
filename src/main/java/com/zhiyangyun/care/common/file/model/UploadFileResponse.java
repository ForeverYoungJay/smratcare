package com.zhiyangyun.care.common.file.model;

import lombok.Data;

@Data
public class UploadFileResponse {
  private String fileName;
  private String originalFileName;
  private String fileUrl;
  private String fileType;
  private Long fileSize;
}
