package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** 楼栋平面图数据集：楼栋 → 楼层 → 按朝向分到走廊南北两侧的房间。 */
@Data
public class FloorPlanResponse {
  private String billMonth;
  private Integer totalRooms = 0;
  private Integer occupiedRooms = 0;
  private Integer emptyRooms = 0;
  private Integer disabledRooms = 0;
  private Integer overdueRooms = 0;
  private Integer totalBeds = 0;
  private Integer occupiedBeds = 0;
  /** 床位使用率，百分数保留 1 位。 */
  private BigDecimal bedUsageRate = BigDecimal.ZERO;
  private List<Building> buildings = new ArrayList<>();

  @Data
  public static class Building {
    private String building;
    private Integer roomCount = 0;
    private Integer bedCount = 0;
    private List<Floor> floors = new ArrayList<>();
  }

  @Data
  public static class Floor {
    private String floorNo;
    private Integer roomCount = 0;
    private Integer occupiedBeds = 0;
    private Integer bedCount = 0;
    /** 走廊南侧（朝南）。 */
    private List<Room> southRooms = new ArrayList<>();
    /** 走廊北侧（朝北）。 */
    private List<Room> northRooms = new ArrayList<>();
    /** 未标注朝向或东西向的房间。 */
    private List<Room> otherRooms = new ArrayList<>();
  }

  @Data
  public static class Room {
    private Long roomId;
    private String roomNo;
    private String roomType;
    private String orientation;
    private String orientationText;
    private Integer capacity = 0;
    private Integer totalBeds = 0;
    private Integer occupiedBeds = 0;
    private Integer elderCount = 0;
    private Boolean enabled = true;
    /** NORMAL 正常 / OVERDUE 欠费 / EMPTY 空房 / DISABLED 未启用。 */
    private String planStatus;
    private String planStatusText;
    private BigDecimal overdueAmount = BigDecimal.ZERO;
    private Boolean electricityUnpaid = false;
    private BigDecimal electricityFee = BigDecimal.ZERO;
    private List<Resident> residents = new ArrayList<>();
  }

  @Data
  public static class Resident {
    private Long elderId;
    private String elderName;
    private String bedNo;
    private String careLevel;
    private BigDecimal outstandingAmount = BigDecimal.ZERO;
  }
}
