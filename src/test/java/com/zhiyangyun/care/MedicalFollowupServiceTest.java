package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.medicalcare.entity.MedicalFollowupPlan;
import com.zhiyangyun.care.medicalcare.entity.MedicalFollowupRecord;
import com.zhiyangyun.care.medicalcare.mapper.MedicalFollowupPlanMapper;
import com.zhiyangyun.care.medicalcare.model.MedicalFollowupPlanRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalFollowupRecordRequest;
import com.zhiyangyun.care.medicalcare.service.MedicalFollowupService;
import com.zhiyangyun.care.medicalcare.service.impl.MedicalFollowupServiceImpl;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MedicalFollowupServiceTest {

  private static final Long ORG_ID = 1L;
  private static final Long ELDER_ID = 4001L;

  @Autowired
  private MedicalFollowupService followupService;

  @Autowired
  private MedicalFollowupPlanMapper planMapper;

  @Autowired
  private CareTaskDailyMapper careTaskDailyMapper;

  @Test
  void createRecord_autoSchedulesNextFollowupDate_andSyncsPlan() {
    LocalDate today = LocalDate.now();
    MedicalFollowupPlanRequest planRequest = new MedicalFollowupPlanRequest();
    planRequest.setElderId(ELDER_ID);
    planRequest.setDiseaseType("DIABETES");
    planRequest.setPlanName("糖尿病随访");
    planRequest.setFrequencyDays(14);
    planRequest.setNextFollowupDate(today);
    planRequest.setTargetIndicators("空腹血糖<7.0mmol/L");
    MedicalFollowupPlan plan = followupService.createPlan(ORG_ID, planRequest);
    assertNotNull(plan.getId());
    assertEquals("ACTIVE", plan.getStatus());

    MedicalFollowupRecordRequest recordRequest = new MedicalFollowupRecordRequest();
    recordRequest.setPlanId(plan.getId());
    recordRequest.setFollowupDate(today);
    recordRequest.setVitalJson("{\"fbg\":6.2}");
    recordRequest.setMedicationCompliance("GOOD");
    recordRequest.setAssessmentLevel("CONTROLLED");
    recordRequest.setAssessment("血糖控制达标");
    MedicalFollowupRecord record = followupService.createRecord(ORG_ID, recordRequest);

    assertEquals(today.plusDays(14), record.getNextFollowupDate());
    MedicalFollowupPlan updated = planMapper.selectById(plan.getId());
    assertEquals(today, updated.getLastFollowupDate());
    assertEquals(today.plusDays(14), updated.getNextFollowupDate());
  }

  @Test
  void generateDueReminders_createsCareTaskOnceForDuePlan() {
    LocalDate today = LocalDate.now();
    MedicalFollowupPlanRequest planRequest = new MedicalFollowupPlanRequest();
    planRequest.setElderId(ELDER_ID);
    planRequest.setDiseaseType("HYPERTENSION");
    planRequest.setFrequencyDays(30);
    planRequest.setNextFollowupDate(today.minusDays(1));
    MedicalFollowupPlan plan = followupService.createPlan(ORG_ID, planRequest);

    int created = followupService.generateDueReminders(today);
    assertTrue(created >= 1);
    assertEquals(1L, countReminderTasks(plan.getId()));

    // 再次生成不重复建待办
    followupService.generateDueReminders(today);
    assertEquals(1L, countReminderTasks(plan.getId()));
  }

  private long countReminderTasks(Long planId) {
    Long count = careTaskDailyMapper.selectCount(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(CareTaskDaily::getSourceType, MedicalFollowupServiceImpl.REMINDER_SOURCE_TYPE)
        .eq(CareTaskDaily::getSourceId, planId));
    return count == null ? 0L : count;
  }
}
