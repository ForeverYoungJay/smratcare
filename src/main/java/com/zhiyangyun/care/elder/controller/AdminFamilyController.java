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
import com.zhiyangyun.care.elder.model.FamilyBindRequest;
import com.zhiyangyun.care.elder.model.FamilyRelationItem;
import com.zhiyangyun.care.elder.model.FamilyUserPageItem;
import com.zhiyangyun.care.elder.model.AdminFamilyUserUpsertRequest;
import com.zhiyangyun.care.elder.service.FamilyService;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/family")
public class AdminFamilyController {
  private final FamilyUserMapper familyUserMapper;
  private final ElderFamilyMapper elderFamilyMapper;
  private final ElderMapper elderMapper;
  private final FamilyService familyService;

  public AdminFamilyController(FamilyUserMapper familyUserMapper,
      ElderFamilyMapper elderFamilyMapper,
      ElderMapper elderMapper,
      FamilyService familyService) {
    this.familyUserMapper = familyUserMapper;
    this.elderFamilyMapper = elderFamilyMapper;
    this.elderMapper = elderMapper;
    this.familyService = familyService;
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
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
      item.setIdCardNo(user == null ? null : user.getIdCardNo());
      item.setRelation(relation.getRelation());
      item.setIsPrimary(relation.getIsPrimary());
      items.add(item);
    }
    return Result.ok(items);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/users/upsert")
  public Result<FamilyUserPageItem> upsertUser(@Valid @RequestBody AdminFamilyUserUpsertRequest request) {
    Long orgId = AuthContext.getOrgId();
    String phone = request.getPhone() == null ? "" : request.getPhone().trim();
    String idCardNo = request.getIdCardNo() == null ? "" : request.getIdCardNo().trim();
    String realName = request.getRealName() == null ? "" : request.getRealName().trim();
    if (phone.isEmpty()) {
      throw new IllegalArgumentException("家属手机号不能为空");
    }
    if (idCardNo.isEmpty()) {
      throw new IllegalArgumentException("家属身份证号不能为空");
    }
    if (realName.isEmpty()) {
      throw new IllegalArgumentException("家属姓名不能为空");
    }

    FamilyUser user = familyUserMapper.selectOne(
        Wrappers.lambdaQuery(FamilyUser.class)
            .eq(FamilyUser::getIsDeleted, 0)
            .eq(FamilyUser::getOrgId, orgId)
            .eq(FamilyUser::getPhone, phone)
            .last("LIMIT 1"));
    if (user == null) {
      user = new FamilyUser();
      user.setOrgId(orgId);
      user.setPhone(phone);
      user.setRealName(realName);
      user.setIdCardNo(idCardNo);
      user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
      familyUserMapper.insert(user);
    } else {
      user.setRealName(realName);
      user.setIdCardNo(idCardNo);
      if (request.getStatus() != null) {
        user.setStatus(request.getStatus());
      } else if (user.getStatus() == null) {
        user.setStatus(1);
      }
      familyUserMapper.updateById(user);
    }

    FamilyUserPageItem item = new FamilyUserPageItem();
    item.setId(user.getId());
    item.setRealName(user.getRealName());
    item.setPhone(user.getPhone());
    item.setIdCardNo(user.getIdCardNo());
    item.setStatus(user.getStatus());
    item.setElderCount(0L);
    item.setCreateTime(user.getCreateTime());
    return Result.ok(item);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @PostMapping("/relations/bind")
  public Result<FamilyRelationItem> bindRelation(@Valid @RequestBody FamilyBindRequest request) {
    if (request.getFamilyUserId() == null || request.getFamilyUserId() <= 0) {
      throw new IllegalArgumentException("家属ID不能为空");
    }
    Long orgId = AuthContext.getOrgId();
    FamilyUser familyUser = familyUserMapper.selectOne(
        Wrappers.lambdaQuery(FamilyUser.class)
            .eq(FamilyUser::getIsDeleted, 0)
            .eq(FamilyUser::getOrgId, orgId)
            .eq(FamilyUser::getId, request.getFamilyUserId())
            .last("LIMIT 1"));
    if (familyUser == null) {
      throw new IllegalArgumentException("家属信息不存在或已失效");
    }
    request.setOrgId(orgId);
    ElderFamily relation = familyService.bindElder(request);
    FamilyRelationItem item = new FamilyRelationItem();
    item.setId(relation.getId());
    item.setFamilyUserId(relation.getFamilyUserId());
    item.setRealName(familyUser.getRealName());
    item.setPhone(familyUser.getPhone());
    item.setIdCardNo(familyUser.getIdCardNo());
    item.setRelation(relation.getRelation());
    item.setIsPrimary(relation.getIsPrimary());
    return Result.ok(item);
  }

  @PreAuthorize("hasAnyRole('HR_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @DeleteMapping("/relations/{relationId}")
  public Result<Void> removeRelation(@PathVariable Long relationId) {
    Long orgId = AuthContext.getOrgId();
    ElderFamily relation = elderFamilyMapper.selectById(relationId);
    if (relation != null
        && relation.getIsDeleted() != null
        && relation.getIsDeleted() == 0
        && (orgId == null || orgId.equals(relation.getOrgId()))) {
      relation.setIsDeleted(1);
      elderFamilyMapper.updateById(relation);
    }
    return Result.ok(null);
  }
}
