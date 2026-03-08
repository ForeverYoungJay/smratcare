package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaQuickChatState;
import com.zhiyangyun.care.oa.mapper.OaQuickChatStateMapper;
import com.zhiyangyun.care.oa.model.OaQuickChatFanoutRequest;
import com.zhiyangyun.care.oa.model.OaQuickChatStateRequest;
import com.zhiyangyun.care.oa.model.OaQuickChatStateResponse;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oa/quick-chat/state")
@PreAuthorize("isAuthenticated() and !hasRole('FAMILY')")
public class OaQuickChatStateController {
  private static final int MAX_STATE_BYTES = 2 * 1024 * 1024;
  private static final TypeReference<List<Map<String, Object>>> ROOM_LIST_TYPE = new TypeReference<>() {};
  private final OaQuickChatStateMapper quickChatStateMapper;
  private final StaffMapper staffMapper;
  private final ObjectMapper objectMapper;

  public OaQuickChatStateController(
      OaQuickChatStateMapper quickChatStateMapper,
      StaffMapper staffMapper,
      ObjectMapper objectMapper) {
    this.quickChatStateMapper = quickChatStateMapper;
    this.staffMapper = staffMapper;
    this.objectMapper = objectMapper;
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

  @PostMapping("/fanout")
  public Result<Integer> fanoutState(@Valid @RequestBody OaQuickChatFanoutRequest request) {
    String stateJson = request.getStateJson();
    if (stateJson != null && stateJson.getBytes(StandardCharsets.UTF_8).length > MAX_STATE_BYTES) {
      throw new IllegalArgumentException("聊天同步数据过大，请减少历史记录后重试");
    }
    List<Map<String, Object>> sourceRooms = parseRooms(stateJson);
    if (sourceRooms.isEmpty()) {
      return Result.ok(0);
    }
    Long orgId = AuthContext.getOrgId();
    Long operatorId = AuthContext.getStaffId();
    List<Long> targetIds = resolveTargetStaffIds(orgId, request.getTargetStaffIds());
    if (targetIds.isEmpty()) {
      return Result.ok(0);
    }
    int affected = 0;
    for (Long targetStaffId : targetIds) {
      List<Map<String, Object>> visibleRooms = filterRoomsByMember(sourceRooms, targetStaffId);
      if (visibleRooms.isEmpty()) {
        continue;
      }
      OaQuickChatState row = quickChatStateMapper.selectOne(Wrappers.lambdaQuery(OaQuickChatState.class)
          .eq(OaQuickChatState::getIsDeleted, 0)
          .eq(orgId != null, OaQuickChatState::getOrgId, orgId)
          .eq(OaQuickChatState::getStaffId, targetStaffId)
          .last("limit 1"));
      String mergedJson = visibleRoomsToJson(visibleRooms);
      if (mergedJson.getBytes(StandardCharsets.UTF_8).length > MAX_STATE_BYTES) {
        continue;
      }
      if (row == null) {
        row = new OaQuickChatState();
        row.setTenantId(orgId);
        row.setOrgId(orgId);
        row.setStaffId(targetStaffId);
        row.setCreatedBy(operatorId);
        row.setStateJson(mergedJson);
        quickChatStateMapper.insert(row);
      } else {
        List<Map<String, Object>> currentRooms = parseRooms(row.getStateJson());
        List<Map<String, Object>> mergedRooms = mergeRooms(currentRooms, visibleRooms);
        String finalJson = visibleRoomsToJson(mergedRooms);
        if (finalJson.getBytes(StandardCharsets.UTF_8).length > MAX_STATE_BYTES) {
          continue;
        }
        row.setStateJson(finalJson);
        quickChatStateMapper.updateById(row);
      }
      affected++;
    }
    return Result.ok(affected);
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

  private List<Long> resolveTargetStaffIds(Long orgId, List<Long> targetStaffIds) {
    if (targetStaffIds == null || targetStaffIds.isEmpty()) {
      return List.of();
    }
    Set<Long> idSet = new LinkedHashSet<>();
    for (Long id : targetStaffIds) {
      if (id != null && id > 0) {
        idSet.add(id);
      }
    }
    if (idSet.isEmpty()) {
      return List.of();
    }
    List<StaffAccount> staffs = staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId)
        .in(StaffAccount::getId, idSet));
    return staffs.stream()
        .map(StaffAccount::getId)
        .filter(id -> id != null && id > 0)
        .distinct()
        .toList();
  }

  private List<Map<String, Object>> parseRooms(String stateJson) {
    if (stateJson == null || stateJson.isBlank()) {
      return List.of();
    }
    try {
      List<Map<String, Object>> rooms = objectMapper.readValue(stateJson, ROOM_LIST_TYPE);
      if (rooms == null) {
        return List.of();
      }
      return rooms.stream().filter(item -> item != null && !item.isEmpty()).toList();
    } catch (Exception ex) {
      return List.of();
    }
  }

  private String visibleRoomsToJson(List<Map<String, Object>> rooms) {
    try {
      return objectMapper.writeValueAsString(rooms == null ? List.of() : rooms);
    } catch (Exception ex) {
      return "[]";
    }
  }

  private List<Map<String, Object>> filterRoomsByMember(List<Map<String, Object>> rooms, Long staffId) {
    if (rooms == null || rooms.isEmpty() || staffId == null) {
      return List.of();
    }
    String staffIdText = String.valueOf(staffId);
    List<Map<String, Object>> visible = new ArrayList<>();
    for (Map<String, Object> room : rooms) {
      Object rawMembers = room.get("memberIds");
      if (!(rawMembers instanceof List<?> memberList)) {
        continue;
      }
      boolean hit = memberList.stream()
          .map(String::valueOf)
          .anyMatch(staffIdText::equals);
      if (hit) {
        visible.add(room);
      }
    }
    return visible;
  }

  private List<Map<String, Object>> mergeRooms(
      List<Map<String, Object>> currentRooms,
      List<Map<String, Object>> incomingRooms) {
    Map<String, Map<String, Object>> merged = new LinkedHashMap<>();
    for (Map<String, Object> room : currentRooms == null ? List.<Map<String, Object>>of() : currentRooms) {
      String roomId = asText(room.get("id"));
      if (roomId.isBlank()) continue;
      merged.put(roomId, room);
    }
    for (Map<String, Object> room : incomingRooms == null ? List.<Map<String, Object>>of() : incomingRooms) {
      String roomId = asText(room.get("id"));
      if (roomId.isBlank()) continue;
      Map<String, Object> existed = merged.get(roomId);
      if (existed == null) {
        merged.put(roomId, room);
        continue;
      }
      long existedTs = roomUpdatedAtMs(existed);
      long incomingTs = roomUpdatedAtMs(room);
      if (incomingTs >= existedTs) {
        merged.put(roomId, room);
      }
    }
    List<Map<String, Object>> rows = new ArrayList<>(merged.values());
    rows.sort(Comparator.comparingLong(this::roomUpdatedAtMs).reversed());
    return rows;
  }

  private long roomUpdatedAtMs(Map<String, Object> room) {
    if (room == null) return 0L;
    String updatedAt = asText(room.get("updatedAt"));
    if (updatedAt.isBlank()) {
      updatedAt = asText(room.get("createdAt"));
    }
    try {
      return java.time.Instant.parse(updatedAt).toEpochMilli();
    } catch (Exception ignored) {}
    try {
      return java.time.LocalDateTime.parse(updatedAt).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    } catch (Exception ignored) {}
    return 0L;
  }

  private String asText(Object value) {
    return value == null ? "" : String.valueOf(value).trim();
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
