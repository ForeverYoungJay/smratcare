package com.zhiyangyun.care.finance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.entity.ElderAccount;
import com.zhiyangyun.care.finance.entity.ElderAccountLog;
import com.zhiyangyun.care.finance.mapper.ElderAccountLogMapper;
import com.zhiyangyun.care.finance.mapper.ElderAccountMapper;
import com.zhiyangyun.care.finance.model.ElderAccountAdjustRequest;
import com.zhiyangyun.care.finance.model.ElderAccountLogResponse;
import com.zhiyangyun.care.finance.model.ElderAccountResponse;
import com.zhiyangyun.care.finance.model.ElderAccountUpdateRequest;
import com.zhiyangyun.care.finance.service.ElderAccountService;
import com.zhiyangyun.care.store.entity.ElderPointsAccount;
import com.zhiyangyun.care.store.mapper.ElderPointsAccountMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElderAccountServiceImpl implements ElderAccountService {
  private final ElderAccountMapper accountMapper;
  private final ElderAccountLogMapper logMapper;
  private final ElderMapper elderMapper;
  private final ElderPointsAccountMapper pointsAccountMapper;

  public ElderAccountServiceImpl(ElderAccountMapper accountMapper,
      ElderAccountLogMapper logMapper,
      ElderMapper elderMapper,
      ElderPointsAccountMapper pointsAccountMapper) {
    this.accountMapper = accountMapper;
    this.logMapper = logMapper;
    this.elderMapper = elderMapper;
    this.pointsAccountMapper = pointsAccountMapper;
  }

  @Override
  @Transactional
  public ElderAccountResponse getOrCreate(Long orgId, Long elderId, Long operatorId) {
    ElderAccount account = accountMapper.selectOne(Wrappers.lambdaQuery(ElderAccount.class)
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
      accountMapper.insert(account);
    }
    List<Long> elderIds = List.of(elderId);
    return toAccountResponse(account,
        elderNameMap(elderIds).get(elderId),
        pointsAccountMap(elderIds).get(elderId));
  }

  @Override
  public IPage<ElderAccountResponse> page(Long orgId, long pageNo, long pageSize, String keyword) {
    List<Long> elderIds = null;
    if (keyword != null && !keyword.isBlank()) {
      elderIds = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
          .eq(ElderProfile::getIsDeleted, 0)
          .eq(orgId != null, ElderProfile::getOrgId, orgId)
          .like(ElderProfile::getFullName, keyword))
          .stream().map(ElderProfile::getId).toList();
      if (elderIds.isEmpty()) {
        return new Page<>(pageNo, pageSize, 0);
      }
    }

    var wrapper = Wrappers.lambdaQuery(ElderAccount.class)
        .eq(ElderAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderAccount::getOrgId, orgId);
    if (elderIds != null) {
      wrapper.in(ElderAccount::getElderId, elderIds);
    }
    wrapper.orderByDesc(ElderAccount::getUpdateTime);

    IPage<ElderAccount> page = accountMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> pageElderIds = page.getRecords().stream()
        .map(ElderAccount::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, String> elderNameMap = elderNameMap(pageElderIds);
    Map<Long, ElderPointsAccount> pointsMap = pointsAccountMap(pageElderIds);

    IPage<ElderAccountResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream()
        .map(acc -> toAccountResponse(acc, elderNameMap.get(acc.getElderId()), pointsMap.get(acc.getElderId())))
        .toList());
    return resp;
  }

  @Override
  public IPage<ElderAccountLogResponse> logPage(Long orgId, long pageNo, long pageSize,
      Long elderId, Long accountId, String keyword) {
    var wrapper = Wrappers.lambdaQuery(ElderAccountLog.class)
        .eq(ElderAccountLog::getIsDeleted, 0)
        .eq(orgId != null, ElderAccountLog::getOrgId, orgId);
    if (elderId != null) {
      wrapper.eq(ElderAccountLog::getElderId, elderId);
    }
    if (accountId != null) {
      wrapper.eq(ElderAccountLog::getAccountId, accountId);
    }
    if (keyword != null && !keyword.isBlank()) {
      List<Long> elderIds = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
          .eq(ElderProfile::getIsDeleted, 0)
          .eq(orgId != null, ElderProfile::getOrgId, orgId)
          .like(ElderProfile::getFullName, keyword))
          .stream().map(ElderProfile::getId).toList();
      if (elderIds.isEmpty()) {
        return new Page<>(pageNo, pageSize, 0);
      }
      wrapper.in(ElderAccountLog::getElderId, elderIds);
    }
    wrapper.orderByDesc(ElderAccountLog::getCreateTime);
    IPage<ElderAccountLog> page = logMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, String> elderNameMap = elderNameMap(page.getRecords().stream()
        .map(ElderAccountLog::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList());

    IPage<ElderAccountLogResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream()
        .map(log -> toLogResponse(log, elderNameMap.get(log.getElderId())))
        .toList());
    return resp;
  }

  @Override
  @Transactional
  public ElderAccountResponse adjust(Long orgId, Long operatorId, ElderAccountAdjustRequest request) {
    if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("amount must be positive");
    }
    Long elderId = request.getElderId();
    if (elderId == null && request.getElderName() != null && !request.getElderName().isBlank()) {
      elderId = resolveElderId(orgId, request.getElderName());
    }
    if (elderId == null) {
      throw new IllegalArgumentException("elderId or elderName required");
    }
    ElderAccount account = accountMapper.selectOne(Wrappers.lambdaQuery(ElderAccount.class)
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
      accountMapper.insert(account);
    }
    applyChange(account, request.getAmount(), request.getDirection(), "ADJUST", null,
        request.getRemark(), operatorId);
    List<Long> elderIds = List.of(account.getElderId());
    return toAccountResponse(account,
        elderNameMap(elderIds).get(account.getElderId()),
        pointsAccountMap(elderIds).get(account.getElderId()));
  }

  @Override
  @Transactional
  public ElderAccountResponse updateAccount(Long orgId, Long operatorId, ElderAccountUpdateRequest request) {
    Long elderId = request.getElderId();
    if (elderId == null && request.getElderName() != null && !request.getElderName().isBlank()) {
      elderId = resolveElderId(orgId, request.getElderName());
    }
    if (elderId == null) {
      throw new IllegalArgumentException("elderId or elderName required");
    }
    ElderAccount account = accountMapper.selectOne(Wrappers.lambdaQuery(ElderAccount.class)
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
      accountMapper.insert(account);
    }
    if (request.getCreditLimit() != null) {
      account.setCreditLimit(request.getCreditLimit());
    }
    if (request.getWarnThreshold() != null) {
      account.setWarnThreshold(request.getWarnThreshold());
    }
    if (request.getStatus() != null) {
      account.setStatus(request.getStatus());
    }
    if (request.getRemark() != null) {
      account.setRemark(request.getRemark());
    }
    accountMapper.updateById(account);
    List<Long> elderIds = List.of(account.getElderId());
    return toAccountResponse(account,
        elderNameMap(elderIds).get(account.getElderId()),
        pointsAccountMap(elderIds).get(account.getElderId()));
  }

  @Override
  @Transactional
  public void chargeForTask(Long orgId, Long elderId, Long taskDailyId, BigDecimal amount,
      String remark, Long operatorId) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      return;
    }
    ElderAccount account = accountMapper.selectOne(Wrappers.lambdaQuery(ElderAccount.class)
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
      accountMapper.insert(account);
    }
    applyChange(account, amount, "DEBIT", "TASK", taskDailyId, remark, operatorId);
  }

  @Override
  public List<ElderAccountResponse> warnings(Long orgId) {
    List<ElderAccount> accounts = accountMapper.selectList(Wrappers.lambdaQuery(ElderAccount.class)
        .eq(ElderAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderAccount::getOrgId, orgId)
        .eq(ElderAccount::getStatus, 1));
    List<ElderAccount> warningList = accounts.stream()
        .filter(acc -> acc.getWarnThreshold() != null
            && acc.getBalance() != null
            && acc.getBalance().compareTo(acc.getWarnThreshold()) <= 0)
        .toList();
    List<Long> warningElderIds = warningList.stream()
        .map(ElderAccount::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, String> elderNameMap = elderNameMap(warningElderIds);
    Map<Long, ElderPointsAccount> pointsMap = pointsAccountMap(warningElderIds);
    return warningList.stream()
        .map(acc -> toAccountResponse(acc, elderNameMap.get(acc.getElderId()), pointsMap.get(acc.getElderId())))
        .toList();
  }

  private void applyChange(ElderAccount account, BigDecimal amount, String direction,
      String sourceType, Long sourceId, String remark, Long operatorId) {
    if (direction == null || (!direction.equalsIgnoreCase("DEBIT") && !direction.equalsIgnoreCase("CREDIT"))) {
      throw new IllegalArgumentException("direction must be DEBIT or CREDIT");
    }
    BigDecimal balance = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
    BigDecimal delta = amount;
    if (direction.equalsIgnoreCase("DEBIT")) {
      balance = balance.subtract(delta);
    } else {
      balance = balance.add(delta);
    }
    account.setBalance(balance);
    accountMapper.updateById(account);

    ElderAccountLog log = new ElderAccountLog();
    log.setTenantId(account.getTenantId());
    log.setOrgId(account.getOrgId());
    log.setElderId(account.getElderId());
    log.setAccountId(account.getId());
    log.setAmount(amount);
    log.setBalanceAfter(balance);
    log.setDirection(direction.toUpperCase());
    log.setSourceType(sourceType);
    log.setSourceId(sourceId);
    log.setRemark(remark);
    log.setCreatedBy(operatorId);
    logMapper.insert(log);
  }

  private Map<Long, String> elderNameMap(List<Long> elderIds) {
    if (elderIds == null || elderIds.isEmpty()) {
      return Map.of();
    }
    return elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
            .in(ElderProfile::getId, elderIds))
        .stream()
        .collect(Collectors.toMap(ElderProfile::getId, ElderProfile::getFullName, (a, b) -> a));
  }

  private Long resolveElderId(Long orgId, String elderName) {
    if (elderName == null || elderName.isBlank()) {
      return null;
    }
    ElderProfile elder = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getFullName, elderName)
        .last("LIMIT 1"));
    return elder == null ? null : elder.getId();
  }

  private ElderAccountResponse toAccountResponse(ElderAccount account, String elderName,
      ElderPointsAccount pointsAccount) {
    ElderAccountResponse resp = new ElderAccountResponse();
    resp.setId(account.getId());
    resp.setElderId(account.getElderId());
    resp.setElderName(elderName);
    resp.setBalance(account.getBalance());
    resp.setCreditLimit(account.getCreditLimit());
    resp.setWarnThreshold(account.getWarnThreshold());
    resp.setStatus(account.getStatus());
    if (pointsAccount != null) {
      resp.setPointsBalance(pointsAccount.getPointsBalance());
      resp.setPointsStatus(pointsAccount.getStatus());
    }
    resp.setRemark(account.getRemark());
    resp.setCreateTime(account.getCreateTime());
    resp.setUpdateTime(account.getUpdateTime());
    return resp;
  }

  private ElderAccountLogResponse toLogResponse(ElderAccountLog log, String elderName) {
    ElderAccountLogResponse resp = new ElderAccountLogResponse();
    resp.setId(log.getId());
    resp.setAccountId(log.getAccountId());
    resp.setElderId(log.getElderId());
    resp.setElderName(elderName);
    resp.setAmount(log.getAmount());
    resp.setBalanceAfter(log.getBalanceAfter());
    resp.setDirection(log.getDirection());
    resp.setSourceType(log.getSourceType());
    resp.setSourceId(log.getSourceId());
    resp.setRemark(log.getRemark());
    resp.setCreateTime(log.getCreateTime());
    return resp;
  }

  private Map<Long, ElderPointsAccount> pointsAccountMap(List<Long> elderIds) {
    if (elderIds == null || elderIds.isEmpty()) {
      return Map.of();
    }
    return pointsAccountMapper.selectList(Wrappers.lambdaQuery(ElderPointsAccount.class)
            .in(ElderPointsAccount::getElderId, elderIds))
        .stream()
        .collect(Collectors.toMap(ElderPointsAccount::getElderId, item -> item, (a, b) -> a));
  }
}
