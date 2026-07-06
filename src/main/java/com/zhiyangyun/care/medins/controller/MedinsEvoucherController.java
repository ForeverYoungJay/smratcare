package com.zhiyangyun.care.medins.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.medins.entity.MedinsEvoucher;
import com.zhiyangyun.care.medins.model.MedinsEvoucherBindRequest;
import com.zhiyangyun.care.medins.service.MedinsEvoucherService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 长者医保电子凭证登记与校验接口。 */
@RestController
@RequestMapping("/api/medins")
public class MedinsEvoucherController {

  private static final String MANAGER =
      "hasAnyRole('MEDICAL_MINISTER','NURSING_MINISTER','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String VIEWER =
      "hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','MEDICAL_MINISTER','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";

  private final MedinsEvoucherService evoucherService;

  public MedinsEvoucherController(MedinsEvoucherService evoucherService) {
    this.evoucherService = evoucherService;
  }

  @PostMapping("/evouchers")
  @PreAuthorize(MANAGER)
  public Result<Long> bind(@Valid @RequestBody MedinsEvoucherBindRequest request) {
    return Result.ok(evoucherService.bind(request));
  }

  @GetMapping("/evouchers/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<MedinsEvoucher>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String bindStatus,
      @RequestParam(required = false) String verifyStatus) {
    return Result.ok(evoucherService.page(pageNo, pageSize, elderId, bindStatus, verifyStatus));
  }

  @PostMapping("/evouchers/{id}/verify")
  @PreAuthorize(MANAGER)
  public Result<MedinsEvoucher> verify(@PathVariable("id") Long id) {
    return Result.ok(evoucherService.verify(id));
  }

  @PostMapping("/evouchers/{id}/unbind")
  @PreAuthorize(MANAGER)
  public Result<Void> unbind(@PathVariable("id") Long id) {
    evoucherService.unbind(id);
    return Result.ok(null);
  }
}
