package com.zhiyangyun.care.compliance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.compliance.entity.SensitiveAccessLog;
import com.zhiyangyun.care.compliance.model.SensitiveAccessSummary;
import java.time.LocalDateTime;

/**
 * 敏感数据访问审计服务。业务代码在展示/导出敏感个人信息时显式调用 record 留痕。
 */
public interface SensitiveAccessService {

  /**
   * 记录一次敏感数据访问。操作人、机构、IP、requestId 由上下文自动补全。
   *
   * @param action       动作：VIEW / EXPORT / DECRYPT / PRINT
   * @param dataCategory 数据类别：ELDER_HEALTH / ELDER_IDCARD / FAMILY_PRIVACY / BILL ...
   * @param targetType   目标类型：ELDER / FAMILY / BILL ...
   * @param targetId     目标主键
   * @param targetName   目标名称（建议脱敏后传入）
   * @param fields       涉及的敏感字段（逗号分隔）
   * @param purpose      访问用途/业务场景
   * @param success      是否成功（false 表示被拒绝访问）
   */
  void record(String action, String dataCategory, String targetType, Long targetId,
              String targetName, String fields, String purpose, boolean success);

  IPage<SensitiveAccessLog> page(Long orgId, Long actorId, String dataCategory, String action,
                                 LocalDateTime start, LocalDateTime end, long pageNo, long pageSize);

  SensitiveAccessSummary summary(Long orgId, int days);
}
