package com.zhiyangyun.care.govreport.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.govreport.entity.GovChannelConfig;
import com.zhiyangyun.care.govreport.entity.GovReportSnapshot;
import com.zhiyangyun.care.govreport.entity.GovReportTask;
import com.zhiyangyun.care.govreport.model.GovReceiptCallbackRequest;
import com.zhiyangyun.care.govreport.model.GovReportBuildRequest;
import com.zhiyangyun.care.govreport.service.GovReportService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 监管/医保上报接口。 */
@RestController
@RequestMapping("/api/govreport")
public class GovReportController {

  private static final String REPORTER =
      "hasAnyRole('FINANCE_MINISTER','MEDICAL_MINISTER','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String VIEWER =
      "hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','MEDICAL_MINISTER','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String CHANNEL_ADMIN = "hasAnyRole('DIRECTOR','SYS_ADMIN','ADMIN')";

  private final GovReportService govReportService;

  public GovReportController(GovReportService govReportService) {
    this.govReportService = govReportService;
  }

  @PostMapping("/tasks/build")
  @PreAuthorize(REPORTER)
  public Result<Long> build(@Valid @RequestBody GovReportBuildRequest request) {
    return Result.ok(govReportService.buildTask(request));
  }

  @PostMapping("/tasks/{id}/send")
  @PreAuthorize(REPORTER)
  public Result<GovReportTask> send(@PathVariable("id") Long id) {
    return Result.ok(govReportService.send(id));
  }

  @GetMapping("/tasks/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<GovReportTask>> pageTasks(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String channel,
      @RequestParam(required = false) String reportType,
      @RequestParam(required = false) String status) {
    return Result.ok(govReportService.pageTasks(pageNo, pageSize, channel, reportType, status));
  }

  @GetMapping("/tasks/{id}/snapshot")
  @PreAuthorize(VIEWER)
  public Result<GovReportSnapshot> snapshot(@PathVariable("id") Long id) {
    return Result.ok(govReportService.getSnapshot(id));
  }

  @GetMapping("/channels")
  @PreAuthorize(CHANNEL_ADMIN)
  public Result<List<GovChannelConfig>> channels() {
    return Result.ok(govReportService.listChannels(AuthContext.getOrgId()));
  }

  /**
   * 渠道回执回调。生产环境须在 Spring Security 白名单放行本路径，并在服务侧校验渠道签名 {@code sign}。
   */
  @PostMapping("/receipts/callback")
  public Result<Void> callback(@Valid @RequestBody GovReceiptCallbackRequest request) {
    govReportService.receiveCallback(request);
    return Result.ok(null);
  }
}
