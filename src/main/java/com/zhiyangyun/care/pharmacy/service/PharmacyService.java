package com.zhiyangyun.care.pharmacy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.pharmacy.entity.DrugBatch;
import com.zhiyangyun.care.pharmacy.entity.DrugCatalog;
import com.zhiyangyun.care.pharmacy.entity.DrugDispenseRecord;
import com.zhiyangyun.care.pharmacy.model.DrugDispenseRequest;
import com.zhiyangyun.care.pharmacy.model.DrugInboundRequest;
import java.util.List;

/** 药事管理：药品目录、批次库存、FEFO 发药、临期/低库存监控。 */
public interface PharmacyService {

  /** 药品入库（新批次或累加同批号），返回批次 id。 */
  Long inbound(DrugInboundRequest request);

  /** 按 FEFO 发药，按批次扣减并生成发药记录，返回记录列表。 */
  List<DrugDispenseRecord> dispense(DrugDispenseRequest request);

  IPage<DrugCatalog> pageDrugs(int pageNo, int pageSize, String keyword, String category, Integer status);

  IPage<DrugBatch> pageBatches(int pageNo, int pageSize, Long drugId, String status);

  IPage<DrugDispenseRecord> pageDispenseRecords(int pageNo, int pageSize, Long drugId, Long elderId);

  /** 扫描临期批次与低于安全库存的药品并生成告警（供定时任务调用），返回生成告警数。 */
  int scanExpiryAndLowStock(int expiryWarningDays);
}
