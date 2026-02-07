package com.zhiyangyun.care.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.entity.Org;
import com.zhiyangyun.care.auth.mapper.OrgMapper;
import com.zhiyangyun.care.auth.model.OrgRequest;
import com.zhiyangyun.care.auth.model.Result;
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
@RequestMapping("/api/admin/orgs")
public class AdminOrgController {
  private final OrgMapper orgMapper;

  public AdminOrgController(OrgMapper orgMapper) {
    this.orgMapper = orgMapper;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Result<Org> create(@Valid @RequestBody OrgRequest request) {
    Org org = new Org();
    org.setOrgCode(request.getOrgCode());
    org.setOrgName(request.getOrgName());
    org.setOrgType(request.getOrgType());
    org.setStatus(request.getStatus());
    org.setContactName(request.getContactName());
    org.setContactPhone(request.getContactPhone());
    org.setAddress(request.getAddress());
    orgMapper.insert(org);
    return Result.ok(org);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Result<Org> update(@PathVariable Long id, @Valid @RequestBody OrgRequest request) {
    Org org = orgMapper.selectById(id);
    if (org == null) {
      return Result.error(404, "Org not found");
    }
    org.setOrgCode(request.getOrgCode());
    org.setOrgName(request.getOrgName());
    org.setOrgType(request.getOrgType());
    org.setStatus(request.getStatus());
    org.setContactName(request.getContactName());
    org.setContactPhone(request.getContactPhone());
    org.setAddress(request.getAddress());
    orgMapper.updateById(org);
    return Result.ok(org);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Org org = orgMapper.selectById(id);
    if (org == null) {
      return Result.error(404, "Org not found");
    }
    org.setIsDeleted(1);
    orgMapper.updateById(org);
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public Result<Org> get(@PathVariable Long id) {
    return Result.ok(orgMapper.selectById(id));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public Result<IPage<Org>> list(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String sortBy,
      @RequestParam(defaultValue = "desc") String order) {
    var wrapper = Wrappers.lambdaQuery(Org.class)
        .eq(Org::getIsDeleted, 0);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(Org::getOrgName, keyword)
          .or().like(Org::getOrgCode, keyword));
    }
    if (sortBy != null && !sortBy.isBlank()) {
      boolean asc = "asc".equalsIgnoreCase(order);
      if ("orgName".equals(sortBy)) {
        wrapper.orderBy(true, asc, Org::getOrgName);
      } else if ("orgCode".equals(sortBy)) {
        wrapper.orderBy(true, asc, Org::getOrgCode);
      } else if ("createTime".equals(sortBy)) {
        wrapper.orderBy(true, asc, Org::getCreateTime);
      }
    }
    return Result.ok(orgMapper.selectPage(new Page<>(page, size), wrapper));
  }
}
