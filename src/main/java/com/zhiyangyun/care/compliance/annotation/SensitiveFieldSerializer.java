package com.zhiyangyun.care.compliance.annotation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.zhiyangyun.care.compliance.config.SensitiveMaskingSupport;
import java.io.IOException;

/**
 * {@link SensitiveField} 对应的 Jackson 序列化器：按字段类型脱敏输出。
 * 是否脱敏由 {@link SensitiveMaskingSupport}（机构级开关 + 角色豁免）决定，任何异常均降级为原文输出，不影响主流程。
 */
public class SensitiveFieldSerializer extends JsonSerializer<String> implements ContextualSerializer {

  private final SensitiveType type;

  public SensitiveFieldSerializer() {
    this(null);
  }

  public SensitiveFieldSerializer(SensitiveType type) {
    this.type = type;
  }

  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if (value == null) {
      gen.writeNull();
      return;
    }
    if (type == null) {
      gen.writeString(value);
      return;
    }
    gen.writeString(SensitiveMaskingSupport.maskIfRequired(type, value));
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
    if (property != null) {
      SensitiveField annotation = property.getAnnotation(SensitiveField.class);
      if (annotation == null) {
        annotation = property.getContextAnnotation(SensitiveField.class);
      }
      if (annotation != null) {
        return new SensitiveFieldSerializer(annotation.type());
      }
    }
    return this;
  }
}
