package com.zhiyangyun.care.nursing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.nursing.entity.ShiftTemplate;
import com.zhiyangyun.care.nursing.mapper.ShiftTemplateMapper;
import com.zhiyangyun.care.nursing.model.ShiftTemplateApplyRequest;
import com.zhiyangyun.care.nursing.model.ShiftTemplateBatchSaveRequest;
import com.zhiyangyun.care.nursing.model.ShiftTemplateRuleRequest;
import com.zhiyangyun.care.nursing.model.ShiftTemplateRequest;
import com.zhiyangyun.care.nursing.model.ShiftTemplateResponse;
import com.zhiyangyun.care.nursing.service.ShiftTemplateService;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.schedule.entity.AttendanceRecord;
import com.zhiyangyun.care.schedule.entity.StaffSchedule;
import com.zhiyangyun.care.schedule.mapper.AttendanceRecordMapper;
import com.zhiyangyun.care.schedule.mapper.StaffScheduleMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShiftTemplateServiceImpl implements ShiftTemplateService {
  private final ShiftTemplateMapper shiftTemplateMapper;
  private final StaffScheduleMapper staffScheduleMapper;
  private final AttendanceRecordMapper attendanceRecordMapper;
  private final StaffMapper staffMapper;
  private final OaTaskMapper oaTaskMapper;
  private final OaTodoMapper oaTodoMapper;

  public ShiftTemplateServiceImpl(ShiftTemplateMapper shiftTemplateMapper,
      StaffScheduleMapper staffScheduleMapper,
      AttendanceRecordMapper attendanceRecordMapper,
      StaffMapper staffMapper,
      OaTaskMapper oaTaskMapper,
      OaTodoMapper oaTodoMapper) {
    this.shiftTemplateMapper = shiftTemplateMapper;
    this.staffScheduleMapper = staffScheduleMapper;
    this.attendanceRecordMapper = attendanceRecordMapper;
    this.staffMapper = staffMapper;
    this.oaTaskMapper = oaTaskMapper;
    this.oaTodoMapper = oaTodoMapper;
  }

  @Override
  public ShiftTemplateResponse create(ShiftTemplateRequest request) {
    ShiftTemplate template = new ShiftTemplate();
    fillEntity(template, request);
    template.setCreatedBy(request.getCreatedBy());
    shiftTemplateMapper.insert(template);
    return toResponse(template);
  }

  @Override
  @Transactional
  public List<ShiftTemplateResponse> saveBatch(
      ShiftTemplateBatchSaveRequest request, Long tenantId, Long orgId, Long operatorId) {
    if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
      return List.of();
    }
    String schemeName = request.getName() == null ? null : request.getName().trim();
    if (schemeName == null || schemeName.isEmpty()) {
      throw new IllegalArgumentException("方案名称不能为空");
    }
    if (request.getReplaceExisting() == null || request.getReplaceExisting() == 1) {
      List<ShiftTemplate> existed = shiftTemplateMapper.selectList(Wrappers.lambdaQuery(ShiftTemplate.class)
          .eq(ShiftTemplate::getIsDeleted, 0)
          .eq(tenantId != null, ShiftTemplate::getTenantId, tenantId)
          .eq(ShiftTemplate::getName, schemeName));
      for (ShiftTemplate item : existed) {
        item.setIsDeleted(1);
        shiftTemplateMapper.updateById(item);
      }
    }
    List<ShiftTemplateResponse> responses = new ArrayList<>();
    int sort = 0;
    for (ShiftTemplateRuleRequest item : request.getItems()) {
      ShiftTemplate entity = item.getId() == null ? new ShiftTemplate() : shiftTemplateMapper.selectById(item.getId());
      if (entity == null) {
        entity = new ShiftTemplate();
      }
      entity.setTenantId(tenantId);
      entity.setOrgId(orgId);
      entity.setName(schemeName);
      entity.setIsDeleted(0);
      entity.setRuleSort(sort++);
      entity.setShiftCode(item.getShiftCode());
      entity.setStartTime(item.getStartTime());
      entity.setEndTime(item.getEndTime());
      entity.setCrossDay(item.getCrossDay() == null ? 0 : item.getCrossDay());
      entity.setRequiredStaffCount(item.getRequiredStaffCount() == null ? 1 : item.getRequiredStaffCount());
      entity.setRecurrenceType(item.getRecurrenceType() == null || item.getRecurrenceType().isBlank()
          ? "WEEKLY_ONCE" : item.getRecurrenceType());
      entity.setExecuteStaffId(item.getExecuteStaffId());
      entity.setAttendanceLinked(item.getAttendanceLinked() == null ? 1 : item.getAttendanceLinked());
      entity.setEnabled(item.getEnabled() == null ? (request.getEnabled() == null ? 1 : request.getEnabled()) : item.getEnabled());
      entity.setRemark(item.getRemark() == null || item.getRemark().isBlank() ? request.getRemark() : item.getRemark());
      entity.setCreatedBy(operatorId);
      if (entity.getId() == null) {
        shiftTemplateMapper.insert(entity);
      } else {
        shiftTemplateMapper.updateById(entity);
      }
      responses.add(toResponse(entity));
    }
    return responses;
  }

  @Override
  public ShiftTemplateResponse update(Long id, ShiftTemplateRequest request) {
    ShiftTemplate template = shiftTemplateMapper.selectById(id);
    if (template == null || !request.getTenantId().equals(template.getTenantId())) {
      return null;
    }
    fillEntity(template, request);
    shiftTemplateMapper.updateById(template);
    return toResponse(template);
  }

  @Override
  public ShiftTemplateResponse get(Long id, Long tenantId) {
    ShiftTemplate template = shiftTemplateMapper.selectById(id);
    if (template == null || (tenantId != null && !tenantId.equals(template.getTenantId()))) {
      return null;
    }
    return toResponse(template);
  }

  @Override
  public IPage<ShiftTemplateResponse> page(Long tenantId, long pageNo, long pageSize, String keyword,
      Integer enabled) {
    var wrapper = Wrappers.lambdaQuery(ShiftTemplate.class)
        .eq(ShiftTemplate::getIsDeleted, 0)
        .eq(tenantId != null, ShiftTemplate::getTenantId, tenantId)
        .eq(enabled != null, ShiftTemplate::getEnabled, enabled)
        .orderByAsc(ShiftTemplate::getName)
        .orderByAsc(ShiftTemplate::getRuleSort)
        .orderByAsc(ShiftTemplate::getShiftCode);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(ShiftTemplate::getName, keyword).or().like(ShiftTemplate::getShiftCode, keyword));
    }
    IPage<ShiftTemplate> page = shiftTemplateMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  public List<ShiftTemplateResponse> list(Long tenantId, Integer enabled) {
    var wrapper = Wrappers.lambdaQuery(ShiftTemplate.class)
        .eq(ShiftTemplate::getIsDeleted, 0)
        .eq(tenantId != null, ShiftTemplate::getTenantId, tenantId)
        .eq(enabled != null, ShiftTemplate::getEnabled, enabled)
        .orderByAsc(ShiftTemplate::getName)
        .orderByAsc(ShiftTemplate::getRuleSort)
        .orderByAsc(ShiftTemplate::getShiftCode);
    return shiftTemplateMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  @Transactional
  public int apply(Long id, Long tenantId, Long operatorId, ShiftTemplateApplyRequest request) {
    ShiftTemplate seed = shiftTemplateMapper.selectById(id);
    if (seed == null || !Objects.equals(seed.getTenantId(), tenantId)) {
      return 0;
    }
    LocalDate start = request.getStartDate();
    LocalDate end = request.getEndDate();
    if (start == null || end == null || end.isBefore(start)) {
      throw new IllegalArgumentException("实施日期范围不合法");
    }
    var templateWrapper = Wrappers.lambdaQuery(ShiftTemplate.class)
        .eq(ShiftTemplate::getIsDeleted, 0)
        .eq(ShiftTemplate::getTenantId, tenantId)
        .eq(ShiftTemplate::getName, seed.getName())
        .eq(ShiftTemplate::getEnabled, 1)
        .orderByAsc(ShiftTemplate::getRuleSort)
        .orderByAsc(ShiftTemplate::getShiftCode);
    List<ShiftTemplate> templates = shiftTemplateMapper.selectList(templateWrapper);
    if (templates.isEmpty()) {
      templates = List.of(seed);
    }

    int created = 0;
    for (ShiftTemplate template : templates) {
      if (template.getExecuteStaffId() == null) {
        continue;
      }
      for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
        if (!shouldApplyOnDate(template.getRecurrenceType(), start, date)) {
          continue;
        }
        long exists = staffScheduleMapper.selectCount(
            Wrappers.lambdaQuery(StaffSchedule.class)
                .eq(StaffSchedule::getIsDeleted, 0)
                .eq(StaffSchedule::getTenantId, tenantId)
                .eq(StaffSchedule::getStaffId, template.getExecuteStaffId())
                .eq(StaffSchedule::getDutyDate, date)
                .eq(StaffSchedule::getShiftCode, template.getShiftCode()));
        if (exists > 0) {
          continue;
        }

        StaffSchedule schedule = new StaffSchedule();
        schedule.setTenantId(template.getTenantId());
        schedule.setOrgId(template.getOrgId());
        schedule.setStaffId(template.getExecuteStaffId());
        schedule.setSourceTemplateId(template.getId());
        schedule.setSourceTemplateName(template.getName());
        schedule.setDutyDate(date);
        schedule.setShiftCode(template.getShiftCode());
        schedule.setStartTime(LocalDateTime.of(date, template.getStartTime()));
        LocalDate endDate = template.getCrossDay() != null && template.getCrossDay() == 1 ? date.plusDays(1) : date;
        schedule.setEndTime(LocalDateTime.of(endDate, template.getEndTime()));
        schedule.setStatus(1);
        schedule.setCreatedBy(operatorId);
        staffScheduleMapper.insert(schedule);
        syncScheduleArtifacts(schedule, template, operatorId);
        created++;

        if (template.getAttendanceLinked() != null && template.getAttendanceLinked() == 1) {
          long attendanceExists = attendanceRecordMapper.selectCount(
              Wrappers.lambdaQuery(AttendanceRecord.class)
                  .eq(AttendanceRecord::getIsDeleted, 0)
                  .eq(AttendanceRecord::getTenantId, tenantId)
                  .eq(AttendanceRecord::getStaffId, template.getExecuteStaffId())
                  .eq(AttendanceRecord::getStatus, "SCHEDULED")
                  .ge(AttendanceRecord::getCreateTime, LocalDateTime.of(date, LocalTime.MIN))
                  .le(AttendanceRecord::getCreateTime, LocalDateTime.of(date, LocalTime.MAX)));
          if (attendanceExists == 0) {
            AttendanceRecord attendance = new AttendanceRecord();
            attendance.setTenantId(template.getTenantId());
            attendance.setOrgId(template.getOrgId());
            attendance.setStaffId(template.getExecuteStaffId());
            attendance.setStatus("SCHEDULED");
            attendance.setCreatedBy(operatorId);
            attendanceRecordMapper.insert(attendance);
          }
        }
      }
    }
    return created;
  }

  @Override
  public void delete(Long id, Long tenantId) {
    ShiftTemplate template = shiftTemplateMapper.selectById(id);
    if (template != null && tenantId != null && tenantId.equals(template.getTenantId())) {
      template.setIsDeleted(1);
      shiftTemplateMapper.updateById(template);
    }
  }

  private void fillEntity(ShiftTemplate template, ShiftTemplateRequest request) {
    template.setTenantId(request.getTenantId());
    template.setOrgId(request.getOrgId());
    template.setName(request.getName());
    template.setRuleSort(request.getRuleSort() == null ? 0 : request.getRuleSort());
    template.setShiftCode(request.getShiftCode());
    template.setStartTime(request.getStartTime());
    template.setEndTime(request.getEndTime());
    template.setCrossDay(request.getCrossDay() == null ? 0 : request.getCrossDay());
    template.setRequiredStaffCount(request.getRequiredStaffCount() == null ? 1 : request.getRequiredStaffCount());
    template.setRecurrenceType(request.getRecurrenceType() == null || request.getRecurrenceType().isBlank()
        ? "WEEKLY_ONCE" : request.getRecurrenceType());
    template.setExecuteStaffId(request.getExecuteStaffId());
    template.setAttendanceLinked(request.getAttendanceLinked() == null ? 1 : request.getAttendanceLinked());
    template.setEnabled(request.getEnabled() == null ? 1 : request.getEnabled());
    template.setRemark(request.getRemark());
  }

  private ShiftTemplateResponse toResponse(ShiftTemplate template) {
    ShiftTemplateResponse response = new ShiftTemplateResponse();
    response.setId(template.getId());
    response.setTenantId(template.getTenantId());
    response.setOrgId(template.getOrgId());
    response.setName(template.getName());
    response.setRuleSort(template.getRuleSort());
    response.setShiftCode(template.getShiftCode());
    response.setStartTime(template.getStartTime());
    response.setEndTime(template.getEndTime());
    response.setCrossDay(template.getCrossDay());
    response.setRequiredStaffCount(template.getRequiredStaffCount());
    response.setRecurrenceType(template.getRecurrenceType());
    response.setExecuteStaffId(template.getExecuteStaffId());
    response.setAttendanceLinked(template.getAttendanceLinked());
    if (template.getExecuteStaffId() != null) {
      StaffAccount staff = staffMapper.selectById(template.getExecuteStaffId());
      response.setExecuteStaffName(staff == null ? null : staff.getRealName());
    }
    response.setEnabled(template.getEnabled());
    response.setRemark(template.getRemark());
    return response;
  }

  private boolean shouldApplyOnDate(String recurrenceType, LocalDate startDate, LocalDate date) {
    if ("DAILY_ONCE".equalsIgnoreCase(recurrenceType)) {
      return true;
    }
    if ("WEEKLY_TWICE".equalsIgnoreCase(recurrenceType)) {
      int diff = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, date);
      if (diff < 0) {
        return false;
      }
      int mod = diff % 7;
      return mod == 0 || mod == 3;
    }
    int startDow = startDate.getDayOfWeek().getValue();
    int currentDow = date.getDayOfWeek().getValue();
    return startDow == currentDow;
  }

  private void syncScheduleArtifacts(StaffSchedule schedule, ShiftTemplate template, Long operatorId) {
    if (schedule == null || schedule.getId() == null || schedule.getStaffId() == null) {
      return;
    }
    StaffAccount staff = staffMapper.selectById(schedule.getStaffId());
    if (staff == null) {
      return;
    }
    OaTask task = schedule.getCalendarTaskId() == null ? null : oaTaskMapper.selectById(schedule.getCalendarTaskId());
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
      oaTaskMapper.insert(task);
    } else {
      oaTaskMapper.updateById(task);
    }
    schedule.setCalendarTaskId(task.getId());

    OaTodo todo = schedule.getDutyTodoId() == null ? null : oaTodoMapper.selectById(schedule.getDutyTodoId());
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
    LocalDateTime dueTime = schedule.getStartTime() == null ? null : schedule.getStartTime().minusHours(2);
    todo.setDueTime(dueTime == null ? LocalDateTime.now() : dueTime);
    todo.setStatus("OPEN");
    todo.setAssigneeId(staff.getId());
    todo.setAssigneeName(staff.getRealName());
    if (todo.getId() == null) {
      oaTodoMapper.insert(todo);
    } else {
      oaTodoMapper.updateById(todo);
    }
    schedule.setDutyTodoId(todo.getId());
    staffScheduleMapper.updateById(schedule);
  }

  private String safe(Object value) {
    return value == null ? "" : String.valueOf(value);
  }
}
