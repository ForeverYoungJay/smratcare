package com.zhiyangyun.care.common.web;

public final class RequestTraceContext {
  private static final ThreadLocal<String> REQUEST_ID_HOLDER = new ThreadLocal<>();

  private RequestTraceContext() {
  }

  public static void setRequestId(String requestId) {
    if (requestId == null || requestId.isBlank()) {
      REQUEST_ID_HOLDER.remove();
      return;
    }
    REQUEST_ID_HOLDER.set(requestId.trim());
  }

  public static String getRequestId() {
    return REQUEST_ID_HOLDER.get();
  }

  public static void clear() {
    REQUEST_ID_HOLDER.remove();
  }
}
