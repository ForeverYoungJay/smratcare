package com.zhiyangyun.care.standard.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.standard.entity.ServiceItem;
import com.zhiyangyun.care.standard.mapper.ServiceItemMapper;
import com.zhiyangyun.care.standard.model.ServiceItemRequest;
import com.zhiyangyun.care.standard.model.ServiceItemResponse;
import com.zhiyangyun.care.standard.service.ServiceItemService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ServiceItemServiceImpl implements ServiceItemService {
  private final ServiceItemMapper itemMapper;

  public ServiceItemServiceImpl(ServiceItemMapper itemMapper) {
    this.itemMapper = itemMapper;
  }

  @Override
  public ServiceItemResponse create(ServiceItemRequest request) {
    ServiceItem item = new ServiceItem();
    item.setTenantId(request.getTenantId());
    item.setOrgId(request.getOrgId());
    item.setName(request.getName());
    item.setCategory(request.getCategory());
    item.setDefaultDuration(request.getDefaultDuration());
    item.setDefaultPoints(request.getDefaultPoints());
    item.setEnabled(request.getEnabled());
    item.setRemark(request.getRemark());
    item.setCreatedBy(request.getCreatedBy());
    itemMapper.insert(item);
    return toResponse(item);
  }

  @Override
  public ServiceItemResponse update(Long id, ServiceItemRequest request) {
    ServiceItem item = itemMapper.selectById(id);
    if (item == null || !request.getTenantId().equals(item.getTenantId())) {
      return null;
    }
    item.setName(request.getName());
    item.setCategory(request.getCategory());
    item.setDefaultDuration(request.getDefaultDuration());
    item.setDefaultPoints(request.getDefaultPoints());
    item.setEnabled(request.getEnabled());
    item.setRemark(request.getRemark());
    itemMapper.updateById(item);
    return toResponse(item);
  }

  @Override
  public ServiceItemResponse get(Long id, Long tenantId) {
    ServiceItem item = itemMapper.selectById(id);
    if (item == null || (tenantId != null && !tenantId.equals(item.getTenantId()))) {
      return null;
    }
    return toResponse(item);
  }

  @Override
  public IPage<ServiceItemResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Integer enabled) {
    var wrapper = Wrappers.lambdaQuery(ServiceItem.class)
        .eq(ServiceItem::getIsDeleted, 0)
        .eq(tenantId != null, ServiceItem::getTenantId, tenantId);
    if (enabled != null) {
      wrapper.eq(ServiceItem::getEnabled, enabled);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(ServiceItem::getName, keyword).or().like(ServiceItem::getCategory, keyword));
    }
    IPage<ServiceItem> page = itemMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  public List<ServiceItemResponse> list(Long tenantId, Integer enabled) {
    var wrapper = Wrappers.lambdaQuery(ServiceItem.class)
        .eq(ServiceItem::getIsDeleted, 0)
        .eq(tenantId != null, ServiceItem::getTenantId, tenantId)
        .eq(enabled != null, ServiceItem::getEnabled, enabled)
        .orderByAsc(ServiceItem::getName);
    return itemMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  public void delete(Long id, Long tenantId) {
    ServiceItem item = itemMapper.selectById(id);
    if (item != null && tenantId != null && tenantId.equals(item.getTenantId())) {
      item.setIsDeleted(1);
      itemMapper.updateById(item);
    }
  }

  private ServiceItemResponse toResponse(ServiceItem item) {
    ServiceItemResponse response = new ServiceItemResponse();
    response.setId(item.getId());
    response.setTenantId(item.getTenantId());
    response.setOrgId(item.getOrgId());
    response.setName(item.getName());
    response.setCategory(item.getCategory());
    response.setDefaultDuration(item.getDefaultDuration());
    response.setDefaultPoints(item.getDefaultPoints());
    response.setEnabled(item.getEnabled());
    response.setRemark(item.getRemark());
    return response;
  }
}
