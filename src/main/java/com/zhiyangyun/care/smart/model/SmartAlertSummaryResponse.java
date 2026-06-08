package com.zhiyangyun.care.smart.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class SmartAlertSummaryResponse {
  private Long totalDeviceCount = 0L;
  private Long onlineDeviceCount = 0L;
  private Long offlineDeviceCount = 0L;
  private Long openAlertCount = 0L;
  private Long criticalAlertCount = 0L;
  private Long todayEventCount = 0L;
  private List<LevelCount> levelStats = new ArrayList<>();

  @Data
  public static class LevelCount {
    private String level;
    private Long count;

    public LevelCount(String level, Long count) {
      this.level = level;
      this.count = count;
    }
  }
}
