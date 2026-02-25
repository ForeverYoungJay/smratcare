package com.zhiyangyun.care.store.model.material;

import java.util.Arrays;

public enum MaterialOrderStatus {
  DRAFT,
  APPROVED,
  COMPLETED,
  CANCELLED;

  public String code() {
    return name();
  }

  public static MaterialOrderStatus from(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    String normalized = raw.trim().toUpperCase();
    return Arrays.stream(values())
        .filter(item -> item.name().equals(normalized))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("invalid material order status: " + raw));
  }
}
