package com.zhiyangyun.care.oa.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.security.TokenBlacklistService;
import com.zhiyangyun.care.auth.security.TokenProvider;
import io.jsonwebtoken.Claims;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class OaQuickChatWebSocketHandler extends TextWebSocketHandler {
  private static final String ATTR_KEY = "quickChatUserKey";

  private final TokenProvider tokenProvider;
  private final TokenBlacklistService tokenBlacklistService;
  private final ObjectMapper objectMapper;
  private final ConcurrentMap<String, Set<WebSocketSession>> sessionsByUserKey = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, String> userKeyBySessionId = new ConcurrentHashMap<>();

  public OaQuickChatWebSocketHandler(
      TokenProvider tokenProvider,
      TokenBlacklistService tokenBlacklistService,
      ObjectMapper objectMapper) {
    this.tokenProvider = tokenProvider;
    this.tokenBlacklistService = tokenBlacklistService;
    this.objectMapper = objectMapper;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    String token = resolveToken(session.getUri());
    if (token == null || token.isBlank()) {
      session.close(CloseStatus.POLICY_VIOLATION.withReason("missing token"));
      return;
    }
    String userKey = resolveUserKey(token);
    if (userKey == null || userKey.isBlank()) {
      session.close(CloseStatus.POLICY_VIOLATION.withReason("invalid token"));
      return;
    }
    sessionsByUserKey.computeIfAbsent(userKey, key -> ConcurrentHashMap.newKeySet()).add(session);
    userKeyBySessionId.put(session.getId(), userKey);
    session.getAttributes().put(ATTR_KEY, userKey);
    sendRaw(session, Map.of(
        "eventType", "WS_READY",
        "at", System.currentTimeMillis()));
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    removeSession(session);
    if (session.isOpen()) {
      session.close(CloseStatus.SERVER_ERROR);
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    removeSession(session);
  }

  public int sendToStaff(Long orgId, Long staffId, Map<String, Object> payload) {
    if (orgId == null || staffId == null || payload == null || payload.isEmpty()) {
      return 0;
    }
    String userKey = buildUserKey(orgId, staffId);
    Set<WebSocketSession> sessions = sessionsByUserKey.get(userKey);
    if (sessions == null || sessions.isEmpty()) {
      return 0;
    }
    int delivered = 0;
    List<WebSocketSession> stale = new ArrayList<>();
    for (WebSocketSession session : sessions) {
      if (session == null || !session.isOpen()) {
        stale.add(session);
        continue;
      }
      try {
        sendRaw(session, payload);
        delivered++;
      } catch (Exception ex) {
        stale.add(session);
      }
    }
    if (!stale.isEmpty()) {
      sessions.removeAll(stale);
    }
    if (sessions.isEmpty()) {
      sessionsByUserKey.remove(userKey);
    }
    return delivered;
  }

  private void sendRaw(WebSocketSession session, Map<String, Object> payload) throws Exception {
    String json = objectMapper.writeValueAsString(payload);
    synchronized (session) {
      if (session.isOpen()) {
        session.sendMessage(new TextMessage(json));
      }
    }
  }

  private void removeSession(WebSocketSession session) {
    if (session == null) return;
    String sessionId = session.getId();
    String userKey = userKeyBySessionId.remove(sessionId);
    if (userKey == null) {
      Object attr = session.getAttributes().get(ATTR_KEY);
      if (attr != null) {
        userKey = String.valueOf(attr);
      }
    }
    if (userKey == null || userKey.isBlank()) {
      return;
    }
    Set<WebSocketSession> sessions = sessionsByUserKey.get(userKey);
    if (sessions == null) {
      return;
    }
    sessions.removeIf(item -> item == null || Objects.equals(item.getId(), sessionId));
    if (sessions.isEmpty()) {
      sessionsByUserKey.remove(userKey);
    }
  }

  private String resolveToken(URI uri) {
    if (uri == null) return "";
    String query = uri.getRawQuery();
    if (query == null || query.isBlank()) return "";
    String[] parts = query.split("&");
    for (String part : parts) {
      if (part == null || part.isBlank()) continue;
      int idx = part.indexOf('=');
      if (idx <= 0) continue;
      String key = decode(part.substring(0, idx));
      if (!"token".equals(key)) continue;
      return decode(part.substring(idx + 1));
    }
    return "";
  }

  private String decode(String text) {
    if (text == null) return "";
    try {
      return URLDecoder.decode(text, StandardCharsets.UTF_8);
    } catch (Exception ex) {
      return text;
    }
  }

  private String resolveUserKey(String token) {
    try {
      Claims claims = tokenProvider.parseToken(token);
      String jti = claims.getId();
      if (tokenBlacklistService.isBlacklisted(jti)) {
        return null;
      }
      Long orgId = claims.get("orgId", Long.class);
      String staffId = claims.getSubject();
      if (orgId == null || staffId == null || staffId.isBlank()) {
        return null;
      }
      return buildUserKey(orgId, Long.valueOf(staffId));
    } catch (Exception ex) {
      return null;
    }
  }

  private String buildUserKey(Long orgId, Long staffId) {
    return orgId + ":" + staffId;
  }
}
