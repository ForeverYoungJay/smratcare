package com.zhiyangyun.care.nursing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.nursing.entity.ShiftTemplate;
import com.zhiyangyun.care.nursing.mapper.ShiftTemplateMapper;
import com.zhiyangyun.care.nursing.model.ShiftTemplateApplyRequest;
import com.zhiyangyun.care.nursing.model.ShiftTemplateRequest;
import com.zhiyangyun.care.nursing.model.ShiftTemplateResponse;
import com.zhiyangyun.care.nursing.service.ShiftTemplateService;
import com.zhiyangyun.care.schedule.entity.AttendanceRecord;
import com.zhiyangyun.care.schedule.entity.StaffSchedule;
import com.zhiyangyun.care.schedule.mapper.AttendanceRecordMapper;
import com.zhiyangyun.care.schedule.mapper.StaffScheduleMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

  public ShiftTemplateServiceImpl(ShiftTemplateMapper shiftTemplateMapper,
      StaffScheduleMapper staffScheduleMapper,
      AttendanceRecordMapper attendanceRecordMapper,
      StaffMapper staffMapper) {
    this.shiftTemplateMapper = shiftTemplateMapper;
    this.staffScheduleMapper = staffScheduleMapper;
    this.attendanceRecordMapper = attendanceRecordMapper;
    this.staffMapper = staffMapper;
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
        .orderByAsc(ShiftTemplate::getShiftCode);
    return shiftTemplateMapper.selectList(wrapper).stream().map(this::toResponse).toList();
  }

  @Override
  @Transactional
  public int apply(Long id, Long tenantId, Long operatorId, ShiftTemplateApplyRequest request) {
    ShiftTemplate template = shiftTemplateMapper.selectById(id);
    if (template == null || !Objects.equals(template.getTenantId(), tenantId)) {
      return 0;
    }
    if (template.getExecuteStaffId() == null) {
      throw new IllegalArgumentException("排班方案未设置执行人");
    }
    LocalDate start = request.getStartDate();
    LocalDate end = request.getEndDate();
    if (start == null || end == null || end.isBefore(start)) {
      throw new IllegalArgumentException("实施日期范围不合法");
    }

    int created = 0;
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
      schedule.setDutyDate(date);
      schedule.setShiftCode(template.getShiftCode());
      schedule.setStartTime(LocalDateTime.of(date, template.getStartTime()));
      LocalDate endDate = template.getCrossDay() != null && template.getCrossDay() == 1 ? date.plusDays(1) : date;
      schedule.setEndTime(LocalDateTime.of(endDate, template.getEndTime()));
      schedule.setStatus(1);
      schedule.setCreatedBy(operatorId);
      staffScheduleMapper.insert(schedule);
      created++;

      if (template.getAttendanceLinked() != null && template.getAttendanceLinked() == 1) {
        long attendanceExists = attendanceRecordMapper.selectCount(
            Wrappers.lambdaQuery(AttendanceRecord.class)
                .eq(AttendanceRecord::getIsDeleted, 0)
                .eq(AttendanceRecord::getTenantId, tenantId)
                .eq(AttendanceRecord::getStaffId, template.getExecuteStaffId())
                .eq(AttendanceRecord::getStatus, "SCHEDULED")
                .ge(AttendanceRecord::getCreateTime, LocalDateTime.of(date, java.time.LocalTime.MIN))
                .le(AttendanceRecord::getCreateTime, LocalDateTime.of(date, java.time.LocalTime.MAX)));
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
}
