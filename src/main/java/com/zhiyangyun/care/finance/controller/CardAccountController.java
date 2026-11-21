package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.entity.CardAccount;
import com.zhiyangyun.care.finance.entity.ElderAccount;
import com.zhiyangyun.care.finance.entity.ElderAccountLog;
import com.zhiyangyun.care.finance.mapper.CardAccountMapper;
import com.zhiyangyun.care.finance.mapper.ElderAccountLogMapper;
import com.zhiyangyun.care.finance.mapper.ElderAccountMapper;
import com.zhiyangyun.care.finance.model.CardAccountRequest;
import com.zhiyangyun.care.finance.model.CardAccountResponse;
import com.zhiyangyun.care.finance.model.CardTradeLogResponse;
import com.zhiyangyun.care.finance.model.CardTradeRequest;
import com.zhiyangyun.care.finance.support.CardLifecycle;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/card/account")
// 卡务（开卡、挂失、补卡、注销）由行政办理；充值、消费涉及资金，仅财务可操作
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','HR_EMPLOYEE','HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class CardAccountController {
  private static final String FUNDS_ROLES = "hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";

  private final CardAccountMapper cardAccountMapper;
  private final ElderMapper elderMapper;
  private final ElderAccountMapper elderAccountMapper;
  private final ElderAccountLogMapper elderAccountLogMapper;

  public CardAccountController(
      CardAccountMapper cardAccountMapper,
      ElderMapper elderMapper,
      ElderAccountMapper elderAccountMapper,
      ElderAccountLogMapper elderAccountLogMapper) {
    this.cardAccountMapper = cardAccountMapper;
    this.elderMapper = elderMapper;
    this.elderAccountMapper = elderAccountMapper;
    this.elderAccountLogMapper = elderAccountLogMapper;
  }

  @GetMapping("/page")
  public Result<IPage<CardAccountResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(CardAccount.class)
        .eq(CardAccount::getIsDeleted, 0)
        .eq(orgId != null, CardAccount::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), CardAccount::getStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      List<Long> elderIds = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
              .eq(ElderProfile::getIsDeleted, 0)
              .eq(orgId != null, ElderProfile::getOrgId, orgId)
              .like(ElderProfile::getFullName, keyword))
          .stream()
          .map(ElderProfile::getId)
          .toList();
      if (elderIds.isEmpty()) {
        wrapper.like(CardAccount::getCardNo, keyword);
      } else {
        wrapper.and(w -> w.like(CardAccount::getCardNo, keyword)
            .or()
            .in(CardAccount::getElderId, elderIds));
      }
    }
    wrapper.orderByDesc(CardAccount::getUpdateTime);
    IPage<CardAccount> page = cardAccountMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return Result.ok(toCardResponsePage(page));
  }

  @PostMapping
  @Transactional
  public Result<CardAccount> create(@Valid @RequestBody CardAccountRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long inUse = cardAccountMapper.selectCount(Wrappers.lambdaQuery(CardAccount.class)
        .eq(CardAccount::getIsDeleted, 0)
        .eq(orgId != null, CardAccount::getOrgId, orgId)
        .eq(CardAccount::getElderId, request.getElderId())
        .notIn(CardAccount::getStatus, CardLifecycle.CLOSED));
    if (inUse != null && inUse > 0) {
      throw new IllegalArgumentException("该老人已有在用的一卡通，挂失后请走补卡");
    }
    CardAccount account = new CardAccount();
    account.setTenantId(orgId);
    account.setOrgId(orgId);
    account.setElderId(request.getElderId());
    account.setCardNo(request.getCardNo() == null || request.getCardNo().isBlank() ? genCardNo() : request.getCardNo());
    // 新卡只能是“已申领待激活”或直接激活，其余状态走卡务操作
    account.setStatus("PENDING".equals(request.getStatus()) ? "PENDING" : "ACTIVE");
    account.setLossFlag(0);
    account.setOpenTime(LocalDateTime.now());
    account.setRemark(request.getRemark());
    account.setCreatedBy(AuthContext.getStaffId());
    cardAccountMapper.insert(account);
    getOrCreateElderAccount(orgId, request.getElderId(), AuthContext.getStaffId());
    return Result.ok(account);
  }

  @PutMapping("/{id}")
  public Result<CardAccount> update(@PathVariable Long id, @Valid @RequestBody CardAccountRequest request) {
    CardAccount account = cardAccountMapper.selectById(id);
    if (account == null) {
      return Result.ok(null);
    }
    // 状态只能通过卡务操作（/lifecycle）变更，编辑只改卡号和备注
    account.setCardNo(request.getCardNo() == null || request.getCardNo().isBlank() ? account.getCardNo() : request.getCardNo());
    account.setRemark(request.getRemark());
    cardAccountMapper.updateById(account);
    return Result.ok(account);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    CardAccount account = cardAccountMapper.selectById(id);
    if (account != null) {
      account.setIsDeleted(1);
      cardAccountMapper.updateById(account);
    }
    return Result.ok(null);
  }

  public record LifecycleRequest(String action, String reason, String newCardNo) {}

  /** 卡务操作：激活、挂失、解挂、冻结、解冻、补卡（旧卡作废并关联新卡）、注销；返回操作后的卡（补卡返回新卡） */
  @PostMapping("/{id}/lifecycle")
  @Transactional
  public Result<CardAccount> lifecycle(@PathVariable Long id, @RequestBody LifecycleRequest request) {
    Long orgId = AuthContext.getOrgId();
    CardAccount card = cardAccountMapper.selectById(id);
    if (card == null || !Integer.valueOf(0).equals(card.getIsDeleted())
        || (orgId != null && !orgId.equals(card.getOrgId()))) {
      throw new IllegalArgumentException("卡不存在");
    }
    String action = request.action() == null ? "" : request.action().trim().toUpperCase();
    String next = CardLifecycle.next(card, action);
    if (("REPORT_LOSS".equals(action) || "CANCEL".equals(action)) && (request.reason() == null || request.reason().isBlank())) {
      throw new IllegalArgumentException("请填写挂失或注销原因");
    }
    LocalDateTime now = LocalDateTime.now();
    card.setStatus(next);
    card.setLossFlag("LOST".equals(next) ? 1 : 0);
    card.setStatusReason(request.reason());
    card.setStatusChangedAt(now);
    cardAccountMapper.updateById(card);
    if (!"REISSUE".equals(action)) {
      return Result.ok(card);
    }
    CardAccount replacement = new CardAccount();
    replacement.setTenantId(card.getTenantId());
    replacement.setOrgId(card.getOrgId());
    replacement.setElderId(card.getElderId());
    replacement.setCardNo(request.newCardNo() == null || request.newCardNo().isBlank() ? genCardNo() : request.newCardNo().trim());
    replacement.setStatus("PENDING");
    replacement.setLossFlag(0);
    replacement.setOpenTime(now);
    replacement.setPrevCardId(card.getId());
    replacement.setStatusReason("补卡，原卡 " + card.getCardNo());
    replacement.setStatusChangedAt(now);
    replacement.setCreatedBy(AuthContext.getStaffId());
    cardAccountMapper.insert(replacement);
    return Result.ok(replacement);
  }

  @PostMapping("/recharge")
  @PreAuthorize(FUNDS_ROLES)
  @Transactional
  public Result<ElderAccountLog> recharge(@Valid @RequestBody CardTradeRequest request) {
    return Result.ok(applyTrade(request, "CARD_RECHARGE", "CREDIT"));
  }

  @PostMapping("/consume")
  @PreAuthorize(FUNDS_ROLES)
  @Transactional
  public Result<ElderAccountLog> consume(@Valid @RequestBody CardTradeRequest request) {
    return Result.ok(applyTrade(request, "CARD_CONSUME", "DEBIT"));
  }

  @GetMapping("/recharge/page")
  public Result<IPage<CardTradeLogResponse>> rechargePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    return Result.ok(pageTradeLogs(pageNo, pageSize, "CARD_RECHARGE", keyword));
  }

  @GetMapping("/consume/page")
  public Result<IPage<CardTradeLogResponse>> consumePage(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    return Result.ok(pageTradeLogs(pageNo, pageSize, "CARD_CONSUME", keyword));
  }

  private IPage<CardTradeLogResponse> pageTradeLogs(long pageNo, long pageSize, String sourceType, String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(ElderAccountLog.class)
        .eq(ElderAccountLog::getIsDeleted, 0)
        .eq(orgId != null, ElderAccountLog::getOrgId, orgId)
        .eq(ElderAccountLog::getSourceType, sourceType);
    if (keyword != null && !keyword.isBlank()) {
      List<Long> elderIdsByName = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
              .eq(ElderProfile::getIsDeleted, 0)
              .eq(orgId != null, ElderProfile::getOrgId, orgId)
              .like(ElderProfile::getFullName, keyword))
          .stream()
          .map(ElderProfile::getId)
          .toList();
      List<Long> elderIdsByCardNo = cardAccountMapper.selectList(Wrappers.lambdaQuery(CardAccount.class)
              .eq(CardAccount::getIsDeleted, 0)
              .eq(orgId != null, CardAccount::getOrgId, orgId)
              .like(CardAccount::getCardNo, keyword))
          .stream()
          .map(CardAccount::getElderId)
          .filter(Objects::nonNull)
          .toList();
      List<Long> elderIds = java.util.stream.Stream.concat(elderIdsByName.stream(), elderIdsByCardNo.stream())
          .distinct()
          .toList();
      if (elderIds.isEmpty()) {
        return new Page<>(pageNo, pageSize, 0);
      }
      wrapper.in(ElderAccountLog::getElderId, elderIds);
    }
    wrapper.orderByDesc(ElderAccountLog::getCreateTime);
    IPage<ElderAccountLog> page = elderAccountLogMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return toTradeResponsePage(page);
  }

  private ElderAccountLog applyTrade(CardTradeRequest request, String sourceType, String direction) {
    if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    CardAccount cardAccount = cardAccountMapper.selectById(request.getCardAccountId());
    if (cardAccount == null || cardAccount.getIsDeleted() != null && cardAccount.getIsDeleted() == 1) {
      throw new IllegalArgumentException("card account not found");
    }
    if (!"ACTIVE".equalsIgnoreCase(cardAccount.getStatus()) || (cardAccount.getLossFlag() != null && cardAccount.getLossFlag() == 1)) {
      throw new IllegalArgumentException("card account unavailable");
    }
    ElderAccount account = getOrCreateElderAccount(cardAccount.getOrgId(), cardAccount.getElderId(), AuthContext.getStaffId());
    BigDecimal balance = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
    if ("DEBIT".equals(direction)) {
      BigDecimal credit = account.getCreditLimit() == null ? BigDecimal.ZERO : account.getCreditLimit();
      if (balance.add(credit).compareTo(request.getAmount()) < 0) {
        throw new IllegalArgumentException("余额不足");
      }
      balance = balance.subtract(request.getAmount());
    } else {
      balance = balance.add(request.getAmount());
      cardAccount.setLastRechargeTime(LocalDateTime.now());
      cardAccountMapper.updateById(cardAccount);
    }
    account.setBalance(balance);
    elderAccountMapper.updateById(account);

    ElderAccountLog log = new ElderAccountLog();
    log.setTenantId(account.getTenantId());
    log.setOrgId(account.getOrgId());
    log.setElderId(account.getElderId());
    log.setAccountId(account.getId());
    log.setAmount(request.getAmount());
    log.setBalanceAfter(balance);
    log.setDirection(direction);
    log.setSourceType(sourceType);
    log.setSourceId(cardAccount.getId());
    log.setRemark(request.getRemark());
    log.setCreatedBy(AuthContext.getStaffId());
    elderAccountLogMapper.insert(log);
    return log;
  }

  private ElderAccount getOrCreateElderAccount(Long orgId, Long elderId, Long operatorId) {
    ElderAccount account = elderAccountMapper.selectOne(Wrappers.lambdaQuery(ElderAccount.class)
        .eq(ElderAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderAccount::getOrgId, orgId)
        .eq(ElderAccount::getElderId, elderId));
    if (account == null) {
      account = new ElderAccount();
      account.setTenantId(orgId);
      account.setOrgId(orgId);
      account.setElderId(elderId);
      account.setBalance(BigDecimal.ZERO);
      account.setCreditLimit(BigDecimal.ZERO);
      account.setWarnThreshold(BigDecimal.ZERO);
      account.setStatus(1);
      account.setCreatedBy(operatorId);
      elderAccountMapper.insert(account);
    }
    return account;
  }

  private IPage<CardAccountResponse> toCardResponsePage(IPage<CardAccount> page) {
    List<Long> elderIds = page.getRecords().stream()
        .map(CardAccount::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, String> elderNameMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(elderIds).stream()
            .collect(Collectors.toMap(ElderProfile::getId, ElderProfile::getFullName, (a, b) -> a));
    Map<Long, ElderAccount> accountMap = elderIds.isEmpty()
        ? Map.of()
        : elderAccountMapper.selectList(Wrappers.lambdaQuery(ElderAccount.class)
            .eq(ElderAccount::getIsDeleted, 0)
            .in(ElderAccount::getElderId, elderIds)).stream()
            .collect(Collectors.toMap(ElderAccount::getElderId, a -> a, (a, b) -> a));

    IPage<CardAccountResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(item -> {
      CardAccountResponse r = new CardAccountResponse();
      r.setId(item.getId());
      r.setElderId(item.getElderId());
      r.setElderName(elderNameMap.get(item.getElderId()));
      r.setCardNo(item.getCardNo());
      r.setStatus(item.getStatus());
      r.setLossFlag(item.getLossFlag());
      r.setOpenTime(item.getOpenTime());
      r.setLastRechargeTime(item.getLastRechargeTime());
      r.setRemark(item.getRemark());
      r.setPrevCardId(item.getPrevCardId());
      r.setStatusReason(item.getStatusReason());
      r.setStatusChangedAt(item.getStatusChangedAt());
      r.setCreateTime(item.getCreateTime());
      r.setUpdateTime(item.getUpdateTime());
      ElderAccount elderAccount = accountMap.get(item.getElderId());
      if (elderAccount != null) {
        r.setBalance(elderAccount.getBalance());
        r.setCreditLimit(elderAccount.getCreditLimit());
      }
      return r;
    }).toList());
    return resp;
  }

  private IPage<CardTradeLogResponse> toTradeResponsePage(IPage<ElderAccountLog> page) {
    List<Long> elderIds = page.getRecords().stream()
        .map(ElderAccountLog::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, String> elderNameMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(elderIds).stream()
            .collect(Collectors.toMap(ElderProfile::getId, ElderProfile::getFullName, (a, b) -> a));
    Map<Long, CardAccount> cardMap = elderIds.isEmpty()
        ? Map.of()
        : cardAccountMapper.selectList(Wrappers.lambdaQuery(CardAccount.class)
            .eq(CardAccount::getIsDeleted, 0)
            .in(CardAccount::getElderId, elderIds)).stream()
            .collect(Collectors.toMap(CardAccount::getElderId, c -> c,
                (a, b) -> CardLifecycle.CLOSED.contains(a.getStatus()) ? b : a));

    IPage<CardTradeLogResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(log -> {
      CardTradeLogResponse r = new CardTradeLogResponse();
      r.setId(log.getId());
      CardAccount card = cardMap.get(log.getElderId());
      if (card != null) {
        r.setCardAccountId(card.getId());
        r.setCardNo(card.getCardNo());
      }
      r.setElderId(log.getElderId());
      r.setElderName(elderNameMap.get(log.getElderId()));
      r.setAmount(log.getAmount());
      r.setBalanceAfter(log.getBalanceAfter());
      r.setDirection(log.getDirection());
      r.setSourceType(log.getSourceType());
      r.setRemark(log.getRemark());
      r.setCreateTime(log.getCreateTime());
      return r;
    }).toList());
    return resp;
  }

  private String genCardNo() {
    return "C" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
  }
}
