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
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
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
public class CardAccountController {
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
    CardAccount existing = cardAccountMapper.selectOne(Wrappers.lambdaQuery(CardAccount.class)
        .eq(CardAccount::getIsDeleted, 0)
        .eq(orgId != null, CardAccount::getOrgId, orgId)
        .eq(CardAccount::getElderId, request.getElderId()));
    if (existing != null) {
      throw new IllegalArgumentException("该老人已开通一卡通");
    }
    CardAccount account = new CardAccount();
    account.setTenantId(orgId);
    account.setOrgId(orgId);
    account.setElderId(request.getElderId());
    account.setCardNo(request.getCardNo() == null || request.getCardNo().isBlank() ? genCardNo() : request.getCardNo());
    account.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
    account.setLossFlag(request.getLossFlag() == null ? 0 : request.getLossFlag());
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
    account.setCardNo(request.getCardNo() == null || request.getCardNo().isBlank() ? account.getCardNo() : request.getCardNo());
    account.setStatus(request.getStatus() == null ? account.getStatus() : request.getStatus());
    account.setLossFlag(request.getLossFlag() == null ? account.getLossFlag() : request.getLossFlag());
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

  @PostMapping("/recharge")
  @Transactional
  public Result<ElderAccountLog> recharge(@Valid @RequestBody CardTradeRequest request) {
    return Result.ok(applyTrade(request, "CARD_RECHARGE", "CREDIT"));
  }

  @PostMapping("/consume")
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
            .collect(Collectors.toMap(CardAccount::getElderId, c -> c, (a, b) -> a));

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
