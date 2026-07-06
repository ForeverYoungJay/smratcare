package com.zhiyangyun.care.compliance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhiyangyun.care.compliance.entity.ExportLog;
import com.zhiyangyun.care.compliance.mapper.ExportLogMapper;
import com.zhiyangyun.care.compliance.model.ExportConfirmRequest;
import com.zhiyangyun.care.compliance.model.ExportTicketResponse;
import com.zhiyangyun.care.compliance.service.SensitiveAccessService;
import com.zhiyangyun.care.compliance.service.impl.ExportAuditServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ExportAuditServiceTest {

  @Mock
  private ExportLogMapper mapper;
  @Mock
  private SensitiveAccessService sensitiveAccessService;

  private ExportAuditServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new ExportAuditServiceImpl(mapper, sensitiveAccessService);
    var auth = new UsernamePasswordAuthenticationToken(
        "9001", "N/A", List.of(new SimpleGrantedAuthority("ROLE_NURSING_MINISTER")));
    auth.setDetails(Map.of(
        "orgId", 1L,
        "username", "tester",
        "roleCodes", List.of("NURSING_MINISTER")));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void confirm_should_issue_pending_ticket() {
    ExportConfirmRequest request = new ExportConfirmRequest();
    request.setModule("elder_list");
    request.setScope("在住长者，共 120 行");
    request.setPurpose("民政报表");

    ExportTicketResponse response = service.confirm(request);

    assertNotNull(response.getExportTicket());
    assertNotNull(response.getExpiresAt());
    ArgumentCaptor<ExportLog> captor = ArgumentCaptor.forClass(ExportLog.class);
    verify(mapper).insert(captor.capture());
    assertEquals("PENDING", captor.getValue().getStatus());
    assertEquals("ELDER_LIST", captor.getValue().getModule());
    assertEquals(9001L, captor.getValue().getActorId());
    assertEquals(1L, captor.getValue().getOrgId());
  }

  @Test
  void confirm_should_require_purpose() {
    ExportConfirmRequest request = new ExportConfirmRequest();
    request.setModule("ELDER_LIST");

    assertThrows(IllegalArgumentException.class, () -> service.confirm(request));
  }

  @Test
  void complete_should_mark_used_and_record_sensitive_access() {
    ExportLog record = pendingRecord();
    when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);

    service.complete(record.getExportTicket(), 120L);

    assertEquals("USED", record.getStatus());
    assertEquals(120, record.getRowCount());
    assertNotNull(record.getUsedAt());
    verify(mapper).updateById(record);
    verify(sensitiveAccessService).record(anyString(), anyString(), anyString(), anyLong(),
        anyString(), any(), any(), anyBoolean());
  }

  @Test
  void complete_should_reject_other_actor() {
    ExportLog record = pendingRecord();
    record.setActorId(8888L);
    when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> service.complete(record.getExportTicket(), 10L));
    assertTrue(ex.getMessage().contains("本人"));
  }

  @Test
  void complete_should_reject_expired_ticket() {
    ExportLog record = pendingRecord();
    record.setExpiresAt(LocalDateTime.now().minusMinutes(1));
    when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);

    assertThrows(IllegalArgumentException.class, () -> service.complete(record.getExportTicket(), 10L));
    assertEquals("EXPIRED", record.getStatus());
  }

  @Test
  void complete_should_reject_used_ticket() {
    ExportLog record = pendingRecord();
    record.setStatus("USED");
    when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(record);

    assertThrows(IllegalArgumentException.class, () -> service.complete(record.getExportTicket(), 10L));
  }

  private static ExportLog pendingRecord() {
    ExportLog record = new ExportLog();
    record.setId(100L);
    record.setOrgId(1L);
    record.setActorId(9001L);
    record.setModule("ELDER_LIST");
    record.setPurpose("民政报表");
    record.setExportTicket("EXP-test-ticket");
    record.setStatus("PENDING");
    record.setExpiresAt(LocalDateTime.now().plusMinutes(10));
    return record;
  }
}
