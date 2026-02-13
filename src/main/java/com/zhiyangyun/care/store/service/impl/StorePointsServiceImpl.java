package com.zhiyangyun.care.store.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.store.entity.ElderPointsAccount;
import com.zhiyangyun.care.store.entity.ElderProfile;
import com.zhiyangyun.care.store.entity.PointsLog;
import com.zhiyangyun.care.store.mapper.ElderPointsAccountMapper;
import com.zhiyangyun.care.store.mapper.ElderProfileMapper;
import com.zhiyangyun.care.store.mapper.PointsLogMapper;
import com.zhiyangyun.care.store.model.PointsAccountResponse;
import com.zhiyangyun.care.store.model.PointsAdjustRequest;
import com.zhiyangyun.care.store.model.PointsLogResponse;
import com.zhiyangyun.care.store.service.StorePointsService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StorePointsServiceImpl implements StorePointsService {
  private final ElderPointsAccountMapper accountMapper;
  private final PointsLogMapper logMapper;
  private final ElderProfileMapper elderMapper;

  public StorePointsServiceImpl(ElderPointsAccountMapper accountMapper,
      PointsLogMapper logMapper,
      ElderProfileMapper elderMapper) {
    this.accountMapper = accountMapper;
    this.logMapper = logMapper;
    this.elderMapper = elderMapper;
  }

  @Override
  public IPage<PointsAccountResponse> page(Long orgId, long pageNo, long pageSize, String keyword, Integer status) {
    List<Long> elderIds = null;
    if (keyword != null && !keyword.isBlank()) {
      elderIds = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
          .eq(ElderProfile::getIsDeleted, 0)
          .eq(orgId != null, ElderProfile::getOrgId, orgId)
          .like(ElderProfile::getFullName, keyword))
          .stream()
          .map(ElderProfile::getId)
          .toList();
      if (elderIds.isEmpty()) {
        return new Page<>(pageNo, pageSize, 0);
      }
    }

    var wrapper = Wrappers.lambdaQuery(ElderPointsAccount.class)
        .eq(ElderPointsAccount::getIsDeleted, 0)
        .eq(orgId != null, ElderPointsAccount::getOrgId, orgId)
        .eq(status != null, ElderPointsAccount::getStatus, status);
    if (elderIds != null) {
      wrapper.in(ElderPointsAccount::getElderId, elderIds);
    }
    wrapper.orderByDesc(ElderPointsAccount::getUpdateTime);
    IPage<ElderPointsAccount> page = accountMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, String> elderNameMap = elderNameMap(page.getRecords().stream()
        .map(ElderPointsAccount::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList());
    IPage<PointsAccountResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream()
        .map(acc -> toAccountResponse(acc, elderNameMap.get(acc.getElderId())))
        .toList());
    return resp;
  }

  @Override
  public IPage<PointsLogResponse> logPage(Long orgId, long pageNo, long pageSize, Long elderId, String keyword) {
    var wrapper = Wrappers.lambdaQuery(PointsLog.class)
        .eq(PointsLog::getIsDeleted, 0)
        .eq(orgId != null, PointsLog::getOrgId, orgId)
        .eq(elderId != null, PointsLog::getElderId, elderId)
        .orderByDesc(PointsLog::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      List<Long> elderIds = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
          .eq(ElderProfile::getIsDeleted, 0)
          .eq(orgId != null, ElderProfile::getOrgId, orgId)
          .like(ElderProfile::getFullName, keyword))
          .stream()
          .map(ElderProfile::getId)
          .toList();
      if (elderIds.isEmpty()) {
        return new Page<>(pageNo, pageSize, 0);
      }
      wrapper.in(PointsLog::getElderId, elderIds);
    }
    IPage<PointsLog> page = logMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, String> elderNameMap = elderNameMap(page.getRecords().stream()
        .map(PointsLog::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList());
    IPage<PointsLogResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream()
        .map(log -> toLogResponse(log, elderNameMap.get(log.getElderId())))
        .toList());
    return resp;
  }

  @Override
  @Transactional
  public void adjust(PointsAdjustRequest request) {
    if (request.getPoints() == null || request.getPoints() <= 0) {
      throw new IllegalArgumentException("points must be positive");
    }
    ElderPointsAccount account = accountMapper.selectOne(
        Wrappers.lambdaQuery(ElderPointsAccount.class)
            .eq(ElderPointsAccount::getOrgId, request.getOrgId())
            .eq(ElderPointsAccount::getElderId, request.getElderId())
            .eq(ElderPointsAccount::getIsDeleted, 0));
    if (account == null) {
      account = new ElderPointsAccount();
      account.setOrgId(request.getOrgId());
      account.setElderId(request.getElderId());
      account.setPointsBalance(0);
      account.setStatus(1);
      accountMapper.insert(account);
    }
    int balance = account.getPointsBalance() == null ? 0 : account.getPointsBalance();
    int delta = request.getPoints();
    if ("DEBIT".equalsIgnoreCase(request.getDirection())) {
      if (balance < delta) {
        throw new IllegalStateException("points insufficient");
      }
      balance -= delta;
    } else {
      balance += delta;
    }
    account.setPointsBalance(balance);
    accountMapper.updateById(account);

    PointsLog log = new PointsLog();
    log.setOrgId(account.getOrgId());
    log.setElderId(account.getElderId());
    log.setChangeType("DEBIT".equalsIgnoreCase(request.getDirection()) ? "DEDUCT" : "RECHARGE");
    log.setChangePoints(delta);
    log.setBalanceAfter(balance);
    log.setRemark(request.getRemark());
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

  private PointsAccountResponse toAccountResponse(ElderPointsAccount account, String elderName) {
    PointsAccountResponse resp = new PointsAccountResponse();
    resp.setId(account.getId());
    resp.setOrgId(account.getOrgId());
    resp.setElderId(account.getElderId());
    resp.setElderName(elderName);
    resp.setPointsBalance(account.getPointsBalance());
    resp.setStatus(account.getStatus());
    resp.setUpdateTime(account.getUpdateTime());
    return resp;
  }

  private PointsLogResponse toLogResponse(PointsLog log, String elderName) {
    PointsLogResponse resp = new PointsLogResponse();
    resp.setId(log.getId());
    resp.setElderId(log.getElderId());
    resp.setElderName(elderName);
    resp.setChangeType(log.getChangeType());
    resp.setChangePoints(log.getChangePoints());
    resp.setBalanceAfter(log.getBalanceAfter());
    resp.setRefOrderId(log.getRefOrderId());
    resp.setRemark(log.getRemark());
    resp.setCreateTime(log.getCreateTime());
    return resp;
  }
}
