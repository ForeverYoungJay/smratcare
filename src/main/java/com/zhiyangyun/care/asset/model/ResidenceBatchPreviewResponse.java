package com.zhiyangyun.care.asset.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ResidenceBatchPreviewResponse {
  private String previewToken;
  private String mode;
  private String strategy;
  private Integer createBuildingCount = 0;
  private Integer createFloorCount = 0;
  private Integer createRoomCount = 0;
  private Integer createBedCount = 0;
  private Integer overwriteSafeCount = 0;
  private Integer skipCount = 0;
  private Integer conflictCount = 0;
  private List<String> warnings = new ArrayList<>();
  private List<String> conflicts = new ArrayList<>();
  private List<String> skipped = new ArrayList<>();
  private List<String> safeOverwriteTargets = new ArrayList<>();
  private List<ResidenceBatchPreviewItem> items = new ArrayList<>();
}
