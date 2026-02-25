package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.life.entity.MaintenanceRequest;
import com.zhiyangyun.care.life.mapper.MaintenanceRequestMapper;
import com.zhiyangyun.care.life.model.MaintenanceRequestCreateRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/life/maintenance")
public class MaintenanceController {
  private final MaintenanceRequestMapper requestMapper;
  private final RoomMapper roomMapper;

  public MaintenanceController(MaintenanceRequestMapper requestMapper, RoomMapper roomMapper) {
    this.requestMapper = requestMapper;
    this.roomMapper = roomMapper;
  }

  @GetMapping("/page")
  public Result<IPage<MaintenanceRequest>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateFrom,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      @RequestParam(required = false) LocalDate dateTo) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(MaintenanceRequest.class)
        .eq(MaintenanceRequest::getIsDeleted, 0)
        .eq(orgId != null, MaintenanceRequest::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), MaintenanceRequest::getStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(MaintenanceRequest::getRoomNo, keyword)
          .or().like(MaintenanceRequest::getIssueType, keyword)
          .or().like(MaintenanceRequest::getReporterName, keyword)
          .or().like(MaintenanceRequest::getAssigneeName, keyword));
    }
    if (dateFrom != null) {
      wrapper.ge(MaintenanceRequest::getReportedAt, dateFrom.atStartOfDay());
    }
    if (dateTo != null) {
      wrapper.lt(MaintenanceRequest::getReportedAt, dateTo.plusDays(1).atStartOfDay());
    }
    wrapper.orderByDesc(MaintenanceRequest::getReportedAt);
    return Result.ok(requestMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<MaintenanceRequest> create(@Valid @RequestBody MaintenanceRequestCreateRequest request) {
    Long orgId = AuthContext.getOrgId();
    MaintenanceRequest entity = new MaintenanceRequest();
    entity.setTenantId(orgId);
    entity.setOrgId(orgId);
    entity.setRoomId(request.getRoomId());
    entity.setRoomNo(resolveRoomNo(request.getRoomId()));
    entity.setReporterName(request.getReporterName());
    entity.setAssigneeName(request.getAssigneeName());
    entity.setIssueType(request.getIssueType());
    entity.setDescription(request.getDescription());
    entity.setPriority(request.getPriority() == null ? "NORMAL" : request.getPriority());
    entity.setStatus(request.getStatus() == null ? "OPEN" : request.getStatus());
    entity.setReportedAt(request.getReportedAt() == null ? LocalDateTime.now() : request.getReportedAt());
    entity.setRemark(request.getRemark());
    entity.setCreatedBy(AuthContext.getStaffId());
    requestMapper.insert(entity);
    return Result.ok(entity);
  }

  @PutMapping("/{id}")
  public Result<MaintenanceRequest> update(@PathVariable Long id, @Valid @RequestBody MaintenanceRequestCreateRequest request) {
    MaintenanceRequest entity = requestMapper.selectById(id);
    if (entity == null) {
      return Result.ok(null);
    }
    entity.setRoomId(request.getRoomId());
    entity.setRoomNo(resolveRoomNo(request.getRoomId()));
    entity.setReporterName(request.getReporterName());
    entity.setAssigneeName(request.getAssigneeName());
    entity.setIssueType(request.getIssueType());
    entity.setDescription(request.getDescription());
    entity.setPriority(request.getPriority() == null ? entity.getPriority() : request.getPriority());
    entity.setStatus(request.getStatus() == null ? entity.getStatus() : request.getStatus());
    entity.setReportedAt(request.getReportedAt() == null ? entity.getReportedAt() : request.getReportedAt());
    entity.setRemark(request.getRemark());
    requestMapper.updateById(entity);
    return Result.ok(entity);
  }

  @PutMapping("/{id}/complete")
  public Result<MaintenanceRequest> complete(@PathVariable Long id) {
    MaintenanceRequest entity = requestMapper.selectById(id);
    if (entity == null) {
      return Result.ok(null);
    }
    entity.setStatus("COMPLETED");
    entity.setCompletedAt(LocalDateTime.now());
    requestMapper.updateById(entity);
    return Result.ok(entity);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    MaintenanceRequest entity = requestMapper.selectById(id);
    if (entity != null) {
      entity.setIsDeleted(1);
      requestMapper.updateById(entity);
    }
    return Result.ok(null);
  }

  private String resolveRoomNo(Long roomId) {
    if (roomId == null) {
      return null;
    }
    Room room = roomMapper.selectById(roomId);
    return room == null ? null : room.getRoomNo();
  }
}
