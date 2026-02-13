package com.zhiyangyun.care.standard.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.standard.entity.CarePackage;
import com.zhiyangyun.care.standard.mapper.CarePackageMapper;
import com.zhiyangyun.care.standard.model.CarePackageRequest;
import com.zhiyangyun.care.standard.model.CarePackageResponse;
import com.zhiyangyun.care.standard.service.CarePackageService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CarePackageServiceImpl implements CarePackageService {
  private final CarePackageMapper packageMapper;

  public CarePackageServiceImpl(CarePackageMapper packageMapper) {
    this.packageMapper = packageMapper;
  }

  @Override
  public CarePackageResponse create(CarePackageRequest request) {
    CarePackage pack = new CarePackage();
    pack.setTenantId(request.getTenantId());
    pack.setOrgId(request.getOrgId());
    pack.setName(request.getName());
    pack.setCareLevel(request.getCareLevel());
    pack.setCycleDays(request.getCycleDays());
    pack.setEnabled(request.getEnabled());
    pack.setRemark(request.getRemark());
    pack.setCreatedBy(request.getCreatedBy());
    packageMapper.insert(pack);
    return toResponse(pack);
  }

  @Override
  public CarePackageResponse update(Long id, CarePackageRequest request) {
    CarePackage pack = packageMapper.selectById(id);
    if (pack == null || !request.getTenantId().equals(pack.getTenantId())) {
      return null;
    }
    pack.setName(request.getName());
    pack.setCareLevel(request.getCareLevel());
    pack.setCycleDays(request.getCycleDays());
    pack.setEnabled(request.getEnabled());
    pack.setRemark(request.getRemark());
    packageMapper.updateById(pack);
    return toResponse(pack);
  }

  @Override
  public CarePackageResponse get(Long id, Long tenantId) {
    CarePackage pack = packageMapper.selectById(id);
    if (pack == null || (tenantId != null && !tenantId.equals(pack.getTenantId()))) {
      return null;
    }
    return toResponse(pack);
  }

  @Override
  public IPage<CarePackageResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Integer enabled) {
    var wrapper = Wrappers.lambdaQuery(CarePackage.class)
        .eq(CarePackage::getIsDeleted, 0)
        .eq(tenantId != null, CarePackage::getTenantId, tenantId);
    if (enabled != null) {
      wrapper.eq(CarePackage::getEnabled, enabled);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(CarePackage::getName, keyword).or().like(CarePackage::getCareLevel, keyword));
    }
    IPage<CarePackage> page = packageMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  public List<CarePackageResponse> list(Long tenantId, Integer enabled) {
    var wrapper = Wrappers.lambdaQuery(CarePackage.class)
        .eq(CarePackage::getIsDeleted, 0)
        .eq(tenantId != null, CarePackage::getTenantId, tenantId)
        .eq(enabled != null, CarePackage::getEnabled, enabled)
        .orderByAsc(CarePackage::getName);
    return packageMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  public void delete(Long id, Long tenantId) {
    CarePackage pack = packageMapper.selectById(id);
    if (pack != null && tenantId != null && tenantId.equals(pack.getTenantId())) {
      pack.setIsDeleted(1);
      packageMapper.updateById(pack);
    }
  }

  private CarePackageResponse toResponse(CarePackage pack) {
    CarePackageResponse response = new CarePackageResponse();
    response.setId(pack.getId());
    response.setTenantId(pack.getTenantId());
    response.setOrgId(pack.getOrgId());
    response.setName(pack.getName());
    response.setCareLevel(pack.getCareLevel());
    response.setCycleDays(pack.getCycleDays());
    response.setEnabled(pack.getEnabled());
    response.setRemark(pack.getRemark());
    return response;
  }
}
