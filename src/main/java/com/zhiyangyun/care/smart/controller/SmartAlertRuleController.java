package com.zhiyangyun.care.smart.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.smart.entity.SmartAlertRule;
import com.zhiyangyun.care.smart.mapper.SmartAlertRuleMapper;
import com.zhiyangyun.care.smart.model.SmartAlertRuleRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.beans.BeanUtils;
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

/** 智慧安全场景规则配置。 */
@RestController
@RequestMapping("/api/smart/rules")
public class SmartAlertRuleController {

  private static final String VIEWER =
      "hasAnyRole('NURSING_EMPLOYEE','NURSING_MINISTER','MEDICAL_MINISTER','LOGISTICS_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN','STAFF')";
  private static final String MANAGER =
      "hasAnyRole('NURSING_MINISTER','MEDICAL_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";

  private final SmartAlertRuleMapper ruleMapper;

  public SmartAlertRuleController(SmartAlertRuleMapper ruleMapper) {
    this.ruleMapper = ruleMapper;
  }

  @GetMapping("/page")
  @PreAuthorize(VIEWER)
  public Result<IPage<SmartAlertRule>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String eventType,
      @RequestParam(required = false) Integer enabled) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(SmartAlertRule.class)
        .eq(SmartAlertRule::getIsDeleted, 0)
        .and(w -> w.isNull(SmartAlertRule::getOrgId)
            .or(orgId != null, q -> q.eq(SmartAlertRule::getOrgId, orgId)))
        .eq(eventType != null && !eventType.isBlank(), SmartAlertRule::getEventType, eventType)
        .eq(enabled != null, SmartAlertRule::getEnabled, enabled)
        .orderByAsc(SmartAlertRule::getPriority)
        .orderByDesc(SmartAlertRule::getId);
    return Result.ok(ruleMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  @PreAuthorize(MANAGER)
  public Result<SmartAlertRule> create(@Valid @RequestBody SmartAlertRuleRequest request) {
    SmartAlertRule rule = new SmartAlertRule();
    BeanUtils.copyProperties(request, rule);
    rule.setOrgId(AuthContext.getOrgId());
    rule.setCreatedBy(AuthContext.getStaffId());
    if (rule.getEnabled() == null) {
      rule.setEnabled(1);
    }
    ruleMapper.insert(rule);
    return Result.ok(rule);
  }

  @PutMapping("/{id}")
  @PreAuthorize(MANAGER)
  public Result<SmartAlertRule> update(@PathVariable("id") Long id,
      @Valid @RequestBody SmartAlertRuleRequest request) {
    SmartAlertRule rule = ruleMapper.selectById(id);
    if (rule == null || Integer.valueOf(1).equals(rule.getIsDeleted())) {
      return Result.error(404, "规则不存在");
    }
    Long orgId = AuthContext.getOrgId();
    if (rule.getOrgId() == null) {
      return Result.error(403, "内置全局规则不可编辑，请复制为机构规则后修改");
    }
    if (orgId != null && !orgId.equals(rule.getOrgId())) {
      return Result.error(403, "无权编辑其他机构的规则");
    }
    BeanUtils.copyProperties(request, rule);
    rule.setId(id);
    rule.setUpdateTime(LocalDateTime.now());
    ruleMapper.updateById(rule);
    return Result.ok(rule);
  }

  @PutMapping("/{id}/enabled")
  @PreAuthorize(MANAGER)
  public Result<Void> setEnabled(@PathVariable("id") Long id, @RequestParam boolean enabled) {
    SmartAlertRule rule = ruleMapper.selectById(id);
    if (rule == null || Integer.valueOf(1).equals(rule.getIsDeleted())) {
      return Result.error(404, "规则不存在");
    }
    rule.setEnabled(enabled ? 1 : 0);
    rule.setUpdateTime(LocalDateTime.now());
    ruleMapper.updateById(rule);
    return Result.ok(null);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize(MANAGER)
  public Result<Void> delete(@PathVariable("id") Long id) {
    SmartAlertRule rule = ruleMapper.selectById(id);
    if (rule == null) {
      return Result.error(404, "规则不存在");
    }
    if (rule.getOrgId() == null) {
      return Result.error(403, "内置全局规则不可删除");
    }
    rule.setIsDeleted(1);
    ruleMapper.updateById(rule);
    return Result.ok(null);
  }
}
