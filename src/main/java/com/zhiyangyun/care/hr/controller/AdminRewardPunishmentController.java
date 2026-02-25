package com.zhiyangyun.care.hr.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.hr.entity.StaffRewardPunishment;
import com.zhiyangyun.care.hr.mapper.StaffRewardPunishmentMapper;
import com.zhiyangyun.care.hr.model.StaffRewardPunishmentRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasRole('ADMIN')")
public class AdminRewardPunishmentController {
  private final StaffRewardPunishmentMapper rewardPunishmentMapper;

  public AdminRewardPunishmentController(StaffRewardPunishmentMapper rewardPunishmentMapper) {
    this.rewardPunishmentMapper = rewardPunishmentMapper;
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
    return Result.ok(record);
  }

  @PutMapping("/{id}")
  public Result<StaffRewardPunishment> update(@PathVariable Long id,
      @Valid @RequestBody StaffRewardPunishmentRequest request) {
    StaffRewardPunishment record = rewardPunishmentMapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (record == null || (orgId != null && !orgId.equals(record.getOrgId()))) {
      return Result.error(404, "奖惩记录不存在");
    }
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
    return Result.ok(record);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    StaffRewardPunishment record = rewardPunishmentMapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (record != null && (orgId == null || orgId.equals(record.getOrgId()))) {
      record.setIsDeleted(1);
      rewardPunishmentMapper.updateById(record);
    }
    return Result.ok(null);
  }
}
