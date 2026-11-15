package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.entity.CareTaskExecuteLog;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.mapper.CareTaskExecuteLogMapper;
import com.zhiyangyun.care.model.ExecuteTaskRequest;
import com.zhiyangyun.care.model.ExecuteTaskResponse;
import com.zhiyangyun.care.model.TaskStatus;
import com.zhiyangyun.care.service.CareTaskService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CareTaskServiceTest {
  @Autowired
  private CareTaskService careTaskService;

  @Autowired
  private CareTaskDailyMapper careTaskDailyMapper;

  @Autowired
  private CareTaskExecuteLogMapper executeLogMapper;

  @Test
  void executeTask_success_withoutSuspicious() {
    ExecuteTaskRequest request = new ExecuteTaskRequest();
    request.setTaskDailyId(400L);
    request.setStaffId(501L);
    request.setBedQrCode("QR1");
    request.setRemark("ok");

    ExecuteTaskResponse response = careTaskService.executeTask(null, request);
    assertEquals(TaskStatus.DONE.name(), response.getStatus());
    assertFalse(response.isAbnormal());
    assertFalse(response.isSuspicious());
  }

  @Test
  void executeTask_marksSuspicious_whenDifferentRoomWithin3Minutes() {
    ExecuteTaskRequest request = new ExecuteTaskRequest();
    request.setTaskDailyId(401L);
    request.setStaffId(500L);
    request.setBedQrCode("QR1");
    request.setRemark("check");

    ExecuteTaskResponse response = careTaskService.executeTask(null, request);
    assertEquals(TaskStatus.DONE.name(), response.getStatus());
    assertFalse(response.isAbnormal());
    assertTrue(response.isSuspicious());
  }

  @Test
  void executeTask_is_idempotent() {
    CareTaskDaily task = new CareTaskDaily();
    task.setTenantId(1L);
    task.setOrgId(1L);
    task.setElderId(200L);
    task.setBedId(100L);
    task.setTemplateId(300L);
    task.setTaskDate(LocalDate.now());
    task.setPlanTime(LocalDateTime.now());
    task.setStatus(TaskStatus.PENDING.name());
    task.setIsDeleted(0);
    careTaskDailyMapper.insert(task);

    ExecuteTaskRequest request = new ExecuteTaskRequest();
    request.setTaskDailyId(task.getId());
    request.setStaffId(501L);
    request.setBedQrCode("QR1");

    careTaskService.executeTask(1L, request);
    careTaskService.executeTask(1L, request);

    long logCount = executeLogMapper.selectCount(Wrappers.lambdaQuery(CareTaskExecuteLog.class)
        .eq(CareTaskExecuteLog::getTaskDailyId, task.getId())
        .eq(CareTaskExecuteLog::getResultStatus, 1));
    assertEquals(1L, logCount);
  }
}
