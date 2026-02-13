package com.zhiyangyun.care.schedule.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.schedule.entity.StaffSchedule;
import com.zhiyangyun.care.schedule.mapper.StaffScheduleMapper;
import com.zhiyangyun.care.schedule.model.ScheduleRequest;
import com.zhiyangyun.care.schedule.model.ScheduleResponse;
import com.zhiyangyun.care.schedule.service.ScheduleService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ScheduleServiceImpl implements ScheduleService {
  private final StaffScheduleMapper scheduleMapper;
  private final StaffMapper staffMapper;

  public ScheduleServiceImpl(StaffScheduleMapper scheduleMapper, StaffMapper staffMapper) {
    this.scheduleMapper = scheduleMapper;
    this.staffMapper = staffMapper;
  }

  @Override
  public IPage<ScheduleResponse> page(Long orgId, long pageNo, long pageSize, Long staffId,
      LocalDate dateFrom, LocalDate dateTo, Integer status, String sortBy, String sortOrder) {
    var wrapper = Wrappers.lambdaQuery(StaffSchedule.class)
        .eq(StaffSchedule::getIsDeleted, 0)
        .eq(orgId != null, StaffSchedule::getOrgId, orgId)
        .eq(staffId != null, StaffSchedule::getStaffId, staffId)
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
    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIds(staffIds).stream()
        .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));

    IPage<ScheduleResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(item -> {
      ScheduleResponse response = new ScheduleResponse();
      response.setId(item.getId());
      response.setStaffId(item.getStaffId());
      StaffAccount staff = staffMap.get(item.getStaffId());
      response.setStaffName(staff == null ? null : staff.getRealName());
      response.setDutyDate(item.getDutyDate());
      response.setShiftCode(item.getShiftCode());
      response.setStartTime(item.getStartTime());
      response.setEndTime(item.getEndTime());
      response.setStatus(item.getStatus());
      return response;
    }).toList());
    return resp;
  }

  @Override
  public ScheduleResponse create(Long orgId, Long staffId, ScheduleRequest request) {
    StaffSchedule schedule = new StaffSchedule();
    schedule.setTenantId(orgId);
    schedule.setOrgId(orgId);
    schedule.setStaffId(request.getStaffId());
    schedule.setDutyDate(request.getDutyDate());
    schedule.setShiftCode(request.getShiftCode());
    schedule.setStartTime(request.getStartTime());
    schedule.setEndTime(request.getEndTime());
    schedule.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    schedule.setCreatedBy(staffId);
    scheduleMapper.insert(schedule);
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
    return toResponse(schedule);
  }

  @Override
  public void delete(Long orgId, Long id) {
    StaffSchedule schedule = scheduleMapper.selectById(id);
    if (schedule == null || (orgId != null && !orgId.equals(schedule.getOrgId()))) {
      return;
    }
    schedule.setIsDeleted(1);
    scheduleMapper.updateById(schedule);
  }

  private ScheduleResponse toResponse(StaffSchedule schedule) {
    ScheduleResponse response = new ScheduleResponse();
    response.setId(schedule.getId());
    response.setStaffId(schedule.getStaffId());
    StaffAccount staff = staffMapper.selectById(schedule.getStaffId());
    response.setStaffName(staff == null ? null : staff.getRealName());
    response.setDutyDate(schedule.getDutyDate());
    response.setShiftCode(schedule.getShiftCode());
    response.setStartTime(schedule.getStartTime());
    response.setEndTime(schedule.getEndTime());
    response.setStatus(schedule.getStatus());
    return response;
  }
}
