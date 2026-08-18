package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.service.ElderOccupancyReadService;
import com.zhiyangyun.care.finance.entity.FinanceElectricityReading;
import com.zhiyangyun.care.finance.mapper.FinanceElectricityReadingMapper;
import com.zhiyangyun.care.finance.model.FloorPlanResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.YearMonth;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 楼栋平面图：把房间按朝向分到走廊两侧，并把欠费/空房/未启用的配色口径一次算清。
 * 颜色优先级：未启用 &gt; 空房 &gt; 欠费 &gt; 正常。
 */
@RestController
@RequestMapping("/api/finance/floor-plan")
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','NURSING_MINISTER','NURSING_EMPLOYEE','DIRECTOR','SYS_ADMIN','ADMIN')")
public class FloorPlanController {
  private final RoomMapper roomMapper;
  private final BedMapper bedMapper;
  private final ElderMapper elderMapper;
  private final BillMonthlyMapper billMonthlyMapper;
  private final FinanceElectricityReadingMapper electricityReadingMapper;
  private final ElderOccupancyReadService elderOccupancyReadService;

  public FloorPlanController(RoomMapper roomMapper,
      BedMapper bedMapper,
      ElderMapper elderMapper,
      BillMonthlyMapper billMonthlyMapper,
      FinanceElectricityReadingMapper electricityReadingMapper,
      ElderOccupancyReadService elderOccupancyReadService) {
    this.roomMapper = roomMapper;
    this.bedMapper = bedMapper;
    this.elderMapper = elderMapper;
    this.billMonthlyMapper = billMonthlyMapper;
    this.electricityReadingMapper = electricityReadingMapper;
    this.elderOccupancyReadService = elderOccupancyReadService;
  }

  @GetMapping
  public Result<FloorPlanResponse> floorPlan(@RequestParam(required = false) String month) {
    Long orgId = AuthContext.getOrgId();
    YearMonth targetMonth = parseMonth(month);
    FloorPlanResponse response = new FloorPlanResponse();
    response.setBillMonth(targetMonth.toString());

    List<Room> rooms = roomMapper.selectList(
        Wrappers.lambdaQuery(Room.class)
            .eq(Room::getIsDeleted, 0)
            .eq(orgId != null, Room::getOrgId, orgId));
    if (rooms.isEmpty()) {
      return Result.ok(response);
    }
    List<Bed> beds = bedMapper.selectList(
        Wrappers.lambdaQuery(Bed.class)
            .eq(Bed::getIsDeleted, 0)
            .eq(orgId != null, Bed::getOrgId, orgId));
    Map<Long, List<Bed>> bedsByRoom = beds.stream()
        .filter(item -> item.getRoomId() != null)
        .collect(Collectors.groupingBy(Bed::getRoomId));

    // elderId -> 占用的床位（口径与床位全景一致，避免 elder.bed_id 脏投影）
    Map<Long, Bed> occupiedBedByElder = elderOccupancyReadService.buildOccupiedBedMapByElderId(orgId, beds);
    Map<Long, List<Long>> elderIdsByRoom = new HashMap<>();
    for (Map.Entry<Long, Bed> entry : occupiedBedByElder.entrySet()) {
      Bed bed = entry.getValue();
      if (bed == null || bed.getRoomId() == null) {
        continue;
      }
      elderIdsByRoom.computeIfAbsent(bed.getRoomId(), key -> new ArrayList<>()).add(entry.getKey());
    }

    Map<Long, ElderProfile> elderMap = occupiedBedByElder.keySet().isEmpty()
        ? new HashMap<>()
        : elderMapper.selectList(
                Wrappers.lambdaQuery(ElderProfile.class)
                    .eq(ElderProfile::getIsDeleted, 0)
                    .in(ElderProfile::getId, occupiedBedByElder.keySet()))
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity(), (a, b) -> a, HashMap::new));

    // 当月每位长者的欠费
    Map<Long, BigDecimal> outstandingByElder = new HashMap<>();
    if (!elderMap.isEmpty()) {
      List<BillMonthly> bills = billMonthlyMapper.selectList(
          Wrappers.lambdaQuery(BillMonthly.class)
              .eq(BillMonthly::getIsDeleted, 0)
              .eq(orgId != null, BillMonthly::getOrgId, orgId)
              .eq(BillMonthly::getBillMonth, targetMonth.toString())
              .ne(BillMonthly::getStatus, 9)
              .in(BillMonthly::getElderId, elderMap.keySet()));
      for (BillMonthly bill : bills) {
        BigDecimal outstanding = scale2(bill.getOutstandingAmount()).max(BigDecimal.ZERO);
        outstandingByElder.merge(bill.getElderId(), outstanding, BigDecimal::add);
      }
    }

    // 当月每个房间的电费缴费状态
    Map<Long, FinanceElectricityReading> readingByRoom = electricityReadingMapper.selectList(
            Wrappers.lambdaQuery(FinanceElectricityReading.class)
                .eq(FinanceElectricityReading::getIsDeleted, 0)
                .eq(orgId != null, FinanceElectricityReading::getOrgId, orgId)
                .eq(FinanceElectricityReading::getBillMonth, targetMonth.toString()))
        .stream()
        .filter(item -> item.getRoomId() != null)
        .collect(Collectors.toMap(
            FinanceElectricityReading::getRoomId, Function.identity(), (a, b) -> a, HashMap::new));

    Map<String, Map<String, List<FloorPlanResponse.Room>>> grouped = new LinkedHashMap<>();
    int totalBeds = 0;
    int occupiedBeds = 0;
    int occupiedRooms = 0;
    int emptyRooms = 0;
    int disabledRooms = 0;
    int overdueRooms = 0;

    List<Room> sortedRooms = rooms.stream()
        .sorted(Comparator
            .comparing((Room item) -> normalizeText(item.getBuilding()))
            .thenComparing(item -> normalizeText(item.getFloorNo()))
            .thenComparing(item -> normalizeText(item.getRoomNo())))
        .toList();

    for (Room room : sortedRooms) {
      List<Bed> roomBeds = bedsByRoom.getOrDefault(room.getId(), List.of());
      List<Long> roomElderIds = elderIdsByRoom.getOrDefault(room.getId(), List.of());
      FloorPlanResponse.Room view = new FloorPlanResponse.Room();
      view.setRoomId(room.getId());
      view.setRoomNo(room.getRoomNo());
      view.setRoomType(room.getRoomType());
      view.setOrientation(room.getOrientation());
      view.setOrientationText(orientationText(room.getOrientation()));
      view.setCapacity(room.getCapacity() == null ? 0 : room.getCapacity());
      view.setTotalBeds(roomBeds.size());
      view.setOccupiedBeds(roomElderIds.size());
      view.setElderCount(roomElderIds.size());
      boolean enabled = !Integer.valueOf(0).equals(room.getStatus());
      view.setEnabled(enabled);

      BigDecimal roomOverdue = BigDecimal.ZERO;
      for (Long elderId : roomElderIds) {
        ElderProfile elder = elderMap.get(elderId);
        FloorPlanResponse.Resident resident = new FloorPlanResponse.Resident();
        resident.setElderId(elderId);
        resident.setElderName(elder == null ? ("长者#" + elderId) : elder.getFullName());
        resident.setCareLevel(elder == null ? null : elder.getCareLevel());
        Bed bed = occupiedBedByElder.get(elderId);
        resident.setBedNo(bed == null ? null : bed.getBedNo());
        BigDecimal outstanding = outstandingByElder.getOrDefault(elderId, BigDecimal.ZERO);
        resident.setOutstandingAmount(outstanding);
        roomOverdue = roomOverdue.add(outstanding);
        view.getResidents().add(resident);
      }
      view.setOverdueAmount(roomOverdue);

      FinanceElectricityReading reading = readingByRoom.get(room.getId());
      view.setElectricityUnpaid(reading != null && !"PAID".equals(reading.getPayStatus()));
      view.setElectricityFee(reading == null ? BigDecimal.ZERO : scale2(reading.getFeeAmount()));

      String planStatus;
      if (!enabled) {
        planStatus = "DISABLED";
        disabledRooms += 1;
      } else if (roomElderIds.isEmpty()) {
        planStatus = "EMPTY";
        emptyRooms += 1;
      } else if (roomOverdue.compareTo(BigDecimal.ZERO) > 0 || Boolean.TRUE.equals(view.getElectricityUnpaid())) {
        planStatus = "OVERDUE";
        overdueRooms += 1;
        occupiedRooms += 1;
      } else {
        planStatus = "NORMAL";
        occupiedRooms += 1;
      }
      view.setPlanStatus(planStatus);
      view.setPlanStatusText(planStatusText(planStatus));

      totalBeds += roomBeds.size();
      occupiedBeds += roomElderIds.size();

      String building = normalizeText(room.getBuilding()).isBlank() ? "未分楼栋" : room.getBuilding().trim();
      String floorNo = normalizeText(room.getFloorNo()).isBlank() ? "未分楼层" : room.getFloorNo().trim();
      grouped.computeIfAbsent(building, key -> new LinkedHashMap<>())
          .computeIfAbsent(floorNo, key -> new ArrayList<>())
          .add(view);
    }

    for (Map.Entry<String, Map<String, List<FloorPlanResponse.Room>>> buildingEntry : grouped.entrySet()) {
      FloorPlanResponse.Building building = new FloorPlanResponse.Building();
      building.setBuilding(buildingEntry.getKey());
      for (Map.Entry<String, List<FloorPlanResponse.Room>> floorEntry : buildingEntry.getValue().entrySet()) {
        FloorPlanResponse.Floor floor = new FloorPlanResponse.Floor();
        floor.setFloorNo(floorEntry.getKey());
        for (FloorPlanResponse.Room room : floorEntry.getValue()) {
          if ("SOUTH".equals(room.getOrientation())) {
            floor.getSouthRooms().add(room);
          } else if ("NORTH".equals(room.getOrientation())) {
            floor.getNorthRooms().add(room);
          } else {
            floor.getOtherRooms().add(room);
          }
          floor.setRoomCount(floor.getRoomCount() + 1);
          floor.setBedCount(floor.getBedCount() + room.getTotalBeds());
          floor.setOccupiedBeds(floor.getOccupiedBeds() + room.getOccupiedBeds());
        }
        building.setRoomCount(building.getRoomCount() + floor.getRoomCount());
        building.setBedCount(building.getBedCount() + floor.getBedCount());
        building.getFloors().add(floor);
      }
      response.getBuildings().add(building);
    }

    response.setTotalRooms(sortedRooms.size());
    response.setOccupiedRooms(occupiedRooms);
    response.setEmptyRooms(emptyRooms);
    response.setDisabledRooms(disabledRooms);
    response.setOverdueRooms(overdueRooms);
    response.setTotalBeds(totalBeds);
    response.setOccupiedBeds(occupiedBeds);
    response.setBedUsageRate(totalBeds <= 0
        ? BigDecimal.ZERO
        : BigDecimal.valueOf(occupiedBeds)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(totalBeds), 1, RoundingMode.HALF_UP));
    return Result.ok(response);
  }

  private static String orientationText(String orientation) {
    if (orientation == null) {
      return "未标注";
    }
    return switch (orientation) {
      case "SOUTH" -> "南向";
      case "NORTH" -> "北向";
      case "EAST" -> "东向";
      case "WEST" -> "西向";
      default -> orientation;
    };
  }

  private static String planStatusText(String planStatus) {
    return switch (planStatus) {
      case "DISABLED" -> "未启用";
      case "EMPTY" -> "空房";
      case "OVERDUE" -> "欠费";
      default -> "正常";
    };
  }

  private static YearMonth parseMonth(String month) {
    if (month == null || month.isBlank()) {
      return YearMonth.now();
    }
    try {
      return YearMonth.parse(month.trim());
    } catch (Exception ignored) {
      return YearMonth.now();
    }
  }

  private static String normalizeText(String value) {
    return value == null ? "" : value.trim();
  }

  private static BigDecimal scale2(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
  }
}
