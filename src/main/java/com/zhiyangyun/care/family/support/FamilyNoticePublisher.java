package com.zhiyangyun.care.family.support;

import com.zhiyangyun.care.oa.entity.OaNotice;
import com.zhiyangyun.care.oa.mapper.OaNoticeMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 家属端消息中心以“已发布公告”为数据源。
 * 该组件用于业务事件（急救发起、护理等级调整、活动相册更新等）自动向家属侧广播一条公告，
 * 标题关键词（紧急/提醒/活动）决定家属端的告警等级与分类。发布失败只记日志，不阻断主流程。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FamilyNoticePublisher {

  private final OaNoticeMapper oaNoticeMapper;

  public void publish(Long orgId, String title, String content) {
    try {
      OaNotice notice = new OaNotice();
      notice.setTenantId(orgId);
      notice.setOrgId(orgId);
      notice.setTitle(title);
      notice.setContent(content);
      notice.setPublisherName("系统联动");
      notice.setPublishTime(LocalDateTime.now());
      notice.setStatus("PUBLISHED");
      oaNoticeMapper.insert(notice);
    } catch (Exception e) {
      log.warn("family notice publish failed: {}", e.getMessage());
    }
  }
}
