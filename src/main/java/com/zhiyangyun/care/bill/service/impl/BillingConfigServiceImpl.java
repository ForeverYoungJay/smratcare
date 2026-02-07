package com.zhiyangyun.care.bill.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.bill.entity.BillingCareLevelFee;
import com.zhiyangyun.care.bill.entity.BillingConfigEntry;
import com.zhiyangyun.care.bill.mapper.BillingCareLevelFeeMapper;
import com.zhiyangyun.care.bill.mapper.BillingConfigMapper;
import com.zhiyangyun.care.bill.service.BillingConfigService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingConfigServiceImpl implements BillingConfigService {
  private final BillingConfigMapper configMapper;
  private final BillingCareLevelFeeMapper careLevelFeeMapper;

  public BillingConfigServiceImpl(
      BillingConfigMapper configMapper,
      BillingCareLevelFeeMapper careLevelFeeMapper) {
    this.configMapper = configMapper;
    this.careLevelFeeMapper = careLevelFeeMapper;
  }

  @Override
  @Transactional
  public BillingConfigEntry upsertConfig(BillingConfigEntry entry) {
    BillingConfigEntry existing = configMapper.selectOne(
        Wrappers.lambdaQuery(BillingConfigEntry.class)
            .eq(BillingConfigEntry::getOrgId, entry.getOrgId())
            .eq(BillingConfigEntry::getConfigKey, entry.getConfigKey())
            .eq(BillingConfigEntry::getEffectiveMonth, entry.getEffectiveMonth()));
    if (existing == null) {
      configMapper.insert(entry);
      return entry;
    }
    entry.setId(existing.getId());
    configMapper.updateById(entry);
    return entry;
  }

  @Override
  @Transactional
  public BillingCareLevelFee upsertCareLevelFee(BillingCareLevelFee fee) {
    BillingCareLevelFee existing = careLevelFeeMapper.selectOne(
        Wrappers.lambdaQuery(BillingCareLevelFee.class)
            .eq(BillingCareLevelFee::getOrgId, fee.getOrgId())
            .eq(BillingCareLevelFee::getCareLevel, fee.getCareLevel())
            .eq(BillingCareLevelFee::getEffectiveMonth, fee.getEffectiveMonth()));
    if (existing == null) {
      careLevelFeeMapper.insert(fee);
      return fee;
    }
    fee.setId(existing.getId());
    careLevelFeeMapper.updateById(fee);
    return fee;
  }

  @Override
  public List<BillingConfigEntry> listConfigs(Long orgId, String effectiveMonth) {
    return configMapper.selectList(
        Wrappers.lambdaQuery(BillingConfigEntry.class)
            .eq(BillingConfigEntry::getOrgId, orgId)
            .eq(effectiveMonth != null, BillingConfigEntry::getEffectiveMonth, effectiveMonth)
            .orderByDesc(BillingConfigEntry::getEffectiveMonth));
  }

  @Override
  public List<BillingCareLevelFee> listCareLevelFees(Long orgId, String effectiveMonth) {
    return careLevelFeeMapper.selectList(
        Wrappers.lambdaQuery(BillingCareLevelFee.class)
            .eq(BillingCareLevelFee::getOrgId, orgId)
            .eq(effectiveMonth != null, BillingCareLevelFee::getEffectiveMonth, effectiveMonth)
            .orderByDesc(BillingCareLevelFee::getEffectiveMonth));
  }

  @Override
  public BigDecimal getConfigValue(Long orgId, String configKey, String billMonth) {
    List<BillingConfigEntry> entries = configMapper.selectList(
        Wrappers.lambdaQuery(BillingConfigEntry.class)
            .eq(BillingConfigEntry::getOrgId, orgId)
            .eq(BillingConfigEntry::getConfigKey, configKey)
            .eq(BillingConfigEntry::getStatus, 1)
            .orderByDesc(BillingConfigEntry::getEffectiveMonth));
    return entries.stream()
        .filter(e -> e.getEffectiveMonth().compareTo(billMonth) <= 0)
        .max(Comparator.comparing(BillingConfigEntry::getEffectiveMonth))
        .map(BillingConfigEntry::getConfigValue)
        .orElse(BigDecimal.ZERO);
  }

  @Override
  public Map<String, BigDecimal> getCareLevelFees(Long orgId, String billMonth) {
    List<BillingCareLevelFee> fees = careLevelFeeMapper.selectList(
        Wrappers.lambdaQuery(BillingCareLevelFee.class)
            .eq(BillingCareLevelFee::getOrgId, orgId)
            .eq(BillingCareLevelFee::getStatus, 1)
            .orderByDesc(BillingCareLevelFee::getEffectiveMonth));

    Map<String, BillingCareLevelFee> latestByLevel = new HashMap<>();
    for (BillingCareLevelFee fee : fees) {
      if (fee.getEffectiveMonth().compareTo(billMonth) <= 0
          && !latestByLevel.containsKey(fee.getCareLevel())) {
        latestByLevel.put(fee.getCareLevel(), fee);
      }
    }

    Map<String, BigDecimal> result = new HashMap<>();
    for (Map.Entry<String, BillingCareLevelFee> entry : latestByLevel.entrySet()) {
      result.put(entry.getKey(), entry.getValue().getFeeMonthly());
    }
    return result;
  }
}
