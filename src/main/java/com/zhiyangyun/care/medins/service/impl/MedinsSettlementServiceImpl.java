package com.zhiyangyun.care.medins.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.ltci.entity.LtciSettlement;
import com.zhiyangyun.care.ltci.mapper.LtciSettlementMapper;
import com.zhiyangyun.care.medins.adapter.MedinsChannelAdapter;
import com.zhiyangyun.care.medins.adapter.MedinsChannelResult;
import com.zhiyangyun.care.medins.entity.MedinsChannelConfig;
import com.zhiyangyun.care.medins.entity.MedinsSettlementSheet;
import com.zhiyangyun.care.medins.entity.MedinsUploadTask;
import com.zhiyangyun.care.medins.mapper.MedinsSettlementSheetMapper;
import com.zhiyangyun.care.medins.mapper.MedinsUploadTaskMapper;
import com.zhiyangyun.care.medins.model.MedinsReceiptCallbackRequest;
import com.zhiyangyun.care.medins.service.MedinsChannelService;
import com.zhiyangyun.care.medins.service.MedinsSettlementService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MedinsSettlementServiceImpl implements MedinsSettlementService {

  /** 清单状态机。 */
  public static final String SHEET_DRAFT = "DRAFT";
  public static final String SHEET_UPLOADED = "UPLOADED";
  public static final String SHEET_RECEIPTED = "RECEIPTED";
  public static final String SHEET_RECONCILED = "RECONCILED";
  public static final String SHEET_REJECTED = "REJECTED";

  /** 上报任务状态。 */
  private static final String TASK_PENDING = "PENDING";
  private static final String TASK_SENT = "SENT";
  private static final String TASK_ACKED = "ACKED";
  private static final String TASK_FAILED = "FAILED";

  private static final String BIZ_SETTLE_SHEET = "SETTLE_SHEET";

  private final MedinsSettlementSheetMapper sheetMapper;
  private final MedinsUploadTaskMapper taskMapper;
  private final LtciSettlementMapper ltciSettlementMapper;
  private final MedinsChannelService channelService;
  private final ObjectMapper objectMapper;
  private final Map<String, MedinsChannelAdapter> adapters;

  public MedinsSettlementServiceImpl(MedinsSettlementSheetMapper sheetMapper,
      MedinsUploadTaskMapper taskMapper, LtciSettlementMapper ltciSettlementMapper,
      MedinsChannelService channelService, ObjectMapper objectMapper,
      List<MedinsChannelAdapter> adapterList) {
    this.sheetMapper = sheetMapper;
    this.taskMapper = taskMapper;
    this.ltciSettlementMapper = ltciSettlementMapper;
    this.channelService = channelService;
    this.objectMapper = objectMapper;
    this.adapters = adapterList.stream()
        .collect(Collectors.toMap(MedinsChannelAdapter::channel, a -> a));
  }

  @Override
  @Transactional
  public MedinsSettlementSheet generateSheet(Long ltciSettlementId, Long elderId, String month) {
    LtciSettlement ltci = locateLtciSettlement(ltciSettlementId, elderId, month);
    if (!"SUBMITTED".equals(ltci.getSettleStatus()) && !"SETTLED".equals(ltci.getSettleStatus())) {
      throw new IllegalStateException(
          "仅已提交(SUBMITTED)或已结算(SETTLED)的长护险结算单可生成医保结算清单，当前=" + ltci.getSettleStatus());
    }

    MedinsSettlementSheet existing = sheetMapper.selectOne(
        Wrappers.lambdaQuery(MedinsSettlementSheet.class)
            .eq(MedinsSettlementSheet::getIsDeleted, 0)
            .eq(MedinsSettlementSheet::getOrgId, ltci.getOrgId())
            .eq(MedinsSettlementSheet::getLtciSettlementId, ltci.getId())
            .last("limit 1"));
    if (existing != null && !SHEET_DRAFT.equals(existing.getSheetStatus())) {
      throw new IllegalStateException(
          "该长护险结算单已生成医保结算清单且已流转，不能重复生成: " + existing.getSheetNo());
    }

    MedinsSettlementSheet sheet = existing != null ? existing : new MedinsSettlementSheet();
    sheet.setOrgId(ltci.getOrgId());
    sheet.setLtciSettlementId(ltci.getId());
    sheet.setElderId(ltci.getElderId());
    sheet.setSheetNo("MIS" + ltci.getSettleMonth() + "-" + ltci.getElderId());
    sheet.setSettleMonth(ltci.getSettleMonth());
    sheet.setTotalFee(ltci.getTotalFee() == null ? 0L : ltci.getTotalFee());
    sheet.setFundPay(ltci.getFundPay() == null ? 0L : ltci.getFundPay());
    sheet.setSelfPay(ltci.getSelfPay() == null ? 0L : ltci.getSelfPay());
    sheet.setSheetStatus(SHEET_DRAFT);
    if (existing == null) {
      sheet.setCreatedBy(AuthContext.getStaffId());
      sheetMapper.insert(sheet);
    } else {
      sheetMapper.updateById(sheet);
    }
    return sheet;
  }

  @Override
  public IPage<MedinsSettlementSheet> pageSheets(long pageNo, long pageSize, Long elderId,
      String month, String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(MedinsSettlementSheet.class)
        .eq(MedinsSettlementSheet::getIsDeleted, 0)
        .eq(orgId != null, MedinsSettlementSheet::getOrgId, orgId)
        .eq(elderId != null, MedinsSettlementSheet::getElderId, elderId)
        .eq(StringUtils.hasText(month), MedinsSettlementSheet::getSettleMonth, month)
        .eq(StringUtils.hasText(status), MedinsSettlementSheet::getSheetStatus, status)
        .orderByDesc(MedinsSettlementSheet::getSettleMonth)
        .orderByDesc(MedinsSettlementSheet::getId);
    return sheetMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  public MedinsSettlementSheet getSheet(Long sheetId) {
    return loadSheet(sheetId);
  }

  @Override
  @Transactional
  public MedinsUploadTask uploadSheet(Long sheetId, String channel) {
    MedinsSettlementSheet sheet = loadSheet(sheetId);
    if (!SHEET_DRAFT.equals(sheet.getSheetStatus())
        && !SHEET_REJECTED.equals(sheet.getSheetStatus())) {
      throw new IllegalStateException(
          "仅草稿(DRAFT)或驳回(REJECTED)清单可上传，当前=" + sheet.getSheetStatus());
    }
    String targetChannel = StringUtils.hasText(channel) ? channel : sheet.getChannel();
    if (!StringUtils.hasText(targetChannel)) {
      throw new IllegalArgumentException("请指定上传渠道");
    }

    String payloadJson = buildPayload(sheet, targetChannel);
    MedinsUploadTask task = new MedinsUploadTask();
    task.setOrgId(sheet.getOrgId());
    task.setSheetId(sheet.getId());
    task.setChannel(targetChannel);
    task.setBizType(BIZ_SETTLE_SHEET);
    task.setTaskStatus(TASK_PENDING);
    task.setPayloadJson(payloadJson);
    task.setPayloadHash(sha256(payloadJson));
    task.setRetryCount(0);
    task.setCreatedBy(AuthContext.getStaffId());
    taskMapper.insert(task);

    return doSend(task, sheet);
  }

  @Override
  @Transactional
  public MedinsUploadTask retryTask(Long taskId) {
    MedinsUploadTask task = loadTask(taskId);
    if (!TASK_FAILED.equals(task.getTaskStatus())) {
      throw new IllegalStateException("仅失败(FAILED)任务可重发，当前=" + task.getTaskStatus());
    }
    MedinsSettlementSheet sheet = loadSheet(task.getSheetId());
    return doSend(task, sheet);
  }

  @Override
  @Transactional
  public MedinsUploadTask queryReceipt(Long taskId) {
    MedinsUploadTask task = loadTask(taskId);
    if (!TASK_SENT.equals(task.getTaskStatus())) {
      throw new IllegalStateException("仅已发送(SENT)任务可查询回执，当前=" + task.getTaskStatus());
    }
    MedinsChannelAdapter adapter = adapters.get(task.getChannel());
    if (adapter == null) {
      throw new IllegalArgumentException("无对应医保渠道适配器: " + task.getChannel());
    }
    MedinsChannelConfig config = channelService.resolveChannel(task.getChannel(), task.getOrgId());
    MedinsChannelResult result;
    try {
      result = adapter.queryReceipt(task, config);
    } catch (Exception ex) {
      result = MedinsChannelResult.fail("回执查询异常: " + ex.getMessage());
    }
    if (result == null || !result.isSuccess()) {
      task.setLastError(result == null ? "回执查询失败" : result.getErrorDetail());
      taskMapper.updateById(task);
      return task;
    }
    if (result.isAcked()) {
      MedinsSettlementSheet sheet = loadSheet(task.getSheetId());
      markAcked(task, sheet, result.getReceiptCode(), result.getReceiptJson());
    }
    return task;
  }

  @Override
  @Transactional
  public void receiveCallback(MedinsReceiptCallbackRequest request) {
    MedinsUploadTask task = taskMapper.selectById(request.getTaskId());
    if (task == null || Integer.valueOf(1).equals(task.getIsDeleted())) {
      throw new IllegalArgumentException("医保上报任务不存在: " + request.getTaskId());
    }
    MedinsSettlementSheet sheet = sheetMapper.selectById(task.getSheetId());
    if (sheet == null || Integer.valueOf(1).equals(sheet.getIsDeleted())) {
      throw new IllegalArgumentException("医保结算清单不存在: " + task.getSheetId());
    }
    boolean acked = "ACKED".equalsIgnoreCase(request.getReceiptStatus());
    if (acked) {
      markAcked(task, sheet, request.getReceiptCode(), request.getReceiptJson());
      return;
    }
    // 驳回：任务失败留痕，清单进入 REJECTED，可修正后重传。
    task.setTaskStatus(TASK_FAILED);
    task.setReceiptCode(request.getReceiptCode());
    task.setReceiptJson(request.getReceiptJson());
    task.setLastError(request.getErrorDetail());
    taskMapper.updateById(task);
    sheet.setSheetStatus(SHEET_REJECTED);
    sheet.setRejectReason(request.getErrorDetail());
    sheetMapper.updateById(sheet);
  }

  @Override
  @Transactional
  public void reconcileSheet(Long sheetId) {
    MedinsSettlementSheet sheet = loadSheet(sheetId);
    if (!SHEET_RECEIPTED.equals(sheet.getSheetStatus())) {
      throw new IllegalStateException(
          "仅已回执(RECEIPTED)清单可对账，当前=" + sheet.getSheetStatus());
    }
    sheet.setSheetStatus(SHEET_RECONCILED);
    sheet.setReconciledAt(LocalDateTime.now());
    sheetMapper.updateById(sheet);
  }

  @Override
  public IPage<MedinsUploadTask> pageTasks(long pageNo, long pageSize, Long sheetId,
      String status) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(MedinsUploadTask.class)
        .eq(MedinsUploadTask::getIsDeleted, 0)
        .eq(orgId != null, MedinsUploadTask::getOrgId, orgId)
        .eq(sheetId != null, MedinsUploadTask::getSheetId, sheetId)
        .eq(StringUtils.hasText(status), MedinsUploadTask::getTaskStatus, status)
        .orderByDesc(MedinsUploadTask::getId);
    return taskMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  private MedinsUploadTask doSend(MedinsUploadTask task, MedinsSettlementSheet sheet) {
    MedinsChannelAdapter adapter = adapters.get(task.getChannel());
    if (adapter == null) {
      return markFailed(task, "无对应医保渠道适配器: " + task.getChannel());
    }
    MedinsChannelConfig config = channelService.resolveChannel(task.getChannel(), task.getOrgId());
    MedinsChannelResult result;
    try {
      result = adapter.uploadSettlementSheet(sheet, task.getPayloadJson(), config);
    } catch (Exception ex) {
      return markFailed(task, "上传异常: " + ex.getMessage());
    }
    if (result == null || !result.isSuccess()) {
      return markFailed(task, result == null ? "上传失败" : result.getErrorDetail());
    }
    LocalDateTime now = LocalDateTime.now();
    task.setSentAt(now);
    task.setLastError(null);
    task.setReceiptCode(result.getReceiptCode());
    sheet.setChannel(task.getChannel());
    sheet.setUploadedAt(now);
    if (result.isAcked()) {
      task.setTaskStatus(TASK_ACKED);
      task.setAckedAt(now);
      task.setReceiptJson(result.getReceiptJson());
      sheet.setSheetStatus(SHEET_RECEIPTED);
      sheet.setReceiptedAt(now);
      sheet.setReceiptCode(result.getReceiptCode());
    } else {
      task.setTaskStatus(TASK_SENT);
      sheet.setSheetStatus(SHEET_UPLOADED);
    }
    sheet.setRejectReason(null);
    taskMapper.updateById(task);
    sheetMapper.updateById(sheet);
    return task;
  }

  private void markAcked(MedinsUploadTask task, MedinsSettlementSheet sheet, String receiptCode,
      String receiptJson) {
    LocalDateTime now = LocalDateTime.now();
    task.setTaskStatus(TASK_ACKED);
    task.setAckedAt(now);
    task.setLastError(null);
    if (StringUtils.hasText(receiptCode)) {
      task.setReceiptCode(receiptCode);
    }
    task.setReceiptJson(receiptJson);
    taskMapper.updateById(task);
    sheet.setSheetStatus(SHEET_RECEIPTED);
    sheet.setReceiptedAt(now);
    sheet.setReceiptCode(StringUtils.hasText(receiptCode) ? receiptCode : task.getReceiptCode());
    sheet.setRejectReason(null);
    sheetMapper.updateById(sheet);
  }

  private MedinsUploadTask markFailed(MedinsUploadTask task, String error) {
    task.setTaskStatus(TASK_FAILED);
    task.setLastError(error);
    task.setRetryCount(task.getRetryCount() == null ? 1 : task.getRetryCount() + 1);
    taskMapper.updateById(task);
    return task;
  }

  private LtciSettlement locateLtciSettlement(Long ltciSettlementId, Long elderId, String month) {
    Long orgId = AuthContext.getOrgId();
    LtciSettlement ltci;
    if (ltciSettlementId != null) {
      ltci = ltciSettlementMapper.selectById(ltciSettlementId);
      if (ltci == null || Integer.valueOf(1).equals(ltci.getIsDeleted())) {
        throw new IllegalArgumentException("长护险结算单不存在: " + ltciSettlementId);
      }
    } else {
      if (elderId == null || !StringUtils.hasText(month)) {
        throw new IllegalArgumentException("请指定长护险结算单 ID，或长者与结算月份");
      }
      ltci = ltciSettlementMapper.selectOne(Wrappers.lambdaQuery(LtciSettlement.class)
          .eq(LtciSettlement::getIsDeleted, 0)
          .eq(orgId != null, LtciSettlement::getOrgId, orgId)
          .eq(LtciSettlement::getElderId, elderId)
          .eq(LtciSettlement::getSettleMonth, month)
          .orderByDesc(LtciSettlement::getId)
          .last("limit 1"));
      if (ltci == null) {
        throw new IllegalArgumentException("未找到该长者当月的长护险结算单: " + month);
      }
    }
    if (orgId != null && !orgId.equals(ltci.getOrgId())) {
      throw new IllegalArgumentException("无权操作其他机构的长护险结算单");
    }
    return ltci;
  }

  private String buildPayload(MedinsSettlementSheet sheet, String channel) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("bizType", BIZ_SETTLE_SHEET);
    payload.put("channel", channel);
    payload.put("sheetNo", sheet.getSheetNo());
    payload.put("elderId", sheet.getElderId());
    payload.put("settleMonth", sheet.getSettleMonth());
    payload.put("totalFee", sheet.getTotalFee());
    payload.put("fundPay", sheet.getFundPay());
    payload.put("selfPay", sheet.getSelfPay());
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (Exception ex) {
      throw new IllegalArgumentException("结算清单报文序列化失败: " + ex.getMessage(), ex);
    }
  }

  private MedinsSettlementSheet loadSheet(Long sheetId) {
    MedinsSettlementSheet sheet = sheetMapper.selectById(sheetId);
    if (sheet == null || Integer.valueOf(1).equals(sheet.getIsDeleted())) {
      throw new IllegalArgumentException("医保结算清单不存在: " + sheetId);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(sheet.getOrgId())) {
      throw new IllegalArgumentException("无权操作其他机构的医保结算清单");
    }
    return sheet;
  }

  private MedinsUploadTask loadTask(Long taskId) {
    MedinsUploadTask task = taskMapper.selectById(taskId);
    if (task == null || Integer.valueOf(1).equals(task.getIsDeleted())) {
      throw new IllegalArgumentException("医保上报任务不存在: " + taskId);
    }
    Long orgId = AuthContext.getOrgId();
    if (orgId != null && !orgId.equals(task.getOrgId())) {
      throw new IllegalArgumentException("无权操作其他机构的医保上报任务");
    }
    return task;
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
