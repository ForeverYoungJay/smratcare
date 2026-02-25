package com.zhiyangyun.care.nursing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.nursing.entity.CaregiverGroup;
import com.zhiyangyun.care.nursing.entity.CaregiverGroupMember;
import com.zhiyangyun.care.nursing.mapper.CaregiverGroupMapper;
import com.zhiyangyun.care.nursing.mapper.CaregiverGroupMemberMapper;
import com.zhiyangyun.care.nursing.model.CaregiverGroupRequest;
import com.zhiyangyun.care.nursing.model.CaregiverGroupResponse;
import com.zhiyangyun.care.nursing.service.CaregiverGroupService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CaregiverGroupServiceImpl implements CaregiverGroupService {
  private final CaregiverGroupMapper groupMapper;
  private final CaregiverGroupMemberMapper memberMapper;
  private final StaffMapper staffMapper;

  public CaregiverGroupServiceImpl(CaregiverGroupMapper groupMapper, CaregiverGroupMemberMapper memberMapper,
      StaffMapper staffMapper) {
    this.groupMapper = groupMapper;
    this.memberMapper = memberMapper;
    this.staffMapper = staffMapper;
  }

  @Override
  @Transactional
  public CaregiverGroupResponse create(CaregiverGroupRequest request) {
    CaregiverGroup group = new CaregiverGroup();
    group.setTenantId(request.getTenantId());
    group.setOrgId(request.getOrgId());
    group.setName(request.getName());
    group.setLeaderStaffId(request.getLeaderStaffId());
    group.setEnabled(request.getEnabled() == null ? 1 : request.getEnabled());
    group.setRemark(request.getRemark());
    group.setCreatedBy(request.getCreatedBy());
    groupMapper.insert(group);
    replaceMembers(group.getId(), request);
    return assembleResponse(group, request.getMemberStaffIds());
  }

  @Override
  @Transactional
  public CaregiverGroupResponse update(Long id, CaregiverGroupRequest request) {
    CaregiverGroup group = groupMapper.selectById(id);
    if (group == null || !request.getTenantId().equals(group.getTenantId())) {
      return null;
    }
    group.setName(request.getName());
    group.setLeaderStaffId(request.getLeaderStaffId());
    group.setEnabled(request.getEnabled() == null ? 1 : request.getEnabled());
    group.setRemark(request.getRemark());
    groupMapper.updateById(group);
    replaceMembers(group.getId(), request);
    return assembleResponse(group, request.getMemberStaffIds());
  }

  @Override
  public CaregiverGroupResponse get(Long id, Long tenantId) {
    CaregiverGroup group = groupMapper.selectById(id);
    if (group == null || (tenantId != null && !tenantId.equals(group.getTenantId()))) {
      return null;
    }
    List<Long> memberStaffIds = memberMapper.selectByGroupIds(List.of(id)).stream()
        .map(CaregiverGroupMember::getStaffId)
        .distinct()
        .toList();
    return assembleResponse(group, memberStaffIds);
  }

  @Override
  public IPage<CaregiverGroupResponse> page(Long tenantId, long pageNo, long pageSize, String keyword,
      Integer enabled) {
    var wrapper = Wrappers.lambdaQuery(CaregiverGroup.class)
        .eq(CaregiverGroup::getIsDeleted, 0)
        .eq(tenantId != null, CaregiverGroup::getTenantId, tenantId)
        .eq(enabled != null, CaregiverGroup::getEnabled, enabled)
        .orderByDesc(CaregiverGroup::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.like(CaregiverGroup::getName, keyword);
    }
    IPage<CaregiverGroup> page = groupMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return toPageResponse(page);
  }

  @Override
  public List<CaregiverGroupResponse> list(Long tenantId, Integer enabled) {
    var wrapper = Wrappers.lambdaQuery(CaregiverGroup.class)
        .eq(CaregiverGroup::getIsDeleted, 0)
        .eq(tenantId != null, CaregiverGroup::getTenantId, tenantId)
        .eq(enabled != null, CaregiverGroup::getEnabled, enabled)
        .orderByAsc(CaregiverGroup::getName);
    List<CaregiverGroup> groups = groupMapper.selectList(wrapper);
    if (groups.isEmpty()) {
      return List.of();
    }
    List<Long> groupIds = groups.stream().map(CaregiverGroup::getId).toList();
    Map<Long, List<Long>> memberMap = memberMapper.selectByGroupIds(groupIds).stream()
        .collect(Collectors.groupingBy(CaregiverGroupMember::getGroupId,
            Collectors.mapping(CaregiverGroupMember::getStaffId, Collectors.toList())));
    return groups.stream()
        .map(group -> assembleResponse(group, memberMap.getOrDefault(group.getId(), List.of())))
        .toList();
  }

  @Override
  public void delete(Long id, Long tenantId) {
    CaregiverGroup group = groupMapper.selectById(id);
    if (group != null && tenantId != null && tenantId.equals(group.getTenantId())) {
      group.setIsDeleted(1);
      groupMapper.updateById(group);
      memberMapper.softDeleteByGroupId(id);
    }
  }

  private void replaceMembers(Long groupId, CaregiverGroupRequest request) {
    memberMapper.softDeleteByGroupId(groupId);
    if (request.getMemberStaffIds() == null || request.getMemberStaffIds().isEmpty()) {
      return;
    }
    Set<Long> uniqueStaffIds = request.getMemberStaffIds().stream()
        .filter(id -> id != null && id > 0)
        .collect(Collectors.toSet());
    for (Long staffId : uniqueStaffIds) {
      CaregiverGroupMember member = new CaregiverGroupMember();
      member.setTenantId(request.getTenantId());
      member.setOrgId(request.getOrgId());
      member.setGroupId(groupId);
      member.setStaffId(staffId);
      member.setCreatedBy(request.getCreatedBy());
      memberMapper.insert(member);
    }
  }

  private IPage<CaregiverGroupResponse> toPageResponse(IPage<CaregiverGroup> page) {
    List<Long> groupIds = page.getRecords().stream().map(CaregiverGroup::getId).toList();
    Map<Long, List<Long>> memberMap = groupIds.isEmpty()
        ? Collections.emptyMap()
        : memberMapper.selectByGroupIds(groupIds).stream()
            .collect(Collectors.groupingBy(CaregiverGroupMember::getGroupId,
                Collectors.mapping(CaregiverGroupMember::getStaffId, Collectors.toList())));

    Set<Long> staffIds = page.getRecords().stream().map(CaregiverGroup::getLeaderStaffId)
        .filter(id -> id != null && id > 0)
        .collect(Collectors.toSet());
    memberMap.values().forEach(staffIds::addAll);

    Map<Long, StaffAccount> staffMap = staffIds.isEmpty() ? Collections.emptyMap()
        : staffMapper.selectBatchIds(staffIds).stream()
            .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));

    IPage<CaregiverGroupResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(group -> {
      List<Long> memberStaffIds = memberMap.getOrDefault(group.getId(), List.of());
      CaregiverGroupResponse response = assembleResponse(group, memberStaffIds);
      StaffAccount leader = staffMap.get(group.getLeaderStaffId());
      response.setLeaderStaffName(leader == null ? null : leader.getRealName());
      return response;
    }).toList());
    return resp;
  }

  private CaregiverGroupResponse assembleResponse(CaregiverGroup group, List<Long> memberStaffIds) {
    CaregiverGroupResponse response = new CaregiverGroupResponse();
    response.setId(group.getId());
    response.setTenantId(group.getTenantId());
    response.setOrgId(group.getOrgId());
    response.setName(group.getName());
    response.setLeaderStaffId(group.getLeaderStaffId());
    response.setEnabled(group.getEnabled());
    response.setRemark(group.getRemark());
    response.setMemberStaffIds(memberStaffIds == null ? List.of() : memberStaffIds);
    if (group.getLeaderStaffId() != null) {
      StaffAccount leader = staffMapper.selectById(group.getLeaderStaffId());
      response.setLeaderStaffName(leader == null ? null : leader.getRealName());
    }
    return response;
  }
}
