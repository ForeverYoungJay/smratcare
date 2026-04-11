package com.zhiyangyun.care.elder.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderBedRelation;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderBedRelationMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.model.BedReleaseResult;
import com.zhiyangyun.care.elder.model.BedStatus;
import com.zhiyangyun.care.elder.service.ElderOccupancyService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElderOccupancyServiceImpl implements ElderOccupancyService {
  private final ElderMapper elderMapper;
  private final BedMapper bedMapper;
  private final ElderBedRelationMapper relationMapper;

  public ElderOccupancyServiceImpl(
      ElderMapper elderMapper,
      BedMapper bedMapper,
      ElderBedRelationMapper relationMapper) {
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.relationMapper = relationMapper;
  }

  @Override
  @Transactional
  public BedReleaseResult releaseBedAndCloseRelation(
      Long tenantId,
      Long orgId,
      Long elderId,
      LocalDate endDate,
      String reason) {
    BedReleaseResult result = new BedReleaseResult();
    if (elderId == null) {
      return result;
    }

    ElderProfile elder = elderMapper.selectOne(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getId, elderId)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(tenantId != null, ElderProfile::getTenantId, tenantId)
            .eq(orgId != null, ElderProfile::getOrgId, orgId)
            .last("LIMIT 1 FOR UPDATE"));
    if (elder == null) {
      return result;
    }

    int closedRelations = relationMapper.update(
        null,
        Wrappers.lambdaUpdate(ElderBedRelation.class)
            .eq(ElderBedRelation::getIsDeleted, 0)
            .eq(tenantId != null, ElderBedRelation::getTenantId, tenantId)
            .eq(orgId != null, ElderBedRelation::getOrgId, orgId)
            .eq(ElderBedRelation::getElderId, elderId)
            .eq(ElderBedRelation::getActiveFlag, 1)
            .set(ElderBedRelation::getActiveFlag, 0)
            .set(ElderBedRelation::getEndDate, endDate == null ? LocalDate.now() : endDate)
            .set(reason != null && !reason.isBlank(), ElderBedRelation::getRemark, reason));
    result.setClosedRelationCount(closedRelations);

    Long previousBedId = elder.getBedId();
    result.setPreviousBedId(previousBedId);
    if (previousBedId != null) {
      Bed bed = bedMapper.selectOne(
          Wrappers.lambdaQuery(Bed.class)
              .eq(Bed::getId, previousBedId)
              .eq(Bed::getIsDeleted, 0)
              .eq(tenantId != null, Bed::getTenantId, tenantId)
              .eq(orgId != null, Bed::getOrgId, orgId)
              .last("LIMIT 1 FOR UPDATE"));
      if (bed != null && (Objects.equals(bed.getElderId(), elderId) || bed.getElderId() == null)) {
        bed.setElderId(null);
        bed.setStatus(BedStatus.AVAILABLE);
        bed.setOccupancySource(null);
        bed.setOccupancyRefType(null);
        bed.setOccupancyRefId(null);
        bed.setLockExpiresAt(null);
        bed.setOccupancyNote(null);
        bed.setLastReleaseReason(reason);
        bed.setLastReleasedAt(LocalDateTime.now());
        bedMapper.updateById(bed);
        result.setReleasedBedId(bed.getId());
      }
      elder.setBedId(null);
      elderMapper.updateById(elder);
    }

    return result;
  }
}
