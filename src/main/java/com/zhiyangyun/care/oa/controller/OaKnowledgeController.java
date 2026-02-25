package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaKnowledge;
import com.zhiyangyun.care.oa.mapper.OaKnowledgeMapper;
import com.zhiyangyun.care.oa.model.OaBatchIdsRequest;
import com.zhiyangyun.care.oa.model.OaKnowledgeRequest;
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
@RequestMapping("/api/oa/knowledge")
public class OaKnowledgeController {
  private final OaKnowledgeMapper knowledgeMapper;

  public OaKnowledgeController(OaKnowledgeMapper knowledgeMapper) {
    this.knowledgeMapper = knowledgeMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaKnowledge>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = Wrappers.lambdaQuery(OaKnowledge.class)
        .eq(OaKnowledge::getIsDeleted, 0)
        .eq(orgId != null, OaKnowledge::getOrgId, orgId)
        .eq(category != null && !category.isBlank(), OaKnowledge::getCategory, category)
        .eq(normalizedStatus != null, OaKnowledge::getStatus, normalizedStatus);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaKnowledge::getTitle, keyword)
          .or().like(OaKnowledge::getTags, keyword)
          .or().like(OaKnowledge::getAuthorName, keyword));
    }
    wrapper.orderByDesc(OaKnowledge::getPublishedAt).orderByDesc(OaKnowledge::getCreateTime);
    return Result.ok(knowledgeMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<OaKnowledge> create(@Valid @RequestBody OaKnowledgeRequest request) {
    Long orgId = AuthContext.getOrgId();
    OaKnowledge knowledge = new OaKnowledge();
    knowledge.setTenantId(orgId);
    knowledge.setOrgId(orgId);
    knowledge.setTitle(request.getTitle());
    knowledge.setCategory(request.getCategory());
    knowledge.setTags(request.getTags());
    knowledge.setContent(request.getContent());
    knowledge.setAuthorId(request.getAuthorId() == null ? AuthContext.getStaffId() : request.getAuthorId());
    knowledge.setAuthorName(
        request.getAuthorName() == null || request.getAuthorName().isBlank()
            ? AuthContext.getUsername()
            : request.getAuthorName());
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null && "ARCHIVED".equals(normalizedStatus)) {
      throw new IllegalArgumentException("创建知识库不支持 ARCHIVED 状态");
    }
    knowledge.setStatus(normalizedStatus == null ? "DRAFT" : normalizedStatus);
    knowledge.setPublishedAt(
        "PUBLISHED".equalsIgnoreCase(knowledge.getStatus()) ? LocalDateTime.now() : null);
    knowledge.setRemark(request.getRemark());
    knowledge.setCreatedBy(AuthContext.getStaffId());
    knowledgeMapper.insert(knowledge);
    return Result.ok(knowledge);
  }

  @PutMapping("/{id}")
  public Result<OaKnowledge> update(@PathVariable Long id, @Valid @RequestBody OaKnowledgeRequest request) {
    OaKnowledge knowledge = findAccessibleKnowledge(id);
    if (knowledge == null) {
      return Result.ok(null);
    }
    if ("ARCHIVED".equals(knowledge.getStatus())) {
      throw new IllegalArgumentException("已归档知识不可编辑");
    }
    knowledge.setTitle(request.getTitle());
    knowledge.setCategory(request.getCategory());
    knowledge.setTags(request.getTags());
    knowledge.setContent(request.getContent());
    knowledge.setAuthorId(request.getAuthorId());
    knowledge.setAuthorName(request.getAuthorName());
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null) {
      ensureStatusTransition(knowledge.getStatus(), normalizedStatus);
      knowledge.setStatus(normalizedStatus);
    }
    if ("PUBLISHED".equals(knowledge.getStatus()) && knowledge.getPublishedAt() == null) {
      knowledge.setPublishedAt(LocalDateTime.now());
    }
    knowledge.setRemark(request.getRemark());
    knowledgeMapper.updateById(knowledge);
    return Result.ok(knowledge);
  }

  @PutMapping("/{id}/publish")
  public Result<OaKnowledge> publish(@PathVariable Long id) {
    OaKnowledge knowledge = findAccessibleKnowledge(id);
    if (knowledge == null) {
      return Result.ok(null);
    }
    ensureStatusTransition(knowledge.getStatus(), "PUBLISHED");
    knowledge.setStatus("PUBLISHED");
    if (knowledge.getPublishedAt() == null) {
      knowledge.setPublishedAt(LocalDateTime.now());
    }
    knowledgeMapper.updateById(knowledge);
    return Result.ok(knowledge);
  }

  @PutMapping("/{id}/archive")
  public Result<OaKnowledge> archive(@PathVariable Long id) {
    OaKnowledge knowledge = findAccessibleKnowledge(id);
    if (knowledge == null) {
      return Result.ok(null);
    }
    ensureStatusTransition(knowledge.getStatus(), "ARCHIVED");
    knowledge.setStatus("ARCHIVED");
    knowledgeMapper.updateById(knowledge);
    return Result.ok(knowledge);
  }

  @PutMapping("/batch/publish")
  public Result<Integer> batchPublish(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaKnowledge> list = knowledgeMapper.selectList(Wrappers.lambdaQuery(OaKnowledge.class)
        .eq(OaKnowledge::getIsDeleted, 0)
        .eq(orgId != null, OaKnowledge::getOrgId, orgId)
        .in(OaKnowledge::getId, ids));
    int count = 0;
    for (OaKnowledge item : list) {
      if (!"DRAFT".equals(item.getStatus())) {
        continue;
      }
      item.setStatus("PUBLISHED");
      if (item.getPublishedAt() == null) {
        item.setPublishedAt(LocalDateTime.now());
      }
      knowledgeMapper.updateById(item);
      count++;
    }
    return Result.ok(count);
  }

  @PutMapping("/batch/archive")
  public Result<Integer> batchArchive(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaKnowledge> list = knowledgeMapper.selectList(Wrappers.lambdaQuery(OaKnowledge.class)
        .eq(OaKnowledge::getIsDeleted, 0)
        .eq(orgId != null, OaKnowledge::getOrgId, orgId)
        .in(OaKnowledge::getId, ids));
    int count = 0;
    for (OaKnowledge item : list) {
      if ("ARCHIVED".equals(item.getStatus())) {
        continue;
      }
      item.setStatus("ARCHIVED");
      knowledgeMapper.updateById(item);
      count++;
    }
    return Result.ok(count);
  }

  @DeleteMapping("/batch")
  public Result<Integer> batchDelete(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaKnowledge> list = knowledgeMapper.selectList(Wrappers.lambdaQuery(OaKnowledge.class)
        .eq(OaKnowledge::getIsDeleted, 0)
        .eq(orgId != null, OaKnowledge::getOrgId, orgId)
        .in(OaKnowledge::getId, ids));
    for (OaKnowledge item : list) {
      item.setIsDeleted(1);
      knowledgeMapper.updateById(item);
    }
    return Result.ok(list.size());
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = Wrappers.lambdaQuery(OaKnowledge.class)
        .eq(OaKnowledge::getIsDeleted, 0)
        .eq(orgId != null, OaKnowledge::getOrgId, orgId)
        .eq(category != null && !category.isBlank(), OaKnowledge::getCategory, category)
        .eq(normalizedStatus != null, OaKnowledge::getStatus, normalizedStatus)
        .orderByDesc(OaKnowledge::getPublishedAt)
        .orderByDesc(OaKnowledge::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaKnowledge::getTitle, keyword)
          .or().like(OaKnowledge::getTags, keyword)
          .or().like(OaKnowledge::getAuthorName, keyword));
    }
    List<OaKnowledge> list = knowledgeMapper.selectList(wrapper);
    List<String> headers = List.of("ID", "标题", "分类", "标签", "作者", "状态", "发布时间", "备注");
    List<List<String>> rows = list.stream()
        .map(item -> List.of(
            safe(item.getId()),
            safe(item.getTitle()),
            safe(item.getCategory()),
            safe(item.getTags()),
            safe(item.getAuthorName()),
            safe(item.getStatus()),
            formatDateTime(item.getPublishedAt()),
            safe(item.getRemark())))
        .toList();
    return csvResponse("oa-knowledge", headers, rows);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaKnowledge knowledge = findAccessibleKnowledge(id);
    if (knowledge != null) {
      knowledge.setIsDeleted(1);
      knowledgeMapper.updateById(knowledge);
    }
    return Result.ok(null);
  }

  private OaKnowledge findAccessibleKnowledge(Long id) {
    Long orgId = AuthContext.getOrgId();
    return knowledgeMapper.selectOne(Wrappers.lambdaQuery(OaKnowledge.class)
        .eq(OaKnowledge::getId, id)
        .eq(OaKnowledge::getIsDeleted, 0)
        .eq(orgId != null, OaKnowledge::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private void ensureStatusTransition(String fromStatus, String toStatus) {
    if (toStatus == null || fromStatus == null || toStatus.equals(fromStatus)) {
      return;
    }
    if ("DRAFT".equals(fromStatus) && ("PUBLISHED".equals(toStatus) || "ARCHIVED".equals(toStatus))) {
      return;
    }
    if ("PUBLISHED".equals(fromStatus) && "ARCHIVED".equals(toStatus)) {
      return;
    }
    throw new IllegalArgumentException("知识状态不允许从 " + fromStatus + " 变更为 " + toStatus);
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase();
    if (!"DRAFT".equals(normalized) && !"PUBLISHED".equals(normalized) && !"ARCHIVED".equals(normalized)) {
      throw new IllegalArgumentException("status 仅支持 DRAFT/PUBLISHED/ARCHIVED");
    }
    return normalized;
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
