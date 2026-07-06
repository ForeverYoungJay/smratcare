package com.zhiyangyun.care.compliance.service;

import com.zhiyangyun.care.compliance.model.SecurityPolicyView;

/**
 * 登录安全：登录留痕（成功/失败）与登录失败锁定校验。
 * 留痕与锁定校验的内部异常均不得阻断登录主流程（fail-open + warn 日志）。
 */
public interface LoginSecurityService {

  /**
   * 按机构策略校验账号是否因连续失败被锁定；锁定则抛出 IllegalArgumentException。
   * 规则：自最近一次成功登录之后、锁定窗口（login_fail_lock_minutes）内的失败次数 >= login_fail_lock_count 即锁定。
   */
  void assertNotLocked(String username, SecurityPolicyView policy);

  /** 记录一次登录事件（loginType：PASSWORD / 2FA_CHALLENGE / 2FA_VERIFY）。 */
  void record(Long orgId, Long staffId, String username, String loginType, boolean success, String failReason);
}
