package com.zhiyangyun.care.life.model;

import java.util.Map;
import java.util.Set;

public final class DiningConstants {
  private DiningConstants() {}

  public static final String MSG_INVALID_DATE_RANGE = "开始日期不能晚于结束日期";
  public static final String MSG_INVALID_ENABLE_STATUS = "状态仅支持 ENABLED 或 DISABLED";
  public static final String MSG_INVALID_DELIVERY_STATUS = "状态仅支持 PENDING、DELIVERED、FAILED";
  public static final String MSG_ORDER_NOT_FOUND_OR_FORBIDDEN = "点餐单不存在或无权限";
  public static final String MSG_PREP_ZONE_NOT_FOUND_OR_FORBIDDEN = "备餐分区不存在或无权限";
  public static final String MSG_DELIVERY_AREA_NOT_FOUND_OR_FORBIDDEN = "送餐区域不存在或无权限";
  public static final String MSG_OVERRIDE_ALREADY_REVIEWED = "该申请已处理，不能重复审批";
  public static final String MSG_INVALID_OVERRIDE_REVIEW_STATUS = "reviewStatus 仅支持 APPROVED 或 REJECTED";

  public static final String MEAL_TYPE_BREAKFAST = "BREAKFAST";
  public static final String MEAL_TYPE_LUNCH = "LUNCH";
  public static final String MEAL_TYPE_DINNER = "DINNER";
  public static final String MEAL_TYPE_SNACK = "SNACK";
  public static final String MEAL_TYPE_UNKNOWN = "UNKNOWN";

  public static final Set<String> ENABLE_DISABLE_STATUS_SET = Set.of("ENABLED", "DISABLED");
  public static final String STATUS_ENABLED = "ENABLED";
  public static final String STATUS_DISABLED = "DISABLED";

  public static final String ORDER_STATUS_CREATED = "CREATED";
  public static final String ORDER_STATUS_PREPARING = "PREPARING";
  public static final String ORDER_STATUS_DELIVERING = "DELIVERING";
  public static final String ORDER_STATUS_DELIVERED = "DELIVERED";
  public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
  public static final Set<String> ORDER_STATUS_SET = Set.of(
      ORDER_STATUS_CREATED,
      ORDER_STATUS_PREPARING,
      ORDER_STATUS_DELIVERING,
      ORDER_STATUS_DELIVERED,
      ORDER_STATUS_CANCELLED);
  public static final Map<String, Set<String>> ORDER_STATUS_TRANSITIONS = Map.of(
      ORDER_STATUS_CREATED, Set.of(ORDER_STATUS_PREPARING, ORDER_STATUS_CANCELLED),
      ORDER_STATUS_PREPARING, Set.of(ORDER_STATUS_DELIVERING, ORDER_STATUS_CANCELLED),
      ORDER_STATUS_DELIVERING, Set.of(ORDER_STATUS_DELIVERED, ORDER_STATUS_CANCELLED),
      ORDER_STATUS_DELIVERED, Set.of(),
      ORDER_STATUS_CANCELLED, Set.of());

  public static final String DELIVERY_STATUS_PENDING = "PENDING";
  public static final String DELIVERY_STATUS_DELIVERED = "DELIVERED";
  public static final String DELIVERY_STATUS_FAILED = "FAILED";
  public static final Set<String> DELIVERY_STATUS_SET = Set.of(
      DELIVERY_STATUS_PENDING,
      DELIVERY_STATUS_DELIVERED,
      DELIVERY_STATUS_FAILED);

  public static final String OVERRIDE_STATUS_PENDING = "PENDING";
  public static final String OVERRIDE_STATUS_APPROVED = "APPROVED";
  public static final String OVERRIDE_STATUS_REJECTED = "REJECTED";
}
