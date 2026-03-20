package com.zhiyangyun.care.asset.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ResidenceGenerationFloorRule {
  private Integer floorNo;
  private Boolean skipRoomGeneration = false;
  private String roomType;
  private String genderLimit;
  private List<String> excludedRoomNos = new ArrayList<>();
}
