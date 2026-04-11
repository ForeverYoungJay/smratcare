package com.zhiyangyun.care.elder.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class OccupancyRepairResponse {
  private int fixedElderProjectionCount;
  private int fixedBedProjectionCount;
  private int releasedOrphanBedCount;
  private int clearedOrphanElderCount;
  private List<String> actions = new ArrayList<>();
}
