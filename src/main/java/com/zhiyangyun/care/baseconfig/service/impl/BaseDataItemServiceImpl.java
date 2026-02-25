package com.zhiyangyun.care.baseconfig.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.baseconfig.entity.BaseDataItem;
import com.zhiyangyun.care.baseconfig.mapper.BaseDataItemMapper;
import com.zhiyangyun.care.baseconfig.model.BaseConfigGroup;
import com.zhiyangyun.care.baseconfig.model.BaseDataImportErrorItem;
import com.zhiyangyun.care.baseconfig.model.BaseDataImportItem;
import com.zhiyangyun.care.baseconfig.model.BaseDataImportRequest;
import com.zhiyangyun.care.baseconfig.model.BaseDataImportResult;
import com.zhiyangyun.care.baseconfig.model.BaseDataItemRequest;
import com.zhiyangyun.care.baseconfig.model.BaseDataItemResponse;
import com.zhiyangyun.care.baseconfig.service.BaseDataItemService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class BaseDataItemServiceImpl implements BaseDataItemService {
  private final BaseDataItemMapper baseDataItemMapper;

  public BaseDataItemServiceImpl(BaseDataItemMapper baseDataItemMapper) {
    this.baseDataItemMapper = baseDataItemMapper;
  }

  @Override
  public BaseDataItemResponse create(BaseDataItemRequest request) {
    SanitizedRequest sanitized = sanitizeRequest(request);
    ensureUnique(null, request.getTenantId(), sanitized.configGroup(), sanitized.itemCode(), sanitized.itemName());
    BaseDataItem item = new BaseDataItem();
    item.setTenantId(request.getTenantId());
    item.setOrgId(request.getOrgId());
    item.setConfigGroup(sanitized.configGroup());
    item.setItemCode(sanitized.itemCode());
    item.setItemName(sanitized.itemName());
    item.setStatus(sanitized.status());
    item.setSortNo(sanitized.sortNo());
    item.setRemark(sanitized.remark());
    item.setCreatedBy(request.getCreatedBy());
    baseDataItemMapper.insert(item);
    return toResponse(item);
  }

  @Override
  public BaseDataItemResponse update(Long id, BaseDataItemRequest request) {
    SanitizedRequest sanitized = sanitizeRequest(request);
    BaseDataItem item = baseDataItemMapper.selectById(id);
    if (item == null || item.getIsDeleted() != null && item.getIsDeleted() == 1) {
      throw new IllegalArgumentException("配置项不存在");
    }
    if (!request.getTenantId().equals(item.getTenantId())) {
      throw new IllegalArgumentException("无权限访问该配置项");
    }
    ensureUnique(id, request.getTenantId(), sanitized.configGroup(), sanitized.itemCode(), sanitized.itemName());
    item.setConfigGroup(sanitized.configGroup());
    item.setItemCode(sanitized.itemCode());
    item.setItemName(sanitized.itemName());
    item.setStatus(sanitized.status());
    item.setSortNo(sanitized.sortNo());
    item.setRemark(sanitized.remark());
    baseDataItemMapper.updateById(item);
    return toResponse(item);
  }

  @Override
  public IPage<BaseDataItemResponse> page(Long tenantId, long pageNo, long pageSize, String configGroup, String keyword, Integer status) {
    String normalizedConfigGroup = trimToNull(configGroup);
    String normalizedKeyword = trimToNull(keyword);
    var wrapper = Wrappers.lambdaQuery(BaseDataItem.class)
        .eq(BaseDataItem::getIsDeleted, 0)
        .eq(tenantId != null, BaseDataItem::getTenantId, tenantId)
        .eq(normalizedConfigGroup != null, BaseDataItem::getConfigGroup, normalizedConfigGroup)
        .eq(status != null, BaseDataItem::getStatus, status)
        .orderByAsc(BaseDataItem::getSortNo)
        .orderByAsc(BaseDataItem::getItemName);
    if (normalizedKeyword != null) {
      wrapper.and(w -> w.like(BaseDataItem::getItemName, normalizedKeyword).or().like(BaseDataItem::getItemCode, normalizedKeyword));
    }
    IPage<BaseDataItem> page = baseDataItemMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  public java.util.List<BaseDataItemResponse> list(Long tenantId, String configGroup, Integer status) {
    return list(tenantId, configGroup, null, status);
  }

  @Override
  public java.util.List<BaseDataItemResponse> list(Long tenantId, String configGroup, String keyword, Integer status) {
    String normalizedConfigGroup = trimToNull(configGroup);
    String normalizedKeyword = trimToNull(keyword);
    var wrapper = Wrappers.lambdaQuery(BaseDataItem.class)
        .eq(BaseDataItem::getIsDeleted, 0)
        .eq(tenantId != null, BaseDataItem::getTenantId, tenantId)
        .eq(normalizedConfigGroup != null, BaseDataItem::getConfigGroup, normalizedConfigGroup)
        .eq(status != null, BaseDataItem::getStatus, status)
        .orderByAsc(BaseDataItem::getSortNo)
        .orderByAsc(BaseDataItem::getItemName);
    if (normalizedKeyword != null) {
      wrapper.and(w -> w.like(BaseDataItem::getItemName, normalizedKeyword).or().like(BaseDataItem::getItemCode, normalizedKeyword));
    }
    return baseDataItemMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  public int batchUpdateStatus(Long tenantId, List<Long> ids, Integer status) {
    if (status == null || (status != 0 && status != 1)) {
      throw new IllegalArgumentException("状态仅支持0或1");
    }
    List<Long> sanitizedIds = sanitizeIds(ids);
    if (tenantId == null || sanitizedIds.isEmpty()) {
      return 0;
    }
    List<BaseDataItem> items = baseDataItemMapper.selectList(Wrappers.lambdaQuery(BaseDataItem.class)
        .eq(BaseDataItem::getIsDeleted, 0)
        .eq(BaseDataItem::getTenantId, tenantId)
        .in(BaseDataItem::getId, sanitizedIds));
    int updatedCount = 0;
    for (BaseDataItem item : items) {
      if (status.equals(item.getStatus())) {
        continue;
      }
      item.setStatus(status);
      baseDataItemMapper.updateById(item);
      updatedCount++;
    }
    return updatedCount;
  }

  @Override
  public BaseDataImportResult previewImport(Long tenantId, Long orgId, Long staffId, BaseDataImportRequest request) {
    return processImport(tenantId, orgId, staffId, request, true);
  }

  @Override
  public BaseDataImportResult importItems(Long tenantId, Long orgId, Long staffId, BaseDataImportRequest request) {
    return processImport(tenantId, orgId, staffId, request, false);
  }

  private BaseDataImportResult processImport(
      Long tenantId,
      Long orgId,
      Long staffId,
      BaseDataImportRequest request,
      boolean preview) {
    String normalizedGroup = trimToNull(request.getConfigGroup());
    validateConfigGroup(normalizedGroup);
    List<BaseDataImportItem> items = request.getItems() == null ? List.of() : request.getItems();
    List<String> errors = new ArrayList<>();
    List<BaseDataImportErrorItem> errorItems = new ArrayList<>();
    Set<String> inputCodes = new HashSet<>();
    Set<String> inputNames = new HashSet<>();
    int successCount = 0;
    int createCount = 0;
    int updateCount = 0;
    for (int index = 0; index < items.size(); index++) {
      BaseDataImportItem row = items.get(index);
      try {
        BaseDataItemRequest rowRequest = new BaseDataItemRequest();
        rowRequest.setTenantId(tenantId);
        rowRequest.setOrgId(orgId);
        rowRequest.setCreatedBy(staffId);
        rowRequest.setConfigGroup(normalizedGroup);
        rowRequest.setItemCode(row == null ? null : row.getItemCode());
        rowRequest.setItemName(row == null ? null : row.getItemName());
        rowRequest.setStatus(row == null || row.getStatus() == null ? 1 : row.getStatus());
        rowRequest.setSortNo(row == null ? null : row.getSortNo());
        rowRequest.setRemark(row == null ? null : row.getRemark());
        SanitizedRequest sanitized = sanitizeRequest(rowRequest);
        if (!inputCodes.add(sanitized.itemCode())) {
          throw new IllegalArgumentException("导入文件中配置编码重复");
        }
        if (!inputNames.add(sanitized.itemName())) {
          throw new IllegalArgumentException("导入文件中配置名称重复");
        }
        BaseDataItem existing = baseDataItemMapper.selectOne(Wrappers.lambdaQuery(BaseDataItem.class)
            .eq(BaseDataItem::getIsDeleted, 0)
            .eq(BaseDataItem::getTenantId, tenantId)
            .eq(BaseDataItem::getConfigGroup, normalizedGroup)
            .eq(BaseDataItem::getItemCode, sanitized.itemCode())
            .last("LIMIT 1"));
        if (existing == null && preview) {
          ensureUnique(null, tenantId, sanitized.configGroup(), sanitized.itemCode(), sanitized.itemName());
          createCount++;
        } else if (existing == null) {
          create(rowRequest);
          createCount++;
        } else if (preview) {
          ensureUnique(existing.getId(), tenantId, sanitized.configGroup(), sanitized.itemCode(), sanitized.itemName());
          updateCount++;
        } else {
          update(existing.getId(), rowRequest);
          updateCount++;
        }
        successCount++;
      } catch (Exception ex) {
        String errorMessage = "第" + (index + 2) + "行：" + ex.getMessage();
        errors.add(errorMessage);
        BaseDataImportErrorItem errorItem = new BaseDataImportErrorItem();
        errorItem.setRowNo(index + 2);
        errorItem.setItemCode(row == null ? null : row.getItemCode());
        errorItem.setItemName(row == null ? null : row.getItemName());
        errorItem.setStatus(row == null ? null : row.getStatus());
        errorItem.setSortNo(row == null ? null : row.getSortNo());
        errorItem.setRemark(row == null ? null : row.getRemark());
        errorItem.setErrorMessage(ex.getMessage());
        errorItems.add(errorItem);
      }
    }
    BaseDataImportResult result = new BaseDataImportResult();
    result.setPreview(preview);
    result.setTotalCount(items.size());
    result.setSuccessCount(successCount);
    result.setCreateCount(createCount);
    result.setUpdateCount(updateCount);
    result.setFailCount(items.size() - successCount);
    result.setErrors(errors);
    result.setErrorItems(errorItems);
    return result;
  }

  @Override
  public void delete(Long id, Long tenantId) {
    BaseDataItem item = baseDataItemMapper.selectById(id);
    if (item == null || item.getIsDeleted() != null && item.getIsDeleted() == 1) {
      throw new IllegalArgumentException("配置项不存在");
    }
    if (tenantId == null || !tenantId.equals(item.getTenantId())) {
      throw new IllegalArgumentException("无权限访问该配置项");
    }
    item.setIsDeleted(1);
    baseDataItemMapper.updateById(item);
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
    String normalized = trimToNull(code);
    return normalized == null ? null : normalized.toUpperCase();
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
    response.setConfigGroupLabel(BaseConfigGroup.fromCode(item.getConfigGroup()).map(BaseConfigGroup::getLabel).orElse(item.getConfigGroup()));
    response.setItemCode(item.getItemCode());
    response.setItemName(item.getItemName());
    response.setStatus(item.getStatus());
    response.setSortNo(item.getSortNo());
    response.setRemark(item.getRemark());
    return response;
  }

  private SanitizedRequest sanitizeRequest(BaseDataItemRequest request) {
    String configGroup = trimToNull(request.getConfigGroup());
    validateConfigGroup(configGroup);
    String itemCode = normalizeCode(request.getItemCode());
    if (itemCode == null) {
      throw new IllegalArgumentException("配置编码不能为空");
    }
    String itemName = trimToNull(request.getItemName());
    if (itemName == null) {
      throw new IllegalArgumentException("配置名称不能为空");
    }
    if (itemName.length() > 128) {
      throw new IllegalArgumentException("配置名称长度不能超过128");
    }
    if (itemCode.length() > 64) {
      throw new IllegalArgumentException("配置编码长度不能超过64");
    }
    Integer status = request.getStatus();
    if (status == null || (status != 0 && status != 1)) {
      throw new IllegalArgumentException("状态仅支持0或1");
    }
    Integer sortNo = request.getSortNo() == null ? 0 : request.getSortNo();
    if (sortNo < 0 || sortNo > 9999) {
      throw new IllegalArgumentException("排序范围为0-9999");
    }
    String remark = trimToNull(request.getRemark());
    if (remark != null && remark.length() > 255) {
      throw new IllegalArgumentException("备注长度不能超过255");
    }
    return new SanitizedRequest(configGroup, itemCode, itemName, status, sortNo, remark);
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private List<Long> sanitizeIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return ids.stream().filter(id -> id != null && id > 0).distinct().toList();
  }

  private record SanitizedRequest(
      String configGroup,
      String itemCode,
      String itemName,
      Integer status,
      Integer sortNo,
      String remark) {}
}
