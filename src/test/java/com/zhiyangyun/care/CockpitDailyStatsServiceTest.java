package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.mapper.OrgMapper;
import com.zhiyangyun.care.bill.entity.BillItem;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillItemMapper;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.cockpit.entity.StatsDailyCare;
import com.zhiyangyun.care.cockpit.entity.StatsDailyFinance;
import com.zhiyangyun.care.cockpit.entity.StatsDailyOperation;
import com.zhiyangyun.care.cockpit.mapper.StatsDailyCareMapper;
import com.zhiyangyun.care.cockpit.mapper.StatsDailyFinanceMapper;
import com.zhiyangyun.care.cockpit.mapper.StatsDailyOperationMapper;
import com.zhiyangyun.care.cockpit.service.impl.CockpitDailyStatsServiceImpl;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.model.TaskStatus;
import com.zhiyangyun.care.schedule.entity.StaffSchedule;
import com.zhiyangyun.care.schedule.mapper.StaffScheduleMapper;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.mapper.SmartAlertMapper;
import com.zhiyangyun.care.survey.entity.SurveySubmission;
import com.zhiyangyun.care.survey.mapper.SurveySubmissionMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * 经营驾驶舱 BI 每日预聚合服务测试：
 * 验证从业务表口径计算出的运营/财务/护理三张每日汇总数值正确。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CockpitDailyStatsServiceTest {

  @Mock
  private StatsDailyOperationMapper operationMapper;
  @Mock
  private StatsDailyFinanceMapper financeMapper;
  @Mock
  private StatsDailyCareMapper careMapper;
  @Mock
  private BedMapper bedMapper;
  @Mock
  private ElderMapper elderMapper;
  @Mock
  private BillMonthlyMapper billMonthlyMapper;
  @Mock
  private BillItemMapper billItemMapper;
  @Mock
  private PaymentRecordMapper paymentRecordMapper;
  @Mock
  private CareTaskDailyMapper careTaskDailyMapper;
  @Mock
  private StaffScheduleMapper staffScheduleMapper;
  @Mock
  private SmartAlertMapper smartAlertMapper;
  @Mock
  private SurveySubmissionMapper surveySubmissionMapper;
  @Mock
  private OrgMapper orgMapper;

  private CockpitDailyStatsServiceImpl service;

  private final LocalDate statDate = LocalDate.of(2026, 7, 4);

  @BeforeEach
  void setUp() {
    service = new CockpitDailyStatsServiceImpl(
        operationMapper, financeMapper, careMapper,
        bedMapper, elderMapper, billMonthlyMapper, billItemMapper, paymentRecordMapper,
        careTaskDailyMapper, staffScheduleMapper, smartAlertMapper, surveySubmissionMapper,
        orgMapper, new ObjectMapper());

    // 床位：4 张，2 张占用
    Bed occupied1 = new Bed();
    occupied1.setElderId(101L);
    Bed occupied2 = new Bed();
    occupied2.setElderId(102L);
    when(bedMapper.selectList(any())).thenReturn(List.of(occupied1, occupied2, new Bed(), new Bed()));

    // 在住长者：2 人（一级/二级），入住 10/20 天
    ElderProfile elder1 = new ElderProfile();
    elder1.setCareLevel("一级");
    elder1.setAdmissionDate(statDate.minusDays(10));
    ElderProfile elder2 = new ElderProfile();
    elder2.setCareLevel("二级");
    elder2.setAdmissionDate(statDate.minusDays(20));
    when(elderMapper.selectList(any())).thenReturn(List.of(elder1, elder2));
    // selectCount 依次为：当日入住 1、当日退住 1
    when(elderMapper.selectCount(any())).thenReturn(1L, 1L);

    // 账单：应收 1000、已回款 600、欠费 400
    BillMonthly bill = new BillMonthly();
    bill.setId(9001L);
    bill.setTotalAmount(new BigDecimal("1000.00"));
    bill.setPaidAmount(new BigDecimal("600.00"));
    bill.setOutstandingAmount(new BigDecimal("400.00"));
    when(billMonthlyMapper.selectList(any())).thenReturn(List.of(bill));

    // 当日回款流水 200
    PaymentRecord payment = new PaymentRecord();
    payment.setAmount(new BigDecimal("200.00"));
    when(paymentRecordMapper.selectList(any())).thenReturn(List.of(payment));

    // 费用科目：床位费 800 + 餐费 200
    BillItem bedFee = new BillItem();
    bedFee.setItemType("BED_FEE");
    bedFee.setAmount(new BigDecimal("800.00"));
    BillItem mealFee = new BillItem();
    mealFee.setItemType("MEAL_FEE");
    mealFee.setAmount(new BigDecimal("200.00"));
    when(billItemMapper.selectList(any())).thenReturn(List.of(bedFee, mealFee));

    // 护理工单：4 单完成 3 单
    CareTaskDaily done1 = task(TaskStatus.DONE.name());
    CareTaskDaily done2 = task(TaskStatus.DONE.name());
    CareTaskDaily done3 = task(TaskStatus.DONE.name());
    CareTaskDaily pending = task(TaskStatus.PENDING.name());
    when(careTaskDailyMapper.selectList(any())).thenReturn(List.of(done1, done2, done3, pending));

    // 排班：2 名员工各 8 小时
    StaffSchedule shift1 = shift(201L, statDate.atTime(8, 0), statDate.atTime(16, 0));
    StaffSchedule shift2 = shift(202L, statDate.atTime(16, 0), statDate.plusDays(1).atStartOfDay());
    when(staffScheduleMapper.selectList(any())).thenReturn(List.of(shift1, shift2));

    // 告警：2 条，1 条已处理未升级（达标），1 条 OPEN
    SmartAlert resolved = new SmartAlert();
    resolved.setStatus("RESOLVED");
    resolved.setEscalationCount(0);
    SmartAlert open = new SmartAlert();
    open.setStatus("OPEN");
    when(smartAlertMapper.selectList(any())).thenReturn(List.of(resolved, open));

    // 满意度问卷：90 分 + 80 分
    SurveySubmission survey1 = new SurveySubmission();
    survey1.setScoreEffective(90);
    SurveySubmission survey2 = new SurveySubmission();
    survey2.setScoreEffective(80);
    when(surveySubmissionMapper.selectList(any())).thenReturn(List.of(survey1, survey2));

    // 无历史汇总行 -> 走 insert
    when(operationMapper.selectOne(any())).thenReturn(null);
    when(financeMapper.selectOne(any())).thenReturn(null);
    when(careMapper.selectOne(any())).thenReturn(null);
  }

  @Test
  void aggregate_should_compute_operation_finance_and_care_daily_stats() {
    service.aggregate(1L, statDate);

    ArgumentCaptor<StatsDailyOperation> opCaptor = ArgumentCaptor.forClass(StatsDailyOperation.class);
    verify(operationMapper).insert(opCaptor.capture());
    StatsDailyOperation op = opCaptor.getValue();
    assertEquals(1L, op.getOrgId());
    assertEquals(statDate, op.getStatDate());
    assertEquals(4, op.getTotalBeds());
    assertEquals(2, op.getOccupiedBeds());
    assertEquals(new BigDecimal("50.00"), op.getOccupancyRate());
    assertEquals(2, op.getResidentCount());
    assertEquals(1, op.getAdmissions());
    assertEquals(1, op.getDischarges());
    // 床位周转率 = (1+1)/4*100
    assertEquals(new BigDecimal("50.00"), op.getBedTurnoverRate());
    // 平均在住时长 = (10+20)/2
    assertEquals(new BigDecimal("15.0"), op.getAvgStayDays());
    assertTrue(op.getCareLevelJson().contains("一级"));
    assertTrue(op.getCareLevelJson().contains("二级"));

    ArgumentCaptor<StatsDailyFinance> finCaptor = ArgumentCaptor.forClass(StatsDailyFinance.class);
    verify(financeMapper).insert(finCaptor.capture());
    StatsDailyFinance fin = finCaptor.getValue();
    assertEquals(new BigDecimal("1000.00"), fin.getRevenueAmount());
    assertEquals(new BigDecimal("600.00"), fin.getPaidAmount());
    assertEquals(new BigDecimal("400.00"), fin.getOutstandingAmount());
    assertEquals(new BigDecimal("200.00"), fin.getPaidDailyAmount());
    // 回款率 = 600/1000*100
    assertEquals(new BigDecimal("60.00"), fin.getCollectionRate());
    assertTrue(fin.getCostStructureJson().contains("BED_FEE"));
    assertTrue(fin.getCostStructureJson().contains("MEAL_FEE"));

    ArgumentCaptor<StatsDailyCare> careCaptor = ArgumentCaptor.forClass(StatsDailyCare.class);
    verify(careMapper).insert(careCaptor.capture());
    StatsDailyCare care = careCaptor.getValue();
    assertEquals(4, care.getCareTaskTotal());
    assertEquals(3, care.getCareTaskDone());
    // 完成率 = 3/4*100
    assertEquals(new BigDecimal("75.00"), care.getCareTaskCompletionRate());
    assertEquals(2, care.getScheduledStaffCount());
    assertEquals(new BigDecimal("16.0"), care.getScheduledHours());
    assertEquals(new BigDecimal("8.0"), care.getHoursPerStaff());
    assertEquals(2, care.getAlertTotal());
    assertEquals(1, care.getAlertOnTime());
    // 告警响应达标率 = 1/2*100
    assertEquals(new BigDecimal("50.00"), care.getAlertOnTimeRate());
    // 满意度 = (90+80)/2
    assertEquals(new BigDecimal("85.0"), care.getSatisfactionScore());
    assertEquals(2, care.getSatisfactionCount());
  }

  private static CareTaskDaily task(String status) {
    CareTaskDaily task = new CareTaskDaily();
    task.setStatus(status);
    return task;
  }

  private static StaffSchedule shift(Long staffId, LocalDateTime start, LocalDateTime end) {
    StaffSchedule schedule = new StaffSchedule();
    schedule.setStaffId(staffId);
    schedule.setStartTime(start);
    schedule.setEndTime(end);
    return schedule;
  }
}
