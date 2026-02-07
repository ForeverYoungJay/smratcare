package com.zhiyangyun.care.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store/product")
public class StoreProductController {
  private final ProductMapper productMapper;

  public StoreProductController(ProductMapper productMapper) {
    this.productMapper = productMapper;
  }

  @GetMapping("/page")
  public Result<IPage<Product>> page(
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
    return Result.ok(productMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/{id}")
  public Result<Product> get(@PathVariable Long id) {
    return Result.ok(productMapper.selectById(id));
  }

  @PostMapping
  public Result<Product> create(@Valid @RequestBody Product product) {
    product.setOrgId(AuthContext.getOrgId());
    applyDefaultsForCreate(product);
    productMapper.insert(product);
    return Result.ok(product);
  }

  @PutMapping("/{id}")
  public Result<Product> update(@PathVariable Long id, @Valid @RequestBody Product product) {
    product.setId(id);
    product.setOrgId(AuthContext.getOrgId());
    Product existing = productMapper.selectById(id);
    applyDefaultsForUpdate(product, existing);
    productMapper.updateById(product);
    return Result.ok(product);
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
