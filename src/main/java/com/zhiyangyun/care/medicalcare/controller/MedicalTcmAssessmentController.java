package com.zhiyangyun.care.medicalcare.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.health.support.ElderResolveSupport;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.medicalcare.entity.MedicalTcmAssessment;
import com.zhiyangyun.care.medicalcare.mapper.MedicalTcmAssessmentMapper;
import com.zhiyangyun.care.medicalcare.model.MedicalTcmAssessmentRequest;
import com.zhiyangyun.care.medicalcare.model.MedicalTcmAssessmentSummaryResponse;
import com.zhiyangyun.care.model.CareTaskCreateRequest;
import com.zhiyangyun.care.service.CareTaskService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medical-care/tcm-assessments")
public class MedicalTcmAssessmentController {
  private final MedicalTcmAssessmentMapper mapper;
  private final ElderResolveSupport elderResolveSupport;
  private final CareTaskService careTaskService;
  private final CareTaskDailyMapper careTaskDailyMapper;

  public MedicalTcmAssessmentController(
      MedicalTcmAssessmentMapper mapper,
      ElderResolveSupport elderResolveSupport,
      CareTaskService careTaskService,
      CareTaskDailyMapper careTaskDailyMapper) {
    this.mapper = mapper;
    this.elderResolveSupport = elderResolveSupport;
    this.careTaskService = careTaskService;
    this.careTaskDailyMapper = careTaskDailyMapper;
  }

  @GetMapping("/page")
  public Result<IPage<MedicalTcmAssessment>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String constitutionType,
      @RequestParam(required = false) String assessorName,
      @RequestParam(required = false) Integer isReassessment,
      @RequestParam(required = false) Integer familyVisible,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = buildQuery(orgId, elderId, constitutionType, assessorName, isReassessment, familyVisible, status, dateFrom, dateTo,
        keyword);
    wrapper.orderByDesc(MedicalTcmAssessment::getAssessmentDate).orderByDesc(MedicalTcmAssessment::getUpdateTime);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/summary")
  public Result<MedicalTcmAssessmentSummaryResponse> summary(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String constitutionType,
      @RequestParam(required = false) String assessorName,
      @RequestParam(required = false) Integer isReassessment,
      @RequestParam(required = false) Integer familyVisible,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String dateFrom,
      @RequestParam(required = false) String dateTo,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<MedicalTcmAssessment> baseWrapper = buildQuery(orgId, elderId, constitutionType, assessorName, isReassessment,
        familyVisible, status, dateFrom, dateTo, keyword);

    long total = mapper.selectCount(baseWrapper);
    long draft = mapper.selectCount(buildQuery(orgId, elderId, constitutionType, assessorName, isReassessment, familyVisible, "DRAFT",
        dateFrom, dateTo, keyword));
    long published = mapper.selectCount(buildQuery(orgId, elderId, constitutionType, assessorName, isReassessment, familyVisible,
        "PUBLISHED", dateFrom, dateTo, keyword));
    long reassessmentCount = mapper.selectCount(buildQuery(orgId, elderId, constitutionType, assessorName, 1, familyVisible, status,
        dateFrom, dateTo, keyword));
    long familyVisibleCount = mapper.selectCount(buildQuery(orgId, elderId, constitutionType, assessorName, isReassessment, 1, status,
        dateFrom, dateTo, keyword));

    List<MedicalTcmAssessment> rows = mapper.selectList(baseWrapper);
    long nursingTaskSuggestedCount = rows.stream()
        .filter(item -> item.getGenerateNursingTask() != null && item.getGenerateNursingTask() == 1)
        .count();
    long balancedCount = rows.stream()
        .filter(item -> "BALANCED".equals(item.getConstitutionPrimary()))
        .count();
    long biasedCount = rows.stream()
        .filter(item -> item.getConstitutionPrimary() != null && !"BALANCED".equals(item.getConstitutionPrimary()))
        .count();
    long lowConfidenceCount = rows.stream()
        .filter(item -> item.getConfidence() != null)
        .filter(item -> item.getConfidence().compareTo(BigDecimal.valueOf(60)) < 0)
        .count();

    MedicalTcmAssessmentSummaryResponse response = new MedicalTcmAssessmentSummaryResponse();
    response.setTotalCount(total);
    response.setDraftCount(draft);
    response.setPublishedCount(published);
    response.setReassessmentCount(reassessmentCount);
    response.setFamilyVisibleCount(familyVisibleCount);
    response.setNursingTaskSuggestedCount(nursingTaskSuggestedCount);
    response.setBalancedConstitutionCount(balancedCount);
    response.setBiasedConstitutionCount(biasedCount);
    response.setLowConfidenceCount(lowConfidenceCount);
    return Result.ok(response);
  }

  @PostMapping
  public Result<MedicalTcmAssessment> create(@Valid @RequestBody MedicalTcmAssessmentRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    MedicalTcmAssessment item = new MedicalTcmAssessment();
    fillFromRequest(item, request, elderId);
    item.setTenantId(orgId);
    item.setOrgId(orgId);
    item.setCreatedBy(AuthContext.getStaffId());
    item.setStatus(item.getStatus() == null || item.getStatus().isBlank() ? "DRAFT" : item.getStatus());
    item.setIsDeleted(0);
    mapper.insert(item);
    return Result.ok(item);
  }

  @PutMapping("/{id}")
  public Result<MedicalTcmAssessment> update(@PathVariable Long id, @Valid @RequestBody MedicalTcmAssessmentRequest request) {
    MedicalTcmAssessment item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item == null || (item.getIsDeleted() != null && item.getIsDeleted() == 1)
        || (orgId != null && !orgId.equals(item.getOrgId()))) {
      return Result.ok(null);
    }
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    fillFromRequest(item, request, elderId);
    mapper.updateById(item);
    return Result.ok(item);
  }

  @PostMapping("/{id}/publish")
  public Result<MedicalTcmAssessment> publish(@PathVariable Long id) {
    MedicalTcmAssessment item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item == null || (item.getIsDeleted() != null && item.getIsDeleted() == 1)
        || (orgId != null && !orgId.equals(item.getOrgId()))) {
      return Result.ok(null);
    }
    item.setStatus("PUBLISHED");
    mapper.updateById(item);
    if (item.getGenerateNursingTask() != null && item.getGenerateNursingTask() == 1) {
      createTcmCareTask(item);
    }
    return Result.ok(item);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    MedicalTcmAssessment item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item != null && (item.getIsDeleted() == null || item.getIsDeleted() == 0)
        && (orgId == null || orgId.equals(item.getOrgId()))) {
      item.setIsDeleted(1);
      mapper.updateById(item);
    }
    return Result.ok(null);
  }

  private void fillFromRequest(MedicalTcmAssessment item, MedicalTcmAssessmentRequest request, Long elderId) {
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setAssessmentDate(request.getAssessmentDate());
    item.setAssessorId(request.getAssessorId() == null ? AuthContext.getStaffId() : request.getAssessorId());
    item.setAssessorName(request.getAssessorName() == null || request.getAssessorName().isBlank() ? AuthContext.getUsername()
        : request.getAssessorName());
    item.setAssessmentScene(request.getAssessmentScene() == null || request.getAssessmentScene().isBlank() ? "ADMISSION"
        : request.getAssessmentScene());
    item.setConstitutionPrimary(request.getConstitutionPrimary());
    item.setConstitutionSecondary(request.getConstitutionSecondary());
    item.setScore(request.getScore());
    item.setConfidence(request.getConfidence());
    item.setFeatureSummary(request.getFeatureSummary());
    item.setSuggestionDiet(request.getSuggestionDiet());
    item.setSuggestionRoutine(request.getSuggestionRoutine());
    item.setSuggestionExercise(request.getSuggestionExercise());
    item.setSuggestionEmotion(request.getSuggestionEmotion());
    item.setNursingTip(request.getNursingTip());
    item.setSuggestionPoints(request.getSuggestionPoints());
    item.setQuestionnaireJson(request.getQuestionnaireJson());
    item.setIsReassessment(request.getIsReassessment() == null ? 0 : request.getIsReassessment());
    item.setFamilyVisible(request.getFamilyVisible() == null ? 0 : request.getFamilyVisible());
    item.setGenerateNursingTask(request.getGenerateNursingTask() == null ? 0 : request.getGenerateNursingTask());
    item.setStatus(request.getStatus());
  }

  private LambdaQueryWrapper<MedicalTcmAssessment> buildQuery(
      Long orgId,
      Long elderId,
      String constitutionType,
      String assessorName,
      Integer isReassessment,
      Integer familyVisible,
      String status,
      String dateFrom,
      String dateTo,
      String keyword) {
    LocalDate from = parseDate(dateFrom);
    LocalDate to = parseDate(dateTo);
    LambdaQueryWrapper<MedicalTcmAssessment> wrapper = Wrappers.lambdaQuery(MedicalTcmAssessment.class)
        .eq(MedicalTcmAssessment::getIsDeleted, 0)
        .eq(orgId != null, MedicalTcmAssessment::getOrgId, orgId)
        .eq(elderId != null, MedicalTcmAssessment::getElderId, elderId)
        .eq(assessorName != null && !assessorName.isBlank(), MedicalTcmAssessment::getAssessorName, assessorName)
        .eq(isReassessment != null, MedicalTcmAssessment::getIsReassessment, isReassessment)
        .eq(familyVisible != null, MedicalTcmAssessment::getFamilyVisible, familyVisible)
        .eq(status != null && !status.isBlank(), MedicalTcmAssessment::getStatus, status)
        .ge(from != null, MedicalTcmAssessment::getAssessmentDate, from)
        .le(to != null, MedicalTcmAssessment::getAssessmentDate, to);
    if (constitutionType != null && !constitutionType.isBlank()) {
      wrapper.and(w -> w.eq(MedicalTcmAssessment::getConstitutionPrimary, constitutionType)
          .or().eq(MedicalTcmAssessment::getConstitutionSecondary, constitutionType));
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(MedicalTcmAssessment::getElderName, keyword)
          .or().like(MedicalTcmAssessment::getSuggestionPoints, keyword)
          .or().like(MedicalTcmAssessment::getFeatureSummary, keyword)
          .or().like(MedicalTcmAssessment::getAssessorName, keyword));
    }
    return wrapper;
  }

  private LocalDate parseDate(String date) {
    if (date == null || date.isBlank()) {
      return null;
    }
    return LocalDate.parse(date);
  }

  private void createTcmCareTask(MedicalTcmAssessment item) {
    CareTaskCreateRequest request = new CareTaskCreateRequest();
    request.setElderId(item.getElderId());
    request.setTaskName("体质调养护理任务");
    request.setPlanTime(LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_DATE_TIME));
    request.setStatus("PENDING");
    Long taskId = careTaskService.createTask(item.getOrgId(), request);
    CareTaskDaily taskDaily = careTaskDailyMapper.selectById(taskId);
    if (taskDaily != null) {
      taskDaily.setSourceType("TCM_CONSTITUTION");
      taskDaily.setSourceId(item.getId());
      careTaskDailyMapper.updateById(taskDaily);
    }
  }
}
