package com.zhiyangyun.care.medins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.ltci.entity.LtciSettlement;
import com.zhiyangyun.care.ltci.mapper.LtciSettlementMapper;
import com.zhiyangyun.care.medins.adapter.MedinsMockAdapter;
import com.zhiyangyun.care.medins.entity.MedinsChannelConfig;
import com.zhiyangyun.care.medins.entity.MedinsSettlementSheet;
import com.zhiyangyun.care.medins.entity.MedinsUploadTask;
import com.zhiyangyun.care.medins.mapper.MedinsSettlementSheetMapper;
import com.zhiyangyun.care.medins.mapper.MedinsUploadTaskMapper;
import com.zhiyangyun.care.medins.model.MedinsReceiptCallbackRequest;
import com.zhiyangyun.care.medins.service.MedinsChannelService;
import com.zhiyangyun.care.medins.service.impl.MedinsSettlementServiceImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 医保结算清单状态机单测：生成 → 上传(MOCK 渠道) → 回执 → 对账/驳回。 */
@ExtendWith(MockitoExtension.class)
class MedinsSettlementServiceTest {

  @Mock
  private MedinsSettlementSheetMapper sheetMapper;
  @Mock
  private MedinsUploadTaskMapper taskMapper;
  @Mock
  private LtciSettlementMapper ltciSettlementMapper;
  @Mock
  private MedinsChannelService channelService;

  private MedinsSettlementServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new MedinsSettlementServiceImpl(sheetMapper, taskMapper, ltciSettlementMapper,
        channelService, new ObjectMapper(), List.of(new MedinsMockAdapter()));
  }

  private LtciSettlement ltciSettlement(String status) {
    LtciSettlement ltci = new LtciSettlement();
    ltci.setId(100L);
    ltci.setOrgId(1L);
    ltci.setElderId(9L);
    ltci.setSettleMonth("202606");
    ltci.setTotalFee(500000L);
    ltci.setFundPay(400000L);
    ltci.setSelfPay(100000L);
    ltci.setSettleStatus(status);
    ltci.setIsDeleted(0);
    return ltci;
  }

  private MedinsSettlementSheet sheet(Long id, String status) {
    MedinsSettlementSheet sheet = new MedinsSettlementSheet();
    sheet.setId(id);
    sheet.setOrgId(1L);
    sheet.setLtciSettlementId(100L);
    sheet.setElderId(9L);
    sheet.setSheetNo("MIS202606-9");
    sheet.setSettleMonth("202606");
    sheet.setTotalFee(500000L);
    sheet.setFundPay(400000L);
    sheet.setSelfPay(100000L);
    sheet.setSheetStatus(status);
    sheet.setIsDeleted(0);
    return sheet;
  }

  private MedinsChannelConfig mockChannelConfig() {
    MedinsChannelConfig config = new MedinsChannelConfig();
    config.setChannel(MedinsMockAdapter.CHANNEL);
    config.setEndpoint("mock://medins.local/settle");
    config.setSecretRef("ref:medins.mock.secret");
    config.setEnabled(1);
    return config;
  }

  /** 已提交的长护险结算单 → 生成 DRAFT 清单，金额照抄（统筹/自付）。 */
  @Test
  void generateSheet_fromSubmittedLtci_createsDraft() {
    when(ltciSettlementMapper.selectById(100L)).thenReturn(ltciSettlement("SUBMITTED"));
    when(sheetMapper.selectOne(any())).thenReturn(null);

    MedinsSettlementSheet sheet = service.generateSheet(100L, null, null);

    assertEquals("DRAFT", sheet.getSheetStatus());
    assertEquals("MIS202606-9", sheet.getSheetNo());
    assertEquals(500000L, sheet.getTotalFee());
    assertEquals(400000L, sheet.getFundPay());
    assertEquals(100000L, sheet.getSelfPay());
    assertEquals(100L, sheet.getLtciSettlementId());
    verify(sheetMapper).insert(any(MedinsSettlementSheet.class));
  }

  /** 草稿状态的长护险结算单不允许生成清单。 */
  @Test
  void generateSheet_draftLtci_throws() {
    when(ltciSettlementMapper.selectById(100L)).thenReturn(ltciSettlement("DRAFT"));

    assertThrows(IllegalStateException.class, () -> service.generateSheet(100L, null, null));
  }

  /** 已流转（非 DRAFT）的清单不允许重复生成。 */
  @Test
  void generateSheet_alreadyUploaded_throws() {
    when(ltciSettlementMapper.selectById(100L)).thenReturn(ltciSettlement("SUBMITTED"));
    when(sheetMapper.selectOne(any())).thenReturn(sheet(1L, "UPLOADED"));

    assertThrows(IllegalStateException.class, () -> service.generateSheet(100L, null, null));
  }

  /** MOCK 渠道上传：任务 SENT（异步回执），清单 DRAFT → UPLOADED，报文快照+指纹落库。 */
  @Test
  void uploadSheet_mockChannel_sentAndSnapshot() {
    when(sheetMapper.selectById(1L)).thenReturn(sheet(1L, "DRAFT"));
    when(channelService.resolveChannel(eq(MedinsMockAdapter.CHANNEL), anyLong()))
        .thenReturn(mockChannelConfig());

    MedinsUploadTask task = service.uploadSheet(1L, MedinsMockAdapter.CHANNEL);

    assertEquals("SENT", task.getTaskStatus());
    assertNotNull(task.getReceiptCode());
    assertNotNull(task.getPayloadJson());
    assertNotNull(task.getPayloadHash());
    assertTrue(task.getPayloadJson().contains("\"sheetNo\":\"MIS202606-9\""));
    assertNull(task.getLastError());

    ArgumentCaptor<MedinsSettlementSheet> captor =
        ArgumentCaptor.forClass(MedinsSettlementSheet.class);
    verify(sheetMapper).updateById(captor.capture());
    assertEquals("UPLOADED", captor.getValue().getSheetStatus());
    assertEquals(MedinsMockAdapter.CHANNEL, captor.getValue().getChannel());
    assertNotNull(captor.getValue().getUploadedAt());
  }

  /** 未知渠道 → 任务 FAILED，重试计数 +1，清单保持原状态。 */
  @Test
  void uploadSheet_unknownChannel_failsTask() {
    when(sheetMapper.selectById(1L)).thenReturn(sheet(1L, "DRAFT"));

    MedinsUploadTask task = service.uploadSheet(1L, "NOT_EXIST");

    assertEquals("FAILED", task.getTaskStatus());
    assertEquals(1, task.getRetryCount());
    assertTrue(task.getLastError().contains("无对应医保渠道适配器"));
  }

  /** 非草稿/驳回清单不允许上传。 */
  @Test
  void uploadSheet_receiptedSheet_throws() {
    when(sheetMapper.selectById(1L)).thenReturn(sheet(1L, "RECEIPTED"));

    assertThrows(IllegalStateException.class,
        () -> service.uploadSheet(1L, MedinsMockAdapter.CHANNEL));
  }

  /** 回执查询（MOCK 同步终态）：任务 SENT → ACKED，清单 UPLOADED → RECEIPTED。 */
  @Test
  void queryReceipt_acked_marksSheetReceipted() {
    MedinsUploadTask task = new MedinsUploadTask();
    task.setId(7L);
    task.setOrgId(1L);
    task.setSheetId(1L);
    task.setChannel(MedinsMockAdapter.CHANNEL);
    task.setTaskStatus("SENT");
    task.setReceiptCode("MI-1-1");
    task.setIsDeleted(0);
    when(taskMapper.selectById(7L)).thenReturn(task);
    when(sheetMapper.selectById(1L)).thenReturn(sheet(1L, "UPLOADED"));
    when(channelService.resolveChannel(eq(MedinsMockAdapter.CHANNEL), anyLong()))
        .thenReturn(mockChannelConfig());

    MedinsUploadTask result = service.queryReceipt(7L);

    assertEquals("ACKED", result.getTaskStatus());
    assertNotNull(result.getAckedAt());
    assertNotNull(result.getReceiptJson());

    ArgumentCaptor<MedinsSettlementSheet> captor =
        ArgumentCaptor.forClass(MedinsSettlementSheet.class);
    verify(sheetMapper).updateById(captor.capture());
    assertEquals("RECEIPTED", captor.getValue().getSheetStatus());
    assertEquals("MI-1-1", captor.getValue().getReceiptCode());
  }

  /** 渠道回调驳回：任务 FAILED，清单 → REJECTED 并留驳回原因。 */
  @Test
  void receiveCallback_rejected_marksSheetRejected() {
    MedinsUploadTask task = new MedinsUploadTask();
    task.setId(7L);
    task.setOrgId(1L);
    task.setSheetId(1L);
    task.setChannel(MedinsMockAdapter.CHANNEL);
    task.setTaskStatus("SENT");
    task.setIsDeleted(0);
    when(taskMapper.selectById(7L)).thenReturn(task);
    when(sheetMapper.selectById(1L)).thenReturn(sheet(1L, "UPLOADED"));

    MedinsReceiptCallbackRequest request = new MedinsReceiptCallbackRequest();
    request.setTaskId(7L);
    request.setReceiptStatus("REJECTED");
    request.setErrorDetail("参保信息不一致");
    service.receiveCallback(request);

    assertEquals("FAILED", task.getTaskStatus());
    ArgumentCaptor<MedinsSettlementSheet> captor =
        ArgumentCaptor.forClass(MedinsSettlementSheet.class);
    verify(sheetMapper).updateById(captor.capture());
    assertEquals("REJECTED", captor.getValue().getSheetStatus());
    assertEquals("参保信息不一致", captor.getValue().getRejectReason());
  }

  /** 对账：RECEIPTED → RECONCILED；非已回执清单拒绝对账。 */
  @Test
  void reconcileSheet_stateMachine() {
    when(sheetMapper.selectById(1L)).thenReturn(sheet(1L, "RECEIPTED"));
    service.reconcileSheet(1L);
    ArgumentCaptor<MedinsSettlementSheet> captor =
        ArgumentCaptor.forClass(MedinsSettlementSheet.class);
    verify(sheetMapper).updateById(captor.capture());
    assertEquals("RECONCILED", captor.getValue().getSheetStatus());
    assertNotNull(captor.getValue().getReconciledAt());

    when(sheetMapper.selectById(2L)).thenReturn(sheet(2L, "DRAFT"));
    assertThrows(IllegalStateException.class, () -> service.reconcileSheet(2L));
  }
}
