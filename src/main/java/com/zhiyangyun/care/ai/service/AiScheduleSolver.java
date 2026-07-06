package com.zhiyangyun.care.ai.service;

import com.zhiyangyun.care.ai.model.SolverAssignment;
import com.zhiyangyun.care.ai.model.SolverConstraint;
import com.zhiyangyun.care.ai.model.SolverResult;
import com.zhiyangyun.care.ai.model.SolverShift;
import com.zhiyangyun.care.ai.model.SolverStaff;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * 智能排班求解器（纯 Java 启发式，无外部依赖）：贪心构造 + 局部交换改进。
 *
 * <p>硬约束：每人每天至多一个班次；请假日不排班；班次资质（角色）要求；
 * 每人每周最大工时；连续上班天数上限；夜班后强制休一天（可配置）。
 *
 * <p>软约束（目标函数，越小越好）：总工时方差（工时均衡）+ 夜班次数方差（夜班公平），各带权重。
 */
@Component
public class AiScheduleSolver {
  private static final int IMPROVE_MAX_PASSES = 4;

  /**
   * @param days 排班周期内的日期（升序）
   * @param shifts 班次槽位定义
   * @param staffList 候选员工
   * @param unavailable 不可排集合，key = staffId + "@" + date（如请假）
   * @param constraint 约束参数
   */
  public SolverResult solve(List<LocalDate> days, List<SolverShift> shifts, List<SolverStaff> staffList,
      Set<String> unavailable, SolverConstraint constraint) {
    SolverResult result = new SolverResult();
    if (days == null || days.isEmpty() || shifts == null || shifts.isEmpty()
        || staffList == null || staffList.isEmpty()) {
      return result;
    }
    SolverConstraint cons = constraint == null ? new SolverConstraint() : constraint;
    Set<String> blocked = unavailable == null ? Set.of() : unavailable;
    State state = new State(staffList);

    // 贪心构造：按日期顺序、白班在前夜班在后，为每个槽位挑当前"负担最轻"的可行员工
    List<SolverShift> orderedShifts = new ArrayList<>(shifts);
    orderedShifts.sort(Comparator.comparing(SolverShift::isNight)
        .thenComparing(s -> s.getStartTime() == null ? java.time.LocalTime.MIN : s.getStartTime()));
    for (LocalDate day : days) {
      for (SolverShift shift : orderedShifts) {
        int need = Math.max(1, shift.getRequiredCount());
        for (int i = 0; i < need; i++) {
          SolverStaff pick = pickStaff(day, shift, staffList, blocked, cons, state);
          if (pick == null) {
            result.getUnfilledSlots().add(day + " " + shift.getShiftCode() + " 缺1人");
            continue;
          }
          state.assign(pick, day, shift);
        }
      }
    }

    // 局部改进：把某槽位从"重负担"员工挪给"轻负担"员工，若目标函数下降则接受
    for (int pass = 0; pass < IMPROVE_MAX_PASSES; pass++) {
      boolean improved = false;
      double current = objective(state, cons);
      List<State.Slot> slots = new ArrayList<>(state.slots);
      for (State.Slot slot : slots) {
        for (SolverStaff candidate : staffList) {
          if (candidate.getId().equals(slot.staff.getId())) {
            continue;
          }
          if (!state.feasibleWithout(candidate, slot, blocked, cons)) {
            continue;
          }
          state.move(slot, candidate);
          double next = objective(state, cons);
          if (next < current - 1e-9) {
            current = next;
            improved = true;
          } else {
            state.moveBack(slot);
          }
        }
      }
      if (!improved) {
        break;
      }
    }

    for (State.Slot slot : state.slots) {
      result.getAssignments().add(new SolverAssignment(
          slot.staff.getId(), slot.staff.getName(), slot.day, slot.shift.getShiftCode(), slot.shift.isNight()));
    }
    result.getAssignments().sort(Comparator.comparing(SolverAssignment::getDutyDate)
        .thenComparing(SolverAssignment::getShiftCode)
        .thenComparing(SolverAssignment::getStaffId));
    for (SolverStaff staff : staffList) {
      result.getHoursByStaff().put(staff.getId(), state.hours.getOrDefault(staff.getId(), 0d));
      result.getNightsByStaff().put(staff.getId(), state.nights.getOrDefault(staff.getId(), 0));
    }
    result.setSoftScore(objective(state, cons));
    return result;
  }

  private SolverStaff pickStaff(LocalDate day, SolverShift shift, List<SolverStaff> staffList,
      Set<String> blocked, SolverConstraint cons, State state) {
    SolverStaff best = null;
    double bestHours = 0;
    int bestNights = 0;
    for (SolverStaff staff : staffList) {
      if (!state.feasible(staff, day, shift, blocked, cons)) {
        continue;
      }
      double hours = state.hours.getOrDefault(staff.getId(), 0d);
      int nights = state.nights.getOrDefault(staff.getId(), 0);
      boolean better;
      if (best == null) {
        better = true;
      } else if (shift.isNight() && nights != bestNights) {
        better = nights < bestNights;
      } else if (Math.abs(hours - bestHours) > 1e-9) {
        better = hours < bestHours;
      } else {
        better = staff.getId() < best.getId();
      }
      if (better) {
        best = staff;
        bestHours = hours;
        bestNights = nights;
      }
    }
    return best;
  }

  private double objective(State state, SolverConstraint cons) {
    List<Double> hourValues = new ArrayList<>();
    List<Double> nightValues = new ArrayList<>();
    for (SolverStaff staff : state.staffList) {
      hourValues.add(state.hours.getOrDefault(staff.getId(), 0d));
      nightValues.add((double) state.nights.getOrDefault(staff.getId(), 0));
    }
    return variance(hourValues) * cons.getWorkloadBalanceWeight()
        + variance(nightValues) * cons.getNightFairnessWeight();
  }

  private double variance(List<Double> values) {
    if (values.isEmpty()) {
      return 0;
    }
    double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    return values.stream().mapToDouble(v -> (v - mean) * (v - mean)).sum() / values.size();
  }

  /** 求解内部状态：分配槽位与每人工时/夜班/按日占用统计。 */
  private static final class State {
    final List<SolverStaff> staffList;
    final List<Slot> slots = new ArrayList<>();
    final Map<Long, Double> hours = new HashMap<>();
    final Map<Long, Integer> nights = new HashMap<>();
    /** staffId -> 已占用日期 -> 该日班次 */
    final Map<Long, Map<LocalDate, SolverShift>> byDay = new HashMap<>();

    State(List<SolverStaff> staffList) {
      this.staffList = staffList;
    }

    static final class Slot {
      SolverStaff staff;
      SolverStaff previous;
      final LocalDate day;
      final SolverShift shift;

      Slot(SolverStaff staff, LocalDate day, SolverShift shift) {
        this.staff = staff;
        this.day = day;
        this.shift = shift;
      }
    }

    void assign(SolverStaff staff, LocalDate day, SolverShift shift) {
      slots.add(new Slot(staff, day, shift));
      apply(staff, day, shift, 1);
    }

    void move(Slot slot, SolverStaff to) {
      slot.previous = slot.staff;
      apply(slot.staff, slot.day, slot.shift, -1);
      slot.staff = to;
      apply(to, slot.day, slot.shift, 1);
    }

    void moveBack(Slot slot) {
      if (slot.previous == null) {
        return;
      }
      apply(slot.staff, slot.day, slot.shift, -1);
      slot.staff = slot.previous;
      slot.previous = null;
      apply(slot.staff, slot.day, slot.shift, 1);
    }

    private void apply(SolverStaff staff, LocalDate day, SolverShift shift, int sign) {
      hours.merge(staff.getId(), shift.durationHours() * sign, Double::sum);
      if (shift.isNight()) {
        nights.merge(staff.getId(), sign, Integer::sum);
      }
      Map<LocalDate, SolverShift> dayMap = byDay.computeIfAbsent(staff.getId(), k -> new HashMap<>());
      if (sign > 0) {
        dayMap.put(day, shift);
      } else {
        dayMap.remove(day);
      }
    }

    boolean feasible(SolverStaff staff, LocalDate day, SolverShift shift,
        Set<String> blocked, SolverConstraint cons) {
      Map<LocalDate, SolverShift> dayMap = byDay.getOrDefault(staff.getId(), Map.of());
      // 每人每天至多一个班次
      if (dayMap.containsKey(day)) {
        return false;
      }
      // 请假冲突
      if (blocked.contains(staff.getId() + "@" + day)) {
        return false;
      }
      // 资质要求
      if (shift.getRequiredRoles() != null && !shift.getRequiredRoles().isEmpty()) {
        List<String> roles = staff.getRoleCodes();
        boolean qualified = roles != null && shift.getRequiredRoles().stream()
            .anyMatch(required -> roles.stream().anyMatch(r -> r.equalsIgnoreCase(required)));
        if (!qualified) {
          return false;
        }
      }
      // 夜班后强制休一天：昨天上了夜班则今天不可排；今天排夜班则明天不能已有班
      if (cons.isNightRestEnabled()) {
        SolverShift prev = dayMap.get(day.minusDays(1));
        if (prev != null && prev.isNight()) {
          return false;
        }
        if (shift.isNight() && dayMap.containsKey(day.plusDays(1))) {
          return false;
        }
      }
      // 连续上班天数上限
      int consecutive = 0;
      LocalDate cursor = day.minusDays(1);
      while (dayMap.containsKey(cursor)) {
        consecutive++;
        cursor = cursor.minusDays(1);
      }
      // 向后也要算（局部改进阶段可能往中间插）
      cursor = day.plusDays(1);
      while (dayMap.containsKey(cursor)) {
        consecutive++;
        cursor = cursor.plusDays(1);
      }
      if (consecutive + 1 > cons.getMaxConsecutiveDays()) {
        return false;
      }
      // 每周最大工时（ISO 周，周一为一周开始）
      double weekHours = 0;
      WeekFields weekFields = WeekFields.ISO;
      int week = day.get(weekFields.weekOfWeekBasedYear());
      int weekYear = day.get(weekFields.weekBasedYear());
      for (Map.Entry<LocalDate, SolverShift> entry : dayMap.entrySet()) {
        if (entry.getKey().get(weekFields.weekOfWeekBasedYear()) == week
            && entry.getKey().get(weekFields.weekBasedYear()) == weekYear) {
          weekHours += entry.getValue().durationHours();
        }
      }
      return weekHours + shift.durationHours() <= cons.getMaxWeeklyHours() + 1e-9;
    }

    /** 判断把 slot 挪给 candidate 是否可行（先临时摘除原占用再校验）。 */
    boolean feasibleWithout(SolverStaff candidate, Slot slot, Set<String> blocked, SolverConstraint cons) {
      apply(slot.staff, slot.day, slot.shift, -1);
      boolean ok = feasible(candidate, slot.day, slot.shift, blocked, cons);
      apply(slot.staff, slot.day, slot.shift, 1);
      return ok;
    }
  }
}
