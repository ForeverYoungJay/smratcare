package com.zhiyangyun.care.elder.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.store.model.admin.ElderDiseaseRequest;
import com.zhiyangyun.care.store.service.DiseaseRuleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/elder")
public class ElderDiseaseController {
  private final DiseaseRuleService diseaseRuleService;

  public ElderDiseaseController(DiseaseRuleService diseaseRuleService) {
    this.diseaseRuleService = diseaseRuleService;
  }

  @PutMapping("/{elderId}/diseases")
  public Result<Void> updateDiseases(
      @PathVariable Long elderId,
      @Valid @RequestBody ElderDiseaseRequest request) {
    Long orgId = AuthContext.getOrgId();
    diseaseRuleService.saveElderDiseases(orgId, elderId, request.getDiseaseIds());
    return Result.ok(null);
  }

  @GetMapping("/{elderId}/diseases")
  public Result<List<Long>> listDiseases(
      @PathVariable Long elderId) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(diseaseRuleService.getElderDiseaseIds(orgId, elderId));
  }
}
