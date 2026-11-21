package com.zhiyangyun.care.fire.service;

import com.zhiyangyun.care.logistics.entity.LogisticsCamera;

/**
 * 实时视频画面接入点。接入合法的视频平台时，实现此接口并注册为 Spring Bean（会替换默认的未接入实现），
 * 返回带时效的播放地址（如 HLS/WebRTC/FLV）。页面只展示地址，不直接连摄像头。
 */
public interface FireVideoLiveGateway {
  LiveStream open(LogisticsCamera camera);

  record LiveStream(boolean configured, String url, String protocol, String message) {
    public static LiveStream notConnected() {
      return new LiveStream(false, null, null, "尚未接入视频平台，画面为占位示意");
    }
  }
}
