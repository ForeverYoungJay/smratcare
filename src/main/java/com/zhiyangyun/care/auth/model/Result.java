package com.zhiyangyun.care.auth.model;

import com.zhiyangyun.care.common.web.RequestTraceContext;
import lombok.Data;

@Data
public class Result<T> {
  private int code;
  private String message;
  private T data;
  private boolean success;
  private long timestamp;
  private String requestId;

  public static <T> Result<T> ok(T data) {
    Result<T> result = new Result<>();
    result.code = 0;
    result.message = "OK";
    result.data = data;
    result.success = true;
    result.timestamp = System.currentTimeMillis();
    result.requestId = RequestTraceContext.getRequestId();
    return result;
  }

  public static <T> Result<T> error(int code, String message) {
    Result<T> result = new Result<>();
    result.code = code;
    result.message = message;
    result.success = false;
    result.timestamp = System.currentTimeMillis();
    result.requestId = RequestTraceContext.getRequestId();
    return result;
  }
}
