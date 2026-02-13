package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaDocument;
import com.zhiyangyun.care.oa.mapper.OaDocumentMapper;
import com.zhiyangyun.care.oa.model.OaDocumentRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
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
@RequestMapping("/api/oa/document")
public class OaDocumentController {
  private final OaDocumentMapper documentMapper;

  public OaDocumentController(OaDocumentMapper documentMapper) {
    this.documentMapper = documentMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaDocument>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String folder,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaDocument.class)
        .eq(OaDocument::getIsDeleted, 0)
        .eq(orgId != null, OaDocument::getOrgId, orgId)
        .eq(folder != null && !folder.isBlank(), OaDocument::getFolder, folder);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaDocument::getName, keyword)
          .or().like(OaDocument::getUploaderName, keyword));
    }
    wrapper.orderByDesc(OaDocument::getUploadedAt);
    return Result.ok(documentMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<OaDocument> create(@Valid @RequestBody OaDocumentRequest request) {
    Long orgId = AuthContext.getOrgId();
    OaDocument doc = new OaDocument();
    doc.setTenantId(orgId);
    doc.setOrgId(orgId);
    doc.setName(request.getName());
    doc.setFolder(request.getFolder());
    doc.setUrl(request.getUrl());
    doc.setSizeBytes(request.getSizeBytes());
    doc.setUploaderId(request.getUploaderId());
    doc.setUploaderName(request.getUploaderName() == null ? AuthContext.getUsername() : request.getUploaderName());
    doc.setUploadedAt(request.getUploadedAt() == null ? LocalDateTime.now() : request.getUploadedAt());
    doc.setRemark(request.getRemark());
    doc.setCreatedBy(AuthContext.getStaffId());
    documentMapper.insert(doc);
    return Result.ok(doc);
  }

  @PutMapping("/{id}")
  public Result<OaDocument> update(@PathVariable Long id, @Valid @RequestBody OaDocumentRequest request) {
    OaDocument doc = documentMapper.selectById(id);
    if (doc == null) {
      return Result.ok(null);
    }
    doc.setName(request.getName());
    doc.setFolder(request.getFolder());
    doc.setUrl(request.getUrl());
    doc.setSizeBytes(request.getSizeBytes());
    doc.setUploaderId(request.getUploaderId());
    doc.setUploaderName(request.getUploaderName());
    doc.setUploadedAt(request.getUploadedAt());
    doc.setRemark(request.getRemark());
    documentMapper.updateById(doc);
    return Result.ok(doc);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaDocument doc = documentMapper.selectById(id);
    if (doc != null) {
      doc.setIsDeleted(1);
      documentMapper.updateById(doc);
    }
    return Result.ok(null);
  }
}
