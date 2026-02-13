package com.zhiyangyun.care.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import com.zhiyangyun.care.store.model.ProductStockResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store/product")
public class StoreProductController {
  private final ProductMapper productMapper;
  private final InventoryBatchMapper inventoryBatchMapper;
  private final AuditLogService auditLogService;

  public StoreProductController(ProductMapper productMapper,
      InventoryBatchMapper inventoryBatchMapper,
      AuditLogService auditLogService) {
    this.productMapper = productMapper;
    this.inventoryBatchMapper = inventoryBatchMapper;
    this.auditLogService = auditLogService;
  }

  @GetMapping("/page")
  public Result<IPage<ProductStockResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(orgId != null, Product::getOrgId, orgId)
        .eq(Product::getIsDeleted, 0);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(Product::getProductName, keyword)
          .or().like(Product::getProductCode, keyword));
    }
    wrapper.orderByDesc(Product::getCreateTime);
    IPage<Product> page = productMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> productIds = page.getRecords().stream().map(Product::getId).toList();
    Map<Long, Integer> stockMap = new HashMap<>();
    if (!productIds.isEmpty()) {
      List<InventoryBatch> batches = inventoryBatchMapper.selectList(
          new LambdaQueryWrapper<InventoryBatch>()
              .in(InventoryBatch::getProductId, productIds)
              .eq(InventoryBatch::getIsDeleted, 0));
      for (InventoryBatch batch : batches) {
        stockMap.merge(batch.getProductId(),
            batch.getQuantity() == null ? 0 : batch.getQuantity(),
            Integer::sum);
      }
    }
    IPage<ProductStockResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(product -> {
      ProductStockResponse item = new ProductStockResponse();
      item.setId(product.getId());
      item.setIdStr(String.valueOf(product.getId()));
      item.setOrgId(product.getOrgId());
      item.setProductCode(product.getProductCode());
      item.setProductName(product.getProductName());
      item.setPrice(product.getPrice());
      item.setPointsPrice(product.getPointsPrice());
      item.setSafetyStock(product.getSafetyStock());
      item.setCurrentStock(stockMap.getOrDefault(product.getId(), 0));
      item.setStatus(product.getStatus());
      item.setTagIds(product.getTagIds());
      item.setCreateTime(product.getCreateTime());
      return item;
    }).toList());
    return Result.ok(resp);
  }

  @GetMapping("/{id}")
  public Result<Product> get(@PathVariable Long id) {
    return Result.ok(productMapper.selectById(id));
  }

  @PostMapping
  public Result<Product> create(@Valid @RequestBody Product product) {
    Long orgId = AuthContext.getOrgId();
    product.setOrgId(orgId);
    applyDefaultsForCreate(product);
    productMapper.insert(product);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "CREATE", "PRODUCT", product.getId(), "新增商品");
    return Result.ok(product);
  }

  @PutMapping("/{id}")
  public Result<Product> update(@PathVariable Long id, @Valid @RequestBody Product product) {
    Long orgId = AuthContext.getOrgId();
    product.setId(id);
    product.setOrgId(orgId);
    Product existing = productMapper.selectById(id);
    applyDefaultsForUpdate(product, existing);
    productMapper.updateById(product);
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "UPDATE", "PRODUCT", id, "更新商品");
    return Result.ok(product);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    Product existing = productMapper.selectById(id);
    if (existing != null) {
      existing.setIsDeleted(1);
      productMapper.updateById(existing);
    }
    auditLogService.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(),
        "DELETE", "PRODUCT", id, "删除商品");
    return Result.ok(null);
  }

  private void applyDefaultsForCreate(Product product) {
    if (isBlank(product.getProductCode())) {
      product.setProductCode("P" + System.currentTimeMillis());
    }
    if (product.getPrice() == null) {
      product.setPrice(BigDecimal.ZERO);
    }
    if (product.getPointsPrice() == null) {
      product.setPointsPrice(0);
    }
    if (product.getSafetyStock() == null) {
      product.setSafetyStock(0);
    }
    if (product.getStatus() == null) {
      product.setStatus(1);
    }
  }

  private void applyDefaultsForUpdate(Product product, Product existing) {
    if (existing == null) {
      applyDefaultsForCreate(product);
      return;
    }
    if (isBlank(product.getProductCode())) {
      product.setProductCode(existing.getProductCode());
    }
    if (product.getProductName() == null) {
      product.setProductName(existing.getProductName());
    }
    if (product.getPrice() == null) {
      product.setPrice(existing.getPrice());
    }
    if (product.getPointsPrice() == null) {
      product.setPointsPrice(existing.getPointsPrice());
    }
    if (product.getSafetyStock() == null) {
      product.setSafetyStock(existing.getSafetyStock());
    }
    if (product.getStatus() == null) {
      product.setStatus(existing.getStatus());
    }
    if (product.getCategory() == null) {
      product.setCategory(existing.getCategory());
    }
    if (product.getUnit() == null) {
      product.setUnit(existing.getUnit());
    }
    if (product.getTagIds() == null) {
      product.setTagIds(existing.getTagIds());
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
