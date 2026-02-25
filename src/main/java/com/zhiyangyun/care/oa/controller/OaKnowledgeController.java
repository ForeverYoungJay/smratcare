package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaKnowledge;
import com.zhiyangyun.care.oa.mapper.OaKnowledgeMapper;
import com.zhiyangyun.care.oa.model.OaKnowledgeRequest;
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
    var wrapper = Wrappers.lambdaQuery(OaKnowledge.class)
        .eq(OaKnowledge::getIsDeleted, 0)
        .eq(orgId != null, OaKnowledge::getOrgId, orgId)
        .eq(category != null && !category.isBlank(), OaKnowledge::getCategory, category)
        .eq(status != null && !status.isBlank(), OaKnowledge::getStatus, status);
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
    knowledge.setStatus(request.getStatus() == null ? "DRAFT" : request.getStatus());
    knowledge.setPublishedAt(
        "PUBLISHED".equalsIgnoreCase(knowledge.getStatus()) ? LocalDateTime.now() : null);
    knowledge.setRemark(request.getRemark());
    knowledge.setCreatedBy(AuthContext.getStaffId());
    knowledgeMapper.insert(knowledge);
    return Result.ok(knowledge);
  }

  @PutMapping("/{id}")
  public Result<OaKnowledge> update(@PathVariable Long id, @Valid @RequestBody OaKnowledgeRequest request) {
    OaKnowledge knowledge = knowledgeMapper.selectById(id);
    if (knowledge == null) {
      return Result.ok(null);
    }
    knowledge.setTitle(request.getTitle());
    knowledge.setCategory(request.getCategory());
    knowledge.setTags(request.getTags());
    knowledge.setContent(request.getContent());
    knowledge.setAuthorId(request.getAuthorId());
    knowledge.setAuthorName(request.getAuthorName());
    knowledge.setStatus(request.getStatus());
    if ("PUBLISHED".equalsIgnoreCase(request.getStatus()) && knowledge.getPublishedAt() == null) {
      knowledge.setPublishedAt(LocalDateTime.now());
    }
    knowledge.setRemark(request.getRemark());
    knowledgeMapper.updateById(knowledge);
    return Result.ok(knowledge);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaKnowledge knowledge = knowledgeMapper.selectById(id);
    if (knowledge != null) {
      knowledge.setIsDeleted(1);
      knowledgeMapper.updateById(knowledge);
    }
    return Result.ok(null);
  }
}
