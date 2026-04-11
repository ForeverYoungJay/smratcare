package com.zhiyangyun.care.elder.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderBedRelation;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderBedRelationMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.model.BedStatus;
import com.zhiyangyun.care.elder.model.OccupancyHealthIssueResponse;
import com.zhiyangyun.care.elder.model.OccupancyRepairResponse;
import com.zhiyangyun.care.elder.service.OccupancyConsistencyService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OccupancyConsistencyServiceImpl implements OccupancyConsistencyService {
  private static final String OCCUPANCY_SOURCE_RELATION = "RELATION";
  private static final String OCCUPANCY_REF_TYPE_RELATION = "ELDER_BED_RELATION";

  private final ElderMapper elderMapper;
  private final BedMapper bedMapper;
  private final ElderBedRelationMapper relationMapper;

  public OccupancyConsistencyServiceImpl(
      ElderMapper elderMapper,
      BedMapper bedMapper,
      ElderBedRelationMapper relationMapper) {
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.relationMapper = relationMapper;
  }

  @Override
  public List<OccupancyHealthIssueResponse> inspect(Long tenantId, Long orgId, Long elderId, Long bedId) {
    ScopeData scope = loadScope(tenantId, orgId, elderId, bedId);
    return buildIssues(scope);
  }

  @Override
  @Transactional
  public OccupancyRepairResponse repair(
      Long tenantId,
      Long orgId,
      Long elderId,
      Long bedId,
      String reason,
      Long operatorId) {
    ScopeData scope = loadScope(tenantId, orgId, elderId, bedId);
    OccupancyRepairResponse response = new OccupancyRepairResponse();
    String normalizedReason = reason == null || reason.isBlank() ? "占床一致性修复" : reason.trim();

    for (ElderBedRelation relation : scope.relationsById.values()) {
      ElderProfile elder = scope.eldersById.get(relation.getElderId());
      Bed bed = scope.bedsById.get(relation.getBedId());
      if (elder != null && !Objects.equals(elder.getBedId(), relation.getBedId())) {
        elder.setBedId(relation.getBedId());
        elderMapper.updateById(elder);
        response.setFixedElderProjectionCount(response.getFixedElderProjectionCount() + 1);
        response.getActions().add("老人#" + elder.getId() + "床位投影对齐到关系#" + relation.getId());
      }
      if (bed != null) {
        boolean changed = false;
        if (!Objects.equals(bed.getElderId(), relation.getElderId())) {
          bed.setElderId(relation.getElderId());
          changed = true;
        }
        if (bed.getStatus() == null || !Objects.equals(bed.getStatus(), BedStatus.OCCUPIED)) {
          bed.setStatus(BedStatus.OCCUPIED);
          changed = true;
        }
        if (!Objects.equals(bed.getOccupancyRefId(), relation.getId())) {
          bed.setOccupancySource(OCCUPANCY_SOURCE_RELATION);
          bed.setOccupancyRefType(OCCUPANCY_REF_TYPE_RELATION);
          bed.setOccupancyRefId(relation.getId());
          bed.setOccupancyNote(normalizedReason);
          changed = true;
        }
        if (changed) {
          bedMapper.updateById(bed);
          response.setFixedBedProjectionCount(response.getFixedBedProjectionCount() + 1);
          response.getActions().add("床位#" + bed.getId() + "占用投影对齐到关系#" + relation.getId());
        }
      }
    }

    Set<Long> relatedElderIds = scope.relationsById.values().stream()
        .map(ElderBedRelation::getElderId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    for (ElderProfile elder : scope.eldersById.values()) {
      if (elder.getBedId() != null && !relatedElderIds.contains(elder.getId())) {
        Long previousBedId = elder.getBedId();
        elder.setBedId(null);
        elderMapper.updateById(elder);
        response.setClearedOrphanElderCount(response.getClearedOrphanElderCount() + 1);
        response.getActions().add("清理老人#" + elder.getId() + "孤立床位投影#" + previousBedId);
      }
    }

    Set<Long> relatedBedIds = scope.relationsById.values().stream()
        .map(ElderBedRelation::getBedId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    for (Bed bed : scope.bedsById.values()) {
      boolean orphanOccupied = (bed.getElderId() != null || Objects.equals(bed.getStatus(), BedStatus.OCCUPIED))
          && !relatedBedIds.contains(bed.getId());
      if (!orphanOccupied) {
        continue;
      }
      bed.setElderId(null);
      if (Objects.equals(bed.getStatus(), BedStatus.OCCUPIED)) {
        bed.setStatus(BedStatus.AVAILABLE);
      }
      bed.setOccupancySource(null);
      bed.setOccupancyRefType(null);
      bed.setOccupancyRefId(null);
      bed.setOccupancyNote(normalizedReason);
      bed.setLastReleaseReason(normalizedReason);
      bed.setLastReleasedAt(LocalDateTime.now());
      bedMapper.updateById(bed);
      response.setReleasedOrphanBedCount(response.getReleasedOrphanBedCount() + 1);
      response.getActions().add("释放床位#" + bed.getId() + "孤立占用");
    }

    if (response.getActions().isEmpty()) {
      response.getActions().add("未发现需要修复的问题");
    }
    return response;
  }

  private List<OccupancyHealthIssueResponse> buildIssues(ScopeData scope) {
    List<OccupancyHealthIssueResponse> issues = new ArrayList<>();

    Map<Long, Long> elderRelationCount = scope.relationsById.values().stream()
        .map(ElderBedRelation::getElderId)
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
    elderRelationCount.forEach((elderId, count) -> {
      if (count > 1) {
        ElderProfile elder = scope.eldersById.get(elderId);
        issues.add(issue("MULTIPLE_ACTIVE_RELATIONS_ELDER", "HIGH", elder, null, null,
            "老人存在 " + count + " 条激活占床关系",
            "优先清理重复激活关系，并以最新有效关系为准重建投影"));
      }
    });

    Map<Long, Long> bedRelationCount = scope.relationsById.values().stream()
        .map(ElderBedRelation::getBedId)
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
    bedRelationCount.forEach((bedId, count) -> {
      if (count > 1) {
        Bed bed = scope.bedsById.get(bedId);
        issues.add(issue("MULTIPLE_ACTIVE_RELATIONS_BED", "HIGH", null, bed, null,
            "床位存在 " + count + " 条激活占用关系",
            "优先清理重复激活关系，避免多老人共占同床"));
      }
    });

    for (ElderBedRelation relation : scope.relationsById.values()) {
      ElderProfile elder = scope.eldersById.get(relation.getElderId());
      Bed bed = scope.bedsById.get(relation.getBedId());
      if (elder == null) {
        issues.add(issue("RELATION_ORPHAN_ELDER", "HIGH", null, bed, relation,
            "激活关系指向的老人不存在或已删除",
            "核查关系来源后关闭异常关系"));
        continue;
      }
      if (bed == null) {
        issues.add(issue("RELATION_ORPHAN_BED", "HIGH", elder, null, relation,
            "激活关系指向的床位不存在或已删除",
            "核查关系来源后关闭异常关系并清理老人床位投影"));
        continue;
      }
      if (!Objects.equals(elder.getBedId(), relation.getBedId())) {
        issues.add(issue("RELATION_ELDER_MISMATCH", "MEDIUM", elder, bed, relation,
            "老人档案床位投影与激活关系不一致",
            "以激活关系为准回写 elder.bed_id"));
      }
      if (!Objects.equals(bed.getElderId(), relation.getElderId())
          || !Objects.equals(bed.getStatus(), BedStatus.OCCUPIED)) {
        issues.add(issue("RELATION_BED_MISMATCH", "MEDIUM", elder, bed, relation,
            "床位占用投影与激活关系不一致",
            "以激活关系为准回写 bed.elder_id 与状态"));
      }
    }

    Set<Long> relatedElderIds = scope.relationsById.values().stream()
        .map(ElderBedRelation::getElderId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    for (ElderProfile elder : scope.eldersById.values()) {
      if (elder.getBedId() != null && !relatedElderIds.contains(elder.getId())) {
        Bed bed = scope.bedsById.get(elder.getBedId());
        issues.add(issue("ELDER_ORPHAN_BED_POINTER", "MEDIUM", elder, bed, null,
            "老人档案存在床位投影，但没有激活占床关系",
            "如无真实占床，应清理 elder.bed_id；如真实在住，应补建激活关系"));
      }
    }

    Set<Long> relatedBedIds = scope.relationsById.values().stream()
        .map(ElderBedRelation::getBedId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    for (Bed bed : scope.bedsById.values()) {
      boolean orphanOccupied = (bed.getElderId() != null || Objects.equals(bed.getStatus(), BedStatus.OCCUPIED))
          && !relatedBedIds.contains(bed.getId());
      if (orphanOccupied) {
        ElderProfile elder = bed.getElderId() == null ? null : scope.eldersById.get(bed.getElderId());
        issues.add(issue("BED_ORPHAN_OCCUPANCY", "MEDIUM", elder, bed, null,
            "床位显示已占用，但没有激活占床关系",
            "如无真实占床，应释放床位；如真实在住，应补建激活关系"));
      }
    }

    return issues;
  }

  private ScopeData loadScope(Long tenantId, Long orgId, Long elderId, Long bedId) {
    ScopeData scope = new ScopeData();
    List<ElderBedRelation> relations = relationMapper.selectList(Wrappers.lambdaQuery(ElderBedRelation.class)
        .eq(ElderBedRelation::getIsDeleted, 0)
        .eq(tenantId != null, ElderBedRelation::getTenantId, tenantId)
        .eq(orgId != null, ElderBedRelation::getOrgId, orgId)
        .eq(ElderBedRelation::getActiveFlag, 1)
        .eq(elderId != null, ElderBedRelation::getElderId, elderId)
        .eq(bedId != null, ElderBedRelation::getBedId, bedId));
    scope.relationsById = relations.stream()
        .collect(Collectors.toMap(ElderBedRelation::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

    List<ElderProfile> elders = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(tenantId != null, ElderProfile::getTenantId, tenantId)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .and(elderId != null || bedId != null, q -> {
          if (elderId != null) {
            q.eq(ElderProfile::getId, elderId);
          }
          if (bedId != null) {
            if (elderId != null) {
              q.or();
            }
            q.eq(ElderProfile::getBedId, bedId);
          }
        }));
    if (elderId == null && bedId == null) {
      elders = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
          .eq(ElderProfile::getIsDeleted, 0)
          .eq(tenantId != null, ElderProfile::getTenantId, tenantId)
          .eq(orgId != null, ElderProfile::getOrgId, orgId)
          .isNotNull(ElderProfile::getBedId));
    }
    scope.eldersById = elders.stream()
        .collect(Collectors.toMap(ElderProfile::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

    List<Bed> beds = bedMapper.selectList(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(tenantId != null, Bed::getTenantId, tenantId)
        .eq(orgId != null, Bed::getOrgId, orgId)
        .and(elderId != null || bedId != null, q -> {
          if (bedId != null) {
            q.eq(Bed::getId, bedId);
          }
          if (elderId != null) {
            if (bedId != null) {
              q.or();
            }
            q.eq(Bed::getElderId, elderId);
          }
        }));
    if (elderId == null && bedId == null) {
      beds = bedMapper.selectList(Wrappers.lambdaQuery(Bed.class)
          .eq(Bed::getIsDeleted, 0)
          .eq(tenantId != null, Bed::getTenantId, tenantId)
          .eq(orgId != null, Bed::getOrgId, orgId)
          .and(q -> q.isNotNull(Bed::getElderId).or().eq(Bed::getStatus, BedStatus.OCCUPIED)));
    }
    scope.bedsById = beds.stream()
        .collect(Collectors.toMap(Bed::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

    relations.forEach(relation -> {
      if (relation.getElderId() != null && !scope.eldersById.containsKey(relation.getElderId())) {
        ElderProfile elder = elderMapper.selectById(relation.getElderId());
        if (elder != null && !Integer.valueOf(1).equals(elder.getIsDeleted())) {
          scope.eldersById.put(elder.getId(), elder);
        }
      }
      if (relation.getBedId() != null && !scope.bedsById.containsKey(relation.getBedId())) {
        Bed bed = bedMapper.selectById(relation.getBedId());
        if (bed != null && !Integer.valueOf(1).equals(bed.getIsDeleted())) {
          scope.bedsById.put(bed.getId(), bed);
        }
      }
    });
    return scope;
  }

  private OccupancyHealthIssueResponse issue(
      String issueType,
      String severity,
      ElderProfile elder,
      Bed bed,
      ElderBedRelation relation,
      String detail,
      String suggestion) {
    OccupancyHealthIssueResponse item = new OccupancyHealthIssueResponse();
    item.setIssueType(issueType);
    item.setSeverity(severity);
    item.setElderId(elder == null ? null : elder.getId());
    item.setElderName(elder == null ? null : elder.getFullName());
    item.setBedId(bed == null ? null : bed.getId());
    item.setBedNo(bed == null ? null : bed.getBedNo());
    item.setRelationId(relation == null ? null : relation.getId());
    item.setDetail(detail);
    item.setSuggestion(suggestion);
    return item;
  }

  private static class ScopeData {
    private Map<Long, ElderProfile> eldersById = new LinkedHashMap<>();
    private Map<Long, Bed> bedsById = new LinkedHashMap<>();
    private Map<Long, ElderBedRelation> relationsById = new LinkedHashMap<>();
  }
}
