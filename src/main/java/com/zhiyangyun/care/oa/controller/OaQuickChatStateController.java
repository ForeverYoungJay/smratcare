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
import com.zhiyangyun.care.oa.model.OaQuickChatEventBatchRequest;
import com.zhiyangyun.care.oa.model.OaQuickChatEventItemRequest;
import com.zhiyangyun.care.oa.model.OaQuickChatFanoutRequest;
import com.zhiyangyun.care.oa.model.OaQuickChatStateRequest;
import com.zhiyangyun.care.oa.model.OaQuickChatStateResponse;
import com.zhiyangyun.care.oa.realtime.OaQuickChatWebSocketHandler;
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
@PreAuthorize("isAuthenticated()")
public class OaQuickChatStateController {
  private static final int MAX_STATE_BYTES = 2 * 1024 * 1024;
  private static final TypeReference<List<Map<String, Object>>> ROOM_LIST_TYPE = new TypeReference<>() {};
  private final OaQuickChatStateMapper quickChatStateMapper;
  private final StaffMapper staffMapper;
  private final ObjectMapper objectMapper;
  private final OaQuickChatWebSocketHandler webSocketHandler;

  public OaQuickChatStateController(
      OaQuickChatStateMapper quickChatStateMapper,
      StaffMapper staffMapper,
      ObjectMapper objectMapper,
      OaQuickChatWebSocketHandler webSocketHandler) {
    this.quickChatStateMapper = quickChatStateMapper;
    this.staffMapper = staffMapper;
    this.objectMapper = objectMapper;
    this.webSocketHandler = webSocketHandler;
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
    List<StaffAccount> targets = resolveTargetStaffs(orgId, request.getTargetStaffIds());
    if (targets.isEmpty()) {
      return Result.ok(0);
    }
    int affected = 0;
    for (StaffAccount target : targets) {
      Long targetStaffId = target == null ? null : target.getId();
      if (targetStaffId == null || targetStaffId <= 0) continue;
      List<Map<String, Object>> visibleRooms = filterRoomsByMember(sourceRooms, target);
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
      webSocketHandler.sendToStaff(orgId, targetStaffId, Map.of(
          "eventType", "SYNC_HINT",
          "reason", "fanout",
          "at", System.currentTimeMillis()));
      affected++;
    }
    return Result.ok(affected);
  }

  @PostMapping("/event/batch")
  public Result<Integer> publishBatchEvents(@Valid @RequestBody OaQuickChatEventBatchRequest request) {
    Long orgId = AuthContext.getOrgId();
    int delivered = 0;
    for (OaQuickChatEventItemRequest item : request.getEvents()) {
      if (item == null) continue;
      List<StaffAccount> targets = resolveTargetStaffs(orgId, item.getTargetStaffIds());
      if (targets.isEmpty()) continue;
      Map<String, Object> payload = buildEventPayload(item);
      for (StaffAccount staff : targets) {
        Long staffId = staff == null ? null : staff.getId();
        if (staffId == null || staffId <= 0) continue;
        delivered += webSocketHandler.sendToStaff(orgId, staffId, payload);
      }
    }
    return Result.ok(delivered);
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

  private List<StaffAccount> resolveTargetStaffs(Long orgId, List<String> targetStaffIds) {
    if (targetStaffIds == null || targetStaffIds.isEmpty()) {
      return List.of();
    }
    Set<Long> idSet = new LinkedHashSet<>();
    Set<String> usernameSet = new LinkedHashSet<>();
    for (String raw : targetStaffIds) {
      String text = raw == null ? "" : raw.trim();
      if (text.isBlank()) continue;
      try {
        long parsed = Long.parseLong(text);
        if (parsed > 0) {
          idSet.add(parsed);
          continue;
        }
      } catch (NumberFormatException ignore) {
      }
      usernameSet.add(text);
    }
    if (idSet.isEmpty() && usernameSet.isEmpty()) {
      return List.of();
    }
    List<StaffAccount> staffs = staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
        .eq(StaffAccount::getIsDeleted, 0)
        .eq(orgId != null, StaffAccount::getOrgId, orgId));
    if (staffs == null || staffs.isEmpty()) {
      return List.of();
    }
    Set<Long> seen = new LinkedHashSet<>();
    List<StaffAccount> rows = new ArrayList<>();
    for (StaffAccount row : staffs) {
      if (row == null || row.getId() == null || row.getId() <= 0) continue;
      boolean matchedById = idSet.contains(row.getId());
      String username = row.getUsername() == null ? "" : row.getUsername().trim();
      boolean matchedByUsername = !username.isBlank() && usernameSet.contains(username);
      if (!matchedById && !matchedByUsername) continue;
      if (!seen.add(row.getId())) continue;
      rows.add(row);
    }
    return rows;
  }

  private List<Map<String, Object>> filterRoomsByMember(List<Map<String, Object>> rooms, StaffAccount staff) {
    if (rooms == null || rooms.isEmpty() || staff == null || staff.getId() == null) {
      return List.of();
    }
    String staffIdText = String.valueOf(staff.getId());
    String username = staff.getUsername() == null ? "" : staff.getUsername().trim();
    String staffNo = staff.getStaffNo() == null ? "" : staff.getStaffNo().trim();
    List<Map<String, Object>> visible = new ArrayList<>();
    for (Map<String, Object> room : rooms) {
      Object rawMembers = room.get("memberIds");
      if (!(rawMembers instanceof List<?> memberList)) {
        continue;
      }
      boolean hit = memberList.stream()
          .map(item -> item == null ? "" : String.valueOf(item).trim())
          .anyMatch(member -> member.equals(staffIdText)
              || (!username.isBlank() && member.equals(username))
              || (!staffNo.isBlank() && member.equals(staffNo)));
      if (hit) {
        visible.add(room);
      }
    }
    return visible;
  }

  private Map<String, Object> buildEventPayload(OaQuickChatEventItemRequest item) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("eventType", asText(item.getEventType()));
    if (item.getRoomId() != null && !item.getRoomId().isBlank()) {
      payload.put("roomId", item.getRoomId());
    }
    if (item.getRoom() != null && !item.getRoom().isEmpty()) {
      payload.put("room", item.getRoom());
    }
    if (item.getMessage() != null && !item.getMessage().isEmpty()) {
      payload.put("message", item.getMessage());
    }
    if (item.getMeta() != null && !item.getMeta().isEmpty()) {
      payload.put("meta", item.getMeta());
    }
    payload.put("at", System.currentTimeMillis());
    return payload;
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
