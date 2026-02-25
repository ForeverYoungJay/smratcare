package com.zhiyangyun.care.health.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import org.springframework.stereotype.Component;

@Component
public class ElderResolveSupport {
  private final ElderMapper elderMapper;

  public ElderResolveSupport(ElderMapper elderMapper) {
    this.elderMapper = elderMapper;
  }

  public Long resolveElderId(Long orgId, Long elderId, String elderName) {
    if (elderId != null) {
      return elderId;
    }
    if (elderName == null || elderName.isBlank()) {
      throw new IllegalArgumentException("elderName required");
    }
    ElderProfile elder = elderMapper.selectOne(Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getIsDeleted, 0)
        .eq(orgId != null, ElderProfile::getOrgId, orgId)
        .eq(ElderProfile::getFullName, elderName)
        .last("LIMIT 1"));
    if (elder == null) {
      throw new IllegalArgumentException("elder not found");
    }
    return elder.getId();
  }

  public String resolveElderName(Long elderId, String fallback) {
    if (fallback != null && !fallback.isBlank()) {
      return fallback;
    }
    if (elderId == null) {
      return null;
    }
    ElderProfile elder = elderMapper.selectById(elderId);
    return elder == null ? null : elder.getFullName();
  }
}
