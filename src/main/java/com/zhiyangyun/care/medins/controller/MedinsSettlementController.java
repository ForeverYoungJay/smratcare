package com.zhiyangyun.care.medins.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.medins.entity.MedinsSettlementSheet;
import com.zhiyangyun.care.medins.entity.MedinsUploadTask;
import com.zhiyangyun.care.medins.model.MedinsReceiptCallbackRequest;
import com.zhiyangyun.care.medins.service.MedinsSettlementService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 医保结算清单与上报任务接口。 */
@RestController
@RequestMapping("/api/medins")
public class MedinsSettlementController {

  private static final String UPLOADER =
      "hasAnyRole('FINANCE_MINISTER','MEDICAL_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String VIEWER =
      "hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','MEDICAL_MINISTER','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";

  private final MedinsSettlementService settlementService;

  public MedinsSettlementController(MedinsSettlementService settlementService) {
    this.settlementService = settlementService;
  }

  /** 从长护险结算单生成医保结算清单：ltciSettlementId 与 elderId+month 二选一。 */
  @PostMapping("/sheets/generate")
  @PreAuthorize(UPLOADER)
  public Result<MedinsSettlementSheet> generate(
      @RequestParam(required = false) Long ltciSettlementId,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String month) {
    return Result.ok(settlementService.generateSheet(ltciSettlementId, elderId, month));
  }

  @GetMapping("/sheets/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<MedinsSettlementSheet>> pageSheets(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String month,
      @RequestParam(required = false) String status) {
    return Result.ok(settlementService.pageSheets(pageNo, pageSize, elderId, month, status));
  }

  @GetMapping("/sheets/{id}")
  @PreAuthorize(VIEWER)
  public Result<MedinsSettlementSheet> getSheet(@PathVariable("id") Long id) {
    return Result.ok(settlementService.getSheet(id));
  }

  @PostMapping("/sheets/{id}/upload")
  @PreAuthorize(UPLOADER)
  public Result<MedinsUploadTask> upload(
      @PathVariable("id") Long id,
      @RequestParam(required = false) String channel) {
    return Result.ok(settlementService.uploadSheet(id, channel));
  }

  @PostMapping("/sheets/{id}/reconcile")
  @PreAuthorize(UPLOADER)
  public Result<Void> reconcile(@PathVariable("id") Long id) {
    settlementService.reconcileSheet(id);
    return Result.ok(null);
  }

  @GetMapping("/tasks/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<MedinsUploadTask>> pageTasks(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long sheetId,
      @RequestParam(required = false) String status) {
    return Result.ok(settlementService.pageTasks(pageNo, pageSize, sheetId, status));
  }

  @PostMapping("/tasks/{id}/retry")
  @PreAuthorize(UPLOADER)
  public Result<MedinsUploadTask> retry(@PathVariable("id") Long id) {
    return Result.ok(settlementService.retryTask(id));
  }

  @PostMapping("/tasks/{id}/query-receipt")
  @PreAuthorize(UPLOADER)
  public Result<MedinsUploadTask> queryReceipt(@PathVariable("id") Long id) {
    return Result.ok(settlementService.queryReceipt(id));
  }

  /**
   * 医保渠道回执回调。生产环境须在 Spring Security 白名单放行本路径，
   * 并在服务侧校验渠道签名 {@code sign}。
   */
  @PostMapping("/receipts/callback")
  public Result<Void> callback(@Valid @RequestBody MedinsReceiptCallbackRequest request) {
    settlementService.receiveCallback(request);
    return Result.ok(null);
  }
}
