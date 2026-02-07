package com.zhiyangyun.care.store.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.store.entity.Disease;
import com.zhiyangyun.care.store.mapper.DiseaseMapper;
import com.zhiyangyun.care.store.model.admin.DiseaseRequest;
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
@RequestMapping("/api/admin/disease")
public class AdminDiseaseController {
  private final DiseaseMapper diseaseMapper;

  public AdminDiseaseController(DiseaseMapper diseaseMapper) {
    this.diseaseMapper = diseaseMapper;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public Result<Disease> create(@Valid @RequestBody DiseaseRequest request) {
    Disease disease = new Disease();
    disease.setOrgId(AuthContext.getOrgId());
    disease.setDiseaseCode(request.getDiseaseCode());
    disease.setDiseaseName(request.getDiseaseName());
    disease.setRemark(request.getRemark());
    diseaseMapper.insert(disease);
    return Result.ok(disease);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public Result<Disease> update(@PathVariable Long id, @Valid @RequestBody DiseaseRequest request) {
    Disease disease = diseaseMapper.selectById(id);
    if (disease == null) {
      return Result.error(404, "Disease not found");
    }
    disease.setOrgId(AuthContext.getOrgId());
    disease.setDiseaseCode(request.getDiseaseCode());
    disease.setDiseaseName(request.getDiseaseName());
    disease.setRemark(request.getRemark());
    diseaseMapper.updateById(disease);
    return Result.ok(disease);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    Disease disease = diseaseMapper.selectById(id);
    if (disease == null) {
      return Result.error(404, "Disease not found");
    }
    disease.setIsDeleted(1);
    diseaseMapper.updateById(disease);
    return Result.ok(null);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public Result<Disease> get(@PathVariable Long id) {
    return Result.ok(diseaseMapper.selectById(id));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public Result<IPage<Disease>> page(
      @RequestParam(defaultValue = "1") long page,
      @RequestParam(defaultValue = "20") long size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String sortBy,
      @RequestParam(defaultValue = "desc") String order) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(Disease.class)
        .eq(Disease::getIsDeleted, 0)
        .eq(orgId != null, Disease::getOrgId, orgId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(Disease::getDiseaseName, keyword)
          .or().like(Disease::getDiseaseCode, keyword));
    }
    if (sortBy != null && !sortBy.isBlank()) {
      boolean asc = "asc".equalsIgnoreCase(order);
      if ("diseaseName".equals(sortBy)) {
        wrapper.orderBy(true, asc, Disease::getDiseaseName);
      } else if ("diseaseCode".equals(sortBy)) {
        wrapper.orderBy(true, asc, Disease::getDiseaseCode);
      } else if ("createTime".equals(sortBy)) {
        wrapper.orderBy(true, asc, Disease::getCreateTime);
      }
    }
    return Result.ok(diseaseMapper.selectPage(new Page<>(page, size), wrapper));
  }
}
