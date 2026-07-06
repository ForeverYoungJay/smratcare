package com.zhiyangyun.care.family.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.FamilyUserMapper;
import com.zhiyangyun.care.family.entity.FamilyNotifyLog;
import com.zhiyangyun.care.family.entity.FamilyRechargeOrder;
import com.zhiyangyun.care.family.entity.FamilySmsCodeLog;
import com.zhiyangyun.care.family.mapper.FamilyNotifyLogMapper;
import com.zhiyangyun.care.family.mapper.FamilyRechargeOrderMapper;
import com.zhiyangyun.care.family.mapper.FamilySmsCodeLogMapper;
import com.zhiyangyun.care.family.model.FamilyNotifyCommand;
import com.zhiyangyun.care.family.service.FamilyWechatNotifyService;
import java.math.RoundingMode;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  private final ElderMapper elderMapper;
  private final FamilyUserMapper familyUserMapper;
  private final FamilyWechatNotifyService familyWechatNotifyService;

  public FamilyOpsController(FamilySmsCodeLogMapper familySmsCodeLogMapper,
      FamilyNotifyLogMapper familyNotifyLogMapper,
      FamilyRechargeOrderMapper familyRechargeOrderMapper,
      ElderMapper elderMapper,
      FamilyUserMapper familyUserMapper,
      FamilyWechatNotifyService familyWechatNotifyService) {
    this.familySmsCodeLogMapper = familySmsCodeLogMapper;
    this.familyNotifyLogMapper = familyNotifyLogMapper;
    this.familyRechargeOrderMapper = familyRechargeOrderMapper;
    this.elderMapper = elderMapper;
    this.familyUserMapper = familyUserMapper;
    this.familyWechatNotifyService = familyWechatNotifyService;
  }

  /**
   * 订阅消息推送链路端到端自测：为指定家属构造一条通知并真实投递，返回落库结果。
   * 便于上线前验证 access_token / openId 绑定 / 模板配置是否打通。
   */
  @PreAuthorize("hasAnyRole('FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/test-notify")
  public Result<TestNotifyResponse> testNotify(@RequestBody TestNotifyRequest request) {
    Long orgId = AuthContext.getOrgId();
    if (request == null || request.getFamilyUserId() == null) {
      throw new IllegalArgumentException("familyUserId 不能为空");
    }
    FamilyUser familyUser = familyUserMapper.selectById(request.getFamilyUserId());
    if (familyUser == null || Integer.valueOf(1).equals(familyUser.getIsDeleted())
        || (orgId != null && !orgId.equals(familyUser.getOrgId()))) {
      throw new IllegalArgumentException("家属账号不存在或不属于当前机构");
    }

    FamilyNotifyCommand command = new FamilyNotifyCommand();
    command.setOrgId(familyUser.getOrgId());
    command.setFamilyUserId(familyUser.getId());
    command.setElderId(request.getElderId());
    command.setEventType(normalizeText(request.getEventType(), "GENERAL"));
    command.setLevel(normalizeText(request.getLevel(), "normal"));
    command.setTitle(normalizeText(request.getTitle(), "推送链路自测"));
    command.setContent(normalizeText(request.getContent(), "这是一条家属端订阅消息推送链路的端到端测试通知。"));
    command.setPagePath(normalizeOptional(request.getPagePath()));
    familyWechatNotifyService.notifyFamily(command);

    FamilyNotifyLog latest = familyNotifyLogMapper.selectOne(
        Wrappers.lambdaQuery(FamilyNotifyLog.class)
            .eq(FamilyNotifyLog::getIsDeleted, 0)
            .eq(FamilyNotifyLog::getOrgId, familyUser.getOrgId())
            .eq(FamilyNotifyLog::getFamilyUserId, familyUser.getId())
            .orderByDesc(FamilyNotifyLog::getId)
            .last("LIMIT 1"));
    TestNotifyResponse response = new TestNotifyResponse();
    response.setFamilyUserId(familyUser.getId());
    response.setBoundOpenId(familyUser.getOpenId() != null && !familyUser.getOpenId().isBlank());
    if (latest != null) {
      response.setLogId(latest.getId());
      response.setStatus(latest.getStatus());
      response.setLastError(latest.getLastError());
      response.setTemplateId(latest.getTemplateId());
    }
    response.setSummary(buildTestNotifySummary(response));
    return Result.ok(response);
  }

  private String buildTestNotifySummary(TestNotifyResponse response) {
    if (!response.isBoundOpenId()) {
      return "该家属尚未绑定微信 openId，通知已跳过；请在小程序登录后自动绑定或引导授权。";
    }
    String status = normalizeStatus(response.getStatus());
    if ("SUCCESS".equals(status)) {
      return "推送链路打通：订阅消息已成功下发，请在微信客户端确认收到。";
    }
    if ("SKIPPED".equals(status)) {
      return "通知被跳过：" + normalizeText(response.getLastError(), "可能是家属关闭了该类型提醒。");
    }
    if ("FAILED".equals(status)) {
      return "推送失败：" + normalizeText(response.getLastError(), "请核查模板ID、access_token 与 openId。");
    }
    return "通知已入库，状态：" + status;
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

  @PreAuthorize("hasAnyRole('FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @GetMapping("/recharge-ledger")
  public Result<RechargeLedgerResponse> rechargeLedger(
      @RequestParam(defaultValue = "1") Integer pageNo,
      @RequestParam(defaultValue = "20") Integer pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String channel,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeOptional(status);
    String normalizedChannel = normalizeOptional(channel);
    String normalizedKeyword = normalizeOptional(keyword);

    List<Long> elderIds = null;
    List<Long> familyUserIds = null;
    if (normalizedKeyword != null) {
      elderIds = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
              .eq(orgId != null, ElderProfile::getOrgId, orgId)
              .eq(ElderProfile::getIsDeleted, 0)
              .like(ElderProfile::getFullName, normalizedKeyword))
          .stream()
          .map(ElderProfile::getId)
          .distinct()
          .toList();
      familyUserIds = familyUserMapper.selectList(Wrappers.lambdaQuery(FamilyUser.class)
              .eq(orgId != null, FamilyUser::getOrgId, orgId)
              .eq(FamilyUser::getIsDeleted, 0)
              .and(wrapper -> wrapper
                  .like(FamilyUser::getRealName, normalizedKeyword)
                  .or()
                  .like(FamilyUser::getPhone, normalizedKeyword)))
          .stream()
          .map(FamilyUser::getId)
          .distinct()
          .toList();
    }

    LambdaQueryWrapper<FamilyRechargeOrder> baseQuery = buildRechargeLedgerQuery(
        orgId,
        normalizedStatus,
        normalizedChannel,
        normalizedKeyword,
        elderIds,
        familyUserIds);
    List<FamilyRechargeOrder> filteredOrders = familyRechargeOrderMapper.selectList(
        baseQuery.orderByDesc(FamilyRechargeOrder::getCreateTime));
    int safePageNo = Math.max(pageNo == null ? 1 : pageNo, 1);
    int safePageSize = Math.max(pageSize == null ? 20 : pageSize, 1);
    int fromIndex = Math.min((safePageNo - 1) * safePageSize, filteredOrders.size());
    int toIndex = Math.min(fromIndex + safePageSize, filteredOrders.size());
    List<FamilyRechargeOrder> pageOrders = filteredOrders.subList(fromIndex, toIndex);

    List<Long> pageElderIds = pageOrders.stream().map(FamilyRechargeOrder::getElderId).filter(id -> id != null).distinct().toList();
    List<Long> pageFamilyIds = pageOrders.stream().map(FamilyRechargeOrder::getFamilyUserId).filter(id -> id != null).distinct().toList();
    Map<Long, ElderProfile> elderMap = pageElderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
                .eq(orgId != null, ElderProfile::getOrgId, orgId)
                .eq(ElderProfile::getIsDeleted, 0)
                .in(ElderProfile::getId, pageElderIds))
            .stream()
            .collect(java.util.stream.Collectors.toMap(ElderProfile::getId, elder -> elder, (a, b) -> a));
    Map<Long, FamilyUser> familyMap = pageFamilyIds.isEmpty()
        ? Map.of()
        : familyUserMapper.selectList(Wrappers.lambdaQuery(FamilyUser.class)
                .eq(orgId != null, FamilyUser::getOrgId, orgId)
                .eq(FamilyUser::getIsDeleted, 0)
                .in(FamilyUser::getId, pageFamilyIds))
            .stream()
            .collect(java.util.stream.Collectors.toMap(FamilyUser::getId, user -> user, (a, b) -> a));

    RechargeLedgerSummary summary = new RechargeLedgerSummary();
    summary.setTotalCount(filteredOrders.size());
    summary.setPendingCount(countRechargeByStatuses(filteredOrders, "INIT", "PREPAY_CREATED"));
    summary.setPaidCount(countRechargeByStatuses(filteredOrders, "PAID"));
    summary.setAbnormalCount(countRechargeByStatuses(filteredOrders, "FAILED", "CLOSED"));
    summary.setAutoPayEnabledCount((int) filteredOrders.stream()
        .filter(item -> containsAnyText(item.getRemark(), "自动扣费", "AUTO_PAY", "自动代扣"))
        .count());
    summary.setTotalAmount(sumRechargeAmount(filteredOrders));
    summary.setPaidAmount(sumRechargeAmount(filteredOrders, "PAID"));
    summary.setAbnormalAmount(sumRechargeAmount(filteredOrders, "FAILED", "CLOSED"));

    RechargeLedgerResponse response = new RechargeLedgerResponse();
    response.setPageNo(safePageNo);
    response.setPageSize(safePageSize);
    response.setTotal(filteredOrders.size());
    response.setSummary(summary);
    response.setList(pageOrders.stream().map(order -> toRechargeLedgerItem(order, elderMap, familyMap)).toList());
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

  private String defaultText(String text, String fallback) {
    return normalizeText(text, fallback);
  }

  private String normalizeOptional(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    return text.trim();
  }

  private LambdaQueryWrapper<FamilyRechargeOrder> buildRechargeLedgerQuery(
      Long orgId,
      String normalizedStatus,
      String normalizedChannel,
      String normalizedKeyword,
      List<Long> elderIds,
      List<Long> familyUserIds) {
    LambdaQueryWrapper<FamilyRechargeOrder> query = Wrappers.lambdaQuery(FamilyRechargeOrder.class)
        .eq(FamilyRechargeOrder::getIsDeleted, 0)
        .eq(orgId != null, FamilyRechargeOrder::getOrgId, orgId)
        .eq(normalizedStatus != null, FamilyRechargeOrder::getStatus, normalizedStatus)
        .eq(normalizedChannel != null, FamilyRechargeOrder::getChannel, normalizedChannel);
    if (normalizedKeyword != null) {
      boolean hasElders = elderIds != null && !elderIds.isEmpty();
      boolean hasFamilies = familyUserIds != null && !familyUserIds.isEmpty();
      query.and(wrapper -> {
        wrapper.like(FamilyRechargeOrder::getOutTradeNo, normalizedKeyword)
            .or()
            .like(FamilyRechargeOrder::getRemark, normalizedKeyword);
        if (hasElders) {
          wrapper.or().in(FamilyRechargeOrder::getElderId, elderIds);
        }
        if (hasFamilies) {
          wrapper.or().in(FamilyRechargeOrder::getFamilyUserId, familyUserIds);
        }
      });
    }
    return query;
  }

  private boolean containsAnyText(String text, String... needles) {
    if (text == null || text.isBlank() || needles == null || needles.length == 0) {
      return false;
    }
    String normalized = text.toUpperCase(Locale.ROOT);
    for (String needle : needles) {
      if (needle != null && !needle.isBlank() && normalized.contains(needle.toUpperCase(Locale.ROOT))) {
        return true;
      }
    }
    return false;
  }

  private int countRechargeByStatuses(List<FamilyRechargeOrder> orders, String... statuses) {
    if (orders == null || orders.isEmpty() || statuses == null || statuses.length == 0) {
      return 0;
    }
    List<String> statusList = java.util.Arrays.stream(statuses).map(this::normalizeStatus).toList();
    return (int) orders.stream()
        .filter(item -> statusList.contains(normalizeStatus(item.getStatus())))
        .count();
  }

  private BigDecimal sumRechargeAmount(List<FamilyRechargeOrder> orders, String... statuses) {
    if (orders == null || orders.isEmpty()) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    List<String> statusList = statuses == null || statuses.length == 0
        ? List.of()
        : java.util.Arrays.stream(statuses).map(this::normalizeStatus).toList();
    return orders.stream()
        .filter(item -> statusList.isEmpty() || statusList.contains(normalizeStatus(item.getStatus())))
        .map(item -> item.getAmount() == null ? BigDecimal.ZERO : item.getAmount())
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2, RoundingMode.HALF_UP);
  }

  private RechargeLedgerItem toRechargeLedgerItem(
      FamilyRechargeOrder order,
      Map<Long, ElderProfile> elderMap,
      Map<Long, FamilyUser> familyMap) {
    RechargeLedgerItem item = new RechargeLedgerItem();
    item.setId(order.getId());
    item.setOutTradeNo(order.getOutTradeNo());
    item.setFamilyUserId(order.getFamilyUserId());
    item.setFamilyName(defaultText(familyMap.get(order.getFamilyUserId()) == null ? null : familyMap.get(order.getFamilyUserId()).getRealName(), "未命名家属"));
    item.setFamilyPhone(familyMap.get(order.getFamilyUserId()) == null ? null : familyMap.get(order.getFamilyUserId()).getPhone());
    item.setElderId(order.getElderId());
    item.setElderName(defaultText(elderMap.get(order.getElderId()) == null ? null : elderMap.get(order.getElderId()).getFullName(), "未绑定老人"));
    item.setAmount(order.getAmount() == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : order.getAmount().setScale(2, RoundingMode.HALF_UP));
    item.setChannel(defaultText(order.getChannel(), "WECHAT_JSAPI"));
    item.setStatus(normalizeStatus(order.getStatus()));
    item.setStatusText(rechargeStatusText(order.getStatus()));
    item.setRemark(normalizeText(order.getRemark(), ""));
    item.setCreateTime(order.getCreateTime() == null ? null : order.getCreateTime().format(DATETIME_FMT));
    item.setPaidTime(order.getPaidAt() == null ? null : order.getPaidAt().format(DATETIME_FMT));
    item.setWxTransactionId(order.getWxTransactionId());
    return item;
  }

  private String rechargeStatusText(String status) {
    String normalized = normalizeStatus(status);
    if ("PAID".equals(normalized)) return "已支付";
    if ("PREPAY_CREATED".equals(normalized)) return "待支付";
    if ("CLOSED".equals(normalized)) return "已关闭";
    if ("FAILED".equals(normalized)) return "支付失败";
    return "待创建";
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

  @Data
  public static class RechargeLedgerResponse {
    private int pageNo;
    private int pageSize;
    private int total;
    private RechargeLedgerSummary summary = new RechargeLedgerSummary();
    private List<RechargeLedgerItem> list = new ArrayList<>();
  }

  @Data
  public static class RechargeLedgerSummary {
    private int totalCount;
    private int pendingCount;
    private int paidCount;
    private int abnormalCount;
    private int autoPayEnabledCount;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal paidAmount = BigDecimal.ZERO;
    private BigDecimal abnormalAmount = BigDecimal.ZERO;
  }

  @Data
  public static class TestNotifyRequest {
    private Long familyUserId;
    private Long elderId;
    private String eventType;
    private String level;
    private String title;
    private String content;
    private String pagePath;
  }

  @Data
  public static class TestNotifyResponse {
    private Long familyUserId;
    private boolean boundOpenId;
    private Long logId;
    private String status;
    private String templateId;
    private String lastError;
    private String summary;
  }

  @Data
  public static class RechargeLedgerItem {
    private Long id;
    private String outTradeNo;
    private Long familyUserId;
    private String familyName;
    private String familyPhone;
    private Long elderId;
    private String elderName;
    private BigDecimal amount;
    private String channel;
    private String status;
    private String statusText;
    private String remark;
    private String createTime;
    private String paidTime;
    private String wxTransactionId;
  }
}
