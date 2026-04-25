package com.zhiyangyun.care.baseconfig.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class BaseDataItemServiceImpl implements BaseDataItemService {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Map<String, String> PRESET_CODE_MAP = buildPresetCodeMap();
  private static final Map<String, String> PRESET_FUNCTION_CODE_MAP = buildPresetFunctionCodeMap();
  private final BaseDataItemMapper baseDataItemMapper;

  public BaseDataItemServiceImpl(BaseDataItemMapper baseDataItemMapper) {
    this.baseDataItemMapper = baseDataItemMapper;
  }

  @Override
  public BaseDataItemResponse create(BaseDataItemRequest request) {
    SanitizedRequest sanitized = sanitizeRequest(request, null, null);
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
    BaseDataItem item = baseDataItemMapper.selectById(id);
    if (item == null || item.getIsDeleted() != null && item.getIsDeleted() == 1) {
      throw new IllegalArgumentException("配置项不存在");
    }
    if (!request.getTenantId().equals(item.getTenantId())) {
      throw new IllegalArgumentException("无权限访问该配置项");
    }
    SanitizedRequest sanitized = sanitizeRequest(request, item.getItemCode(), id, item.getRemark());
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
        SanitizedRequest sanitized = sanitizeRequest(rowRequest, null, null);
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

  private SanitizedRequest sanitizeRequest(BaseDataItemRequest request, String fallbackCode, Long excludeId) {
    return sanitizeRequest(request, fallbackCode, excludeId, null);
  }

  private SanitizedRequest sanitizeRequest(BaseDataItemRequest request, String fallbackCode, Long excludeId, String existingRemark) {
    String configGroup = trimToNull(request.getConfigGroup());
    validateConfigGroup(configGroup);
    String itemName = trimToNull(request.getItemName());
    if (itemName == null) {
      throw new IllegalArgumentException("配置名称不能为空");
    }
    if (itemName.length() > 128) {
      throw new IllegalArgumentException("配置名称长度不能超过128");
    }
    String itemCode = normalizeCode(request.getItemCode());
    if (itemCode == null) {
      itemCode = normalizeCode(fallbackCode);
    }
    if (itemCode == null) {
      itemCode = generateItemCode(request.getTenantId(), configGroup, itemName, excludeId);
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
    remark = normalizeRemark(configGroup, request.getTenantId(), itemName, itemCode, remark, excludeId, existingRemark);
    return new SanitizedRequest(configGroup, itemCode, itemName, status, sortNo, remark);
  }

  private String normalizeRemark(
      String configGroup,
      Long tenantId,
      String itemName,
      String itemCode,
      String remark,
      Long excludeId,
      String existingRemark) {
    if (!"ADMISSION_ROOM_TYPE".equals(configGroup)) {
      return remark;
    }
    RoomTypeRemark parsed = parseRoomTypeRemark(remark);
    RoomTypeRemark existing = parseRoomTypeRemark(existingRemark);
    String roomKind = parsed.roomKind() != null ? parsed.roomKind() : existing.roomKind();
    Integer defaultCapacity = parsed.defaultCapacity() != null ? parsed.defaultCapacity() : existing.defaultCapacity();
    if ("FUNCTIONAL".equals(roomKind)) {
      String roomCode = firstNonBlank(parsed.roomCode(), existing.roomCode());
      if (roomCode == null) {
        roomCode = generateFunctionalRoomCode(tenantId, itemName, itemCode, excludeId);
      } else {
        roomCode = normalizeFunctionalRoomCode(roomCode);
      }
      return buildRoomTypeRemarkJson(parsed.text(), defaultCapacity, roomKind, roomCode);
    }
    return buildRoomTypeRemarkJson(parsed.text(), defaultCapacity, roomKind, null);
  }

  private String generateItemCode(Long tenantId, String configGroup, String itemName, Long excludeId) {
    String presetCode = PRESET_CODE_MAP.get(buildPresetKey(configGroup, itemName));
    if (presetCode != null && !itemCodeExists(tenantId, configGroup, presetCode, excludeId)) {
      return presetCode;
    }
    String asciiCode = normalizeCode(itemName.replaceAll("[^A-Za-z0-9]+", "_"));
    if (asciiCode != null) {
      return uniqueCandidateCode(tenantId, configGroup, asciiCode, excludeId);
    }
    String groupPrefix = buildGroupPrefix(configGroup);
    return uniqueCandidateCode(tenantId, configGroup, groupPrefix, excludeId);
  }

  private String generateFunctionalRoomCode(Long tenantId, String itemName, String itemCode, Long excludeId) {
    String presetCode = PRESET_FUNCTION_CODE_MAP.get(buildPresetKey("ADMISSION_ROOM_TYPE", itemName));
    if (presetCode != null && !functionalRoomCodeExists(tenantId, presetCode, excludeId)) {
      return presetCode;
    }
    List<String> candidates = new ArrayList<>();
    String itemCodeCandidate = deriveFunctionalCodeFromText(itemCode);
    if (itemCodeCandidate != null) {
      candidates.add(itemCodeCandidate);
    }
    String itemNameCandidate = deriveFunctionalCodeFromText(itemName);
    if (itemNameCandidate != null) {
      candidates.add(itemNameCandidate);
    }
    for (String candidate : candidates) {
      String normalized = normalizeFunctionalRoomCode(candidate);
      if (normalized != null && !functionalRoomCodeExists(tenantId, normalized, excludeId)) {
        return normalized;
      }
    }
    for (char first = 'X'; first <= 'Z'; first++) {
      for (char second = 'A'; second <= 'Z'; second++) {
        String candidate = "" + first + second;
        if (!functionalRoomCodeExists(tenantId, candidate, excludeId)) {
          return candidate;
        }
      }
    }
    throw new IllegalArgumentException("自动生成功能房编码失败，请稍后重试");
  }

  private boolean functionalRoomCodeExists(Long tenantId, String roomCode, Long excludeId) {
    if (tenantId == null || roomCode == null) {
      return false;
    }
    List<BaseDataItem> items = baseDataItemMapper.selectList(Wrappers.lambdaQuery(BaseDataItem.class)
        .eq(BaseDataItem::getIsDeleted, 0)
        .eq(BaseDataItem::getTenantId, tenantId)
        .eq(BaseDataItem::getConfigGroup, "ADMISSION_ROOM_TYPE"));
    for (BaseDataItem item : items) {
      if (excludeId != null && excludeId.equals(item.getId())) {
        continue;
      }
      RoomTypeRemark parsed = parseRoomTypeRemark(item.getRemark());
      if ("FUNCTIONAL".equals(parsed.roomKind()) && roomCode.equals(parsed.roomCode())) {
        return true;
      }
    }
    return false;
  }

  private String normalizeFunctionalRoomCode(String roomCode) {
    String normalized = trimToNull(roomCode);
    if (normalized == null) {
      return null;
    }
    normalized = normalized.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
    if (normalized.isBlank()) {
      return null;
    }
    return normalized.length() <= 3 ? normalized : normalized.substring(0, 3);
  }

  private String deriveFunctionalCodeFromText(String text) {
    String normalized = trimToNull(text);
    if (normalized == null) {
      return null;
    }
    normalized = normalized.toUpperCase(Locale.ROOT)
        .replace("ROOM_", "")
        .replace("ROOM", "")
        .replace("TYPE", "")
        .replaceAll("[^A-Z]+", " ")
        .trim();
    if (normalized.isBlank()) {
      return null;
    }
    String[] parts = normalized.split("\\s+");
    StringBuilder builder = new StringBuilder();
    for (String part : parts) {
      if (!part.isBlank()) {
        builder.append(part.charAt(0));
      }
      if (builder.length() >= 3) {
        break;
      }
    }
    if (builder.length() == 0 && !parts[0].isBlank()) {
      builder.append(parts[0], 0, Math.min(parts[0].length(), 3));
    }
    return builder.toString();
  }

  private RoomTypeRemark parseRoomTypeRemark(String remark) {
    String raw = trimToNull(remark);
    if (raw == null) {
      return new RoomTypeRemark("", null, null, null);
    }
    try {
      JsonNode node = OBJECT_MAPPER.readTree(raw);
      Integer defaultCapacity = node.hasNonNull("defaultCapacity") ? node.get("defaultCapacity").asInt() : null;
      String roomKind = trimToNull(node.path("roomKind").asText(null));
      if (roomKind != null) {
        roomKind = roomKind.toUpperCase(Locale.ROOT);
      } else if (Integer.valueOf(0).equals(defaultCapacity)) {
        roomKind = "FUNCTIONAL";
      }
      String roomCode = normalizeFunctionalRoomCode(node.path("roomCode").asText(null));
      return new RoomTypeRemark(trimToNull(node.path("text").asText("")), defaultCapacity, roomKind, roomCode);
    } catch (Exception ex) {
      return new RoomTypeRemark(raw, null, null, null);
    }
  }

  private String buildRoomTypeRemarkJson(String text, Integer defaultCapacity, String roomKind, String roomCode) {
    try {
      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("text", text == null ? "" : text);
      payload.put("defaultCapacity", defaultCapacity);
      payload.put("roomKind", roomKind);
      if (roomCode != null) {
        payload.put("roomCode", roomCode);
      }
      return OBJECT_MAPPER.writeValueAsString(payload);
    } catch (Exception ex) {
      throw new IllegalArgumentException("房间类型备注格式无效");
    }
  }

  private String uniqueCandidateCode(Long tenantId, String configGroup, String baseCode, Long excludeId) {
    String normalizedBase = normalizeCode(baseCode);
    if (normalizedBase == null) {
      normalizedBase = "ITEM";
    }
    normalizedBase = trimCodeLength(normalizedBase, 64);
    if (!itemCodeExists(tenantId, configGroup, normalizedBase, excludeId)) {
      return normalizedBase;
    }
    int suffix = 2;
    while (suffix < 10000) {
      String candidate = trimCodeLength(normalizedBase, 64 - String.valueOf(suffix).length() - 1) + "_" + suffix;
      if (!itemCodeExists(tenantId, configGroup, candidate, excludeId)) {
        return candidate;
      }
      suffix++;
    }
    throw new IllegalArgumentException("自动生成配置编码失败，请稍后重试");
  }

  private boolean itemCodeExists(Long tenantId, String configGroup, String itemCode, Long excludeId) {
    if (tenantId == null || configGroup == null || itemCode == null) {
      return false;
    }
    var wrapper = Wrappers.lambdaQuery(BaseDataItem.class)
        .eq(BaseDataItem::getIsDeleted, 0)
        .eq(BaseDataItem::getTenantId, tenantId)
        .eq(BaseDataItem::getConfigGroup, configGroup)
        .eq(BaseDataItem::getItemCode, itemCode);
    if (excludeId != null) {
      wrapper.ne(BaseDataItem::getId, excludeId);
    }
    return baseDataItemMapper.selectCount(wrapper) > 0;
  }

  private String buildGroupPrefix(String configGroup) {
    String normalized = normalizeCode(configGroup == null ? null : configGroup.replaceAll("[^A-Za-z0-9]+", "_"));
    if (normalized == null) {
      return "ITEM";
    }
    String[] parts = normalized.split("_+");
    StringBuilder builder = new StringBuilder();
    for (String part : parts) {
      if (!part.isBlank()) {
        builder.append(part.charAt(0));
      }
      if (builder.length() >= 6) {
        break;
      }
    }
    String prefix = builder.length() > 0 ? builder.toString() : normalized;
    return trimCodeLength(prefix, 12);
  }

  private String trimCodeLength(String value, int maxLength) {
    if (value == null || value.length() <= maxLength) {
      return value;
    }
    return value.substring(0, Math.max(1, maxLength));
  }

  private static Map<String, String> buildPresetCodeMap() {
    Map<String, String> map = new LinkedHashMap<>();
    map.put(buildPresetKey("ADMISSION_BED_TYPE", "标准床"), "BED_STANDARD");
    map.put(buildPresetKey("ADMISSION_BED_TYPE", "护理床"), "BED_CARE");
    map.put(buildPresetKey("ADMISSION_BED_TYPE", "电动护理床"), "BED_ELECTRIC");
    map.put(buildPresetKey("ADMISSION_BED_TYPE", "电动床"), "BED_ELECTRIC");
    map.put(buildPresetKey("ADMISSION_BED_TYPE", "防压疮床"), "BED_ANTI_DECUBITUS");
    map.put(buildPresetKey("ADMISSION_BED_TYPE", "医用床"), "BED_MEDICAL");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "单人间"), "ROOM_SINGLE");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "双人间"), "ROOM_DOUBLE");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "三人间"), "ROOM_TRIPLE");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "护理房"), "ROOM_CARE");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "套间"), "ROOM_SUITE");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "护理站"), "ROOM_NURSING_STATION");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "开水房"), "ROOM_WATER");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "洗衣房"), "ROOM_LAUNDRY");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "卫生间"), "ROOM_TOILET");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "浴室"), "ROOM_BATH");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "治疗室"), "ROOM_TREATMENT");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "库房"), "ROOM_STORAGE");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "活动室"), "ROOM_ACTIVITY");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "餐厅"), "ROOM_DINING");
    map.put(buildPresetKey("ADMISSION_AREA", "A区"), "AREA_A");
    map.put(buildPresetKey("ADMISSION_AREA", "B区"), "AREA_B");
    map.put(buildPresetKey("ADMISSION_AREA", "C区"), "AREA_C");
    return map;
  }

  private static Map<String, String> buildPresetFunctionCodeMap() {
    Map<String, String> map = new LinkedHashMap<>();
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "护理站"), "N");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "开水房"), "W");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "洗衣房"), "L");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "卫生间"), "T");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "浴室"), "B");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "治疗室"), "Z");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "库房"), "K");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "活动室"), "H");
    map.put(buildPresetKey("ADMISSION_ROOM_TYPE", "餐厅"), "C");
    return map;
  }

  private static String buildPresetKey(String configGroup, String itemName) {
    return (configGroup == null ? "" : configGroup.trim().toUpperCase()) + "::" + (itemName == null ? "" : itemName.trim());
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

  private record RoomTypeRemark(
      String text,
      Integer defaultCapacity,
      String roomKind,
      String roomCode) {}

  private String firstNonBlank(String... values) {
    if (values == null) {
      return null;
    }
    for (String value : values) {
      String trimmed = trimToNull(value);
      if (trimmed != null) {
        return trimmed;
      }
    }
    return null;
  }
}
