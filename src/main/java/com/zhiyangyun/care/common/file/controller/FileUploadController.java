package com.zhiyangyun.care.common.file.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.common.file.model.UploadFileResponse;
import com.zhiyangyun.care.common.file.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {
  private final FileStorageService fileStorageService;

  public FileUploadController(FileStorageService fileStorageService) {
    this.fileStorageService = fileStorageService;
  }

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Result<UploadFileResponse> upload(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "bizType", required = false) String bizType) {
    return Result.ok(fileStorageService.upload(file, bizType));
  }
}
