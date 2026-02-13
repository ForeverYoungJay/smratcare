package com.zhiyangyun.care.asset.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.asset.entity.Building;
import com.zhiyangyun.care.asset.mapper.BuildingMapper;
import com.zhiyangyun.care.asset.model.BuildingRequest;
import com.zhiyangyun.care.asset.model.BuildingResponse;
import com.zhiyangyun.care.asset.service.BuildingService;
import org.springframework.stereotype.Service;

@Service
public class BuildingServiceImpl implements BuildingService {
  private final BuildingMapper buildingMapper;

  public BuildingServiceImpl(BuildingMapper buildingMapper) {
    this.buildingMapper = buildingMapper;
  }

  @Override
  public BuildingResponse create(BuildingRequest request) {
    ensureNameUnique(null, request.getTenantId(), request.getName());
    Building building = new Building();
    building.setTenantId(request.getTenantId());
    building.setOrgId(request.getOrgId());
    building.setName(request.getName());
    building.setCode(request.getCode());
    building.setStatus(request.getStatus());
    building.setSortNo(request.getSortNo());
    building.setRemark(request.getRemark());
    building.setCreatedBy(request.getCreatedBy());
    buildingMapper.insert(building);
    return toResponse(building);
  }

  @Override
  public BuildingResponse update(Long id, BuildingRequest request) {
    Building building = buildingMapper.selectById(id);
    if (building == null) {
      return null;
    }
    if (!request.getTenantId().equals(building.getTenantId())) {
      throw new IllegalArgumentException("无权限访问该楼栋");
    }
    ensureNameUnique(id, request.getTenantId(), request.getName());
    building.setName(request.getName());
    building.setCode(request.getCode());
    building.setStatus(request.getStatus());
    building.setSortNo(request.getSortNo());
    building.setRemark(request.getRemark());
    buildingMapper.updateById(building);
    return toResponse(building);
  }

  @Override
  public BuildingResponse get(Long id) {
    Building building = buildingMapper.selectById(id);
    return building == null ? null : toResponse(building);
  }

  @Override
  public IPage<BuildingResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Integer status) {
    var wrapper = Wrappers.lambdaQuery(Building.class)
        .eq(Building::getIsDeleted, 0)
        .eq(tenantId != null, Building::getTenantId, tenantId);
    if (status != null) {
      wrapper.eq(Building::getStatus, status);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(Building::getName, keyword).or().like(Building::getCode, keyword));
    }
    IPage<Building> page = buildingMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  public java.util.List<BuildingResponse> list(Long tenantId) {
    var wrapper = Wrappers.lambdaQuery(Building.class)
        .eq(Building::getIsDeleted, 0)
        .eq(tenantId != null, Building::getTenantId, tenantId)
        .orderByAsc(Building::getSortNo)
        .orderByAsc(Building::getName);
    return buildingMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  public void delete(Long id, Long tenantId) {
    Building building = buildingMapper.selectById(id);
    if (building != null && tenantId != null && tenantId.equals(building.getTenantId())) {
      building.setIsDeleted(1);
      buildingMapper.updateById(building);
    }
  }

  private void ensureNameUnique(Long id, Long tenantId, String name) {
    if (tenantId == null || name == null || name.isBlank()) {
      return;
    }
    var wrapper = Wrappers.lambdaQuery(Building.class)
        .eq(Building::getIsDeleted, 0)
        .eq(Building::getTenantId, tenantId)
        .eq(Building::getName, name);
    if (id != null) {
      wrapper.ne(Building::getId, id);
    }
    if (buildingMapper.selectCount(wrapper) > 0) {
      throw new IllegalArgumentException("楼栋名称已存在");
    }
  }

  private BuildingResponse toResponse(Building building) {
    BuildingResponse response = new BuildingResponse();
    response.setId(building.getId());
    response.setTenantId(building.getTenantId());
    response.setOrgId(building.getOrgId());
    response.setName(building.getName());
    response.setCode(building.getCode());
    response.setStatus(building.getStatus());
    response.setSortNo(building.getSortNo());
    response.setRemark(building.getRemark());
    return response;
  }
}
