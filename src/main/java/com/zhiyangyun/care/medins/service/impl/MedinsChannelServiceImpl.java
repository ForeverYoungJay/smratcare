package com.zhiyangyun.care.medins.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.medins.entity.MedinsChannelConfig;
import com.zhiyangyun.care.medins.mapper.MedinsChannelConfigMapper;
import com.zhiyangyun.care.medins.model.MedinsChannelSaveRequest;
import com.zhiyangyun.care.medins.service.MedinsChannelService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedinsChannelServiceImpl implements MedinsChannelService {

  private final MedinsChannelConfigMapper channelMapper;

  public MedinsChannelServiceImpl(MedinsChannelConfigMapper channelMapper) {
    this.channelMapper = channelMapper;
  }

  @Override
  public List<MedinsChannelConfig> listChannels(Long orgId) {
    return channelMapper.selectList(Wrappers.lambdaQuery(MedinsChannelConfig.class)
        .eq(MedinsChannelConfig::getIsDeleted, 0)
        .and(w -> w.isNull(MedinsChannelConfig::getOrgId)
            .or(orgId != null, q -> q.eq(MedinsChannelConfig::getOrgId, orgId)))
        .orderByAsc(MedinsChannelConfig::getChannel));
  }

  @Override
  @Transactional
  public Long saveChannel(MedinsChannelSaveRequest request) {
    if (request.getSecretRef() != null && request.getSecretRef().length() > 120) {
      throw new IllegalArgumentException("密钥引用过长，仅支持密钥库键名，禁止填明文密钥");
    }
    Long orgId = AuthContext.getOrgId();
    if (request.getId() != null) {
      MedinsChannelConfig config = loadConfig(request.getId());
      config.setChannel(request.getChannel());
      config.setRegionCode(request.getRegionCode());
      config.setEndpoint(request.getEndpoint());
      config.setAppId(request.getAppId());
      config.setSecretRef(request.getSecretRef());
      if (request.getEnabled() != null) {
        config.setEnabled(request.getEnabled());
      }
      config.setRemark(request.getRemark());
      channelMapper.updateById(config);
      return config.getId();
    }
    MedinsChannelConfig config = new MedinsChannelConfig();
    config.setOrgId(orgId);
    config.setChannel(request.getChannel());
    config.setRegionCode(request.getRegionCode());
    config.setEndpoint(request.getEndpoint());
    config.setAppId(request.getAppId());
    config.setSecretRef(request.getSecretRef());
    config.setEnabled(request.getEnabled() == null ? 1 : request.getEnabled());
    config.setRemark(request.getRemark());
    config.setCreatedBy(AuthContext.getStaffId());
    channelMapper.insert(config);
    return config.getId();
  }

  @Override
  @Transactional
  public void toggleChannel(Long id, boolean enabled) {
    MedinsChannelConfig config = loadConfig(id);
    config.setEnabled(enabled ? 1 : 0);
    channelMapper.updateById(config);
  }

  @Override
  public MedinsChannelConfig resolveChannel(String channel, Long orgId) {
    if (orgId != null) {
      MedinsChannelConfig orgCfg = channelMapper.selectOne(
          Wrappers.lambdaQuery(MedinsChannelConfig.class)
              .eq(MedinsChannelConfig::getIsDeleted, 0)
              .eq(MedinsChannelConfig::getOrgId, orgId)
              .eq(MedinsChannelConfig::getChannel, channel)
              .eq(MedinsChannelConfig::getEnabled, 1)
              .last("limit 1"));
      if (orgCfg != null) {
        return orgCfg;
      }
    }
    return channelMapper.selectOne(Wrappers.lambdaQuery(MedinsChannelConfig.class)
        .eq(MedinsChannelConfig::getIsDeleted, 0)
        .isNull(MedinsChannelConfig::getOrgId)
        .eq(MedinsChannelConfig::getChannel, channel)
        .eq(MedinsChannelConfig::getEnabled, 1)
        .last("limit 1"));
  }

  private MedinsChannelConfig loadConfig(Long id) {
    MedinsChannelConfig config = channelMapper.selectById(id);
    if (config == null || Integer.valueOf(1).equals(config.getIsDeleted())) {
      throw new IllegalArgumentException("医保渠道配置不存在: " + id);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && config.getOrgId() != null && !orgId.equals(config.getOrgId())) {
      throw new IllegalArgumentException("无权操作其他机构的渠道配置");
    }
    return config;
  }
}
