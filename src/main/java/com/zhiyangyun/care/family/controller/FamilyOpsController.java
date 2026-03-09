package com.zhiyangyun.care.family.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.family.entity.FamilyNotifyLog;
import com.zhiyangyun.care.family.entity.FamilyRechargeOrder;
import com.zhiyangyun.care.family.entity.FamilySmsCodeLog;
import com.zhiyangyun.care.family.mapper.FamilyNotifyLogMapper;
import com.zhiyangyun.care.family.mapper.FamilyRechargeOrderMapper;
import com.zhiyangyun.care.family.mapper.FamilySmsCodeLogMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/family/ops")
public class FamilyOpsController {
  private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  private static final int TOP_ERROR_SAMPLE_LIMIT = 300;

  private final FamilySmsCodeLogMapper familySmsCodeLogMapper;
  private final FamilyNotifyLogMapper familyNotifyLogMapper;
  private final FamilyRechargeOrderMapper familyRechargeOrderMapper;

  public FamilyOpsController(FamilySmsCodeLogMapper familySmsCodeLogMapper,
      FamilyNotifyLogMapper familyNotifyLogMapper,
      FamilyRechargeOrderMapper familyRechargeOrderMapper) {
    this.familySmsCodeLogMapper = familySmsCodeLogMapper;
    this.familyNotifyLogMapper = familyNotifyLogMapper;
    this.familyRechargeOrderMapper = familyRechargeOrderMapper;
  }

  @PreAuthorize("hasAnyRole('FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/health")
  public Result<FamilyOpsHealthResponse> health(@RequestParam(defaultValue = "24") Integer hours) {
    int windowHours = normalizeWindowHours(hours);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime since = now.minusHours(windowHours);
    Long orgId = AuthContext.getOrgId();

    SmsStats sms = buildSmsStats(orgId, since);
    NotifyStats notify = buildNotifyStats(orgId, since);
    RechargeStats recharge = buildRechargeStats(orgId, since);

    List<TopErrorItem> topErrors = new ArrayList<>();
    topErrors.addAll(buildSmsTopErrors(orgId, since));
    topErrors.addAll(buildNotifyTopErrors(orgId, since));
    topErrors.addAll(buildRechargeTopErrors(orgId, since));
    topErrors = sortAndTakeTop(topErrors, 8);

    FamilyOpsHealthResponse response = new FamilyOpsHealthResponse();
    response.setCheckedAt(now.format(DATETIME_FMT));
    response.setWindowHours(windowHours);
    response.setLevel(resolveLevel(sms, notify, recharge));
    response.setSummary(buildSummary(sms, notify, recharge));
    response.setSms(sms);
    response.setNotify(notify);
    response.setRecharge(recharge);
    response.setTopErrors(topErrors);
    response.setSuggestions(buildSuggestions(sms, notify, recharge));
    return Result.ok(response);
  }

  private SmsStats buildSmsStats(Long orgId, LocalDateTime since) {
    SmsStats stats = new SmsStats();
    stats.setTotal(countSms(orgId, since, null));
    stats.setSent(countSms(orgId, since, List.of("SENT")));
    stats.setVerified(countSms(orgId, since, List.of("VERIFIED")));
    stats.setUsed(countSms(orgId, since, List.of("USED")));
    stats.setExpired(countSms(orgId, since, List.of("EXPIRED")));
    stats.setFailed(countSms(orgId, since, List.of("FAILED")));
    return stats;
  }

  private NotifyStats buildNotifyStats(Long orgId, LocalDateTime since) {
    NotifyStats stats = new NotifyStats();
    stats.setTotal(countNotify(orgId, since, null));
    stats.setSuccess(countNotify(orgId, since, List.of("SUCCESS")));
    stats.setFailed(countNotify(orgId, since, List.of("FAILED")));
    stats.setPending(countNotify(orgId, since, List.of("PENDING")));
    stats.setSkipped(countNotify(orgId, since, List.of("SKIPPED")));
    stats.setPendingRetry(countNotifyRetryPending(orgId, since));
    return stats;
  }

  private RechargeStats buildRechargeStats(Long orgId, LocalDateTime since) {
    RechargeStats stats = new RechargeStats();
    List<FamilyRechargeOrder> orders = familyRechargeOrderMapper.selectList(
        Wrappers.lambdaQuery(FamilyRechargeOrder.class)
            .eq(FamilyRechargeOrder::getIsDeleted, 0)
            .eq(orgId != null, FamilyRechargeOrder::getOrgId, orgId)
            .ge(FamilyRechargeOrder::getUpdateTime, since));
    stats.setTotal(orders.size());
    BigDecimal totalAmount = BigDecimal.ZERO;
    BigDecimal paidAmount = BigDecimal.ZERO;
    for (FamilyRechargeOrder order : orders) {
      String status = normalizeStatus(order.getStatus());
      if ("INIT".equals(status)) {
        stats.setInit(stats.getInit() + 1);
      } else if ("PREPAY_CREATED".equals(status)) {
        stats.setPrepayCreated(stats.getPrepayCreated() + 1);
      } else if ("PAID".equals(status)) {
        stats.setPaid(stats.getPaid() + 1);
      } else if ("FAILED".equals(status)) {
        stats.setFailed(stats.getFailed() + 1);
      } else if ("CLOSED".equals(status)) {
        stats.setClosed(stats.getClosed() + 1);
      }
      BigDecimal amount = order.getAmount() == null ? BigDecimal.ZERO : order.getAmount();
      totalAmount = totalAmount.add(amount);
      if ("PAID".equals(status)) {
        paidAmount = paidAmount.add(amount);
      }
    }
    stats.setTotalAmount(totalAmount);
    stats.setPaidAmount(paidAmount);
    return stats;
  }

  private long countSms(Long orgId, LocalDateTime since, List<String> statuses) {
    LambdaQueryWrapper<FamilySmsCodeLog> query = Wrappers.lambdaQuery(FamilySmsCodeLog.class)
        .eq(FamilySmsCodeLog::getIsDeleted, 0)
        .eq(orgId != null, FamilySmsCodeLog::getOrgId, orgId)
        .ge(FamilySmsCodeLog::getCreateTime, since);
    if (statuses != null && !statuses.isEmpty()) {
      query.in(FamilySmsCodeLog::getStatus, statuses);
    }
    Long count = familySmsCodeLogMapper.selectCount(query);
    return count == null ? 0 : count;
  }

  private long countNotify(Long orgId, LocalDateTime since, List<String> statuses) {
    LambdaQueryWrapper<FamilyNotifyLog> query = Wrappers.lambdaQuery(FamilyNotifyLog.class)
        .eq(FamilyNotifyLog::getIsDeleted, 0)
        .eq(orgId != null, FamilyNotifyLog::getOrgId, orgId)
        .ge(FamilyNotifyLog::getCreateTime, since);
    if (statuses != null && !statuses.isEmpty()) {
      query.in(FamilyNotifyLog::getStatus, statuses);
    }
    Long count = familyNotifyLogMapper.selectCount(query);
    return count == null ? 0 : count;
  }

  private long countNotifyRetryPending(Long orgId, LocalDateTime since) {
    Long count = familyNotifyLogMapper.selectCount(
        Wrappers.lambdaQuery(FamilyNotifyLog.class)
            .eq(FamilyNotifyLog::getIsDeleted, 0)
            .eq(orgId != null, FamilyNotifyLog::getOrgId, orgId)
            .eq(FamilyNotifyLog::getStatus, "FAILED")
            .isNotNull(FamilyNotifyLog::getNextRetryAt)
            .ge(FamilyNotifyLog::getUpdateTime, since));
    return count == null ? 0 : count;
  }

  private List<TopErrorItem> buildSmsTopErrors(Long orgId, LocalDateTime since) {
    List<FamilySmsCodeLog> rows = familySmsCodeLogMapper.selectList(
        Wrappers.lambdaQuery(FamilySmsCodeLog.class)
            .eq(FamilySmsCodeLog::getIsDeleted, 0)
            .eq(orgId != null, FamilySmsCodeLog::getOrgId, orgId)
            .eq(FamilySmsCodeLog::getStatus, "FAILED")
            .ge(FamilySmsCodeLog::getCreateTime, since)
            .orderByDesc(FamilySmsCodeLog::getUpdateTime)
            .last("LIMIT " + TOP_ERROR_SAMPLE_LIMIT));
    return buildTopErrors("SMS", rows.stream().map(FamilySmsCodeLog::getLastError).toList());
  }

  private List<TopErrorItem> buildNotifyTopErrors(Long orgId, LocalDateTime since) {
    List<FamilyNotifyLog> rows = familyNotifyLogMapper.selectList(
        Wrappers.lambdaQuery(FamilyNotifyLog.class)
            .eq(FamilyNotifyLog::getIsDeleted, 0)
            .eq(orgId != null, FamilyNotifyLog::getOrgId, orgId)
            .eq(FamilyNotifyLog::getStatus, "FAILED")
            .ge(FamilyNotifyLog::getCreateTime, since)
            .orderByDesc(FamilyNotifyLog::getUpdateTime)
            .last("LIMIT " + TOP_ERROR_SAMPLE_LIMIT));
    return buildTopErrors("WECHAT_NOTIFY", rows.stream().map(FamilyNotifyLog::getLastError).toList());
  }

  private List<TopErrorItem> buildRechargeTopErrors(Long orgId, LocalDateTime since) {
    List<FamilyRechargeOrder> rows = familyRechargeOrderMapper.selectList(
        Wrappers.lambdaQuery(FamilyRechargeOrder.class)
            .eq(FamilyRechargeOrder::getIsDeleted, 0)
            .eq(orgId != null, FamilyRechargeOrder::getOrgId, orgId)
            .in(FamilyRechargeOrder::getStatus, "FAILED", "CLOSED")
            .ge(FamilyRechargeOrder::getUpdateTime, since)
            .orderByDesc(FamilyRechargeOrder::getUpdateTime)
            .last("LIMIT " + TOP_ERROR_SAMPLE_LIMIT));
    return buildTopErrors("WECHAT_PAY", rows.stream()
        .map(item -> normalizeText(item.getRemark(), "订单状态：" + normalizeStatus(item.getStatus())))
        .toList());
  }

  private List<TopErrorItem> buildTopErrors(String source, List<String> reasons) {
    if (reasons == null || reasons.isEmpty()) {
      return List.of();
    }
    Map<String, Integer> countMap = new HashMap<>();
    for (String reason : reasons) {
      String normalizedReason = normalizeText(reason, "未知错误");
      countMap.merge(normalizedReason, 1, Integer::sum);
    }
    return countMap.entrySet().stream()
        .map(entry -> {
          TopErrorItem item = new TopErrorItem();
          item.setSource(source);
          item.setReason(entry.getKey());
          item.setCount(entry.getValue());
          return item;
        })
        .sorted(Comparator.comparingInt(TopErrorItem::getCount).reversed())
        .limit(5)
        .toList();
  }

  private List<TopErrorItem> sortAndTakeTop(List<TopErrorItem> all, int size) {
    if (all == null || all.isEmpty()) {
      return List.of();
    }
    return all.stream()
        .sorted(Comparator.comparingInt(TopErrorItem::getCount).reversed()
            .thenComparing(TopErrorItem::getSource, Comparator.nullsLast(String::compareTo)))
        .limit(Math.max(size, 1))
        .toList();
  }

  private String resolveLevel(SmsStats sms, NotifyStats notify, RechargeStats recharge) {
    if (recharge.getFailed() >= 3 || notify.getFailed() >= 5) {
      return "critical";
    }
    if (recharge.getFailed() > 0 || recharge.getClosed() > 0
        || notify.getFailed() > 0 || notify.getPendingRetry() > 0
        || sms.getFailed() > 0) {
      return "warning";
    }
    return "healthy";
  }

  private String buildSummary(SmsStats sms, NotifyStats notify, RechargeStats recharge) {
    if (recharge.getFailed() == 0 && recharge.getClosed() == 0
        && notify.getFailed() == 0 && notify.getPendingRetry() == 0
        && sms.getFailed() == 0) {
      return "家属端短信、微信通知与充值链路整体健康";
    }
    return "检测到异常链路，请优先处理失败短信、失败通知与支付异常订单";
  }

  private List<String> buildSuggestions(SmsStats sms, NotifyStats notify, RechargeStats recharge) {
    List<String> suggestions = new ArrayList<>();
    if (sms.getFailed() > 0) {
      suggestions.add("短信验证码失败 " + sms.getFailed() + " 条，建议核查短信通道配置与风控限频策略。");
    }
    if (notify.getFailed() > 0 || notify.getPendingRetry() > 0) {
      suggestions.add("微信通知失败/待重试共 " + (notify.getFailed() + notify.getPendingRetry())
          + " 条，建议检查模板ID、openId 绑定与 access_token 获取。");
    }
    if (recharge.getFailed() > 0 || recharge.getClosed() > 0) {
      suggestions.add("支付异常订单 " + (recharge.getFailed() + recharge.getClosed())
          + " 笔，建议复核回调签名、交易状态同步与异常待办处理。");
    }
    if (suggestions.isEmpty()) {
      suggestions.add("近一周期未发现异常，建议保持每日巡检与每周配置核查。");
    }
    return suggestions;
  }

  private int normalizeWindowHours(Integer hours) {
    int fallback = 24;
    if (hours == null) {
      return fallback;
    }
    return Math.min(Math.max(hours, 1), 168);
  }

  private String normalizeStatus(String status) {
    return normalizeText(status, "UNKNOWN").toUpperCase(Locale.ROOT);
  }

  private String normalizeText(String text, String fallback) {
    if (text == null || text.isBlank()) {
      return fallback;
    }
    return text.trim();
  }

  @Data
  public static class FamilyOpsHealthResponse {
    private String checkedAt;
    private Integer windowHours;
    private String level;
    private String summary;
    private SmsStats sms = new SmsStats();
    private NotifyStats notify = new NotifyStats();
    private RechargeStats recharge = new RechargeStats();
    private List<TopErrorItem> topErrors = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
  }

  @Data
  public static class SmsStats {
    private long total;
    private long sent;
    private long verified;
    private long used;
    private long expired;
    private long failed;
  }

  @Data
  public static class NotifyStats {
    private long total;
    private long success;
    private long failed;
    private long pending;
    private long skipped;
    private long pendingRetry;
  }

  @Data
  public static class RechargeStats {
    private int total;
    private int init;
    private int prepayCreated;
    private int paid;
    private int closed;
    private int failed;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal paidAmount = BigDecimal.ZERO;
  }

  @Data
  public static class TopErrorItem {
    private String source;
    private String reason;
    private int count;
  }
}
