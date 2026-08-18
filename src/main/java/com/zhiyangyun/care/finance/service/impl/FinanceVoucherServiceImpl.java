package com.zhiyangyun.care.finance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.entity.FinanceConsumerVoucher;
import com.zhiyangyun.care.finance.entity.FinanceConsumerVoucherUsage;
import com.zhiyangyun.care.finance.mapper.FinanceConsumerVoucherMapper;
import com.zhiyangyun.care.finance.mapper.FinanceConsumerVoucherUsageMapper;
import com.zhiyangyun.care.finance.model.FinanceVoucherIssueRequest;
import com.zhiyangyun.care.finance.model.FinanceVoucherUsageView;
import com.zhiyangyun.care.finance.model.FinanceVoucherView;
import com.zhiyangyun.care.finance.model.PaymentVoucherUse;
import com.zhiyangyun.care.finance.service.FinanceVoucherService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceVoucherServiceImpl implements FinanceVoucherService {
  private static final DateTimeFormatter NO_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final String STATUS_ACTIVE = "ACTIVE";
  private static final String STATUS_USED = "USED";
  private static final String STATUS_EXPIRED = "EXPIRED";
  private static final String STATUS_REVOKED = "REVOKED";

  private final FinanceConsumerVoucherMapper voucherMapper;
  private final FinanceConsumerVoucherUsageMapper usageMapper;
  private final BillMonthlyMapper billMonthlyMapper;
  private final ElderMapper elderMapper;

  public FinanceVoucherServiceImpl(FinanceConsumerVoucherMapper voucherMapper,
      FinanceConsumerVoucherUsageMapper usageMapper,
      BillMonthlyMapper billMonthlyMapper,
      ElderMapper elderMapper) {
    this.voucherMapper = voucherMapper;
    this.usageMapper = usageMapper;
    this.billMonthlyMapper = billMonthlyMapper;
    this.elderMapper = elderMapper;
  }

  @Override
  public IPage<FinanceVoucherView> page(long pageNo, long pageSize, Long elderId, String status, String keyword) {
    Long orgId = AuthContext.getOrgId();
    expireOverdueVouchers(orgId);
    var wrapper = Wrappers.lambdaQuery(FinanceConsumerVoucher.class)
        .eq(FinanceConsumerVoucher::getIsDeleted, 0)
        .eq(orgId != null, FinanceConsumerVoucher::getOrgId, orgId);
    if (elderId != null) {
      wrapper.eq(FinanceConsumerVoucher::getElderId, elderId);
    }
    String normalizedStatus = normalizeUpper(status);
    if (!normalizedStatus.isEmpty()) {
      wrapper.eq(FinanceConsumerVoucher::getStatus, normalizedStatus);
    }
    if (keyword != null && !keyword.isBlank()) {
      String trimmed = keyword.trim();
      wrapper.and(it -> it.like(FinanceConsumerVoucher::getVoucherNo, trimmed)
          .or()
          .like(FinanceConsumerVoucher::getVoucherName, trimmed));
    }
    wrapper.orderByDesc(FinanceConsumerVoucher::getId);
    IPage<FinanceConsumerVoucher> page = voucherMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);

    Map<Long, String> elderNames = loadElderNames(page.getRecords().stream()
        .map(FinanceConsumerVoucher::getElderId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet()));
    IPage<FinanceVoucherView> viewPage = new Page<>(pageNo, pageSize);
    viewPage.setTotal(page.getTotal());
    viewPage.setRecords(page.getRecords().stream()
        .map(item -> toView(item, elderNames.get(item.getElderId())))
        .toList());
    return viewPage;
  }

  @Override
  @Transactional
  public List<FinanceVoucherView> issue(FinanceVoucherIssueRequest request, Long operatorStaffId) {
    Long orgId = AuthContext.getOrgId();
    if (orgId == null) {
      throw new IllegalStateException("当前登录用户缺少机构信息，无法发放消费券");
    }
    BigDecimal faceAmount = scale(request.getFaceAmount());
    if (faceAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("券面额必须大于 0");
    }
    if (request.getValidFrom() != null && request.getValidTo() != null
        && request.getValidTo().isBefore(request.getValidFrom())) {
      throw new IllegalArgumentException("有效期结束日不能早于开始日");
    }
    List<Long> targets = request.getElderIds() == null || request.getElderIds().isEmpty()
        ? List.of()
        : request.getElderIds().stream().filter(Objects::nonNull).distinct().toList();
    if (!targets.isEmpty()) {
      Set<Long> existing = elderMapper.selectList(
              Wrappers.lambdaQuery(ElderProfile.class)
                  .eq(ElderProfile::getIsDeleted, 0)
                  .eq(ElderProfile::getOrgId, orgId)
                  .in(ElderProfile::getId, targets))
          .stream().map(ElderProfile::getId).collect(Collectors.toSet());
      List<Long> missing = targets.stream().filter(id -> !existing.contains(id)).toList();
      if (!missing.isEmpty()) {
        throw new IllegalArgumentException("以下长者不属于当前机构或不存在：" + missing);
      }
    }

    LocalDateTime now = LocalDateTime.now();
    List<Long> elderTargets = targets.isEmpty() ? List.of((Long) null) : targets;
    List<FinanceVoucherView> created = new ArrayList<>();
    for (Long elderId : elderTargets) {
      FinanceConsumerVoucher voucher = new FinanceConsumerVoucher();
      voucher.setTenantId(orgId);
      voucher.setOrgId(orgId);
      voucher.setElderId(elderId);
      voucher.setVoucherNo(generateVoucherNo());
      voucher.setVoucherName(request.getVoucherName().trim());
      voucher.setFaceAmount(faceAmount);
      voucher.setBalanceAmount(faceAmount);
      voucher.setMinBillAmount(scale(request.getMinBillAmount()));
      voucher.setAllowSplit(Boolean.FALSE.equals(request.getAllowSplit()) ? 0 : 1);
      voucher.setValidFrom(request.getValidFrom());
      voucher.setValidTo(request.getValidTo());
      voucher.setStatus(STATUS_ACTIVE);
      voucher.setSource(normalizeText(request.getSource()));
      voucher.setIssuedBy(operatorStaffId);
      voucher.setIssuedAt(now);
      voucher.setRemark(normalizeText(request.getRemark()));
      voucherMapper.insert(voucher);
      created.add(toView(voucher, elderId == null ? null : loadElderNames(Set.of(elderId)).get(elderId)));
    }
    return created;
  }

  @Override
  @Transactional
  public void revoke(Long voucherId, String reason, Long operatorStaffId) {
    FinanceConsumerVoucher voucher = findForUpdate(voucherId);
    if (voucher == null) {
      throw new IllegalArgumentException("消费券不存在");
    }
    ensureOrgAccess(voucher.getOrgId());
    if (STATUS_REVOKED.equals(voucher.getStatus())) {
      return;
    }
    if (scale(voucher.getBalanceAmount()).compareTo(scale(voucher.getFaceAmount())) < 0) {
      throw new IllegalStateException("该券已部分核销，不能作废，请先冲正相关收款");
    }
    voucher.setStatus(STATUS_REVOKED);
    voucher.setRevokedBy(operatorStaffId);
    voucher.setRevokedAt(LocalDateTime.now());
    voucher.setRevokeReason(normalizeText(reason));
    voucherMapper.updateById(voucher);
  }

  @Override
  public List<FinanceVoucherView> listUsable(Long elderId, BigDecimal billAmount, LocalDate onDate) {
    Long orgId = AuthContext.getOrgId();
    LocalDate targetDate = onDate == null ? LocalDate.now() : onDate;
    BigDecimal amount = scale(billAmount);
    var wrapper = Wrappers.lambdaQuery(FinanceConsumerVoucher.class)
        .eq(FinanceConsumerVoucher::getIsDeleted, 0)
        .eq(orgId != null, FinanceConsumerVoucher::getOrgId, orgId)
        .eq(FinanceConsumerVoucher::getStatus, STATUS_ACTIVE)
        .gt(FinanceConsumerVoucher::getBalanceAmount, BigDecimal.ZERO);
    if (elderId != null) {
      wrapper.and(it -> it.isNull(FinanceConsumerVoucher::getElderId)
          .or()
          .eq(FinanceConsumerVoucher::getElderId, elderId));
    } else {
      wrapper.isNull(FinanceConsumerVoucher::getElderId);
    }
    wrapper.orderByAsc(FinanceConsumerVoucher::getValidTo).orderByDesc(FinanceConsumerVoucher::getId);

    Map<Long, String> elderNames = elderId == null ? Map.of() : loadElderNames(Set.of(elderId));
    return voucherMapper.selectList(wrapper).stream()
        .filter(item -> withinValidity(item, targetDate))
        .filter(item -> amount.compareTo(BigDecimal.ZERO) <= 0
            || scale(item.getMinBillAmount()).compareTo(amount) <= 0)
        .map(item -> toView(item, elderNames.get(item.getElderId())))
        .toList();
  }

  @Override
  @Transactional
  public BigDecimal consume(
      List<PaymentVoucherUse> uses,
      Long orgId,
      Long elderId,
      BigDecimal billTotalAmount,
      Long billMonthlyId,
      Long paymentRecordId,
      Long operatorStaffId) {
    if (uses == null || uses.isEmpty()) {
      return BigDecimal.ZERO;
    }
    Set<Long> seen = new HashSet<>();
    BigDecimal total = BigDecimal.ZERO;
    LocalDate today = LocalDate.now();
    BigDecimal billAmount = scale(billTotalAmount);
    for (PaymentVoucherUse use : uses) {
      if (use == null || use.getVoucherId() == null) {
        continue;
      }
      if (!seen.add(use.getVoucherId())) {
        throw new IllegalArgumentException("同一张消费券不能在一次收款中重复使用");
      }
      BigDecimal amount = scale(use.getAmount());
      if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("消费券抵扣金额必须大于 0");
      }
      FinanceConsumerVoucher voucher = findForUpdate(use.getVoucherId());
      if (voucher == null) {
        throw new IllegalArgumentException("消费券不存在");
      }
      if (orgId != null && voucher.getOrgId() != null && !Objects.equals(orgId, voucher.getOrgId())) {
        throw new IllegalStateException("消费券不属于当前机构：" + voucher.getVoucherNo());
      }
      if (voucher.getElderId() != null && !Objects.equals(voucher.getElderId(), elderId)) {
        throw new IllegalStateException("消费券 " + voucher.getVoucherNo() + " 已绑定其他长者，不能用于本账单");
      }
      if (!STATUS_ACTIVE.equals(voucher.getStatus())) {
        throw new IllegalStateException("消费券 " + voucher.getVoucherNo() + " 当前状态为 "
            + statusText(voucher.getStatus()) + "，不可使用");
      }
      if (!withinValidity(voucher, today)) {
        throw new IllegalStateException("消费券 " + voucher.getVoucherNo() + " 不在有效期内");
      }
      BigDecimal balance = scale(voucher.getBalanceAmount());
      if (amount.compareTo(balance) > 0) {
        throw new IllegalStateException("消费券 " + voucher.getVoucherNo() + " 剩余额度为 " + balance
            + " 元，不足以抵扣 " + amount + " 元");
      }
      if (Integer.valueOf(0).equals(voucher.getAllowSplit()) && amount.compareTo(balance) < 0) {
        throw new IllegalStateException("消费券 " + voucher.getVoucherNo() + " 不允许拆分使用，需一次用完 "
            + balance + " 元");
      }
      BigDecimal threshold = scale(voucher.getMinBillAmount());
      if (threshold.compareTo(BigDecimal.ZERO) > 0 && billAmount.compareTo(threshold) < 0) {
        throw new IllegalStateException("消费券 " + voucher.getVoucherNo() + " 需账单应收满 " + threshold + " 元");
      }

      BigDecimal newBalance = balance.subtract(amount);
      voucher.setBalanceAmount(newBalance);
      if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
        voucher.setStatus(STATUS_USED);
      }
      voucherMapper.updateById(voucher);

      FinanceConsumerVoucherUsage usage = new FinanceConsumerVoucherUsage();
      usage.setTenantId(voucher.getOrgId());
      usage.setOrgId(voucher.getOrgId());
      usage.setVoucherId(voucher.getId());
      usage.setElderId(elderId);
      usage.setBillMonthlyId(billMonthlyId);
      usage.setPaymentRecordId(paymentRecordId);
      usage.setAmount(amount);
      usage.setDirection("USE");
      usage.setOperatorStaffId(operatorStaffId);
      usageMapper.insert(usage);

      total = total.add(amount);
    }
    return total;
  }

  @Override
  @Transactional
  public BigDecimal release(Long paymentRecordId, Long operatorStaffId, String remark) {
    if (paymentRecordId == null) {
      return BigDecimal.ZERO;
    }
    List<FinanceConsumerVoucherUsage> usages = usageMapper.selectList(
        Wrappers.lambdaQuery(FinanceConsumerVoucherUsage.class)
            .eq(FinanceConsumerVoucherUsage::getIsDeleted, 0)
            .eq(FinanceConsumerVoucherUsage::getPaymentRecordId, paymentRecordId));
    BigDecimal released = BigDecimal.ZERO;
    for (FinanceConsumerVoucherUsage usage : usages) {
      BigDecimal amount = scale(usage.getAmount());
      if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      BigDecimal signed = "RELEASE".equals(usage.getDirection()) ? amount.negate() : amount;
      released = released.add(signed);
    }
    if (released.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    // 逐张券退回：按该笔收款上的净核销额度归还
    Map<Long, BigDecimal> perVoucher = new java.util.LinkedHashMap<>();
    for (FinanceConsumerVoucherUsage usage : usages) {
      BigDecimal amount = scale(usage.getAmount());
      BigDecimal signed = "RELEASE".equals(usage.getDirection()) ? amount.negate() : amount;
      perVoucher.merge(usage.getVoucherId(), signed, BigDecimal::add);
    }
    for (Map.Entry<Long, BigDecimal> entry : perVoucher.entrySet()) {
      BigDecimal amount = scale(entry.getValue());
      if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      FinanceConsumerVoucher voucher = findForUpdate(entry.getKey());
      if (voucher == null) {
        continue;
      }
      BigDecimal restored = scale(voucher.getBalanceAmount()).add(amount);
      BigDecimal face = scale(voucher.getFaceAmount());
      voucher.setBalanceAmount(restored.compareTo(face) > 0 ? face : restored);
      if (STATUS_USED.equals(voucher.getStatus())
          && scale(voucher.getBalanceAmount()).compareTo(BigDecimal.ZERO) > 0) {
        voucher.setStatus(STATUS_ACTIVE);
      }
      voucherMapper.updateById(voucher);

      FinanceConsumerVoucherUsage back = new FinanceConsumerVoucherUsage();
      back.setTenantId(voucher.getOrgId());
      back.setOrgId(voucher.getOrgId());
      back.setVoucherId(voucher.getId());
      back.setElderId(voucher.getElderId());
      back.setPaymentRecordId(paymentRecordId);
      back.setAmount(amount);
      back.setDirection("RELEASE");
      back.setOperatorStaffId(operatorStaffId);
      back.setRemark(normalizeText(remark));
      usageMapper.insert(back);
    }
    return released;
  }

  @Override
  public List<FinanceVoucherUsageView> usageList(Long voucherId) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(FinanceConsumerVoucherUsage.class)
        .eq(FinanceConsumerVoucherUsage::getIsDeleted, 0)
        .eq(orgId != null, FinanceConsumerVoucherUsage::getOrgId, orgId);
    if (voucherId != null) {
      wrapper.eq(FinanceConsumerVoucherUsage::getVoucherId, voucherId);
    }
    wrapper.orderByDesc(FinanceConsumerVoucherUsage::getId);
    List<FinanceConsumerVoucherUsage> rows = usageMapper.selectList(wrapper);
    if (rows.isEmpty()) {
      return List.of();
    }
    Map<Long, FinanceConsumerVoucher> voucherMap = voucherMapper.selectList(
            Wrappers.lambdaQuery(FinanceConsumerVoucher.class)
                .in(FinanceConsumerVoucher::getId, rows.stream()
                    .map(FinanceConsumerVoucherUsage::getVoucherId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList()))
        .stream()
        .collect(Collectors.toMap(FinanceConsumerVoucher::getId, Function.identity(), (a, b) -> a));
    Map<Long, String> billMonths = loadBillMonths(rows.stream()
        .map(FinanceConsumerVoucherUsage::getBillMonthlyId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet()));
    Map<Long, String> elderNames = loadElderNames(rows.stream()
        .map(FinanceConsumerVoucherUsage::getElderId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet()));
    return rows.stream().map(row -> {
      FinanceVoucherUsageView view = new FinanceVoucherUsageView();
      view.setId(row.getId());
      view.setVoucherId(row.getVoucherId());
      FinanceConsumerVoucher voucher = voucherMap.get(row.getVoucherId());
      view.setVoucherNo(voucher == null ? null : voucher.getVoucherNo());
      view.setElderId(row.getElderId());
      view.setElderName(elderNames.get(row.getElderId()));
      view.setBillMonthlyId(row.getBillMonthlyId());
      view.setBillMonth(billMonths.get(row.getBillMonthlyId()));
      view.setPaymentRecordId(row.getPaymentRecordId());
      view.setAmount(scale(row.getAmount()));
      view.setDirection(row.getDirection());
      view.setDirectionText("RELEASE".equals(row.getDirection()) ? "额度退回" : "核销");
      view.setRemark(row.getRemark());
      view.setCreateTime(row.getCreateTime());
      return view;
    }).toList();
  }

  private void expireOverdueVouchers(Long orgId) {
    LocalDate today = LocalDate.now();
    List<FinanceConsumerVoucher> overdue = voucherMapper.selectList(
        Wrappers.lambdaQuery(FinanceConsumerVoucher.class)
            .eq(FinanceConsumerVoucher::getIsDeleted, 0)
            .eq(orgId != null, FinanceConsumerVoucher::getOrgId, orgId)
            .eq(FinanceConsumerVoucher::getStatus, STATUS_ACTIVE)
            .isNotNull(FinanceConsumerVoucher::getValidTo)
            .lt(FinanceConsumerVoucher::getValidTo, today));
    for (FinanceConsumerVoucher voucher : overdue) {
      voucher.setStatus(STATUS_EXPIRED);
      voucherMapper.updateById(voucher);
    }
  }

  private FinanceConsumerVoucher findForUpdate(Long voucherId) {
    if (voucherId == null) {
      return null;
    }
    return voucherMapper.selectOne(
        Wrappers.lambdaQuery(FinanceConsumerVoucher.class)
            .eq(FinanceConsumerVoucher::getId, voucherId)
            .eq(FinanceConsumerVoucher::getIsDeleted, 0)
            .last("LIMIT 1 FOR UPDATE"));
  }

  private boolean withinValidity(FinanceConsumerVoucher voucher, LocalDate date) {
    if (voucher.getValidFrom() != null && date.isBefore(voucher.getValidFrom())) {
      return false;
    }
    return voucher.getValidTo() == null || !date.isAfter(voucher.getValidTo());
  }

  private FinanceVoucherView toView(FinanceConsumerVoucher voucher, String elderName) {
    FinanceVoucherView view = new FinanceVoucherView();
    view.setId(voucher.getId());
    view.setElderId(voucher.getElderId());
    view.setElderName(elderName);
    view.setVoucherNo(voucher.getVoucherNo());
    view.setVoucherName(voucher.getVoucherName());
    view.setFaceAmount(scale(voucher.getFaceAmount()));
    view.setBalanceAmount(scale(voucher.getBalanceAmount()));
    view.setMinBillAmount(scale(voucher.getMinBillAmount()));
    view.setAllowSplit(!Integer.valueOf(0).equals(voucher.getAllowSplit()));
    view.setValidFrom(voucher.getValidFrom());
    view.setValidTo(voucher.getValidTo());
    view.setStatus(voucher.getStatus());
    view.setStatusText(statusText(voucher.getStatus()));
    view.setSource(voucher.getSource());
    view.setIssuedAt(voucher.getIssuedAt());
    view.setRevokeReason(voucher.getRevokeReason());
    view.setRemark(voucher.getRemark());
    return view;
  }

  private Map<Long, String> loadElderNames(Set<Long> elderIds) {
    if (elderIds == null || elderIds.isEmpty()) {
      return Map.of();
    }
    return elderMapper.selectList(
            Wrappers.lambdaQuery(ElderProfile.class).in(ElderProfile::getId, elderIds))
        .stream()
        .filter(item -> item.getId() != null && item.getFullName() != null)
        .collect(Collectors.toMap(ElderProfile::getId, ElderProfile::getFullName, (a, b) -> a));
  }

  private Map<Long, String> loadBillMonths(Set<Long> billIds) {
    if (billIds == null || billIds.isEmpty()) {
      return Map.of();
    }
    return billMonthlyMapper.selectList(
            Wrappers.lambdaQuery(BillMonthly.class).in(BillMonthly::getId, billIds))
        .stream()
        .filter(item -> item.getId() != null && item.getBillMonth() != null)
        .collect(Collectors.toMap(BillMonthly::getId, BillMonthly::getBillMonth, (a, b) -> a));
  }

  private void ensureOrgAccess(Long targetOrgId) {
    if (targetOrgId == null) {
      return;
    }
    Long currentOrgId = AuthContext.getOrgId();
    if (currentOrgId == null || AuthContext.isAdmin()) {
      return;
    }
    if (!Objects.equals(currentOrgId, targetOrgId)) {
      throw new AccessDeniedException("No permission to access another organization");
    }
  }

  private String generateVoucherNo() {
    return "CV" + LocalDate.now().format(NO_DATE_FMT)
        + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
  }

  private static String statusText(String status) {
    if (status == null) {
      return "-";
    }
    return switch (status) {
      case STATUS_ACTIVE -> "可用";
      case STATUS_USED -> "已用完";
      case STATUS_EXPIRED -> "已过期";
      case STATUS_REVOKED -> "已作废";
      default -> status;
    };
  }

  private static String normalizeUpper(String value) {
    return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
  }

  private static String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private static BigDecimal scale(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(2, java.math.RoundingMode.HALF_UP);
  }
}
