package com.zhiyangyun.care.compliance.model;

import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 敏感数据访问审计概览（用于合规看板）。
 */
@Data
public class SensitiveAccessSummary {
  private int days;
  private long totalAccess;
  private long exportCount;
  private long deniedCount;
  private long distinctActors;
  private long distinctTargets;
  /** 按数据类别统计访问次数。 */
  private Map<String, Long> byCategory;
  /** 按动作统计（VIEW/EXPORT/DECRYPT/PRINT）。 */
  private Map<String, Long> byAction;
  /** 访问最频繁的操作人 TopN。 */
  private List<ActorAccessCount> topActors;

  @Data
  public static class ActorAccessCount {
    private Long actorId;
    private String actorName;
    private long count;
  }
}
