package com.zhiyangyun.care.asset.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ResidenceBatchGenerationRequest {
  @NotBlank
  private String mode = "FULL_INIT";

  private String strategy = "FILL_MISSING";

  private List<String> buildingNames = new ArrayList<>();

  @Min(1)
  @Max(20)
  private Integer buildingCount = 1;

  @Min(1)
  @Max(999)
  private Integer buildingStartNo = 1;

  private String buildingNameStyle = "NUMERIC_SUFFIX";
  private String buildingCodePrefix = "B";

  private Long buildingId;
  private Long floorId;
  private Long roomId;

  @Min(1)
  @Max(50)
  private Integer floorStartNo = 1;

  @Min(1)
  @Max(50)
  private Integer floorEndNo = 8;

  private String floorNameStyle = "F";

  @Min(1)
  @Max(999)
  private Integer roomStartNo = 1;

  @Min(1)
  @Max(999)
  private Integer roomEndNo = 20;

  @Min(2)
  @Max(3)
  private Integer roomSeqWidth = 2;

  private String roomType = "ROOM_DOUBLE";
  private String defaultGenderLimit;
  private String defaultBedType;

  @Min(0)
  @Max(12)
  private Integer bedsPerRoom;

  private String bedLabelStyle = "ALPHA";
  private String bedNoSeparator = "-";

  private List<ResidenceGenerationFloorRule> floorRules = new ArrayList<>();
  private List<ResidenceGenerationSpecialRoomRule> specialRooms = new ArrayList<>();
}
