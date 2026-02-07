package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.model.ExecuteTaskRequest;
import com.zhiyangyun.care.model.ExecuteTaskResponse;
import com.zhiyangyun.care.model.TaskStatus;
import com.zhiyangyun.care.service.CareTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CareTaskServiceTest {
  @Autowired
  private CareTaskService careTaskService;

  @Test
  void executeTask_success_withoutSuspicious() {
    ExecuteTaskRequest request = new ExecuteTaskRequest();
    request.setTaskDailyId(400L);
    request.setStaffId(501L);
    request.setBedQrCode("QR1");
    request.setRemark("ok");

    ExecuteTaskResponse response = careTaskService.executeTask(request);
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

    ExecuteTaskResponse response = careTaskService.executeTask(request);
    assertEquals(TaskStatus.DONE.name(), response.getStatus());
    assertFalse(response.isAbnormal());
    assertTrue(response.isSuspicious());
  }
}
