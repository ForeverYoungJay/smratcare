package com.zhiyangyun.care.life.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.life.entity.DiningDeliveryArea;
import com.zhiyangyun.care.life.mapper.DiningDeliveryAreaMapper;
import com.zhiyangyun.care.life.model.DiningConstants;
import com.zhiyangyun.care.life.model.DiningDeliveryAreaRequest;
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
@RequestMapping("/api/life/dining/delivery-area")
public class DiningDeliveryAreaController {
  private final DiningDeliveryAreaMapper deliveryAreaMapper;

  public DiningDeliveryAreaController(DiningDeliveryAreaMapper deliveryAreaMapper) {
    this.deliveryAreaMapper = deliveryAreaMapper;
  }

  @GetMapping("/page")
  public Result<IPage<DiningDeliveryArea>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(DiningDeliveryArea.class)
        .eq(DiningDeliveryArea::getIsDeleted, 0)
        .eq(orgId != null, DiningDeliveryArea::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), DiningDeliveryArea::getStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(DiningDeliveryArea::getAreaName, keyword)
          .or().like(DiningDeliveryArea::getAreaCode, keyword)
          .or().like(DiningDeliveryArea::getRoomScope, keyword)
          .or().like(DiningDeliveryArea::getManagerName, keyword));
    }
    wrapper.orderByDesc(DiningDeliveryArea::getUpdateTime);
    return Result.ok(deliveryAreaMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/list")
  public Result<List<DiningDeliveryArea>> list(@RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    return Result.ok(deliveryAreaMapper.selectList(Wrappers.lambdaQuery(DiningDeliveryArea.class)
        .eq(DiningDeliveryArea::getIsDeleted, 0)
        .eq(orgId != null, DiningDeliveryArea::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), DiningDeliveryArea::getStatus, status)
        .orderByAsc(DiningDeliveryArea::getAreaCode)));
  }

  @PostMapping
  public Result<DiningDeliveryArea> create(@Valid @RequestBody DiningDeliveryAreaRequest request) {
    Long orgId = AuthContext.getOrgId();
    DiningDeliveryArea area = new DiningDeliveryArea();
    area.setTenantId(orgId);
    area.setOrgId(orgId);
    area.setAreaCode(request.getAreaCode().trim());
    area.setAreaName(request.getAreaName().trim());
    area.setBuildingName(normalizeText(request.getBuildingName()));
    area.setFloorNo(normalizeText(request.getFloorNo()));
    area.setRoomScope(normalizeText(request.getRoomScope()));
    area.setManagerName(normalizeText(request.getManagerName()));
    String status = request.getStatus() == null ? DiningConstants.STATUS_ENABLED : request.getStatus();
    validateStatus(status);
    area.setStatus(status);
    area.setRemark(normalizeText(request.getRemark()));
    area.setCreatedBy(AuthContext.getStaffId());
    deliveryAreaMapper.insert(area);
    return Result.ok(area);
  }

  @PutMapping("/{id}")
  public Result<DiningDeliveryArea> update(@PathVariable Long id, @Valid @RequestBody DiningDeliveryAreaRequest request) {
    DiningDeliveryArea area = getAreaInOrg(id, AuthContext.getOrgId());
    if (area == null) {
      return Result.ok(null);
    }
    area.setAreaCode(request.getAreaCode().trim());
    area.setAreaName(request.getAreaName().trim());
    area.setBuildingName(normalizeText(request.getBuildingName()));
    area.setFloorNo(normalizeText(request.getFloorNo()));
    area.setRoomScope(normalizeText(request.getRoomScope()));
    area.setManagerName(normalizeText(request.getManagerName()));
    if (request.getStatus() != null) {
      validateStatus(request.getStatus());
      area.setStatus(request.getStatus());
    }
    area.setRemark(normalizeText(request.getRemark()));
    deliveryAreaMapper.updateById(area);
    return Result.ok(area);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    DiningDeliveryArea area = getAreaInOrg(id, AuthContext.getOrgId());
    if (area != null) {
      area.setIsDeleted(1);
      deliveryAreaMapper.updateById(area);
    }
    return Result.ok(null);
  }

  private DiningDeliveryArea getAreaInOrg(Long id, Long orgId) {
    return deliveryAreaMapper.selectOne(Wrappers.lambdaQuery(DiningDeliveryArea.class)
        .eq(DiningDeliveryArea::getId, id)
        .eq(DiningDeliveryArea::getIsDeleted, 0)
        .eq(orgId != null, DiningDeliveryArea::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private void validateStatus(String status) {
    if (status == null || !DiningConstants.ENABLE_DISABLE_STATUS_SET.contains(status)) {
      throw new IllegalArgumentException(DiningConstants.MSG_INVALID_ENABLE_STATUS);
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
