package com.zhiyangyun.care.medorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.medorder.service.MedicalOrderScheduleGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 医嘱执行计划生成单测。 */
class MedicalOrderScheduleGeneratorTest {

  private final LocalDate day = LocalDate.of(2026, 7, 1);

  @Test
  void qdOnce() {
    List<LocalDateTime> t = MedicalOrderScheduleGenerator.timesForDate("QD", day);
    assertEquals(1, t.size());
    assertEquals(LocalDateTime.of(day, java.time.LocalTime.of(8, 0)), t.get(0));
  }

  @Test
  void bidTwice() {
    assertEquals(2, MedicalOrderScheduleGenerator.timesForDate("BID", day).size());
  }

  @Test
  void tidThreeAtFixedTimes() {
    List<LocalDateTime> t = MedicalOrderScheduleGenerator.timesForDate("TID", day);
    assertEquals(3, t.size());
    assertEquals(8, t.get(0).getHour());
    assertEquals(12, t.get(1).getHour());
    assertEquals(18, t.get(2).getHour());
  }

  @Test
  void q8hEveryEightHours() {
    List<LocalDateTime> t = MedicalOrderScheduleGenerator.timesForDate("Q8H", day);
    assertEquals(3, t.size());
    assertEquals(6, t.get(0).getHour());
    assertEquals(14, t.get(1).getHour());
    assertEquals(22, t.get(2).getHour());
  }

  @Test
  void q12hTwice() {
    assertEquals(2, MedicalOrderScheduleGenerator.timesForDate("Q12H", day).size());
  }

  @Test
  void caseInsensitive() {
    assertEquals(3, MedicalOrderScheduleGenerator.timesForDate("tid", day).size());
  }

  @Test
  void prnAndUnknownEmpty() {
    assertTrue(MedicalOrderScheduleGenerator.timesForDate("PRN", day).isEmpty());
    assertTrue(MedicalOrderScheduleGenerator.timesForDate("ST", day).isEmpty());
    assertTrue(MedicalOrderScheduleGenerator.timesForDate("XYZ", day).isEmpty());
    assertTrue(MedicalOrderScheduleGenerator.timesForDate(null, day).isEmpty());
  }
}
