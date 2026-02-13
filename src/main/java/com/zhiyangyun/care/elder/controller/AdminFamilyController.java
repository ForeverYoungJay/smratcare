package com.zhiyangyun.care.elder.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.ElderFamily;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.mapper.ElderFamilyMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.FamilyUserMapper;
import com.zhiyangyun.care.elder.model.FamilyRelationItem;
import com.zhiyangyun.care.elder.model.FamilyUserPageItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/family")
public class AdminFamilyController {
  private final FamilyUserMapper familyUserMapper;
  private final ElderFamilyMapper elderFamilyMapper;
  private final ElderMapper elderMapper;

  public AdminFamilyController(FamilyUserMapper familyUserMapper,
      ElderFamilyMapper elderFamilyMapper,
      ElderMapper elderMapper) {
    this.familyUserMapper = familyUserMapper;
    this.elderFamilyMapper = elderFamilyMapper;
    this.elderMapper = elderMapper;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/users/page")
  public Result<IPage<FamilyUserPageItem>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String realName,
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) String elderName,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortOrder) {
    Long orgId = AuthContext.getOrgId();

    List<Long> elderFilterFamilyIds = null;
    if (elderName != null && !elderName.isBlank()) {
      List<Long> elderIds = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
          .eq(orgId != null, ElderProfile::getOrgId, orgId)
          .eq(ElderProfile::getIsDeleted, 0)
          .like(ElderProfile::getFullName, elderName))
          .stream()
          .map(ElderProfile::getId)
          .toList();
      if (elderIds.isEmpty()) {
        return Result.ok(new Page<>(pageNo, pageSize));
      }
      elderFilterFamilyIds = elderFamilyMapper.selectList(Wrappers.lambdaQuery(ElderFamily.class)
              .eq(orgId != null, ElderFamily::getOrgId, orgId)
              .eq(ElderFamily::getIsDeleted, 0)
              .in(ElderFamily::getElderId, elderIds))
          .stream()
          .map(ElderFamily::getFamilyUserId)
          .distinct()
          .toList();
      if (elderFilterFamilyIds.isEmpty()) {
        return Result.ok(new Page<>(pageNo, pageSize));
      }
    }

    var wrapper = Wrappers.lambdaQuery(FamilyUser.class)
        .eq(orgId != null, FamilyUser::getOrgId, orgId)
        .eq(FamilyUser::getIsDeleted, 0)
        .like(realName != null && !realName.isBlank(), FamilyUser::getRealName, realName)
        .like(phone != null && !phone.isBlank(), FamilyUser::getPhone, phone)
        .eq(status != null, FamilyUser::getStatus, status)
        .in(elderFilterFamilyIds != null, FamilyUser::getId, elderFilterFamilyIds);

    boolean asc = "asc".equalsIgnoreCase(sortOrder);
    if ("realName".equals(sortBy)) {
      wrapper.orderBy(true, asc, FamilyUser::getRealName);
    } else if ("phone".equals(sortBy)) {
      wrapper.orderBy(true, asc, FamilyUser::getPhone);
    } else if ("createTime".equals(sortBy)) {
      wrapper.orderBy(true, asc, FamilyUser::getCreateTime);
    }

    IPage<FamilyUser> page = familyUserMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> familyIds = page.getRecords().stream().map(FamilyUser::getId).toList();
    Map<Long, Long> countMap = new HashMap<>();
    if (!familyIds.isEmpty()) {
      elderFamilyMapper.selectList(Wrappers.lambdaQuery(ElderFamily.class)
              .eq(orgId != null, ElderFamily::getOrgId, orgId)
              .eq(ElderFamily::getIsDeleted, 0)
              .in(ElderFamily::getFamilyUserId, familyIds))
          .forEach(rel -> countMap.merge(rel.getFamilyUserId(), 1L, Long::sum));
    }

    IPage<FamilyUserPageItem> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    List<FamilyUserPageItem> items = new ArrayList<>();
    for (FamilyUser user : page.getRecords()) {
      FamilyUserPageItem item = new FamilyUserPageItem();
      item.setId(user.getId());
      item.setRealName(user.getRealName());
      item.setPhone(user.getPhone());
      item.setIdCardNo(user.getIdCardNo());
      item.setStatus(user.getStatus());
      item.setElderCount(countMap.getOrDefault(user.getId(), 0L));
      item.setCreateTime(user.getCreateTime());
      items.add(item);
    }
    resp.setRecords(items);
    return Result.ok(resp);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/relations")
  public Result<List<FamilyRelationItem>> relations(@RequestParam Long elderId) {
    Long orgId = AuthContext.getOrgId();
    List<ElderFamily> relations = elderFamilyMapper.selectList(Wrappers.lambdaQuery(ElderFamily.class)
        .eq(orgId != null, ElderFamily::getOrgId, orgId)
        .eq(ElderFamily::getElderId, elderId)
        .eq(ElderFamily::getIsDeleted, 0));
    if (relations.isEmpty()) {
      return Result.ok(List.of());
    }
    List<Long> familyIds = relations.stream().map(ElderFamily::getFamilyUserId).distinct().toList();
    Map<Long, FamilyUser> userMap = familyUserMapper.selectList(Wrappers.lambdaQuery(FamilyUser.class)
            .eq(orgId != null, FamilyUser::getOrgId, orgId)
            .eq(FamilyUser::getIsDeleted, 0)
            .in(FamilyUser::getId, familyIds))
        .stream()
        .collect(Collectors.toMap(FamilyUser::getId, u -> u, (a, b) -> a));

    List<FamilyRelationItem> items = new ArrayList<>();
    for (ElderFamily relation : relations) {
      FamilyUser user = userMap.get(relation.getFamilyUserId());
      FamilyRelationItem item = new FamilyRelationItem();
      item.setId(relation.getId());
      item.setFamilyUserId(relation.getFamilyUserId());
      item.setRealName(user == null ? null : user.getRealName());
      item.setPhone(user == null ? null : user.getPhone());
      item.setRelation(relation.getRelation());
      item.setIsPrimary(relation.getIsPrimary());
      items.add(item);
    }
    return Result.ok(items);
  }
}
