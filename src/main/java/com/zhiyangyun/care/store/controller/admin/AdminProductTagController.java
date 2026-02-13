package com.zhiyangyun.care.store.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.store.entity.ProductTag;
import com.zhiyangyun.care.store.mapper.ProductTagMapper;
import com.zhiyangyun.care.store.model.admin.ProductTagRequest;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import jakarta.validation.Valid;
import java.util.Locale;
import java.util.Random;
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
    tag.setTagCode(resolveTagCode(request.getTagCode(), request.getTagName(), tag.getOrgId()));
    tag.setTagName(request.getTagName());
    tag.setTagType(request.getTagType());
    tag.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    productTagMapper.insert(tag);
    return Result.ok(tag);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Result<ProductTag> update(@PathVariable Long id, @Valid @RequestBody ProductTagRequest request) {
    ProductTag tag = productTagMapper.selectById(id);
    if (tag == null && request.getTagCode() != null && !request.getTagCode().isBlank()) {
      Long orgId = AuthContext.getOrgId();
      tag = productTagMapper.selectOne(Wrappers.lambdaQuery(ProductTag.class)
          .eq(ProductTag::getIsDeleted, 0)
          .eq(orgId != null, ProductTag::getOrgId, orgId)
          .eq(ProductTag::getTagCode, request.getTagCode()));
    }
    if (tag == null) {
      return Result.error(404, "Tag not found");
    }
    tag.setOrgId(AuthContext.getOrgId());
    tag.setTagCode(resolveTagCode(request.getTagCode(), request.getTagName(), tag.getOrgId()));
    tag.setTagName(request.getTagName());
    tag.setTagType(request.getTagType());
    tag.setStatus(request.getStatus() == null ? tag.getStatus() : request.getStatus());
    productTagMapper.updateById(tag);
    return Result.ok(tag);
  }

  private String resolveTagCode(String inputCode, String tagName, Long orgId) {
    if (inputCode != null && !inputCode.isBlank()) {
      return inputCode.trim();
    }
    String base = toPinyin(tagName);
    if (base.isBlank()) {
      base = "TAG_" + System.currentTimeMillis();
    }
    String code = base;
    if (existsCode(orgId, code)) {
      code = base + "_" + new Random().nextInt(9999);
    }
    return code;
  }

  private boolean existsCode(Long orgId, String code) {
    return productTagMapper.selectCount(Wrappers.lambdaQuery(ProductTag.class)
        .eq(ProductTag::getIsDeleted, 0)
        .eq(orgId != null, ProductTag::getOrgId, orgId)
        .eq(ProductTag::getTagCode, code)) > 0;
  }

  private String toPinyin(String text) {
    if (text == null) return "";
    StringBuilder sb = new StringBuilder();
    HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
    format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
    format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    for (char c : text.toCharArray()) {
      if (c <= 127) {
        if (Character.isLetterOrDigit(c)) {
          sb.append(Character.toUpperCase(c));
        } else {
          sb.append('_');
        }
        continue;
      }
      try {
        String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format);
        if (pinyin != null && pinyin.length > 0) {
          sb.append(pinyin[0]);
        }
      } catch (Exception ignored) {
        sb.append('_');
      }
      sb.append('_');
    }
    String normalized = sb.toString().replaceAll("_+", "_");
    if (normalized.startsWith("_")) normalized = normalized.substring(1);
    if (normalized.endsWith("_")) normalized = normalized.substring(0, normalized.length() - 1);
    return normalized.toUpperCase(Locale.ROOT);
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
