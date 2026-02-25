package com.zhiyangyun.care.baseconfig.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.baseconfig.entity.BaseDataItem;
import com.zhiyangyun.care.baseconfig.mapper.BaseDataItemMapper;
import com.zhiyangyun.care.baseconfig.model.BaseConfigGroup;
import com.zhiyangyun.care.baseconfig.model.BaseDataItemRequest;
import com.zhiyangyun.care.baseconfig.model.BaseDataItemResponse;
import com.zhiyangyun.care.baseconfig.service.BaseDataItemService;
import org.springframework.stereotype.Service;

@Service
public class BaseDataItemServiceImpl implements BaseDataItemService {
  private final BaseDataItemMapper baseDataItemMapper;

  public BaseDataItemServiceImpl(BaseDataItemMapper baseDataItemMapper) {
    this.baseDataItemMapper = baseDataItemMapper;
  }

  @Override
  public BaseDataItemResponse create(BaseDataItemRequest request) {
    validateConfigGroup(request.getConfigGroup());
    String code = normalizeCode(request.getItemCode());
    ensureUnique(null, request.getTenantId(), request.getConfigGroup(), code, request.getItemName());
    BaseDataItem item = new BaseDataItem();
    item.setTenantId(request.getTenantId());
    item.setOrgId(request.getOrgId());
    item.setConfigGroup(request.getConfigGroup());
    item.setItemCode(code);
    item.setItemName(request.getItemName());
    item.setStatus(request.getStatus());
    item.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
    item.setRemark(request.getRemark());
    item.setCreatedBy(request.getCreatedBy());
    baseDataItemMapper.insert(item);
    return toResponse(item);
  }

  @Override
  public BaseDataItemResponse update(Long id, BaseDataItemRequest request) {
    validateConfigGroup(request.getConfigGroup());
    BaseDataItem item = baseDataItemMapper.selectById(id);
    if (item == null || item.getIsDeleted() != null && item.getIsDeleted() == 1) {
      return null;
    }
    if (!request.getTenantId().equals(item.getTenantId())) {
      throw new IllegalArgumentException("无权限访问该配置项");
    }
    String code = normalizeCode(request.getItemCode());
    ensureUnique(id, request.getTenantId(), request.getConfigGroup(), code, request.getItemName());
    item.setConfigGroup(request.getConfigGroup());
    item.setItemCode(code);
    item.setItemName(request.getItemName());
    item.setStatus(request.getStatus());
    item.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
    item.setRemark(request.getRemark());
    baseDataItemMapper.updateById(item);
    return toResponse(item);
  }

  @Override
  public IPage<BaseDataItemResponse> page(Long tenantId, long pageNo, long pageSize, String configGroup, String keyword, Integer status) {
    var wrapper = Wrappers.lambdaQuery(BaseDataItem.class)
        .eq(BaseDataItem::getIsDeleted, 0)
        .eq(tenantId != null, BaseDataItem::getTenantId, tenantId)
        .eq(configGroup != null && !configGroup.isBlank(), BaseDataItem::getConfigGroup, configGroup)
        .eq(status != null, BaseDataItem::getStatus, status)
        .orderByAsc(BaseDataItem::getSortNo)
        .orderByAsc(BaseDataItem::getItemName);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(BaseDataItem::getItemName, keyword).or().like(BaseDataItem::getItemCode, keyword));
    }
    IPage<BaseDataItem> page = baseDataItemMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  public java.util.List<BaseDataItemResponse> list(Long tenantId, String configGroup, Integer status) {
    var wrapper = Wrappers.lambdaQuery(BaseDataItem.class)
        .eq(BaseDataItem::getIsDeleted, 0)
        .eq(tenantId != null, BaseDataItem::getTenantId, tenantId)
        .eq(configGroup != null && !configGroup.isBlank(), BaseDataItem::getConfigGroup, configGroup)
        .eq(status != null, BaseDataItem::getStatus, status)
        .orderByAsc(BaseDataItem::getSortNo)
        .orderByAsc(BaseDataItem::getItemName);
    return baseDataItemMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  public void delete(Long id, Long tenantId) {
    BaseDataItem item = baseDataItemMapper.selectById(id);
    if (item != null && tenantId != null && tenantId.equals(item.getTenantId())) {
      item.setIsDeleted(1);
      baseDataItemMapper.updateById(item);
    }
  }

  private void ensureUnique(Long id, Long tenantId, String configGroup, String itemCode, String itemName) {
    if (tenantId == null) {
      return;
    }
    var codeWrapper = Wrappers.lambdaQuery(BaseDataItem.class)
        .eq(BaseDataItem::getIsDeleted, 0)
        .eq(BaseDataItem::getTenantId, tenantId)
        .eq(BaseDataItem::getConfigGroup, configGroup)
        .eq(BaseDataItem::getItemCode, itemCode);
    if (id != null) {
      codeWrapper.ne(BaseDataItem::getId, id);
    }
    if (baseDataItemMapper.selectCount(codeWrapper) > 0) {
      throw new IllegalArgumentException("同分组下配置编码已存在");
    }

    var nameWrapper = Wrappers.lambdaQuery(BaseDataItem.class)
        .eq(BaseDataItem::getIsDeleted, 0)
        .eq(BaseDataItem::getTenantId, tenantId)
        .eq(BaseDataItem::getConfigGroup, configGroup)
        .eq(BaseDataItem::getItemName, itemName);
    if (id != null) {
      nameWrapper.ne(BaseDataItem::getId, id);
    }
    if (baseDataItemMapper.selectCount(nameWrapper) > 0) {
      throw new IllegalArgumentException("同分组下配置名称已存在");
    }
  }

  private String normalizeCode(String code) {
    return code == null ? null : code.trim().toUpperCase();
  }

  private void validateConfigGroup(String configGroup) {
    if (configGroup == null || configGroup.isBlank()) {
      throw new IllegalArgumentException("配置分组不能为空");
    }
    if (!BaseConfigGroup.codes().contains(configGroup.trim())) {
      throw new IllegalArgumentException("无效的配置分组");
    }
  }

  private BaseDataItemResponse toResponse(BaseDataItem item) {
    BaseDataItemResponse response = new BaseDataItemResponse();
    response.setId(item.getId());
    response.setConfigGroup(item.getConfigGroup());
    response.setItemCode(item.getItemCode());
    response.setItemName(item.getItemName());
    response.setStatus(item.getStatus());
    response.setSortNo(item.getSortNo());
    response.setRemark(item.getRemark());
    return response;
  }
}
