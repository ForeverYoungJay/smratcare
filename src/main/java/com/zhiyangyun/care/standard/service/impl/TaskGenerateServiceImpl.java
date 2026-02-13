package com.zhiyangyun.care.standard.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.entity.CareTaskTemplate;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.mapper.CareTaskTemplateMapper;
import com.zhiyangyun.care.standard.entity.CarePackage;
import com.zhiyangyun.care.standard.entity.CarePackageItem;
import com.zhiyangyun.care.standard.entity.ElderPackage;
import com.zhiyangyun.care.standard.entity.ServiceItem;
import com.zhiyangyun.care.standard.mapper.CarePackageItemMapper;
import com.zhiyangyun.care.standard.mapper.CarePackageMapper;
import com.zhiyangyun.care.standard.mapper.ElderPackageMapper;
import com.zhiyangyun.care.standard.mapper.ServiceItemMapper;
import com.zhiyangyun.care.standard.service.TaskGenerateService;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskGenerateServiceImpl implements TaskGenerateService {
  private final ElderPackageMapper elderPackageMapper;
  private final CarePackageItemMapper packageItemMapper;
  private final CarePackageMapper packageMapper;
  private final ServiceItemMapper serviceItemMapper;
  private final CareTaskDailyMapper taskDailyMapper;
  private final CareTaskTemplateMapper taskTemplateMapper;
  private final ElderMapper elderMapper;

  public TaskGenerateServiceImpl(ElderPackageMapper elderPackageMapper,
                                 CarePackageItemMapper packageItemMapper,
                                 CarePackageMapper packageMapper,
                                 ServiceItemMapper serviceItemMapper,
                                 CareTaskDailyMapper taskDailyMapper,
                                 CareTaskTemplateMapper taskTemplateMapper,
                                 ElderMapper elderMapper) {
    this.elderPackageMapper = elderPackageMapper;
    this.packageItemMapper = packageItemMapper;
    this.packageMapper = packageMapper;
    this.serviceItemMapper = serviceItemMapper;
    this.taskDailyMapper = taskDailyMapper;
    this.taskTemplateMapper = taskTemplateMapper;
    this.elderMapper = elderMapper;
  }

  @Override
  @Transactional
  public int generateByElderPackage(Long tenantId, LocalDate date, boolean force) {
    if (tenantId == null || date == null) {
      return 0;
    }
    Long existingCount = taskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getTenantId, tenantId)
        .eq(CareTaskDaily::getTaskDate, date)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(CareTaskDaily::getSourceType, "ELDER_PACKAGE"));
    if (!force && existingCount != null && existingCount > 0) {
      return 0;
    }

    List<ElderPackage> elderPackages = elderPackageMapper.selectList(Wrappers.lambdaQuery(ElderPackage.class)
        .eq(ElderPackage::getIsDeleted, 0)
        .eq(ElderPackage::getTenantId, tenantId)
        .eq(ElderPackage::getStatus, 1)
        .le(ElderPackage::getStartDate, date)
        .and(w -> w.isNull(ElderPackage::getEndDate).or().ge(ElderPackage::getEndDate, date)));

    if (elderPackages.isEmpty()) {
      return 0;
    }

    Map<Long, CarePackage> packageMap = packageMapper.selectBatchIds(
            elderPackages.stream().map(ElderPackage::getPackageId).filter(Objects::nonNull).distinct().toList())
        .stream().collect(Collectors.toMap(CarePackage::getId, v -> v));

    List<Long> packageIds = elderPackages.stream().map(ElderPackage::getPackageId).filter(Objects::nonNull).distinct().toList();
    List<CarePackageItem> packageItems = packageItemMapper.selectList(Wrappers.lambdaQuery(CarePackageItem.class)
        .eq(CarePackageItem::getIsDeleted, 0)
        .eq(CarePackageItem::getTenantId, tenantId)
        .in(!packageIds.isEmpty(), CarePackageItem::getPackageId, packageIds)
        .eq(CarePackageItem::getEnabled, 1));

    Map<Long, List<CarePackageItem>> itemsByPackage = packageItems.stream()
        .collect(Collectors.groupingBy(CarePackageItem::getPackageId));

    Map<Long, ServiceItem> serviceItemMap = serviceItemMapper.selectBatchIds(
            packageItems.stream().map(CarePackageItem::getItemId).filter(Objects::nonNull).distinct().toList())
        .stream().collect(Collectors.toMap(ServiceItem::getId, v -> v));

    Map<Long, CareTaskTemplate> templateMap = new HashMap<>();
    int count = 0;
    for (ElderPackage elderPackage : elderPackages) {
      ElderProfile elder = elderMapper.selectById(elderPackage.getElderId());
      if (elder == null) {
        continue;
      }
      List<CarePackageItem> items = itemsByPackage.getOrDefault(elderPackage.getPackageId(), List.of());
      for (CarePackageItem pkgItem : items) {
        ServiceItem serviceItem = serviceItemMap.get(pkgItem.getItemId());
        if (serviceItem == null || serviceItem.getEnabled() != null && serviceItem.getEnabled() == 0) {
          continue;
        }
        CareTaskTemplate template = templateMap.computeIfAbsent(serviceItem.getId(), key -> {
          CareTaskTemplate t = new CareTaskTemplate();
          t.setTenantId(tenantId);
          t.setOrgId(elder.getOrgId());
          t.setTaskName(serviceItem.getName());
          t.setFrequencyPerDay(pkgItem.getFrequencyPerDay() == null ? 1 : pkgItem.getFrequencyPerDay());
          t.setCareLevelRequired(elder.getCareLevel());
          t.setEnabled(false);
          t.setCreatedBy(elderPackage.getCreatedBy());
          taskTemplateMapper.insert(t);
          return t;
        });

        int frequency = pkgItem.getFrequencyPerDay() == null ? 1 : pkgItem.getFrequencyPerDay();
        for (int i = 0; i < frequency; i++) {
          CareTaskDaily task = new CareTaskDaily();
          task.setTenantId(tenantId);
          task.setOrgId(elder.getOrgId());
          task.setElderId(elder.getId());
          task.setBedId(elder.getBedId());
          task.setTemplateId(template.getId());
          task.setTaskDate(date);
          task.setPlanTime(date.atStartOfDay().plusHours(8).plusMinutes(i * 60));
          task.setStatus("PENDING");
          task.setSourceType("ELDER_PACKAGE");
          task.setSourceId(elderPackage.getId());
          task.setCreatedBy(elderPackage.getCreatedBy());
          taskDailyMapper.insert(task);
          count++;
        }
      }
    }

    return count;
  }
}
