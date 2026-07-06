package com.zhiyangyun.care.compliance.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.common.web.RequestTraceContext;
import com.zhiyangyun.care.compliance.entity.AuthLoginLog;
import com.zhiyangyun.care.compliance.mapper.AuthLoginLogMapper;
import com.zhiyangyun.care.compliance.model.SecurityPolicyView;
import com.zhiyangyun.care.compliance.service.LoginSecurityService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class LoginSecurityServiceImpl implements LoginSecurityService {
  private static final Logger log = LoggerFactory.getLogger(LoginSecurityServiceImpl.class);

  private final AuthLoginLogMapper mapper;

  public LoginSecurityServiceImpl(AuthLoginLogMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void assertNotLocked(String username, SecurityPolicyView policy) {
    if (username == null || username.isBlank() || policy == null) {
      return;
    }
    Integer lockCount = policy.getLoginFailLockCount();
    if (lockCount == null || lockCount <= 0) {
      return;
    }
    int lockMinutes = policy.getLoginFailLockMinutes() == null || policy.getLoginFailLockMinutes() <= 0
        ? 30 : policy.getLoginFailLockMinutes();
    Long recentFailures;
    try {
      LocalDateTime windowStart = LocalDateTime.now().minusMinutes(lockMinutes);
      AuthLoginLog lastSuccess = mapper.selectOne(Wrappers.lambdaQuery(AuthLoginLog.class)
          .eq(AuthLoginLog::getIsDeleted, 0)
          .eq(AuthLoginLog::getUsername, username)
          .eq(AuthLoginLog::getResult, "SUCCESS")
          .orderByDesc(AuthLoginLog::getCreateTime)
          .last("LIMIT 1"));
      LocalDateTime since = windowStart;
      if (lastSuccess != null && lastSuccess.getCreateTime() != null && lastSuccess.getCreateTime().isAfter(windowStart)) {
        since = lastSuccess.getCreateTime();
      }
      recentFailures = mapper.selectCount(Wrappers.lambdaQuery(AuthLoginLog.class)
          .eq(AuthLoginLog::getIsDeleted, 0)
          .eq(AuthLoginLog::getUsername, username)
          .eq(AuthLoginLog::getResult, "FAIL")
          .gt(AuthLoginLog::getCreateTime, since));
    } catch (Exception ex) {
      // 锁定校验失败不得阻断登录（如表尚未初始化）
      log.warn("[Compliance] 登录锁定校验失败，跳过: {}", ex.getMessage());
      return;
    }
    if (recentFailures != null && recentFailures >= lockCount) {
      throw new IllegalArgumentException("登录失败次数过多，账号已临时锁定，请 " + lockMinutes + " 分钟后再试");
    }
  }

  @Override
  public void record(Long orgId, Long staffId, String username, String loginType, boolean success, String failReason) {
    try {
      AuthLoginLog record = new AuthLoginLog();
      record.setTenantId(orgId);
      record.setOrgId(orgId);
      record.setStaffId(staffId);
      record.setUsername(username == null ? "" : truncate(username, 120));
      record.setLoginType(loginType == null ? "PASSWORD" : loginType);
      record.setResult(success ? "SUCCESS" : "FAIL");
      record.setFailReason(truncate(failReason, 255));
      record.setRequestId(RequestTraceContext.getRequestId());
      HttpServletRequest request = currentRequest();
      if (request != null) {
        record.setIp(resolveIp(request));
        record.setUserAgent(truncate(request.getHeader("User-Agent"), 255));
      }
      record.setIsDeleted(0);
      mapper.insert(record);
    } catch (Exception ex) {
      // 登录留痕不得影响登录主流程
      log.warn("[Compliance] 登录留痕失败: {}", ex.getMessage());
    }
  }

  private static String truncate(String value, int maxLength) {
    if (value == null) {
      return null;
    }
    return value.length() > maxLength ? value.substring(0, maxLength) : value;
  }

  private static HttpServletRequest currentRequest() {
    var attrs = RequestContextHolder.getRequestAttributes();
    if (attrs instanceof ServletRequestAttributes servletAttrs) {
      return servletAttrs.getRequest();
    }
    return null;
  }

  private static String resolveIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
