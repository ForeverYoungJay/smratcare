package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaNotice;
import com.zhiyangyun.care.oa.mapper.OaNoticeMapper;
import com.zhiyangyun.care.oa.model.OaNoticeRequest;
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
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaNotice.class)
        .eq(OaNotice::getIsDeleted, 0)
        .eq(orgId != null, OaNotice::getOrgId, orgId);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(OaNotice::getTitle, keyword)
          .or().like(OaNotice::getContent, keyword));
    }
    wrapper.orderByDesc(OaNotice::getPublishTime);
    return Result.ok(noticeMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/{id}")
  public Result<OaNotice> get(@PathVariable Long id) {
    return Result.ok(noticeMapper.selectById(id));
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
    notice.setStatus(request.getStatus() == null ? "DRAFT" : request.getStatus());
    notice.setPublishTime(request.getPublishTime());
    notice.setCreatedBy(AuthContext.getStaffId());
    noticeMapper.insert(notice);
    return Result.ok(notice);
  }

  @PutMapping("/{id}")
  public Result<OaNotice> update(@PathVariable Long id, @Valid @RequestBody OaNoticeRequest request) {
    OaNotice notice = noticeMapper.selectById(id);
    if (notice == null) {
      return Result.ok(null);
    }
    notice.setTitle(request.getTitle());
    notice.setContent(request.getContent());
    notice.setStatus(request.getStatus());
    notice.setPublishTime(request.getPublishTime());
    noticeMapper.updateById(notice);
    return Result.ok(notice);
  }

  @PutMapping("/{id}/publish")
  public Result<OaNotice> publish(@PathVariable Long id) {
    OaNotice notice = noticeMapper.selectById(id);
    if (notice == null) {
      return Result.ok(null);
    }
    notice.setStatus("PUBLISHED");
    notice.setPublishTime(LocalDateTime.now());
    noticeMapper.updateById(notice);
    return Result.ok(notice);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaNotice notice = noticeMapper.selectById(id);
    if (notice != null) {
      notice.setIsDeleted(1);
      noticeMapper.updateById(notice);
    }
    return Result.ok(null);
  }
}
