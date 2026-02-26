package com.zhiyangyun.care.baseconfig.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum BaseConfigGroup {
  ELDER_CATEGORY("老人类别"),
  MARKETING_CUSTOMER_TAG("客户标签"),
  MARKETING_SOURCE_CHANNEL("来源渠道"),
  ASSESSMENT_TYPE("评估"),
  ADMISSION_BED_TYPE("床位类型"),
  ADMISSION_ROOM_TYPE("房间类型"),
  ADMISSION_AREA("区域设置"),
  ADMISSION_BUILDING("楼栋管理"),
  ACTIVITY_TYPE("活动"),
  COMMUNITY_REPAIR_CATEGORY("维修分类"),
  COMMUNITY_TASK_TYPE("任务类型设置"),
  DISCHARGE_FEE_CONFIG("退住费用设置"),
  FEE_TYPE("费用"),
  REFUND_REASON("退款原因"),
  TRIAL_STAY_PACKAGE("试住套餐"),
  SYSTEM_ORG_INTRO("机构介绍"),
  SYSTEM_ORG_NEWS("机构动态"),
  SYSTEM_LIFE_ENTERTAINMENT("生活娱乐"),
  SYSTEM_APP_VERSION("APP版本管理"),
  SYSTEM_DICTIONARY("系统字典"),
  SYSTEM_MESSAGE("留言管理");

  private final String label;

  BaseConfigGroup(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public static Set<String> codes() {
    return Arrays.stream(values()).map(Enum::name).collect(Collectors.toSet());
  }

  public static List<Map<String, String>> options() {
    return Arrays.stream(values())
        .map(item -> Map.of("code", item.name(), "label", item.getLabel()))
        .toList();
  }

  public static Optional<BaseConfigGroup> fromCode(String code) {
    if (code == null || code.isBlank()) {
      return Optional.empty();
    }
    return Arrays.stream(values()).filter(item -> item.name().equals(code.trim())).findFirst();
  }
}
