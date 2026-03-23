package com.zhiyangyun.care.schedule.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.schedule.entity.StaffSchedule;
import com.zhiyangyun.care.schedule.entity.ShiftSwapRequest;
import com.zhiyangyun.care.schedule.mapper.StaffScheduleMapper;
import com.zhiyangyun.care.schedule.mapper.ShiftSwapRequestMapper;
import com.zhiyangyun.care.schedule.model.ScheduleRequest;
import com.zhiyangyun.care.schedule.model.ScheduleResponse;
import com.zhiyangyun.care.schedule.service.ScheduleService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ScheduleServiceImpl implements ScheduleService {
  private final StaffScheduleMapper scheduleMapper;
  private final StaffMapper staffMapper;
  private final OaTaskMapper taskMapper;
  private final OaTodoMapper todoMapper;
  private final ShiftSwapRequestMapper shiftSwapRequestMapper;

  public ScheduleServiceImpl(
      StaffScheduleMapper scheduleMapper,
      StaffMapper staffMapper,
      OaTaskMapper taskMapper,
      OaTodoMapper todoMapper,
      ShiftSwapRequestMapper shiftSwapRequestMapper) {
    this.scheduleMapper = scheduleMapper;
    this.staffMapper = staffMapper;
    this.taskMapper = taskMapper;
    this.todoMapper = todoMapper;
    this.shiftSwapRequestMapper = shiftSwapRequestMapper;
  }

  @Override
  public IPage<ScheduleResponse> page(Long orgId, Long requesterStaffId, boolean adminView, long pageNo, long pageSize, Long staffId,
      LocalDate dateFrom, LocalDate dateTo, Integer status, String sortBy, String sortOrder) {
    Long queryStaffId = staffId;
    if (!adminView && requesterStaffId != null) {
      queryStaffId = requesterStaffId;
    }
    var wrapper = Wrappers.lambdaQuery(StaffSchedule.class)
        .eq(StaffSchedule::getIsDeleted, 0)
        .eq(orgId != null, StaffSchedule::getOrgId, orgId)
        .eq(queryStaffId != null, StaffSchedule::getStaffId, queryStaffId)
        .ge(dateFrom != null, StaffSchedule::getDutyDate, dateFrom)
        .le(dateTo != null, StaffSchedule::getDutyDate, dateTo)
        .eq(status != null, StaffSchedule::getStatus, status);

    boolean asc = "asc".equalsIgnoreCase(sortOrder);
    if ("dutyDate".equals(sortBy)) {
      wrapper.orderBy(true, asc, StaffSchedule::getDutyDate);
    } else if ("createTime".equals(sortBy)) {
      wrapper.orderBy(true, asc, StaffSchedule::getCreateTime);
    } else {
      wrapper.orderByDesc(StaffSchedule::getDutyDate);
    }

    IPage<StaffSchedule> page = scheduleMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> staffIds = page.getRecords().stream().map(StaffSchedule::getStaffId).distinct().toList();
    Map<Long, StaffAccount> staffMap = staffIds.isEmpty()
        ? Collections.emptyMap()
        : staffMapper.selectBatchIdsSafe(staffIds).stream()
            .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));

    IPage<ScheduleResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(item -> {
      ScheduleResponse response = new ScheduleResponse();
      response.setId(item.getId());
      response.setStaffId(item.getStaffId());
      StaffAccount staff = staffMap.get(item.getStaffId());
      response.setStaffName(staff == null ? null : staff.getRealName());
      response.setSourceTemplateId(item.getSourceTemplateId());
      response.setSourceTemplateName(item.getSourceTemplateName());
      response.setDutyDate(item.getDutyDate());
      response.setShiftCode(item.getShiftCode());
      response.setStartTime(item.getStartTime());
      response.setEndTime(item.getEndTime());
      response.setCalendarTaskId(item.getCalendarTaskId());
      response.setDutyTodoId(item.getDutyTodoId());
      response.setStatus(item.getStatus());
      return response;
    }).toList());
    return resp;
  }

  @Override
  public IPage<ScheduleResponse> swapCandidatePage(
      Long orgId, Long requesterStaffId, long pageNo, long pageSize, Long targetStaffId, LocalDate dutyDate) {
    if (requesterStaffId == null || targetStaffId == null || dutyDate == null) {
      return new Page<>(pageNo, pageSize, 0);
    }
    var wrapper = Wrappers.lambdaQuery(StaffSchedule.class)
        .eq(StaffSchedule::getIsDeleted, 0)
        .eq(orgId != null, StaffSchedule::getOrgId, orgId)
        .eq(StaffSchedule::getStaffId, targetStaffId)
        .eq(StaffSchedule::getDutyDate, dutyDate)
        .eq(StaffSchedule::getStatus, 1)
        .orderByAsc(StaffSchedule::getStartTime)
        .orderByAsc(StaffSchedule::getId);
    IPage<StaffSchedule> page = scheduleMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    IPage<ScheduleResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(this::toResponse).toList());
    return resp;
  }

  @Override
  public ScheduleResponse create(Long orgId, Long staffId, ScheduleRequest request) {
    StaffSchedule schedule = new StaffSchedule();
    schedule.setTenantId(orgId);
    schedule.setOrgId(orgId);
    schedule.setStaffId(request.getStaffId());
    schedule.setSourceTemplateId(request.getSourceTemplateId());
    schedule.setSourceTemplateName(request.getSourceTemplateName());
    schedule.setDutyDate(request.getDutyDate());
    schedule.setShiftCode(request.getShiftCode());
    schedule.setStartTime(request.getStartTime());
    schedule.setEndTime(request.getEndTime());
    schedule.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    schedule.setCreatedBy(staffId);
    scheduleMapper.insert(schedule);
    syncScheduleArtifacts(schedule, staffId);
    return toResponse(schedule);
  }

  @Override
  public ScheduleResponse update(Long orgId, Long staffId, ScheduleRequest request) {
    StaffSchedule schedule = scheduleMapper.selectById(request.getId());
    if (schedule == null || (orgId != null && !orgId.equals(schedule.getOrgId()))) {
      return null;
    }
    if (request.getStaffId() != null) {
      schedule.setStaffId(request.getStaffId());
    }
    if (request.getSourceTemplateId() != null) {
      schedule.setSourceTemplateId(request.getSourceTemplateId());
    }
    if (request.getSourceTemplateName() != null) {
      schedule.setSourceTemplateName(request.getSourceTemplateName());
    }
    if (request.getDutyDate() != null) {
      schedule.setDutyDate(request.getDutyDate());
    }
    if (request.getShiftCode() != null) {
      schedule.setShiftCode(request.getShiftCode());
    }
    if (request.getStartTime() != null) {
      schedule.setStartTime(request.getStartTime());
    }
    if (request.getEndTime() != null) {
      schedule.setEndTime(request.getEndTime());
    }
    if (request.getStatus() != null) {
      schedule.setStatus(request.getStatus());
    }
    scheduleMapper.updateById(schedule);
    syncScheduleArtifacts(schedule, staffId);
    return toResponse(schedule);
  }

  @Override
  public void delete(Long orgId, Long id) {
    StaffSchedule schedule = scheduleMapper.selectById(id);
    if (schedule == null || (orgId != null && !orgId.equals(schedule.getOrgId()))) {
      return;
    }
    markScheduleArtifactsDeleted(schedule);
    schedule.setIsDeleted(1);
    scheduleMapper.updateById(schedule);
  }

  @Override
  public ScheduleResponse getOne(Long orgId, Long requesterStaffId, Long id, boolean adminView) {
    StaffSchedule schedule = scheduleMapper.selectById(id);
    if (schedule == null || (orgId != null && !orgId.equals(schedule.getOrgId()))
        || (schedule.getIsDeleted() != null && schedule.getIsDeleted() == 1)) {
      return null;
    }
    if (!adminView && requesterStaffId != null && !requesterStaffId.equals(schedule.getStaffId())) {
      return null;
    }
    return toResponse(schedule);
  }

  @Override
  public boolean applySwapByApproval(Long orgId, Long swapRequestId, Long operatorId, LocalDateTime approvedAt) {
    if (swapRequestId == null) {
      return false;
    }
    ShiftSwapRequest swap = shiftSwapRequestMapper.selectById(swapRequestId);
    if (swap == null || (swap.getIsDeleted() != null && swap.getIsDeleted() == 1)
        || (orgId != null && !orgId.equals(swap.getOrgId()))) {
      return false;
    }
    StaffSchedule applicant = scheduleMapper.selectById(swap.getApplicantScheduleId());
    StaffSchedule target = scheduleMapper.selectById(swap.getTargetScheduleId());
    if (applicant == null || target == null) {
      return false;
    }
    Long oldApplicantStaffId = applicant.getStaffId();
    Long oldTargetStaffId = target.getStaffId();
    applicant.setStaffId(oldTargetStaffId);
    target.setStaffId(oldApplicantStaffId);
    scheduleMapper.updateById(applicant);
    scheduleMapper.updateById(target);
    syncScheduleArtifacts(applicant, operatorId);
    syncScheduleArtifacts(target, operatorId);
    swap.setStatus("APPROVED");
    swap.setApprovalStatus("APPROVED");
    swap.setCompletedAt(approvedAt == null ? LocalDateTime.now() : approvedAt);
    shiftSwapRequestMapper.updateById(swap);
    return true;
  }

  private ScheduleResponse toResponse(StaffSchedule schedule) {
    ScheduleResponse response = new ScheduleResponse();
    response.setId(schedule.getId());
    response.setStaffId(schedule.getStaffId());
    StaffAccount staff = staffMapper.selectById(schedule.getStaffId());
    response.setStaffName(staff == null ? null : staff.getRealName());
    response.setSourceTemplateId(schedule.getSourceTemplateId());
    response.setSourceTemplateName(schedule.getSourceTemplateName());
    response.setDutyDate(schedule.getDutyDate());
    response.setShiftCode(schedule.getShiftCode());
    response.setStartTime(schedule.getStartTime());
    response.setEndTime(schedule.getEndTime());
    response.setCalendarTaskId(schedule.getCalendarTaskId());
    response.setDutyTodoId(schedule.getDutyTodoId());
    response.setStatus(schedule.getStatus());
    return response;
  }

  private void syncScheduleArtifacts(StaffSchedule schedule, Long operatorId) {
    if (schedule == null || schedule.getId() == null || schedule.getStaffId() == null) {
      return;
    }
    StaffAccount staff = staffMapper.selectById(schedule.getStaffId());
    if (staff == null) {
      return;
    }
    OaTask task = schedule.getCalendarTaskId() == null ? null : taskMapper.selectById(schedule.getCalendarTaskId());
    if (task == null) {
      task = new OaTask();
      task.setTenantId(schedule.getOrgId());
      task.setOrgId(schedule.getOrgId());
      task.setCreatedBy(operatorId);
      task.setSourceTodoId(schedule.getId());
      task.setPlanCategory("SHIFT_DUTY");
    }
    task.setTitle("值班排班：" + safe(staff.getRealName()) + " " + safe(schedule.getShiftCode()));
    task.setDescription("来源方案：" + safe(schedule.getSourceTemplateName()) + "，排班日期：" + schedule.getDutyDate());
    task.setStartTime(schedule.getStartTime());
    task.setEndTime(schedule.getEndTime());
    task.setPriority("NORMAL");
    task.setStatus("OPEN");
    task.setAssigneeId(staff.getId());
    task.setAssigneeName(staff.getRealName());
    task.setCalendarType("PERSONAL");
    task.setUrgency("NORMAL");
    task.setEventColor("#13c2c2");
    task.setCollaboratorIds(null);
    task.setCollaboratorNames(null);
    task.setIsRecurring(0);
    task.setRecurrenceRule(null);
    task.setRecurrenceInterval(null);
    task.setRecurrenceCount(null);
    if (task.getId() == null) {
      taskMapper.insert(task);
    } else {
      taskMapper.updateById(task);
    }
    schedule.setCalendarTaskId(task.getId());

    OaTodo todo = schedule.getDutyTodoId() == null ? null : todoMapper.selectById(schedule.getDutyTodoId());
    if (todo == null) {
      todo = new OaTodo();
      todo.setTenantId(schedule.getOrgId());
      todo.setOrgId(schedule.getOrgId());
      todo.setCreatedBy(operatorId);
    }
    todo.setTitle("【值班提醒】" + safe(schedule.getDutyDate()) + " " + safe(schedule.getShiftCode()));
    todo.setContent("[SHIFT_DUTY:" + schedule.getId() + "] 员工：" + safe(staff.getRealName())
        + "；班次：" + safe(schedule.getShiftCode())
        + "；开始时间：" + safe(schedule.getStartTime())
        + "；方案：" + safe(schedule.getSourceTemplateName()));
    todo.setDueTime(schedule.getStartTime() == null ? LocalDateTime.now() : schedule.getStartTime().minusHours(2));
    todo.setStatus("OPEN");
    todo.setAssigneeId(staff.getId());
    todo.setAssigneeName(staff.getRealName());
    if (todo.getId() == null) {
      todoMapper.insert(todo);
    } else {
      todoMapper.updateById(todo);
    }
    schedule.setDutyTodoId(todo.getId());
    scheduleMapper.updateById(schedule);
  }

  private void markScheduleArtifactsDeleted(StaffSchedule schedule) {
    if (schedule == null) {
      return;
    }
    if (schedule.getCalendarTaskId() != null) {
      OaTask task = taskMapper.selectById(schedule.getCalendarTaskId());
      if (task != null) {
        task.setIsDeleted(1);
        taskMapper.updateById(task);
      }
    }
    if (schedule.getDutyTodoId() != null) {
      OaTodo todo = todoMapper.selectById(schedule.getDutyTodoId());
      if (todo != null) {
        todo.setStatus("DONE");
        todoMapper.updateById(todo);
      }
    }
  }

  private String safe(Object value) {
    return value == null ? "" : String.valueOf(value);
  }
}
