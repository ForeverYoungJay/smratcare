package com.zhiyangyun.care.elder.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.ElderFamily;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.model.FamilyBindRequest;
import com.zhiyangyun.care.elder.model.FamilyElderItem;
import com.zhiyangyun.care.elder.model.FamilyRegisterRequest;
import com.zhiyangyun.care.elder.mapper.ElderFamilyMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.service.FamilyService;
import com.zhiyangyun.care.family.config.FamilyPortalProperties;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/family")
public class FamilyController {
  private static final Logger log = LoggerFactory.getLogger(FamilyController.class);
  private final FamilyService familyService;
  private final ElderFamilyMapper elderFamilyMapper;
  private final ElderMapper elderMapper;
  private final FamilyPortalProperties familyPortalProperties;

  public FamilyController(FamilyService familyService,
      ElderFamilyMapper elderFamilyMapper,
      ElderMapper elderMapper,
      FamilyPortalProperties familyPortalProperties) {
    this.familyService = familyService;
    this.elderFamilyMapper = elderFamilyMapper;
    this.elderMapper = elderMapper;
    this.familyPortalProperties = familyPortalProperties;
  }

  @PostMapping("/register")
  public Result<FamilyUser> register(@Valid @RequestBody FamilyRegisterRequest request) {
    return Result.ok(familyService.register(request));
  }

  /**
   * @deprecated 请迁移至 /api/family/bindings（Family 聚合接口）。该接口将逐步下线。
   */
  @Deprecated(forRemoval = true, since = "2026-03")
  @PostMapping("/bindElder")
  public Result<ElderFamily> bindElder(@Valid @RequestBody FamilyBindRequest request, HttpServletResponse response) {
    ensureLegacyApiEnabled();
    markDeprecated(response, "/api/family/bindings");
    log.warn("Deprecated family API called: /api/family/bindElder, familyUserId={}, orgId={}",
        AuthContext.getStaffId(), AuthContext.getOrgId());
    request.setOrgId(AuthContext.getOrgId());
    request.setFamilyUserId(AuthContext.getStaffId());
    return Result.ok(familyService.bindElder(request));
  }

  /**
   * @deprecated 请迁移至 /api/family/bindings（Family 聚合接口）。该接口将逐步下线。
   */
  @Deprecated(forRemoval = true, since = "2026-03")
  @GetMapping("/my-elders")
  public Result<List<FamilyElderItem>> myElders(HttpServletResponse response) {
    ensureLegacyApiEnabled();
    markDeprecated(response, "/api/family/bindings");
    log.warn("Deprecated family API called: /api/family/my-elders, familyUserId={}, orgId={}",
        AuthContext.getStaffId(), AuthContext.getOrgId());
    Long orgId = AuthContext.getOrgId();
    Long familyUserId = AuthContext.getStaffId();
    List<ElderFamily> relations = elderFamilyMapper.selectList(
        Wrappers.lambdaQuery(ElderFamily.class)
            .eq(ElderFamily::getOrgId, orgId)
            .eq(ElderFamily::getFamilyUserId, familyUserId)
            .eq(ElderFamily::getIsDeleted, 0));
    List<FamilyElderItem> items = new ArrayList<>();
    for (ElderFamily relation : relations) {
      ElderProfile elder = elderMapper.selectById(relation.getElderId());
      FamilyElderItem item = new FamilyElderItem();
      item.setElderId(relation.getElderId());
      item.setElderName(elder == null ? null : elder.getFullName());
      item.setRelation(relation.getRelation());
      item.setIsPrimary(relation.getIsPrimary());
      items.add(item);
    }
    return Result.ok(items);
  }

  private void ensureLegacyApiEnabled() {
    if (familyPortalProperties.isLegacyApiEnabled() && !isLegacySunsetReached()) {
      return;
    }
    throw new ResponseStatusException(HttpStatus.GONE, "该接口已下线，请改用 Family 聚合接口");
  }

  private void markDeprecated(HttpServletResponse response, String replacementPath) {
    if (response == null) {
      return;
    }
    response.setHeader("Deprecation", "true");
    response.setHeader("Sunset", familyPortalProperties.getLegacyApiSunsetDate());
    response.setHeader("Link", "<" + replacementPath + ">; rel=\"successor-version\"");
    response.setHeader("X-Deprecated-Reason", "Use family aggregation APIs");
  }

  private boolean isLegacySunsetReached() {
    String configured = familyPortalProperties.getLegacyApiSunsetDate();
    if (configured == null || configured.isBlank()) {
      return false;
    }
    try {
      return !LocalDate.now().isBefore(LocalDate.parse(configured.trim()));
    } catch (DateTimeParseException ex) {
      log.warn("Invalid app.family.legacy-api-sunset-date config: {}", configured);
      return false;
    }
  }
}
