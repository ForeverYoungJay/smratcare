package com.zhiyangyun.care.hr.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.hr.entity.StaffPointsAccount;
import com.zhiyangyun.care.hr.entity.StaffPointsLog;
import com.zhiyangyun.care.hr.mapper.StaffPointsAccountMapper;
import com.zhiyangyun.care.hr.mapper.StaffPointsLogMapper;
import com.zhiyangyun.care.hr.model.StaffPointsAccountResponse;
import com.zhiyangyun.care.hr.model.StaffPointsAdjustRequest;
import com.zhiyangyun.care.hr.model.StaffPointsLogResponse;
import com.zhiyangyun.care.hr.service.StaffPointsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffPointsServiceImpl implements StaffPointsService {
  private final StaffPointsAccountMapper accountMapper;
  private final StaffPointsLogMapper logMapper;
  private final StaffMapper staffMapper;

  public StaffPointsServiceImpl(StaffPointsAccountMapper accountMapper,
      StaffPointsLogMapper logMapper,
      StaffMapper staffMapper) {
    this.accountMapper = accountMapper;
    this.logMapper = logMapper;
    this.staffMapper = staffMapper;
  }

  @Override
  @Transactional
  public StaffPointsAccountResponse adjust(Long orgId, Long operatorId, StaffPointsAdjustRequest request) {
    StaffPointsAccount account = getOrCreateAccount(orgId, request.getStaffId());

    int changePoints = request.getChangePoints() == null ? 0 : request.getChangePoints();
    String changeType = request.getChangeType() == null ? "ADJUST" : request.getChangeType().toUpperCase();
    int effectiveChange = changePoints;
    if ("DEDUCT".equals(changeType)) {
      effectiveChange = -Math.abs(changePoints);
    } else if ("EARN".equals(changeType)) {
      effectiveChange = Math.abs(changePoints);
    }

    int newBalance = account.getPointsBalance() + effectiveChange;
    if (newBalance < 0) {
      return null;
    }

    applyChange(account, orgId, request.getStaffId(), changeType, effectiveChange, newBalance,
        "MANUAL", operatorId, request.getRemark());

    StaffPointsAccountResponse response = new StaffPointsAccountResponse();
    response.setStaffId(account.getStaffId());
    response.setPointsBalance(account.getPointsBalance());
    response.setTotalEarned(account.getTotalEarned());
    response.setTotalDeducted(account.getTotalDeducted());
    response.setStatus(account.getStatus());
    return response;
  }

  @Override
  @Transactional
  public StaffPointsAccountResponse awardForTask(Long orgId, Long staffId, Long taskDailyId, int points, String remark) {
    if (orgId == null || staffId == null || taskDailyId == null || points == 0) {
      return null;
    }
    boolean exists = logMapper.selectCount(
        Wrappers.lambdaQuery(StaffPointsLog.class)
            .eq(StaffPointsLog::getIsDeleted, 0)
            .eq(StaffPointsLog::getOrgId, orgId)
            .eq(StaffPointsLog::getStaffId, staffId)
            .eq(StaffPointsLog::getSourceType, "TASK")
            .eq(StaffPointsLog::getSourceId, taskDailyId)) > 0;
    if (exists) {
      StaffPointsAccount account = getOrCreateAccount(orgId, staffId);
      return toResponse(account);
    }

    StaffPointsAccount account = getOrCreateAccount(orgId, staffId);
    int newBalance = account.getPointsBalance() + points;
    if (newBalance < 0) {
      return null;
    }
    String changeType = points >= 0 ? "EARN" : "DEDUCT";
    applyChange(account, orgId, staffId, changeType, points, newBalance, "TASK", taskDailyId, remark);
    return toResponse(account);
  }

  @Override
  @Transactional
  public StaffPointsAccountResponse awardForReview(Long orgId, Long staffId, Long taskDailyId, int points, String remark) {
    if (orgId == null || staffId == null || taskDailyId == null || points == 0) {
      return null;
    }
    boolean exists = logMapper.selectCount(
        Wrappers.lambdaQuery(StaffPointsLog.class)
            .eq(StaffPointsLog::getIsDeleted, 0)
            .eq(StaffPointsLog::getOrgId, orgId)
            .eq(StaffPointsLog::getStaffId, staffId)
            .eq(StaffPointsLog::getSourceType, "REVIEW")
            .eq(StaffPointsLog::getSourceId, taskDailyId)) > 0;
    if (exists) {
      StaffPointsAccount account = getOrCreateAccount(orgId, staffId);
      return toResponse(account);
    }
    StaffPointsAccount account = getOrCreateAccount(orgId, staffId);
    int newBalance = account.getPointsBalance() + points;
    if (newBalance < 0) {
      return null;
    }
    String changeType = points >= 0 ? "EARN" : "DEDUCT";
    applyChange(account, orgId, staffId, changeType, points, newBalance, "REVIEW", taskDailyId, remark);
    return toResponse(account);
  }

  @Override
  public IPage<StaffPointsLogResponse> pageLogs(Long orgId, long pageNo, long pageSize, Long staffId,
      String dateFrom, String dateTo) {
    var wrapper = Wrappers.lambdaQuery(StaffPointsLog.class)
        .eq(StaffPointsLog::getIsDeleted, 0)
        .eq(orgId != null, StaffPointsLog::getOrgId, orgId)
        .eq(staffId != null, StaffPointsLog::getStaffId, staffId);
    if (dateFrom != null && !dateFrom.isBlank()) {
      LocalDate start = LocalDate.parse(dateFrom);
      wrapper.ge(StaffPointsLog::getCreateTime, LocalDateTime.of(start, LocalTime.MIN));
    }
    if (dateTo != null && !dateTo.isBlank()) {
      LocalDate end = LocalDate.parse(dateTo);
      wrapper.le(StaffPointsLog::getCreateTime, LocalDateTime.of(end, LocalTime.MAX));
    }
    wrapper.orderByDesc(StaffPointsLog::getCreateTime);

    IPage<StaffPointsLog> page = logMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIds(
            page.getRecords().stream().map(StaffPointsLog::getStaffId).distinct().toList())
        .stream()
        .collect(Collectors.toMap(StaffAccount::getId, s -> s));

    IPage<StaffPointsLogResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(log -> {
      StaffPointsLogResponse item = new StaffPointsLogResponse();
      StaffAccount staff = staffMap.get(log.getStaffId());
      item.setId(log.getId());
      item.setStaffId(log.getStaffId());
      item.setStaffName(staff == null ? null : staff.getRealName());
      item.setChangeType(log.getChangeType());
      item.setChangePoints(log.getChangePoints());
      item.setBalanceAfter(log.getBalanceAfter());
      item.setSourceType(log.getSourceType());
      item.setSourceId(log.getSourceId());
      item.setRemark(log.getRemark());
      item.setCreateTime(log.getCreateTime());
      return item;
    }).toList());
    return resp;
  }

  @Override
  @Transactional
  public void awardSurveyPoints(Long orgId, Long staffId, Long submissionId, Integer points, String remark) {
    if (orgId == null || staffId == null || points == null || points <= 0) {
      return;
    }
    StaffPointsAccount account = getOrCreateAccount(orgId, staffId);
    int newBalance = account.getPointsBalance() + points;
    if (newBalance < 0) {
      return;
    }
    applyChange(account, orgId, staffId, "EARN", points, newBalance, "SURVEY", submissionId, remark);
  }

  private StaffPointsAccount getOrCreateAccount(Long orgId, Long staffId) {
    StaffPointsAccount account = accountMapper.selectOne(
        Wrappers.lambdaQuery(StaffPointsAccount.class)
            .eq(StaffPointsAccount::getOrgId, orgId)
            .eq(StaffPointsAccount::getStaffId, staffId)
            .eq(StaffPointsAccount::getIsDeleted, 0));
    if (account == null) {
      account = new StaffPointsAccount();
      account.setOrgId(orgId);
      account.setStaffId(staffId);
      account.setPointsBalance(0);
      account.setTotalEarned(0);
      account.setTotalDeducted(0);
      account.setStatus(1);
      accountMapper.insert(account);
    }
    return account;
  }

  private void applyChange(StaffPointsAccount account, Long orgId, Long staffId, String changeType,
      int changePoints, int newBalance, String sourceType, Long sourceId, String remark) {
    account.setPointsBalance(newBalance);
    if (changePoints > 0) {
      account.setTotalEarned(account.getTotalEarned() + changePoints);
    } else if (changePoints < 0) {
      account.setTotalDeducted(account.getTotalDeducted() + Math.abs(changePoints));
    }
    accountMapper.updateById(account);

    StaffPointsLog log = new StaffPointsLog();
    log.setOrgId(orgId);
    log.setStaffId(staffId);
    log.setChangeType(changeType);
    log.setChangePoints(changePoints);
    log.setBalanceAfter(newBalance);
    log.setSourceType(sourceType);
    log.setSourceId(sourceId);
    log.setRemark(remark);
    logMapper.insert(log);
  }

  private StaffPointsAccountResponse toResponse(StaffPointsAccount account) {
    StaffPointsAccountResponse response = new StaffPointsAccountResponse();
    response.setStaffId(account.getStaffId());
    response.setPointsBalance(account.getPointsBalance());
    response.setTotalEarned(account.getTotalEarned());
    response.setTotalDeducted(account.getTotalDeducted());
    response.setStatus(account.getStatus());
    return response;
  }
}
