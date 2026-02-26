package com.zhiyangyun.care.store.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.entity.ProductCategory;
import com.zhiyangyun.care.store.mapper.ProductCategoryMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import com.zhiyangyun.care.store.model.admin.ProductCategoryRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/product-category")
public class AdminProductCategoryController {
  private final ProductCategoryMapper productCategoryMapper;
  private final ProductMapper productMapper;

  public AdminProductCategoryController(ProductCategoryMapper productCategoryMapper,
      ProductMapper productMapper) {
    this.productCategoryMapper = productCategoryMapper;
    this.productMapper = productMapper;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Result<ProductCategory> create(@Valid @RequestBody ProductCategoryRequest request) {
    Long orgId = AuthContext.getOrgId();
    if (existsCode(orgId, request.getCategoryCode(), null)) {
      return Result.error(400, "分类编码已存在");
    }
    ProductCategory category = new ProductCategory();
    category.setOrgId(orgId);
    category.setCategoryCode(request.getCategoryCode().trim());
    category.setCategoryName(request.getCategoryName().trim());
    category.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    category.setRemark(request.getRemark());
    productCategoryMapper.insert(category);
    return Result.ok(category);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Result<ProductCategory> update(@PathVariable Long id,
      @Valid @RequestBody ProductCategoryRequest request) {
    Long orgId = AuthContext.getOrgId();
    ProductCategory category = productCategoryMapper.selectById(id);
    if (!isAccessible(category, orgId)) {
      return Result.error(404, "Category not found");
    }
    if (existsCode(orgId, request.getCategoryCode(), id)) {
      return Result.error(400, "分类编码已存在");
    }
    String oldCategoryName = category.getCategoryName();
    String newCategoryName = request.getCategoryName().trim();
    category.setOrgId(orgId);
    category.setCategoryCode(request.getCategoryCode().trim());
    category.setCategoryName(newCategoryName);
    category.setStatus(request.getStatus() == null ? category.getStatus() : request.getStatus());
    category.setRemark(request.getRemark());
    productCategoryMapper.updateById(category);
    if (oldCategoryName != null && !oldCategoryName.equals(newCategoryName)) {
      var products = productMapper.selectList(Wrappers.lambdaQuery(Product.class)
          .eq(Product::getIsDeleted, 0)
          .eq(orgId != null, Product::getOrgId, orgId)
          .eq(Product::getCategory, oldCategoryName));
      for (Product product : products) {
        product.setCategory(newCategoryName);
        productMapper.updateById(product);
      }
    }
    return Result.ok(category);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    ProductCategory category = productCategoryMapper.selectById(id);
    if (!isAccessible(category, orgId)) {
      return Result.error(404, "Category not found");
    }
    long linkedProductCount = productMapper.selectCount(Wrappers.lambdaQuery(Product.class)
        .eq(Product::getIsDeleted, 0)
        .eq(orgId != null, Product::getOrgId, orgId)
        .eq(Product::getCategory, category.getCategoryName()));
    if (linkedProductCount > 0) {
      return Result.error(400, "该分类下存在商品，不能删除");
    }
    category.setIsDeleted(1);
    productCategoryMapper.updateById(category);
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public Result<ProductCategory> get(@PathVariable Long id) {
    Long orgId = AuthContext.getOrgId();
    ProductCategory category = productCategoryMapper.selectById(id);
    if (!isAccessible(category, orgId)) {
      return Result.error(404, "Category not found");
    }
    return Result.ok(category);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public Result<IPage<ProductCategory>> page(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) String sortBy,
      @RequestParam(defaultValue = "desc") String order) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(ProductCategory.class)
        .eq(ProductCategory::getIsDeleted, 0)
        .eq(orgId != null, ProductCategory::getOrgId, orgId)
        .eq(status != null, ProductCategory::getStatus, status);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(ProductCategory::getCategoryName, keyword)
          .or().like(ProductCategory::getCategoryCode, keyword));
    }
    if (sortBy != null && !sortBy.isBlank()) {
      boolean asc = "asc".equalsIgnoreCase(order);
      if ("categoryName".equals(sortBy)) {
        wrapper.orderBy(true, asc, ProductCategory::getCategoryName);
      } else if ("categoryCode".equals(sortBy)) {
        wrapper.orderBy(true, asc, ProductCategory::getCategoryCode);
      } else if ("createTime".equals(sortBy)) {
        wrapper.orderBy(true, asc, ProductCategory::getCreateTime);
      }
    } else {
      wrapper.orderByDesc(ProductCategory::getCreateTime);
    }
    return Result.ok(productCategoryMapper.selectPage(new Page<>(page, size), wrapper));
  }

  private boolean existsCode(Long orgId, String code, Long excludeId) {
    return productCategoryMapper.selectCount(Wrappers.lambdaQuery(ProductCategory.class)
        .eq(ProductCategory::getIsDeleted, 0)
        .eq(orgId != null, ProductCategory::getOrgId, orgId)
        .eq(ProductCategory::getCategoryCode, code.trim())
        .ne(excludeId != null, ProductCategory::getId, excludeId)) > 0;
  }

  private boolean isAccessible(ProductCategory category, Long orgId) {
    if (category == null) {
      return false;
    }
    if (category.getIsDeleted() != null && category.getIsDeleted() == 1) {
      return false;
    }
    return orgId == null || orgId.equals(category.getOrgId());
  }
}
