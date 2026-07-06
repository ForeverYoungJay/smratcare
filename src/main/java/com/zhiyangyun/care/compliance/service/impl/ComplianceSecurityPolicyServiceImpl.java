package com.zhiyangyun.care.compliance.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.compliance.entity.ComplianceSecurityPolicy;
import com.zhiyangyun.care.compliance.mapper.ComplianceSecurityPolicyMapper;
import com.zhiyangyun.care.compliance.model.SecurityPolicyUpdateRequest;
import com.zhiyangyun.care.compliance.model.SecurityPolicyView;
import com.zhiyangyun.care.compliance.service.ComplianceSecurityPolicyService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ComplianceSecurityPolicyServiceImpl implements ComplianceSecurityPolicyService {
  private static final Logger log = LoggerFactory.getLogger(ComplianceSecurityPolicyServiceImpl.class);

  private static final String DEFAULT_EXEMPT_ROLES = "SYS_ADMIN,ADMIN,DIRECTOR";
  private static final int DEFAULT_LOCK_COUNT = 5;
  private static final int DEFAULT_LOCK_MINUTES = 30;
  private static final int DEFAULT_RETENTION_DAYS = 400;
  private static final long CACHE_TTL_MILLIS = 60_000L;

  private final ComplianceSecurityPolicyMapper mapper;
  private final Map<Long, CachedPolicy> cache = new ConcurrentHashMap<>();

  public ComplianceSecurityPolicyServiceImpl(ComplianceSecurityPolicyMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public SecurityPolicyView getEffectivePolicy(Long orgId) {
    if (orgId == null) {
      return defaults(null);
    }
    CachedPolicy cached = cache.get(orgId);
    long now = System.currentTimeMillis();
    if (cached != null && cached.expireAt() > now) {
      return cached.view();
    }
    SecurityPolicyView view;
    try {
      ComplianceSecurityPolicy entity = selectByOrg(orgId);
      view = entity == null ? defaults(orgId) : toView(entity);
    } catch (Exception ex) {
      // 策略读取失败不得阻断登录/序列化等主流程，降级为默认策略
      log.warn("[Compliance] 安全策略读取失败，使用默认策略 orgId={}: {}", orgId, ex.getMessage());
      view = defaults(orgId);
    }
    cache.put(orgId, new CachedPolicy(view, now + CACHE_TTL_MILLIS));
    return view;
  }

  @Override
  public SecurityPolicyView update(Long orgId, SecurityPolicyUpdateRequest request) {
    if (orgId == null) {
      throw new IllegalArgumentException("机构ID不能为空");
    }
    if (request == null) {
      throw new IllegalArgumentException("策略内容不能为空");
    }
    int retentionDays = request.getLogRetentionDays() == null ? DEFAULT_RETENTION_DAYS : request.getLogRetentionDays();
    if (retentionDays < MIN_LOG_RETENTION_DAYS) {
      throw new IllegalArgumentException("日志留存天数不得低于 " + MIN_LOG_RETENTION_DAYS + " 天（等保2.0要求日志留存不少于6个月）");
    }
    int passwordMaxDays = normalizeNonNegative(request.getPasswordMaxDays(), 0);
    int lockCount = normalizeNonNegative(request.getLoginFailLockCount(), DEFAULT_LOCK_COUNT);
    int lockMinutes = request.getLoginFailLockMinutes() == null || request.getLoginFailLockMinutes() <= 0
        ? DEFAULT_LOCK_MINUTES : request.getLoginFailLockMinutes();
    int sessionTimeout = normalizeNonNegative(request.getSessionTimeoutMinutes(), 0);

    ComplianceSecurityPolicy entity = selectByOrg(orgId);
    boolean create = entity == null;
    if (create) {
      entity = new ComplianceSecurityPolicy();
      entity.setTenantId(orgId);
      entity.setOrgId(orgId);
      entity.setIsDeleted(0);
    }
    entity.setTwoFactorEnabled(Boolean.TRUE.equals(request.getTwoFactorEnabled()) ? 1 : 0);
    entity.setTwoFactorRoles(joinRoles(request.getTwoFactorRoles(), null));
    entity.setPasswordMaxDays(passwordMaxDays);
    entity.setLoginFailLockCount(lockCount);
    entity.setLoginFailLockMinutes(lockMinutes);
    entity.setSessionTimeoutMinutes(sessionTimeout);
    entity.setMaskingEnabled(Boolean.TRUE.equals(request.getMaskingEnabled()) ? 1 : 0);
    entity.setMaskingExemptRoles(joinRoles(request.getMaskingExemptRoles(), DEFAULT_EXEMPT_ROLES));
    entity.setLogRetentionDays(retentionDays);
    if (create) {
      mapper.insert(entity);
    } else {
      mapper.updateById(entity);
    }
    cache.remove(orgId);
    return toView(entity);
  }

  private ComplianceSecurityPolicy selectByOrg(Long orgId) {
    return mapper.selectOne(Wrappers.lambdaQuery(ComplianceSecurityPolicy.class)
        .eq(ComplianceSecurityPolicy::getOrgId, orgId)
        .eq(ComplianceSecurityPolicy::getIsDeleted, 0)
        .last("LIMIT 1"));
  }

  private SecurityPolicyView defaults(Long orgId) {
    SecurityPolicyView view = new SecurityPolicyView();
    view.setOrgId(orgId);
    view.setTwoFactorEnabled(false);
    view.setTwoFactorRoles(List.of());
    view.setPasswordMaxDays(0);
    view.setLoginFailLockCount(DEFAULT_LOCK_COUNT);
    view.setLoginFailLockMinutes(DEFAULT_LOCK_MINUTES);
    view.setSessionTimeoutMinutes(0);
    view.setMaskingEnabled(false);
    view.setMaskingExemptRoles(splitRoles(DEFAULT_EXEMPT_ROLES));
    view.setLogRetentionDays(DEFAULT_RETENTION_DAYS);
    return view;
  }

  private SecurityPolicyView toView(ComplianceSecurityPolicy entity) {
    SecurityPolicyView view = new SecurityPolicyView();
    view.setOrgId(entity.getOrgId());
    view.setTwoFactorEnabled(Integer.valueOf(1).equals(entity.getTwoFactorEnabled()));
    view.setTwoFactorRoles(splitRoles(entity.getTwoFactorRoles()));
    view.setPasswordMaxDays(entity.getPasswordMaxDays() == null ? 0 : entity.getPasswordMaxDays());
    view.setLoginFailLockCount(entity.getLoginFailLockCount() == null ? DEFAULT_LOCK_COUNT : entity.getLoginFailLockCount());
    view.setLoginFailLockMinutes(entity.getLoginFailLockMinutes() == null || entity.getLoginFailLockMinutes() <= 0
        ? DEFAULT_LOCK_MINUTES : entity.getLoginFailLockMinutes());
    view.setSessionTimeoutMinutes(entity.getSessionTimeoutMinutes() == null ? 0 : entity.getSessionTimeoutMinutes());
    view.setMaskingEnabled(Integer.valueOf(1).equals(entity.getMaskingEnabled()));
    List<String> exemptRoles = splitRoles(entity.getMaskingExemptRoles());
    view.setMaskingExemptRoles(exemptRoles.isEmpty() ? splitRoles(DEFAULT_EXEMPT_ROLES) : exemptRoles);
    int retention = entity.getLogRetentionDays() == null ? DEFAULT_RETENTION_DAYS : entity.getLogRetentionDays();
    view.setLogRetentionDays(Math.max(retention, MIN_LOG_RETENTION_DAYS));
    return view;
  }

  private static int normalizeNonNegative(Integer value, int fallback) {
    if (value == null || value < 0) {
      return fallback;
    }
    return value;
  }

  private static List<String> splitRoles(String roles) {
    if (roles == null || roles.isBlank()) {
      return List.of();
    }
    return Arrays.stream(roles.split(","))
        .map(String::trim)
        .filter(item -> !item.isEmpty())
        .map(item -> item.toUpperCase(Locale.ROOT))
        .distinct()
        .collect(Collectors.toList());
  }

  private static String joinRoles(List<String> roles, String fallback) {
    if (roles == null || roles.isEmpty()) {
      return fallback;
    }
    List<String> normalized = new ArrayList<>();
    for (String role : roles) {
      if (role == null || role.isBlank()) {
        continue;
      }
      String value = role.trim().toUpperCase(Locale.ROOT);
      if (!normalized.contains(value)) {
        normalized.add(value);
      }
    }
    return normalized.isEmpty() ? fallback : String.join(",", normalized);
  }

  private record CachedPolicy(SecurityPolicyView view, long expireAt) {}
}
