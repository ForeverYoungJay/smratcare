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

  public ElderServiceImpl(ElderMapper elderMapper, BedMapper bedMapper,
      ElderBedRelationMapper relationMapper, RoomMapper roomMapper) {
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.relationMapper = relationMapper;
    this.roomMapper = roomMapper;
  }

  @Override
  public ElderResponse create(ElderCreateRequest request) {
    ElderProfile elder = new ElderProfile();
    elder.setOrgId(request.getOrgId());
    elder.setElderCode(request.getElderCode());
    elder.setElderQrCode(QrCodeUtil.generate());
    elder.setFullName(request.getFullName());
    elder.setIdCardNo(request.getIdCardNo());
    elder.setGender(request.getGender());
    elder.setBirthDate(request.getBirthDate());
    elder.setPhone(request.getPhone());
    elder.setAdmissionDate(request.getAdmissionDate());
    elder.setStatus(request.getStatus());
    elder.setCareLevel(request.getCareLevel());
    elder.setRemark(request.getRemark());
    elderMapper.insert(elder);
    return toResponse(elder, null);
  }

  @Override
  public ElderResponse update(Long id, ElderUpdateRequest request) {
    ElderProfile elder = elderMapper.selectById(id);
    if (elder == null) {
      return null;
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
    Bed bed = elder.getBedId() == null ? null : bedMapper.selectById(elder.getBedId());
    return toResponse(elder, bed);
  }

  @Override
  public ElderResponse get(Long id) {
    ElderProfile elder = elderMapper.selectById(id);
    if (elder == null) {
      return null;
    }
    Bed bed = elder.getBedId() == null ? null : bedMapper.selectById(elder.getBedId());
    return toResponse(elder, bed);
  }

  @Override
  public IPage<ElderResponse> page(long pageNo, long pageSize, String keyword) {
    var wrapper = Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(ElderProfile::getFullName, keyword)
          .or().like(ElderProfile::getElderCode, keyword)
          .or().like(ElderProfile::getPhone, keyword));
    }
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
    Bed bed = bedMapper.selectById(request.getBedId());
    if (bed == null) {
      return null;
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
    relation.setOrgId(elder.getOrgId());
    relation.setElderId(elder.getId());
    relation.setBedId(bed.getId());
    relation.setStartDate(request.getStartDate());
    relation.setActiveFlag(1);
    relationMapper.insert(relation);

    bed.setElderId(elder.getId());
    bed.setStatus(2);
    bedMapper.updateById(bed);

    elder.setBedId(bed.getId());
    elderMapper.updateById(elder);
    return toResponse(elder, bed);
  }

  @Override
  @Transactional
  public ElderResponse unbindBed(Long elderId, LocalDate endDate, String reason) {
    ElderProfile elder = elderMapper.selectById(elderId);
    if (elder == null) {
      return null;
    }
    ElderBedRelation elderActive = relationMapper.selectOne(
        Wrappers.lambdaQuery(ElderBedRelation.class)
            .eq(ElderBedRelation::getOrgId, elder.getOrgId())
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

    elder.setBedId(null);
    elder.setStatus(3);
    elderMapper.updateById(elder);
    return toResponse(elder, null);
  }

  private ElderResponse toResponse(ElderProfile elder, Bed bed) {
    ElderResponse response = new ElderResponse();
    response.setId(elder.getId());
    response.setOrgId(elder.getOrgId());
    response.setElderCode(elder.getElderCode());
    response.setElderQrCode(elder.getElderQrCode());
    response.setFullName(elder.getFullName());
    response.setIdCardNo(elder.getIdCardNo());
    response.setGender(elder.getGender());
    response.setBirthDate(elder.getBirthDate());
    response.setPhone(elder.getPhone());
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
}
