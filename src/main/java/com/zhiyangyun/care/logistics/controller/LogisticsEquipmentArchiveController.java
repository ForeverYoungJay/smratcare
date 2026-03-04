package com.zhiyangyun.care.logistics.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.MaintenanceRequest;
import com.zhiyangyun.care.life.mapper.MaintenanceRequestMapper;
import com.zhiyangyun.care.logistics.entity.LogisticsEquipmentArchive;
import com.zhiyangyun.care.logistics.mapper.LogisticsEquipmentArchiveMapper;
import com.zhiyangyun.care.logistics.model.LogisticsEquipmentArchiveRequest;
import com.zhiyangyun.care.logistics.model.LogisticsMaintenanceTodoGenerateResult;
import com.zhiyangyun.care.logistics.service.LogisticsEquipmentTodoService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
@RequestMapping("/api/logistics/equipment")
public class LogisticsEquipmentArchiveController {
  private final LogisticsEquipmentArchiveMapper equipmentMapper;
  private final MaintenanceRequestMapper maintenanceRequestMapper;
  private final LogisticsEquipmentTodoService equipmentTodoService;

  public LogisticsEquipmentArchiveController(
      LogisticsEquipmentArchiveMapper equipmentMapper,
      MaintenanceRequestMapper maintenanceRequestMapper,
      LogisticsEquipmentTodoService equipmentTodoService) {
    this.equipmentMapper = equipmentMapper;
    this.maintenanceRequestMapper = maintenanceRequestMapper;
    this.equipmentTodoService = equipmentTodoService;
  }

  @GetMapping("/page")
  public Result<IPage<LogisticsEquipmentArchive>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String category) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(LogisticsEquipmentArchive.class)
        .eq(LogisticsEquipmentArchive::getIsDeleted, 0)
        .eq(orgId != null, LogisticsEquipmentArchive::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), LogisticsEquipmentArchive::getStatus, status)
        .eq(category != null && !category.isBlank(), LogisticsEquipmentArchive::getCategory, category);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(LogisticsEquipmentArchive::getEquipmentCode, keyword)
          .or().like(LogisticsEquipmentArchive::getEquipmentName, keyword)
          .or().like(LogisticsEquipmentArchive::getCategory, keyword)
          .or().like(LogisticsEquipmentArchive::getLocation, keyword)
          .or().like(LogisticsEquipmentArchive::getSerialNo, keyword));
    }
    wrapper.orderByDesc(LogisticsEquipmentArchive::getCreateTime);
    return Result.ok(equipmentMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<LogisticsEquipmentArchive> create(@Valid @RequestBody LogisticsEquipmentArchiveRequest request) {
    Long orgId = AuthContext.getOrgId();
    LogisticsEquipmentArchive entity = new LogisticsEquipmentArchive();
    entity.setTenantId(orgId);
    entity.setOrgId(orgId);
    applyFields(entity, request, true);
    entity.setCreatedBy(AuthContext.getStaffId());
    equipmentMapper.insert(entity);
    return Result.ok(entity);
  }

  @PutMapping("/{id}")
  public Result<LogisticsEquipmentArchive> update(
      @PathVariable Long id,
      @Valid @RequestBody LogisticsEquipmentArchiveRequest request) {
    LogisticsEquipmentArchive entity = getInOrg(id, AuthContext.getOrgId());
    if (entity == null) {
      return Result.ok(null);
    }
    applyFields(entity, request, false);
    equipmentMapper.updateById(entity);
    return Result.ok(entity);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    LogisticsEquipmentArchive entity = getInOrg(id, AuthContext.getOrgId());
    if (entity != null) {
      entity.setIsDeleted(1);
      equipmentMapper.updateById(entity);
    }
    return Result.ok(null);
  }

  @PutMapping("/{id}/generate-maintenance")
  public Result<MaintenanceRequest> generateMaintenance(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    LogisticsEquipmentArchive equipment = getInOrg(id, orgId);
    if (equipment == null) {
      return Result.ok(null);
    }
    MaintenanceRequest request = new MaintenanceRequest();
    request.setTenantId(orgId);
    request.setOrgId(orgId);
    request.setReporterName(equipment.getMaintainerName() == null ? "设备档案" : equipment.getMaintainerName());
    request.setAssigneeName(equipment.getMaintainerName());
    request.setIssueType("设备维保");
    request.setDescription(buildMaintenanceDescription(equipment));
    request.setPriority("NORMAL");
    request.setStatus("OPEN");
    request.setReportedAt(LocalDateTime.now());
    request.setLaborCost(BigDecimal.ZERO);
    request.setMaterialCost(BigDecimal.ZERO);
    request.setTotalCost(BigDecimal.ZERO);
    request.setRemark("由设备档案一键发起");
    request.setCreatedBy(AuthContext.getStaffId());
    maintenanceRequestMapper.insert(request);
    return Result.ok(request);
  }

  @PostMapping("/generate-maintenance-todos")
  public Result<Map<String, Long>> generateMaintenanceTodos(
      @RequestParam(required = false, defaultValue = "30") int days) {
    Long orgId = AuthContext.getOrgId();
    Long operatorStaffId = AuthContext.getStaffId();
    try {
      LogisticsMaintenanceTodoGenerateResult generateResult =
          equipmentTodoService.generateMaintenanceTodos(orgId, days, operatorStaffId);
      equipmentTodoService.saveJobLog(orgId, "MANUAL", days, generateResult, "SUCCESS", null, operatorStaffId);
      Map<String, Long> result = new HashMap<>();
      result.put("createdCount", generateResult.getCreatedCount());
      result.put("skippedCount", generateResult.getSkippedCount());
      result.put("totalMatched", generateResult.getTotalMatched());
      return Result.ok(result);
    } catch (RuntimeException ex) {
      equipmentTodoService.saveJobLog(
          orgId,
          "MANUAL",
          days,
          null,
          "FAILED",
          truncateMessage(ex.getMessage()),
          operatorStaffId);
      throw ex;
    }
  }

  private LogisticsEquipmentArchive getInOrg(Long id, Long orgId) {
    return equipmentMapper.selectOne(Wrappers.lambdaQuery(LogisticsEquipmentArchive.class)
        .eq(LogisticsEquipmentArchive::getId, id)
        .eq(LogisticsEquipmentArchive::getIsDeleted, 0)
        .eq(orgId != null, LogisticsEquipmentArchive::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private void applyFields(
      LogisticsEquipmentArchive entity,
      LogisticsEquipmentArchiveRequest request,
      boolean create) {
    entity.setEquipmentCode(request.getEquipmentCode().trim());
    entity.setEquipmentName(request.getEquipmentName().trim());
    entity.setCategory(normalizeText(request.getCategory()));
    entity.setBrand(normalizeText(request.getBrand()));
    entity.setModelNo(normalizeText(request.getModelNo()));
    entity.setSerialNo(normalizeText(request.getSerialNo()));
    entity.setPurchaseDate(request.getPurchaseDate());
    entity.setWarrantyUntil(request.getWarrantyUntil());
    entity.setLocation(normalizeText(request.getLocation()));
    entity.setMaintainerName(normalizeText(request.getMaintainerName()));
    entity.setLastMaintainedAt(request.getLastMaintainedAt());
    entity.setNextMaintainedAt(request.getNextMaintainedAt());
    if (request.getStatus() == null || request.getStatus().isBlank()) {
      entity.setStatus(create ? "ENABLED" : entity.getStatus());
    } else {
      entity.setStatus(request.getStatus());
    }
    entity.setRemark(normalizeText(request.getRemark()));
  }

  private String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String buildMaintenanceDescription(LogisticsEquipmentArchive equipment) {
    StringBuilder sb = new StringBuilder();
    sb.append("设备维保申请：");
    sb.append(equipment.getEquipmentCode()).append(" / ").append(equipment.getEquipmentName());
    if (equipment.getLocation() != null && !equipment.getLocation().isBlank()) {
      sb.append("，位置：").append(equipment.getLocation());
    }
    if (equipment.getSerialNo() != null && !equipment.getSerialNo().isBlank()) {
      sb.append("，序列号：").append(equipment.getSerialNo());
    }
    if (equipment.getNextMaintainedAt() != null) {
      sb.append("，计划维保时间：").append(equipment.getNextMaintainedAt());
    }
    return sb.toString();
  }

  private String truncateMessage(String message) {
    if (message == null) {
      return null;
    }
    if (message.length() <= 500) {
      return message;
    }
    return message.substring(0, 500);
  }

}
