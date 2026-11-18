package com.zhiyangyun.care.common.file.service;

import com.zhiyangyun.care.common.file.model.UploadFileResponse;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
  UploadFileResponse upload(MultipartFile file, String bizType);

  /**
   * 已上传文件的 SHA-256（十六进制）。用于报告签署留证；存储方式还不支持时返回空（OSS 尚未实现，接入时覆盖此方法）。
   */
  default Optional<String> sha256(String fileUrl) {
    return Optional.empty();
  }
}
