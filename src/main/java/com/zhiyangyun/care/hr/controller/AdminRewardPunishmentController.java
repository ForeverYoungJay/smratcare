package com.zhiyangyun.care.hr.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.hr.entity.StaffPointsAccount;
import com.zhiyangyun.care.hr.entity.StaffPointsLog;
import com.zhiyangyun.care.hr.entity.StaffRewardPunishment;
import com.zhiyangyun.care.hr.mapper.StaffPointsAccountMapper;
import com.zhiyangyun.care.hr.mapper.StaffPointsLogMapper;
import com.zhiyangyun.care.hr.mapper.StaffRewardPunishmentMapper;
import com.zhiyangyun.care.hr.model.StaffRewardPunishmentRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
@RequestMapping("/api/admin/hr/reward-punishment")
@PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class AdminRewardPunishmentController {
  private static final String POINTS_SOURCE_TYPE = "REWARD_PUNISHMENT";

  private final StaffRewardPunishmentMapper rewardPunishmentMapper;
  private final StaffPointsAccountMapper pointsAccountMapper;
  private final StaffPointsLogMapper pointsLogMapper;

  public AdminRewardPunishmentController(StaffRewardPunishmentMapper rewardPunishmentMapper,
      StaffPointsAccountMapper pointsAccountMapper,
      StaffPointsLogMapper pointsLogMapper) {
    this.rewardPunishmentMapper = rewardPunishmentMapper;
    this.pointsAccountMapper = pointsAccountMapper;
    this.pointsLogMapper = pointsLogMapper;
  }

  @GetMapping("/page")
  public Result<IPage<StaffRewardPunishment>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long staffId,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) LocalDate dateFrom,
      @RequestParam(required = false) LocalDate dateTo,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(StaffRewardPunishment.class)
        .eq(StaffRewardPunishment::getIsDeleted, 0)
        .eq(orgId != null, StaffRewardPunishment::getOrgId, orgId)
        .eq(staffId != null, StaffRewardPunishment::getStaffId, staffId)
        .eq(type != null && !type.isBlank(), StaffRewardPunishment::getType, type)
        .eq(status != null && !status.isBlank(), StaffRewardPunishment::getStatus, status)
        .ge(dateFrom != null, StaffRewardPunishment::getOccurredDate, dateFrom)
        .le(dateTo != null, StaffRewardPunishment::getOccurredDate, dateTo);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(StaffRewardPunishment::getTitle, keyword)
          .or().like(StaffRewardPunishment::getReason, keyword)
          .or().like(StaffRewardPunishment::getStaffName, keyword));
    }
    wrapper.orderByDesc(StaffRewardPunishment::getOccurredDate).orderByDesc(StaffRewardPunishment::getCreateTime);
    return Result.ok(rewardPunishmentMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  @Transactional
  public Result<StaffRewardPunishment> create(@Valid @RequestBody StaffRewardPunishmentRequest request) {
    Long orgId = AuthContext.getOrgId();
    StaffRewardPunishment record = new StaffRewardPunishment();
    record.setOrgId(orgId);
    record.setStaffId(request.getStaffId());
    record.setStaffName(request.getStaffName());
    record.setType(request.getType());
    record.setLevel(request.getLevel());
    record.setTitle(request.getTitle());
    record.setReason(request.getReason());
    record.setAmount(request.getAmount());
    record.setPoints(request.getPoints());
    record.setOccurredDate(request.getOccurredDate());
    record.setStatus(request.getStatus() == null ? "EFFECTIVE" : request.getStatus());
    record.setRemark(request.getRemark());
    record.setCreatedBy(AuthContext.getStaffId());
    rewardPunishmentMapper.insert(record);
    syncPointsForRewardPunishment(null, record);
    return Result.ok(record);
  }

  @PutMapping("/{id}")
  @Transactional
  public Result<StaffRewardPunishment> update(@PathVariable Long id,
      @Valid @RequestBody StaffRewardPunishmentRequest request) {
    StaffRewardPunishment record = rewardPunishmentMapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (record == null || (orgId != null && !orgId.equals(record.getOrgId()))) {
      return Result.error(404, "奖惩记录不存在");
    }
    StaffRewardPunishment before = copyRecord(record);
    record.setStaffId(request.getStaffId());
    record.setStaffName(request.getStaffName());
    record.setType(request.getType());
    record.setLevel(request.getLevel());
    record.setTitle(request.getTitle());
    record.setReason(request.getReason());
    record.setAmount(request.getAmount());
    record.setPoints(request.getPoints());
    record.setOccurredDate(request.getOccurredDate());
    record.setStatus(request.getStatus());
    record.setRemark(request.getRemark());
    rewardPunishmentMapper.updateById(record);
    syncPointsForRewardPunishment(before, record);
    return Result.ok(record);
  }

  @DeleteMapping("/{id}")
  @Transactional
  public Result<Void> delete(@PathVariable Long id) {
    StaffRewardPunishment record = rewardPunishmentMapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (record != null && (orgId == null || orgId.equals(record.getOrgId()))) {
      StaffRewardPunishment before = copyRecord(record);
      record.setIsDeleted(1);
      rewardPunishmentMapper.updateById(record);
      syncPointsForRewardPunishment(before, null);
    }
    return Result.ok(null);
  }

  private void syncPointsForRewardPunishment(StaffRewardPunishment before, StaffRewardPunishment after) {
    Long orgId = after != null ? after.getOrgId() : before == null ? null : before.getOrgId();
    Long rewardId = after != null ? after.getId() : before == null ? null : before.getId();
    if (orgId == null || rewardId == null) {
      return;
    }

    StaffPointsLog existingLog = findRewardPointsLog(orgId, rewardId);
    if (shouldSyncPoints(after)) {
      int effectivePoints = resolveEffectivePoints(after);
      if (effectivePoints != 0) {
        if (existingLog == null) {
          existingLog = new StaffPointsLog();
          existingLog.setOrgId(orgId);
          existingLog.setSourceType(POINTS_SOURCE_TYPE);
          existingLog.setSourceId(rewardId);
          existingLog.setBalanceAfter(0);
        }
        existingLog.setStaffId(after.getStaffId());
        existingLog.setChangeType(effectivePoints >= 0 ? "EARN" : "DEDUCT");
        existingLog.setChangePoints(effectivePoints);
        existingLog.setRemark(buildPointsRemark(after));
        existingLog.setIsDeleted(0);
        if (existingLog.getId() == null) {
          pointsLogMapper.insert(existingLog);
        } else {
          pointsLogMapper.updateById(existingLog);
        }
      } else if (existingLog != null && (existingLog.getIsDeleted() == null || existingLog.getIsDeleted() == 0)) {
        existingLog.setIsDeleted(1);
        pointsLogMapper.updateById(existingLog);
      }
    } else if (existingLog != null && (existingLog.getIsDeleted() == null || existingLog.getIsDeleted() == 0)) {
      existingLog.setIsDeleted(1);
      pointsLogMapper.updateById(existingLog);
    }

    Set<Long> affectedStaffIds = new LinkedHashSet<>();
    if (before != null && before.getStaffId() != null) {
      affectedStaffIds.add(before.getStaffId());
    }
    if (after != null && after.getStaffId() != null) {
      affectedStaffIds.add(after.getStaffId());
    }
    if (existingLog != null && existingLog.getStaffId() != null) {
      affectedStaffIds.add(existingLog.getStaffId());
    }
    for (Long staffId : affectedStaffIds) {
      recalculatePointsAccount(orgId, staffId);
    }
  }

  private StaffPointsLog findRewardPointsLog(Long orgId, Long rewardId) {
    List<StaffPointsLog> logs = pointsLogMapper.selectList(
        Wrappers.lambdaQuery(StaffPointsLog.class)
            .eq(StaffPointsLog::getOrgId, orgId)
            .eq(StaffPointsLog::getSourceType, POINTS_SOURCE_TYPE)
            .eq(StaffPointsLog::getSourceId, rewardId)
            .orderByAsc(StaffPointsLog::getCreateTime)
            .orderByAsc(StaffPointsLog::getId));
    return logs.isEmpty() ? null : logs.get(0);
  }

  private void recalculatePointsAccount(Long orgId, Long staffId) {
    if (orgId == null || staffId == null) {
      return;
    }
    StaffPointsAccount account = pointsAccountMapper.selectOne(
        Wrappers.lambdaQuery(StaffPointsAccount.class)
            .eq(StaffPointsAccount::getOrgId, orgId)
            .eq(StaffPointsAccount::getStaffId, staffId)
            .eq(StaffPointsAccount::getIsDeleted, 0));
    if (account == null) {
      account = new StaffPointsAccount();
      account.setOrgId(orgId);
      account.setStaffId(staffId);
      account.setStatus(1);
      account.setIsDeleted(0);
      account.setPointsBalance(0);
      account.setTotalEarned(0);
      account.setTotalDeducted(0);
      pointsAccountMapper.insert(account);
    }

    List<StaffPointsLog> activeLogs = pointsLogMapper.selectList(
        Wrappers.lambdaQuery(StaffPointsLog.class)
            .eq(StaffPointsLog::getOrgId, orgId)
            .eq(StaffPointsLog::getStaffId, staffId)
            .eq(StaffPointsLog::getIsDeleted, 0)
            .orderByAsc(StaffPointsLog::getCreateTime)
            .orderByAsc(StaffPointsLog::getId));

    int balance = 0;
    int totalEarned = 0;
    int totalDeducted = 0;
    for (StaffPointsLog log : activeLogs) {
      int changePoints = log.getChangePoints() == null ? 0 : log.getChangePoints();
      balance += changePoints;
      if (changePoints > 0) {
        totalEarned += changePoints;
      } else if (changePoints < 0) {
        totalDeducted += Math.abs(changePoints);
      }
      if (log.getBalanceAfter() == null || log.getBalanceAfter() != balance) {
        log.setBalanceAfter(balance);
        pointsLogMapper.updateById(log);
      }
    }

    account.setPointsBalance(balance);
    account.setTotalEarned(totalEarned);
    account.setTotalDeducted(totalDeducted);
    pointsAccountMapper.updateById(account);
  }

  private boolean shouldSyncPoints(StaffRewardPunishment record) {
    return record != null
        && (record.getIsDeleted() == null || record.getIsDeleted() == 0)
        && "EFFECTIVE".equalsIgnoreCase(record.getStatus())
        && record.getStaffId() != null
        && resolveEffectivePoints(record) != 0;
  }

  private int resolveEffectivePoints(StaffRewardPunishment record) {
    if (record == null || record.getPoints() == null || record.getPoints() == 0) {
      return 0;
    }
    int absolutePoints = Math.abs(record.getPoints());
    return "REWARD".equalsIgnoreCase(record.getType()) ? absolutePoints : -absolutePoints;
  }

  private String buildPointsRemark(StaffRewardPunishment record) {
    String title = record.getTitle() == null ? "" : record.getTitle().trim();
    String remark = record.getRemark() == null ? "" : record.getRemark().trim();
    if (!title.isEmpty() && !remark.isEmpty()) {
      return "奖惩同步：" + title + "；" + remark;
    }
    if (!title.isEmpty()) {
      return "奖惩同步：" + title;
    }
    if (!remark.isEmpty()) {
      return "奖惩同步：" + remark;
    }
    return "奖惩积分同步";
  }

  private StaffRewardPunishment copyRecord(StaffRewardPunishment source) {
    if (source == null) {
      return null;
    }
    StaffRewardPunishment copy = new StaffRewardPunishment();
    copy.setId(source.getId());
    copy.setOrgId(source.getOrgId());
    copy.setStaffId(source.getStaffId());
    copy.setStaffName(source.getStaffName());
    copy.setType(source.getType());
    copy.setLevel(source.getLevel());
    copy.setTitle(source.getTitle());
    copy.setReason(source.getReason());
    copy.setAmount(source.getAmount());
    copy.setPoints(source.getPoints());
    copy.setOccurredDate(source.getOccurredDate());
    copy.setStatus(source.getStatus());
    copy.setRemark(source.getRemark());
    copy.setCreatedBy(source.getCreatedBy());
    copy.setCreateTime(source.getCreateTime());
    copy.setUpdateTime(source.getUpdateTime());
    copy.setIsDeleted(source.getIsDeleted());
    return copy;
  }
}
