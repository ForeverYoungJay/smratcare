package com.zhiyangyun.care.common.file.service;

import com.zhiyangyun.care.common.file.model.UploadFileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
  UploadFileResponse upload(MultipartFile file, String bizType);
}
