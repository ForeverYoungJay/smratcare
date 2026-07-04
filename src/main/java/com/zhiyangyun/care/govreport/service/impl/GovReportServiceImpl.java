package com.zhiyangyun.care.govreport.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.govreport.adapter.ReportChannelAdapter;
import com.zhiyangyun.care.govreport.adapter.ReportFieldMapper;
import com.zhiyangyun.care.govreport.adapter.ReportSendResult;
import com.zhiyangyun.care.govreport.entity.GovChannelConfig;
import com.zhiyangyun.care.govreport.entity.GovReportReceipt;
import com.zhiyangyun.care.govreport.entity.GovReportSnapshot;
import com.zhiyangyun.care.govreport.entity.GovReportTask;
import com.zhiyangyun.care.govreport.mapper.GovChannelConfigMapper;
import com.zhiyangyun.care.govreport.mapper.GovReportReceiptMapper;
import com.zhiyangyun.care.govreport.mapper.GovReportSnapshotMapper;
import com.zhiyangyun.care.govreport.mapper.GovReportTaskMapper;
import com.zhiyangyun.care.govreport.model.GovReceiptCallbackRequest;
import com.zhiyangyun.care.govreport.model.GovReportBuildRequest;
import com.zhiyangyun.care.govreport.service.GovReportService;
import com.zhiyangyun.care.ltci.entity.LtciSettlement;
import com.zhiyangyun.care.ltci.mapper.LtciSettlementMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class GovReportServiceImpl implements GovReportService {

  private static final String PENDING = "PENDING";
  private static final String SENT = "SENT";
  private static final String ACKED = "ACKED";
  private static final String FAILED = "FAILED";

  private final GovReportTaskMapper taskMapper;
  private final GovReportSnapshotMapper snapshotMapper;
  private final GovReportReceiptMapper receiptMapper;
  private final GovChannelConfigMapper channelMapper;
  private final LtciSettlementMapper settlementMapper;
  private final ObjectMapper objectMapper;
  private final Map<String, ReportChannelAdapter> adapters;

  public GovReportServiceImpl(GovReportTaskMapper taskMapper, GovReportSnapshotMapper snapshotMapper,
      GovReportReceiptMapper receiptMapper, GovChannelConfigMapper channelMapper,
      LtciSettlementMapper settlementMapper, ObjectMapper objectMapper,
      List<ReportChannelAdapter> adapterList) {
    this.taskMapper = taskMapper;
    this.snapshotMapper = snapshotMapper;
    this.receiptMapper = receiptMapper;
    this.channelMapper = channelMapper;
    this.settlementMapper = settlementMapper;
    this.objectMapper = objectMapper;
    this.adapters = adapterList.stream()
        .collect(Collectors.toMap(ReportChannelAdapter::channel, a -> a));
  }

  @Override
  @Transactional
  public Long buildTask(GovReportBuildRequest request) {
    Long orgId = AuthContext.getOrgId();
    GovChannelConfig config = resolveChannel(request.getChannel(), orgId);

    List<Map<String, Object>> records = collectRecords(request, orgId);
    Map<String, String> mapping = parseMapping(config);
    List<Map<String, Object>> mapped = ReportFieldMapper.applyAll(records, mapping);

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("reportType", request.getReportType());
    payload.put("channel", request.getChannel());
    payload.put("period", request.getPeriod());
    payload.put("recordCount", mapped.size());
    payload.put("records", mapped);
    String payloadJson = writeJson(payload);

    GovReportTask task = new GovReportTask();
    task.setOrgId(orgId);
    task.setReportType(request.getReportType());
    task.setChannel(request.getChannel());
    task.setPeriod(request.getPeriod());
    task.setTaskStatus(PENDING);
    task.setTriggerType("MANUAL");
    task.setRecordCount(mapped.size());
    task.setRetryCount(0);
    task.setRemark(request.getRemark());
    task.setCreatedBy(AuthContext.getStaffId());
    taskMapper.insert(task);

    GovReportSnapshot snapshot = new GovReportSnapshot();
    snapshot.setOrgId(orgId);
    snapshot.setTaskId(task.getId());
    snapshot.setPayloadJson(payloadJson);
    snapshot.setPayloadHash(sha256(payloadJson));
    snapshot.setFieldMappingVersion(config != null ? config.getFieldMappingVersion() : null);
    snapshotMapper.insert(snapshot);
    return task.getId();
  }

  @Override
  @Transactional
  public GovReportTask send(Long taskId) {
    GovReportTask task = loadTask(taskId);
    return doSend(task);
  }

  private GovReportTask doSend(GovReportTask task) {
    ReportChannelAdapter adapter = adapters.get(task.getChannel());
    if (adapter == null) {
      return markFailed(task, "无对应渠道适配器: " + task.getChannel());
    }
    GovReportSnapshot snapshot = snapshotMapper.selectOne(Wrappers.lambdaQuery(GovReportSnapshot.class)
        .eq(GovReportSnapshot::getIsDeleted, 0)
        .eq(GovReportSnapshot::getTaskId, task.getId())
        .last("limit 1"));
    if (snapshot == null) {
      return markFailed(task, "报文快照缺失，请先构建任务");
    }
    GovChannelConfig config = resolveChannel(task.getChannel(), task.getOrgId());
    ReportSendResult result;
    try {
      result = adapter.send(task, snapshot.getPayloadJson(), config);
    } catch (Exception ex) {
      return markFailed(task, "发送异常: " + ex.getMessage());
    }
    if (result == null || !result.isSuccess()) {
      return markFailed(task, result == null ? "发送失败" : result.getErrorDetail());
    }
    LocalDateTime now = LocalDateTime.now();
    task.setSentAt(now);
    task.setLastError(null);
    if (result.isAcked()) {
      task.setTaskStatus(ACKED);
      task.setAckedAt(now);
    } else {
      task.setTaskStatus(SENT);
    }
    taskMapper.updateById(task);
    writeReceipt(task, result.getReceiptCode(), result.isAcked() ? ACKED : SENT, null);
    return task;
  }

  private GovReportTask markFailed(GovReportTask task, String error) {
    task.setTaskStatus(FAILED);
    task.setLastError(error);
    task.setRetryCount(task.getRetryCount() == null ? 1 : task.getRetryCount() + 1);
    taskMapper.updateById(task);
    writeReceipt(task, null, FAILED, error);
    return task;
  }

  @Override
  @Transactional
  public void receiveCallback(GovReceiptCallbackRequest request) {
    GovReportTask task = taskMapper.selectById(request.getTaskId());
    if (task == null || Integer.valueOf(1).equals(task.getIsDeleted())) {
      throw new IllegalArgumentException("上报任务不存在: " + request.getTaskId());
    }
    boolean acked = ACKED.equalsIgnoreCase(request.getReceiptStatus());
    task.setTaskStatus(acked ? ACKED : FAILED);
    if (acked) {
      task.setAckedAt(LocalDateTime.now());
      task.setLastError(null);
    } else {
      task.setLastError(request.getErrorDetail());
    }
    taskMapper.updateById(task);
    writeReceipt(task, request.getReceiptCode(), acked ? ACKED : FAILED, request.getErrorDetail());
  }

  @Override
  public IPage<GovReportTask> pageTasks(long pageNo, long pageSize, String channel,
      String reportType, String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(GovReportTask.class)
        .eq(GovReportTask::getIsDeleted, 0)
        .eq(orgId != null, GovReportTask::getOrgId, orgId)
        .eq(StringUtils.hasText(channel), GovReportTask::getChannel, channel)
        .eq(StringUtils.hasText(reportType), GovReportTask::getReportType, reportType)
        .eq(StringUtils.hasText(status), GovReportTask::getTaskStatus, status)
        .orderByDesc(GovReportTask::getId);
    return taskMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  public GovReportSnapshot getSnapshot(Long taskId) {
    loadTask(taskId);
    return snapshotMapper.selectOne(Wrappers.lambdaQuery(GovReportSnapshot.class)
        .eq(GovReportSnapshot::getIsDeleted, 0)
        .eq(GovReportSnapshot::getTaskId, taskId)
        .last("limit 1"));
  }

  @Override
  public List<GovChannelConfig> listChannels(Long orgId) {
    return channelMapper.selectList(Wrappers.lambdaQuery(GovChannelConfig.class)
        .eq(GovChannelConfig::getIsDeleted, 0)
        .and(w -> w.isNull(GovChannelConfig::getOrgId)
            .or(orgId != null, q -> q.eq(GovChannelConfig::getOrgId, orgId)))
        .orderByAsc(GovChannelConfig::getChannel));
  }

  @Override
  public int retryFailed(int maxRetry) {
    List<GovReportTask> tasks = taskMapper.selectList(Wrappers.lambdaQuery(GovReportTask.class)
        .eq(GovReportTask::getIsDeleted, 0)
        .eq(GovReportTask::getTaskStatus, FAILED)
        .lt(GovReportTask::getRetryCount, maxRetry));
    int count = 0;
    for (GovReportTask task : tasks) {
      doSend(task);
      count++;
    }
    return count;
  }

  private List<Map<String, Object>> collectRecords(GovReportBuildRequest request, Long orgId) {
    List<Map<String, Object>> records = new ArrayList<>();
    if ("LTCI_SETTLE".equals(request.getReportType()) && StringUtils.hasText(request.getPeriod())) {
      List<LtciSettlement> settlements = settlementMapper.selectList(
          Wrappers.lambdaQuery(LtciSettlement.class)
              .eq(LtciSettlement::getIsDeleted, 0)
              .eq(orgId != null, LtciSettlement::getOrgId, orgId)
              .eq(LtciSettlement::getSettleMonth, request.getPeriod())
              .eq(LtciSettlement::getSettleStatus, "SUBMITTED"));
      for (LtciSettlement s : settlements) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("elderId", s.getElderId());
        r.put("settleMonth", s.getSettleMonth());
        r.put("totalFee", s.getTotalFee());
        r.put("fundPay", s.getFundPay());
        r.put("selfPay", s.getSelfPay());
        r.put("overQuota", s.getOverQuota());
        records.add(r);
      }
    }
    // 其余上报类型（ORG_INFO/BED/ELDER/SERVICE）的数据采集在后续迭代按渠道字典补齐。
    return records;
  }

  private GovChannelConfig resolveChannel(String channel, Long orgId) {
    if (orgId != null) {
      GovChannelConfig orgCfg = channelMapper.selectOne(Wrappers.lambdaQuery(GovChannelConfig.class)
          .eq(GovChannelConfig::getIsDeleted, 0)
          .eq(GovChannelConfig::getOrgId, orgId)
          .eq(GovChannelConfig::getChannel, channel)
          .eq(GovChannelConfig::getEnabled, 1)
          .last("limit 1"));
      if (orgCfg != null) {
        return orgCfg;
      }
    }
    return channelMapper.selectOne(Wrappers.lambdaQuery(GovChannelConfig.class)
        .eq(GovChannelConfig::getIsDeleted, 0)
        .isNull(GovChannelConfig::getOrgId)
        .eq(GovChannelConfig::getChannel, channel)
        .eq(GovChannelConfig::getEnabled, 1)
        .last("limit 1"));
  }

  private Map<String, String> parseMapping(GovChannelConfig config) {
    if (config == null || !StringUtils.hasText(config.getFieldMappingJson())) {
      return Map.of();
    }
    try {
      return objectMapper.readValue(config.getFieldMappingJson(),
          new TypeReference<LinkedHashMap<String, String>>() {});
    } catch (Exception ex) {
      throw new IllegalArgumentException("渠道字段映射配置解析失败: " + ex.getMessage(), ex);
    }
  }

  private void writeReceipt(GovReportTask task, String code, String status, String error) {
    GovReportReceipt receipt = new GovReportReceipt();
    receipt.setOrgId(task.getOrgId());
    receipt.setTaskId(task.getId());
    receipt.setReceiptCode(code);
    receipt.setReceiptStatus(status);
    receipt.setErrorDetail(error);
    receipt.setReceivedAt(LocalDateTime.now());
    receipt.setRetryCount(task.getRetryCount());
    receiptMapper.insert(receipt);
  }

  private GovReportTask loadTask(Long taskId) {
    GovReportTask task = taskMapper.selectById(taskId);
    if (task == null || Integer.valueOf(1).equals(task.getIsDeleted())) {
      throw new IllegalArgumentException("上报任务不存在: " + taskId);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(task.getOrgId())) {
      throw new IllegalArgumentException("无权操作其他机构的上报任务");
    }
    return task;
  }

  private String writeJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception ex) {
      throw new IllegalArgumentException("报文序列化失败: " + ex.getMessage(), ex);
    }
  }

  private String sha256(String text) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception ex) {
      return null;
    }
  }
}
