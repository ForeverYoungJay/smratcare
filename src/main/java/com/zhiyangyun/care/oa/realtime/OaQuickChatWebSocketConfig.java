package com.zhiyangyun.care.oa.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class OaQuickChatWebSocketConfig implements WebSocketConfigurer {
  private final OaQuickChatWebSocketHandler quickChatWebSocketHandler;

  public OaQuickChatWebSocketConfig(OaQuickChatWebSocketHandler quickChatWebSocketHandler) {
    this.quickChatWebSocketHandler = quickChatWebSocketHandler;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(quickChatWebSocketHandler, "/ws/quick-chat")
        .setAllowedOriginPatterns("*");
  }
}
