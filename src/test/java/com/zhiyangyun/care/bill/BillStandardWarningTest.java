package com.zhiyangyun.care.bill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zhiyangyun.care.bill.service.impl.BillServiceImpl;
import com.zhiyangyun.care.store.entity.ElderProfile;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BillStandardWarningTest {

  private static ElderProfile elder(String careLevel) {
    ElderProfile elder = new ElderProfile();
    elder.setOrgId(1L);
    elder.setCareLevel(careLevel);
    return elder;
  }

  @Test
  void lists_missing_bed_fee_and_unpriced_care_levels() {
    List<String> warnings = BillServiceImpl.missingStandardWarnings(
        List.of(elder("一级"), elder("二级"), elder("二级"), elder(null)), "2026-11",
        orgId -> BigDecimal.ZERO,
        orgId -> Map.of("一级", BigDecimal.valueOf(1200)));

    assertEquals(List.of(
        "床位费标准未配置（2026-11），床位费按 0 计",
        "护理等级「二级」未配置收费标准，2 位长者护理费按 0 计",
        "护理等级「（未设置护理等级）」未配置收费标准，1 位长者护理费按 0 计"), warnings);
  }

  @Test
  void no_warning_when_standards_complete() {
    List<String> warnings = BillServiceImpl.missingStandardWarnings(
        List.of(elder("一级")), "2026-11",
        orgId -> BigDecimal.valueOf(2000),
        orgId -> Map.of("一级", BigDecimal.valueOf(1200)));
    assertEquals(List.of(), warnings);
  }
}
