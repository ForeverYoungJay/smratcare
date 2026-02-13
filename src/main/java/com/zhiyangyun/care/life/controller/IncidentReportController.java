package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.life.entity.IncidentReport;
import com.zhiyangyun.care.life.mapper.IncidentReportMapper;
import com.zhiyangyun.care.life.model.IncidentReportRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
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
@RequestMapping("/api/life/incident")
public class IncidentReportController {
  private final IncidentReportMapper incidentMapper;
  private final ElderMapper elderMapper;

  public IncidentReportController(IncidentReportMapper incidentMapper, ElderMapper elderMapper) {
    this.incidentMapper = incidentMapper;
    this.elderMapper = elderMapper;
  }

  @GetMapping("/page")
  public Result<IPage<IncidentReport>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(IncidentReport.class)
        .eq(IncidentReport::getIsDeleted, 0)
        .eq(orgId != null, IncidentReport::getOrgId, orgId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(IncidentReport::getElderName, keyword)
          .or().like(IncidentReport::getIncidentType, keyword)
          .or().like(IncidentReport::getReporterName, keyword));
    }
    wrapper.orderByDesc(IncidentReport::getIncidentTime);
    return Result.ok(incidentMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<IncidentReport> create(@Valid @RequestBody IncidentReportRequest request) {
    Long orgId = AuthContext.getOrgId();
    IncidentReport report = new IncidentReport();
    report.setTenantId(orgId);
    report.setOrgId(orgId);
    report.setElderId(request.getElderId());
    report.setElderName(resolveElderName(request.getElderId(), request.getElderName()));
    report.setReporterName(request.getReporterName());
    report.setIncidentTime(request.getIncidentTime());
    report.setIncidentType(request.getIncidentType());
    report.setLevel(request.getLevel() == null ? "NORMAL" : request.getLevel());
    report.setDescription(request.getDescription());
    report.setActionTaken(request.getActionTaken());
    report.setStatus(request.getStatus() == null ? "OPEN" : request.getStatus());
    report.setCreatedBy(AuthContext.getStaffId());
    incidentMapper.insert(report);
    return Result.ok(report);
  }

  @PutMapping("/{id}")
  public Result<IncidentReport> update(@PathVariable Long id, @Valid @RequestBody IncidentReportRequest request) {
    IncidentReport existing = incidentMapper.selectById(id);
    if (existing == null) {
      return Result.ok(null);
    }
    existing.setElderId(request.getElderId());
    existing.setElderName(resolveElderName(request.getElderId(), request.getElderName()));
    existing.setReporterName(request.getReporterName());
    existing.setIncidentTime(request.getIncidentTime());
    existing.setIncidentType(request.getIncidentType());
    existing.setLevel(request.getLevel());
    existing.setDescription(request.getDescription());
    existing.setActionTaken(request.getActionTaken());
    existing.setStatus(request.getStatus());
    incidentMapper.updateById(existing);
    return Result.ok(existing);
  }

  @PutMapping("/{id}/close")
  public Result<IncidentReport> close(@PathVariable Long id) {
    IncidentReport existing = incidentMapper.selectById(id);
    if (existing == null) {
      return Result.ok(null);
    }
    existing.setStatus("CLOSED");
    existing.setUpdateTime(LocalDateTime.now());
    incidentMapper.updateById(existing);
    return Result.ok(existing);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    IncidentReport existing = incidentMapper.selectById(id);
    if (existing != null) {
      existing.setIsDeleted(1);
      incidentMapper.updateById(existing);
    }
    return Result.ok(null);
  }

  private String resolveElderName(Long elderId, String fallback) {
    if (fallback != null && !fallback.isBlank()) {
      return fallback;
    }
    if (elderId == null) {
      return null;
    }
    ElderProfile elder = elderMapper.selectById(elderId);
    return elder == null ? null : elder.getFullName();
  }
}
