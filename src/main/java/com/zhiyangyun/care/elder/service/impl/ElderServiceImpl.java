package com.zhiyangyun.care.elder.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderBedRelation;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmission;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderBedRelationMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderChangeLogMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderAdmissionMapper;
import com.zhiyangyun.care.elder.model.AssignBedRequest;
import com.zhiyangyun.care.elder.model.BedStatus;
import com.zhiyangyun.care.elder.model.ElderDepartureType;
import com.zhiyangyun.care.elder.model.ElderLifecycleStatus;
import com.zhiyangyun.care.elder.model.ElderStatus;
import com.zhiyangyun.care.elder.model.BedResponse;
import com.zhiyangyun.care.elder.model.ElderCreateRequest;
import com.zhiyangyun.care.elder.model.ElderResponse;
import com.zhiyangyun.care.elder.model.ElderUpdateRequest;
import com.zhiyangyun.care.elder.model.BedReleaseResult;
import com.zhiyangyun.care.elder.service.ElderLifecycleStateService;
import com.zhiyangyun.care.elder.service.ElderOccupancyService;
import com.zhiyangyun.care.elder.service.ElderService;
import com.zhiyangyun.care.elder.util.QrCodeUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElderServiceImpl implements ElderService {
  private static final String ELDER_NAME_PINYIN_BOUNDARY = "阿芭擦搭蛾发噶哈讥咔垃妈拿哦啪期然撒塌挖昔压匝";
  private static final String ELDER_NAME_PINYIN_INITIAL = "ABCDEFGHJKLMNOPQRSTWXYZ";
  private static final String SOURCE_TYPE_MARKETING_CONTRACT = "MARKETING_CONTRACT";
  private static final String SOURCE_TYPE_HISTORICAL_IMPORT = "HISTORICAL_IMPORT";
  private static final String[] ELDER_PAGE_COLUMNS = {
      "id", "tenant_id", "org_id", "elder_code", "elder_qr_code", "full_name", "id_card_no",
      "gender", "birth_date", "phone", "home_address", "medical_insurance_copy_url",
      "household_copy_url", "medical_record_file_url", "admission_date", "status", "lifecycle_status",
      "departure_type", "lifecycle_updated_at", "last_lifecycle_event_id", "bed_id",
      "care_level", "risk_precommit", "remark", "created_by", "create_time", "update_time", "is_deleted"
  };
  private static final String[] CONTRACT_PAGE_COLUMNS = {
      "id", "tenant_id", "org_id", "lead_id", "elder_id", "contract_no", "status", "signed_at",
      "effective_from", "effective_to", "contract_status", "flow_stage", "create_time", "update_time", "is_deleted"
  };
  private static final String[] ADMISSION_PAGE_COLUMNS = {
      "id", "tenant_id", "org_id", "elder_id", "admission_date", "contract_no", "deposit_amount",
      "remark", "created_by", "create_time", "update_time", "is_deleted"
  };
  private static final String[] BED_PAGE_COLUMNS = {
      "id", "tenant_id", "org_id", "room_id", "bed_no", "bed_qr_code", "status", "elder_id",
      "create_time", "update_time", "is_deleted"
  };
  private static final String[] ROOM_PAGE_COLUMNS = {
      "id", "tenant_id", "org_id", "room_no", "capacity", "status", "create_time", "update_time", "is_deleted"
  };

  private final ElderMapper elderMapper;
  private final BedMapper bedMapper;
  private final ElderBedRelationMapper relationMapper;
  private final RoomMapper roomMapper;
  private final ElderChangeLogMapper changeLogMapper;
  private final CrmContractMapper crmContractMapper;
  private final ElderAdmissionMapper admissionMapper;
  private final ElderLifecycleStateService elderLifecycleStateService;
  private final ElderOccupancyService elderOccupancyService;

  public ElderServiceImpl(ElderMapper elderMapper, BedMapper bedMapper,
      ElderBedRelationMapper relationMapper, RoomMapper roomMapper, ElderChangeLogMapper changeLogMapper,
      CrmContractMapper crmContractMapper, ElderAdmissionMapper admissionMapper,
      ElderLifecycleStateService elderLifecycleStateService,
      ElderOccupancyService elderOccupancyService) {
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.relationMapper = relationMapper;
    this.roomMapper = roomMapper;
    this.changeLogMapper = changeLogMapper;
    this.crmContractMapper = crmContractMapper;
    this.admissionMapper = admissionMapper;
    this.elderLifecycleStateService = elderLifecycleStateService;
    this.elderOccupancyService = elderOccupancyService;
  }

  @Override
  @Transactional
  public ElderResponse create(ElderCreateRequest request) {
    validateCreateRequest(request);
    ElderProfile elder = new ElderProfile();
    elder.setTenantId(request.getTenantId());
    elder.setOrgId(request.getOrgId());
    String elderCode = request.getElderCode();
    if (elderCode == null || elderCode.isBlank()) {
      elderCode = generateElderCode(request.getOrgId());
    }
    elder.setElderCode(elderCode);
    elder.setElderQrCode(QrCodeUtil.generate());
    elder.setFullName(normalizeText(request.getFullName()));
    elder.setIdCardNo(normalizeText(request.getIdCardNo()));
    elder.setGender(request.getGender());
    elder.setBirthDate(request.getBirthDate());
    elder.setPhone(normalizeText(request.getPhone()));
    elder.setHomeAddress(normalizeText(request.getHomeAddress()));
    elder.setMedicalInsuranceCopyUrl(normalizeText(request.getMedicalInsuranceCopyUrl()));
    elder.setHouseholdCopyUrl(normalizeText(request.getHouseholdCopyUrl()));
    elder.setMedicalRecordFileUrl(normalizeText(request.getMedicalRecordFileUrl()));
    elder.setAdmissionDate(request.getAdmissionDate());
    String requestedLifecycleStatus = ElderLifecycleStatus.normalize(request.getLifecycleStatus());
    String requestedDepartureType = normalizeDepartureTypeForLifecycle(requestedLifecycleStatus, request.getDepartureType());
    Integer requestedLegacyStatus = requestedLifecycleStatus == null
        ? request.getStatus()
        : ElderLifecycleStatus.toLegacyStatus(requestedLifecycleStatus, request.getStatus());
    elder.setStatus(requestedLegacyStatus);
    elder.setCareLevel(normalizeText(request.getCareLevel()));
    elder.setRiskPrecommit(normalizeText(request.getRiskPrecommit()));
    elder.setRemark(normalizeText(request.getRemark()));
    elder.setSourceType(normalizeSourceType(request.getSourceType(), null));
    elder.setHistoricalContractFileUrl(normalizeText(request.getHistoricalContractFileUrl()));
    elder.setCreatedBy(request.getCreatedBy());
    elderMapper.insert(elder);
    if (requestedLifecycleStatus != null || requestedDepartureType != null) {
      elderLifecycleStateService.transition(
          elder,
          requestedLifecycleStatus == null
              ? ElderLifecycleStatus.fromLegacyStatus(elder.getStatus(), requestedDepartureType)
              : requestedLifecycleStatus,
          requestedDepartureType,
          "PROFILE_CREATE",
          "创建长者档案",
          "ELDER",
          elder.getId(),
          request.getCreatedBy());
    } else {
      elderLifecycleStateService.transitionFromLegacyStatus(
          elder,
          elder.getStatus(),
          null,
          "PROFILE_CREATE",
          "创建长者档案",
          "ELDER",
          elder.getId(),
          request.getCreatedBy());
    }
    if (request.getBedId() != null) {
      AssignBedRequest assign = new AssignBedRequest();
      assign.setTenantId(request.getTenantId());
      assign.setCreatedBy(request.getCreatedBy());
      assign.setBedId(request.getBedId());
      assign.setStartDate(request.getBedStartDate());
      ElderResponse assigned = assignBed(elder.getId(), assign);
      return assigned == null ? toResponse(elder, null) : assigned;
    }
    return toResponse(elder, null);
  }

  @Override
  @Transactional
  public ElderResponse update(Long id, ElderUpdateRequest request) {
    ElderProfile elder = elderMapper.selectById(id);
    if (elder == null) {
      return null;
    }
    if (request.getTenantId() != null && !request.getTenantId().equals(elder.getTenantId())) {
      throw new IllegalArgumentException("无权限访问该老人");
    }
    Long currentBedId = elder.getBedId();
    String previousStatus = elder.getStatus() == null ? null : String.valueOf(elder.getStatus());
    String previousLifecycleStatus = elder.getLifecycleStatus();
    String previousDepartureType = elder.getDepartureType();
    String previousAdmissionDate = elder.getAdmissionDate() == null ? null : elder.getAdmissionDate().toString();
    String previousCareLevel = elder.getCareLevel();
    String previousSourceType = elder.getSourceType();
    String previousHistoricalContractFileUrl = elder.getHistoricalContractFileUrl();
    if (request.getElderCode() != null) {
      elder.setElderCode(normalizeText(request.getElderCode()));
    }
    if (request.getFullName() != null) {
      elder.setFullName(normalizeText(request.getFullName()));
    }
    if (request.getIdCardNo() != null) {
      elder.setIdCardNo(normalizeText(request.getIdCardNo()));
    }
    if (request.getGender() != null) {
      elder.setGender(request.getGender());
    }
    if (request.getBirthDate() != null) {
      elder.setBirthDate(request.getBirthDate());
    }
    if (request.getPhone() != null) {
      elder.setPhone(normalizeText(request.getPhone()));
    }
    if (request.getHomeAddress() != null) {
      elder.setHomeAddress(normalizeText(request.getHomeAddress()));
    }
    if (request.getMedicalInsuranceCopyUrl() != null) {
      elder.setMedicalInsuranceCopyUrl(normalizeText(request.getMedicalInsuranceCopyUrl()));
    }
    if (request.getHouseholdCopyUrl() != null) {
      elder.setHouseholdCopyUrl(normalizeText(request.getHouseholdCopyUrl()));
    }
    if (request.getMedicalRecordFileUrl() != null) {
      elder.setMedicalRecordFileUrl(normalizeText(request.getMedicalRecordFileUrl()));
    }
    if (request.getAdmissionDate() != null) {
      elder.setAdmissionDate(request.getAdmissionDate());
    }
    String requestedLifecycleStatus = ElderLifecycleStatus.normalize(request.getLifecycleStatus());
    String requestedDepartureType = normalizeDepartureTypeForLifecycle(
        requestedLifecycleStatus,
        request.getDepartureType() == null ? elder.getDepartureType() : request.getDepartureType());
    Integer requestedLegacyStatus = request.getStatus();
    if (requestedLifecycleStatus != null) {
      requestedLegacyStatus = ElderLifecycleStatus.toLegacyStatus(requestedLifecycleStatus, requestedLegacyStatus);
    }
    if (requestedLegacyStatus != null) {
      elder.setStatus(requestedLegacyStatus);
    }
    if (request.getCareLevel() != null) {
      elder.setCareLevel(normalizeText(request.getCareLevel()));
    }
    if (request.getRiskPrecommit() != null) {
      elder.setRiskPrecommit(normalizeText(request.getRiskPrecommit()));
    }
    if (request.getRemark() != null) {
      elder.setRemark(normalizeText(request.getRemark()));
    }
    if (request.getSourceType() != null) {
      elder.setSourceType(normalizeSourceType(request.getSourceType(), null));
    }
    if (request.getHistoricalContractFileUrl() != null) {
      elder.setHistoricalContractFileUrl(normalizeText(request.getHistoricalContractFileUrl()));
    }
    if (requestedLifecycleStatus != null) {
      elderLifecycleStateService.transition(
          elder,
          requestedLifecycleStatus,
          requestedDepartureType,
          "PROFILE_STATUS_CHANGE",
          "档案状态变更",
          "ELDER",
          elder.getId(),
          request.getUpdatedBy());
    } else if (request.getStatus() != null) {
      elderLifecycleStateService.transitionFromLegacyStatus(
          elder,
          request.getStatus(),
          requestedDepartureType,
          "PROFILE_STATUS_CHANGE",
          "档案状态变更",
          "ELDER",
          elder.getId(),
          request.getUpdatedBy());
    } else {
      elderMapper.updateById(elder);
    }
    insertFieldChangeLog(elder, request.getUpdatedBy(), "STATUS_CHANGE", previousStatus,
        elder.getStatus() == null ? null : String.valueOf(elder.getStatus()), "状态变更");
    insertFieldChangeLog(elder, request.getUpdatedBy(), "LIFECYCLE_STATUS_CHANGE", previousLifecycleStatus,
        elder.getLifecycleStatus(), "生命周期主状态变更");
    insertFieldChangeLog(elder, request.getUpdatedBy(), "DEPARTURE_TYPE_CHANGE", previousDepartureType,
        elder.getDepartureType(), "离场类型变更");
    insertFieldChangeLog(elder, request.getUpdatedBy(), "ADMISSION_DATE_CHANGE", previousAdmissionDate,
        elder.getAdmissionDate() == null ? null : elder.getAdmissionDate().toString(), "入院日期变更");
    insertFieldChangeLog(elder, request.getUpdatedBy(), "CARE_LEVEL_CHANGE", previousCareLevel,
        elder.getCareLevel(), "护理等级变更");
    insertFieldChangeLog(elder, request.getUpdatedBy(), "SOURCE_TYPE_CHANGE", previousSourceType,
        elder.getSourceType(), "档案来源变更");
    insertFieldChangeLog(elder, request.getUpdatedBy(), "HISTORICAL_CONTRACT_FILE_CHANGE", previousHistoricalContractFileUrl,
        elder.getHistoricalContractFileUrl(), "历史合同附件变更");
    boolean shouldDischargeUnbind = isDischargedRequest(requestedLifecycleStatus, requestedLegacyStatus)
        && currentBedId != null;
    boolean shouldClearBed = Boolean.TRUE.equals(request.getClearBed()) && request.getBedId() == null && currentBedId != null;
    if (shouldDischargeUnbind) {
      return unbindBed(elder.getId(), LocalDate.now(), "状态变更退住", request.getTenantId(), request.getUpdatedBy());
    }
    if (request.getBedId() != null && !request.getBedId().equals(currentBedId)) {
      Integer nextStatus = requestedLegacyStatus != null ? requestedLegacyStatus : elder.getStatus();
      LocalDate nextAdmissionDate = request.getAdmissionDate() != null ? request.getAdmissionDate() : elder.getAdmissionDate();
      if (!Objects.equals(nextStatus, ElderStatus.IN_HOSPITAL)) {
        throw new IllegalArgumentException("仅在院状态可分配床位");
      }
      if (nextAdmissionDate == null) {
        throw new IllegalArgumentException("选择床位时必须填写入院日期");
      }
      if (request.getBedStartDate() == null) {
        throw new IllegalArgumentException("选择床位时必须填写床位开始日期");
      }
      if (request.getBedStartDate().isBefore(nextAdmissionDate)) {
        throw new IllegalArgumentException("床位开始日期不能早于入院日期");
      }
      AssignBedRequest assign = new AssignBedRequest();
      assign.setTenantId(request.getTenantId());
      assign.setCreatedBy(request.getUpdatedBy());
      assign.setBedId(request.getBedId());
      assign.setStartDate(request.getBedStartDate());
      ElderResponse assigned = assignBed(elder.getId(), assign);
      return assigned == null ? toResponse(elder, null) : assigned;
    }
    if (shouldClearBed) {
      return unbindBed(elder.getId(), LocalDate.now(), "档案编辑解绑床位", request.getTenantId(), request.getUpdatedBy());
    }
    Bed bed = elder.getBedId() == null ? null : bedMapper.selectById(elder.getBedId());
    return toResponse(elder, bed);
  }

  @Override
  public ElderResponse get(Long id, Long tenantId) {
    ElderProfile elder = elderMapper.selectById(id);
    if (elder == null) {
      return null;
    }
    if (tenantId != null && !tenantId.equals(elder.getTenantId())) {
      return null;
    }
    Bed bed = elder.getBedId() == null ? null : bedMapper.selectById(elder.getBedId());
    ElderResponse response = toResponse(elder, bed);
    CrmContract contract = resolveLatestContractMap(List.of(elder), tenantId).get(elder.getId());
    ElderAdmission admission = resolveLatestAdmissionMap(List.of(elder), tenantId).get(elder.getId());
    applyLifecycleFromContract(response, contract);
    applyLifecycleFromAdmission(response, admission, contract);
    applySourceType(response, contract, admission);
    return response;
  }

  @Override
  public IPage<ElderResponse> page(
      Long tenantId,
      long pageNo,
      long pageSize,
      String keyword,
      Boolean signedOnly,
      Integer status,
      String lifecycleStatus,
      String fullName,
      String idCardNo,
      String bedNo,
      String careLevel,
      String sortBy,
      String sortOrder) {
    Set<Long> latestSignedElderIds = null;
    if (Boolean.TRUE.equals(signedOnly)) {
      latestSignedElderIds = resolveLatestSignedElderIds(tenantId);
      if (latestSignedElderIds.isEmpty()) {
        return new Page<>(pageNo, pageSize);
      }
    }
    var baseWrapper = Wrappers.<ElderProfile>query()
        .select(ELDER_PAGE_COLUMNS)
        .eq("is_deleted", 0)
        .eq(tenantId != null, "tenant_id", tenantId)
        .eq(status != null && (lifecycleStatus == null || lifecycleStatus.isBlank()), "status", status)
        .eq(lifecycleStatus != null && !lifecycleStatus.isBlank(), "lifecycle_status", ElderLifecycleStatus.normalize(lifecycleStatus));
    if (latestSignedElderIds != null) {
      baseWrapper.in("id", latestSignedElderIds);
    }
    List<ElderProfile> allRows = elderMapper.selectList(baseWrapper.orderByDesc("create_time"));
    Map<Long, Bed> bedMap = resolveCurrentBedMap(allRows);
    String normalizedKeyword = normalizeText(keyword);
    String normalizedFullName = normalizeText(fullName);
    String normalizedIdCardNo = normalizeText(idCardNo);
    String normalizedBedNo = normalizeText(bedNo);
    String normalizedCareLevel = normalizeText(careLevel);
    List<ElderProfile> filtered = allRows.stream()
        .filter(elder -> matchesPageFilters(
            elder,
            bedMap.get(elder.getId()),
            normalizedKeyword,
            normalizedFullName,
            normalizedIdCardNo,
            normalizedBedNo,
            normalizedCareLevel))
        .sorted(buildPageComparator(sortBy, sortOrder, bedMap))
        .collect(Collectors.toCollection(ArrayList::new));
    Page<ElderProfile> manualPage = new Page<>(pageNo, pageSize, filtered.size());
    int fromIndex = Math.max((int) ((pageNo - 1) * pageSize), 0);
    int toIndex = Math.min(fromIndex + (int) pageSize, filtered.size());
    if (fromIndex >= filtered.size()) {
      manualPage.setRecords(List.of());
    } else {
      manualPage.setRecords(filtered.subList(fromIndex, toIndex));
    }
    return toPagedResponse(manualPage, tenantId);
  }

  private void validateCreateRequest(ElderCreateRequest request) {
    String fullName = normalizeText(request.getFullName());
    if (fullName == null) {
      throw new IllegalArgumentException("姓名不能为空");
    }
    if (request.getBirthDate() != null && request.getBirthDate().isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("出生日期不能晚于今天");
    }
    if (request.getAdmissionDate() != null && request.getBirthDate() != null
        && request.getAdmissionDate().isBefore(request.getBirthDate())) {
      throw new IllegalArgumentException("入院日期不能早于出生日期");
    }
    if (request.getBedStartDate() != null && request.getBedId() == null) {
      throw new IllegalArgumentException("未选择床位时不能填写床位开始日期");
    }
    if (request.getBedId() != null) {
      if (!Objects.equals(request.getStatus(), ElderStatus.IN_HOSPITAL)) {
        throw new IllegalArgumentException("仅在院状态可分配床位");
      }
      if (request.getAdmissionDate() == null) {
        throw new IllegalArgumentException("选择床位时必须填写入院日期");
      }
      if (request.getBedStartDate() == null) {
        throw new IllegalArgumentException("选择床位时必须填写床位开始日期");
      }
      if (request.getBedStartDate().isBefore(request.getAdmissionDate())) {
        throw new IllegalArgumentException("床位开始日期不能早于入院日期");
      }
    }
  }

  private String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String normalizeSourceType(String value, String defaultValue) {
    String normalized = normalizeText(value);
    if (normalized == null) {
      return defaultValue;
    }
    String upper = normalized.toUpperCase();
    if (SOURCE_TYPE_HISTORICAL_IMPORT.equals(upper) || SOURCE_TYPE_MARKETING_CONTRACT.equals(upper)) {
      return upper;
    }
    return defaultValue;
  }

  private Set<Long> resolveLatestSignedElderIds(Long tenantId) {
    Set<Long> signedElderIds = new java.util.LinkedHashSet<>();
    List<CrmContract> contracts = crmContractMapper.selectList(Wrappers.<CrmContract>query()
        .select(CONTRACT_PAGE_COLUMNS)
        .eq("is_deleted", 0)
        .eq(tenantId != null, "tenant_id", tenantId)
        .isNotNull("elder_id")
        .orderByDesc("update_time")
        .orderByDesc("create_time")
        .orderByDesc("id"));
    if (contracts != null && !contracts.isEmpty()) {
      Map<Long, CrmContract> latestByElder = new HashMap<>();
      for (CrmContract contract : contracts) {
        Long elderId = contract.getElderId();
        if (elderId == null || latestByElder.containsKey(elderId)) {
          continue;
        }
        latestByElder.put(elderId, contract);
      }
      latestByElder.values().stream()
          .filter(contract -> {
            String status = contract.getStatus();
            if (status == null) return false;
            String normalized = status.trim().toUpperCase();
            return "SIGNED".equals(normalized) || "EFFECTIVE".equals(normalized);
          })
          .map(CrmContract::getElderId)
          .filter(Objects::nonNull)
          .forEach(signedElderIds::add);
    }
    List<ElderAdmission> admissions = admissionMapper.selectList(Wrappers.<ElderAdmission>query()
        .select(ADMISSION_PAGE_COLUMNS)
        .eq("is_deleted", 0)
        .eq(tenantId != null, "tenant_id", tenantId)
        .isNotNull("elder_id")
        .orderByDesc("admission_date")
        .orderByDesc("create_time")
        .orderByDesc("id"));
    if (admissions != null && !admissions.isEmpty()) {
      Map<Long, ElderAdmission> latestByElder = new HashMap<>();
      for (ElderAdmission admission : admissions) {
        Long elderId = admission.getElderId();
        if (elderId == null || latestByElder.containsKey(elderId)) {
          continue;
        }
        latestByElder.put(elderId, admission);
      }
      latestByElder.values().stream()
          .filter(admission -> admission.getContractNo() != null && !admission.getContractNo().trim().isBlank())
          .map(ElderAdmission::getElderId)
          .filter(Objects::nonNull)
          .forEach(signedElderIds::add);
    }
    return signedElderIds;
  }

  private IPage<ElderResponse> toPagedResponse(IPage<ElderProfile> page, Long tenantId) {
    List<ElderProfile> records = page.getRecords() == null ? List.of() : page.getRecords();
    Map<Long, Bed> bedMap = resolveCurrentBedMap(records);
    Map<Long, CrmContract> contractMap = resolveLatestContractMap(records, tenantId);
    Map<Long, ElderAdmission> admissionMap = resolveLatestAdmissionMap(records, tenantId);
    List<ElderResponse> responseRecords = records.stream().map((elder) -> {
      Bed bed = bedMap.get(elder.getId());
      ElderResponse response = toResponse(elder, bed);
      CrmContract contract = contractMap.get(elder.getId());
      applyLifecycleFromContract(response, contract);
      applyLifecycleFromAdmission(response, admissionMap.get(elder.getId()), contract);
      applySourceType(response, contract, admissionMap.get(elder.getId()));
      return response;
    }).toList();
    Page<ElderResponse> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    responsePage.setRecords(responseRecords);
    return responsePage;
  }

  private Map<Long, Bed> resolveCurrentBedMap(List<ElderProfile> elders) {
    Set<Long> elderIds = elders.stream()
        .map(ElderProfile::getId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    Set<Long> tenantIds = elders.stream()
        .map(ElderProfile::getTenantId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    Set<Long> orgIds = elders.stream()
        .map(ElderProfile::getOrgId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    if (elderIds.isEmpty()) {
      return Map.of();
    }
    Map<Long, ElderBedRelation> activeRelationMap = relationMapper.selectList(Wrappers.lambdaQuery(ElderBedRelation.class)
            .eq(ElderBedRelation::getIsDeleted, 0)
            .eq(ElderBedRelation::getActiveFlag, 1)
            .in(!tenantIds.isEmpty(), ElderBedRelation::getTenantId, tenantIds)
            .in(!orgIds.isEmpty(), ElderBedRelation::getOrgId, orgIds)
            .in(ElderBedRelation::getElderId, elderIds)
            .orderByDesc(ElderBedRelation::getUpdateTime)
            .orderByDesc(ElderBedRelation::getCreateTime)
            .orderByDesc(ElderBedRelation::getId))
        .stream()
        .filter(item -> item.getElderId() != null)
        .collect(Collectors.toMap(ElderBedRelation::getElderId, item -> item, (first, ignored) -> first));
    Set<Long> bedIds = elders.stream()
        .map(ElderProfile::getBedId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    activeRelationMap.values().stream()
        .map(ElderBedRelation::getBedId)
        .filter(Objects::nonNull)
        .forEach(bedIds::add);
    if (bedIds.isEmpty()) {
      return Map.of();
    }
    Map<Long, Bed> bedById = bedMapper.selectList(Wrappers.<Bed>query()
            .select(BED_PAGE_COLUMNS)
            .in("id", bedIds))
        .stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Bed::getId, item -> item, (left, right) -> left));
    Map<Long, Bed> currentBedMap = new HashMap<>();
    for (ElderProfile elder : elders) {
      Long elderId = elder.getId();
      if (elderId == null) {
        continue;
      }
      ElderBedRelation activeRelation = activeRelationMap.get(elderId);
      Long currentBedId = activeRelation != null ? activeRelation.getBedId() : elder.getBedId();
      if (currentBedId != null && bedById.containsKey(currentBedId)) {
        currentBedMap.put(elderId, bedById.get(currentBedId));
      }
    }
    return currentBedMap;
  }

  private Map<Long, com.zhiyangyun.care.elder.entity.Room> resolveRoomMap(List<Bed> beds) {
    Set<Long> roomIds = beds.stream()
        .map(Bed::getRoomId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    if (roomIds.isEmpty()) {
      return Map.of();
    }
    return roomMapper.selectList(Wrappers.<com.zhiyangyun.care.elder.entity.Room>query()
            .select(ROOM_PAGE_COLUMNS)
            .in("id", roomIds))
        .stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(com.zhiyangyun.care.elder.entity.Room::getId, item -> item, (left, right) -> left));
  }

  private Map<Long, CrmContract> resolveLatestContractMap(List<ElderProfile> elders, Long tenantId) {
    Set<Long> elderIds = elders.stream()
        .map(ElderProfile::getId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    if (elderIds.isEmpty()) {
      return Map.of();
    }
    List<CrmContract> contracts = crmContractMapper.selectList(Wrappers.<CrmContract>query()
        .select(CONTRACT_PAGE_COLUMNS)
        .eq("is_deleted", 0)
        .eq(tenantId != null, "tenant_id", tenantId)
        .in("elder_id", elderIds)
        .orderByDesc("update_time")
        .orderByDesc("create_time")
        .orderByDesc("id"));
    Map<Long, CrmContract> map = new HashMap<>();
    for (CrmContract contract : contracts) {
      Long elderId = contract.getElderId();
      if (elderId == null || map.containsKey(elderId)) {
        continue;
      }
      map.put(elderId, contract);
    }
    return map;
  }

  private Map<Long, ElderAdmission> resolveLatestAdmissionMap(List<ElderProfile> elders, Long tenantId) {
    Set<Long> elderIds = elders.stream()
        .map(ElderProfile::getId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    if (elderIds.isEmpty()) {
      return Map.of();
    }
    List<ElderAdmission> admissions = admissionMapper.selectList(Wrappers.<ElderAdmission>query()
        .select(ADMISSION_PAGE_COLUMNS)
        .eq("is_deleted", 0)
        .eq(tenantId != null, "tenant_id", tenantId)
        .in("elder_id", elderIds)
        .orderByDesc("admission_date")
        .orderByDesc("create_time")
        .orderByDesc("id"));
    Map<Long, ElderAdmission> map = new HashMap<>();
    for (ElderAdmission admission : admissions) {
      Long elderId = admission.getElderId();
      if (elderId == null || map.containsKey(elderId)) {
        continue;
      }
      map.put(elderId, admission);
    }
    return map;
  }

  private void applyLifecycleFromContract(ElderResponse response, CrmContract contract) {
    if (response == null) {
      return;
    }
    if (contract == null) {
      return;
    }
    String flowStage = normalizeLifecycleStage(contract.getFlowStage());
    if (flowStage == null) {
      flowStage = normalizeLifecycleStage(resolveLifecycleStageByStatus(contract.getContractStatus(), contract.getStatus()));
    }
    if (flowStage != null) {
      response.setLifecycleStage(flowStage);
    }
    String contractStatus = firstNonBlank(contract.getContractStatus(), contract.getStatus());
    if (contractStatus != null) {
      response.setLifecycleContractStatus(contractStatus);
    }
  }

  private void applyLifecycleFromAdmission(ElderResponse response, ElderAdmission admission, CrmContract contract) {
    if (response == null || contract != null || admission == null) {
      return;
    }
    String contractNo = admission.getContractNo();
    if (contractNo == null || contractNo.trim().isBlank()) {
      return;
    }
    if (response.getLifecycleStage() == null || response.getLifecycleStage().isBlank()) {
      response.setLifecycleStage("SIGNED");
    }
    if (response.getLifecycleContractStatus() == null || response.getLifecycleContractStatus().isBlank()) {
      response.setLifecycleContractStatus("SIGNED");
    }
  }

  private void applySourceType(ElderResponse response, CrmContract contract, ElderAdmission admission) {
    if (response == null) {
      return;
    }
    String sourceType = normalizeSourceType(response.getSourceType(), null);
    String admissionSourceType = admission == null ? null : normalizeSourceType(admission.getSourceType(), null);
    if (sourceType == null && contract != null) {
      sourceType = SOURCE_TYPE_MARKETING_CONTRACT;
    }
    if (sourceType == null && admissionSourceType != null) {
      sourceType = admissionSourceType;
    }
    if (SOURCE_TYPE_HISTORICAL_IMPORT.equals(sourceType)
        && contract != null
        && SOURCE_TYPE_MARKETING_CONTRACT.equals(admissionSourceType)) {
      sourceType = SOURCE_TYPE_MARKETING_CONTRACT;
    }
    if (sourceType == null && admission != null && admission.getContractNo() != null && !admission.getContractNo().trim().isBlank()) {
      sourceType = SOURCE_TYPE_HISTORICAL_IMPORT;
    }
    response.setSourceType(sourceType);
  }

  private void insertFieldChangeLog(ElderProfile elder, Long createdBy, String changeType,
                                    String beforeValue, String afterValue, String reason) {
    String normalizedBefore = normalizeText(beforeValue);
    String normalizedAfter = normalizeText(afterValue);
    if (Objects.equals(normalizedBefore, normalizedAfter)) {
      return;
    }
    insertChangeLog(elder.getTenantId(), elder.getOrgId(), elder.getId(), createdBy, changeType,
        normalizedBefore, normalizedAfter, reason);
  }

  private boolean isAlphabeticKeyword(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return false;
    }
    return keyword.matches("[A-Za-z]+");
  }

  private boolean matchesPageFilters(
      ElderProfile elder,
      Bed bed,
      String keyword,
      String fullName,
      String idCardNo,
      String bedNo,
      String careLevel) {
    if (fullName != null && !matchByPinyinOrText(elder, fullName)) {
      return false;
    }
    if (keyword != null && !matchesSearchKeyword(elder, bed, keyword)) {
      return false;
    }
    if (idCardNo != null && !containsIgnoreCase(elder.getIdCardNo(), idCardNo)) {
      return false;
    }
    if (bedNo != null && !containsIgnoreCase(bed == null ? null : bed.getBedNo(), bedNo)) {
      return false;
    }
    if (careLevel != null && !containsIgnoreCase(elder.getCareLevel(), careLevel)) {
      return false;
    }
    return true;
  }

  private boolean matchesSearchKeyword(ElderProfile elder, Bed bed, String keyword) {
    if (containsIgnoreCase(elder.getIdCardNo(), keyword)
        || containsIgnoreCase(elder.getCareLevel(), keyword)
        || containsIgnoreCase(bed == null ? null : bed.getBedNo(), keyword)) {
      return true;
    }
    return matchByPinyinOrText(elder, keyword);
  }

  private boolean containsIgnoreCase(String source, String expected) {
    if (expected == null || expected.isBlank()) {
      return true;
    }
    if (source == null || source.isBlank()) {
      return false;
    }
    return source.toLowerCase().contains(expected.trim().toLowerCase());
  }

  private Comparator<ElderProfile> buildPageComparator(String sortBy, String sortOrder, Map<Long, Bed> bedMap) {
    Comparator<ElderProfile> comparator;
    if ("fullName".equals(sortBy)) {
      comparator = Comparator.comparing(
          elder -> safeLowerText(elder.getFullName()),
          Comparator.nullsLast(String::compareTo));
    } else if ("birthDate".equals(sortBy)) {
      comparator = Comparator.comparing(ElderProfile::getBirthDate, Comparator.nullsLast(LocalDate::compareTo));
    } else if ("bedNo".equals(sortBy)) {
      comparator = Comparator.comparing(
          elder -> safeLowerText(resolveBedNo(bedMap.get(elder.getId()))),
          Comparator.nullsLast(String::compareTo));
    } else if ("careLevel".equals(sortBy)) {
      comparator = Comparator.comparing(
          elder -> safeLowerText(elder.getCareLevel()),
          Comparator.nullsLast(String::compareTo));
    } else if ("status".equals(sortBy)) {
      comparator = Comparator.comparing(this::lifecycleSortRank, Comparator.nullsLast(Integer::compareTo));
    } else {
      comparator = Comparator.comparing(ElderProfile::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder()));
      sortOrder = "desc";
    }
    if (!"asc".equalsIgnoreCase(sortOrder)) {
      comparator = comparator.reversed();
    }
    return comparator.thenComparing(ElderProfile::getId, Comparator.nullsLast(Long::compareTo));
  }

  private String safeLowerText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim().toLowerCase();
  }

  private String resolveBedNo(Bed bed) {
    if (bed == null || bed.getBedNo() == null || bed.getBedNo().isBlank()) {
      return null;
    }
    return bed.getBedNo().trim();
  }

  private boolean matchByPinyinOrText(ElderProfile elder, String keyword) {
    String fullName = elder.getFullName() == null ? "" : elder.getFullName();
    String elderCode = elder.getElderCode() == null ? "" : elder.getElderCode();
    String phone = elder.getPhone() == null ? "" : elder.getPhone();
    String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
    if (normalizedKeyword.isEmpty()) {
      return true;
    }
    String searchText = (fullName + " " + elderCode + " " + phone + " " + toPinyinInitials(fullName)).toLowerCase();
    if (searchText.contains(normalizedKeyword)) {
      return true;
    }
    return isAlphabeticKeyword(normalizedKeyword) && toPinyinInitials(fullName).contains(normalizedKeyword);
  }

  private String toPinyinInitials(String text) {
    if (text == null || text.isBlank()) {
      return "";
    }
    HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
    format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
    format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    StringBuilder sb = new StringBuilder();
    for (char c : text.toCharArray()) {
      if (Character.isLetterOrDigit(c)) {
        if (c <= 127) {
          sb.append(Character.toLowerCase(c));
          continue;
        }
        try {
          String[] arr = PinyinHelper.toHanyuPinyinStringArray(c, format);
          if (arr != null && arr.length > 0 && arr[0] != null && !arr[0].isBlank()) {
            sb.append(arr[0].charAt(0));
            continue;
          }
        } catch (BadHanyuPinyinOutputFormatCombination ignored) {
        }
      }
      String initial = getChineseInitial(c);
      if (!initial.isBlank()) {
        sb.append(initial.toLowerCase());
      }
    }
    return sb.toString();
  }

  private String getChineseInitial(char c) {
    if (c < '\u4e00' || c > '\u9fa5') {
      return "";
    }
    String ch = String.valueOf(c);
    for (int i = 0; i < ELDER_NAME_PINYIN_BOUNDARY.length(); i += 1) {
      String current = ELDER_NAME_PINYIN_BOUNDARY.substring(i, i + 1);
      String next = i + 1 >= ELDER_NAME_PINYIN_BOUNDARY.length()
          ? null
          : ELDER_NAME_PINYIN_BOUNDARY.substring(i + 1, i + 2);
      if (next == null) {
        return ELDER_NAME_PINYIN_INITIAL.substring(i, i + 1);
      }
      if (ch.compareTo(current) >= 0 && ch.compareTo(next) < 0) {
        return ELDER_NAME_PINYIN_INITIAL.substring(i, i + 1);
      }
    }
    return "";
  }

  @Override
  @Transactional
  public ElderResponse assignBed(Long elderId, AssignBedRequest request) {
    ElderProfile elder = findElderForUpdate(elderId);
    if (elder == null) {
      throw new IllegalArgumentException("老人档案不存在");
    }
    if (request.getTenantId() != null && !request.getTenantId().equals(elder.getTenantId())) {
      throw new IllegalStateException("Org mismatch");
    }
    Bed bed = findBedForUpdate(request.getBedId());
    if (bed == null) {
      throw new IllegalArgumentException("床位不存在");
    }
    if (request.getTenantId() != null && !request.getTenantId().equals(bed.getTenantId())) {
      throw new IllegalStateException("Org mismatch");
    }
    if (!elder.getOrgId().equals(bed.getOrgId())) {
      throw new IllegalStateException("Org mismatch");
    }
    ensureNoDuplicateNameOccupied(elder);

    long occupied = bedMapper.selectCount(Wrappers.lambdaQuery(Bed.class)
        .eq(elder.getOrgId() != null, Bed::getOrgId, elder.getOrgId())
        .eq(elder.getTenantId() != null, Bed::getTenantId, elder.getTenantId())
        .eq(Bed::getRoomId, bed.getRoomId())
        .eq(Bed::getStatus, BedStatus.OCCUPIED)
        .eq(Bed::getIsDeleted, 0));
    com.zhiyangyun.care.elder.entity.Room room = roomMapper.selectById(bed.getRoomId());
    ElderBedRelation elderActive = findActiveRelationByElderForUpdate(elder.getTenantId(), elder.getOrgId(), elder.getId());
    boolean sameRoomTransfer = false;
    if (elderActive != null && elderActive.getBedId() != null) {
      Bed currentBed = elder.getBedId() != null && elder.getBedId().equals(elderActive.getBedId())
          ? findBedForUpdate(elder.getBedId())
          : findBedForUpdate(elderActive.getBedId());
      sameRoomTransfer = currentBed != null
          && Objects.equals(currentBed.getRoomId(), bed.getRoomId())
          && !Objects.equals(currentBed.getId(), bed.getId());
      if (sameRoomTransfer && occupied > 0) {
        occupied -= 1;
      }
    }
    if (room != null && room.getCapacity() != null && occupied >= room.getCapacity()) {
      throw new IllegalStateException("Room capacity exceeded");
    }
    LocalDate relationStartDate = request.getStartDate() == null ? LocalDate.now() : request.getStartDate();
    ElderBedRelation bedActive = findActiveRelationByBedForUpdate(elder.getTenantId(), elder.getOrgId(), bed.getId());
    if (bedActive != null && bedActive.getElderId() != null && bedActive.getElderId().equals(elder.getId())
        && Objects.equals(elder.getBedId(), bed.getId())
        && Objects.equals(bed.getElderId(), elder.getId())) {
      if (bed.getStatus() == null || bed.getStatus() != BedStatus.OCCUPIED) {
        bed.setStatus(BedStatus.OCCUPIED);
        bedMapper.updateById(bed);
      }
      return toResponse(elder, bed);
    }
    if (bedActive != null && !bedActive.getElderId().equals(elder.getId())) {
      throw new IllegalStateException("Bed already assigned");
    }
    if (bed.getElderId() != null && !bed.getElderId().equals(elder.getId())) {
      throw new IllegalStateException("Bed already assigned");
    }
    if (bed.getStatus() != null && bed.getStatus() != BedStatus.AVAILABLE && !Objects.equals(bed.getElderId(), elder.getId())) {
      throw new IllegalStateException("Bed is not available");
    }
    if (elderActive != null) {
      if (Objects.equals(elderActive.getBedId(), bed.getId())
          && Objects.equals(elder.getBedId(), bed.getId())
          && Objects.equals(bed.getElderId(), elder.getId())) {
        if (bed.getStatus() == null || bed.getStatus() != BedStatus.OCCUPIED) {
          bed.setStatus(BedStatus.OCCUPIED);
          bedMapper.updateById(bed);
        }
        return toResponse(elder, bed);
      }
      elderActive.setActiveFlag(0);
      elderActive.setEndDate(relationStartDate.minusDays(1));
      relationMapper.updateById(elderActive);

      Bed oldBed = findBedForUpdate(elderActive.getBedId());
      if (oldBed != null && Objects.equals(oldBed.getElderId(), elder.getId())) {
        oldBed.setElderId(null);
        oldBed.setStatus(BedStatus.AVAILABLE);
        oldBed.setOccupancySource(null);
        oldBed.setOccupancyRefType(null);
        oldBed.setOccupancyRefId(null);
        oldBed.setLockExpiresAt(null);
        oldBed.setOccupancyNote(null);
        oldBed.setLastReleaseReason("调床释放");
        oldBed.setLastReleasedAt(LocalDateTime.now());
        bedMapper.updateById(oldBed);
      }
    }

    ElderBedRelation relation = new ElderBedRelation();
    relation.setTenantId(elder.getTenantId());
    relation.setOrgId(elder.getOrgId());
    relation.setElderId(elder.getId());
    relation.setBedId(bed.getId());
    relation.setStartDate(relationStartDate);
    relation.setActiveFlag(1);
    relation.setCreatedBy(request.getCreatedBy());
    try {
      relationMapper.insert(relation);
    } catch (DuplicateKeyException ex) {
      throw new IllegalStateException("床位占用状态已变更，请刷新后重试", ex);
    }

    bed.setElderId(elder.getId());
    bed.setStatus(BedStatus.OCCUPIED);
    bed.setOccupancySource("RELATION");
    bed.setOccupancyRefType("ELDER_BED_RELATION");
    bed.setOccupancyRefId(relation.getId());
    bed.setLockExpiresAt(null);
    bed.setOccupancyNote(elder.getFullName() == null ? "长者在住" : "长者在住：" + elder.getFullName());
    bedMapper.updateById(bed);

    elder.setBedId(bed.getId());
    if (elder.getStatus() == null || elder.getStatus() == ElderStatus.DISCHARGED) {
      elderLifecycleStateService.transitionFromLegacyStatus(
          elder,
          ElderStatus.IN_HOSPITAL,
          null,
          "BED_ASSIGN_RECOVER",
          "分配床位后恢复在住状态",
          "BED",
          bed.getId(),
          request.getCreatedBy());
    } else {
      elderMapper.updateById(elder);
    }

    insertChangeLog(elder.getTenantId(), elder.getOrgId(), elder.getId(), request.getCreatedBy(),
        "BED_CHANGE", elderActive == null || Objects.equals(elderActive.getBedId(), bed.getId()) ? null : String.valueOf(elderActive.getBedId()),
        String.valueOf(bed.getId()), elderActive == null ? "床位分配" : "调床");
    return toResponse(elder, bed);
  }

  private void ensureNoDuplicateNameOccupied(ElderProfile elder) {
    return;
  }

  @Override
  @Transactional
  public ElderResponse unbindBed(Long elderId, LocalDate endDate, String reason, Long tenantId, Long createdBy) {
    ElderProfile elder = findElderForUpdate(elderId);
    if (elder == null) {
      throw new IllegalArgumentException("老人档案不存在");
    }
    if (tenantId != null && !tenantId.equals(elder.getTenantId())) {
      throw new IllegalArgumentException("无权限访问该老人");
    }
    BedReleaseResult releaseResult = elderOccupancyService.releaseBedAndCloseRelation(
        elder.getTenantId(), elder.getOrgId(), elder.getId(), endDate, reason);
    elder.setBedId(null);
    Long previousBedId = releaseResult.getPreviousBedId();
    insertChangeLog(elder.getTenantId(), elder.getOrgId(), elder.getId(), createdBy,
        "BED_CHANGE", previousBedId == null ? null : String.valueOf(previousBedId), null, reason);
    return toResponse(elder, null);
  }

  private ElderProfile findElderForUpdate(Long elderId) {
    if (elderId == null) {
      return null;
    }
    return elderMapper.selectOne(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getId, elderId)
            .eq(ElderProfile::getIsDeleted, 0)
            .last("LIMIT 1 FOR UPDATE"));
  }

  private Bed findBedForUpdate(Long bedId) {
    if (bedId == null) {
      return null;
    }
    return bedMapper.selectOne(
        Wrappers.lambdaQuery(Bed.class)
            .eq(Bed::getId, bedId)
            .eq(Bed::getIsDeleted, 0)
            .last("LIMIT 1 FOR UPDATE"));
  }

  private ElderBedRelation findActiveRelationByElderForUpdate(Long tenantId, Long orgId, Long elderId) {
    if (elderId == null) {
      return null;
    }
    return relationMapper.selectOne(
        Wrappers.lambdaQuery(ElderBedRelation.class)
            .eq(orgId != null, ElderBedRelation::getOrgId, orgId)
            .eq(tenantId != null, ElderBedRelation::getTenantId, tenantId)
            .eq(ElderBedRelation::getElderId, elderId)
            .eq(ElderBedRelation::getActiveFlag, 1)
            .eq(ElderBedRelation::getIsDeleted, 0)
            .last("LIMIT 1 FOR UPDATE"));
  }

  private ElderBedRelation findActiveRelationByBedForUpdate(Long tenantId, Long orgId, Long bedId) {
    if (bedId == null) {
      return null;
    }
    return relationMapper.selectOne(
        Wrappers.lambdaQuery(ElderBedRelation.class)
            .eq(orgId != null, ElderBedRelation::getOrgId, orgId)
            .eq(tenantId != null, ElderBedRelation::getTenantId, tenantId)
            .eq(ElderBedRelation::getBedId, bedId)
            .eq(ElderBedRelation::getActiveFlag, 1)
            .eq(ElderBedRelation::getIsDeleted, 0)
            .last("LIMIT 1 FOR UPDATE"));
  }

  private void insertChangeLog(Long tenantId, Long orgId, Long elderId, Long createdBy, String changeType,
                               String beforeValue, String afterValue, String reason) {
    if (tenantId == null || orgId == null || elderId == null) {
      return;
    }
    com.zhiyangyun.care.elder.entity.lifecycle.ElderChangeLog log =
        new com.zhiyangyun.care.elder.entity.lifecycle.ElderChangeLog();
    log.setTenantId(tenantId);
    log.setOrgId(orgId);
    log.setElderId(elderId);
    log.setChangeType(changeType);
    log.setBeforeValue(beforeValue);
    log.setAfterValue(afterValue);
    log.setReason(reason);
    log.setCreatedBy(createdBy);
    changeLogMapper.insert(log);
  }

  private ElderResponse toResponse(ElderProfile elder, Bed bed) {
    ElderResponse response = new ElderResponse();
    response.setId(elder.getId());
    response.setTenantId(elder.getTenantId());
    response.setOrgId(elder.getOrgId());
    response.setElderCode(elder.getElderCode());
    response.setElderQrCode(elder.getElderQrCode());
    response.setFullName(elder.getFullName());
    response.setIdCardNo(elder.getIdCardNo());
    response.setGender(elder.getGender());
    response.setBirthDate(elder.getBirthDate());
    response.setPhone(elder.getPhone());
    response.setHomeAddress(elder.getHomeAddress());
    response.setMedicalInsuranceCopyUrl(elder.getMedicalInsuranceCopyUrl());
    response.setHouseholdCopyUrl(elder.getHouseholdCopyUrl());
    response.setMedicalRecordFileUrl(elder.getMedicalRecordFileUrl());
    response.setAdmissionDate(elder.getAdmissionDate());
    response.setStatus(elder.getStatus());
    response.setLifecycleStatus(elder.getLifecycleStatus());
    response.setDepartureType(elder.getDepartureType());
    response.setBedId(elder.getBedId());
    response.setCareLevel(elder.getCareLevel());
    response.setRiskPrecommit(elder.getRiskPrecommit());
    response.setRemark(elder.getRemark());
    response.setSourceType(elder.getSourceType());
    response.setHistoricalContractFileUrl(elder.getHistoricalContractFileUrl());
    response.setLifecycleStage(resolveLifecycleStageByElder(elder));
    response.setLifecycleContractStatus(null);
    if (bed != null) {
      response.setBedId(bed.getId());
      Map<Long, com.zhiyangyun.care.elder.entity.Room> roomMap = resolveRoomMap(List.of(bed));
      com.zhiyangyun.care.elder.entity.Room room =
          bed.getRoomId() == null ? null : roomMap.get(bed.getRoomId());
      BedResponse bedResponse = new BedResponse();
      bedResponse.setId(bed.getId());
      bedResponse.setTenantId(bed.getTenantId());
      bedResponse.setOrgId(bed.getOrgId());
      bedResponse.setRoomId(bed.getRoomId());
      bedResponse.setBedNo(bed.getBedNo());
      bedResponse.setBedQrCode(bed.getBedQrCode());
      bedResponse.setStatus(bed.getStatus());
      bedResponse.setElderId(elder.getId());
      bedResponse.setRoomNo(room == null ? null : room.getRoomNo());
      response.setBedNo(bed.getBedNo());
      response.setRoomNo(room == null ? null : room.getRoomNo());
      response.setCurrentBed(bedResponse);
    }
    return response;
  }

  private String resolveLifecycleStageByElder(ElderProfile elder) {
    if (elder == null) {
      return "PENDING_ASSESSMENT";
    }
    String lifecycleStatus = ElderLifecycleStatus.normalize(elder.getLifecycleStatus());
    if (lifecycleStatus == null) {
      return resolveLifecycleStageByElderStatus(elder.getStatus());
    }
    if (ElderLifecycleStatus.INTENT.equals(lifecycleStatus) || ElderLifecycleStatus.TRIAL.equals(lifecycleStatus)) {
      return "PENDING_ASSESSMENT";
    }
    return "SIGNED";
  }

  private String resolveLifecycleStageByElderStatus(Integer status) {
    if (status == null) {
      return "PENDING_ASSESSMENT";
    }
    if (status == ElderStatus.IN_HOSPITAL
        || status == ElderStatus.OUTING
        || status == ElderStatus.DISCHARGED) {
      return "SIGNED";
    }
    return "PENDING_ASSESSMENT";
  }

  private Integer lifecycleSortRank(ElderProfile elder) {
    if (elder == null) {
      return Integer.MAX_VALUE;
    }
    String lifecycleStatus = ElderLifecycleStatus.normalize(elder.getLifecycleStatus());
    if (ElderLifecycleStatus.INTENT.equals(lifecycleStatus)) return 1;
    if (ElderLifecycleStatus.TRIAL.equals(lifecycleStatus)) return 2;
    if (ElderLifecycleStatus.IN_HOSPITAL.equals(lifecycleStatus)) return 3;
    if (ElderLifecycleStatus.OUTING.equals(lifecycleStatus)) return 4;
    if (ElderLifecycleStatus.MEDICAL_OUTING.equals(lifecycleStatus)) return 5;
    if (ElderLifecycleStatus.DISCHARGE_PENDING.equals(lifecycleStatus)) return 6;
    if (ElderLifecycleStatus.DISCHARGED.equals(lifecycleStatus)) return 7;
    if (ElderLifecycleStatus.DECEASED.equals(lifecycleStatus)) return 8;
    return elder.getStatus() == null ? Integer.MAX_VALUE : elder.getStatus();
  }

  private String normalizeDepartureTypeForLifecycle(String lifecycleStatus, String departureType) {
    String normalizedDepartureType = departureType == null ? null : departureType.trim().toUpperCase();
    String normalizedLifecycleStatus = ElderLifecycleStatus.normalize(lifecycleStatus);
    if (ElderLifecycleStatus.DECEASED.equals(normalizedLifecycleStatus)) {
      return ElderDepartureType.DEATH;
    }
    if (ElderLifecycleStatus.DISCHARGED.equals(normalizedLifecycleStatus) && normalizedDepartureType == null) {
      return ElderDepartureType.NORMAL;
    }
    return normalizedDepartureType;
  }

  private boolean isDischargedRequest(String lifecycleStatus, Integer legacyStatus) {
    String normalizedLifecycleStatus = ElderLifecycleStatus.normalize(lifecycleStatus);
    if (normalizedLifecycleStatus != null) {
      return ElderLifecycleStatus.DISCHARGED.equals(normalizedLifecycleStatus)
          || ElderLifecycleStatus.DECEASED.equals(normalizedLifecycleStatus);
    }
    return Objects.equals(legacyStatus, ElderStatus.DISCHARGED);
  }

  private String normalizeLifecycleStage(String stage) {
    String normalized = stage == null ? "" : stage.trim().toUpperCase();
    if (normalized.isEmpty()) {
      return null;
    }
    if (Objects.equals(normalized, "PENDING_ASSESSMENT")
        || Objects.equals(normalized, "PENDING_BED_SELECT")
        || Objects.equals(normalized, "PENDING_SIGN")
        || Objects.equals(normalized, "SIGNED")) {
      return normalized;
    }
    return null;
  }

  private String resolveLifecycleStageByStatus(String contractStatus, String status) {
    String mergedStatus = firstNonBlank(contractStatus, status);
    if (mergedStatus == null) {
      return null;
    }
    String normalized = mergedStatus.trim().toUpperCase();
    if (normalized.contains("SIGNED") || normalized.contains("EFFECTIVE") || normalized.contains("APPROVED")
        || normalized.contains("签署") || normalized.contains("生效")) {
      return "SIGNED";
    }
    if (normalized.contains("待签") || normalized.contains("签约")) {
      return "PENDING_SIGN";
    }
    if (normalized.contains("入住") || normalized.contains("床位")) {
      return "PENDING_BED_SELECT";
    }
    return "PENDING_ASSESSMENT";
  }

  private String firstNonBlank(String... values) {
    if (values == null || values.length == 0) {
      return null;
    }
    for (String item : values) {
      if (item != null && !item.isBlank()) {
        return item.trim();
      }
    }
    return null;
  }

  private String generateElderCode(Long orgId) {
    Integer max = elderMapper.selectMaxElderCodeNumber(orgId);
    int next = (max == null ? 1000 : max) + 1;
    return "E" + next;
  }
}
