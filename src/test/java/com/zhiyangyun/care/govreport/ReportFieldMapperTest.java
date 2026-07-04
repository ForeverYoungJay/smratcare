package com.zhiyangyun.care.govreport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.govreport.adapter.ReportFieldMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 上报字段映射单测。 */
class ReportFieldMapperTest {

  private Map<String, Object> record() {
    Map<String, Object> r = new LinkedHashMap<>();
    r.put("insuredNo", "IN123");
    r.put("settleMonth", "202606");
    r.put("fundPay", 10500L);
    r.put("selfPay", 9500L);
    r.put("internalNote", "不应上报");
    return r;
  }

  private Map<String, String> mapping() {
    Map<String, String> m = new LinkedHashMap<>();
    m.put("insuredNo", "ylzh");
    m.put("settleMonth", "jsny");
    m.put("fundPay", "tczf");
    m.put("selfPay", "grzf");
    return m;
  }

  /** 命中字段改名，未映射字段被剔除。 */
  @Test
  void mapsAndDropsUnmapped() {
    Map<String, Object> out = ReportFieldMapper.apply(record(), mapping());
    assertEquals("IN123", out.get("ylzh"));
    assertEquals("202606", out.get("jsny"));
    assertEquals(10500L, out.get("tczf"));
    assertEquals(9500L, out.get("grzf"));
    assertFalse(out.containsKey("internalNote"));
    assertFalse(out.containsKey("insuredNo"));
    assertEquals(4, out.size());
  }

  /** 本地缺失的映射字段跳过，不产生 null 键。 */
  @Test
  void skipsMissingLocalFields() {
    Map<String, Object> partial = new LinkedHashMap<>();
    partial.put("insuredNo", "IN9");
    Map<String, Object> out = ReportFieldMapper.apply(partial, mapping());
    assertEquals(1, out.size());
    assertTrue(out.containsKey("ylzh"));
  }

  /** 空映射时原样返回。 */
  @Test
  void emptyMappingReturnsCopy() {
    Map<String, Object> out = ReportFieldMapper.apply(record(), Map.of());
    assertEquals(5, out.size());
    assertEquals("IN123", out.get("insuredNo"));
  }

  /** 批量映射。 */
  @Test
  void applyAllBatch() {
    List<Map<String, Object>> out = ReportFieldMapper.applyAll(List.of(record(), record()), mapping());
    assertEquals(2, out.size());
    assertEquals("IN123", out.get(0).get("ylzh"));
  }
}
