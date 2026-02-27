package com.zhiyangyun.care.crm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.crm.model.MarketingPlanRequest;
import com.zhiyangyun.care.crm.model.MarketingPlanResponse;
import com.zhiyangyun.care.crm.service.MarketingPlanService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marketing/plans")
public class MarketingPlanController {
  private final MarketingPlanService marketingPlanService;

  public MarketingPlanController(MarketingPlanService marketingPlanService) {
    this.marketingPlanService = marketingPlanService;
  }

  @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
  @GetMapping("/page")
  public Result<IPage<MarketingPlanResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String moduleType,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(marketingPlanService.page(orgId, pageNo, pageSize, moduleType, status, keyword));
  }

  @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
  @GetMapping
  public Result<List<MarketingPlanResponse>> list(@RequestParam(required = false) String moduleType) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(marketingPlanService.listByModule(orgId, moduleType));
  }

  @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
  @PostMapping
  public Result<MarketingPlanResponse> create(@Valid @RequestBody MarketingPlanRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    return Result.ok(marketingPlanService.create(orgId, staffId, request));
  }

  @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
  @PutMapping("/{id}")
  public Result<MarketingPlanResponse> update(@PathVariable Long id, @Valid @RequestBody MarketingPlanRequest request) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(marketingPlanService.update(orgId, id, request));
  }

  @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
  @DeleteMapping("/{id}")
  public Result<Void> remove(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    marketingPlanService.remove(orgId, id);
    return Result.ok(null);
  }
}
