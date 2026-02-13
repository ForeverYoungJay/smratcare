package com.zhiyangyun.care.schedule.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.schedule.entity.AttendanceRecord;
import com.zhiyangyun.care.schedule.mapper.AttendanceRecordMapper;
import com.zhiyangyun.care.schedule.model.AttendanceResponse;
import com.zhiyangyun.care.schedule.service.AttendanceService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AttendanceServiceImpl implements AttendanceService {
  private final AttendanceRecordMapper attendanceMapper;
  private final StaffMapper staffMapper;

  public AttendanceServiceImpl(AttendanceRecordMapper attendanceMapper, StaffMapper staffMapper) {
    this.attendanceMapper = attendanceMapper;
    this.staffMapper = staffMapper;
  }

  @Override
  public IPage<AttendanceResponse> page(Long orgId, long pageNo, long pageSize, Long staffId,
      LocalDate dateFrom, LocalDate dateTo, String status, String sortBy, String sortOrder) {
    LocalDateTime from = dateFrom == null ? null : dateFrom.atStartOfDay();
    LocalDateTime to = dateTo == null ? null : dateTo.atTime(LocalTime.MAX);

    var wrapper = Wrappers.lambdaQuery(AttendanceRecord.class)
        .eq(AttendanceRecord::getIsDeleted, 0)
        .eq(orgId != null, AttendanceRecord::getOrgId, orgId)
        .eq(staffId != null, AttendanceRecord::getStaffId, staffId)
        .ge(from != null, AttendanceRecord::getCheckInTime, from)
        .le(to != null, AttendanceRecord::getCheckInTime, to)
        .eq(status != null && !status.isBlank(), AttendanceRecord::getStatus, status);

    boolean asc = "asc".equalsIgnoreCase(sortOrder);
    if ("checkInTime".equals(sortBy)) {
      wrapper.orderBy(true, asc, AttendanceRecord::getCheckInTime);
    } else {
      wrapper.orderByDesc(AttendanceRecord::getCheckInTime);
    }

    IPage<AttendanceRecord> page = attendanceMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> staffIds = page.getRecords().stream().map(AttendanceRecord::getStaffId).distinct().toList();
    Map<Long, StaffAccount> staffMap = staffMapper.selectBatchIds(staffIds).stream()
        .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));

    IPage<AttendanceResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(item -> {
      AttendanceResponse response = new AttendanceResponse();
      response.setId(item.getId());
      response.setStaffId(item.getStaffId());
      StaffAccount staff = staffMap.get(item.getStaffId());
      response.setStaffName(staff == null ? null : staff.getRealName());
      response.setCheckInTime(item.getCheckInTime());
      response.setCheckOutTime(item.getCheckOutTime());
      response.setStatus(item.getStatus());
      return response;
    }).toList());
    return resp;
  }
}
