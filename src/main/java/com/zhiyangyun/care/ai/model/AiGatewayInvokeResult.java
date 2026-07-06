package com.zhiyangyun.care.ai.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/** AI 网关调用结果（规则引擎版返回结构化数据，LLM 版可返回生成文本）。 */
@Data
public class AiGatewayInvokeResult {
  private boolean success;
  private String provider;
  private String scene;
  private String message;
  private Map<String, Object> data = new HashMap<>();

  public static AiGatewayInvokeResult ok(String provider, String scene, Map<String, Object> data) {
    AiGatewayInvokeResult result = new AiGatewayInvokeResult();
    result.setSuccess(true);
    result.setProvider(provider);
    result.setScene(scene);
    if (data != null) {
      result.setData(data);
    }
    return result;
  }

  public static AiGatewayInvokeResult fail(String provider, String scene, String message) {
    AiGatewayInvokeResult result = new AiGatewayInvokeResult();
    result.setSuccess(false);
    result.setProvider(provider);
    result.setScene(scene);
    result.setMessage(message);
    return result;
  }
}
