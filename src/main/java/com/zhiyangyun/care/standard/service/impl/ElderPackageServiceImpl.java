package com.zhiyangyun.care.standard.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.standard.entity.CarePackage;
import com.zhiyangyun.care.standard.entity.ElderPackage;
import com.zhiyangyun.care.standard.mapper.CarePackageMapper;
import com.zhiyangyun.care.standard.mapper.ElderPackageMapper;
import com.zhiyangyun.care.standard.model.ElderPackageRequest;
import com.zhiyangyun.care.standard.model.ElderPackageResponse;
import com.zhiyangyun.care.standard.service.ElderPackageService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ElderPackageServiceImpl implements ElderPackageService {
  private final ElderPackageMapper elderPackageMapper;
  private final ElderMapper elderMapper;
  private final CarePackageMapper carePackageMapper;

  public ElderPackageServiceImpl(ElderPackageMapper elderPackageMapper,
                                 ElderMapper elderMapper,
                                 CarePackageMapper carePackageMapper) {
    this.elderPackageMapper = elderPackageMapper;
    this.elderMapper = elderMapper;
    this.carePackageMapper = carePackageMapper;
  }

  @Override
  public ElderPackageResponse create(ElderPackageRequest request) {
    ElderPackage elderPackage = new ElderPackage();
    elderPackage.setTenantId(request.getTenantId());
    elderPackage.setOrgId(request.getOrgId());
    elderPackage.setElderId(request.getElderId());
    elderPackage.setPackageId(request.getPackageId());
    elderPackage.setStartDate(request.getStartDate());
    elderPackage.setEndDate(request.getEndDate());
    elderPackage.setStatus(request.getStatus());
    elderPackage.setRemark(request.getRemark());
    elderPackage.setCreatedBy(request.getCreatedBy());
    elderPackageMapper.insert(elderPackage);
    return toResponse(elderPackage, null, null);
  }

  @Override
  public ElderPackageResponse update(Long id, ElderPackageRequest request) {
    ElderPackage elderPackage = elderPackageMapper.selectById(id);
    if (elderPackage == null || !request.getTenantId().equals(elderPackage.getTenantId())) {
      return null;
    }
    elderPackage.setPackageId(request.getPackageId());
    elderPackage.setStartDate(request.getStartDate());
    elderPackage.setEndDate(request.getEndDate());
    elderPackage.setStatus(request.getStatus());
    elderPackage.setRemark(request.getRemark());
    elderPackageMapper.updateById(elderPackage);
    return toResponse(elderPackage, null, null);
  }

  @Override
  public IPage<ElderPackageResponse> page(Long tenantId, long pageNo, long pageSize, Long elderId, Integer status) {
    var wrapper = Wrappers.lambdaQuery(ElderPackage.class)
        .eq(ElderPackage::getIsDeleted, 0)
        .eq(tenantId != null, ElderPackage::getTenantId, tenantId)
        .eq(elderId != null, ElderPackage::getElderId, elderId)
        .eq(status != null, ElderPackage::getStatus, status)
        .orderByDesc(ElderPackage::getCreateTime);
    IPage<ElderPackage> page = elderPackageMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);

    List<Long> elderIds = page.getRecords().stream()
        .map(ElderPackage::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(elderIds)
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, v -> v));
    List<Long> packageIds = page.getRecords().stream()
        .map(ElderPackage::getPackageId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, CarePackage> packageMap = packageIds.isEmpty()
        ? Map.of()
        : carePackageMapper.selectBatchIds(packageIds)
            .stream()
            .collect(Collectors.toMap(CarePackage::getId, v -> v));

    return page.convert(item -> toResponse(item, elderMap.get(item.getElderId()), packageMap.get(item.getPackageId())));
  }

  @Override
  public List<ElderPackageResponse> list(Long tenantId, Long elderId) {
    var wrapper = Wrappers.lambdaQuery(ElderPackage.class)
        .eq(ElderPackage::getIsDeleted, 0)
        .eq(tenantId != null, ElderPackage::getTenantId, tenantId)
        .eq(elderId != null, ElderPackage::getElderId, elderId)
        .orderByDesc(ElderPackage::getCreateTime);
    List<ElderPackage> list = elderPackageMapper.selectList(wrapper);

    List<Long> elderIds = list.stream()
        .map(ElderPackage::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(elderIds)
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, v -> v));
    List<Long> packageIds = list.stream()
        .map(ElderPackage::getPackageId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, CarePackage> packageMap = packageIds.isEmpty()
        ? Map.of()
        : carePackageMapper.selectBatchIds(packageIds)
            .stream()
            .collect(Collectors.toMap(CarePackage::getId, v -> v));

    return list.stream().map(item -> toResponse(item, elderMap.get(item.getElderId()), packageMap.get(item.getPackageId()))).toList();
  }

  @Override
  public void delete(Long id, Long tenantId) {
    ElderPackage elderPackage = elderPackageMapper.selectById(id);
    if (elderPackage != null && tenantId != null && tenantId.equals(elderPackage.getTenantId())) {
      elderPackage.setIsDeleted(1);
      elderPackageMapper.updateById(elderPackage);
    }
  }

  private ElderPackageResponse toResponse(ElderPackage item, ElderProfile elder, CarePackage pack) {
    ElderPackageResponse response = new ElderPackageResponse();
    response.setId(item.getId());
    response.setTenantId(item.getTenantId());
    response.setOrgId(item.getOrgId());
    response.setElderId(item.getElderId());
    response.setElderName(elder == null ? null : elder.getFullName());
    response.setPackageId(item.getPackageId());
    response.setPackageName(pack == null ? null : pack.getName());
    response.setStartDate(item.getStartDate());
    response.setEndDate(item.getEndDate());
    response.setStatus(item.getStatus());
    response.setRemark(item.getRemark());
    return response;
  }
}
