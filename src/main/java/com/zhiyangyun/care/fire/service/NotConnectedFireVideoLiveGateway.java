package com.zhiyangyun.care.fire.service;

import com.zhiyangyun.care.logistics.entity.LogisticsCamera;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 默认实现：未接入视频平台。另行提供 FireVideoLiveGateway Bean 时此实现自动失效。 */
@Configuration
public class NotConnectedFireVideoLiveGateway {
  @Bean
  @ConditionalOnMissingBean(FireVideoLiveGateway.class)
  public FireVideoLiveGateway fireVideoLiveGateway() {
    return (LogisticsCamera camera) -> FireVideoLiveGateway.LiveStream.notConnected();
  }
}
