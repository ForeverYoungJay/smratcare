package com.zhiyangyun.care.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthMedicationDeposit;
import com.zhiyangyun.care.health.entity.HealthMedicationRegistration;
import com.zhiyangyun.care.health.entity.HealthMedicationSetting;
import com.zhiyangyun.care.health.mapper.HealthMedicationDepositMapper;
import com.zhiyangyun.care.health.mapper.HealthMedicationRegistrationMapper;
import com.zhiyangyun.care.health.mapper.HealthMedicationSettingMapper;
import com.zhiyangyun.care.health.model.HealthMedicationRemainingItem;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health/medication/remaining")
public class HealthMedicationRemainingController {
  private final HealthMedicationDepositMapper depositMapper;
  private final HealthMedicationRegistrationMapper registrationMapper;
  private final HealthMedicationSettingMapper settingMapper;

  public HealthMedicationRemainingController(
      HealthMedicationDepositMapper depositMapper,
      HealthMedicationRegistrationMapper registrationMapper,
      HealthMedicationSettingMapper settingMapper) {
    this.depositMapper = depositMapper;
    this.registrationMapper = registrationMapper;
    this.settingMapper = settingMapper;
  }

  @GetMapping("/page")
  public Result<IPage<HealthMedicationRemainingItem>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var depositQuery = Wrappers.lambdaQuery(HealthMedicationDeposit.class)
        .eq(HealthMedicationDeposit::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationDeposit::getOrgId, orgId)
        .eq(elderId != null, HealthMedicationDeposit::getElderId, elderId);
    if (keyword != null && !keyword.isBlank()) {
      depositQuery.and(w -> w.like(HealthMedicationDeposit::getElderName, keyword)
          .or().like(HealthMedicationDeposit::getDrugName, keyword));
    }
    List<HealthMedicationDeposit> deposits = depositMapper.selectList(depositQuery);

    var registrationQuery = Wrappers.lambdaQuery(HealthMedicationRegistration.class)
        .eq(HealthMedicationRegistration::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationRegistration::getOrgId, orgId)
        .eq(elderId != null, HealthMedicationRegistration::getElderId, elderId);
    if (keyword != null && !keyword.isBlank()) {
      registrationQuery.and(w -> w.like(HealthMedicationRegistration::getElderName, keyword)
          .or().like(HealthMedicationRegistration::getDrugName, keyword));
    }
    List<HealthMedicationRegistration> registrations = registrationMapper.selectList(registrationQuery);

    List<HealthMedicationSetting> settings = settingMapper.selectList(Wrappers.lambdaQuery(HealthMedicationSetting.class)
        .eq(HealthMedicationSetting::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationSetting::getOrgId, orgId)
        .eq(elderId != null, HealthMedicationSetting::getElderId, elderId));

    Map<String, HealthMedicationRemainingItem> map = new HashMap<>();
    for (HealthMedicationDeposit deposit : deposits) {
      String key = buildKey(deposit.getElderId(), deposit.getDrugId(), deposit.getDrugName());
      HealthMedicationRemainingItem item = map.computeIfAbsent(key, k -> createBase(deposit.getElderId(), deposit.getElderName(), deposit.getDrugId(), deposit.getDrugName()));
      item.setUnit(item.getUnit() == null ? deposit.getUnit() : item.getUnit());
      item.setDepositQty(item.getDepositQty().add(nvl(deposit.getQuantity())));
    }
    for (HealthMedicationRegistration registration : registrations) {
      String key = buildKey(registration.getElderId(), registration.getDrugId(), registration.getDrugName());
      HealthMedicationRemainingItem item = map.computeIfAbsent(key,
          k -> createBase(registration.getElderId(), registration.getElderName(), registration.getDrugId(), registration.getDrugName()));
      item.setUnit(item.getUnit() == null ? registration.getUnit() : item.getUnit());
      item.setUsedQty(item.getUsedQty().add(nvl(registration.getDosageTaken())));
    }
    for (HealthMedicationSetting setting : settings) {
      String key = buildKey(setting.getElderId(), setting.getDrugId(), setting.getDrugName());
      HealthMedicationRemainingItem item = map.computeIfAbsent(key,
          k -> createBase(setting.getElderId(), setting.getElderName(), setting.getDrugId(), setting.getDrugName()));
      item.setMinRemainQty(setting.getMinRemainQty());
    }

    List<HealthMedicationRemainingItem> rows = new ArrayList<>();
    for (HealthMedicationRemainingItem item : map.values()) {
      BigDecimal remain = item.getDepositQty().subtract(item.getUsedQty());
      item.setRemainQty(remain);
      BigDecimal minRemain = item.getMinRemainQty();
      item.setLowStock(minRemain != null && remain.compareTo(minRemain) < 0 ? 1 : 0);
      rows.add(item);
    }
    rows.sort(Comparator.comparing(HealthMedicationRemainingItem::getLowStock, Comparator.reverseOrder())
        .thenComparing(HealthMedicationRemainingItem::getRemainQty));

    long from = Math.max(0, (pageNo - 1) * pageSize);
    long to = Math.min(rows.size(), from + pageSize);
    List<HealthMedicationRemainingItem> pageRows = from >= rows.size() ? List.of() : rows.subList((int) from, (int) to);

    Page<HealthMedicationRemainingItem> page = new Page<>(pageNo, pageSize, rows.size());
    page.setRecords(pageRows);
    return Result.ok(page);
  }

  private static String buildKey(Long elderId, Long drugId, String drugName) {
    return (elderId == null ? 0 : elderId) + "_" + (drugId == null ? (drugName == null ? "" : drugName) : drugId);
  }

  private static BigDecimal nvl(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private static HealthMedicationRemainingItem createBase(Long elderId, String elderName, Long drugId, String drugName) {
    HealthMedicationRemainingItem item = new HealthMedicationRemainingItem();
    item.setElderId(elderId);
    item.setElderName(elderName);
    item.setDrugId(drugId);
    item.setDrugName(drugName);
    item.setDepositQty(BigDecimal.ZERO);
    item.setUsedQty(BigDecimal.ZERO);
    item.setRemainQty(BigDecimal.ZERO);
    item.setLowStock(0);
    return item;
  }
}
