package com.zhiyangyun.care.elder.support;

import com.zhiyangyun.care.elder.entity.ElderFamily;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 家属绑定的逐项授权：亲属关系、代理权、紧急联系人与数据查看权限分别核验 */
public final class FamilyAuthorization {
  public static final String HEALTH = "HEALTH";
  public static final String BILLING = "BILLING";
  public static final String PHOTO = "PHOTO";
  public static final List<String> ALL = List.of(HEALTH, BILLING, PHOTO, "PROXY", "EMERGENCY");

  private FamilyAuthorization() {}

  /** 未过期（有效期含当天）；撤销的绑定已软删除，不会被查到 */
  public static boolean active(ElderFamily relation, LocalDate today) {
    return relation != null && (relation.getValidUntil() == null || !relation.getValidUntil().isBefore(today));
  }

  /** authScopes 为空是历史绑定，保持原来的全量可见 */
  public static boolean allows(ElderFamily relation, String scope) {
    if (relation == null) {
      return false;
    }
    String scopes = relation.getAuthScopes();
    return scopes == null || Arrays.asList(scopes.split(",")).contains(scope);
  }

  /** 只保留合法的授权项，按固定顺序存储 */
  public static String normalize(List<String> scopes) {
    if (scopes == null) {
      return "";
    }
    Set<String> picked = new LinkedHashSet<>();
    for (String scope : ALL) {
      if (scopes.contains(scope)) {
        picked.add(scope);
      }
    }
    return String.join(",", picked);
  }
}
