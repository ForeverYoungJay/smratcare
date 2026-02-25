package com.zhiyangyun.care.health.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.health.entity.HealthMedicationRegistration;
import com.zhiyangyun.care.health.entity.HealthMedicationTask;
import com.zhiyangyun.care.health.mapper.HealthMedicationRegistrationMapper;
import com.zhiyangyun.care.health.mapper.HealthMedicationTaskMapper;
import com.zhiyangyun.care.health.model.HealthMedicationRegistrationRequest;
import com.zhiyangyun.care.health.model.HealthMedicationRegistrationSummaryResponse;
import com.zhiyangyun.care.health.model.HealthNameCountStatItem;
import com.zhiyangyun.care.health.service.HealthMedicationTaskService;
import com.zhiyangyun.care.health.support.ElderResolveSupport;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
@RequestMapping("/api/health/medication/registration")
public class HealthMedicationRegistrationController {
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private final HealthMedicationRegistrationMapper mapper;
  private final HealthMedicationTaskMapper taskMapper;
  private final ElderResolveSupport elderResolveSupport;
  private final HealthMedicationTaskService medicationTaskService;

  public HealthMedicationRegistrationController(
      HealthMedicationRegistrationMapper mapper,
      HealthMedicationTaskMapper taskMapper,
      ElderResolveSupport elderResolveSupport,
      HealthMedicationTaskService medicationTaskService) {
    this.mapper = mapper;
    this.taskMapper = taskMapper;
    this.elderResolveSupport = elderResolveSupport;
    this.medicationTaskService = medicationTaskService;
  }

  @GetMapping("/page")
  public Result<IPage<HealthMedicationRegistration>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String drugName,
      @RequestParam(required = false) String nurseName,
      @RequestParam(required = false) String registerFrom,
      @RequestParam(required = false) String registerTo,
      @RequestParam(required = false) String keyword) {
    var wrapper = buildQuery(
        AuthContext.getOrgId(),
        elderId,
        drugName,
        nurseName,
        registerFrom,
        registerTo,
        keyword);
    wrapper.orderByDesc(HealthMedicationRegistration::getRegisterTime);
    return Result.ok(mapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @GetMapping("/summary")
  public Result<HealthMedicationRegistrationSummaryResponse> summary(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String drugName,
      @RequestParam(required = false) String nurseName,
      @RequestParam(required = false) String registerFrom,
      @RequestParam(required = false) String registerTo,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LocalDateTime from = parseDateTime(registerFrom);
    LocalDateTime to = parseDateTime(registerTo);
    var wrapper = buildQuery(orgId, elderId, drugName, nurseName, registerFrom, registerTo, keyword);
    long totalCount = mapper.selectCount(wrapper);

    BigDecimal totalDosage = mapper.selectList(wrapper).stream()
        .map(HealthMedicationRegistration::getDosageTaken)
        .filter(v -> v != null)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    long todayCount = mapper.selectCount(buildQuery(
        orgId,
        elderId,
        drugName,
        nurseName,
        LocalDate.now().atStartOfDay().format(DATE_TIME_FORMATTER),
        LocalDate.now().plusDays(1).atStartOfDay().minusSeconds(1).format(DATE_TIME_FORMATTER),
        keyword));
    long doneTaskCount = taskMapper.selectCount(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationTask::getOrgId, orgId)
        .eq(elderId != null, HealthMedicationTask::getElderId, elderId)
        .eq(drugName != null && !drugName.isBlank(), HealthMedicationTask::getDrugName, drugName)
        .eq(HealthMedicationTask::getStatus, "DONE")
        .ge(from != null, HealthMedicationTask::getPlannedTime, from)
        .le(to != null, HealthMedicationTask::getPlannedTime, to));
    long pendingTaskCount = taskMapper.selectCount(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationTask::getOrgId, orgId)
        .eq(elderId != null, HealthMedicationTask::getElderId, elderId)
        .eq(drugName != null && !drugName.isBlank(), HealthMedicationTask::getDrugName, drugName)
        .eq(HealthMedicationTask::getStatus, "PENDING")
        .ge(from != null, HealthMedicationTask::getPlannedTime, from)
        .le(to != null, HealthMedicationTask::getPlannedTime, to));

    var nurseStatWrapper = Wrappers.query(HealthMedicationRegistration.class)
        .select("COALESCE(nurse_name, '未填写') AS name", "COUNT(1) AS totalCount")
        .eq("is_deleted", 0)
        .eq(orgId != null, "org_id", orgId)
        .eq(elderId != null, "elder_id", elderId)
        .eq(drugName != null && !drugName.isBlank(), "drug_name", drugName)
        .eq(nurseName != null && !nurseName.isBlank(), "nurse_name", nurseName)
        .ge(from != null, "register_time", from)
        .le(to != null, "register_time", to)
        .groupBy("nurse_name")
        .orderByDesc("totalCount");
    if (keyword != null && !keyword.isBlank()) {
      nurseStatWrapper.and(w -> w.like("elder_name", keyword)
          .or().like("drug_name", keyword)
          .or().like("nurse_name", keyword));
    }
    List<HealthNameCountStatItem> nurseStats = mapper.selectMaps(nurseStatWrapper).stream().map(map -> {
      HealthNameCountStatItem item = new HealthNameCountStatItem();
      item.setName(String.valueOf(map.getOrDefault("name", "未填写")));
      item.setTotalCount(toLong(map.get("totalCount")));
      return item;
    }).toList();

    HealthMedicationRegistrationSummaryResponse response = new HealthMedicationRegistrationSummaryResponse();
    response.setTotalCount(totalCount);
    response.setTodayCount(todayCount);
    response.setTotalDosage(totalDosage);
    response.setDoneTaskCount(doneTaskCount);
    response.setPendingTaskCount(pendingTaskCount);
    response.setNurseStats(nurseStats);
    return Result.ok(response);
  }

  @PostMapping
  public Result<HealthMedicationRegistration> create(@Valid @RequestBody HealthMedicationRegistrationRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    HealthMedicationRegistration item = new HealthMedicationRegistration();
    item.setTenantId(orgId);
    item.setOrgId(orgId);
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setDrugId(request.getDrugId());
    item.setDrugName(request.getDrugName());
    item.setRegisterTime(request.getRegisterTime());
    item.setDosageTaken(request.getDosageTaken());
    item.setUnit(request.getUnit());
    item.setNurseName(request.getNurseName());
    item.setRemark(request.getRemark());
    item.setCreatedBy(AuthContext.getStaffId());
    mapper.insert(item);
    medicationTaskService.completeTaskByRegistration(item);
    return Result.ok(item);
  }

  @PutMapping("/{id}")
  public Result<HealthMedicationRegistration> update(@PathVariable Long id, @Valid @RequestBody HealthMedicationRegistrationRequest request) {
    HealthMedicationRegistration item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item == null || item.getIsDeleted() != null && item.getIsDeleted() == 1
        || orgId != null && !orgId.equals(item.getOrgId())) {
      return Result.ok(null);
    }
    Long elderId = elderResolveSupport.resolveElderId(orgId, request.getElderId(), request.getElderName());
    item.setElderId(elderId);
    item.setElderName(elderResolveSupport.resolveElderName(elderId, request.getElderName()));
    item.setDrugId(request.getDrugId());
    item.setDrugName(request.getDrugName());
    item.setRegisterTime(request.getRegisterTime());
    item.setDosageTaken(request.getDosageTaken());
    item.setUnit(request.getUnit());
    item.setNurseName(request.getNurseName());
    item.setRemark(request.getRemark());
    mapper.updateById(item);
    medicationTaskService.completeTaskByRegistration(item);
    return Result.ok(item);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    HealthMedicationRegistration item = mapper.selectById(id);
    Long orgId = AuthContext.getOrgId();
    if (item != null && (item.getIsDeleted() == null || item.getIsDeleted() == 0)
        && (orgId == null || orgId.equals(item.getOrgId()))) {
      item.setIsDeleted(1);
      mapper.updateById(item);
      medicationTaskService.unlinkTaskByRegistration(item);
    }
    return Result.ok(null);
  }

  private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HealthMedicationRegistration> buildQuery(
      Long orgId,
      Long elderId,
      String drugName,
      String nurseName,
      String registerFrom,
      String registerTo,
      String keyword) {
    LocalDateTime from = parseDateTime(registerFrom);
    LocalDateTime to = parseDateTime(registerTo);
    var wrapper = Wrappers.lambdaQuery(HealthMedicationRegistration.class)
        .eq(HealthMedicationRegistration::getIsDeleted, 0)
        .eq(orgId != null, HealthMedicationRegistration::getOrgId, orgId)
        .eq(elderId != null, HealthMedicationRegistration::getElderId, elderId)
        .eq(drugName != null && !drugName.isBlank(), HealthMedicationRegistration::getDrugName, drugName)
        .eq(nurseName != null && !nurseName.isBlank(), HealthMedicationRegistration::getNurseName, nurseName)
        .ge(from != null, HealthMedicationRegistration::getRegisterTime, from)
        .le(to != null, HealthMedicationRegistration::getRegisterTime, to);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(HealthMedicationRegistration::getElderName, keyword)
          .or().like(HealthMedicationRegistration::getDrugName, keyword)
          .or().like(HealthMedicationRegistration::getNurseName, keyword));
    }
    return wrapper;
  }

  private LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
    } catch (DateTimeParseException ignore) {
      try {
        return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      } catch (DateTimeParseException e) {
        return null;
      }
    }
  }

  private Long toLong(Object value) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    if (value == null) {
      return 0L;
    }
    try {
      return Long.parseLong(String.valueOf(value));
    } catch (NumberFormatException e) {
      return 0L;
    }
  }
}
