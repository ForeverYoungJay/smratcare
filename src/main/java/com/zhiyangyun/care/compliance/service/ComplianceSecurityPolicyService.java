package com.zhiyangyun.care.compliance.service;

import com.zhiyangyun.care.compliance.model.SecurityPolicyUpdateRequest;
import com.zhiyangyun.care.compliance.model.SecurityPolicyView;

public interface ComplianceSecurityPolicyService {

  /** 等保 2.0 日志留存下限（天），任何配置不得低于此值。 */
  int MIN_LOG_RETENTION_DAYS = 180;

  /**
   * 获取机构生效策略；无配置行/查询异常时返回内置默认值（默认 2FA 关、脱敏关、锁定 5 次/30 分钟、留存 400 天），
   * 保证策略读取失败不阻断登录等主流程。带短 TTL 缓存（脱敏序列化按字段高频调用）。
   */
  SecurityPolicyView getEffectivePolicy(Long orgId);

  /** 保存机构策略（不存在则创建），留存天数不得低于 {@link #MIN_LOG_RETENTION_DAYS}。 */
  SecurityPolicyView update(Long orgId, SecurityPolicyUpdateRequest request);
}
