package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.DiningRiskOverride;
import com.zhiyangyun.care.life.mapper.DiningRiskOverrideMapper;
import com.zhiyangyun.care.life.model.DiningConstants;
import com.zhiyangyun.care.life.model.DiningRiskCheckResponse;
import com.zhiyangyun.care.life.model.DiningRiskOverrideApplyRequest;
import com.zhiyangyun.care.life.model.DiningRiskOverrideReviewRequest;
import com.zhiyangyun.care.life.service.DiningRiskService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/life/dining/order/risk-override")
public class DiningRiskOverrideController {
  private final DiningRiskOverrideMapper riskOverrideMapper;
  private final DiningRiskService diningRiskService;
  private final AuditLogService auditLogService;
  private final ObjectMapper objectMapper;

  public DiningRiskOverrideController(
      DiningRiskOverrideMapper riskOverrideMapper,
      DiningRiskService diningRiskService,
      AuditLogService auditLogService,
      ObjectMapper objectMapper) {
    this.riskOverrideMapper = riskOverrideMapper;
    this.diningRiskService = diningRiskService;
    this.auditLogService = auditLogService;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/page")
  public Result<IPage<DiningRiskOverride>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(DiningRiskOverride.class)
        .eq(DiningRiskOverride::getIsDeleted, 0)
        .eq(orgId != null, DiningRiskOverride::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), DiningRiskOverride::getReviewStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(DiningRiskOverride::getElderName, keyword)
          .or().like(DiningRiskOverride::getDishNames, keyword)
          .or().like(DiningRiskOverride::getApplyReason, keyword));
    }
    wrapper.orderByDesc(DiningRiskOverride::getCreateTime);
    return Result.ok(riskOverrideMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping("/apply")
  public Result<DiningRiskOverride> apply(@Valid @RequestBody DiningRiskOverrideApplyRequest request) {
    Long orgId = AuthContext.getOrgId();
    DiningRiskCheckResponse risk = diningRiskService.check(orgId, request.getElderId(), request.getElderName(), request.getDishNames());
    if (risk.isAllowed()) {
      throw new IllegalArgumentException("未命中风险规则，无需申请放行");
    }

    DiningRiskOverride override = new DiningRiskOverride();
    override.setTenantId(orgId);
    override.setOrgId(orgId);
    override.setElderId(risk.getElderId());
    override.setElderName(risk.getElderName());
    override.setDishNames(risk.getDishNames());
    override.setRiskDetail(toJson(risk));
    override.setApplyReason(request.getApplyReason());
    override.setApplyStaffId(AuthContext.getStaffId());
    override.setReviewStatus(DiningConstants.OVERRIDE_STATUS_PENDING);
    override.setCreatedBy(AuthContext.getStaffId());
    riskOverrideMapper.insert(override);

    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "DINING_RISK_OVERRIDE", override.getId(), "提交点餐风险放行申请");
    return Result.ok(override);
  }

  @PutMapping("/{id}/review")
  public Result<DiningRiskOverride> review(@PathVariable Long id, @Valid @RequestBody DiningRiskOverrideReviewRequest request) {
    Long orgId = AuthContext.getOrgId();
    DiningRiskOverride override = riskOverrideMapper.selectOne(Wrappers.lambdaQuery(DiningRiskOverride.class)
        .eq(DiningRiskOverride::getIsDeleted, 0)
        .eq(orgId != null, DiningRiskOverride::getOrgId, orgId)
        .eq(DiningRiskOverride::getId, id)
        .last("LIMIT 1"));
    if (override == null) {
      return Result.ok(null);
    }
    if (!DiningConstants.OVERRIDE_STATUS_PENDING.equals(override.getReviewStatus())) {
      throw new IllegalArgumentException(DiningConstants.MSG_OVERRIDE_ALREADY_REVIEWED);
    }
    if (!DiningConstants.OVERRIDE_STATUS_APPROVED.equals(request.getReviewStatus())
        && !DiningConstants.OVERRIDE_STATUS_REJECTED.equals(request.getReviewStatus())) {
      throw new IllegalArgumentException(DiningConstants.MSG_INVALID_OVERRIDE_REVIEW_STATUS);
    }

    override.setReviewStatus(request.getReviewStatus());
    override.setReviewRemark(request.getReviewRemark());
    override.setReviewStaffId(AuthContext.getStaffId());
    override.setReviewedAt(LocalDateTime.now());
    if (DiningConstants.OVERRIDE_STATUS_APPROVED.equals(request.getReviewStatus())) {
      override.setEffectiveUntil(request.getEffectiveUntil() == null ? LocalDateTime.now().plusHours(2) : request.getEffectiveUntil());
    }
    riskOverrideMapper.updateById(override);

    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "REVIEW", "DINING_RISK_OVERRIDE", override.getId(), "审批点餐风险放行申请=" + request.getReviewStatus());
    return Result.ok(override);
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }
}
