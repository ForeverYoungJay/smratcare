package com.zhiyangyun.care.standard.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.standard.entity.CarePackageItem;
import com.zhiyangyun.care.standard.entity.ServiceItem;
import com.zhiyangyun.care.standard.mapper.CarePackageItemMapper;
import com.zhiyangyun.care.standard.mapper.ServiceItemMapper;
import com.zhiyangyun.care.standard.model.CarePackageItemRequest;
import com.zhiyangyun.care.standard.model.CarePackageItemResponse;
import com.zhiyangyun.care.standard.service.CarePackageItemService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CarePackageItemServiceImpl implements CarePackageItemService {
  private final CarePackageItemMapper itemMapper;
  private final ServiceItemMapper serviceItemMapper;

  public CarePackageItemServiceImpl(CarePackageItemMapper itemMapper, ServiceItemMapper serviceItemMapper) {
    this.itemMapper = itemMapper;
    this.serviceItemMapper = serviceItemMapper;
  }

  @Override
  public CarePackageItemResponse create(CarePackageItemRequest request) {
    CarePackageItem item = new CarePackageItem();
    item.setTenantId(request.getTenantId());
    item.setOrgId(request.getOrgId());
    item.setPackageId(request.getPackageId());
    item.setItemId(request.getItemId());
    item.setFrequencyPerDay(request.getFrequencyPerDay());
    item.setEnabled(request.getEnabled());
    item.setSortNo(request.getSortNo());
    item.setRemark(request.getRemark());
    item.setCreatedBy(request.getCreatedBy());
    itemMapper.insert(item);
    return toResponse(item, null);
  }

  @Override
  public CarePackageItemResponse update(Long id, CarePackageItemRequest request) {
    CarePackageItem item = itemMapper.selectById(id);
    if (item == null || !request.getTenantId().equals(item.getTenantId())) {
      return null;
    }
    item.setItemId(request.getItemId());
    item.setFrequencyPerDay(request.getFrequencyPerDay());
    item.setEnabled(request.getEnabled());
    item.setSortNo(request.getSortNo());
    item.setRemark(request.getRemark());
    itemMapper.updateById(item);
    return toResponse(item, null);
  }

  @Override
  public IPage<CarePackageItemResponse> page(Long tenantId, long pageNo, long pageSize, Long packageId) {
    var wrapper = Wrappers.lambdaQuery(CarePackageItem.class)
        .eq(CarePackageItem::getIsDeleted, 0)
        .eq(tenantId != null, CarePackageItem::getTenantId, tenantId)
        .eq(packageId != null, CarePackageItem::getPackageId, packageId)
        .orderByAsc(CarePackageItem::getSortNo);
    IPage<CarePackageItem> page = itemMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> itemIds = page.getRecords().stream()
        .map(CarePackageItem::getItemId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, ServiceItem> itemMap = itemIds.isEmpty()
        ? Map.of()
        : serviceItemMapper.selectBatchIds(itemIds)
            .stream()
            .collect(Collectors.toMap(ServiceItem::getId, v -> v));
    return page.convert(it -> toResponse(it, itemMap.get(it.getItemId())));
  }

  @Override
  public List<CarePackageItemResponse> list(Long tenantId, Long packageId) {
    var wrapper = Wrappers.lambdaQuery(CarePackageItem.class)
        .eq(CarePackageItem::getIsDeleted, 0)
        .eq(tenantId != null, CarePackageItem::getTenantId, tenantId)
        .eq(packageId != null, CarePackageItem::getPackageId, packageId)
        .orderByAsc(CarePackageItem::getSortNo);
    List<CarePackageItem> items = itemMapper.selectList(wrapper);
    List<Long> itemIds = items.stream()
        .map(CarePackageItem::getItemId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, ServiceItem> itemMap = itemIds.isEmpty()
        ? Map.of()
        : serviceItemMapper.selectBatchIds(itemIds)
            .stream()
            .collect(Collectors.toMap(ServiceItem::getId, v -> v));
    return items.stream().map(it -> toResponse(it, itemMap.get(it.getItemId()))).toList();
  }

  @Override
  public void delete(Long id, Long tenantId) {
    CarePackageItem item = itemMapper.selectById(id);
    if (item != null && tenantId != null && tenantId.equals(item.getTenantId())) {
      item.setIsDeleted(1);
      itemMapper.updateById(item);
    }
  }

  private CarePackageItemResponse toResponse(CarePackageItem item, ServiceItem serviceItem) {
    CarePackageItemResponse response = new CarePackageItemResponse();
    response.setId(item.getId());
    response.setTenantId(item.getTenantId());
    response.setOrgId(item.getOrgId());
    response.setPackageId(item.getPackageId());
    response.setItemId(item.getItemId());
    response.setFrequencyPerDay(item.getFrequencyPerDay());
    response.setEnabled(item.getEnabled());
    response.setSortNo(item.getSortNo());
    response.setRemark(item.getRemark());
    response.setItemName(serviceItem == null ? null : serviceItem.getName());
    return response;
  }
}
