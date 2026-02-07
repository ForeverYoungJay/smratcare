package com.zhiyangyun.care.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.model.InventoryAdjustRequest;
import com.zhiyangyun.care.store.model.InventoryAlertItem;
import com.zhiyangyun.care.store.model.InventoryExpiryAlertItem;
import com.zhiyangyun.care.store.service.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
  private final InventoryService inventoryService;
  private final InventoryBatchMapper inventoryBatchMapper;

  public InventoryController(InventoryService inventoryService,
      InventoryBatchMapper inventoryBatchMapper) {
    this.inventoryService = inventoryService;
    this.inventoryBatchMapper = inventoryBatchMapper;
  }

  @PostMapping("/adjust")
  public Result<Void> adjust(@Valid @RequestBody InventoryAdjustRequest request) {
    request.setOrgId(AuthContext.getOrgId());
    inventoryService.adjust(request);
    return Result.ok(null);
  }

  @GetMapping("/alerts")
  public Result<List<InventoryAlertItem>> alerts() {
    return Result.ok(inventoryService.lowStockAlerts(AuthContext.getOrgId()));
  }

  @GetMapping("/expiry-alerts")
  public Result<List<InventoryExpiryAlertItem>> expiryAlerts(
      @RequestParam(defaultValue = "30") int days) {
    return Result.ok(inventoryService.expiryAlerts(AuthContext.getOrgId(), days));
  }

  @GetMapping("/page")
  public Result<IPage<InventoryBatch>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long productId) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<InventoryBatch> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(orgId != null, InventoryBatch::getOrgId, orgId)
        .eq(InventoryBatch::getIsDeleted, 0)
        .eq(productId != null, InventoryBatch::getProductId, productId)
        .orderByDesc(InventoryBatch::getCreateTime);
    return Result.ok(inventoryBatchMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }
}
