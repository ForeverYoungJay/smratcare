package com.zhiyangyun.care.bill.service;

import com.zhiyangyun.care.bill.entity.BillingCareLevelFee;
import com.zhiyangyun.care.bill.entity.BillingConfigEntry;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BillingConfigService {
  BillingConfigEntry upsertConfig(BillingConfigEntry entry);

  BillingCareLevelFee upsertCareLevelFee(BillingCareLevelFee fee);

  List<BillingConfigEntry> listConfigs(Long orgId, String effectiveMonth);

  List<BillingCareLevelFee> listCareLevelFees(Long orgId, String effectiveMonth);

  BigDecimal getConfigValue(Long orgId, String configKey, String billMonth);

  Map<String, BigDecimal> getCareLevelFees(Long orgId, String billMonth);
}
