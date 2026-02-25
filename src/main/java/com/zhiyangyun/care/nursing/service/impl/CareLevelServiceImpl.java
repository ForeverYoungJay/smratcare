package com.zhiyangyun.care.nursing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.nursing.entity.CareLevel;
import com.zhiyangyun.care.nursing.mapper.CareLevelMapper;
import com.zhiyangyun.care.nursing.model.CareLevelRequest;
import com.zhiyangyun.care.nursing.model.CareLevelResponse;
import com.zhiyangyun.care.nursing.service.CareLevelService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CareLevelServiceImpl implements CareLevelService {
  private final CareLevelMapper careLevelMapper;

  public CareLevelServiceImpl(CareLevelMapper careLevelMapper) {
    this.careLevelMapper = careLevelMapper;
  }

  @Override
  public CareLevelResponse create(CareLevelRequest request) {
    CareLevel level = new CareLevel();
    fillEntity(level, request);
    level.setCreatedBy(request.getCreatedBy());
    careLevelMapper.insert(level);
    return toResponse(level);
  }

  @Override
  public CareLevelResponse update(Long id, CareLevelRequest request) {
    CareLevel level = careLevelMapper.selectById(id);
    if (level == null || !request.getTenantId().equals(level.getTenantId())) {
      return null;
    }
    fillEntity(level, request);
    careLevelMapper.updateById(level);
    return toResponse(level);
  }

  @Override
  public CareLevelResponse get(Long id, Long tenantId) {
    CareLevel level = careLevelMapper.selectById(id);
    if (level == null || (tenantId != null && !tenantId.equals(level.getTenantId()))) {
      return null;
    }
    return toResponse(level);
  }

  @Override
  public IPage<CareLevelResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Integer enabled) {
    var wrapper = Wrappers.lambdaQuery(CareLevel.class)
        .eq(CareLevel::getIsDeleted, 0)
        .eq(tenantId != null, CareLevel::getTenantId, tenantId)
        .eq(enabled != null, CareLevel::getEnabled, enabled)
        .orderByAsc(CareLevel::getSeverity)
        .orderByAsc(CareLevel::getLevelCode);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(CareLevel::getLevelCode, keyword).or().like(CareLevel::getLevelName, keyword));
    }
    return careLevelMapper.selectPage(new Page<>(pageNo, pageSize), wrapper).convert(this::toResponse);
  }

  @Override
  public List<CareLevelResponse> list(Long tenantId, Integer enabled) {
    var wrapper = Wrappers.lambdaQuery(CareLevel.class)
        .eq(CareLevel::getIsDeleted, 0)
        .eq(tenantId != null, CareLevel::getTenantId, tenantId)
        .eq(enabled != null, CareLevel::getEnabled, enabled)
        .orderByAsc(CareLevel::getSeverity)
        .orderByAsc(CareLevel::getLevelCode);
    return careLevelMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  public void delete(Long id, Long tenantId) {
    CareLevel level = careLevelMapper.selectById(id);
    if (level != null && tenantId != null && tenantId.equals(level.getTenantId())) {
      level.setIsDeleted(1);
      careLevelMapper.updateById(level);
    }
  }

  private void fillEntity(CareLevel level, CareLevelRequest request) {
    level.setTenantId(request.getTenantId());
    level.setOrgId(request.getOrgId());
    level.setLevelCode(request.getLevelCode());
    level.setLevelName(request.getLevelName());
    level.setSeverity(request.getSeverity() == null ? 1 : request.getSeverity());
    level.setMonthlyFee(request.getMonthlyFee());
    level.setEnabled(request.getEnabled() == null ? 1 : request.getEnabled());
    level.setRemark(request.getRemark());
  }

  private CareLevelResponse toResponse(CareLevel level) {
    CareLevelResponse response = new CareLevelResponse();
    response.setId(level.getId());
    response.setTenantId(level.getTenantId());
    response.setOrgId(level.getOrgId());
    response.setLevelCode(level.getLevelCode());
    response.setLevelName(level.getLevelName());
    response.setSeverity(level.getSeverity());
    response.setMonthlyFee(level.getMonthlyFee());
    response.setEnabled(level.getEnabled());
    response.setRemark(level.getRemark());
    return response;
  }
}
