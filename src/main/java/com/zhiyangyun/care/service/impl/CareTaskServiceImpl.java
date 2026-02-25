package com.zhiyangyun.care.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.entity.CareTaskExecuteLog;
import com.zhiyangyun.care.entity.CareTaskTemplate;
import com.zhiyangyun.care.entity.CareTaskReview;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.mapper.CareTaskExecuteLogMapper;
import com.zhiyangyun.care.mapper.CareTaskTemplateMapper;
import com.zhiyangyun.care.mapper.CareTaskReviewMapper;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.model.CareTaskTodayItem;
import com.zhiyangyun.care.model.CareTaskCreateRequest;
import com.zhiyangyun.care.model.CareTaskExecuteLogItem;
import com.zhiyangyun.care.model.CareTaskSummaryResponse;
import com.zhiyangyun.care.model.ExecuteTaskRequest;
import com.zhiyangyun.care.model.ExecuteTaskResponse;
import com.zhiyangyun.care.model.TaskStatus;
import com.zhiyangyun.care.service.CareTaskService;
import com.zhiyangyun.care.finance.service.ElderAccountService;
import com.zhiyangyun.care.hr.service.StaffPointsService;
import com.zhiyangyun.care.hr.entity.StaffPointsRule;
import com.zhiyangyun.care.hr.mapper.StaffPointsRuleMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CareTaskServiceImpl implements CareTaskService {
  private static final int TASK_COMPLETE_POINTS = 1;

  private final CareTaskDailyMapper dailyMapper;
  private final CareTaskTemplateMapper templateMapper;
  private final CareTaskExecuteLogMapper logMapper;
  private final CareTaskReviewMapper reviewMapper;
  private final ElderMapper elderMapper;
  private final BedMapper bedMapper;
  private final RoomMapper roomMapper;
  private final StaffPointsService staffPointsService;
  private final StaffPointsRuleMapper staffPointsRuleMapper;
  private final ElderAccountService elderAccountService;
  private final StaffMapper staffMapper;

  public CareTaskServiceImpl(
      CareTaskDailyMapper dailyMapper,
      CareTaskTemplateMapper templateMapper,
      CareTaskExecuteLogMapper logMapper,
      CareTaskReviewMapper reviewMapper,
      ElderMapper elderMapper,
      BedMapper bedMapper,
      RoomMapper roomMapper,
      StaffPointsService staffPointsService,
      StaffPointsRuleMapper staffPointsRuleMapper,
      ElderAccountService elderAccountService,
      StaffMapper staffMapper) {
    this.dailyMapper = dailyMapper;
    this.templateMapper = templateMapper;
    this.logMapper = logMapper;
    this.reviewMapper = reviewMapper;
    this.elderMapper = elderMapper;
    this.bedMapper = bedMapper;
    this.roomMapper = roomMapper;
    this.staffPointsService = staffPointsService;
    this.staffPointsRuleMapper = staffPointsRuleMapper;
    this.elderAccountService = elderAccountService;
    this.staffMapper = staffMapper;
  }

  @Override
  public List<CareTaskTodayItem> getTodayTasks(Long tenantId, Long staffId, LocalDate date) {
    LambdaQueryWrapper<CareTaskDaily> wrapper = Wrappers.lambdaQuery();
    wrapper.eq(CareTaskDaily::getTaskDate, date);
    wrapper.eq(CareTaskDaily::getIsDeleted, 0);
    if (tenantId != null) {
      wrapper.eq(CareTaskDaily::getTenantId, tenantId);
    }
    if (staffId != null) {
      wrapper.and(w -> w.eq(CareTaskDaily::getAssignedStaffId, staffId)
          .or().isNull(CareTaskDaily::getAssignedStaffId));
    }
    List<CareTaskDaily> tasks = dailyMapper.selectList(wrapper);

    Map<Long, ElderProfile> elderMap = new HashMap<>();
    Map<Long, Bed> bedMap = new HashMap<>();
    Map<Long, Room> roomMap = new HashMap<>();
    Map<Long, CareTaskTemplate> templateMap = new HashMap<>();

    for (CareTaskDaily task : tasks) {
      if (task.getElderId() != null && !elderMap.containsKey(task.getElderId())) {
        elderMap.put(task.getElderId(), elderMapper.selectById(task.getElderId()));
      }
      if (task.getBedId() != null && !bedMap.containsKey(task.getBedId())) {
        bedMap.put(task.getBedId(), bedMapper.selectById(task.getBedId()));
      }
      if (task.getTemplateId() != null && !templateMap.containsKey(task.getTemplateId())) {
        templateMap.put(task.getTemplateId(), templateMapper.selectById(task.getTemplateId()));
      }
    }

    for (Bed bed : bedMap.values()) {
      if (bed != null && bed.getRoomId() != null && !roomMap.containsKey(bed.getRoomId())) {
        roomMap.put(bed.getRoomId(), roomMapper.selectById(bed.getRoomId()));
      }
    }

    List<Long> taskIds = tasks.stream().map(CareTaskDaily::getId).filter(Objects::nonNull).toList();
    Map<Long, Boolean> suspiciousMap = logMapper.selectList(Wrappers.lambdaQuery(CareTaskExecuteLog.class)
            .eq(CareTaskExecuteLog::getIsDeleted, 0)
            .in(!taskIds.isEmpty(), CareTaskExecuteLog::getTaskDailyId, taskIds)
            .eq(CareTaskExecuteLog::getSuspiciousFlag, 1))
        .stream()
        .collect(Collectors.toMap(CareTaskExecuteLog::getTaskDailyId, v -> true, (a, b) -> true));

    List<Long> staffIds = tasks.stream()
        .map(CareTaskDaily::getAssignedStaffId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, StaffAccount> staffMap = staffIds.isEmpty()
        ? Map.of()
        : staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
            .in(StaffAccount::getId, staffIds)
            .eq(StaffAccount::getIsDeleted, 0))
            .stream()
            .collect(Collectors.toMap(StaffAccount::getId, Function.identity(), (a, b) -> a));

    List<CareTaskTodayItem> result = new ArrayList<>();
    for (CareTaskDaily task : tasks) {
      CareTaskTodayItem item = new CareTaskTodayItem();
      item.setTaskDailyId(task.getId());
      item.setElderId(task.getElderId());
      ElderProfile elder = safeGet(elderMap, task.getElderId());
      item.setElderName(elder != null ? elder.getFullName() : null);
      item.setCareLevel(elder != null ? elder.getCareLevel() : null);
      item.setBedId(task.getBedId());
      Bed bed = safeGet(bedMap, task.getBedId());
      Room room = bed != null ? roomMap.get(bed.getRoomId()) : null;
      item.setRoomNo(room != null ? room.getRoomNo() : null);
      item.setStaffId(task.getAssignedStaffId());
      StaffAccount staff = safeGet(staffMap, task.getAssignedStaffId());
      item.setStaffName(staff == null ? null : staff.getRealName());
      CareTaskTemplate template = safeGet(templateMap, task.getTemplateId());
      item.setTaskName(template != null ? template.getTaskName() : null);
      item.setPlanTime(task.getPlanTime());
      item.setStatus(task.getStatus());
      item.setSuspiciousFlag(suspiciousMap.getOrDefault(task.getId(), false));
      item.setOverdueFlag(isOverdue(task));
      result.add(item);
    }

    return result;
  }

  @Override
  public List<CareTaskExecuteLogItem> listExecuteLogs(Long tenantId, Long taskDailyId) {
    if (taskDailyId == null) {
      return List.of();
    }
    CareTaskDaily task = dailyMapper.selectById(taskDailyId);
    if (task == null || (tenantId != null && !tenantId.equals(task.getTenantId()))) {
      return List.of();
    }
    List<CareTaskExecuteLog> logs = logMapper.selectList(Wrappers.lambdaQuery(CareTaskExecuteLog.class)
        .eq(CareTaskExecuteLog::getIsDeleted, 0)
        .eq(CareTaskExecuteLog::getTaskDailyId, taskDailyId)
        .eq(tenantId != null, CareTaskExecuteLog::getTenantId, tenantId)
        .orderByDesc(CareTaskExecuteLog::getExecuteTime)
        .orderByDesc(CareTaskExecuteLog::getCreateTime)
        .last("LIMIT 100"));
    if (logs.isEmpty()) {
      return List.of();
    }
    List<Long> staffIds = logs.stream()
        .map(CareTaskExecuteLog::getStaffId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, StaffAccount> staffMap = staffIds.isEmpty()
        ? Map.of()
        : staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
            .in(StaffAccount::getId, staffIds)
            .eq(StaffAccount::getIsDeleted, 0))
            .stream()
            .collect(Collectors.toMap(StaffAccount::getId, Function.identity(), (a, b) -> a));

    return logs.stream().map(log -> {
      CareTaskExecuteLogItem item = new CareTaskExecuteLogItem();
      item.setId(log.getId());
      item.setTaskDailyId(log.getTaskDailyId());
      item.setStaffId(log.getStaffId());
      StaffAccount staff = staffMap.get(log.getStaffId());
      item.setStaffName(staff == null ? null : staff.getRealName());
      item.setExecuteTime(log.getExecuteTime());
      item.setBedQrCode(log.getBedQrCode());
      item.setResultStatus(log.getResultStatus());
      item.setSuspiciousFlag(log.getSuspiciousFlag() != null && log.getSuspiciousFlag() == 1);
      item.setRemark(log.getRemark());
      return item;
    }).toList();
  }

  private <K, V> V safeGet(Map<K, V> map, K key) {
    if (map == null || key == null) {
      return null;
    }
    return map.get(key);
  }

  @Override
  public IPage<CareTaskTodayItem> page(Long tenantId, long pageNo, long pageSize, LocalDate dateFrom,
      LocalDate dateTo, Long staffId, String roomNo, String careLevel, String status, String keyword,
      Boolean overdueOnly) {
    LambdaQueryWrapper<CareTaskDaily> wrapper = buildTaskFilterWrapper(tenantId, dateFrom, dateTo, staffId,
        roomNo, careLevel, status, keyword, overdueOnly);

    IPage<CareTaskDaily> page = dailyMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<CareTaskDaily> tasks = page.getRecords();

    Map<Long, CareTaskTemplate> templateMap = new HashMap<>();
    for (CareTaskDaily task : tasks) {
      if (task.getTemplateId() != null && !templateMap.containsKey(task.getTemplateId())) {
        templateMap.put(task.getTemplateId(), templateMapper.selectById(task.getTemplateId()));
      }
    }

    List<Long> bedIds = tasks.stream()
        .map(CareTaskDaily::getBedId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Bed> bedMap = bedIds.isEmpty()
        ? Map.of()
        : bedMapper.selectBatchIds(bedIds)
            .stream()
            .collect(Collectors.toMap(Bed::getId, Function.identity()));

    List<Long> roomIds = bedMap.values().stream()
        .map(Bed::getRoomId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Room> roomMap = roomIds.isEmpty()
        ? Map.of()
        : roomMapper.selectBatchIds(roomIds)
            .stream()
            .collect(Collectors.toMap(Room::getId, Function.identity()));

    List<Long> elderIds = tasks.stream()
        .map(CareTaskDaily::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectBatchIds(elderIds)
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity()));

    List<Long> taskIds = tasks.stream().map(CareTaskDaily::getId).filter(Objects::nonNull).toList();
    Map<Long, Boolean> suspiciousMap = logMapper.selectList(Wrappers.lambdaQuery(CareTaskExecuteLog.class)
            .eq(CareTaskExecuteLog::getIsDeleted, 0)
            .in(!taskIds.isEmpty(), CareTaskExecuteLog::getTaskDailyId, taskIds)
            .eq(CareTaskExecuteLog::getSuspiciousFlag, 1))
        .stream()
        .collect(Collectors.toMap(CareTaskExecuteLog::getTaskDailyId, v -> true, (a, b) -> true));

    List<Long> staffIds = tasks.stream()
        .map(CareTaskDaily::getAssignedStaffId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, StaffAccount> staffMap = staffIds.isEmpty()
        ? Map.of()
        : staffMapper.selectList(Wrappers.lambdaQuery(StaffAccount.class)
            .in(StaffAccount::getId, staffIds)
            .eq(StaffAccount::getIsDeleted, 0))
            .stream()
            .collect(Collectors.toMap(StaffAccount::getId, Function.identity(), (a, b) -> a));

    List<CareTaskTodayItem> items = tasks.stream().map(task -> {
      CareTaskTodayItem item = new CareTaskTodayItem();
      item.setTaskDailyId(task.getId());
      item.setElderId(task.getElderId());
      ElderProfile elder = elderMap.get(task.getElderId());
      item.setElderName(elder != null ? elder.getFullName() : null);
      item.setCareLevel(elder != null ? elder.getCareLevel() : null);
      item.setBedId(task.getBedId());
      Bed bed = bedMap.get(task.getBedId());
      Room room = bed != null ? roomMap.get(bed.getRoomId()) : null;
      item.setRoomNo(room != null ? room.getRoomNo() : null);
      item.setStaffId(task.getAssignedStaffId());
      StaffAccount staff = staffMap.get(task.getAssignedStaffId());
      item.setStaffName(staff == null ? null : staff.getRealName());
      CareTaskTemplate template = templateMap.get(task.getTemplateId());
      item.setTaskName(template != null ? template.getTaskName() : null);
      item.setPlanTime(task.getPlanTime());
      item.setStatus(task.getStatus());
      item.setSuspiciousFlag(suspiciousMap.getOrDefault(task.getId(), false));
      item.setOverdueFlag(isOverdue(task));
      return item;
    }).toList();

    Page<CareTaskTodayItem> result = new Page<>(pageNo, pageSize);
    result.setTotal(page.getTotal());
    result.setRecords(items);
    return result;
  }

  @Override
  public CareTaskSummaryResponse summary(Long tenantId, LocalDate dateFrom, LocalDate dateTo, Long staffId,
      String roomNo, String careLevel, String status, String keyword, Boolean overdueOnly) {
    LambdaQueryWrapper<CareTaskDaily> wrapper = buildTaskFilterWrapper(tenantId, dateFrom, dateTo, staffId,
        roomNo, careLevel, status, keyword, overdueOnly);
    List<CareTaskDaily> tasks = dailyMapper.selectList(wrapper);

    long total = tasks.size();
    long pending = tasks.stream().filter(t -> TaskStatus.PENDING.name().equals(t.getStatus())).count();
    long done = tasks.stream().filter(t -> TaskStatus.DONE.name().equals(t.getStatus())).count();
    long exception = tasks.stream().filter(t -> TaskStatus.EXCEPTION.name().equals(t.getStatus())).count();
    long assigned = tasks.stream().filter(t -> t.getAssignedStaffId() != null).count();
    long overdue = tasks.stream().filter(this::isOverdue).count();

    List<Long> taskIds = tasks.stream().map(CareTaskDaily::getId).filter(Objects::nonNull).toList();
    long suspicious = 0;
    if (!taskIds.isEmpty()) {
      Set<Long> suspiciousTaskIds = logMapper.selectList(Wrappers.lambdaQuery(CareTaskExecuteLog.class)
              .eq(CareTaskExecuteLog::getIsDeleted, 0)
              .in(CareTaskExecuteLog::getTaskDailyId, taskIds)
              .eq(CareTaskExecuteLog::getSuspiciousFlag, 1))
          .stream()
          .map(CareTaskExecuteLog::getTaskDailyId)
          .filter(Objects::nonNull)
          .collect(Collectors.toCollection(HashSet::new));
      suspicious = suspiciousTaskIds.size();
    }

    CareTaskSummaryResponse response = new CareTaskSummaryResponse();
    response.setTotalCount(total);
    response.setPendingCount(pending);
    response.setDoneCount(done);
    response.setExceptionCount(exception);
    response.setOverdueCount(overdue);
    response.setSuspiciousCount(suspicious);
    response.setAssignedCount(assigned);
    response.setUnassignedCount(total - assigned);
    response.setCompletionRate(total == 0 ? 0D : (double) done / total);
    response.setExceptionRate(total == 0 ? 0D : (double) exception / total);
    return response;
  }

  @Override
  public void assignTask(Long tenantId, Long taskDailyId, Long staffId, boolean force) {
    CareTaskDaily task = dailyMapper.selectById(taskDailyId);
    if (task == null) {
      throw new IllegalArgumentException("任务不存在");
    }
    if (tenantId != null && !tenantId.equals(task.getTenantId())) {
      throw new IllegalArgumentException("无权限访问该任务");
    }
    if (!force && task.getAssignedStaffId() != null && !task.getAssignedStaffId().equals(staffId)) {
      throw new IllegalArgumentException("任务已分配护工");
    }
    task.setAssignedStaffId(staffId);
    dailyMapper.updateById(task);
  }

  @Override
  public int assignBatch(Long tenantId, String dateFrom, String dateTo, Long staffId, String roomNo,
      String floorNo, String building, String careLevel, String status, boolean force) {
    if (staffId == null) {
      return 0;
    }
    LambdaQueryWrapper<CareTaskDaily> wrapper = Wrappers.lambdaQuery();
    wrapper.eq(CareTaskDaily::getIsDeleted, 0);
    if (tenantId != null) {
      wrapper.eq(CareTaskDaily::getTenantId, tenantId);
    }
    if (dateFrom != null && !dateFrom.isBlank()) {
      wrapper.ge(CareTaskDaily::getTaskDate, LocalDate.parse(dateFrom));
    }
    if (dateTo != null && !dateTo.isBlank()) {
      wrapper.le(CareTaskDaily::getTaskDate, LocalDate.parse(dateTo));
    }
    if (status != null && !status.isBlank()) {
      wrapper.eq(CareTaskDaily::getStatus, status);
    }
    if (!force) {
      wrapper.isNull(CareTaskDaily::getAssignedStaffId);
    }

    if (roomNo != null && !roomNo.isBlank() || building != null && !building.isBlank() || floorNo != null && !floorNo.isBlank()) {
      var roomWrapper = Wrappers.lambdaQuery(Room.class)
          .eq(Room::getIsDeleted, 0)
          .eq(tenantId != null, Room::getTenantId, tenantId);
      if (roomNo != null && !roomNo.isBlank()) {
        roomWrapper.like(Room::getRoomNo, roomNo);
      }
      if (building != null && !building.isBlank()) {
        roomWrapper.like(Room::getBuilding, building);
      }
      if (floorNo != null && !floorNo.isBlank()) {
        roomWrapper.like(Room::getFloorNo, floorNo);
      }
      List<Long> roomIds = roomMapper.selectList(roomWrapper).stream().map(Room::getId).toList();
      if (roomIds.isEmpty()) {
        return 0;
      }
      List<Long> bedIds = bedMapper.selectList(Wrappers.lambdaQuery(Bed.class)
              .eq(Bed::getIsDeleted, 0)
              .eq(tenantId != null, Bed::getTenantId, tenantId)
              .in(Bed::getRoomId, roomIds))
          .stream().map(Bed::getId).toList();
      if (bedIds.isEmpty()) {
        return 0;
      }
      wrapper.in(CareTaskDaily::getBedId, bedIds);
    }

    if (careLevel != null && !careLevel.isBlank()) {
      List<Long> elderIds = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
              .eq(ElderProfile::getIsDeleted, 0)
              .eq(tenantId != null, ElderProfile::getTenantId, tenantId)
              .eq(ElderProfile::getCareLevel, careLevel))
          .stream().map(ElderProfile::getId).toList();
      if (elderIds.isEmpty()) {
        return 0;
      }
      wrapper.in(CareTaskDaily::getElderId, elderIds);
    }

    CareTaskDaily update = new CareTaskDaily();
    update.setAssignedStaffId(staffId);
    return dailyMapper.update(update, wrapper);
  }

  @Override
  public void reviewTask(Long tenantId, Long taskDailyId, Long staffId, Integer score, String comment,
      String reviewerType, Long reviewerId, String reviewerName, LocalDateTime reviewTime) {
    CareTaskReview review = new CareTaskReview();
    review.setTenantId(tenantId);
    review.setOrgId(tenantId);
    review.setTaskDailyId(taskDailyId);
    review.setStaffId(staffId);
    review.setScore(score);
    review.setComment(comment);
    review.setReviewerType(reviewerType);
    review.setReviewerId(reviewerId);
    review.setReviewerName(reviewerName);
    review.setReviewTime(reviewTime == null ? LocalDateTime.now() : reviewTime);
    reviewMapper.insert(review);

    CareTaskDaily task = dailyMapper.selectById(taskDailyId);
    if (task != null) {
      StaffPointsRule rule = resolvePointsRule(task.getOrgId(), task.getTemplateId());
      int reviewPoints = calculateReviewPoints(rule, score);
      if (reviewPoints != 0) {
        staffPointsService.awardForReview(task.getOrgId(), staffId, taskDailyId, reviewPoints, "任务评价积分");
      }
    }
  }

  @Override
  public Long createTask(Long tenantId, CareTaskCreateRequest request) {
    ElderProfile elder = elderMapper.selectById(request.getElderId());
    if (elder == null) {
      throw new IllegalArgumentException("老人不存在");
    }
    if (tenantId != null && !tenantId.equals(elder.getTenantId())) {
      throw new IllegalArgumentException("无权限访问该老人");
    }
    CareTaskTemplate template = null;
    if (request.getTemplateId() != null) {
      template = templateMapper.selectById(request.getTemplateId());
      if (template == null || (tenantId != null && !tenantId.equals(template.getTenantId()))) {
        throw new IllegalArgumentException("模板不存在");
      }
    } else {
      if (request.getTaskName() == null || request.getTaskName().isBlank()) {
        throw new IllegalArgumentException("任务名称不能为空");
      }
      CareTaskTemplate custom = new CareTaskTemplate();
      custom.setTenantId(tenantId != null ? tenantId : elder.getTenantId());
      custom.setOrgId(tenantId != null ? tenantId : elder.getOrgId());
      custom.setTaskName(request.getTaskName());
      custom.setFrequencyPerDay(1);
      custom.setEnabled(false);
      templateMapper.insert(custom);
      template = custom;
    }
    LocalDateTime planTime = LocalDateTime.parse(request.getPlanTime(), DateTimeFormatter.ISO_DATE_TIME);
    CareTaskDaily task = new CareTaskDaily();
    task.setTenantId(tenantId != null ? tenantId : elder.getTenantId());
    task.setOrgId(tenantId != null ? tenantId : elder.getOrgId());
    task.setElderId(request.getElderId());
    task.setBedId(elder.getBedId());
    task.setTemplateId(template.getId());
    task.setTaskDate(planTime.toLocalDate());
    task.setPlanTime(planTime);
    task.setAssignedStaffId(request.getStaffId());
    String status = request.getStatus();
    task.setStatus(status == null || status.isBlank() ? TaskStatus.PENDING.name() : status);
    dailyMapper.insert(task);
    return task.getId();
  }

  @Override
  @Transactional
  public ExecuteTaskResponse executeTask(Long tenantId, ExecuteTaskRequest request) {
    ExecuteTaskResponse response = new ExecuteTaskResponse();
    response.setTaskDailyId(request.getTaskDailyId());

    CareTaskDaily task = dailyMapper.selectById(request.getTaskDailyId());
    if (task == null) {
      response.setStatus(TaskStatus.EXCEPTION.name());
      response.setAbnormal(true);
      response.setSuspicious(false);
      response.setMessage("Task not found");
      return response;
    }
    if (tenantId != null && !tenantId.equals(task.getTenantId())) {
      response.setStatus(TaskStatus.EXCEPTION.name());
      response.setAbnormal(true);
      response.setSuspicious(false);
      response.setMessage("Unauthorized");
      return response;
    }

    if (TaskStatus.DONE.name().equals(task.getStatus())) {
      response.setStatus(TaskStatus.DONE.name());
      response.setAbnormal(false);
      response.setSuspicious(false);
      response.setMessage("Task already completed");
      return response;
    }

    ElderProfile elder = elderMapper.selectById(task.getElderId());
    Bed elderBed = elder != null ? bedMapper.selectById(elder.getBedId()) : null;
    if (elderBed == null || elderBed.getBedQrCode() == null
        || !elderBed.getBedQrCode().equals(request.getBedQrCode())) {
      task.setStatus(TaskStatus.EXCEPTION.name());
      dailyMapper.updateById(task);

      CareTaskExecuteLog log = new CareTaskExecuteLog();
      log.setTenantId(task.getTenantId());
      log.setOrgId(task.getOrgId());
      log.setTaskDailyId(task.getId());
      log.setElderId(task.getElderId());
      log.setBedId(task.getBedId());
      log.setStaffId(request.getStaffId());
      log.setExecuteTime(LocalDateTime.now());
      log.setBedQrCode(request.getBedQrCode());
      log.setResultStatus(0);
      log.setSuspiciousFlag(0);
      log.setRemark(request.getRemark());
      logMapper.insert(log);

      StaffPointsRule rule = resolvePointsRule(task.getOrgId(), task.getTemplateId());
      int taskPoints = calculateTaskPoints(rule, false, false);
      if (taskPoints != 0) {
        staffPointsService.awardForTask(task.getOrgId(), request.getStaffId(), task.getId(),
            taskPoints, "护理任务失败");
      }

      response.setStatus(TaskStatus.EXCEPTION.name());
      response.setAbnormal(true);
      response.setSuspicious(false);
      response.setMessage("Bed QR code mismatch");
      return response;
    }

    boolean suspicious = isSuspicious(task.getTenantId(), request.getStaffId(), elderBed.getRoomId());

    task.setStatus(TaskStatus.DONE.name());
    task.setAssignedStaffId(request.getStaffId());
    dailyMapper.updateById(task);

    CareTaskExecuteLog log = new CareTaskExecuteLog();
    log.setTenantId(task.getTenantId());
    log.setOrgId(task.getOrgId());
    log.setTaskDailyId(task.getId());
    log.setElderId(task.getElderId());
    log.setBedId(task.getBedId());
    log.setStaffId(request.getStaffId());
    log.setExecuteTime(LocalDateTime.now());
    log.setBedQrCode(request.getBedQrCode());
    log.setResultStatus(1);
    log.setSuspiciousFlag(suspicious ? 1 : 0);
    log.setRemark(request.getRemark());
    logMapper.insert(log);

    StaffPointsRule rule = resolvePointsRule(task.getOrgId(), task.getTemplateId());
    int taskPoints = calculateTaskPoints(rule, suspicious, true);
    if (taskPoints != 0) {
      staffPointsService.awardForTask(task.getOrgId(), request.getStaffId(), task.getId(),
          taskPoints, "护理任务完成");
    }
    if (!suspicious) {
      CareTaskTemplate template = templateMapper.selectById(task.getTemplateId());
      if (template != null && template.getChargeAmount() != null) {
        elderAccountService.chargeForTask(task.getOrgId(), task.getElderId(), task.getId(),
            template.getChargeAmount(), "护理任务收费", request.getStaffId());
      }
    }

    response.setStatus(TaskStatus.DONE.name());
    response.setAbnormal(false);
    response.setSuspicious(suspicious);
    response.setMessage(suspicious ? "Executed with suspicious flag" : "Executed successfully");
    return response;
  }

  private boolean isSuspicious(Long tenantId, Long staffId, Long currentRoomId) {
    LambdaQueryWrapper<CareTaskExecuteLog> wrapper = Wrappers.lambdaQuery();
    wrapper.eq(CareTaskExecuteLog::getStaffId, staffId)
        .eq(tenantId != null, CareTaskExecuteLog::getTenantId, tenantId)
        .orderByDesc(CareTaskExecuteLog::getExecuteTime)
        .last("LIMIT 1");
    CareTaskExecuteLog lastLog = logMapper.selectOne(wrapper);
    if (lastLog == null) {
      return false;
    }

    Bed lastBed = bedMapper.selectById(lastLog.getBedId());
    if (lastBed == null || lastBed.getRoomId() == null || currentRoomId == null) {
      return false;
    }

    if (lastBed.getRoomId().equals(currentRoomId)) {
      return false;
    }

    Duration duration = Duration.between(lastLog.getExecuteTime(), LocalDateTime.now());
    return duration.toMinutes() < 3;
  }

  private boolean isOverdue(CareTaskDaily task) {
    if (task == null || task.getPlanTime() == null) {
      return false;
    }
    return TaskStatus.PENDING.name().equals(task.getStatus())
        && task.getPlanTime().isBefore(LocalDateTime.now());
  }

  private LambdaQueryWrapper<CareTaskDaily> buildTaskFilterWrapper(Long tenantId, LocalDate dateFrom,
      LocalDate dateTo, Long staffId, String roomNo, String careLevel, String status, String keyword,
      Boolean overdueOnly) {
    LambdaQueryWrapper<CareTaskDaily> wrapper = Wrappers.lambdaQuery();
    wrapper.eq(CareTaskDaily::getIsDeleted, 0);
    if (tenantId != null) {
      wrapper.eq(CareTaskDaily::getTenantId, tenantId);
    }
    if (dateFrom != null && dateTo != null) {
      wrapper.between(CareTaskDaily::getTaskDate, dateFrom, dateTo);
    } else if (dateFrom != null) {
      wrapper.ge(CareTaskDaily::getTaskDate, dateFrom);
    } else if (dateTo != null) {
      wrapper.le(CareTaskDaily::getTaskDate, dateTo);
    }
    if (staffId != null) {
      wrapper.eq(CareTaskDaily::getAssignedStaffId, staffId);
    }
    if (status != null && !status.isBlank()) {
      wrapper.eq(CareTaskDaily::getStatus, status);
    }
    if (Boolean.TRUE.equals(overdueOnly)) {
      wrapper.eq(CareTaskDaily::getStatus, TaskStatus.PENDING.name())
          .lt(CareTaskDaily::getPlanTime, LocalDateTime.now());
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(CareTaskDaily::getId, keyword)
          .or().like(CareTaskDaily::getBedId, keyword));
    }

    if (roomNo != null && !roomNo.isBlank()) {
      List<Long> roomIds = roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
              .eq(Room::getIsDeleted, 0)
              .eq(tenantId != null, Room::getTenantId, tenantId)
              .like(Room::getRoomNo, roomNo))
          .stream().map(Room::getId).toList();
      if (roomIds.isEmpty()) {
        return wrapper.eq(CareTaskDaily::getId, -1L);
      }
      List<Long> bedIds = bedMapper.selectList(Wrappers.lambdaQuery(Bed.class)
              .eq(Bed::getIsDeleted, 0)
              .eq(tenantId != null, Bed::getTenantId, tenantId)
              .in(Bed::getRoomId, roomIds))
          .stream().map(Bed::getId).toList();
      if (bedIds.isEmpty()) {
        return wrapper.eq(CareTaskDaily::getId, -1L);
      }
      wrapper.in(CareTaskDaily::getBedId, bedIds);
    }

    if (careLevel != null && !careLevel.isBlank()) {
      List<Long> elderIds = elderMapper.selectList(Wrappers.lambdaQuery(ElderProfile.class)
              .eq(ElderProfile::getIsDeleted, 0)
              .eq(tenantId != null, ElderProfile::getTenantId, tenantId)
              .eq(ElderProfile::getCareLevel, careLevel))
          .stream().map(ElderProfile::getId).toList();
      if (elderIds.isEmpty()) {
        return wrapper.eq(CareTaskDaily::getId, -1L);
      }
      wrapper.in(CareTaskDaily::getElderId, elderIds);
    }

    return wrapper;
  }

  @Override
  @Transactional
  public int generateDailyTasks(Long tenantId, LocalDate date) {
    return generateDailyTasks(tenantId, date, false);
  }

  @Override
  @Transactional
  public int generateDailyTasks(Long tenantId, LocalDate date, boolean force) {
    var existingWrapper = Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getTaskDate, date);
    if (tenantId != null) {
      existingWrapper.eq(CareTaskDaily::getTenantId, tenantId);
    }
    Long existingCount = dailyMapper.selectCount(existingWrapper);
    if (!force && existingCount != null && existingCount > 0) {
      return 0;
    }

    var elderWrapper = Wrappers.lambdaQuery(ElderProfile.class)
        .eq(ElderProfile::getStatus, 1);
    if (tenantId != null) {
      elderWrapper.eq(ElderProfile::getTenantId, tenantId);
    }
    List<ElderProfile> elders = elderMapper.selectList(elderWrapper);
    var templateWrapper = Wrappers.lambdaQuery(CareTaskTemplate.class)
        .eq(CareTaskTemplate::getEnabled, true);
    if (tenantId != null) {
      templateWrapper.eq(CareTaskTemplate::getTenantId, tenantId);
    }
    List<CareTaskTemplate> templates = templateMapper.selectList(templateWrapper);

    int count = 0;
    for (ElderProfile elder : elders) {
      for (CareTaskTemplate template : templates) {
        if (template.getCareLevelRequired() != null && elder.getCareLevel() != null
            && !template.getCareLevelRequired().equals(elder.getCareLevel())) {
          continue;
        }

        int frequency = template.getFrequencyPerDay() == null ? 1 : template.getFrequencyPerDay();
        for (int i = 0; i < frequency; i++) {
          CareTaskDaily task = new CareTaskDaily();
          task.setTenantId(tenantId != null ? tenantId : elder.getTenantId());
          task.setOrgId(elder.getOrgId());
          task.setElderId(elder.getId());
          task.setBedId(elder.getBedId());
          task.setTemplateId(template.getId());
          task.setTaskDate(date);
          task.setPlanTime(date.atStartOfDay().plusHours(8).plusMinutes(i * 60));
          task.setStatus(TaskStatus.PENDING.name());
          dailyMapper.insert(task);
          count++;
        }
      }
    }

    return count;
  }

  private StaffPointsRule resolvePointsRule(Long orgId, Long templateId) {
    if (orgId == null) {
      return defaultRule();
    }
    StaffPointsRule rule = staffPointsRuleMapper.selectOne(
        Wrappers.lambdaQuery(StaffPointsRule.class)
            .eq(StaffPointsRule::getIsDeleted, 0)
            .eq(StaffPointsRule::getOrgId, orgId)
            .eq(templateId != null, StaffPointsRule::getTemplateId, templateId)
            .eq(StaffPointsRule::getStatus, 1));
    if (rule == null) {
      rule = staffPointsRuleMapper.selectOne(
          Wrappers.lambdaQuery(StaffPointsRule.class)
              .eq(StaffPointsRule::getIsDeleted, 0)
              .eq(StaffPointsRule::getOrgId, orgId)
              .isNull(StaffPointsRule::getTemplateId)
              .eq(StaffPointsRule::getStatus, 1));
    }
    return rule == null ? defaultRule() : rule;
  }

  private StaffPointsRule defaultRule() {
    StaffPointsRule rule = new StaffPointsRule();
    rule.setBasePoints(TASK_COMPLETE_POINTS);
    rule.setScoreWeight(BigDecimal.ZERO);
    rule.setSuspiciousPenalty(0);
    rule.setFailPoints(0);
    return rule;
  }

  private int calculateTaskPoints(StaffPointsRule rule, boolean suspicious, boolean success) {
    int points = rule.getBasePoints() == null ? 0 : rule.getBasePoints();
    if (suspicious) {
      points += rule.getSuspiciousPenalty() == null ? 0 : rule.getSuspiciousPenalty();
    }
    if (!success) {
      points += rule.getFailPoints() == null ? 0 : rule.getFailPoints();
    }
    return points;
  }

  private int calculateReviewPoints(StaffPointsRule rule, Integer score) {
    if (score == null || rule.getScoreWeight() == null) {
      return 0;
    }
    return rule.getScoreWeight()
        .multiply(BigDecimal.valueOf(score))
        .setScale(0, RoundingMode.HALF_UP)
        .intValue();
  }
}
