package com.zhiyangyun.care.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.ai.model.SolverAssignment;
import com.zhiyangyun.care.ai.model.SolverConstraint;
import com.zhiyangyun.care.ai.model.SolverResult;
import com.zhiyangyun.care.ai.model.SolverShift;
import com.zhiyangyun.care.ai.model.SolverStaff;
import com.zhiyangyun.care.ai.service.AiScheduleSolver;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** 智能排班求解器单测：硬约束（请假/资质/夜班后休/连班/周工时）+ 软约束（工时均衡、夜班公平）。 */
class AiScheduleSolverTest {

  private final AiScheduleSolver solver = new AiScheduleSolver();

  private SolverStaff staff(long id, String name, String... roles) {
    SolverStaff s = new SolverStaff();
    s.setId(id);
    s.setName(name);
    s.setRoleCodes(List.of(roles));
    return s;
  }

  private SolverShift shift(String code, int startHour, int endHour, int required, boolean night) {
    SolverShift s = new SolverShift();
    s.setShiftCode(code);
    s.setStartTime(LocalTime.of(startHour, 0));
    s.setEndTime(LocalTime.of(endHour % 24, 0));
    s.setCrossDay(endHour >= 24 ? 1 : 0);
    s.setRequiredCount(required);
    s.setNight(night);
    return s;
  }

  private List<LocalDate> days(String from, int count) {
    List<LocalDate> result = new ArrayList<>();
    LocalDate start = LocalDate.parse(from);
    for (int i = 0; i < count; i++) {
      result.add(start.plusDays(i));
    }
    return result;
  }

  @Test
  void fillsAllSlotsWhenStaffSufficient() {
    var result = solver.solve(
        days("2026-07-06", 7),
        List.of(shift("DAY", 8, 16, 1, false)),
        List.of(staff(1, "甲"), staff(2, "乙"), staff(3, "丙")),
        Set.of(),
        new SolverConstraint());
    assertTrue(result.getUnfilledSlots().isEmpty());
    assertEquals(7, result.getAssignments().size());
  }

  @Test
  void onePersonAtMostOneShiftPerDay() {
    var result = solver.solve(
        days("2026-07-06", 3),
        List.of(shift("DAY", 8, 16, 1, false), shift("EVENING", 16, 22, 1, false)),
        List.of(staff(1, "甲"), staff(2, "乙"), staff(3, "丙")),
        Set.of(),
        new SolverConstraint());
    Map<String, Integer> perStaffDay = new HashMap<>();
    for (SolverAssignment a : result.getAssignments()) {
      perStaffDay.merge(a.getStaffId() + "@" + a.getDutyDate(), 1, Integer::sum);
    }
    assertTrue(perStaffDay.values().stream().allMatch(c -> c == 1));
  }

  @Test
  void respectsLeaveConflict() {
    LocalDate day = LocalDate.parse("2026-07-06");
    var result = solver.solve(
        List.of(day),
        List.of(shift("DAY", 8, 16, 2, false)),
        List.of(staff(1, "甲"), staff(2, "乙"), staff(3, "丙")),
        Set.of("1@" + day),
        new SolverConstraint());
    assertTrue(result.getAssignments().stream().noneMatch(a -> a.getStaffId() == 1L));
    assertEquals(2, result.getAssignments().size());
  }

  @Test
  void respectsQualificationRequirement() {
    SolverShift nurseShift = shift("NURSE_DAY", 8, 16, 1, false);
    nurseShift.setRequiredRoles(List.of("NURSING_EMPLOYEE"));
    var result = solver.solve(
        days("2026-07-06", 3),
        List.of(nurseShift),
        List.of(staff(1, "护士", "NURSING_EMPLOYEE"), staff(2, "行政", "HR_EMPLOYEE")),
        Set.of(),
        new SolverConstraint());
    assertFalse(result.getAssignments().isEmpty());
    assertTrue(result.getAssignments().stream().allMatch(a -> a.getStaffId() == 1L));
  }

  @Test
  void enforcesRestDayAfterNightShift() {
    // 只有一个人 + 每天一个夜班：夜班后必须休息，因此隔天才能再排
    var result = solver.solve(
        days("2026-07-06", 4),
        List.of(shift("NIGHT", 22, 30, 1, true)),
        List.of(staff(1, "甲")),
        Set.of(),
        new SolverConstraint());
    List<LocalDate> assignedDays = result.getAssignments().stream()
        .map(SolverAssignment::getDutyDate).sorted().toList();
    for (int i = 1; i < assignedDays.size(); i++) {
      assertTrue(assignedDays.get(i).toEpochDay() - assignedDays.get(i - 1).toEpochDay() >= 2,
          "夜班后未强制休息：" + assignedDays);
    }
    assertFalse(result.getUnfilledSlots().isEmpty());
  }

  @Test
  void enforcesMaxConsecutiveDays() {
    SolverConstraint cons = new SolverConstraint();
    cons.setMaxConsecutiveDays(3);
    cons.setMaxWeeklyHours(80);
    var result = solver.solve(
        days("2026-07-06", 7),
        List.of(shift("DAY", 8, 16, 1, false)),
        List.of(staff(1, "甲")),
        Set.of(),
        cons);
    List<LocalDate> assignedDays = result.getAssignments().stream()
        .map(SolverAssignment::getDutyDate).sorted().toList();
    int consecutive = 1;
    for (int i = 1; i < assignedDays.size(); i++) {
      if (assignedDays.get(i).toEpochDay() - assignedDays.get(i - 1).toEpochDay() == 1) {
        consecutive++;
        assertTrue(consecutive <= 3, "连班超限：" + assignedDays);
      } else {
        consecutive = 1;
      }
    }
  }

  @Test
  void enforcesMaxWeeklyHours() {
    SolverConstraint cons = new SolverConstraint();
    cons.setMaxWeeklyHours(24); // 8小时班最多3次/周
    cons.setMaxConsecutiveDays(7);
    var result = solver.solve(
        days("2026-07-06", 7), // 2026-07-06 是周一，7天同一 ISO 周
        List.of(shift("DAY", 8, 16, 1, false)),
        List.of(staff(1, "甲")),
        Set.of(),
        cons);
    double hours = result.getHoursByStaff().getOrDefault(1L, 0d);
    assertTrue(hours <= 24 + 1e-9, "周工时超限：" + hours);
    assertFalse(result.getUnfilledSlots().isEmpty());
  }

  @Test
  void balancesWorkloadAcrossStaff() {
    var result = solver.solve(
        days("2026-07-06", 6),
        List.of(shift("DAY", 8, 16, 1, false)),
        List.of(staff(1, "甲"), staff(2, "乙"), staff(3, "丙")),
        Set.of(),
        new SolverConstraint());
    // 6个班3个人，均衡时每人2个班（16小时）
    for (double hours : result.getHoursByStaff().values()) {
      assertEquals(16, hours, 1e-9);
    }
    assertEquals(0, result.getSoftScore(), 1e-9);
  }

  @Test
  void distributesNightShiftsFairly() {
    SolverConstraint cons = new SolverConstraint();
    cons.setNightRestEnabled(false);
    cons.setMaxWeeklyHours(80);
    var result = solver.solve(
        days("2026-07-06", 6),
        List.of(shift("NIGHT", 22, 30, 1, true)),
        List.of(staff(1, "甲"), staff(2, "乙"), staff(3, "丙")),
        Set.of(),
        cons);
    for (int nights : result.getNightsByStaff().values()) {
      assertEquals(2, nights);
    }
  }

  @Test
  void reportsUnfilledWhenNoFeasibleStaff() {
    LocalDate day = LocalDate.parse("2026-07-06");
    var result = solver.solve(
        List.of(day),
        List.of(shift("DAY", 8, 16, 2, false)),
        List.of(staff(1, "甲")),
        Set.of(),
        new SolverConstraint());
    assertEquals(1, result.getAssignments().size());
    assertEquals(1, result.getUnfilledSlots().size());
  }

  @Test
  void emptyInputReturnsEmptyResult() {
    SolverResult result = solver.solve(List.of(), List.of(), List.of(), Set.of(), null);
    assertTrue(result.getAssignments().isEmpty());
    assertTrue(result.getUnfilledSlots().isEmpty());
  }
}
