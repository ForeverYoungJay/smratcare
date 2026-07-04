package com.zhiyangyun.care.govreport.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.govreport.entity.GovChannelConfig;
import com.zhiyangyun.care.govreport.entity.GovReportSnapshot;
import com.zhiyangyun.care.govreport.entity.GovReportTask;
import com.zhiyangyun.care.govreport.model.GovReceiptCallbackRequest;
import com.zhiyangyun.care.govreport.model.GovReportBuildRequest;
import java.util.List;

/** 监管/医保上报业务：构建报文→发送→回执→重试。 */
public interface GovReportService {

  /** 构建上报任务：采集数据、按渠道字段映射生成报文、落快照，返回任务 id。 */
  Long buildTask(GovReportBuildRequest request);

  /** 发送指定任务到对应渠道适配器。 */
  GovReportTask send(Long taskId);

  /** 接收渠道回执回调，更新任务终态。 */
  void receiveCallback(GovReceiptCallbackRequest request);

  /** 分页查询上报任务。 */
  IPage<GovReportTask> pageTasks(long pageNo, long pageSize, String channel, String reportType,
      String status);

  /** 查看任务报文快照。 */
  GovReportSnapshot getSnapshot(Long taskId);

  /** 渠道配置列表。 */
  List<GovChannelConfig> listChannels(Long orgId);

  /** 重试失败任务（供定时任务调用），返回重试条数。 */
  int retryFailed(int maxRetry);
}
