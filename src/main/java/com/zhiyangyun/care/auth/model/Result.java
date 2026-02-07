package com.zhiyangyun.care.auth.model;

import lombok.Data;

@Data
public class Result<T> {
  private int code;
  private String message;
  private T data;

  public static <T> Result<T> ok(T data) {
    Result<T> result = new Result<>();
    result.code = 0;
    result.message = "OK";
    result.data = data;
    return result;
  }

  public static <T> Result<T> error(int code, String message) {
    Result<T> result = new Result<>();
    result.code = code;
    result.message = message;
    return result;
  }
}
