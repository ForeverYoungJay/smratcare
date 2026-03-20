package com.zhiyangyun.care.asset.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ResidenceBatchCommitResponse {
  private Integer createdBuildingCount = 0;
  private Integer createdFloorCount = 0;
  private Integer createdRoomCount = 0;
  private Integer createdBedCount = 0;
  private Integer updatedRoomCount = 0;
  private Integer updatedBedCount = 0;
  private Integer skippedCount = 0;
  private List<String> messages = new ArrayList<>();
}
