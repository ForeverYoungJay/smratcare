package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.life.entity.HealthBasicRecord;
import com.zhiyangyun.care.life.mapper.HealthBasicRecordMapper;
import com.zhiyangyun.care.life.model.HealthBasicRecordRequest;
import jakarta.validation.Valid;
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
@RequestMapping("/api/life/health-basic")
public class HealthBasicRecordController {
  private final HealthBasicRecordMapper recordMapper;
  private final ElderMapper elderMapper;

  public HealthBasicRecordController(HealthBasicRecordMapper recordMapper, ElderMapper elderMapper) {
    this.recordMapper = recordMapper;
    this.elderMapper = elderMapper;
  }

  @GetMapping("/page")
  public Result<IPage<HealthBasicRecord>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(HealthBasicRecord.class)
        .eq(HealthBasicRecord::getIsDeleted, 0)
        .eq(orgId != null, HealthBasicRecord::getOrgId, orgId)
        .eq(elderId != null, HealthBasicRecord::getElderId, elderId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.like(HealthBasicRecord::getElderName, keyword);
    }
    wrapper.orderByDesc(HealthBasicRecord::getRecordDate);
    return Result.ok(recordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<HealthBasicRecord> create(@Valid @RequestBody HealthBasicRecordRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long elderId = resolveElderId(orgId, request.getElderId(), request.getElderName());
    HealthBasicRecord record = new HealthBasicRecord();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    record.setElderId(elderId);
    record.setElderName(resolveElderName(elderId, request.getElderName()));
    record.setRecordDate(request.getRecordDate());
    record.setHeightCm(request.getHeightCm());
    record.setWeightKg(request.getWeightKg());
    record.setBmi(request.getBmi());
    record.setBloodPressure(request.getBloodPressure());
    record.setHeartRate(request.getHeartRate());
    record.setRemark(request.getRemark());
    record.setCreatedBy(AuthContext.getStaffId());
    recordMapper.insert(record);
    return Result.ok(record);
  }

  @PutMapping("/{id}")
  public Result<HealthBasicRecord> update(@PathVariable Long id, @Valid @RequestBody HealthBasicRecordRequest request) {
    HealthBasicRecord record = recordMapper.selectById(id);
    if (record == null) {
      return Result.ok(null);
    }
    Long elderId = resolveElderId(AuthContext.getOrgId(), request.getElderId(), request.getElderName());
    record.setElderId(elderId);
    record.setElderName(resolveElderName(elderId, request.getElderName()));
    record.setRecordDate(request.getRecordDate());
    record.setHeightCm(request.getHeightCm());
    record.setWeightKg(request.getWeightKg());
    record.setBmi(request.getBmi());
    record.setBloodPressure(request.getBloodPressure());
    record.setHeartRate(request.getHeartRate());
    record.setRemark(request.getRemark());
    recordMapper.updateById(record);
    return Result.ok(record);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    HealthBasicRecord record = recordMapper.selectById(id);
    if (record != null) {
      record.setIsDeleted(1);
      recordMapper.updateById(record);
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

  private Long resolveElderId(Long orgId, Long elderId, String elderName) {
    if (elderId != null) {
      return elderId;
    }
    if (elderName == null || elderName.isBlank()) {
      throw new IllegalArgumentException("elderName required");
    }
    ElderProfile elder = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getFullName, elderName)
        .last("LIMIT 1"));
    if (elder == null) {
      throw new IllegalArgumentException("elder not found");
    }
    return elder.getId();
  }
}
