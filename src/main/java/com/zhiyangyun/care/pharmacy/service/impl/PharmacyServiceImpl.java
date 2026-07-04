package com.zhiyangyun.care.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.pharmacy.entity.DrugBatch;
import com.zhiyangyun.care.pharmacy.entity.DrugCatalog;
import com.zhiyangyun.care.pharmacy.entity.DrugDispenseRecord;
import com.zhiyangyun.care.pharmacy.mapper.DrugBatchMapper;
import com.zhiyangyun.care.pharmacy.mapper.DrugCatalogMapper;
import com.zhiyangyun.care.pharmacy.mapper.DrugDispenseRecordMapper;
import com.zhiyangyun.care.pharmacy.model.DrugDispensePlanItem;
import com.zhiyangyun.care.pharmacy.model.DrugDispenseRequest;
import com.zhiyangyun.care.pharmacy.model.DrugInboundRequest;
import com.zhiyangyun.care.pharmacy.service.DrugFefoAllocator;
import com.zhiyangyun.care.pharmacy.service.PharmacyService;
import com.zhiyangyun.care.smart.entity.SmartAlert;
import com.zhiyangyun.care.smart.mapper.SmartAlertMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PharmacyServiceImpl implements PharmacyService {

  private final DrugCatalogMapper drugMapper;
  private final DrugBatchMapper batchMapper;
  private final DrugDispenseRecordMapper dispenseMapper;
  private final SmartAlertMapper alertMapper;

  public PharmacyServiceImpl(DrugCatalogMapper drugMapper, DrugBatchMapper batchMapper,
      DrugDispenseRecordMapper dispenseMapper, SmartAlertMapper alertMapper) {
    this.drugMapper = drugMapper;
    this.batchMapper = batchMapper;
    this.dispenseMapper = dispenseMapper;
    this.alertMapper = alertMapper;
  }

  @Override
  @Transactional
  public Long inbound(DrugInboundRequest request) {
    if (request.getQuantity() == null || request.getQuantity() <= 0) {
      throw new IllegalArgumentException("入库数量必须大于 0");
    }
    Long orgId = AuthContext.getOrgId();
    LocalDateTime now = LocalDateTime.now();
    DrugBatch existing = batchMapper.selectOne(Wrappers.lambdaQuery(DrugBatch.class)
        .eq(DrugBatch::getIsDeleted, 0)
        .eq(DrugBatch::getOrgId, orgId)
        .eq(DrugBatch::getDrugId, request.getDrugId())
        .eq(DrugBatch::getBatchNo, request.getBatchNo())
        .last("limit 1"));
    if (existing != null) {
      existing.setQuantity((existing.getQuantity() == null ? 0 : existing.getQuantity()) + request.getQuantity());
      existing.setInboundAt(now);
      batchMapper.updateById(existing);
      return existing.getId();
    }
    DrugBatch batch = new DrugBatch();
    batch.setOrgId(orgId);
    batch.setDrugId(request.getDrugId());
    batch.setBatchNo(request.getBatchNo());
    batch.setExpireDate(request.getExpireDate());
    batch.setProductionDate(request.getProductionDate());
    batch.setQuantity(request.getQuantity());
    batch.setPurchasePrice(request.getPurchasePrice() == null ? 0L : request.getPurchasePrice());
    batch.setSupplier(request.getSupplier());
    batch.setInboundAt(now);
    batch.setStatus("ACTIVE");
    batch.setRemark(request.getRemark());
    batch.setCreatedBy(AuthContext.getStaffId());
    batchMapper.insert(batch);
    return batch.getId();
  }

  @Override
  @Transactional
  public List<DrugDispenseRecord> dispense(DrugDispenseRequest request) {
    if (request.getQuantity() == null || request.getQuantity() <= 0) {
      throw new IllegalArgumentException("发药数量必须大于 0");
    }
    Long orgId = AuthContext.getOrgId();
    DrugCatalog drug = drugMapper.selectById(request.getDrugId());
    if (drug == null || Integer.valueOf(1).equals(drug.getIsDeleted())) {
      throw new IllegalArgumentException("药品不存在: " + request.getDrugId());
    }
    List<DrugBatch> batches = batchMapper.selectList(Wrappers.lambdaQuery(DrugBatch.class)
        .eq(DrugBatch::getIsDeleted, 0)
        .eq(DrugBatch::getOrgId, orgId)
        .eq(DrugBatch::getDrugId, request.getDrugId())
        .eq(DrugBatch::getStatus, "ACTIVE")
        .gt(DrugBatch::getQuantity, 0));

    List<DrugDispensePlanItem> plan = DrugFefoAllocator.allocate(batches, request.getQuantity(), LocalDate.now());

    Map<Long, DrugBatch> byId = new HashMap<>();
    for (DrugBatch b : batches) {
      byId.put(b.getId(), b);
    }
    LocalDateTime now = LocalDateTime.now();
    List<DrugDispenseRecord> records = new ArrayList<>();
    for (DrugDispensePlanItem item : plan) {
      DrugBatch b = byId.get(item.getBatchId());
      b.setQuantity(b.getQuantity() - item.getQuantity());
      batchMapper.updateById(b);

      DrugDispenseRecord rec = new DrugDispenseRecord();
      rec.setOrgId(orgId);
      rec.setDrugId(request.getDrugId());
      rec.setBatchId(item.getBatchId());
      rec.setBatchNo(item.getBatchNo());
      rec.setElderId(request.getElderId());
      rec.setOrderId(request.getOrderId());
      rec.setDispenseType("DISPENSE");
      rec.setQuantity(item.getQuantity());
      rec.setOperatorId(AuthContext.getStaffId());
      rec.setDispenseTime(now);
      rec.setRemark(request.getRemark());
      rec.setCreatedBy(AuthContext.getStaffId());
      dispenseMapper.insert(rec);
      records.add(rec);
    }
    return records;
  }

  @Override
  public IPage<DrugCatalog> pageDrugs(int pageNo, int pageSize, String keyword, String category, Integer status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(DrugCatalog.class)
        .eq(DrugCatalog::getIsDeleted, 0)
        .and(w -> w.isNull(DrugCatalog::getOrgId).or(orgId != null, q -> q.eq(DrugCatalog::getOrgId, orgId)))
        .eq(StringUtils.hasText(category), DrugCatalog::getCategory, category)
        .eq(status != null, DrugCatalog::getStatus, status);
    if (StringUtils.hasText(keyword)) {
      wrapper.and(w -> w.like(DrugCatalog::getDrugName, keyword)
          .or().like(DrugCatalog::getDrugCode, keyword)
          .or().like(DrugCatalog::getGenericName, keyword));
    }
    wrapper.orderByAsc(DrugCatalog::getDrugCode);
    return drugMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  public IPage<DrugBatch> pageBatches(int pageNo, int pageSize, Long drugId, String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(DrugBatch.class)
        .eq(DrugBatch::getIsDeleted, 0)
        .eq(orgId != null, DrugBatch::getOrgId, orgId)
        .eq(drugId != null, DrugBatch::getDrugId, drugId)
        .eq(StringUtils.hasText(status), DrugBatch::getStatus, status)
        .orderByAsc(DrugBatch::getExpireDate);
    return batchMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  public IPage<DrugDispenseRecord> pageDispenseRecords(int pageNo, int pageSize, Long drugId, Long elderId) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(DrugDispenseRecord.class)
        .eq(DrugDispenseRecord::getIsDeleted, 0)
        .eq(orgId != null, DrugDispenseRecord::getOrgId, orgId)
        .eq(drugId != null, DrugDispenseRecord::getDrugId, drugId)
        .eq(elderId != null, DrugDispenseRecord::getElderId, elderId)
        .orderByDesc(DrugDispenseRecord::getId);
    return dispenseMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  @Transactional
  public int scanExpiryAndLowStock(int expiryWarningDays) {
    LocalDate today = LocalDate.now();
    LocalDate limit = today.plusDays(Math.max(0, expiryWarningDays));
    int created = 0;

    // 临期批次告警
    List<DrugBatch> nearExpiry = batchMapper.selectList(Wrappers.lambdaQuery(DrugBatch.class)
        .eq(DrugBatch::getIsDeleted, 0)
        .eq(DrugBatch::getStatus, "ACTIVE")
        .gt(DrugBatch::getQuantity, 0)
        .isNotNull(DrugBatch::getExpireDate)
        .le(DrugBatch::getExpireDate, limit));
    for (DrugBatch b : nearExpiry) {
      String alertNo = "EXP" + b.getId();
      if (raiseAlertIfAbsent(b.getOrgId(), alertNo, "DRUG_EXPIRY",
          b.getExpireDate().isBefore(today) ? "CRITICAL" : "WARNING",
          "药品临期/过期",
          "批次[" + b.getBatchNo() + "]效期 " + b.getExpireDate() + "，剩余 " + b.getQuantity())) {
        created++;
      }
    }

    // 低于安全库存告警（按 org+drug 汇总可用量）
    List<DrugBatch> activeBatches = batchMapper.selectList(Wrappers.lambdaQuery(DrugBatch.class)
        .eq(DrugBatch::getIsDeleted, 0)
        .eq(DrugBatch::getStatus, "ACTIVE")
        .gt(DrugBatch::getQuantity, 0));
    Map<String, Integer> sums = new HashMap<>();
    Map<String, DrugBatch> anyBatch = new HashMap<>();
    for (DrugBatch b : activeBatches) {
      String key = b.getOrgId() + ":" + b.getDrugId();
      sums.merge(key, b.getQuantity(), Integer::sum);
      anyBatch.putIfAbsent(key, b);
    }
    for (Map.Entry<String, Integer> e : sums.entrySet()) {
      DrugBatch ref = anyBatch.get(e.getKey());
      DrugCatalog drug = drugMapper.selectById(ref.getDrugId());
      if (drug == null || drug.getSafetyStock() == null || drug.getSafetyStock() <= 0) {
        continue;
      }
      if (e.getValue() < drug.getSafetyStock()) {
        String alertNo = "LOW" + ref.getOrgId() + "-" + ref.getDrugId();
        if (raiseAlertIfAbsent(ref.getOrgId(), alertNo, "DRUG_LOW_STOCK", "WARNING",
            "药品低库存", "药品[" + drug.getDrugName() + "]可用 " + e.getValue()
                + " 低于安全库存 " + drug.getSafetyStock())) {
          created++;
        }
      }
    }
    return created;
  }

  private boolean raiseAlertIfAbsent(Long orgId, String alertNo, String type, String level,
      String title, String content) {
    Long open = alertMapper.selectCount(Wrappers.lambdaQuery(SmartAlert.class)
        .eq(SmartAlert::getIsDeleted, 0)
        .eq(SmartAlert::getAlertNo, alertNo)
        .in(SmartAlert::getStatus, List.of("OPEN", "ACKNOWLEDGED")));
    if (open != null && open > 0) {
      return false;
    }
    LocalDateTime now = LocalDateTime.now();
    SmartAlert alert = new SmartAlert();
    alert.setOrgId(orgId);
    alert.setAlertNo(alertNo);
    alert.setAlertType(type);
    alert.setLevel(level);
    alert.setTitle(title);
    alert.setContent(content);
    alert.setStatus("OPEN");
    alert.setFirstTriggeredAt(now);
    alert.setLatestTriggeredAt(now);
    alert.setEscalationCount(0);
    alert.setNotifyFamily(0);
    alert.setCreateTime(now);
    alert.setUpdateTime(now);
    alert.setIsDeleted(0);
    alertMapper.insert(alert);
    return true;
  }
}
