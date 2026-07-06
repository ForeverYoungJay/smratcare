package com.zhiyangyun.care.compliance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhiyangyun.care.compliance.entity.ComplianceSecurityPolicy;
import com.zhiyangyun.care.compliance.mapper.ComplianceSecurityPolicyMapper;
import com.zhiyangyun.care.compliance.model.SecurityPolicyUpdateRequest;
import com.zhiyangyun.care.compliance.model.SecurityPolicyView;
import com.zhiyangyun.care.compliance.service.impl.ComplianceSecurityPolicyServiceImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComplianceSecurityPolicyServiceTest {

  @Mock
  private ComplianceSecurityPolicyMapper mapper;

  private ComplianceSecurityPolicyServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new ComplianceSecurityPolicyServiceImpl(mapper);
  }

  @Test
  void effective_policy_should_return_defaults_when_no_row() {
    when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

    SecurityPolicyView view = service.getEffectivePolicy(1L);

    assertFalse(view.getTwoFactorEnabled());
    assertFalse(view.getMaskingEnabled());
    assertEquals(0, view.getPasswordMaxDays());
    assertEquals(5, view.getLoginFailLockCount());
    assertEquals(30, view.getLoginFailLockMinutes());
    assertEquals(0, view.getSessionTimeoutMinutes());
    assertEquals(400, view.getLogRetentionDays());
    assertTrue(view.getMaskingExemptRoles().contains("SYS_ADMIN"));
  }

  @Test
  void effective_policy_should_return_defaults_without_query_when_org_missing() {
    SecurityPolicyView view = service.getEffectivePolicy(null);
    assertFalse(view.getTwoFactorEnabled());
    verify(mapper, never()).selectOne(any(LambdaQueryWrapper.class));
  }

  @Test
  void effective_policy_should_map_entity_and_cache_result() {
    ComplianceSecurityPolicy entity = new ComplianceSecurityPolicy();
    entity.setOrgId(1L);
    entity.setTwoFactorEnabled(1);
    entity.setTwoFactorRoles("sys_admin, director");
    entity.setPasswordMaxDays(90);
    entity.setLoginFailLockCount(3);
    entity.setLoginFailLockMinutes(15);
    entity.setSessionTimeoutMinutes(60);
    entity.setMaskingEnabled(1);
    entity.setMaskingExemptRoles("SYS_ADMIN");
    entity.setLogRetentionDays(500);
    when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(entity);

    SecurityPolicyView view = service.getEffectivePolicy(1L);
    assertTrue(view.getTwoFactorEnabled());
    assertEquals(List.of("SYS_ADMIN", "DIRECTOR"), view.getTwoFactorRoles());
    assertEquals(90, view.getPasswordMaxDays());
    assertEquals(3, view.getLoginFailLockCount());
    assertEquals(15, view.getLoginFailLockMinutes());
    assertEquals(60, view.getSessionTimeoutMinutes());
    assertTrue(view.getMaskingEnabled());
    assertEquals(500, view.getLogRetentionDays());

    // 60 秒 TTL 缓存：短时间内二次读取不再查库
    service.getEffectivePolicy(1L);
    verify(mapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
  }

  @Test
  void effective_policy_should_fail_open_to_defaults_on_db_error() {
    when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenThrow(new RuntimeException("table missing"));

    SecurityPolicyView view = service.getEffectivePolicy(2L);
    assertFalse(view.getTwoFactorEnabled());
    assertEquals(400, view.getLogRetentionDays());
  }

  @Test
  void update_should_reject_retention_below_180_days() {
    SecurityPolicyUpdateRequest request = new SecurityPolicyUpdateRequest();
    request.setLogRetentionDays(90);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> service.update(1L, request));
    assertTrue(ex.getMessage().contains("180"));
  }

  @Test
  void update_should_create_row_and_evict_cache() {
    when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
    SecurityPolicyUpdateRequest request = new SecurityPolicyUpdateRequest();
    request.setTwoFactorEnabled(true);
    request.setTwoFactorRoles(List.of("sys_admin"));
    request.setPasswordMaxDays(90);
    request.setLoginFailLockCount(5);
    request.setLoginFailLockMinutes(30);
    request.setSessionTimeoutMinutes(30);
    request.setMaskingEnabled(true);
    request.setMaskingExemptRoles(List.of("SYS_ADMIN"));
    request.setLogRetentionDays(400);

    SecurityPolicyView view = service.update(1L, request);

    ArgumentCaptor<ComplianceSecurityPolicy> captor = ArgumentCaptor.forClass(ComplianceSecurityPolicy.class);
    verify(mapper).insert(captor.capture());
    assertEquals(1L, captor.getValue().getOrgId());
    assertEquals(1, captor.getValue().getTwoFactorEnabled());
    assertEquals("SYS_ADMIN", captor.getValue().getTwoFactorRoles());
    assertTrue(view.getTwoFactorEnabled());
    assertEquals(400, view.getLogRetentionDays());
  }
}
