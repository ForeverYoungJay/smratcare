package com.zhiyangyun.care.elder.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.entity.ElderFileArchive;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderFileArchiveMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import lombok.Data;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 长者电子文件存档：文件本体走既有上传接口，这里只登记归档元数据。 */
@RestController
@RequestMapping("/api/elder/file-archive")
@PreAuthorize("hasAnyRole('NURSING_MINISTER','NURSING_EMPLOYEE','MEDICAL_MINISTER','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class ElderFileArchiveController {
  private static final Set<String> CATEGORIES = Set.of("CONTRACT", "MEDICAL", "CERTIFICATE", "OTHER");

  private final ElderFileArchiveMapper archiveMapper;
  private final ElderMapper elderMapper;
  private final AuditLogService auditLogService;

  public ElderFileArchiveController(ElderFileArchiveMapper archiveMapper,
      ElderMapper elderMapper,
      AuditLogService auditLogService) {
    this.archiveMapper = archiveMapper;
    this.elderMapper = elderMapper;
    this.auditLogService = auditLogService;
  }

  @Data
  public static class ArchiveRequest {
    @NotNull
    private Long elderId;
    @NotBlank
    @Size(max = 24)
    private String category;
    @NotBlank
    @Size(max = 200)
    private String fileName;
    @NotBlank
    @Size(max = 512)
    private String fileUrl;
    private Long fileSize;
    @Size(max = 200)
    private String remark;
  }

  @GetMapping
  public Result<List<ElderFileArchive>> list(
      @RequestParam Long elderId,
      @RequestParam(required = false) String category) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(ElderFileArchive.class)
        .eq(ElderFileArchive::getIsDeleted, 0)
        .eq(orgId != null, ElderFileArchive::getOrgId, orgId)
        .eq(ElderFileArchive::getElderId, elderId);
    String normalized = normalizeCategory(category);
    if (normalized != null) {
      wrapper.eq(ElderFileArchive::getCategory, normalized);
    }
    wrapper.orderByDesc(ElderFileArchive::getUploadedAt).orderByDesc(ElderFileArchive::getId);
    return Result.ok(archiveMapper.selectList(wrapper));
  }

  @PostMapping
  public Result<ElderFileArchive> create(@Valid @RequestBody ArchiveRequest request) {
    Long staffId = AuthContext.getStaffId();
    ElderProfile elder = elderMapper.selectById(request.getElderId());
    if (elder == null || Integer.valueOf(1).equals(elder.getIsDeleted())) {
      throw new IllegalArgumentException("长者不存在");
    }
    ensureOrgAccess(elder.getOrgId());
    String category = normalizeCategory(request.getCategory());
    if (category == null) {
      throw new IllegalArgumentException("文件分类只能是合同/医疗/证件/其他");
    }
    ElderFileArchive archive = new ElderFileArchive();
    archive.setTenantId(elder.getOrgId());
    archive.setOrgId(elder.getOrgId());
    archive.setElderId(elder.getId());
    archive.setCategory(category);
    archive.setFileName(request.getFileName().trim());
    archive.setFileUrl(request.getFileUrl().trim());
    archive.setFileSize(request.getFileSize());
    archive.setRemark(trimToNull(request.getRemark()));
    archive.setUploadedBy(staffId);
    archive.setUploadedAt(LocalDateTime.now());
    archiveMapper.insert(archive);
    auditLogService.record(
        elder.getOrgId(), elder.getOrgId(), staffId, AuthContext.getUsername(),
        "ELDER_FILE_ARCHIVE_ADD", "ELDER_FILE_ARCHIVE", archive.getId(),
        "归档文件 " + archive.getFileName() + "（" + categoryText(category) + "）长者=" + elder.getFullName());
    return Result.ok(archive);
  }

  @DeleteMapping("/{archiveId}")
  public Result<Void> delete(@PathVariable Long archiveId) {
    ElderFileArchive archive = archiveMapper.selectById(archiveId);
    if (archive == null) {
      return Result.ok(null);
    }
    ensureOrgAccess(archive.getOrgId());
    archiveMapper.deleteById(archiveId);
    auditLogService.record(
        archive.getOrgId(), archive.getOrgId(), AuthContext.getStaffId(), AuthContext.getUsername(),
        "ELDER_FILE_ARCHIVE_DELETE", "ELDER_FILE_ARCHIVE", archiveId,
        "删除归档文件 " + archive.getFileName());
    return Result.ok(null);
  }

  private static String normalizeCategory(String category) {
    if (category == null || category.isBlank()) {
      return null;
    }
    String normalized = category.trim().toUpperCase(Locale.ROOT);
    return CATEGORIES.contains(normalized) ? normalized : null;
  }

  private static String categoryText(String category) {
    return switch (category) {
      case "CONTRACT" -> "合同";
      case "MEDICAL" -> "医疗";
      case "CERTIFICATE" -> "证件";
      default -> "其他";
    };
  }

  private void ensureOrgAccess(Long targetOrgId) {
    if (targetOrgId == null) {
      return;
    }
    Long currentOrgId = AuthContext.getOrgId();
    if (currentOrgId == null || AuthContext.isAdmin()) {
      return;
    }
    if (!Objects.equals(currentOrgId, targetOrgId)) {
      throw new AccessDeniedException("No permission to access another organization");
    }
  }

  private static String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
