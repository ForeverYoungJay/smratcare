package com.zhiyangyun.care.pharmacy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.pharmacy.entity.DrugBatch;
import com.zhiyangyun.care.pharmacy.entity.DrugCatalog;
import com.zhiyangyun.care.pharmacy.entity.DrugDispenseRecord;
import com.zhiyangyun.care.pharmacy.model.DrugDispenseRequest;
import com.zhiyangyun.care.pharmacy.model.DrugInboundRequest;
import com.zhiyangyun.care.pharmacy.service.PharmacyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 药事管理接口。 */
@RestController
@RequestMapping("/api/pharmacy")
public class PharmacyController {

  private static final String OPERATOR =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_EMPLOYEE','NURSING_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String VIEWER =
      "hasAnyRole('MEDICAL_EMPLOYEE','MEDICAL_MINISTER','NURSING_EMPLOYEE','NURSING_MINISTER','LOGISTICS_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN','STAFF')";

  private final PharmacyService pharmacyService;

  public PharmacyController(PharmacyService pharmacyService) {
    this.pharmacyService = pharmacyService;
  }

  @GetMapping("/drugs/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<DrugCatalog>> drugs(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) Integer status) {
    return Result.ok(pharmacyService.pageDrugs(pageNo, pageSize, keyword, category, status));
  }

  @GetMapping("/batches/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<DrugBatch>> batches(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) Long drugId,
      @RequestParam(required = false) String status) {
    return Result.ok(pharmacyService.pageBatches(pageNo, pageSize, drugId, status));
  }

  @PostMapping("/inbound")
  @PreAuthorize(OPERATOR)
  public Result<Long> inbound(@Valid @RequestBody DrugInboundRequest request) {
    return Result.ok(pharmacyService.inbound(request));
  }

  @PostMapping("/dispense")
  @PreAuthorize(OPERATOR)
  public Result<List<DrugDispenseRecord>> dispense(@Valid @RequestBody DrugDispenseRequest request) {
    return Result.ok(pharmacyService.dispense(request));
  }

  @GetMapping("/dispense-records/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<DrugDispenseRecord>> dispenseRecords(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) Long drugId,
      @RequestParam(required = false) Long elderId) {
    return Result.ok(pharmacyService.pageDispenseRecords(pageNo, pageSize, drugId, elderId));
  }
}
