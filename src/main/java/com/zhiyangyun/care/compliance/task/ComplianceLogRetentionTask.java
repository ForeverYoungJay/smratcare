package com.zhiyangyun.care.compliance.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.audit.entity.AuditLog;
import com.zhiyangyun.care.audit.mapper.AuditLogMapper;
import com.zhiyangyun.care.compliance.entity.ComplianceSecurityPolicy;
import com.zhiyangyun.care.compliance.mapper.AuthLoginLogMapper;
import com.zhiyangyun.care.compliance.mapper.ComplianceSecurityPolicyMapper;
import com.zhiyangyun.care.compliance.mapper.ExportLogMapper;
import com.zhiyangyun.care.compliance.mapper.SensitiveAccessLogMapper;
import com.zhiyangyun.care.compliance.service.ComplianceSecurityPolicyService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 日志留存与清理任务：按机构安全策略（compliance_security_policy.log_retention_days，默认 400 天）
 * 物理清理审计日志 / 敏感访问日志 / 登录日志 / 导出留痕。
 *
 * <p>硬编码下限 {@link ComplianceSecurityPolicyService#MIN_LOG_RETENTION_DAYS}（180 天）：
 * 等保 2.0 要求日志留存不少于 6 个月，任何配置（含误配）都不会删除 180 天内的日志。
 */
@Component
public class ComplianceLogRetentionTask {
  private static final Logger log = LoggerFactory.getLogger(ComplianceLogRetentionTask.class);

  private final ComplianceSecurityPolicyMapper policyMapper;
  private final AuditLogMapper auditLogMapper;
  private final SensitiveAccessLogMapper sensitiveAccessLogMapper;
  private final AuthLoginLogMapper authLoginLogMapper;
  private final ExportLogMapper exportLogMapper;

  /** 未单独配置策略的机构使用的默认留存天数。 */
  @Value("${app.compliance.log-retention.default-days:400}")
  private int defaultRetentionDays;

  @Value("${app.compliance.log-retention.enabled:true}")
  private boolean enabled;

  public ComplianceLogRetentionTask(ComplianceSecurityPolicyMapper policyMapper,
      AuditLogMapper auditLogMapper,
      SensitiveAccessLogMapper sensitiveAccessLogMapper,
      AuthLoginLogMapper authLoginLogMapper,
      ExportLogMapper exportLogMapper) {
    this.policyMapper = policyMapper;
    this.auditLogMapper = auditLogMapper;
    this.sensitiveAccessLogMapper = sensitiveAccessLogMapper;
    this.authLoginLogMapper = authLoginLogMapper;
    this.exportLogMapper = exportLogMapper;
  }

  /** 每日 02:40 执行清理。 */
  @Scheduled(cron = "${app.compliance.log-retention.cron:0 40 2 * * ?}")
  public void cleanup() {
    if (!enabled) {
      return;
    }
    try {
      LocalDateTime now = LocalDateTime.now();
      Map<Long, Integer> retentionByOrg = loadPolicyRetention();
      List<Long> policyOrgIds = new ArrayList<>(retentionByOrg.keySet());
      int deleted = 0;

      // 1. 已配置策略的机构：按各自留存天数清理（不低于硬编码下限）
      for (Map.Entry<Long, Integer> entry : retentionByOrg.entrySet()) {
        LocalDateTime cutoff = now.minusDays(effectiveDays(entry.getValue()));
        deleted += deleteForOrg(entry.getKey(), cutoff);
      }

      // 2. 其余机构（含 org_id 为空的记录）：按默认留存天数清理
      LocalDateTime defaultCutoff = now.minusDays(effectiveDays(defaultRetentionDays));
      deleted += sensitiveAccessLogMapper.deleteBeforeExcludingOrgs(defaultCutoff, policyOrgIds);
      deleted += authLoginLogMapper.deleteBeforeExcludingOrgs(defaultCutoff, policyOrgIds);
      deleted += exportLogMapper.deleteBeforeExcludingOrgs(defaultCutoff, policyOrgIds);
      deleted += deleteAuditLogExcludingOrgs(defaultCutoff, policyOrgIds);

      log.info("[Compliance] 日志留存清理完成，共删除 {} 条（默认留存 {} 天，下限 {} 天）",
          deleted, effectiveDays(defaultRetentionDays), ComplianceSecurityPolicyService.MIN_LOG_RETENTION_DAYS);
    } catch (Exception ex) {
      log.error("[Compliance] 日志留存清理失败", ex);
    }
  }

  private Map<Long, Integer> loadPolicyRetention() {
    Map<Long, Integer> result = new LinkedHashMap<>();
    List<ComplianceSecurityPolicy> policies = policyMapper.selectList(
        Wrappers.lambdaQuery(ComplianceSecurityPolicy.class)
            .eq(ComplianceSecurityPolicy::getIsDeleted, 0));
    for (ComplianceSecurityPolicy policy : policies) {
      if (policy == null || policy.getOrgId() == null) {
        continue;
      }
      int days = policy.getLogRetentionDays() == null ? defaultRetentionDays : policy.getLogRetentionDays();
      result.put(policy.getOrgId(), days);
    }
    return result;
  }

  private int deleteForOrg(Long orgId, LocalDateTime cutoff) {
    int deleted = 0;
    deleted += sensitiveAccessLogMapper.deleteByOrgBefore(orgId, cutoff);
    deleted += authLoginLogMapper.deleteByOrgBefore(orgId, cutoff);
    deleted += exportLogMapper.deleteByOrgBefore(orgId, cutoff);
    // audit_log 实体无逻辑删除字段，MyBatis-Plus delete 即物理删除
    deleted += auditLogMapper.delete(Wrappers.lambdaQuery(AuditLog.class)
        .eq(AuditLog::getOrgId, orgId)
        .lt(AuditLog::getCreateTime, cutoff));
    return deleted;
  }

  private int deleteAuditLogExcludingOrgs(LocalDateTime cutoff, List<Long> excludeOrgIds) {
    var wrapper = Wrappers.lambdaQuery(AuditLog.class)
        .lt(AuditLog::getCreateTime, cutoff);
    if (excludeOrgIds != null && !excludeOrgIds.isEmpty()) {
      wrapper.and(w -> w.isNull(AuditLog::getOrgId).or().notIn(AuditLog::getOrgId, excludeOrgIds));
    }
    return auditLogMapper.delete(wrapper);
  }

  /** 应用等保 2.0 硬编码下限：任何配置不得低于 180 天。 */
  private static int effectiveDays(int configuredDays) {
    return Math.max(configuredDays, ComplianceSecurityPolicyService.MIN_LOG_RETENTION_DAYS);
  }
}
