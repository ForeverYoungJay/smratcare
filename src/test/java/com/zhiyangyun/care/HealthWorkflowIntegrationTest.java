package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.health.entity.HealthInspection;
import com.zhiyangyun.care.health.entity.HealthMedicationRegistration;
import com.zhiyangyun.care.health.entity.HealthMedicationSetting;
import com.zhiyangyun.care.health.entity.HealthMedicationTask;
import com.zhiyangyun.care.health.entity.HealthNursingLog;
import com.zhiyangyun.care.health.mapper.HealthInspectionMapper;
import com.zhiyangyun.care.health.mapper.HealthMedicationTaskMapper;
import com.zhiyangyun.care.health.mapper.HealthNursingLogMapper;
import com.zhiyangyun.care.health.service.HealthInspectionClosureService;
import com.zhiyangyun.care.health.service.HealthMedicationTaskService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class HealthWorkflowIntegrationTest {
  @Autowired
  private HealthMedicationTaskService medicationTaskService;
  @Autowired
  private HealthMedicationTaskMapper medicationTaskMapper;
  @Autowired
  private HealthInspectionClosureService inspectionClosureService;
  @Autowired
  private HealthInspectionMapper inspectionMapper;
  @Autowired
  private HealthNursingLogMapper nursingLogMapper;

  @Test
  void medication_setting_generates_pending_task_and_registration_marks_done() {
    LocalDate today = LocalDate.now();

    HealthMedicationSetting setting = new HealthMedicationSetting();
    setting.setId(91001L);
    setting.setTenantId(1L);
    setting.setOrgId(1L);
    setting.setElderId(200L);
    setting.setElderName("Elder One");
    setting.setDrugId(61001L);
    setting.setDrugName("阿司匹林");
    setting.setFrequency("QD");
    setting.setCreatedBy(500L);

    medicationTaskService.generateTasksForSetting(setting, today);

    HealthMedicationTask task = medicationTaskMapper.selectOne(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(HealthMedicationTask::getOrgId, 1L)
        .eq(HealthMedicationTask::getSettingId, 91001L)
        .eq(HealthMedicationTask::getTaskDate, today)
        .last("LIMIT 1"));
    assertNotNull(task);
    assertEquals("PENDING", task.getStatus());

    HealthMedicationRegistration registration = new HealthMedicationRegistration();
    registration.setId(92001L);
    registration.setOrgId(1L);
    registration.setElderId(200L);
    registration.setDrugId(61001L);
    registration.setDrugName("阿司匹林");
    registration.setRegisterTime(today.atTime(9, 30));
    registration.setDosageTaken(BigDecimal.valueOf(1));

    medicationTaskService.completeTaskByRegistration(registration);

    HealthMedicationTask done = medicationTaskMapper.selectById(task.getId());
    assertEquals("DONE", done.getStatus());
    assertEquals(registration.getId(), done.getRegistrationId());
  }

  @Test
  void abnormal_inspection_creates_followup_log_and_done_log_closes_inspection() {
    HealthInspection inspection = new HealthInspection();
    inspection.setId(93001L);
    inspection.setTenantId(1L);
    inspection.setOrgId(1L);
    inspection.setElderId(200L);
    inspection.setElderName("Elder One");
    inspection.setInspectionDate(LocalDate.now());
    inspection.setInspectionItem("晨间巡检");
    inspection.setResult("体温异常");
    inspection.setStatus("ABNORMAL");
    inspection.setInspectorName("护士A");
    inspectionMapper.insert(inspection);

    inspectionClosureService.syncFromInspection(inspection, 500L);

    HealthNursingLog log = nursingLogMapper.selectOne(Wrappers.lambdaQuery(HealthNursingLog.class)
        .eq(HealthNursingLog::getIsDeleted, 0)
        .eq(HealthNursingLog::getOrgId, 1L)
        .eq(HealthNursingLog::getSourceInspectionId, inspection.getId())
        .last("LIMIT 1"));
    assertNotNull(log);
    assertEquals("PENDING", log.getStatus());

    log.setStatus("DONE");
    log.setLogTime(LocalDateTime.now());
    nursingLogMapper.updateById(log);
    inspectionClosureService.syncFromNursingLog(log);

    HealthInspection closed = inspectionMapper.selectById(inspection.getId());
    assertEquals("CLOSED", closed.getStatus());

    long count = nursingLogMapper.selectCount(Wrappers.lambdaQuery(HealthNursingLog.class)
        .eq(HealthNursingLog::getIsDeleted, 0)
        .eq(HealthNursingLog::getSourceInspectionId, inspection.getId()));
    assertTrue(count >= 1);
  }
}
