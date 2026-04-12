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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
  private static final String DISEASE_READ_ROLES =
      "hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','MEDICAL_EMPLOYEE','MEDICAL_MINISTER',"
          + "'NURSING_EMPLOYEE','NURSING_MINISTER','HR_EMPLOYEE','HR_MINISTER',"
          + "'LOGISTICS_EMPLOYEE','LOGISTICS_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String DISEASE_WRITE_ROLES =
      "hasAnyRole('MARKETING_EMPLOYEE','MARKETING_MINISTER','MEDICAL_EMPLOYEE','MEDICAL_MINISTER',"
          + "'NURSING_EMPLOYEE','NURSING_MINISTER','HR_EMPLOYEE','HR_MINISTER',"
          + "'LOGISTICS_EMPLOYEE','LOGISTICS_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private static final String DISEASE_DELETE_ROLES =
      "hasAnyRole('LOGISTICS_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private final DiseaseMapper diseaseMapper;

  public AdminDiseaseController(DiseaseMapper diseaseMapper) {
    this.diseaseMapper = diseaseMapper;
  }

  @PreAuthorize(DISEASE_WRITE_ROLES)
  @PostMapping
  public Result<Disease> create(@Valid @RequestBody DiseaseRequest request) {
    Disease disease = new Disease();
    disease.setOrgId(AuthContext.getOrgId());
    disease.setDiseaseCode(resolveDiseaseCode(request.getDiseaseCode(), null));
    disease.setDiseaseName(request.getDiseaseName());
    disease.setRemark(request.getRemark());
    diseaseMapper.insert(disease);
    return Result.ok(disease);
  }

  @PreAuthorize(DISEASE_WRITE_ROLES)
  @PutMapping("/{id}")
  public Result<Disease> update(@PathVariable Long id, @Valid @RequestBody DiseaseRequest request) {
    Disease disease = diseaseMapper.selectById(id);
    if (disease == null) {
      return Result.error(404, "Disease not found");
    }
    disease.setOrgId(AuthContext.getOrgId());
    disease.setDiseaseCode(resolveDiseaseCode(request.getDiseaseCode(), disease.getDiseaseCode()));
    disease.setDiseaseName(request.getDiseaseName());
    disease.setRemark(request.getRemark());
    diseaseMapper.updateById(disease);
    return Result.ok(disease);
  }

  @PreAuthorize(DISEASE_DELETE_ROLES)
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

  @PreAuthorize(DISEASE_READ_ROLES)
  @GetMapping("/{id}")
  public Result<Disease> get(@PathVariable Long id) {
    return Result.ok(diseaseMapper.selectById(id));
  }

  @PreAuthorize(DISEASE_READ_ROLES)
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

  private String resolveDiseaseCode(String incoming, String fallback) {
    String normalized = incoming == null ? "" : incoming.trim();
    if (!normalized.isEmpty()) {
      return normalized.toUpperCase();
    }
    String fallbackCode = fallback == null ? "" : fallback.trim();
    if (!fallbackCode.isEmpty()) {
      return fallbackCode;
    }
    return "DIS-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
  }
}
