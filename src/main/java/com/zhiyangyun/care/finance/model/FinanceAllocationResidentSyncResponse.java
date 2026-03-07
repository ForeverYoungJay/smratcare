package com.zhiyangyun.care.finance.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class FinanceAllocationResidentSyncResponse {
  private LocalDateTime syncTime;
  private String selectedBuilding;
  private String selectedFloorNo;
  private String selectedRoomNo;
  private List<String> buildingOptions = new ArrayList<>();
  private List<String> floorOptions = new ArrayList<>();
  private List<String> roomOptions = new ArrayList<>();
  private List<ResidentOption> residentOptions = new ArrayList<>();

  @Data
  public static class ResidentOption {
    private Long elderId;
    private String elderName;
    private Integer elderStatus;
    private Long bedId;
    private String bedNo;
    private Long roomId;
    private String roomNo;
    private String building;
    private String floorNo;
  }
}
