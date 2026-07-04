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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElderOccupancyServiceImpl implements ElderOccupancyService {
  private static final String OCCUPANCY_SOURCE_WHOLE_ROOM = "WHOLE_ROOM";
  private static final String OCCUPANCY_REF_TYPE_ELDER = "ELDER";
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

    List<ElderBedRelation> activeRelations = relationMapper.selectList(
        Wrappers.lambdaQuery(ElderBedRelation.class)
            .eq(ElderBedRelation::getIsDeleted, 0)
            .eq(tenantId != null, ElderBedRelation::getTenantId, tenantId)
            .eq(orgId != null, ElderBedRelation::getOrgId, orgId)
            .eq(ElderBedRelation::getElderId, elderId)
            .eq(ElderBedRelation::getActiveFlag, 1)
            .last("FOR UPDATE"));
    Set<Long> relationBackedBedIds = new LinkedHashSet<>();
    activeRelations.stream()
        .map(ElderBedRelation::getBedId)
        .filter(Objects::nonNull)
        .forEach(relationBackedBedIds::add);

    Set<Long> wholeRoomLockedBedIds = new LinkedHashSet<>();
    bedMapper.selectList(
            Wrappers.lambdaQuery(Bed.class)
                .eq(Bed::getIsDeleted, 0)
                .eq(tenantId != null, Bed::getTenantId, tenantId)
                .eq(orgId != null, Bed::getOrgId, orgId)
                .eq(Bed::getOccupancySource, OCCUPANCY_SOURCE_WHOLE_ROOM)
                .eq(Bed::getOccupancyRefType, OCCUPANCY_REF_TYPE_ELDER)
                .eq(Bed::getOccupancyRefId, elderId))
        .stream()
        .map(Bed::getId)
        .filter(Objects::nonNull)
        .forEach(wholeRoomLockedBedIds::add);

    Set<Long> legacyFallbackBedIds = new LinkedHashSet<>();
    if (relationBackedBedIds.isEmpty() && elder.getBedId() != null) {
      legacyFallbackBedIds.add(elder.getBedId());
    }

    Set<Long> bedIdsToRelease = new LinkedHashSet<>();
    bedIdsToRelease.addAll(relationBackedBedIds);
    bedIdsToRelease.addAll(wholeRoomLockedBedIds);
    bedIdsToRelease.addAll(legacyFallbackBedIds);

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

    Long previousBedId = relationBackedBedIds.isEmpty() ? elder.getBedId() : relationBackedBedIds.iterator().next();
    if (previousBedId == null && !bedIdsToRelease.isEmpty()) {
      previousBedId = bedIdsToRelease.iterator().next();
    }
    result.setPreviousBedId(previousBedId);
    if (!bedIdsToRelease.isEmpty()) {
      Long releasedBedId = null;
      LocalDateTime releasedAt = LocalDateTime.now();
      for (Long bedId : bedIdsToRelease) {
        if (bedId == null) {
          continue;
        }
        Bed bed = bedMapper.selectOne(
            Wrappers.lambdaQuery(Bed.class)
                .eq(Bed::getId, bedId)
                .eq(Bed::getIsDeleted, 0)
                .eq(tenantId != null, Bed::getTenantId, tenantId)
                .eq(orgId != null, Bed::getOrgId, orgId)
                .last("LIMIT 1 FOR UPDATE"));
        if (shouldReleaseBed(
            bed,
            elderId,
            relationBackedBedIds.contains(bedId),
            wholeRoomLockedBedIds.contains(bedId),
            legacyFallbackBedIds.contains(bedId))) {
          bed.setElderId(null);
          bed.setStatus(BedStatus.AVAILABLE);
          bed.setOccupancySource(null);
          bed.setOccupancyRefType(null);
          bed.setOccupancyRefId(null);
          bed.setLockExpiresAt(null);
          bed.setOccupancyNote(null);
          bed.setLastReleaseReason(reason);
          bed.setLastReleasedAt(releasedAt);
          bedMapper.updateById(bed);
          if (releasedBedId == null) {
            releasedBedId = bed.getId();
          }
        }
      }
      result.setReleasedBedId(releasedBedId);
      elder.setBedId(null);
      elderMapper.updateById(elder);
    } else if (previousBedId != null) {
      Bed bed = bedMapper.selectOne(
          Wrappers.lambdaQuery(Bed.class)
              .eq(Bed::getId, previousBedId)
              .eq(Bed::getIsDeleted, 0)
              .eq(tenantId != null, Bed::getTenantId, tenantId)
              .eq(orgId != null, Bed::getOrgId, orgId)
              .last("LIMIT 1 FOR UPDATE"));
      if (shouldReleaseBed(
          bed,
          elderId,
          relationBackedBedIds.contains(previousBedId),
          wholeRoomLockedBedIds.contains(previousBedId),
          legacyFallbackBedIds.contains(previousBedId))) {
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

  private boolean shouldReleaseBed(
      Bed bed,
      Long elderId,
      boolean relationBacked,
      boolean wholeRoomLocked,
      boolean legacyFallback) {
    if (bed == null) {
      return false;
    }
    if (relationBacked || wholeRoomLocked) {
      return true;
    }
    if (Objects.equals(bed.getElderId(), elderId)) {
      return true;
    }
    return legacyFallback && bed.getElderId() == null;
  }
}
