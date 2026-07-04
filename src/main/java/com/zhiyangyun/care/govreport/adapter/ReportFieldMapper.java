package com.zhiyangyun.care.govreport.adapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 上报字段映射（纯逻辑）：把本地记录字段按渠道配置的 {@code 本地字段 -> 上报字段} 映射改名，
 * 使同一份业务数据可适配不同地区/渠道的报文字段，映射改配置不改代码。
 */
public final class ReportFieldMapper {
  private ReportFieldMapper() {}

  /**
   * 对单条记录应用映射。仅保留映射表命中的字段并改名为上报字段；本地记录缺失的字段跳过。
   *
   * @param record 本地记录（本地字段 -> 值）
   * @param mapping 映射（本地字段 -> 上报字段），为空时原样返回
   */
  public static Map<String, Object> apply(Map<String, Object> record, Map<String, String> mapping) {
    if (record == null) {
      return new LinkedHashMap<>();
    }
    if (mapping == null || mapping.isEmpty()) {
      return new LinkedHashMap<>(record);
    }
    Map<String, Object> out = new LinkedHashMap<>();
    for (Map.Entry<String, String> m : mapping.entrySet()) {
      String localKey = m.getKey();
      String reportKey = m.getValue();
      if (reportKey == null || reportKey.isBlank()) {
        continue;
      }
      if (record.containsKey(localKey)) {
        out.put(reportKey, record.get(localKey));
      }
    }
    return out;
  }

  /** 对记录列表批量应用映射。 */
  public static List<Map<String, Object>> applyAll(List<Map<String, Object>> records,
      Map<String, String> mapping) {
    List<Map<String, Object>> out = new ArrayList<>();
    if (records == null) {
      return out;
    }
    for (Map<String, Object> record : records) {
      out.add(apply(record, mapping));
    }
    return out;
  }
}
