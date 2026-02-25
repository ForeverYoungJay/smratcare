package com.zhiyangyun.care.assessment.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.assessment.entity.AssessmentRecord;
import com.zhiyangyun.care.assessment.entity.AssessmentReminderLog;
import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.assessment.mapper.AssessmentReminderLogMapper;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AssessmentReminderScheduler {
  private final AssessmentRecordMapper recordMapper;
  private final AssessmentReminderLogMapper reminderLogMapper;
  private final OaTodoMapper todoMapper;

  public AssessmentReminderScheduler(AssessmentRecordMapper recordMapper,
      AssessmentReminderLogMapper reminderLogMapper,
      OaTodoMapper todoMapper) {
    this.recordMapper = recordMapper;
    this.reminderLogMapper = reminderLogMapper;
    this.todoMapper = todoMapper;
  }

  @Scheduled(cron = "0 30 8 * * ?")
  public void createDueAssessmentTodos() {
    LocalDate today = LocalDate.now();
    LocalDate threshold = today.plusDays(3);

    List<AssessmentRecord> records = recordMapper.selectList(Wrappers.lambdaQuery(AssessmentRecord.class)
        .eq(AssessmentRecord::getIsDeleted, 0)
        .eq(AssessmentRecord::getStatus, "COMPLETED")
        .isNotNull(AssessmentRecord::getNextAssessmentDate)
        .between(AssessmentRecord::getNextAssessmentDate, today, threshold));

    for (AssessmentRecord record : records) {
      Long orgId = record.getOrgId();
      if (orgId == null) {
        continue;
      }
      AssessmentReminderLog exists = reminderLogMapper.selectOne(Wrappers.lambdaQuery(AssessmentReminderLog.class)
          .eq(AssessmentReminderLog::getIsDeleted, 0)
          .eq(AssessmentReminderLog::getOrgId, orgId)
          .eq(AssessmentReminderLog::getRecordId, record.getId())
          .eq(AssessmentReminderLog::getRemindDate, today)
          .last("LIMIT 1"));
      if (exists != null) {
        continue;
      }

      OaTodo todo = new OaTodo();
      todo.setTenantId(record.getTenantId() == null ? orgId : record.getTenantId());
      todo.setOrgId(orgId);
      todo.setTitle("评估复评提醒 - " + record.getElderName());
      todo.setContent("评估类型：" + record.getAssessmentType() + "，计划复评日期：" + record.getNextAssessmentDate());
      todo.setDueTime(LocalDateTime.of(today, LocalTime.of(17, 0)));
      todo.setStatus("OPEN");
      todo.setAssigneeId(record.getAssessorId() == null ? record.getCreatedBy() : record.getAssessorId());
      todo.setAssigneeName(record.getAssessorName());
      todo.setCreatedBy(0L);
      todoMapper.insert(todo);

      AssessmentReminderLog log = new AssessmentReminderLog();
      log.setTenantId(todo.getTenantId());
      log.setOrgId(orgId);
      log.setRecordId(record.getId());
      log.setRemindDate(today);
      log.setTodoId(todo.getId());
      log.setStatus("CREATED");
      log.setIsDeleted(0);
      reminderLogMapper.insert(log);
    }
  }
}
