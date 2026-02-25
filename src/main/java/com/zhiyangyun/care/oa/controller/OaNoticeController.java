package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaNotice;
import com.zhiyangyun.care.oa.mapper.OaNoticeMapper;
import com.zhiyangyun.care.oa.model.OaBatchIdsRequest;
import com.zhiyangyun.care.oa.model.OaNoticeRequest;
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
@RequestMapping("/api/oa/notice")
public class OaNoticeController {
  private final OaNoticeMapper noticeMapper;

  public OaNoticeController(OaNoticeMapper noticeMapper) {
    this.noticeMapper = noticeMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaNotice>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = Wrappers.lambdaQuery(OaNotice.class)
        .eq(OaNotice::getIsDeleted, 0)
        .eq(orgId != null, OaNotice::getOrgId, orgId)
        .eq(normalizedStatus != null, OaNotice::getStatus, normalizedStatus);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaNotice::getTitle, keyword)
          .or().like(OaNotice::getContent, keyword));
    }
    wrapper.orderByDesc(OaNotice::getPublishTime);
    return Result.ok(noticeMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/{id}")
  public Result<OaNotice> get(@PathVariable Long id) {
    return Result.ok(findAccessibleNotice(id));
  }

  @PostMapping
  public Result<OaNotice> create(@Valid @RequestBody OaNoticeRequest request) {
    Long orgId = AuthContext.getOrgId();
    OaNotice notice = new OaNotice();
    notice.setTenantId(orgId);
    notice.setOrgId(orgId);
    notice.setTitle(request.getTitle());
    notice.setContent(request.getContent());
    notice.setPublisherName(AuthContext.getUsername());
    String normalizedStatus = normalizeStatus(request.getStatus());
    notice.setStatus(normalizedStatus == null ? "DRAFT" : normalizedStatus);
    notice.setPublishTime(request.getPublishTime());
    notice.setCreatedBy(AuthContext.getStaffId());
    if ("PUBLISHED".equals(notice.getStatus()) && notice.getPublishTime() == null) {
      notice.setPublishTime(LocalDateTime.now());
    }
    if ("DRAFT".equals(notice.getStatus()) && notice.getPublishTime() != null) {
      throw new IllegalArgumentException("草稿公告不应设置发布时间");
    }
    noticeMapper.insert(notice);
    return Result.ok(notice);
  }

  @PutMapping("/{id}")
  public Result<OaNotice> update(@PathVariable Long id, @Valid @RequestBody OaNoticeRequest request) {
    OaNotice notice = findAccessibleNotice(id);
    if (notice == null) {
      return Result.ok(null);
    }
    if (!"DRAFT".equals(notice.getStatus())) {
      throw new IllegalArgumentException("仅草稿状态可编辑");
    }
    notice.setTitle(request.getTitle());
    notice.setContent(request.getContent());
    String normalizedStatus = normalizeStatus(request.getStatus());
    if (normalizedStatus != null && !"DRAFT".equals(normalizedStatus)) {
      throw new IllegalArgumentException("编辑公告仅支持保持 DRAFT 状态");
    }
    notice.setStatus("DRAFT");
    notice.setPublishTime(request.getPublishTime());
    if (notice.getPublishTime() != null) {
      throw new IllegalArgumentException("草稿公告不应设置发布时间");
    }
    noticeMapper.updateById(notice);
    return Result.ok(notice);
  }

  @PutMapping("/{id}/publish")
  public Result<OaNotice> publish(@PathVariable Long id) {
    OaNotice notice = findAccessibleNotice(id);
    if (notice == null) {
      return Result.ok(null);
    }
    if ("PUBLISHED".equals(notice.getStatus())) {
      throw new IllegalArgumentException("公告已发布，不能重复发布");
    }
    if (!"DRAFT".equals(notice.getStatus())) {
      throw new IllegalArgumentException("仅草稿状态可发布");
    }
    notice.setStatus("PUBLISHED");
    notice.setPublishTime(LocalDateTime.now());
    noticeMapper.updateById(notice);
    return Result.ok(notice);
  }

  @PutMapping("/batch/publish")
  public Result<Integer> batchPublish(@RequestBody OaBatchIdsRequest request) {
    List<Long> ids = sanitizeIds(request == null ? null : request.getIds());
    if (ids.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    List<OaNotice> notices = noticeMapper.selectList(Wrappers.lambdaQuery(OaNotice.class)
        .eq(OaNotice::getIsDeleted, 0)
        .eq(orgId != null, OaNotice::getOrgId, orgId)
        .in(OaNotice::getId, ids));
    int count = 0;
    for (OaNotice notice : notices) {
      if (!"DRAFT".equals(notice.getStatus())) {
        continue;
      }
      notice.setStatus("PUBLISHED");
      notice.setPublishTime(LocalDateTime.now());
      noticeMapper.updateById(notice);
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
    List<OaNotice> notices = noticeMapper.selectList(Wrappers.lambdaQuery(OaNotice.class)
        .eq(OaNotice::getIsDeleted, 0)
        .eq(orgId != null, OaNotice::getOrgId, orgId)
        .in(OaNotice::getId, ids));
    for (OaNotice notice : notices) {
      notice.setIsDeleted(1);
      noticeMapper.updateById(notice);
    }
    return Result.ok(notices.size());
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    var wrapper = Wrappers.lambdaQuery(OaNotice.class)
        .eq(OaNotice::getIsDeleted, 0)
        .eq(orgId != null, OaNotice::getOrgId, orgId)
        .eq(normalizedStatus != null, OaNotice::getStatus, normalizedStatus)
        .orderByDesc(OaNotice::getPublishTime)
        .orderByDesc(OaNotice::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaNotice::getTitle, keyword).or().like(OaNotice::getContent, keyword));
    }
    List<OaNotice> notices = noticeMapper.selectList(wrapper);
    List<String> headers = List.of("ID", "标题", "状态", "发布人", "发布时间", "内容");
    List<List<String>> rows = notices.stream()
        .map(item -> List.of(
            safe(item.getId()),
            safe(item.getTitle()),
            safe(item.getStatus()),
            safe(item.getPublisherName()),
            formatDateTime(item.getPublishTime()),
            safe(item.getContent())))
        .toList();
    return csvResponse("oa-notice", headers, rows);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaNotice notice = findAccessibleNotice(id);
    if (notice != null) {
      notice.setIsDeleted(1);
      noticeMapper.updateById(notice);
    }
    return Result.ok(null);
  }

  private OaNotice findAccessibleNotice(Long id) {
    Long orgId = AuthContext.getOrgId();
    return noticeMapper.selectOne(Wrappers.lambdaQuery(OaNotice.class)
        .eq(OaNotice::getId, id)
        .eq(OaNotice::getIsDeleted, 0)
        .eq(orgId != null, OaNotice::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase();
    if (!"DRAFT".equals(normalized) && !"PUBLISHED".equals(normalized)) {
      throw new IllegalArgumentException("status 仅支持 DRAFT/PUBLISHED");
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
