package com.zhiyangyun.care.compliance.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 敏感字段脱敏注解：标注在响应 DTO 的 String 字段上，序列化为 JSON 时按 {@link SensitiveType} 打码。
 *
 * <p>脱敏是否生效由两层控制（见 {@link com.zhiyangyun.care.compliance.config.SensitiveMaskingSupport}）：
 * <ul>
 *   <li>机构级开关：compliance_security_policy.masking_enabled（默认关闭，需在"安全策略配置"页开启）；</li>
 *   <li>角色豁免：命中 masking_exempt_roles（默认 SYS_ADMIN/ADMIN/DIRECTOR）的用户可看全文。</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveFieldSerializer.class)
public @interface SensitiveField {
  SensitiveType type();
}
