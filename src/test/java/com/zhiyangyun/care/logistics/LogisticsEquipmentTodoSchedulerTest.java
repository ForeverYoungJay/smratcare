package com.zhiyangyun.care.logistics;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zhiyangyun.care.auth.entity.Org;
import com.zhiyangyun.care.auth.mapper.OrgMapper;
import com.zhiyangyun.care.logistics.model.LogisticsMaintenanceTodoGenerateResult;
import com.zhiyangyun.care.logistics.service.LogisticsEquipmentTodoService;
import com.zhiyangyun.care.logistics.task.LogisticsEquipmentTodoScheduler;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogisticsEquipmentTodoSchedulerTest {

  @Mock
  private LogisticsEquipmentTodoService equipmentTodoService;

  @Mock
  private OrgMapper orgMapper;

  @Test
  void shouldSaveJobLogPerOrg() throws Exception {
    Org first = org(101L);
    Org second = org(202L);
    LogisticsMaintenanceTodoGenerateResult result = new LogisticsMaintenanceTodoGenerateResult();
    result.setCreatedCount(3L);
    result.setSkippedCount(1L);
    result.setTotalMatched(4L);

    when(orgMapper.selectList(any())).thenReturn(List.of(first, second));
    when(equipmentTodoService.generateMaintenanceTodos(101L, 15, 0L)).thenReturn(result);
    when(equipmentTodoService.generateMaintenanceTodos(202L, 15, 0L)).thenThrow(new IllegalStateException("boom"));

    LogisticsEquipmentTodoScheduler scheduler =
        new LogisticsEquipmentTodoScheduler(equipmentTodoService, orgMapper);
    setField(scheduler, "maintenanceTodoEnabled", true);
    setField(scheduler, "maintenanceTodoDays", 15);

    scheduler.createMaintenanceTodosDaily();

    verify(equipmentTodoService).saveJobLog(101L, "SCHEDULED", 15, result, "SUCCESS", null, 0L);
    verify(equipmentTodoService).saveJobLog(eq(202L), eq("SCHEDULED"), eq(15), eq(null), eq("FAILED"), eq("boom"), eq(0L));
  }

  private static Org org(Long id) {
    Org org = new Org();
    org.setId(id);
    org.setStatus(1);
    org.setIsDeleted(0);
    return org;
  }

  private static void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
