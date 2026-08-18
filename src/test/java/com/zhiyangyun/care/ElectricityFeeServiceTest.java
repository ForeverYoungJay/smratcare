package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.finance.entity.FinanceElectricityReading;
import com.zhiyangyun.care.finance.mapper.FinanceElectricityReadingMapper;
import com.zhiyangyun.care.finance.model.ElectricityPriceRequest;
import com.zhiyangyun.care.finance.model.ElectricityReadingRequest;
import com.zhiyangyun.care.finance.service.ElectricityFeeService;
import java.math.BigDecimal;
import java.time.LocalDate;
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

/** 电费：用电量 = 本期 − 上期；电费 = 用电量 × 生效电价。 */
@SpringBootTest
@ActiveProfiles("test")
class ElectricityFeeServiceTest {
  // 用独立机构号，避免污染 org 1 的房间数/聚合断言
  private static final Long ORG_ID = 9301L;

  @Autowired
  private ElectricityFeeService electricityFeeService;

  @Autowired
  private RoomMapper roomMapper;

  @Autowired
  private FinanceElectricityReadingMapper readingMapper;

  @BeforeEach
  void mockAuth() {
    var auth = new UsernamePasswordAuthenticationToken(
        "9301", "N/A", List.of(new SimpleGrantedAuthority("ROLE_FINANCE_EMPLOYEE")));
    auth.setDetails(Map.of("orgId", ORG_ID, "username", "elec-tester"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void clearAuth() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void reading_without_price_is_rejected() {
    Long orgWithoutPrice = 9399L;
    var auth = new UsernamePasswordAuthenticationToken(
        "9399", "N/A", List.of(new SimpleGrantedAuthority("ROLE_FINANCE_EMPLOYEE")));
    auth.setDetails(Map.of("orgId", orgWithoutPrice, "username", "no-price"));
    SecurityContextHolder.getContext().setAuthentication(auth);

    Room room = insertRoom(orgWithoutPrice, "E-NOPRICE-1");
    ElectricityReadingRequest request = readingRequest("2026-04", room.getId(), 0, 100);

    assertThrows(IllegalStateException.class, () -> electricityFeeService.saveReading(request, 700L));
  }

  @Test
  void reading_computes_usage_and_fee() {
    savePrice("0.6800", LocalDate.of(2026, 1, 1));
    Room room = insertRoom(ORG_ID, "E-CALC-1");

    var view = electricityFeeService.saveReading(
        readingRequest("2026-04", room.getId(), 1000, 1250), 700L);

    assertEquals(0, BigDecimal.valueOf(250).compareTo(view.getUsageAmount()));
    // 250 × 0.68 = 170.00
    assertEquals(0, new BigDecimal("170.00").compareTo(view.getFeeAmount()));
    assertEquals("UNPAID", view.getPayStatus());
    assertEquals("ROOM", view.getShareMode());
  }

  @Test
  void current_reading_below_previous_is_rejected() {
    savePrice("0.6800", LocalDate.of(2026, 1, 1));
    Room room = insertRoom(ORG_ID, "E-ROLLBACK-1");
    ElectricityReadingRequest request = readingRequest("2026-04", room.getId(), 500, 400);

    assertThrows(IllegalArgumentException.class, () -> electricityFeeService.saveReading(request, 700L));
  }

  @Test
  void previous_reading_defaults_to_last_month_current_reading() {
    savePrice("1.0000", LocalDate.of(2026, 1, 1));
    Room room = insertRoom(ORG_ID, "E-CARRY-1");

    electricityFeeService.saveReading(readingRequest("2026-04", room.getId(), 0, 800), 700L);
    assertEquals(0, BigDecimal.valueOf(800)
        .compareTo(electricityFeeService.suggestPreviousReading("2026-05", room.getId())));

    ElectricityReadingRequest next = new ElectricityReadingRequest();
    next.setBillMonth("2026-05");
    next.setRoomId(room.getId());
    next.setCurrentReading(BigDecimal.valueOf(950));
    var view = electricityFeeService.saveReading(next, 700L);

    assertEquals(0, BigDecimal.valueOf(800).compareTo(view.getPreviousReading()));
    assertEquals(0, BigDecimal.valueOf(150).compareTo(view.getUsageAmount()));
  }

  @Test
  void repeat_registration_updates_same_record() {
    savePrice("1.0000", LocalDate.of(2026, 1, 1));
    Room room = insertRoom(ORG_ID, "E-UPSERT-1");

    electricityFeeService.saveReading(readingRequest("2026-04", room.getId(), 0, 100), 700L);
    var updated = electricityFeeService.saveReading(readingRequest("2026-04", room.getId(), 0, 180), 700L);

    assertEquals(0, BigDecimal.valueOf(180).compareTo(updated.getCurrentReading()));
    long count = readingMapper.selectCount(
        Wrappers.lambdaQuery(FinanceElectricityReading.class)
            .eq(FinanceElectricityReading::getIsDeleted, 0)
            .eq(FinanceElectricityReading::getOrgId, ORG_ID)
            .eq(FinanceElectricityReading::getBillMonth, "2026-04")
            .eq(FinanceElectricityReading::getRoomId, room.getId()));
    assertEquals(1L, count);
  }

  @Test
  void per_resident_share_without_residents_is_rejected() {
    savePrice("1.0000", LocalDate.of(2026, 1, 1));
    Room room = insertRoom(ORG_ID, "E-SHARE-1");
    ElectricityReadingRequest request = readingRequest("2026-04", room.getId(), 0, 100);
    request.setShareMode("PER_RESIDENT");

    assertThrows(IllegalStateException.class, () -> electricityFeeService.saveReading(request, 700L));
  }

  @Test
  void price_resolves_by_nearest_effective_date() {
    savePrice("0.5000", LocalDate.of(2026, 1, 1));
    savePrice("0.9000", LocalDate.of(2026, 6, 1));

    assertEquals(0, new BigDecimal("0.5000").compareTo(electricityFeeService.resolveUnitPrice("2026-03")));
    assertEquals(0, new BigDecimal("0.9000").compareTo(electricityFeeService.resolveUnitPrice("2026-07")));
  }

  @Test
  void pay_status_toggle_reflects_in_summary() {
    savePrice("1.0000", LocalDate.of(2026, 1, 1));
    Room room = insertRoom(ORG_ID, "E-PAY-1");
    var view = electricityFeeService.saveReading(
        readingRequest("2026-09", room.getId(), 0, 120), 700L);

    var beforePay = electricityFeeService.summary("2026-09");
    assertEquals(1, beforePay.getUnpaidRoomCount());
    assertEquals(0, BigDecimal.valueOf(120).compareTo(beforePay.getUnpaidFee()));

    electricityFeeService.updatePayStatus(view.getId(), true, "现金收取", 700L);
    var afterPay = electricityFeeService.summary("2026-09");
    assertEquals(0, afterPay.getUnpaidRoomCount());
    assertEquals(0, BigDecimal.ZERO.compareTo(afterPay.getUnpaidFee()));
    assertEquals(0, BigDecimal.valueOf(120).compareTo(afterPay.getTotalFee()));
  }

  @Test
  void paid_reading_can_not_be_edited_before_reverting() {
    savePrice("1.0000", LocalDate.of(2026, 1, 1));
    Room room = insertRoom(ORG_ID, "E-LOCK-1");
    var view = electricityFeeService.saveReading(
        readingRequest("2026-10", room.getId(), 0, 100), 700L);
    electricityFeeService.updatePayStatus(view.getId(), true, null, 700L);

    ElectricityReadingRequest request = readingRequest("2026-10", room.getId(), 0, 200);
    assertThrows(IllegalStateException.class, () -> electricityFeeService.saveReading(request, 700L));

    electricityFeeService.updatePayStatus(view.getId(), false, null, 700L);
    var reopened = electricityFeeService.saveReading(request, 700L);
    assertEquals(0, BigDecimal.valueOf(200).compareTo(reopened.getCurrentReading()));
  }

  private void savePrice(String unitPrice, LocalDate effectiveFrom) {
    ElectricityPriceRequest request = new ElectricityPriceRequest();
    request.setUnitPrice(new BigDecimal(unitPrice));
    request.setEffectiveFrom(effectiveFrom);
    electricityFeeService.savePrice(request, 700L);
  }

  private Room insertRoom(Long orgId, String roomNo) {
    Room room = new Room();
    room.setTenantId(orgId);
    room.setOrgId(orgId);
    room.setBuilding("电费测试楼");
    room.setFloorNo("1F");
    room.setRoomNo(roomNo);
    room.setCapacity(2);
    room.setStatus(1);
    roomMapper.insert(room);
    return room;
  }

  private ElectricityReadingRequest readingRequest(
      String billMonth, Long roomId, int previous, int current) {
    ElectricityReadingRequest request = new ElectricityReadingRequest();
    request.setBillMonth(billMonth);
    request.setRoomId(roomId);
    request.setPreviousReading(BigDecimal.valueOf(previous));
    request.setCurrentReading(BigDecimal.valueOf(current));
    return request;
  }
}
