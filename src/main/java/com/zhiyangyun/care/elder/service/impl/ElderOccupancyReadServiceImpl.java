package com.zhiyangyun.care.elder.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderBedRelation;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderBedRelationMapper;
import com.zhiyangyun.care.elder.service.ElderOccupancyReadService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ElderOccupancyReadServiceImpl implements ElderOccupancyReadService {
  private final ElderBedRelationMapper elderBedRelationMapper;
  private final BedMapper bedMapper;

  public ElderOccupancyReadServiceImpl(
      ElderBedRelationMapper elderBedRelationMapper,
      BedMapper bedMapper) {
    this.elderBedRelationMapper = elderBedRelationMapper;
    this.bedMapper = bedMapper;
  }

  @Override
  public Map<Long, Bed> buildOccupiedBedMapByElderId(Long orgId, List<Bed> beds) {
    if (beds == null || beds.isEmpty()) {
      return Map.of();
    }
    Map<Long, Bed> bedById = beds.stream()
        .filter(item -> item.getId() != null)
        .collect(Collectors.toMap(Bed::getId, Function.identity(), (a, b) -> a));
    if (bedById.isEmpty()) {
      return Map.of();
    }
    List<ElderBedRelation> activeRelations = elderBedRelationMapper.selectList(
        Wrappers.lambdaQuery(ElderBedRelation.class)
            .eq(ElderBedRelation::getIsDeleted, 0)
            .eq(orgId != null, ElderBedRelation::getOrgId, orgId)
            .eq(ElderBedRelation::getActiveFlag, 1)
            .in(ElderBedRelation::getBedId, bedById.keySet()));
    Map<Long, Bed> occupiedBedMap = new LinkedHashMap<>();
    Set<Long> relationBedIds = activeRelations.stream()
        .map(ElderBedRelation::getBedId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    for (ElderBedRelation relation : activeRelations) {
      if (relation.getElderId() == null || relation.getBedId() == null) {
        continue;
      }
      Bed bed = bedById.get(relation.getBedId());
      if (bed != null) {
        occupiedBedMap.putIfAbsent(relation.getElderId(), bed);
      }
    }
    for (Bed bed : beds) {
      if (bed.getId() == null || relationBedIds.contains(bed.getId()) || bed.getElderId() == null) {
        continue;
      }
      occupiedBedMap.putIfAbsent(bed.getElderId(), bed);
    }
    return occupiedBedMap;
  }

  @Override
  public Map<Long, Bed> loadOccupiedBedMapByElderIds(Long orgId, List<Long> elderIds) {
    if (elderIds == null || elderIds.isEmpty()) {
      return Map.of();
    }
    List<Long> normalizedElderIds = elderIds.stream()
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    if (normalizedElderIds.isEmpty()) {
      return Map.of();
    }
    List<ElderBedRelation> activeRelations = elderBedRelationMapper.selectList(
        Wrappers.lambdaQuery(ElderBedRelation.class)
            .eq(ElderBedRelation::getIsDeleted, 0)
            .eq(orgId != null, ElderBedRelation::getOrgId, orgId)
            .eq(ElderBedRelation::getActiveFlag, 1)
            .in(ElderBedRelation::getElderId, normalizedElderIds));
    Map<Long, Bed> occupiedBedMap = new LinkedHashMap<>();
    List<Long> relationBedIds = activeRelations.stream()
        .map(ElderBedRelation::getBedId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    if (!relationBedIds.isEmpty()) {
      Map<Long, Bed> bedById = bedMapper.selectList(
              Wrappers.lambdaQuery(Bed.class)
                  .eq(Bed::getIsDeleted, 0)
                  .eq(orgId != null, Bed::getOrgId, orgId)
                  .in(Bed::getId, relationBedIds))
          .stream()
          .filter(item -> item.getId() != null)
          .collect(Collectors.toMap(Bed::getId, Function.identity(), (a, b) -> a));
      for (ElderBedRelation relation : activeRelations) {
        if (relation.getElderId() == null || relation.getBedId() == null) {
          continue;
        }
        Bed bed = bedById.get(relation.getBedId());
        if (bed != null) {
          occupiedBedMap.putIfAbsent(relation.getElderId(), bed);
        }
      }
    }
    List<Long> fallbackElderIds = normalizedElderIds.stream()
        .filter(id -> !occupiedBedMap.containsKey(id))
        .toList();
    if (fallbackElderIds.isEmpty()) {
      return occupiedBedMap;
    }
    bedMapper.selectList(
            Wrappers.lambdaQuery(Bed.class)
                .eq(Bed::getIsDeleted, 0)
                .eq(orgId != null, Bed::getOrgId, orgId)
                .in(Bed::getElderId, fallbackElderIds))
        .forEach(item -> {
          if (item.getElderId() != null) {
            occupiedBedMap.putIfAbsent(item.getElderId(), item);
          }
        });
    return occupiedBedMap;
  }

  @Override
  public long countOccupiedBedsByElderIds(Long orgId, List<Bed> beds, Set<Long> elderIds) {
    if (beds == null || beds.isEmpty() || elderIds == null || elderIds.isEmpty()) {
      return 0L;
    }
    return buildOccupiedBedMapByElderId(orgId, beds).entrySet().stream()
        .filter(entry -> elderIds.contains(entry.getKey()))
        .map(Map.Entry::getValue)
        .filter(Objects::nonNull)
        .map(Bed::getId)
        .filter(Objects::nonNull)
        .distinct()
        .count();
  }
}
