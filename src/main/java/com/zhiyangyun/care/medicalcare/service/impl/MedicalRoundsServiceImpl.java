package com.zhiyangyun.care.medicalcare.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.emr.entity.EmrRecord;
import com.zhiyangyun.care.emr.mapper.EmrRecordMapper;
import com.zhiyangyun.care.health.support.ElderResolveSupport;
import com.zhiyangyun.care.medicalcare.entity.MedicalRoundsPlan;
import com.zhiyangyun.care.medicalcare.entity.MedicalRoundsRecord;
import com.zhiyangyun.care.medicalcare.mapper.MedicalRoundsPlanMapper;
import com.zhiyangyun.care.medicalcare.mapper.MedicalRoundsRecordMapper;
import com.zhiyangyun.care.medicalcare.model.MedicalRoundsPlanRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalRoundsRecordRequest;
import com.zhiyangyun.care.medicalcare.service.MedicalRoundsService;
import com.zhiyangyun.care.medorder.model.MedicalOrderRequest;
import com.zhiyangyun.care.medorder.service.MedicalOrderService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MedicalRoundsServiceImpl implements MedicalRoundsService {

  private static final List<String> PLAN_STATUSES = List.of("PLANNED", "IN_PROGRESS", "DONE", "CANCELED");

  private final MedicalRoundsPlanMapper planMapper;
  private final MedicalRoundsRecordMapper recordMapper;
  private final EmrRecordMapper emrRecordMapper;
  private final MedicalOrderService medicalOrderService;
  private final ElderResolveSupport elderResolveSupport;

  public MedicalRoundsServiceImpl(
      MedicalRoundsPlanMapper planMapper,
      MedicalRoundsRecordMapper recordMapper,
      EmrRecordMapper emrRecordMapper,
      MedicalOrderService medicalOrderService,
      ElderResolveSupport elderResolveSupport) {
    this.planMapper = planMapper;
    this.recordMapper = recordMapper;
    this.emrRecordMapper = emrRecordMapper;
    this.medicalOrderService = medicalOrderService;
    this.elderResolveSupport = elderResolveSupport;
  }

  @Override
  public IPage<MedicalRoundsPlan> pagePlans(Long orgId, int pageNo, int pageSize, Long doctorId, String status,
      String dateFrom, String dateTo) {
    LocalDate from = parseDate(dateFrom);
    LocalDate to = parseDate(dateTo);
    var wrapper = Wrappers.lambdaQuery(MedicalRoundsPlan.class)
        .eq(MedicalRoundsPlan::getIsDeleted, 0)
        .eq(orgId != null, MedicalRoundsPlan::getOrgId, orgId)
        .eq(doctorId != null, MedicalRoundsPlan::getDoctorId, doctorId)
        .eq(StringUtils.hasText(status), MedicalRoundsPlan::getStatus, status)
        .ge(from != null, MedicalRoundsPlan::getPlanDate, from)
        .le(to != null, MedicalRoundsPlan::getPlanDate, to)
        .orderByDesc(MedicalRoundsPlan::getPlanDate)
        .orderByDesc(MedicalRoundsPlan::getId);
    return planMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  @Transactional
  public MedicalRoundsPlan createPlan(Long orgId, MedicalRoundsPlanRequest request) {
    MedicalRoundsPlan plan = new MedicalRoundsPlan();
    plan.setTenantId(orgId);
    plan.setOrgId(orgId);
    plan.setPlanDate(request.getPlanDate());
    plan.setTimeSlot(request.getTimeSlot());
    plan.setDoctorId(request.getDoctorId() != null ? request.getDoctorId() : AuthContext.getStaffId());
    plan.setDoctorName(StringUtils.hasText(request.getDoctorName()) ? request.getDoctorName() : AuthContext.getUsername());
    plan.setArea(request.getArea());
    plan.setElderScope(StringUtils.hasText(request.getElderScope()) ? request.getElderScope() : "AREA");
    plan.setElderIdsJson(request.getElderIdsJson());
    plan.setStatus("PLANNED");
    plan.setRemark(request.getRemark());
    plan.setCreatedBy(AuthContext.getStaffId());
    plan.setIsDeleted(0);
    planMapper.insert(plan);
    return plan;
  }

  @Override
  @Transactional
  public MedicalRoundsPlan updatePlanStatus(Long orgId, Long planId, String status) {
    if (!PLAN_STATUSES.contains(status)) {
      throw new IllegalArgumentException("无效的巡诊计划状态: " + status);
    }
    MedicalRoundsPlan plan = loadPlan(orgId, planId);
    plan.setStatus(status);
    planMapper.updateById(plan);
    return plan;
  }

  @Override
  public IPage<MedicalRoundsRecord> pageRecords(Long orgId, int pageNo, int pageSize, Long planId, Long elderId,
      String resultLevel) {
    var wrapper = Wrappers.lambdaQuery(MedicalRoundsRecord.class)
        .eq(MedicalRoundsRecord::getIsDeleted, 0)
        .eq(orgId != null, MedicalRoundsRecord::getOrgId, orgId)
        .eq(planId != null, MedicalRoundsRecord::getPlanId, planId)
        .eq(elderId != null, MedicalRoundsRecord::getElderId, elderId)
        .eq(StringUtils.hasText(resultLevel), MedicalRoundsRecord::getResultLevel, resultLevel)
        .orderByDesc(MedicalRoundsRecord::getRoundTime)
        .orderByDesc(MedicalRoundsRecord::getId);
    return recordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  @Transactional
  public MedicalRoundsRecord createRecord(Long orgId, MedicalRoundsRecordRequest request) {
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    LocalDateTime roundTime = request.getRoundTime() != null ? request.getRoundTime() : LocalDateTime.now();

    MedicalRoundsRecord record = new MedicalRoundsRecord();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    record.setPlanId(request.getPlanId());
    record.setElderId(elderId);
    record.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    record.setRoundTime(roundTime);
    record.setFindings(request.getFindings());
    record.setHandleOpinion(request.getHandleOpinion());
    record.setResultLevel(StringUtils.hasText(request.getResultLevel()) ? request.getResultLevel() : "NORMAL");
    record.setDoctorId(AuthContext.getStaffId());
    record.setDoctorName(AuthContext.getUsername());
    record.setRemark(request.getRemark());
    record.setCreatedBy(AuthContext.getStaffId());
    record.setIsDeleted(0);

    if (Integer.valueOf(1).equals(request.getGenerateEmr())) {
      EmrRecord emr = new EmrRecord();
      emr.setOrgId(orgId);
      emr.setElderId(elderId);
      emr.setRecordType("PROGRESS");
      emr.setVisitDate(roundTime.toLocalDate());
      emr.setPhysicalExam(request.getFindings());
      emr.setDiagnosis(request.getDiagnosis());
      emr.setTreatmentPlan(request.getHandleOpinion());
      emr.setDoctorId(AuthContext.getStaffId());
      emr.setDoctorName(AuthContext.getUsername());
      emr.setStatus("ACTIVE");
      emr.setRemark("巡诊生成");
      emr.setCreatedBy(AuthContext.getStaffId());
      emrRecordMapper.insert(emr);
      record.setEmrRecordId(emr.getId());
    }

    if (StringUtils.hasText(request.getOrderContent())) {
      MedicalOrderRequest orderRequest = new MedicalOrderRequest();
      orderRequest.setElderId(elderId);
      orderRequest.setEmrId(record.getEmrRecordId());
      orderRequest.setOrderType(StringUtils.hasText(request.getOrderType()) ? request.getOrderType() : "TEMPORARY");
      orderRequest.setCategory(StringUtils.hasText(request.getOrderCategory()) ? request.getOrderCategory() : "DRUG");
      orderRequest.setContent(request.getOrderContent());
      orderRequest.setDosage(request.getOrderDosage());
      orderRequest.setFrequency(request.getOrderFrequency());
      orderRequest.setStartTime(roundTime);
      orderRequest.setRemark("巡诊医嘱建议");
      record.setMedicalOrderId(medicalOrderService.createOrder(orderRequest));
    }

    recordMapper.insert(record);

    if (request.getPlanId() != null) {
      MedicalRoundsPlan plan = planMapper.selectById(request.getPlanId());
      if (plan != null && !Integer.valueOf(1).equals(plan.getIsDeleted())
          && (orgId == null || orgId.equals(plan.getOrgId()))
          && "PLANNED".equals(plan.getStatus())) {
        plan.setStatus("IN_PROGRESS");
        planMapper.updateById(plan);
      }
    }
    return record;
  }

  private MedicalRoundsPlan loadPlan(Long orgId, Long planId) {
    MedicalRoundsPlan plan = planMapper.selectById(planId);
    if (plan == null || Integer.valueOf(1).equals(plan.getIsDeleted())) {
      throw new IllegalArgumentException("巡诊计划不存在: " + planId);
    }
    if (orgId != null && !orgId.equals(plan.getOrgId())) {
      throw new IllegalArgumentException("无权操作其他机构的巡诊计划");
    }
    return plan;
  }

  private LocalDate parseDate(String date) {
    if (date == null || date.isBlank()) {
      return null;
    }
    return LocalDate.parse(date);
  }
}
