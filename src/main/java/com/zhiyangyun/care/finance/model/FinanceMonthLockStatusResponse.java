package com.zhiyangyun.care.finance.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FinanceMonthLockStatusResponse {
  private String month;
  private boolean locked;
  private String closeStatus;
  private String closeStatusLabel;
  private String lockedBy;
  private LocalDateTime lockedAt;
  private String unlockedBy;
  private LocalDateTime unlockedAt;
  private String unlockReason;
}
