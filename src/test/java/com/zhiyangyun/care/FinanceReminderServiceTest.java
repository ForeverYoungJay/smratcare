package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.finance.model.DepositStandardRequest;
import com.zhiyangyun.care.finance.model.DepositTransactionRequest;
import com.zhiyangyun.care.finance.model.ElectricityPriceRequest;
import com.zhiyangyun.care.finance.model.ElectricityReadingRequest;
import com.zhiyangyun.care.finance.service.DepositService;
import com.zhiyangyun.care.finance.service.ElectricityFeeService;
import com.zhiyangyun.care.finance.service.FinanceReminderService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

/** 财务提醒：生成幂等、按类型统计、逐条与一键处理。 */
@SpringBootTest
@ActiveProfiles("test")
class FinanceReminderServiceTest {
  @Autowired
  private FinanceReminderService reminderService;

  @Autowired
  private DepositService depositService;

  @Autowired
  private ElectricityFeeService electricityFeeService;

  @Autowired
  private ElderMapper elderMapper;

  @Autowired
  private RoomMapper roomMapper;

  @Autowired
  private BillMonthlyMapper billMonthlyMapper;

  @AfterEach
  void clearAuth() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void care_fee_reminder_is_idempotent_per_next_month() {
    Long orgId = 9501L;
    useOrg(orgId);
    LocalDate today = LocalDate.of(2026, 5, 18);
    insertElder(orgId, "提醒在住甲");
    insertBill(orgId, "2026-05", "1200");

    assertEquals(1, reminderService.generateNextMonthCareFeeReminder(orgId, today));
    assertEquals(0, reminderService.generateNextMonthCareFeeReminder(orgId, today));

    var page = reminderService.page(1, 10, "PENDING", "NEXT_MONTH_CARE_FEE");
    assertEquals(1, page.getRecords().size());
    var view = page.getRecords().get(0);
    assertEquals("2026-06", view.getBizMonth());
    assertTrue(view.getContent().contains("1200"), "应带上当月账单合计作为参考");
    // 换月后是新的提醒周期
    assertEquals(1, reminderService.generateNextMonthCareFeeReminder(orgId, today.plusMonths(1)));
  }

  @Test
  void deposit_shortfall_reminder_covers_only_unpaid_accounts() {
    Long orgId = 9502L;
    useOrg(orgId);
    saveDepositStandard("1000");
    ElderProfile owing = insertElder(orgId, "提醒押金欠");
    ElderProfile settled = insertElder(orgId, "提醒押金齐");
    depositService.registerPay(depositTxn(owing.getId(), "300"), 900L);
    depositService.registerPay(depositTxn(settled.getId(), "1000"), 900L);

    int created = reminderService.generateDepositShortfallReminders(orgId, LocalDate.now());
    assertEquals(1, created);

    var page = reminderService.page(1, 10, "PENDING", "DEPOSIT_SHORTFALL");
    assertEquals(1, page.getRecords().size());
    assertEquals(owing.getId(), page.getRecords().get(0).getElderId());
    assertEquals(0, new BigDecimal("700.00").compareTo(page.getRecords().get(0).getAmount()));
  }

  @Test
  void electricity_reminders_cover_unrecorded_and_unpaid() {
    Long orgId = 9503L;
    useOrg(orgId);
    ElectricityPriceRequest price = new ElectricityPriceRequest();
    price.setUnitPrice(new BigDecimal("1.0000"));
    price.setEffectiveFrom(LocalDate.now().minusYears(1));
    electricityFeeService.savePrice(price, 900L);

    Room recorded = insertRoom(orgId, "R-REMIND-1");
    insertRoom(orgId, "R-REMIND-2");
    ElectricityReadingRequest reading = new ElectricityReadingRequest();
    reading.setBillMonth(YearMonth.now().toString());
    reading.setRoomId(recorded.getId());
    reading.setPreviousReading(BigDecimal.ZERO);
    reading.setCurrentReading(new BigDecimal("90"));
    electricityFeeService.saveReading(reading, 900L);

    int created = reminderService.generateElectricityReminders(orgId, LocalDate.now());
    // 1 条未登记汇总 + 1 条未缴
    assertEquals(2, created);

    assertEquals(1, reminderService.page(1, 10, "PENDING", "ELECTRICITY_UNRECORDED").getRecords().size());
    assertEquals(1, reminderService.page(1, 10, "PENDING", "ELECTRICITY_UNPAID").getRecords().size());
    // 重复生成不应新增
    assertEquals(0, reminderService.generateElectricityReminders(orgId, LocalDate.now()));
  }

  @Test
  void handle_single_and_handle_all_clear_pending_count() {
    Long orgId = 9504L;
    useOrg(orgId);
    saveDepositStandard("1000");
    ElderProfile first = insertElder(orgId, "提醒处置甲");
    ElderProfile second = insertElder(orgId, "提醒处置乙");
    depositService.registerPay(depositTxn(first.getId(), "100"), 900L);
    depositService.registerPay(depositTxn(second.getId(), "200"), 900L);
    reminderService.generateDepositShortfallReminders(orgId, LocalDate.now());

    var summaryBefore = reminderService.summary();
    assertEquals(2, summaryBefore.getPendingCount());
    assertEquals(2, summaryBefore.getPendingByType().get("DEPOSIT_SHORTFALL"));

    Long firstReminderId = reminderService.page(1, 10, "PENDING", null).getRecords().get(0).getId();
    var handled = reminderService.handle(firstReminderId, "已联系家属");
    assertEquals("HANDLED", handled.getStatus());
    assertEquals(1, reminderService.summary().getPendingCount());

    assertEquals(1, reminderService.handleAll(null, "一键处理"));
    assertEquals(0, reminderService.summary().getPendingCount());
    assertEquals(2, reminderService.summary().getHandledCount());
  }

  private void useOrg(Long orgId) {
    var auth = new UsernamePasswordAuthenticationToken(
        String.valueOf(orgId), "N/A", List.of(new SimpleGrantedAuthority("ROLE_FINANCE_EMPLOYEE")));
    auth.setDetails(Map.of("orgId", orgId, "username", "reminder-tester"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private void saveDepositStandard(String amount) {
    DepositStandardRequest request = new DepositStandardRequest();
    request.setStandardName("提醒测试押金");
    request.setAmount(new BigDecimal(amount));
    request.setEffectiveFrom(LocalDate.now().minusDays(1));
    depositService.saveStandard(request, 900L);
  }

  private DepositTransactionRequest depositTxn(Long elderId, String amount) {
    DepositTransactionRequest request = new DepositTransactionRequest();
    request.setElderId(elderId);
    request.setAmount(new BigDecimal(amount));
    return request;
  }

  private ElderProfile insertElder(Long orgId, String fullName) {
    ElderProfile elder = new ElderProfile();
    elder.setTenantId(orgId);
    elder.setOrgId(orgId);
    elder.setFullName(fullName);
    elder.setStatus(1);
    elderMapper.insert(elder);
    return elder;
  }

  private Room insertRoom(Long orgId, String roomNo) {
    Room room = new Room();
    room.setTenantId(orgId);
    room.setOrgId(orgId);
    room.setBuilding("提醒测试楼");
    room.setFloorNo("1F");
    room.setRoomNo(roomNo);
    room.setCapacity(2);
    room.setStatus(1);
    roomMapper.insert(room);
    return room;
  }

  private void insertBill(Long orgId, String billMonth, String totalAmount) {
    BillMonthly bill = new BillMonthly();
    bill.setOrgId(orgId);
    bill.setElderId(1L);
    bill.setBillMonth(billMonth);
    bill.setTotalAmount(new BigDecimal(totalAmount));
    bill.setPaidAmount(BigDecimal.ZERO);
    bill.setOutstandingAmount(new BigDecimal(totalAmount));
    billMonthlyMapper.insert(bill);
  }
}
