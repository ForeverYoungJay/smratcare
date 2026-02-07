package com.zhiyangyun.care.store.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.store.entity.DiseaseForbiddenTag;
import com.zhiyangyun.care.store.entity.DiseaseForbiddenTagAudit;
import com.zhiyangyun.care.store.entity.ElderDisease;
import com.zhiyangyun.care.store.entity.ProductTag;
import com.zhiyangyun.care.store.mapper.DiseaseForbiddenTagAuditMapper;
import com.zhiyangyun.care.store.mapper.DiseaseForbiddenTagMapper;
import com.zhiyangyun.care.store.mapper.ElderDiseaseMapper;
import com.zhiyangyun.care.store.mapper.ProductTagMapper;
import com.zhiyangyun.care.store.model.admin.ForbiddenTagsResponse;
import com.zhiyangyun.care.store.service.DiseaseRuleService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiseaseRuleServiceImpl implements DiseaseRuleService {
  private final DiseaseForbiddenTagMapper forbiddenTagMapper;
  private final ProductTagMapper productTagMapper;
  private final ElderDiseaseMapper elderDiseaseMapper;
  private final DiseaseForbiddenTagAuditMapper auditMapper;

  public DiseaseRuleServiceImpl(DiseaseForbiddenTagMapper forbiddenTagMapper,
      ProductTagMapper productTagMapper,
      ElderDiseaseMapper elderDiseaseMapper,
      DiseaseForbiddenTagAuditMapper auditMapper) {
    this.forbiddenTagMapper = forbiddenTagMapper;
    this.productTagMapper = productTagMapper;
    this.elderDiseaseMapper = elderDiseaseMapper;
    this.auditMapper = auditMapper;
  }

  @Override
  @Transactional
  public void saveForbiddenTags(Long orgId, Long diseaseId, List<Long> tagIds) {
    List<Long> before = forbiddenTagMapper.selectList(
            Wrappers.lambdaQuery(DiseaseForbiddenTag.class)
                .eq(DiseaseForbiddenTag::getOrgId, orgId)
                .eq(DiseaseForbiddenTag::getDiseaseId, diseaseId)
                .eq(DiseaseForbiddenTag::getIsDeleted, 0))
        .stream()
        .map(DiseaseForbiddenTag::getTagId)
        .sorted()
        .collect(Collectors.toList());

    List<Long> after = tagIds.stream().sorted().collect(Collectors.toList());
    if (before.equals(after)) {
      return;
    }

    forbiddenTagMapper.delete(Wrappers.lambdaQuery(DiseaseForbiddenTag.class)
        .eq(DiseaseForbiddenTag::getOrgId, orgId)
        .eq(DiseaseForbiddenTag::getDiseaseId, diseaseId));

    for (Long tagId : tagIds) {
      DiseaseForbiddenTag record = new DiseaseForbiddenTag();
      record.setOrgId(orgId);
      record.setDiseaseId(diseaseId);
      record.setTagId(tagId);
      record.setForbiddenLevel(1);
      forbiddenTagMapper.insert(record);
    }

    DiseaseForbiddenTagAudit audit = new DiseaseForbiddenTagAudit();
    audit.setOrgId(orgId);
    audit.setDiseaseId(diseaseId);
    audit.setActionType("UPDATE");
    audit.setBeforeTagIds(before.isEmpty() ? null : before.toString());
    audit.setAfterTagIds(after.isEmpty() ? null : after.toString());
    audit.setOperatorStaffId(AuthContext.getStaffId());
    auditMapper.insert(audit);
  }

  @Override
  public ForbiddenTagsResponse getForbiddenTags(Long orgId, Long diseaseId) {
    List<DiseaseForbiddenTag> selected = forbiddenTagMapper.selectList(
        Wrappers.lambdaQuery(DiseaseForbiddenTag.class)
            .eq(DiseaseForbiddenTag::getOrgId, orgId)
            .eq(DiseaseForbiddenTag::getDiseaseId, diseaseId)
            .eq(DiseaseForbiddenTag::getIsDeleted, 0));

    List<Long> selectedIds = selected.stream()
        .map(DiseaseForbiddenTag::getTagId)
        .collect(Collectors.toList());

    List<ProductTag> tags = productTagMapper.selectList(
        Wrappers.lambdaQuery(ProductTag.class)
            .eq(ProductTag::getOrgId, orgId)
            .eq(ProductTag::getIsDeleted, 0)
            .orderByAsc(ProductTag::getTagName));

    Map<String, List<ProductTag>> grouped = tags.stream()
        .collect(Collectors.groupingBy(t -> t.getTagType() == null ? "DEFAULT" : t.getTagType()));
    List<ForbiddenTagsResponse.TagGroup> groups = grouped.entrySet().stream()
        .sorted(Comparator.comparing(Map.Entry::getKey))
        .map(entry -> {
          ForbiddenTagsResponse.TagGroup group = new ForbiddenTagsResponse.TagGroup();
          group.setTagType(entry.getKey());
          List<ForbiddenTagsResponse.TagItem> items = new ArrayList<>();
          for (ProductTag tag : entry.getValue()) {
            ForbiddenTagsResponse.TagItem item = new ForbiddenTagsResponse.TagItem();
            item.setId(tag.getId());
            item.setTagCode(tag.getTagCode());
            item.setTagName(tag.getTagName());
            item.setTagType(tag.getTagType());
            items.add(item);
          }
          group.setTags(items);
          return group;
        })
        .collect(Collectors.toList());

    ForbiddenTagsResponse response = new ForbiddenTagsResponse();
    response.setDiseaseId(diseaseId);
    response.setSelectedTagIds(selectedIds);
    response.setGroups(groups);
    return response;
  }

  @Override
  @Transactional
  public void saveElderDiseases(Long orgId, Long elderId, List<Long> diseaseIds) {
    elderDiseaseMapper.delete(Wrappers.lambdaQuery(ElderDisease.class)
        .eq(ElderDisease::getOrgId, orgId)
        .eq(ElderDisease::getElderId, elderId));

    for (Long diseaseId : diseaseIds) {
      ElderDisease record = new ElderDisease();
      record.setOrgId(orgId);
      record.setElderId(elderId);
      record.setDiseaseId(diseaseId);
      elderDiseaseMapper.insert(record);
    }
  }

  @Override
  public List<Long> getElderDiseaseIds(Long orgId, Long elderId) {
    return elderDiseaseMapper.selectList(
        Wrappers.lambdaQuery(ElderDisease.class)
            .eq(ElderDisease::getOrgId, orgId)
            .eq(ElderDisease::getElderId, elderId)
            .eq(ElderDisease::getIsDeleted, 0))
        .stream()
        .map(ElderDisease::getDiseaseId)
        .collect(Collectors.toList());
  }
}
