package com.zhiyangyun.care.medicalcare.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.health.support.ElderResolveSupport;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.medicalcare.entity.MedicalFollowupPlan;
import com.zhiyangyun.care.medicalcare.entity.MedicalFollowupRecord;
import com.zhiyangyun.care.medicalcare.mapper.MedicalFollowupPlanMapper;
import com.zhiyangyun.care.medicalcare.mapper.MedicalFollowupRecordMapper;
import com.zhiyangyun.care.medicalcare.model.MedicalFollowupPlanRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalFollowupRecordRequest;
import com.zhiyangyun.care.medicalcare.service.MedicalFollowupService;
import com.zhiyangyun.care.model.CareTaskCreateRequest;
import com.zhiyangyun.care.service.CareTaskService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MedicalFollowupServiceImpl implements MedicalFollowupService {

  /** 到期随访提醒写入 care_task_daily 的来源标识。 */
  public static final String REMINDER_SOURCE_TYPE = "CHRONIC_FOLLOWUP";

  private static final Logger log = LoggerFactory.getLogger(MedicalFollowupServiceImpl.class);
  private static final int DEFAULT_FREQUENCY_DAYS = 30;
  private static final List<String> PLAN_STATUSES = List.of("ACTIVE", "PAUSED", "CLOSED");
  private static final Map<String, String> DISEASE_TEXT = Map.of(
      "HYPERTENSION", "高血压",
      "DIABETES", "糖尿病",
      "COPD", "慢阻肺",
      "CHD", "冠心病",
      "STROKE", "脑卒中",
      "OTHER", "其他慢病");

  private final MedicalFollowupPlanMapper planMapper;
  private final MedicalFollowupRecordMapper recordMapper;
  private final CareTaskService careTaskService;
  private final CareTaskDailyMapper careTaskDailyMapper;
  private final ElderResolveSupport elderResolveSupport;

  public MedicalFollowupServiceImpl(
      MedicalFollowupPlanMapper planMapper,
      MedicalFollowupRecordMapper recordMapper,
      CareTaskService careTaskService,
      CareTaskDailyMapper careTaskDailyMapper,
      ElderResolveSupport elderResolveSupport) {
    this.planMapper = planMapper;
    this.recordMapper = recordMapper;
    this.careTaskService = careTaskService;
    this.careTaskDailyMapper = careTaskDailyMapper;
    this.elderResolveSupport = elderResolveSupport;
  }

  @Override
  public IPage<MedicalFollowupPlan> pagePlans(Long orgId, int pageNo, int pageSize, Long elderId, String diseaseType,
      String status, Boolean dueOnly) {
    var wrapper = Wrappers.lambdaQuery(MedicalFollowupPlan.class)
        .eq(MedicalFollowupPlan::getIsDeleted, 0)
        .eq(orgId != null, MedicalFollowupPlan::getOrgId, orgId)
        .eq(elderId != null, MedicalFollowupPlan::getElderId, elderId)
        .eq(StringUtils.hasText(diseaseType), MedicalFollowupPlan::getDiseaseType, diseaseType)
        .eq(StringUtils.hasText(status), MedicalFollowupPlan::getStatus, status);
    if (Boolean.TRUE.equals(dueOnly)) {
      wrapper.eq(MedicalFollowupPlan::getStatus, "ACTIVE")
          .le(MedicalFollowupPlan::getNextFollowupDate, LocalDate.now());
    }
    wrapper.orderByAsc(MedicalFollowupPlan::getNextFollowupDate).orderByDesc(MedicalFollowupPlan::getId);
    return planMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  @Transactional
  public MedicalFollowupPlan createPlan(Long orgId, MedicalFollowupPlanRequest request) {
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    MedicalFollowupPlan plan = new MedicalFollowupPlan();
    plan.setTenantId(orgId);
    plan.setOrgId(orgId);
    plan.setElderId(elderId);
    plan.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    fillPlanFromRequest(plan, request);
    if (plan.getNextFollowupDate() == null) {
      plan.setNextFollowupDate(LocalDate.now().plusDays(plan.getFrequencyDays()));
    }
    plan.setStatus("ACTIVE");
    plan.setCreatedBy(AuthContext.getStaffId());
    plan.setIsDeleted(0);
    planMapper.insert(plan);
    return plan;
  }

  @Override
  @Transactional
  public MedicalFollowupPlan updatePlan(Long orgId, Long planId, MedicalFollowupPlanRequest request) {
    MedicalFollowupPlan plan = loadPlan(orgId, planId);
    fillPlanFromRequest(plan, request);
    planMapper.updateById(plan);
    return plan;
  }

  @Override
  @Transactional
  public MedicalFollowupPlan updatePlanStatus(Long orgId, Long planId, String status) {
    if (!PLAN_STATUSES.contains(status)) {
      throw new IllegalArgumentException("无效的随访计划状态: " + status);
    }
    MedicalFollowupPlan plan = loadPlan(orgId, planId);
    plan.setStatus(status);
    planMapper.updateById(plan);
    return plan;
  }

  @Override
  public IPage<MedicalFollowupRecord> pageRecords(Long orgId, int pageNo, int pageSize, Long planId, Long elderId) {
    var wrapper = Wrappers.lambdaQuery(MedicalFollowupRecord.class)
        .eq(MedicalFollowupRecord::getIsDeleted, 0)
        .eq(orgId != null, MedicalFollowupRecord::getOrgId, orgId)
        .eq(planId != null, MedicalFollowupRecord::getPlanId, planId)
        .eq(elderId != null, MedicalFollowupRecord::getElderId, elderId)
        .orderByDesc(MedicalFollowupRecord::getFollowupDate)
        .orderByDesc(MedicalFollowupRecord::getId);
    return recordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  @Transactional
  public MedicalFollowupRecord createRecord(Long orgId, MedicalFollowupRecordRequest request) {
    MedicalFollowupPlan plan = loadPlan(orgId, request.getPlanId());
    LocalDate followupDate = request.getFollowupDate() != null ? request.getFollowupDate() : LocalDate.now();
    int frequencyDays = plan.getFrequencyDays() == null || plan.getFrequencyDays() <= 0
        ? DEFAULT_FREQUENCY_DAYS
        : plan.getFrequencyDays();
    LocalDate nextDate = request.getNextFollowupDate() != null
        ? request.getNextFollowupDate()
        : followupDate.plusDays(frequencyDays);

    MedicalFollowupRecord record = new MedicalFollowupRecord();
    record.setTenantId(plan.getTenantId());
    record.setOrgId(plan.getOrgId());
    record.setPlanId(plan.getId());
    record.setElderId(plan.getElderId());
    record.setElderName(plan.getElderName());
    record.setFollowupDate(followupDate);
    record.setVitalJson(request.getVitalJson());
    record.setHealthDataId(request.getHealthDataId());
    record.setMedicationCompliance(request.getMedicationCompliance());
    record.setAssessmentLevel(StringUtils.hasText(request.getAssessmentLevel())
        ? request.getAssessmentLevel()
        : "CONTROLLED");
    record.setAssessment(request.getAssessment());
    record.setNextFollowupDate(nextDate);
    record.setDoctorId(AuthContext.getStaffId());
    record.setDoctorName(AuthContext.getUsername());
    record.setRemark(request.getRemark());
    record.setCreatedBy(AuthContext.getStaffId());
    record.setIsDeleted(0);
    recordMapper.insert(record);

    plan.setLastFollowupDate(followupDate);
    plan.setNextFollowupDate(nextDate);
    planMapper.updateById(plan);
    return record;
  }

  @Override
  public List<MedicalFollowupPlan> listDuePlans(Long orgId, LocalDate date) {
    LocalDate target = date != null ? date : LocalDate.now();
    return planMapper.selectList(Wrappers.lambdaQuery(MedicalFollowupPlan.class)
        .eq(MedicalFollowupPlan::getIsDeleted, 0)
        .eq(orgId != null, MedicalFollowupPlan::getOrgId, orgId)
        .eq(MedicalFollowupPlan::getStatus, "ACTIVE")
        .isNotNull(MedicalFollowupPlan::getNextFollowupDate)
        .le(MedicalFollowupPlan::getNextFollowupDate, target)
        .orderByAsc(MedicalFollowupPlan::getNextFollowupDate));
  }

  @Override
  @Transactional
  public int generateDueReminders(LocalDate date) {
    LocalDate target = date != null ? date : LocalDate.now();
    List<MedicalFollowupPlan> duePlans = listDuePlans(null, target);
    int created = 0;
    for (MedicalFollowupPlan plan : duePlans) {
      if (hasPendingReminder(plan)) {
        continue;
      }
      try {
        createReminderTask(plan, target);
        created++;
      } catch (Exception exception) {
        log.warn("生成慢病随访待办失败 planId={}, elderId={}", plan.getId(), plan.getElderId(), exception);
      }
    }
    return created;
  }

  private boolean hasPendingReminder(MedicalFollowupPlan plan) {
    return careTaskDailyMapper.selectOne(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(CareTaskDaily::getOrgId, plan.getOrgId())
        .eq(CareTaskDaily::getSourceType, REMINDER_SOURCE_TYPE)
        .eq(CareTaskDaily::getSourceId, plan.getId())
        .notIn(CareTaskDaily::getStatus, "DONE", "COMPLETED", "CANCELED")
        .last("LIMIT 1")) != null;
  }

  private void createReminderTask(MedicalFollowupPlan plan, LocalDate date) {
    String diseaseText = DISEASE_TEXT.getOrDefault(plan.getDiseaseType(), plan.getDiseaseType());
    CareTaskCreateRequest request = new CareTaskCreateRequest();
    request.setElderId(plan.getElderId());
    request.setTaskName("慢病随访：" + diseaseText);
    request.setPlanTime(date.atTime(9, 0).format(DateTimeFormatter.ISO_DATE_TIME));
    request.setStatus("PENDING");
    Long taskId = careTaskService.createTask(plan.getOrgId(), request);
    CareTaskDaily taskDaily = careTaskDailyMapper.selectById(taskId);
    if (taskDaily != null) {
      taskDaily.setSourceType(REMINDER_SOURCE_TYPE);
      taskDaily.setSourceId(plan.getId());
      careTaskDailyMapper.updateById(taskDaily);
    }
  }

  private void fillPlanFromRequest(MedicalFollowupPlan plan, MedicalFollowupPlanRequest request) {
    plan.setDiseaseType(StringUtils.hasText(request.getDiseaseType()) ? request.getDiseaseType() : "HYPERTENSION");
    plan.setPlanName(request.getPlanName());
    plan.setFrequencyDays(request.getFrequencyDays() == null || request.getFrequencyDays() <= 0
        ? DEFAULT_FREQUENCY_DAYS
        : request.getFrequencyDays());
    plan.setTargetIndicators(request.getTargetIndicators());
    if (request.getNextFollowupDate() != null) {
      plan.setNextFollowupDate(request.getNextFollowupDate());
    }
    plan.setDoctorId(request.getDoctorId() != null ? request.getDoctorId() : AuthContext.getStaffId());
    plan.setDoctorName(StringUtils.hasText(request.getDoctorName()) ? request.getDoctorName() : AuthContext.getUsername());
    plan.setRemark(request.getRemark());
  }

  private MedicalFollowupPlan loadPlan(Long orgId, Long planId) {
    MedicalFollowupPlan plan = planMapper.selectById(planId);
    if (plan == null || Integer.valueOf(1).equals(plan.getIsDeleted())) {
      throw new IllegalArgumentException("随访计划不存在: " + planId);
    }
    if (orgId != null && !orgId.equals(plan.getOrgId())) {
      throw new IllegalArgumentException("无权操作其他机构的随访计划");
    }
    return plan;
  }
}
