package com.zhiyangyun.care.store.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.store.model.MaterialCenterOverviewResponse;
import com.zhiyangyun.care.store.service.MaterialCenterOverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/material-center")
public class MaterialCenterController {
  private final MaterialCenterOverviewService materialCenterOverviewService;

  public MaterialCenterController(MaterialCenterOverviewService materialCenterOverviewService) {
    this.materialCenterOverviewService = materialCenterOverviewService;
  }

  @GetMapping("/overview")
  public Result<MaterialCenterOverviewResponse> overview(
      @RequestParam(defaultValue = "30") int expiryDays) {
    return Result.ok(materialCenterOverviewService.overview(AuthContext.getOrgId(), expiryDays));
  }
}
