package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaSuggestion;
import com.zhiyangyun.care.oa.mapper.OaSuggestionMapper;
import com.zhiyangyun.care.oa.model.OaSuggestionRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oa/suggestion")
public class OaSuggestionController {
  private final OaSuggestionMapper suggestionMapper;

  public OaSuggestionController(OaSuggestionMapper suggestionMapper) {
    this.suggestionMapper = suggestionMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaSuggestion>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "10") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "false") boolean familyCommunicationOnly) {
    Long orgId = AuthContext.getOrgId();
    String normalizedStatus = normalizeStatus(status);
    String normalizedKeyword = keyword == null ? null : keyword.trim();
    var wrapper = Wrappers.lambdaQuery(OaSuggestion.class)
        .eq(OaSuggestion::getIsDeleted, 0)
        .eq(orgId != null, OaSuggestion::getOrgId, orgId)
        .eq(normalizedStatus != null, OaSuggestion::getStatus, normalizedStatus)
        .like(familyCommunicationOnly, OaSuggestion::getContent, "[家属沟通]")
        .and(normalizedKeyword != null && !normalizedKeyword.isBlank(), nested -> nested
            .like(OaSuggestion::getContent, normalizedKeyword)
            .or()
            .like(OaSuggestion::getProposerName, normalizedKeyword)
            .or()
            .like(OaSuggestion::getContact, normalizedKeyword))
        .orderByDesc(OaSuggestion::getCreateTime);
    return Result.ok(suggestionMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<OaSuggestion> create(@Valid @RequestBody OaSuggestionRequest request) {
    Long orgId = AuthContext.getOrgId();
    OaSuggestion suggestion = new OaSuggestion();
    suggestion.setTenantId(orgId);
    suggestion.setOrgId(orgId);
    suggestion.setContent(request.getContent() == null ? null : request.getContent().trim());
    suggestion.setProposerName(blankToNull(request.getProposerName()));
    suggestion.setContact(blankToNull(request.getContact()));
    suggestion.setStatus("PENDING");
    suggestion.setCreatedBy(AuthContext.getStaffId());
    suggestionMapper.insert(suggestion);
    return Result.ok(suggestion);
  }

  @PutMapping("/{id}/status")
  public Result<OaSuggestion> updateStatus(@PathVariable Long id, @RequestParam String status) {
    OaSuggestion suggestion = findAccessible(id);
    if (suggestion == null) {
      return Result.ok(null);
    }
    suggestion.setStatus(normalizeStatus(status));
    suggestionMapper.updateById(suggestion);
    return Result.ok(suggestion);
  }

  private OaSuggestion findAccessible(Long id) {
    Long orgId = AuthContext.getOrgId();
    return suggestionMapper.selectOne(Wrappers.lambdaQuery(OaSuggestion.class)
        .eq(OaSuggestion::getId, id)
        .eq(OaSuggestion::getIsDeleted, 0)
        .eq(orgId != null, OaSuggestion::getOrgId, orgId)
        .last("LIMIT 1"));
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    String normalized = status.trim().toUpperCase();
    if ("ADOPTED".equals(normalized)) {
      return "DONE";
    }
    if ("REJECTED".equals(normalized)) {
      return "CLOSED";
    }
    if (!"PENDING".equals(normalized) && !"PROCESSING".equals(normalized)
        && !"DONE".equals(normalized) && !"CLOSED".equals(normalized)) {
      throw new IllegalArgumentException("status 仅支持 PENDING/PROCESSING/DONE/CLOSED");
    }
    return normalized;
  }

  private String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
