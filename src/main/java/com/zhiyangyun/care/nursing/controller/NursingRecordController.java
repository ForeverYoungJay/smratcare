package com.zhiyangyun.care.nursing.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.nursing.model.NursingRecordItem;
import com.zhiyangyun.care.nursing.service.NursingRecordService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nursing/records")
public class NursingRecordController {
  private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final NursingRecordService nursingRecordService;

  public NursingRecordController(NursingRecordService nursingRecordService) {
    this.nursingRecordService = nursingRecordService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/page")
  public Result<IPage<NursingRecordItem>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String timeFrom,
      @RequestParam(required = false) String timeTo,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) String keyword) {
    return Result.ok(nursingRecordService.page(
        AuthContext.getOrgId(), pageNo, pageSize, parseDateTime(timeFrom), parseDateTime(timeTo), elderId, staffId, keyword));
  }

  private LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    if (value.contains("T")) {
      return LocalDateTime.parse(value);
    }
    return LocalDateTime.parse(value, DATETIME_FORMATTER);
  }
}
