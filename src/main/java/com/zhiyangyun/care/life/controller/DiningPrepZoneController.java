package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.DiningPrepZone;
import com.zhiyangyun.care.life.mapper.DiningPrepZoneMapper;
import com.zhiyangyun.care.life.model.DiningConstants;
import com.zhiyangyun.care.life.model.DiningPrepZoneRequest;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/life/dining/prep-zone")
public class DiningPrepZoneController {
  private final DiningPrepZoneMapper prepZoneMapper;

  public DiningPrepZoneController(DiningPrepZoneMapper prepZoneMapper) {
    this.prepZoneMapper = prepZoneMapper;
  }

  @GetMapping("/page")
  public Result<IPage<DiningPrepZone>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(DiningPrepZone.class)
        .eq(DiningPrepZone::getIsDeleted, 0)
        .eq(orgId != null, DiningPrepZone::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), DiningPrepZone::getStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(DiningPrepZone::getZoneName, keyword)
          .or().like(DiningPrepZone::getZoneCode, keyword)
          .or().like(DiningPrepZone::getManagerName, keyword));
    }
    wrapper.orderByDesc(DiningPrepZone::getUpdateTime);
    return Result.ok(prepZoneMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/list")
  public Result<List<DiningPrepZone>> list(@RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(prepZoneMapper.selectList(Wrappers.lambdaQuery(DiningPrepZone.class)
        .eq(DiningPrepZone::getIsDeleted, 0)
        .eq(orgId != null, DiningPrepZone::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), DiningPrepZone::getStatus, status)
        .orderByAsc(DiningPrepZone::getZoneCode)));
  }

  @PostMapping
  public Result<DiningPrepZone> create(@Valid @RequestBody DiningPrepZoneRequest request) {
    Long orgId = AuthContext.getOrgId();
    validateCapacity(request.getCapacity());
    DiningPrepZone zone = new DiningPrepZone();
    zone.setTenantId(orgId);
    zone.setOrgId(orgId);
    zone.setZoneCode(request.getZoneCode().trim());
    zone.setZoneName(request.getZoneName().trim());
    zone.setKitchenArea(normalizeText(request.getKitchenArea()));
    zone.setCapacity(request.getCapacity());
    zone.setManagerName(normalizeText(request.getManagerName()));
    String status = request.getStatus() == null ? DiningConstants.STATUS_ENABLED : request.getStatus();
    validateStatus(status);
    zone.setStatus(status);
    zone.setRemark(normalizeText(request.getRemark()));
    zone.setCreatedBy(AuthContext.getStaffId());
    prepZoneMapper.insert(zone);
    return Result.ok(zone);
  }

  @PutMapping("/{id}")
  public Result<DiningPrepZone> update(@PathVariable Long id, @Valid @RequestBody DiningPrepZoneRequest request) {
    DiningPrepZone zone = getZoneInOrg(id, AuthContext.getOrgId());
    if (zone == null) {
      return Result.ok(null);
    }
    validateCapacity(request.getCapacity());
    zone.setZoneCode(request.getZoneCode().trim());
    zone.setZoneName(request.getZoneName().trim());
    zone.setKitchenArea(normalizeText(request.getKitchenArea()));
    zone.setCapacity(request.getCapacity());
    zone.setManagerName(normalizeText(request.getManagerName()));
    if (request.getStatus() != null) {
      validateStatus(request.getStatus());
      zone.setStatus(request.getStatus());
    }
    zone.setRemark(normalizeText(request.getRemark()));
    prepZoneMapper.updateById(zone);
    return Result.ok(zone);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    DiningPrepZone zone = getZoneInOrg(id, AuthContext.getOrgId());
    if (zone != null) {
      zone.setIsDeleted(1);
      prepZoneMapper.updateById(zone);
    }
    return Result.ok(null);
  }

  private DiningPrepZone getZoneInOrg(Long id, Long orgId) {
    return prepZoneMapper.selectOne(Wrappers.lambdaQuery(DiningPrepZone.class)
        .eq(DiningPrepZone::getId, id)
        .eq(DiningPrepZone::getIsDeleted, 0)
        .eq(orgId != null, DiningPrepZone::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private void validateStatus(String status) {
    if (status == null || !DiningConstants.ENABLE_DISABLE_STATUS_SET.contains(status)) {
      throw new IllegalArgumentException(DiningConstants.MSG_INVALID_ENABLE_STATUS);
    }
  }

  private void validateCapacity(Integer capacity) {
    if (capacity != null && capacity < 0) {
      throw new IllegalArgumentException("备餐能力不能小于0");
    }
  }

  private String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
