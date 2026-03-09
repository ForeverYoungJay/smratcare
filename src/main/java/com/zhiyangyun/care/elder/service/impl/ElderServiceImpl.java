package com.zhiyangyun.care.elder.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderBedRelation;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderBedRelationMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderChangeLogMapper;
import com.zhiyangyun.care.elder.model.AssignBedRequest;
import com.zhiyangyun.care.elder.model.BedResponse;
import com.zhiyangyun.care.elder.model.ElderCreateRequest;
import com.zhiyangyun.care.elder.model.ElderResponse;
import com.zhiyangyun.care.elder.model.ElderUpdateRequest;
import com.zhiyangyun.care.elder.service.ElderService;
import com.zhiyangyun.care.elder.util.QrCodeUtil;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElderServiceImpl implements ElderService {
  private static final String ELDER_NAME_PINYIN_BOUNDARY = "阿芭擦搭蛾发噶哈讥咔垃妈拿哦啪期然撒塌挖昔压匝";
  private static final String ELDER_NAME_PINYIN_INITIAL = "ABCDEFGHJKLMNOPQRSTWXYZ";

  private final ElderMapper elderMapper;
  private final BedMapper bedMapper;
  private final ElderBedRelationMapper relationMapper;
  private final RoomMapper roomMapper;
  private final ElderChangeLogMapper changeLogMapper;
  private final CrmContractMapper crmContractMapper;

  public ElderServiceImpl(ElderMapper elderMapper, BedMapper bedMapper,
      ElderBedRelationMapper relationMapper, RoomMapper roomMapper, ElderChangeLogMapper changeLogMapper,
      CrmContractMapper crmContractMapper) {
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.relationMapper = relationMapper;
    this.roomMapper = roomMapper;
    this.changeLogMapper = changeLogMapper;
    this.crmContractMapper = crmContractMapper;
  }

  @Override
  public ElderResponse create(ElderCreateRequest request) {
    ElderProfile elder = new ElderProfile();
    elder.setTenantId(request.getTenantId());
    elder.setOrgId(request.getOrgId());
    String elderCode = request.getElderCode();
    if (elderCode == null || elderCode.isBlank()) {
      elderCode = generateElderCode(request.getOrgId());
    }
    elder.setElderCode(elderCode);
    elder.setElderQrCode(QrCodeUtil.generate());
    elder.setFullName(request.getFullName());
    elder.setIdCardNo(request.getIdCardNo());
    elder.setGender(request.getGender());
    elder.setBirthDate(request.getBirthDate());
    elder.setPhone(request.getPhone());
    elder.setHomeAddress(request.getHomeAddress());
    elder.setMedicalInsuranceCopyUrl(request.getMedicalInsuranceCopyUrl());
    elder.setHouseholdCopyUrl(request.getHouseholdCopyUrl());
    elder.setMedicalRecordFileUrl(request.getMedicalRecordFileUrl());
    elder.setAdmissionDate(request.getAdmissionDate());
    elder.setStatus(request.getStatus());
    elder.setCareLevel(request.getCareLevel());
    elder.setRiskPrecommit(request.getRiskPrecommit());
    elder.setRemark(request.getRemark());
    elder.setCreatedBy(request.getCreatedBy());
    elderMapper.insert(elder);
    if (request.getBedId() != null) {
      AssignBedRequest assign = new AssignBedRequest();
      assign.setTenantId(request.getTenantId());
      assign.setCreatedBy(request.getCreatedBy());
      assign.setBedId(request.getBedId());
      LocalDate startDate = request.getBedStartDate();
      if (startDate == null) {
        startDate = request.getAdmissionDate() == null ? LocalDate.now() : request.getAdmissionDate();
      }
      assign.setStartDate(startDate);
      ElderResponse assigned = assignBed(elder.getId(), assign);
      return assigned == null ? toResponse(elder, null) : assigned;
    }
    return toResponse(elder, null);
  }

  @Override
  public ElderResponse update(Long id, ElderUpdateRequest request) {
    ElderProfile elder = elderMapper.selectById(id);
    if (elder == null) {
      return null;
    }
    if (request.getTenantId() != null && !request.getTenantId().equals(elder.getTenantId())) {
      throw new IllegalArgumentException("无权限访问该老人");
    }
    if (request.getFullName() != null) {
      elder.setFullName(request.getFullName());
    }
    if (request.getIdCardNo() != null) {
      elder.setIdCardNo(request.getIdCardNo());
    }
    if (request.getGender() != null) {
      elder.setGender(request.getGender());
    }
    if (request.getBirthDate() != null) {
      elder.setBirthDate(request.getBirthDate());
    }
    if (request.getPhone() != null) {
      elder.setPhone(request.getPhone());
    }
    if (request.getHomeAddress() != null) {
      elder.setHomeAddress(request.getHomeAddress());
    }
    if (request.getMedicalInsuranceCopyUrl() != null) {
      elder.setMedicalInsuranceCopyUrl(request.getMedicalInsuranceCopyUrl());
    }
    if (request.getHouseholdCopyUrl() != null) {
      elder.setHouseholdCopyUrl(request.getHouseholdCopyUrl());
    }
    if (request.getMedicalRecordFileUrl() != null) {
      elder.setMedicalRecordFileUrl(request.getMedicalRecordFileUrl());
    }
    if (request.getAdmissionDate() != null) {
      elder.setAdmissionDate(request.getAdmissionDate());
    }
    if (request.getStatus() != null) {
      elder.setStatus(request.getStatus());
    }
    if (request.getCareLevel() != null) {
      elder.setCareLevel(request.getCareLevel());
    }
    if (request.getRiskPrecommit() != null) {
      elder.setRiskPrecommit(request.getRiskPrecommit());
    }
    if (request.getRemark() != null) {
      elder.setRemark(request.getRemark());
    }
    elderMapper.updateById(elder);
    if (request.getStatus() != null && request.getStatus() == 3 && elder.getBedId() != null) {
      return unbindBed(elder.getId(), LocalDate.now(), "状态变更退住", request.getTenantId(), request.getUpdatedBy());
    }
    if (request.getBedId() != null && !request.getBedId().equals(elder.getBedId())) {
      AssignBedRequest assign = new AssignBedRequest();
      assign.setTenantId(request.getTenantId());
      assign.setCreatedBy(request.getUpdatedBy());
      assign.setBedId(request.getBedId());
      LocalDate startDate = request.getBedStartDate();
      if (startDate == null) {
        startDate = LocalDate.now();
      }
      assign.setStartDate(startDate);
      ElderResponse assigned = assignBed(elder.getId(), assign);
      return assigned == null ? toResponse(elder, null) : assigned;
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
    return toResponse(elder, bed);
  }

  @Override
  public IPage<ElderResponse> page(Long tenantId, long pageNo, long pageSize, String keyword, Boolean signedOnly, Integer status) {
    var baseWrapper = Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(tenantId != null, ElderProfile::getTenantId, tenantId)
        .eq(status != null, ElderProfile::getStatus, status);
    if (Boolean.TRUE.equals(signedOnly)) {
      List<CrmContract> signedContracts = crmContractMapper.selectList(Wrappers.lambdaQuery(CrmContract.class)
          .eq(CrmContract::getIsDeleted, 0)
          .eq(tenantId != null, CrmContract::getTenantId, tenantId)
          .isNotNull(CrmContract::getElderId)
          .in(CrmContract::getStatus, List.of("SIGNED", "EFFECTIVE")));
      Set<Long> signedElderIds = new HashSet<>(signedContracts.stream()
          .map(CrmContract::getElderId)
          .filter(Objects::nonNull)
          .toList());
      if (signedElderIds.isEmpty()) {
        return new Page<>(pageNo, pageSize);
      }
      baseWrapper.in(ElderProfile::getId, signedElderIds);
    }
    String normalizedKeyword = keyword == null ? "" : keyword.trim();
    if (!normalizedKeyword.isEmpty()) {
      var wrapper = Wrappers.lambdaQuery(ElderProfile.class)
          .eq(ElderProfile::getIsDeleted, 0)
          .eq(tenantId != null, ElderProfile::getTenantId, tenantId)
          .eq(status != null, ElderProfile::getStatus, status);
      if (Boolean.TRUE.equals(signedOnly)) {
        List<CrmContract> signedContracts = crmContractMapper.selectList(Wrappers.lambdaQuery(CrmContract.class)
            .eq(CrmContract::getIsDeleted, 0)
            .eq(tenantId != null, CrmContract::getTenantId, tenantId)
            .isNotNull(CrmContract::getElderId)
            .in(CrmContract::getStatus, List.of("SIGNED", "EFFECTIVE")));
        Set<Long> signedElderIds = new HashSet<>(signedContracts.stream()
            .map(CrmContract::getElderId)
            .filter(Objects::nonNull)
            .toList());
        if (signedElderIds.isEmpty()) {
          return new Page<>(pageNo, pageSize);
        }
        wrapper.in(ElderProfile::getId, signedElderIds);
      }
      wrapper.and(w -> w.like(ElderProfile::getFullName, normalizedKeyword)
          .or().like(ElderProfile::getElderCode, normalizedKeyword)
          .or().like(ElderProfile::getPhone, normalizedKeyword));
      wrapper.orderByDesc(ElderProfile::getCreateTime);
      IPage<ElderProfile> page = elderMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
      if (page.getTotal() > 0 || !isAlphabeticKeyword(normalizedKeyword)) {
        return toPagedResponse(page, tenantId);
      }
      List<ElderProfile> allRows = elderMapper.selectList(baseWrapper.orderByDesc(ElderProfile::getCreateTime));
      List<ElderProfile> filtered = allRows.stream()
          .filter(elder -> matchByPinyinOrText(elder, normalizedKeyword))
          .sorted(Comparator.comparing(ElderProfile::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
          .collect(Collectors.toList());
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
    baseWrapper.orderByDesc(ElderProfile::getCreateTime);
    IPage<ElderProfile> page = elderMapper.selectPage(new Page<>(pageNo, pageSize), baseWrapper);
    return toPagedResponse(page, tenantId);
  }

  private IPage<ElderResponse> toPagedResponse(IPage<ElderProfile> page, Long tenantId) {
    List<ElderProfile> records = page.getRecords();
    Map<Long, Bed> bedMap = resolveBedMap(records);
    Map<Long, CrmContract> contractMap = resolveLatestContractMap(records, tenantId);
    List<ElderResponse> responseRecords = records.stream().map((elder) -> {
      Bed bed = elder.getBedId() == null ? null : bedMap.get(elder.getBedId());
      ElderResponse response = toResponse(elder, bed);
      applyLifecycleFromContract(response, contractMap.get(elder.getId()));
      return response;
    }).toList();
    Page<ElderResponse> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    responsePage.setRecords(responseRecords);
    return responsePage;
  }

  private Map<Long, Bed> resolveBedMap(List<ElderProfile> elders) {
    Set<Long> bedIds = elders.stream()
        .map(ElderProfile::getBedId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    if (bedIds.isEmpty()) {
      return Map.of();
    }
    return bedMapper.selectBatchIds(bedIds).stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Bed::getId, item -> item, (left, right) -> left));
  }

  private Map<Long, CrmContract> resolveLatestContractMap(List<ElderProfile> elders, Long tenantId) {
    Set<Long> elderIds = elders.stream()
        .map(ElderProfile::getId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    if (elderIds.isEmpty()) {
      return Map.of();
    }
    List<CrmContract> contracts = crmContractMapper.selectList(Wrappers.lambdaQuery(CrmContract.class)
        .eq(CrmContract::getIsDeleted, 0)
        .eq(tenantId != null, CrmContract::getTenantId, tenantId)
        .in(CrmContract::getElderId, elderIds)
        .orderByDesc(CrmContract::getUpdateTime)
        .orderByDesc(CrmContract::getCreateTime)
        .orderByDesc(CrmContract::getId));
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

  private boolean isAlphabeticKeyword(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return false;
    }
    return keyword.matches("[A-Za-z]+");
  }

  private boolean matchByPinyinOrText(ElderProfile elder, String keyword) {
    String fullName = elder.getFullName() == null ? "" : elder.getFullName();
    String elderCode = elder.getElderCode() == null ? "" : elder.getElderCode();
    String phone = elder.getPhone() == null ? "" : elder.getPhone();
    String searchText = (fullName + " " + elderCode + " " + phone + " " + toPinyinInitials(fullName)).toLowerCase();
    return searchText.contains(keyword.toLowerCase());
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
    ElderProfile elder = elderMapper.selectById(elderId);
    if (elder == null) {
      return null;
    }
    if (request.getTenantId() != null && !request.getTenantId().equals(elder.getTenantId())) {
      throw new IllegalStateException("Org mismatch");
    }
    Bed bed = bedMapper.selectById(request.getBedId());
    if (bed == null) {
      return null;
    }
    if (request.getTenantId() != null && !request.getTenantId().equals(bed.getTenantId())) {
      throw new IllegalStateException("Org mismatch");
    }
    if (!elder.getOrgId().equals(bed.getOrgId())) {
      throw new IllegalStateException("Org mismatch");
    }
    ensureNoDuplicateNameOccupied(elder);

    long occupied = bedMapper.selectCount(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getRoomId, bed.getRoomId())
        .eq(Bed::getStatus, 2)
        .eq(Bed::getIsDeleted, 0));
    com.zhiyangyun.care.elder.entity.Room room = roomMapper.selectById(bed.getRoomId());
    if (room != null && room.getCapacity() != null && occupied >= room.getCapacity()) {
      throw new IllegalStateException("Room capacity exceeded");
    }
    ElderBedRelation bedActive = relationMapper.selectOne(
        Wrappers.lambdaQuery(ElderBedRelation.class)
            .eq(ElderBedRelation::getOrgId, elder.getOrgId())
            .eq(ElderBedRelation::getTenantId, elder.getTenantId())
            .eq(ElderBedRelation::getBedId, bed.getId())
            .eq(ElderBedRelation::getActiveFlag, 1)
            .eq(ElderBedRelation::getIsDeleted, 0));
    if (bedActive != null && !bedActive.getElderId().equals(elder.getId())) {
      throw new IllegalStateException("Bed already assigned");
    }
    if (bed.getElderId() != null && !bed.getElderId().equals(elder.getId())) {
      throw new IllegalStateException("Bed already assigned");
    }
    if (bed.getStatus() != null && bed.getStatus() != 1) {
      throw new IllegalStateException("Bed is not available");
    }

    ElderBedRelation elderActive = relationMapper.selectOne(
        Wrappers.lambdaQuery(ElderBedRelation.class)
            .eq(ElderBedRelation::getOrgId, elder.getOrgId())
            .eq(ElderBedRelation::getTenantId, elder.getTenantId())
            .eq(ElderBedRelation::getElderId, elder.getId())
            .eq(ElderBedRelation::getActiveFlag, 1)
            .eq(ElderBedRelation::getIsDeleted, 0));
    if (elderActive != null) {
      elderActive.setActiveFlag(0);
      elderActive.setEndDate(request.getStartDate().minusDays(1));
      relationMapper.updateById(elderActive);

      Bed oldBed = bedMapper.selectById(elderActive.getBedId());
      if (oldBed != null) {
        oldBed.setElderId(null);
        oldBed.setStatus(1);
        bedMapper.updateById(oldBed);
      }
    }

    ElderBedRelation relation = new ElderBedRelation();
    relation.setTenantId(elder.getTenantId());
    relation.setOrgId(elder.getOrgId());
    relation.setElderId(elder.getId());
    relation.setBedId(bed.getId());
    relation.setStartDate(request.getStartDate());
    relation.setActiveFlag(1);
    relation.setCreatedBy(request.getCreatedBy());
    relationMapper.insert(relation);

    bed.setElderId(elder.getId());
    bed.setStatus(2);
    bedMapper.updateById(bed);

    elder.setBedId(bed.getId());
    if (elder.getStatus() == null || elder.getStatus() == 3) {
      elder.setStatus(1);
    }
    elderMapper.updateById(elder);

    insertChangeLog(elder.getTenantId(), elder.getOrgId(), elder.getId(), request.getCreatedBy(),
        "BED_CHANGE", null, String.valueOf(bed.getId()), "床位分配");
    return toResponse(elder, bed);
  }

  private void ensureNoDuplicateNameOccupied(ElderProfile elder) {
    return;
  }

  @Override
  @Transactional
  public ElderResponse unbindBed(Long elderId, LocalDate endDate, String reason, Long tenantId, Long createdBy) {
    ElderProfile elder = elderMapper.selectById(elderId);
    if (elder == null) {
      return null;
    }
    ElderBedRelation elderActive = relationMapper.selectOne(
        Wrappers.lambdaQuery(ElderBedRelation.class)
            .eq(ElderBedRelation::getOrgId, elder.getOrgId())
            .eq(ElderBedRelation::getTenantId, elder.getTenantId())
            .eq(ElderBedRelation::getElderId, elder.getId())
            .eq(ElderBedRelation::getActiveFlag, 1)
            .eq(ElderBedRelation::getIsDeleted, 0));
    if (elderActive != null) {
      elderActive.setActiveFlag(0);
      elderActive.setEndDate(endDate == null ? LocalDate.now() : endDate);
      if (reason != null && !reason.isBlank()) {
        elderActive.setRemark(reason);
      }
      relationMapper.updateById(elderActive);
    }

    if (elder.getBedId() != null) {
      Bed bed = bedMapper.selectById(elder.getBedId());
      if (bed != null) {
        bed.setElderId(null);
        bed.setStatus(1);
        bedMapper.updateById(bed);
      }
    }

    Long previousBedId = elder.getBedId();
    elder.setBedId(null);
    elder.setStatus(3);
    elderMapper.updateById(elder);
    insertChangeLog(elder.getTenantId(), elder.getOrgId(), elder.getId(), createdBy,
        "BED_CHANGE", previousBedId == null ? null : String.valueOf(previousBedId), null, reason);
    return toResponse(elder, null);
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
    response.setBedId(elder.getBedId());
    response.setCareLevel(elder.getCareLevel());
    response.setRiskPrecommit(elder.getRiskPrecommit());
    response.setRemark(elder.getRemark());
    response.setLifecycleStage(resolveLifecycleStageByElderStatus(elder.getStatus()));
    response.setLifecycleContractStatus(null);
    if (bed != null) {
      BedResponse bedResponse = new BedResponse();
      bedResponse.setId(bed.getId());
      bedResponse.setOrgId(bed.getOrgId());
      bedResponse.setRoomId(bed.getRoomId());
      bedResponse.setBedNo(bed.getBedNo());
      bedResponse.setBedQrCode(bed.getBedQrCode());
      bedResponse.setStatus(bed.getStatus());
      bedResponse.setElderId(bed.getElderId());
      response.setCurrentBed(bedResponse);
    }
    return response;
  }

  private String resolveLifecycleStageByElderStatus(Integer status) {
    if (status == null) {
      return "PENDING_ASSESSMENT";
    }
    if (status == 1 || status == 2 || status == 3) {
      return "SIGNED";
    }
    return "PENDING_ASSESSMENT";
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
