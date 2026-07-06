package com.zhiyangyun.care.medins.service;

import com.zhiyangyun.care.medins.entity.MedinsChannelConfig;
import com.zhiyangyun.care.medins.model.MedinsChannelSaveRequest;
import java.util.List;

/** 医保渠道配置服务。 */
public interface MedinsChannelService {

  /** 当前机构可见渠道（机构专属 + 全局默认）。 */
  List<MedinsChannelConfig> listChannels(Long orgId);

  /** 新增/编辑渠道配置，返回配置 ID。 */
  Long saveChannel(MedinsChannelSaveRequest request);

  /** 启用/停用渠道。 */
  void toggleChannel(Long id, boolean enabled);

  /** 解析生效配置：优先机构专属，回退全局默认；无可用配置返回 null。 */
  MedinsChannelConfig resolveChannel(String channel, Long orgId);
}
