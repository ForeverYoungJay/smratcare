package com.zhiyangyun.care.store.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.entity.MaterialPurchaseOrder;
import com.zhiyangyun.care.store.entity.MaterialTransferOrder;
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.MaterialPurchaseOrderMapper;
import com.zhiyangyun.care.store.mapper.MaterialTransferOrderMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import com.zhiyangyun.care.store.model.MaterialCenterOverviewResponse;
import com.zhiyangyun.care.store.service.MaterialCenterOverviewService;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MaterialCenterOverviewServiceImpl implements MaterialCenterOverviewService {
  private final ProductMapper productMapper;
  private final InventoryBatchMapper inventoryBatchMapper;
  private final MaterialPurchaseOrderMapper purchaseOrderMapper;
  private final MaterialTransferOrderMapper transferOrderMapper;

  public MaterialCenterOverviewServiceImpl(
      ProductMapper productMapper,
      InventoryBatchMapper inventoryBatchMapper,
      MaterialPurchaseOrderMapper purchaseOrderMapper,
      MaterialTransferOrderMapper transferOrderMapper) {
    this.productMapper = productMapper;
    this.inventoryBatchMapper = inventoryBatchMapper;
    this.purchaseOrderMapper = purchaseOrderMapper;
    this.transferOrderMapper = transferOrderMapper;
  }

  @Override
  public MaterialCenterOverviewResponse overview(Long orgId, int expiryDays) {
    int normalizedExpiryDays = Math.max(expiryDays, 0);

    List<Product> products = productMapper.selectList(Wrappers.lambdaQuery(Product.class)
        .eq(Product::getIsDeleted, 0)
        .eq(orgId != null, Product::getOrgId, orgId));

    List<InventoryBatch> batches = inventoryBatchMapper.selectList(Wrappers.lambdaQuery(InventoryBatch.class)
        .eq(InventoryBatch::getIsDeleted, 0)
        .eq(orgId != null, InventoryBatch::getOrgId, orgId));

    Map<Long, Integer> qtyByProduct = new HashMap<>();
    for (InventoryBatch batch : batches) {
      if (batch.getProductId() == null) {
        continue;
      }
      qtyByProduct.merge(batch.getProductId(), batch.getQuantity() == null ? 0 : batch.getQuantity(), Integer::sum);
    }

    long lowStockCount = products.stream()
        .filter(p -> {
          int current = qtyByProduct.getOrDefault(p.getId(), 0);
          int safety = p.getSafetyStock() == null ? 0 : p.getSafetyStock();
          return current < safety;
        })
        .count();

    LocalDate threshold = LocalDate.now().plusDays(normalizedExpiryDays);
    long expiryAlertCount = inventoryBatchMapper.selectCount(Wrappers.lambdaQuery(InventoryBatch.class)
        .eq(InventoryBatch::getIsDeleted, 0)
        .eq(orgId != null, InventoryBatch::getOrgId, orgId)
        .isNotNull(InventoryBatch::getExpireDate)
        .le(InventoryBatch::getExpireDate, threshold));

    long materialPurchaseDraftCount = purchaseOrderMapper.selectCount(Wrappers.lambdaQuery(MaterialPurchaseOrder.class)
        .eq(MaterialPurchaseOrder::getIsDeleted, 0)
        .eq(orgId != null, MaterialPurchaseOrder::getOrgId, orgId)
        .eq(MaterialPurchaseOrder::getStatus, "DRAFT"));

    long materialTransferDraftCount = transferOrderMapper.selectCount(Wrappers.lambdaQuery(MaterialTransferOrder.class)
        .eq(MaterialTransferOrder::getIsDeleted, 0)
        .eq(orgId != null, MaterialTransferOrder::getOrgId, orgId)
        .eq(MaterialTransferOrder::getStatus, "DRAFT"));

    MaterialCenterOverviewResponse response = new MaterialCenterOverviewResponse();
    response.setLowStockCount(Math.max(lowStockCount, 0));
    response.setExpiryAlertCount(Math.max(expiryAlertCount, 0));
    response.setMaterialPurchaseDraftCount(Math.max(materialPurchaseDraftCount, 0));
    response.setMaterialTransferDraftCount(Math.max(materialTransferDraftCount, 0));
    return response;
  }
}
