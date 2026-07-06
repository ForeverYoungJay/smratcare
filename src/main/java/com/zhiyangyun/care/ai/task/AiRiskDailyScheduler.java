package com.zhiyangyun.care.ai.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.ai.service.AiRiskService;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 每日凌晨全员重算健康风险评分，高风险自动生成告警（smart 告警中心）。 */
@Component
public class AiRiskDailyScheduler {
  private static final Logger log = LoggerFactory.getLogger(AiRiskDailyScheduler.class);

  private final ElderMapper elderMapper;
  private final AiRiskService riskService;

  public AiRiskDailyScheduler(ElderMapper elderMapper, AiRiskService riskService) {
    this.elderMapper = elderMapper;
    this.riskService = riskService;
  }

  @Scheduled(cron = "0 20 5 * * ?")
  public void recomputeAllOrgs() {
    List<Long> orgIds = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
            .select(ElderProfile::getOrgId)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(ElderProfile::getStatus, 1)
            .isNotNull(ElderProfile::getOrgId)).stream()
        .map(ElderProfile::getOrgId)
        .distinct()
        .toList();
    for (Long orgId : orgIds) {
      try {
        int count = riskService.recompute(orgId, null, null, null);
        log.info("AI risk daily recompute done, orgId={}, scoreCount={}", orgId, count);
      } catch (Exception ex) {
        log.warn("AI risk daily recompute failed, orgId={}", orgId, ex);
      }
    }
  }
}
