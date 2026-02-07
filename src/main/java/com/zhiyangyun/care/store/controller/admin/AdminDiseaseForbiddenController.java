package com.zhiyangyun.care.store.controller.admin;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.store.model.admin.ForbiddenTagsRequest;
import com.zhiyangyun.care.store.model.admin.ForbiddenTagsResponse;
import com.zhiyangyun.care.store.service.DiseaseRuleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/disease")
public class AdminDiseaseForbiddenController {
  private final DiseaseRuleService diseaseRuleService;

  public AdminDiseaseForbiddenController(DiseaseRuleService diseaseRuleService) {
    this.diseaseRuleService = diseaseRuleService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}/forbidden-tags")
  public Result<Void> saveForbiddenTags(
      @PathVariable Long id,
      @Valid @RequestBody ForbiddenTagsRequest request) {
    Long orgId = AuthContext.getOrgId();
    diseaseRuleService.saveForbiddenTags(orgId, id, request.getTagIds());
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}/forbidden-tags")
  public Result<ForbiddenTagsResponse> getForbiddenTags(
      @PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(diseaseRuleService.getForbiddenTags(orgId, id));
  }
}
