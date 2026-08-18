package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderBedRelation;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderBedRelationMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.model.BedStatus;
import com.zhiyangyun.care.finance.controller.FloorPlanController;
import com.zhiyangyun.care.finance.model.FloorPlanResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

/** 平面图：按朝向分走廊两侧，颜色优先级 未启用 &gt; 空房 &gt; 欠费 &gt; 正常。 */
@SpringBootTest
@ActiveProfiles("test")
class FloorPlanControllerTest {
  private static final Long ORG_ID = 9601L;

  @Autowired
  private FloorPlanController floorPlanController;

  @Autowired
  private RoomMapper roomMapper;

  @Autowired
  private BedMapper bedMapper;

  @Autowired
  private ElderMapper elderMapper;

  @Autowired
  private ElderBedRelationMapper relationMapper;

  @Autowired
  private BillMonthlyMapper billMonthlyMapper;

  @BeforeEach
  void mockAuth() {
    var auth = new UsernamePasswordAuthenticationToken(
        "9601", "N/A", List.of(new SimpleGrantedAuthority("ROLE_FINANCE_EMPLOYEE")));
    auth.setDetails(Map.of("orgId", ORG_ID, "username", "plan-tester"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void clearAuth() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void groups_rooms_by_orientation_and_resolves_colors() {
    String month = YearMonth.now().toString();

    // 南向且住户有欠费 → 黄（欠费）
    Room southOverdue = insertRoom("P-101", "SOUTH", 1);
    ElderProfile owing = insertResident(southOverdue, "平面图欠费");
    insertBill(owing.getId(), month, "1000", "400");

    // 南向且住户无欠费 → 绿（正常）
    Room southNormal = insertRoom("P-102", "SOUTH", 1);
    ElderProfile paid = insertResident(southNormal, "平面图正常");
    insertBill(paid.getId(), month, "1000", "0");

    // 北向空房 → 红（空房）
    Room northEmpty = insertRoom("P-201", "NORTH", 1);
    insertBed(northEmpty, "P-201-1");

    // 未标注朝向且停用 → 灰（未启用），优先级高于空房
    Room disabled = insertRoom("P-301", null, 0);
    insertBed(disabled, "P-301-1");

    FloorPlanResponse plan = floorPlanController.floorPlan(month).getData();
    assertNotNull(plan);
    var building = plan.getBuildings().stream()
        .filter(item -> "平面图测试楼".equals(item.getBuilding()))
        .findFirst()
        .orElseThrow();
    var floor = building.getFloors().get(0);

    assertEquals(2, floor.getSouthRooms().size(), "两间南向房应落在走廊南侧");
    assertEquals(1, floor.getNorthRooms().size(), "一间北向房应落在走廊北侧");
    assertEquals(1, floor.getOtherRooms().size(), "未标注朝向的房间单独分组");

    assertEquals("OVERDUE", roomOf(floor.getSouthRooms(), "P-101").getPlanStatus());
    assertEquals(0, new BigDecimal("400.00")
        .compareTo(roomOf(floor.getSouthRooms(), "P-101").getOverdueAmount()));
    assertEquals("NORMAL", roomOf(floor.getSouthRooms(), "P-102").getPlanStatus());
    assertEquals("EMPTY", roomOf(floor.getNorthRooms(), "P-201").getPlanStatus());
    assertEquals("DISABLED", roomOf(floor.getOtherRooms(), "P-301").getPlanStatus());

    // 顶部统计
    assertEquals(4, plan.getTotalRooms());
    assertEquals(2, plan.getOccupiedRooms());
    assertEquals(1, plan.getEmptyRooms());
    assertEquals(1, plan.getDisabledRooms());
    assertEquals(1, plan.getOverdueRooms());
    assertEquals(4, plan.getTotalBeds());
    assertEquals(2, plan.getOccupiedBeds());
    assertEquals(0, new BigDecimal("50.0").compareTo(plan.getBedUsageRate()));

    // 房间概览要能带出在住长者
    var residents = roomOf(floor.getSouthRooms(), "P-101").getResidents();
    assertEquals(1, residents.size());
    assertTrue(residents.get(0).getElderName().contains("平面图欠费"));
  }

  private FloorPlanResponse.Room roomOf(List<FloorPlanResponse.Room> rooms, String roomNo) {
    return rooms.stream()
        .filter(item -> roomNo.equals(item.getRoomNo()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("未找到房间 " + roomNo));
  }

  private Room insertRoom(String roomNo, String orientation, int status) {
    Room room = new Room();
    room.setTenantId(ORG_ID);
    room.setOrgId(ORG_ID);
    room.setBuilding("平面图测试楼");
    room.setFloorNo("1F");
    room.setRoomNo(roomNo);
    room.setOrientation(orientation);
    room.setCapacity(1);
    room.setStatus(status);
    roomMapper.insert(room);
    return room;
  }

  private Bed insertBed(Room room, String bedNo) {
    Bed bed = new Bed();
    bed.setTenantId(ORG_ID);
    bed.setOrgId(ORG_ID);
    bed.setRoomId(room.getId());
    bed.setBedNo(bedNo);
    bed.setBedQrCode("QR-" + bedNo);
    bed.setStatus(BedStatus.AVAILABLE);
    bedMapper.insert(bed);
    return bed;
  }

  private ElderProfile insertResident(Room room, String fullName) {
    Bed bed = insertBed(room, room.getRoomNo() + "-1");
    ElderProfile elder = new ElderProfile();
    elder.setTenantId(ORG_ID);
    elder.setOrgId(ORG_ID);
    elder.setFullName(fullName);
    elder.setStatus(1);
    elder.setBedId(bed.getId());
    elderMapper.insert(elder);

    bed.setElderId(elder.getId());
    bed.setStatus(BedStatus.OCCUPIED);
    bedMapper.updateById(bed);

    ElderBedRelation relation = new ElderBedRelation();
    relation.setTenantId(ORG_ID);
    relation.setOrgId(ORG_ID);
    relation.setElderId(elder.getId());
    relation.setBedId(bed.getId());
    relation.setStartDate(LocalDate.now());
    relation.setActiveFlag(1);
    relationMapper.insert(relation);
    return elder;
  }

  private void insertBill(Long elderId, String month, String total, String outstanding) {
    BillMonthly bill = new BillMonthly();
    bill.setOrgId(ORG_ID);
    bill.setElderId(elderId);
    bill.setBillMonth(month);
    bill.setTotalAmount(new BigDecimal(total));
    bill.setOutstandingAmount(new BigDecimal(outstanding));
    bill.setPaidAmount(new BigDecimal(total).subtract(new BigDecimal(outstanding)));
    billMonthlyMapper.insert(bill);
  }
}
