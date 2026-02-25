package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaDocument;
import com.zhiyangyun.care.oa.mapper.OaDocumentMapper;
import com.zhiyangyun.care.oa.model.OaBatchIdsRequest;
import com.zhiyangyun.care.oa.model.OaDocumentRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    validateSize(request.getSizeBytes());
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
    OaDocument doc = findAccessibleDocument(id);
    if (doc == null) {
      return Result.ok(null);
    }
    validateSize(request.getSizeBytes());
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

  @DeleteMapping("/batch")
  public Result<Integer> batchDelete(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaDocument> docs = documentMapper.selectList(Wrappers.lambdaQuery(OaDocument.class)
        .eq(OaDocument::getIsDeleted, 0)
        .eq(orgId != null, OaDocument::getOrgId, orgId)
        .in(OaDocument::getId, ids));
    for (OaDocument doc : docs) {
      doc.setIsDeleted(1);
      documentMapper.updateById(doc);
    }
    return Result.ok(docs.size());
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String folder,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaDocument.class)
        .eq(OaDocument::getIsDeleted, 0)
        .eq(orgId != null, OaDocument::getOrgId, orgId)
        .eq(folder != null && !folder.isBlank(), OaDocument::getFolder, folder)
        .orderByDesc(OaDocument::getUploadedAt);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaDocument::getName, keyword).or().like(OaDocument::getUploaderName, keyword));
    }
    List<OaDocument> docs = documentMapper.selectList(wrapper);
    List<String> headers = List.of("ID", "文件名", "目录", "URL", "大小(B)", "上传人", "上传时间", "备注");
    List<List<String>> rows = docs.stream()
        .map(item -> List.of(
            safe(item.getId()),
            safe(item.getName()),
            safe(item.getFolder()),
            safe(item.getUrl()),
            safe(item.getSizeBytes()),
            safe(item.getUploaderName()),
            formatDateTime(item.getUploadedAt()),
            safe(item.getRemark())))
        .toList();
    return csvResponse("oa-document", headers, rows);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaDocument doc = findAccessibleDocument(id);
    if (doc != null) {
      doc.setIsDeleted(1);
      documentMapper.updateById(doc);
    }
    return Result.ok(null);
  }

  private OaDocument findAccessibleDocument(Long id) {
    Long orgId = AuthContext.getOrgId();
    return documentMapper.selectOne(Wrappers.lambdaQuery(OaDocument.class)
        .eq(OaDocument::getId, id)
        .eq(OaDocument::getIsDeleted, 0)
        .eq(orgId != null, OaDocument::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private void validateSize(Long sizeBytes) {
    if (sizeBytes != null && sizeBytes < 0) {
      throw new IllegalArgumentException("sizeBytes 不能小于 0");
    }
  }

  private List<Long> sanitizeIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return ids.stream().filter(id -> id != null && id > 0).distinct().collect(Collectors.toList());
  }

  private String safe(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private String formatDateTime(LocalDateTime value) {
    if (value == null) {
      return "";
    }
    return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  private ResponseEntity<byte[]> csvResponse(String filenameBase, List<String> headers, List<List<String>> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append('\uFEFF');
    sb.append(toCsvLine(headers)).append('\n');
    for (List<String> row : rows) {
      sb.append(toCsvLine(row)).append('\n');
    }
    byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
    String filename = filenameBase + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    HttpHeaders headersObj = new HttpHeaders();
    headersObj.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename + ".csv");
    headersObj.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
    headersObj.setContentLength(bytes.length);
    return ResponseEntity.ok().headers(headersObj).body(bytes);
  }

  private String toCsvLine(List<String> fields) {
    List<String> escaped = new ArrayList<>();
    for (String field : fields) {
      String value = field == null ? "" : field;
      value = value.replace("\"", "\"\"");
      if (value.contains(",") || value.contains("\n") || value.contains("\r") || value.contains("\"")) {
        value = "\"" + value + "\"";
      }
      escaped.add(value);
    }
    return String.join(",", escaped);
  }
}
