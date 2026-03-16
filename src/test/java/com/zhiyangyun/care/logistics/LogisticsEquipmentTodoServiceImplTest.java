package com.zhiyangyun.care.logistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.logistics.entity.LogisticsEquipmentArchive;
import com.zhiyangyun.care.logistics.mapper.LogisticsEquipmentArchiveMapper;
import com.zhiyangyun.care.logistics.mapper.LogisticsMaintenanceTodoJobLogMapper;
import com.zhiyangyun.care.logistics.model.LogisticsMaintenanceTodoGenerateResult;
import com.zhiyangyun.care.logistics.service.impl.LogisticsEquipmentTodoServiceImpl;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogisticsEquipmentTodoServiceImplTest {

  @Mock
  private LogisticsEquipmentArchiveMapper equipmentMapper;

  @Mock
  private LogisticsMaintenanceTodoJobLogMapper jobLogMapper;

  @Mock
  private OaTodoMapper oaTodoMapper;

  @Mock
  private OaTaskMapper oaTaskMapper;

  @Mock
  private StaffMapper staffMapper;

  @Test
  void shouldAssignTodoToResolvedMaintainer() {
    LogisticsEquipmentArchive equipment = new LogisticsEquipmentArchive();
    equipment.setId(1L);
    equipment.setOrgId(10L);
    equipment.setEquipmentCode("EQ-1");
    equipment.setEquipmentName("供氧机");
    equipment.setMaintainerName("alice");
    equipment.setLocation("2F");
    equipment.setNextMaintainedAt(LocalDateTime.now().plusDays(3));
    equipment.setStatus("ENABLED");

    StaffAccount assignee = new StaffAccount();
    assignee.setId(88L);
    assignee.setOrgId(10L);
    assignee.setUsername("alice");
    assignee.setRealName("Alice Zhang");
    assignee.setStatus(1);
    assignee.setIsDeleted(0);

    when(equipmentMapper.selectList(any())).thenReturn(List.of(equipment));
    when(oaTodoMapper.selectOne(any())).thenReturn(null);
    when(staffMapper.selectOne(any())).thenReturn(assignee);
    when(oaTaskMapper.selectOne(any())).thenReturn(null);
    when(oaTodoMapper.insert(any())).thenAnswer(invocation -> {
      OaTodo todo = invocation.getArgument(0);
      todo.setId(1001L);
      return 1;
    });

    LogisticsEquipmentTodoServiceImpl service = new LogisticsEquipmentTodoServiceImpl(
        equipmentMapper,
        jobLogMapper,
        oaTodoMapper,
        oaTaskMapper,
        staffMapper);

    LogisticsMaintenanceTodoGenerateResult result =
        service.generateMaintenanceTodos(10L, 30, 99L);

    assertEquals(1L, result.getCreatedCount());
    assertEquals(0L, result.getSkippedCount());

    ArgumentCaptor<OaTodo> todoCaptor = ArgumentCaptor.forClass(OaTodo.class);
    verify(oaTodoMapper).insert(todoCaptor.capture());
    OaTodo savedTodo = todoCaptor.getValue();
    assertEquals(88L, savedTodo.getAssigneeId());
    assertEquals("Alice Zhang", savedTodo.getAssigneeName());
    assertEquals(99L, savedTodo.getCreatedBy());

    ArgumentCaptor<OaTask> taskCaptor = ArgumentCaptor.forClass(OaTask.class);
    verify(oaTaskMapper).insert(taskCaptor.capture());
    OaTask savedTask = taskCaptor.getValue();
    assertNotNull(savedTask);
    assertEquals(88L, savedTask.getAssigneeId());
    assertEquals("Alice Zhang", savedTask.getAssigneeName());
  }
}
