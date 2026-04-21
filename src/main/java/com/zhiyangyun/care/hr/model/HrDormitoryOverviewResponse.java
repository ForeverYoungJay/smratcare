package com.zhiyangyun.care.hr.model;

import lombok.Data;

@Data
public class HrDormitoryOverviewResponse {
  private Long staffCount;
  private Long liveInDormitoryCount;
  private Long assignedBedCount;
  private Long pendingAssignCount;
  private Long buildingCount;
  private Long roomCount;
  private Long configuredRoomCount;
  private Long configuredBedCapacity;
  private Long meterBoundCount;
  private Long conflictCount;
}
