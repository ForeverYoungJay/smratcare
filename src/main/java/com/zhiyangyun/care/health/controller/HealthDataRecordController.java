package com.zhiyangyun.care.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthDataRecord;
import com.zhiyangyun.care.health.mapper.HealthDataRecordMapper;
import com.zhiyangyun.care.health.model.HealthDataRecordRequest;
import com.zhiyangyun.care.health.support.ElderResolveSupport;
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
@RequestMapping("/api/health/data")
public class HealthDataRecordController {
  private final HealthDataRecordMapper mapper;
  private final ElderResolveSupport elderResolveSupport;

  public HealthDataRecordController(HealthDataRecordMapper mapper, ElderResolveSupport elderResolveSupport) {
    this.mapper = mapper;
    this.elderResolveSupport = elderResolveSupport;
  }

  @GetMapping("/page")
  public Result<IPage<HealthDataRecord>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String dataType,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(HealthDataRecord.class)
        .eq(HealthDataRecord::getIsDeleted, 0)
        .eq(orgId != null, HealthDataRecord::getOrgId, orgId)
        .eq(elderId != null, HealthDataRecord::getElderId, elderId)
        .eq(dataType != null && !dataType.isBlank(), HealthDataRecord::getDataType, dataType);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(HealthDataRecord::getElderName, keyword)
          .or().like(HealthDataRecord::getDataValue, keyword));
    }
    wrapper.orderByDesc(HealthDataRecord::getMeasuredAt);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<HealthDataRecord> create(@Valid @RequestBody HealthDataRecordRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    HealthDataRecord item = new HealthDataRecord();
    item.setTenantId(orgId);
    item.setOrgId(orgId);
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setDataType(request.getDataType());
    item.setDataValue(request.getDataValue());
    item.setMeasuredAt(request.getMeasuredAt());
    item.setSource(request.getSource());
    item.setAbnormalFlag(request.getAbnormalFlag() == null ? 0 : request.getAbnormalFlag());
    item.setRemark(request.getRemark());
    item.setCreatedBy(AuthContext.getStaffId());
    mapper.insert(item);
    return Result.ok(item);
  }

  @PutMapping("/{id}")
  public Result<HealthDataRecord> update(@PathVariable Long id, @Valid @RequestBody HealthDataRecordRequest request) {
    HealthDataRecord item = mapper.selectById(id);
    if (item == null) {
      return Result.ok(null);
    }
    Long elderId = elderResolveSupport.resolveElderId(AuthContext.getOrgId(), request.getElderId(), request.getElderName());
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setDataType(request.getDataType());
    item.setDataValue(request.getDataValue());
    item.setMeasuredAt(request.getMeasuredAt());
    item.setSource(request.getSource());
    item.setAbnormalFlag(request.getAbnormalFlag() == null ? 0 : request.getAbnormalFlag());
    item.setRemark(request.getRemark());
    mapper.updateById(item);
    return Result.ok(item);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    HealthDataRecord item = mapper.selectById(id);
    if (item != null) {
      item.setIsDeleted(1);
      mapper.updateById(item);
    }
    return Result.ok(null);
  }
}
