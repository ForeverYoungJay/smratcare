package com.zhiyangyun.care.elder.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.ElderFamily;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.mapper.ElderFamilyMapper;
import com.zhiyangyun.care.elder.mapper.FamilyUserMapper;
import com.zhiyangyun.care.elder.model.FamilyBindRequest;
import com.zhiyangyun.care.elder.model.FamilyRegisterRequest;
import com.zhiyangyun.care.elder.service.FamilyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyServiceImpl implements FamilyService {
  private final FamilyUserMapper familyUserMapper;
  private final ElderFamilyMapper elderFamilyMapper;

  public FamilyServiceImpl(FamilyUserMapper familyUserMapper, ElderFamilyMapper elderFamilyMapper) {
    this.familyUserMapper = familyUserMapper;
    this.elderFamilyMapper = elderFamilyMapper;
  }

  @Override
  public FamilyUser register(FamilyRegisterRequest request) {
    FamilyUser existing = familyUserMapper.selectOne(
        Wrappers.lambdaQuery(FamilyUser.class)
            .eq(FamilyUser::getOrgId, request.getOrgId())
            .eq(FamilyUser::getPhone, request.getPhone())
            .eq(FamilyUser::getIsDeleted, 0));
    if (existing != null) {
      return existing;
    }
    FamilyUser user = new FamilyUser();
    user.setOrgId(request.getOrgId());
    user.setPhone(request.getPhone());
    user.setRealName(request.getRealName() == null ? request.getPhone() : request.getRealName());
    user.setStatus(1);
    familyUserMapper.insert(user);
    return user;
  }

  @Override
  @Transactional
  public ElderFamily bindElder(FamilyBindRequest request) {
    if (request.getIsPrimary() != null && request.getIsPrimary() == 1) {
      elderFamilyMapper.update(null, Wrappers.lambdaUpdate(ElderFamily.class)
          .set(ElderFamily::getIsPrimary, 0)
          .eq(ElderFamily::getOrgId, request.getOrgId())
          .eq(ElderFamily::getElderId, request.getElderId())
          .eq(ElderFamily::getIsDeleted, 0));
    }
    ElderFamily existing = elderFamilyMapper.selectOne(
        Wrappers.lambdaQuery(ElderFamily.class)
            .eq(ElderFamily::getOrgId, request.getOrgId())
            .eq(ElderFamily::getElderId, request.getElderId())
            .eq(ElderFamily::getFamilyUserId, request.getFamilyUserId())
            .eq(ElderFamily::getIsDeleted, 0));
    if (existing != null) {
      if (request.getIsPrimary() != null) {
        existing.setIsPrimary(request.getIsPrimary());
      }
      if (request.getRelation() != null) {
        existing.setRelation(request.getRelation());
      }
      if (request.getRemark() != null) {
        existing.setRemark(request.getRemark());
      }
      elderFamilyMapper.updateById(existing);
      return existing;
    }
    ElderFamily relation = new ElderFamily();
    relation.setOrgId(request.getOrgId());
    relation.setElderId(request.getElderId());
    relation.setFamilyUserId(request.getFamilyUserId());
    relation.setRelation(request.getRelation());
    relation.setIsPrimary(request.getIsPrimary());
    relation.setRemark(request.getRemark());
    elderFamilyMapper.insert(relation);
    return relation;
  }
}
