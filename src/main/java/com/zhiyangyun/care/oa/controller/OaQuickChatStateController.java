package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaQuickChatState;
import com.zhiyangyun.care.oa.mapper.OaQuickChatStateMapper;
import com.zhiyangyun.care.oa.model.OaQuickChatStateRequest;
import com.zhiyangyun.care.oa.model.OaQuickChatStateResponse;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oa/quick-chat/state")
@PreAuthorize("isAuthenticated() and !hasRole('FAMILY')")
public class OaQuickChatStateController {
  private static final int MAX_STATE_BYTES = 2 * 1024 * 1024;
  private final OaQuickChatStateMapper quickChatStateMapper;

  public OaQuickChatStateController(OaQuickChatStateMapper quickChatStateMapper) {
    this.quickChatStateMapper = quickChatStateMapper;
  }

  @GetMapping
  public Result<OaQuickChatStateResponse> getCurrentState() {
    OaQuickChatState state = findCurrentState();
    if (state == null) {
      return Result.ok(new OaQuickChatStateResponse());
    }
    return Result.ok(toResponse(state));
  }

  @PutMapping
  public Result<OaQuickChatStateResponse> saveCurrentState(@Valid @RequestBody OaQuickChatStateRequest request) {
    String stateJson = request.getStateJson();
    if (stateJson != null && stateJson.getBytes(StandardCharsets.UTF_8).length > MAX_STATE_BYTES) {
      throw new IllegalArgumentException("聊天同步数据过大，请减少历史记录后重试");
    }
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    OaQuickChatState state = findCurrentState();
    if (state == null) {
      state = new OaQuickChatState();
      state.setTenantId(orgId);
      state.setOrgId(orgId);
      state.setStaffId(staffId);
      state.setCreatedBy(staffId);
      state.setStateJson(stateJson);
      quickChatStateMapper.insert(state);
    } else {
      state.setStateJson(stateJson);
      quickChatStateMapper.updateById(state);
    }
    OaQuickChatState latest = quickChatStateMapper.selectById(state.getId());
    return Result.ok(toResponse(latest == null ? state : latest));
  }

  private OaQuickChatState findCurrentState() {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    return quickChatStateMapper.selectOne(Wrappers.lambdaQuery(OaQuickChatState.class)
        .eq(OaQuickChatState::getIsDeleted, 0)
        .eq(orgId != null, OaQuickChatState::getOrgId, orgId)
        .eq(staffId != null, OaQuickChatState::getStaffId, staffId)
        .last("limit 1"));
  }

  private OaQuickChatStateResponse toResponse(OaQuickChatState state) {
    OaQuickChatStateResponse response = new OaQuickChatStateResponse();
    response.setId(state.getId());
    response.setOrgId(state.getOrgId());
    response.setStaffId(state.getStaffId());
    response.setStateJson(state.getStateJson());
    response.setUpdateTime(state.getUpdateTime());
    return response;
  }
}
