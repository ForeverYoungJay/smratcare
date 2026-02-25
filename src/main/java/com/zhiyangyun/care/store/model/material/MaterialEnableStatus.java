package com.zhiyangyun.care.store.model.material;

import java.util.Arrays;

public enum MaterialEnableStatus {
  DISABLED(0),
  ENABLED(1);

  private final int code;

  MaterialEnableStatus(int code) {
    this.code = code;
  }

  public int code() {
    return code;
  }

  public static MaterialEnableStatus from(Integer raw) {
    if (raw == null) {
      return null;
    }
    return Arrays.stream(values())
        .filter(item -> item.code == raw)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("invalid enable status: " + raw));
  }
}
