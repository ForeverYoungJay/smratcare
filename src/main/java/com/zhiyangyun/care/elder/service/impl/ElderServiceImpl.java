package com.zhiyangyun.care.elder.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElderServiceImpl implements ElderService {
  private final ElderMapper elderMapper;
  private final BedMapper bedMapper;
  private final ElderBedRelationMapper relationMapper;
  private final RoomMapper roomMapper;
  private final ElderChangeLogMapper changeLogMapper;

  public ElderServiceImpl(ElderMapper elderMapper, BedMapper bedMapper,
      ElderBedRelationMapper relationMapper, RoomMapper roomMapper, ElderChangeLogMapper changeLogMapper) {
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.relationMapper = relationMapper;
    this.roomMapper = roomMapper;
    this.changeLogMapper = changeLogMapper;
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
    elder.setAdmissionDate(request.getAdmissionDate());
    elder.setStatus(request.getStatus());
    elder.setCareLevel(request.getCareLevel());
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
    if (request.getAdmissionDate() != null) {
      elder.setAdmissionDate(request.getAdmissionDate());
    }
    if (request.getStatus() != null) {
      elder.setStatus(request.getStatus());
    }
    if (request.getCareLevel() != null) {
      elder.setCareLevel(request.getCareLevel());
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
  public IPage<ElderResponse> page(Long tenantId, long pageNo, long pageSize, String keyword) {
    var wrapper = Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(tenantId != null, ElderProfile::getTenantId, tenantId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(ElderProfile::getFullName, keyword)
          .or().like(ElderProfile::getElderCode, keyword)
          .or().like(ElderProfile::getPhone, keyword));
    }
    wrapper.orderByDesc(ElderProfile::getCreateTime);
    IPage<ElderProfile> page = elderMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(elder -> {
      Bed bed = elder.getBedId() == null ? null : bedMapper.selectById(elder.getBedId());
      return toResponse(elder, bed);
    });
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
    response.setAdmissionDate(elder.getAdmissionDate());
    response.setStatus(elder.getStatus());
    response.setBedId(elder.getBedId());
    response.setCareLevel(elder.getCareLevel());
    response.setRemark(elder.getRemark());
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

  private String generateElderCode(Long orgId) {
    Integer max = elderMapper.selectMaxElderCodeNumber(orgId);
    int next = (max == null ? 1000 : max) + 1;
    return "E" + next;
  }
}
