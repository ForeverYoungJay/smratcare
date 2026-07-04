package com.zhiyangyun.care.medorder.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 医嘱执行计划生成（纯逻辑）：按给药频次将某一天拆分为若干执行时点。
 *
 * <p>频次采用医疗常用缩写：QD 每日一次、BID 每日两次、TID 每日三次、QID 每日四次、
 * Q8H 每 8 小时、Q12H 每 12 小时、QOD 隔日一次（按天由上层控制）。PRN 必要时、ST 立即，
 * 不产生固定计划（返回空，由业务临时处理）。
 */
public final class MedicalOrderScheduleGenerator {
  private MedicalOrderScheduleGenerator() {}

  private static final Map<String, String[]> FREQ_TIMES = Map.of(
      "QD", new String[] {"08:00"},
      "QOD", new String[] {"08:00"},
      "BID", new String[] {"08:00", "16:00"},
      "TID", new String[] {"08:00", "12:00", "18:00"},
      "QID", new String[] {"08:00", "12:00", "16:00", "20:00"},
      "Q8H", new String[] {"06:00", "14:00", "22:00"},
      "Q12H", new String[] {"08:00", "20:00"}
  );

  /** 返回指定日期该频次的所有执行时点（无固定计划返回空列表）。 */
  public static List<LocalDateTime> timesForDate(String frequency, LocalDate date) {
    List<LocalDateTime> result = new ArrayList<>();
    if (frequency == null || date == null) {
      return result;
    }
    String[] times = FREQ_TIMES.get(frequency.trim().toUpperCase());
    if (times == null) {
      return result;
    }
    for (String t : times) {
      result.add(LocalDateTime.of(date, LocalTime.parse(t)));
    }
    return result;
  }
}
