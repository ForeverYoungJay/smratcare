package com.zhiyangyun.care.compliance;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhiyangyun.care.compliance.mapper.AuthLoginLogMapper;
import com.zhiyangyun.care.compliance.model.SecurityPolicyView;
import com.zhiyangyun.care.compliance.service.impl.LoginSecurityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginSecurityServiceTest {

  @Mock
  private AuthLoginLogMapper mapper;

  private LoginSecurityServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new LoginSecurityServiceImpl(mapper);
  }

  private static SecurityPolicyView policy(int lockCount, int lockMinutes) {
    SecurityPolicyView view = new SecurityPolicyView();
    view.setLoginFailLockCount(lockCount);
    view.setLoginFailLockMinutes(lockMinutes);
    return view;
  }

  @Test
  void should_throw_when_recent_failures_reach_threshold() {
    when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
    when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> service.assertNotLocked("admin", policy(5, 30)));
    assertTrue(ex.getMessage().contains("锁定"));
  }

  @Test
  void should_pass_when_failures_below_threshold() {
    when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
    when(mapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

    assertDoesNotThrow(() -> service.assertNotLocked("admin", policy(5, 30)));
  }

  @Test
  void should_skip_check_when_lock_disabled() {
    assertDoesNotThrow(() -> service.assertNotLocked("admin", policy(0, 30)));
    verify(mapper, never()).selectCount(any(LambdaQueryWrapper.class));
  }

  @Test
  void should_fail_open_on_db_error() {
    when(mapper.selectOne(any(LambdaQueryWrapper.class))).thenThrow(new RuntimeException("table missing"));

    assertDoesNotThrow(() -> service.assertNotLocked("admin", policy(5, 30)));
  }

  @Test
  void record_should_swallow_db_error() {
    when(mapper.insert(any())).thenThrow(new RuntimeException("table missing"));

    assertDoesNotThrow(() -> service.record(1L, 2L, "admin", "PASSWORD", false, "BAD_CREDENTIALS"));
  }
}
