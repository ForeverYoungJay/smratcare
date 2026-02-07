package com.zhiyangyun.care.elder.util;

import java.util.UUID;

public class QrCodeUtil {
  private QrCodeUtil() {}

  public static String generate() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
