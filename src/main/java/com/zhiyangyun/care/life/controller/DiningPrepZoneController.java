package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.DiningPrepZone;
import com.zhiyangyun.care.life.mapper.DiningPrepZoneMapper;
import com.zhiyangyun.care.life.model.DiningPrepZoneRequest;
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

  @PostMapping
  public Result<DiningPrepZone> create(@Valid @RequestBody DiningPrepZoneRequest request) {
    Long orgId = AuthContext.getOrgId();
    DiningPrepZone zone = new DiningPrepZone();
    zone.setTenantId(orgId);
    zone.setOrgId(orgId);
    zone.setZoneCode(request.getZoneCode());
    zone.setZoneName(request.getZoneName());
    zone.setKitchenArea(request.getKitchenArea());
    zone.setCapacity(request.getCapacity());
    zone.setManagerName(request.getManagerName());
    zone.setStatus(request.getStatus() == null ? "ENABLED" : request.getStatus());
    zone.setRemark(request.getRemark());
    zone.setCreatedBy(AuthContext.getStaffId());
    prepZoneMapper.insert(zone);
    return Result.ok(zone);
  }

  @PutMapping("/{id}")
  public Result<DiningPrepZone> update(@PathVariable Long id, @Valid @RequestBody DiningPrepZoneRequest request) {
    DiningPrepZone zone = prepZoneMapper.selectById(id);
    if (zone == null || zone.getIsDeleted() != null && zone.getIsDeleted() == 1) {
      return Result.ok(null);
    }
    zone.setZoneCode(request.getZoneCode());
    zone.setZoneName(request.getZoneName());
    zone.setKitchenArea(request.getKitchenArea());
    zone.setCapacity(request.getCapacity());
    zone.setManagerName(request.getManagerName());
    zone.setStatus(request.getStatus() == null ? zone.getStatus() : request.getStatus());
    zone.setRemark(request.getRemark());
    prepZoneMapper.updateById(zone);
    return Result.ok(zone);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    DiningPrepZone zone = prepZoneMapper.selectById(id);
    if (zone != null) {
      zone.setIsDeleted(1);
      prepZoneMapper.updateById(zone);
    }
    return Result.ok(null);
  }
}
