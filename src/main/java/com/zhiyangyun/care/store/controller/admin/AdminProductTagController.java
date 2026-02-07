package com.zhiyangyun.care.store.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.store.entity.ProductTag;
import com.zhiyangyun.care.store.mapper.ProductTagMapper;
import com.zhiyangyun.care.store.model.admin.ProductTagRequest;
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
@RequestMapping("/api/admin/product-tag")
public class AdminProductTagController {
  private final ProductTagMapper productTagMapper;

  public AdminProductTagController(ProductTagMapper productTagMapper) {
    this.productTagMapper = productTagMapper;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Result<ProductTag> create(@Valid @RequestBody ProductTagRequest request) {
    ProductTag tag = new ProductTag();
    tag.setOrgId(AuthContext.getOrgId());
    tag.setTagCode(request.getTagCode());
    tag.setTagName(request.getTagName());
    tag.setTagType(request.getTagType());
    productTagMapper.insert(tag);
    return Result.ok(tag);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Result<ProductTag> update(@PathVariable Long id, @Valid @RequestBody ProductTagRequest request) {
    ProductTag tag = productTagMapper.selectById(id);
    if (tag == null) {
      return Result.error(404, "Tag not found");
    }
    tag.setOrgId(AuthContext.getOrgId());
    tag.setTagCode(request.getTagCode());
    tag.setTagName(request.getTagName());
    tag.setTagType(request.getTagType());
    productTagMapper.updateById(tag);
    return Result.ok(tag);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    ProductTag tag = productTagMapper.selectById(id);
    if (tag == null) {
      return Result.error(404, "Tag not found");
    }
    tag.setIsDeleted(1);
    productTagMapper.updateById(tag);
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public Result<ProductTag> get(@PathVariable Long id) {
    return Result.ok(productTagMapper.selectById(id));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public Result<IPage<ProductTag>> page(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String sortBy,
      @RequestParam(defaultValue = "desc") String order) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(ProductTag.class)
        .eq(ProductTag::getIsDeleted, 0)
        .eq(orgId != null, ProductTag::getOrgId, orgId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(ProductTag::getTagName, keyword)
          .or().like(ProductTag::getTagCode, keyword));
    }
    if (sortBy != null && !sortBy.isBlank()) {
      boolean asc = "asc".equalsIgnoreCase(order);
      if ("tagName".equals(sortBy)) {
        wrapper.orderBy(true, asc, ProductTag::getTagName);
      } else if ("tagCode".equals(sortBy)) {
        wrapper.orderBy(true, asc, ProductTag::getTagCode);
      } else if ("createTime".equals(sortBy)) {
        wrapper.orderBy(true, asc, ProductTag::getCreateTime);
      }
    }
    return Result.ok(productTagMapper.selectPage(new Page<>(page, size), wrapper));
  }
}
