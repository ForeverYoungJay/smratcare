package com.zhiyangyun.care.elder.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.ElderFamily;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.model.FamilyBindRequest;
import com.zhiyangyun.care.elder.model.FamilyElderItem;
import com.zhiyangyun.care.elder.model.FamilyRegisterRequest;
import com.zhiyangyun.care.elder.mapper.ElderFamilyMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.service.FamilyService;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/family")
public class FamilyController {
  private final FamilyService familyService;
  private final ElderFamilyMapper elderFamilyMapper;
  private final ElderMapper elderMapper;

  public FamilyController(FamilyService familyService,
      ElderFamilyMapper elderFamilyMapper,
      ElderMapper elderMapper) {
    this.familyService = familyService;
    this.elderFamilyMapper = elderFamilyMapper;
    this.elderMapper = elderMapper;
  }

  @PostMapping("/register")
  public Result<FamilyUser> register(@Valid @RequestBody FamilyRegisterRequest request) {
    return Result.ok(familyService.register(request));
  }

  @PostMapping("/bindElder")
  public Result<ElderFamily> bindElder(@Valid @RequestBody FamilyBindRequest request) {
    request.setOrgId(AuthContext.getOrgId());
    request.setFamilyUserId(AuthContext.getStaffId());
    return Result.ok(familyService.bindElder(request));
  }

  @GetMapping("/my-elders")
  public Result<List<FamilyElderItem>> myElders() {
    Long orgId = AuthContext.getOrgId();
    Long familyUserId = AuthContext.getStaffId();
    List<ElderFamily> relations = elderFamilyMapper.selectList(
        Wrappers.lambdaQuery(ElderFamily.class)
            .eq(ElderFamily::getOrgId, orgId)
            .eq(ElderFamily::getFamilyUserId, familyUserId)
            .eq(ElderFamily::getIsDeleted, 0));
    List<FamilyElderItem> items = new ArrayList<>();
    for (ElderFamily relation : relations) {
      ElderProfile elder = elderMapper.selectById(relation.getElderId());
      FamilyElderItem item = new FamilyElderItem();
      item.setElderId(relation.getElderId());
      item.setElderName(elder == null ? null : elder.getFullName());
      item.setRelation(relation.getRelation());
      item.setIsPrimary(relation.getIsPrimary());
      items.add(item);
    }
    return Result.ok(items);
  }
}
