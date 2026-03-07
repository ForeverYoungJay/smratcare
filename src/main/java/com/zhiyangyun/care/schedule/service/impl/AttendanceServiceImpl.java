package com.zhiyangyun.care.schedule.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.entity.OaTask;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import com.zhiyangyun.care.oa.mapper.OaTaskMapper;
import com.zhiyangyun.care.schedule.entity.AttendanceRecord;
import com.zhiyangyun.care.schedule.entity.AttendanceSeasonRule;
import com.zhiyangyun.care.schedule.mapper.AttendanceRecordMapper;
import com.zhiyangyun.care.schedule.mapper.AttendanceSeasonRuleMapper;
import com.zhiyangyun.care.schedule.model.AttendanceDashboardDayItem;
import com.zhiyangyun.care.schedule.model.AttendanceDashboardOverviewResponse;
import com.zhiyangyun.care.schedule.model.AttendanceResponse;
import com.zhiyangyun.care.schedule.model.AttendanceSeasonRuleRequest;
import com.zhiyangyun.care.schedule.model.AttendanceSeasonRuleResponse;
import com.zhiyangyun.care.schedule.model.AttendanceStaffAvailabilityResponse;
import com.zhiyangyun.care.schedule.service.AttendanceService;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AttendanceServiceImpl implements AttendanceService {
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
  private static final String DEFAULT_WINTER_START = "13:30";
  private static final String DEFAULT_WINTER_END = "17:00";
  private static final String DEFAULT_SUMMER_START = "14:30";
  private static final String DEFAULT_SUMMER_END = "18:00";
  private static final int DEFAULT_GRACE_MINUTES = 10;
  private static final int DEFAULT_OUTING_MAX_MINUTES = 180;
  private static final int DEFAULT_RULE_ENABLED = 1;

  private final AttendanceRecordMapper attendanceMapper;
  private final AttendanceSeasonRuleMapper seasonRuleMapper;
  private final StaffMapper staffMapper;
  private final OaApprovalMapper approvalMapper;
  private final OaTaskMapper taskMapper;
  private final ObjectMapper objectMapper;

  public AttendanceServiceImpl(
      AttendanceRecordMapper attendanceMapper,
      AttendanceSeasonRuleMapper seasonRuleMapper,
      StaffMapper staffMapper,
      OaApprovalMapper approvalMapper,
      OaTaskMapper taskMapper,
      ObjectMapper objectMapper) {
    this.attendanceMapper = attendanceMapper;
    this.seasonRuleMapper = seasonRuleMapper;
    this.staffMapper = staffMapper;
    this.approvalMapper = approvalMapper;
    this.taskMapper = taskMapper;
    this.objectMapper = objectMapper;
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
        .eq(status != null && !status.isBlank(), AttendanceRecord::getStatus, status);
    if (from != null || to != null) {
      wrapper.and(w -> w.ge(from != null, AttendanceRecord::getWorkDate, dateFrom)
          .le(to != null, AttendanceRecord::getWorkDate, dateTo)
          .or()
          .ge(from != null, AttendanceRecord::getCheckInTime, from)
          .le(to != null, AttendanceRecord::getCheckInTime, to));
    }

    boolean asc = "asc".equalsIgnoreCase(sortOrder);
    if ("checkInTime".equals(sortBy)) {
      wrapper.orderBy(true, asc, AttendanceRecord::getCheckInTime);
    } else if ("workDate".equals(sortBy)) {
      wrapper.orderBy(true, asc, AttendanceRecord::getWorkDate);
    } else {
      wrapper.orderByDesc(AttendanceRecord::getWorkDate).orderByDesc(AttendanceRecord::getCheckInTime);
    }

    IPage<AttendanceRecord> page = attendanceMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> staffIds = page.getRecords().stream().map(AttendanceRecord::getStaffId).distinct().toList();
    Map<Long, StaffAccount> staffMap = staffIds.isEmpty()
        ? Collections.emptyMap()
        : staffMapper.selectBatchIdsSafe(staffIds).stream()
            .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));

    IPage<AttendanceResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(item -> {
      AttendanceResponse response = new AttendanceResponse();
      response.setId(item.getId());
      response.setStaffId(item.getStaffId());
      StaffAccount staff = staffMap.get(item.getStaffId());
      response.setStaffName(staff == null ? null : staff.getRealName());
      response.setWorkDate(resolveWorkDate(item));
      response.setCheckInTime(item.getCheckInTime());
      response.setCheckOutTime(item.getCheckOutTime());
      response.setOutingStartTime(item.getOutingStartTime());
      response.setOutingEndTime(item.getOutingEndTime());
      response.setLunchBreakStartTime(item.getLunchBreakStartTime());
      response.setLunchBreakEndTime(item.getLunchBreakEndTime());
      response.setOutingMinutes(safeInt(item.getOutingMinutes()));
      response.setReviewed(item.getReviewed());
      response.setReviewedBy(item.getReviewedBy());
      response.setReviewedAt(item.getReviewedAt());
      response.setReviewRemark(item.getReviewRemark());
      response.setNote(item.getNote());
      response.setStatus(item.getStatus());
      return response;
    }).toList());
    return resp;
  }

  @Override
  public AttendanceDashboardOverviewResponse overview(Long orgId, Long currentStaffId, Long queryStaffId, YearMonth month) {
    Long targetStaffId = queryStaffId != null ? queryStaffId : currentStaffId;
    if (targetStaffId == null) {
      throw new IllegalArgumentException("staffId required");
    }
    YearMonth targetMonth = month == null ? YearMonth.now() : month;
    LocalDate monthStart = targetMonth.atDay(1);
    LocalDate monthEnd = targetMonth.atEndOfMonth();

    List<AttendanceRecord> records = attendanceMapper.selectList(Wrappers.lambdaQuery(AttendanceRecord.class)
        .eq(AttendanceRecord::getIsDeleted, 0)
        .eq(orgId != null, AttendanceRecord::getOrgId, orgId)
        .eq(AttendanceRecord::getStaffId, targetStaffId)
        .and(w -> w.ge(AttendanceRecord::getWorkDate, monthStart)
            .le(AttendanceRecord::getWorkDate, monthEnd)
            .or()
            .ge(AttendanceRecord::getCheckInTime, monthStart.atStartOfDay())
            .lt(AttendanceRecord::getCheckInTime, monthEnd.plusDays(1).atStartOfDay()))
        .orderByAsc(AttendanceRecord::getWorkDate)
        .orderByAsc(AttendanceRecord::getCheckInTime));

    List<OaApproval> leaves = approvalMapper.selectList(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getApplicantId, targetStaffId)
        .eq(OaApproval::getApprovalType, "LEAVE")
        .eq(OaApproval::getStatus, "APPROVED")
        .isNotNull(OaApproval::getStartTime)
        .isNotNull(OaApproval::getEndTime)
        .le(OaApproval::getStartTime, monthEnd.plusDays(1).atStartOfDay())
        .ge(OaApproval::getEndTime, monthStart.atStartOfDay())
        .orderByAsc(OaApproval::getStartTime));

    List<OaTask> tasks = taskMapper.selectList(Wrappers.lambdaQuery(OaTask.class)
        .eq(OaTask::getIsDeleted, 0)
        .eq(orgId != null, OaTask::getOrgId, orgId)
        .eq(OaTask::getAssigneeId, targetStaffId)
        .isNotNull(OaTask::getStartTime)
        .ge(OaTask::getStartTime, monthStart.atStartOfDay())
        .lt(OaTask::getStartTime, monthEnd.plusDays(1).atStartOfDay())
        .orderByAsc(OaTask::getStartTime));

    AttendanceSeasonRule seasonRule = getOrCreateRule(orgId, null);
    SeasonalWorkWindow window = resolveSeasonalWindow(targetMonth, seasonRule);
    Map<LocalDate, AttendanceDashboardDayItem> dayMap = new LinkedHashMap<>();
    for (AttendanceRecord record : records) {
      LocalDate date = resolveWorkDate(record);
      if (date == null) {
        continue;
      }
      AttendanceDashboardDayItem day = dayMap.computeIfAbsent(date, this::emptyDay);
      mergeRecordIntoDay(day, record, window);
    }

    for (OaApproval leave : leaves) {
      LocalDate from = leave.getStartTime().toLocalDate();
      LocalDate to = leave.getEndTime().toLocalDate();
      if (from.isBefore(monthStart)) {
        from = monthStart;
      }
      if (to.isAfter(monthEnd)) {
        to = monthEnd;
      }
      for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
        AttendanceDashboardDayItem day = dayMap.computeIfAbsent(date, this::emptyDay);
        day.setHasLeave(true);
        day.setLeaveTitle(leave.getTitle());
        day.setLeaveType(extractLeaveType(leave.getFormData()));
      }
    }

    for (OaTask task : tasks) {
      if (task.getStartTime() == null) {
        continue;
      }
      LocalDate date = task.getStartTime().toLocalDate();
      if (date.isBefore(monthStart) || date.isAfter(monthEnd)) {
        continue;
      }
      AttendanceDashboardDayItem day = dayMap.computeIfAbsent(date, this::emptyDay);
      String current = day.getScheduleTitles();
      day.setScheduleTitles(current == null || current.isBlank() ? safe(task.getTitle()) : current + "、" + safe(task.getTitle()));
    }

    List<AttendanceDashboardDayItem> days = dayMap.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(Map.Entry::getValue)
        .toList();

    StaffAccount staff = staffMapper.selectById(targetStaffId);
    AttendanceDashboardOverviewResponse response = new AttendanceDashboardOverviewResponse();
    response.setStaffId(targetStaffId);
    response.setStaffName(staff == null ? null : staff.getRealName());
    response.setMonth(targetMonth.toString());
    response.setSeasonType(window.seasonType());
    response.setExpectedWorkStart(window.start().format(TIME_FORMAT));
    response.setExpectedWorkEnd(window.end().format(TIME_FORMAT));
    response.setLateGraceMinutes(window.lateGraceMinutes());
    response.setEarlyLeaveGraceMinutes(window.earlyLeaveGraceMinutes());
    response.setOutingMaxMinutes(window.outingMaxMinutes());
    response.setOnDutyDays(days.stream().filter(item -> item.getCheckInTime() != null).count());
    response.setLeaveDays(days.stream().filter(item -> Boolean.TRUE.equals(item.getHasLeave())).count());
    response.setLateCount(days.stream().filter(item -> Boolean.TRUE.equals(item.getLate())).count());
    response.setEarlyLeaveCount(days.stream().filter(item -> Boolean.TRUE.equals(item.getEarlyLeave())).count());
    response.setAbnormalCount(days.stream().filter(item -> item.getAnomalyText() != null && !item.getAnomalyText().isBlank()).count());
    response.setTotalOutingMinutes(days.stream().map(AttendanceDashboardDayItem::getOutingMinutes).filter(Objects::nonNull).mapToInt(Integer::intValue).sum());
    response.setTotalLunchBreakMinutes(days.stream().map(AttendanceDashboardDayItem::getLunchBreakMinutes).filter(Objects::nonNull).mapToInt(Integer::intValue).sum());
    TodayStatus todayStatus = resolveTodayStatus(targetStaffId, orgId);
    response.setTodayStatus(todayStatus.code());
    response.setTodayStatusLabel(todayStatus.label());
    response.setDays(days);
    return response;
  }

  @Override
  public AttendanceSeasonRuleResponse getSeasonRule(Long orgId) {
    AttendanceSeasonRule rule = getOrCreateRule(orgId, null);
    return toSeasonRuleResponse(rule);
  }

  @Override
  public AttendanceSeasonRuleResponse saveSeasonRule(Long orgId, Long currentStaffId, AttendanceSeasonRuleRequest request) {
    AttendanceSeasonRule rule = getOrCreateRule(orgId, currentStaffId);
    if (request != null) {
      rule.setWinterWorkStart(normalizeClock(request.getWinterWorkStart(), DEFAULT_WINTER_START));
      rule.setWinterWorkEnd(normalizeClock(request.getWinterWorkEnd(), DEFAULT_WINTER_END));
      rule.setSummerWorkStart(normalizeClock(request.getSummerWorkStart(), DEFAULT_SUMMER_START));
      rule.setSummerWorkEnd(normalizeClock(request.getSummerWorkEnd(), DEFAULT_SUMMER_END));
      rule.setLateGraceMinutes(sanitizePositiveOrDefault(request.getLateGraceMinutes(), DEFAULT_GRACE_MINUTES));
      rule.setEarlyLeaveGraceMinutes(sanitizePositiveOrDefault(request.getEarlyLeaveGraceMinutes(), DEFAULT_GRACE_MINUTES));
      rule.setOutingMaxMinutes(sanitizePositiveOrDefault(request.getOutingMaxMinutes(), DEFAULT_OUTING_MAX_MINUTES));
      rule.setLateEnabled(normalizeEnabledFlag(request.getLateEnabled()));
      rule.setEarlyLeaveEnabled(normalizeEnabledFlag(request.getEarlyLeaveEnabled()));
      rule.setOutingOvertimeEnabled(normalizeEnabledFlag(request.getOutingOvertimeEnabled()));
      rule.setMissingCheckoutEnabled(normalizeEnabledFlag(request.getMissingCheckoutEnabled()));
      rule.setEnabledStatus(normalizeEnabledStatus(request.getEnabledStatus()));
      seasonRuleMapper.updateById(rule);
    }
    return toSeasonRuleResponse(rule);
  }

  @Override
  public AttendanceStaffAvailabilityResponse staffAvailability(Long orgId, Long staffId) {
    if (staffId == null) {
      throw new IllegalArgumentException("staffId required");
    }
    StaffAccount staff = staffMapper.selectById(staffId);
    if (staff == null || (staff.getIsDeleted() != null && staff.getIsDeleted() == 1)) {
      throw new IllegalArgumentException("员工不存在");
    }
    LocalDateTime now = LocalDateTime.now();
    OaApproval leave = approvalMapper.selectOne(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getApplicantId, staffId)
        .eq(OaApproval::getApprovalType, "LEAVE")
        .eq(OaApproval::getStatus, "APPROVED")
        .le(OaApproval::getStartTime, now)
        .ge(OaApproval::getEndTime, now)
        .orderByDesc(OaApproval::getStartTime)
        .last("LIMIT 1"));
    AttendanceStaffAvailabilityResponse response = new AttendanceStaffAvailabilityResponse();
    response.setStaffId(staffId);
    response.setStaffName(staff.getRealName());
    response.setStaffNo(staff.getStaffNo());
    response.setContactPhone(staff.getPhone());
    if (leave == null) {
      response.setAvailable(true);
      response.setStatus("AVAILABLE");
      response.setMessage(staff.getRealName() + "当前可联系");
      return response;
    }
    String employeeNo = safe(staff.getStaffNo()).isBlank() ? safe(staff.getPhone()) : safe(staff.getStaffNo());
    response.setAvailable(false);
    response.setStatus("ON_LEAVE");
    response.setMessage("您联系的" + safe(staff.getRealName()) + "在休假中，急事请电联，员工号码" + employeeNo);
    return response;
  }

  @Override
  public AttendanceResponse reviewRecord(Long orgId, Long currentStaffId, Long id, Integer reviewed, String reviewRemark) {
    if (id == null) {
      throw new IllegalArgumentException("id required");
    }
    AttendanceRecord record = attendanceMapper.selectOne(Wrappers.lambdaQuery(AttendanceRecord.class)
        .eq(AttendanceRecord::getId, id)
        .eq(AttendanceRecord::getIsDeleted, 0)
        .eq(orgId != null, AttendanceRecord::getOrgId, orgId)
        .last("LIMIT 1"));
    if (record == null) {
      throw new IllegalArgumentException("考勤记录不存在");
    }
    record.setReviewed(normalizeEnabledFlag(reviewed));
    record.setReviewedBy(currentStaffId);
    record.setReviewedAt(LocalDateTime.now());
    record.setReviewRemark(reviewRemark == null || reviewRemark.isBlank() ? null : reviewRemark.trim());
    attendanceMapper.updateById(record);

    AttendanceResponse response = new AttendanceResponse();
    response.setId(record.getId());
    response.setStaffId(record.getStaffId());
    StaffAccount staff = staffMapper.selectById(record.getStaffId());
    response.setStaffName(staff == null ? null : staff.getRealName());
    response.setWorkDate(resolveWorkDate(record));
    response.setCheckInTime(record.getCheckInTime());
    response.setCheckOutTime(record.getCheckOutTime());
    response.setOutingStartTime(record.getOutingStartTime());
    response.setOutingEndTime(record.getOutingEndTime());
    response.setLunchBreakStartTime(record.getLunchBreakStartTime());
    response.setLunchBreakEndTime(record.getLunchBreakEndTime());
    response.setOutingMinutes(record.getOutingMinutes());
    response.setReviewed(record.getReviewed());
    response.setReviewedBy(record.getReviewedBy());
    response.setReviewedAt(record.getReviewedAt());
    response.setReviewRemark(record.getReviewRemark());
    response.setNote(record.getNote());
    response.setStatus(record.getStatus());
    return response;
  }

  private AttendanceDashboardDayItem emptyDay(LocalDate date) {
    AttendanceDashboardDayItem day = new AttendanceDashboardDayItem();
    day.setDate(date);
    day.setWeekLabel(resolveWeekLabel(date.getDayOfWeek()));
    day.setStatus("未打卡");
    day.setLate(false);
    day.setEarlyLeave(false);
    day.setHasLeave(false);
    day.setOutingMinutes(0);
    day.setLunchBreakMinutes(0);
    day.setWorkMinutes(0);
    return day;
  }

  private void mergeRecordIntoDay(AttendanceDashboardDayItem day, AttendanceRecord record, SeasonalWorkWindow window) {
    if (day == null || record == null) {
      return;
    }
    if (day.getCheckInTime() == null || (record.getCheckInTime() != null && record.getCheckInTime().isBefore(day.getCheckInTime()))) {
      day.setCheckInTime(record.getCheckInTime());
    }
    if (day.getCheckOutTime() == null || (record.getCheckOutTime() != null && record.getCheckOutTime().isAfter(day.getCheckOutTime()))) {
      day.setCheckOutTime(record.getCheckOutTime());
    }
    day.setOutingStartTime(maxTime(day.getOutingStartTime(), record.getOutingStartTime()));
    day.setOutingEndTime(maxTime(day.getOutingEndTime(), record.getOutingEndTime()));
    day.setLunchBreakStartTime(maxTime(day.getLunchBreakStartTime(), record.getLunchBreakStartTime()));
    day.setLunchBreakEndTime(maxTime(day.getLunchBreakEndTime(), record.getLunchBreakEndTime()));
    int outing = safeInt(day.getOutingMinutes()) + resolveOutingMinutes(record);
    int lunch = safeInt(day.getLunchBreakMinutes()) + resolveLunchMinutes(record);
    day.setOutingMinutes(outing);
    day.setLunchBreakMinutes(lunch);

    if (day.getCheckInTime() != null && day.getCheckOutTime() != null) {
      long workMinutes = Duration.between(day.getCheckInTime(), day.getCheckOutTime()).toMinutes() - lunch - outing;
      day.setWorkMinutes((int) Math.max(workMinutes, 0));
      day.setStatus(record.getStatus() == null || record.getStatus().isBlank() ? "NORMAL" : record.getStatus());
    } else if (day.getCheckInTime() != null) {
      day.setStatus("在岗");
    }

    List<String> anomalies = new ArrayList<>();
    if (window.lateEnabled() && day.getCheckInTime() != null) {
      LocalDateTime expectedIn = day.getDate().atTime(window.start());
      if (day.getCheckInTime().isAfter(expectedIn.plusMinutes(window.lateGraceMinutes()))) {
        day.setLate(true);
        anomalies.add("迟到");
      }
    }
    if (window.earlyLeaveEnabled() && day.getCheckOutTime() != null) {
      LocalDateTime expectedOut = day.getDate().atTime(window.end());
      if (day.getCheckOutTime().isBefore(expectedOut.minusMinutes(window.earlyLeaveGraceMinutes()))) {
        day.setEarlyLeave(true);
        anomalies.add("早退");
      }
    }
    if (window.outingOvertimeEnabled() && outing > window.outingMaxMinutes()) {
      anomalies.add("外出时长过长");
    }
    if (window.missingCheckoutEnabled()
        && day.getCheckInTime() != null
        && day.getCheckOutTime() == null
        && day.getDate().isBefore(LocalDate.now())) {
      anomalies.add("缺少签退");
    }
    if (!anomalies.isEmpty()) {
      day.setAnomalyText(String.join("、", anomalies));
    }
  }

  private int resolveOutingMinutes(AttendanceRecord record) {
    if (record == null) {
      return 0;
    }
    Integer explicit = record.getOutingMinutes();
    if (explicit != null && explicit > 0) {
      return explicit;
    }
    if (record.getOutingStartTime() != null && record.getOutingEndTime() != null) {
      return Math.max((int) Duration.between(record.getOutingStartTime(), record.getOutingEndTime()).toMinutes(), 0);
    }
    return 0;
  }

  private int resolveLunchMinutes(AttendanceRecord record) {
    if (record == null || record.getLunchBreakStartTime() == null || record.getLunchBreakEndTime() == null) {
      return 0;
    }
    return Math.max((int) Duration.between(record.getLunchBreakStartTime(), record.getLunchBreakEndTime()).toMinutes(), 0);
  }

  private LocalDateTime maxTime(LocalDateTime a, LocalDateTime b) {
    if (a == null) return b;
    if (b == null) return a;
    return a.isAfter(b) ? a : b;
  }

  private LocalDate resolveWorkDate(AttendanceRecord record) {
    if (record == null) {
      return null;
    }
    if (record.getWorkDate() != null) {
      return record.getWorkDate();
    }
    if (record.getCheckInTime() != null) {
      return record.getCheckInTime().toLocalDate();
    }
    if (record.getCheckOutTime() != null) {
      return record.getCheckOutTime().toLocalDate();
    }
    return null;
  }

  private AttendanceSeasonRule getOrCreateRule(Long orgId, Long currentStaffId) {
    AttendanceSeasonRule rule = seasonRuleMapper.selectOne(Wrappers.lambdaQuery(AttendanceSeasonRule.class)
        .eq(AttendanceSeasonRule::getIsDeleted, 0)
        .eq(orgId != null, AttendanceSeasonRule::getOrgId, orgId)
        .orderByDesc(AttendanceSeasonRule::getUpdateTime)
        .last("LIMIT 1"));
    if (rule != null) {
      return rule;
    }
    AttendanceSeasonRule created = new AttendanceSeasonRule();
    created.setTenantId(orgId);
    created.setOrgId(orgId);
    created.setWinterWorkStart(DEFAULT_WINTER_START);
    created.setWinterWorkEnd(DEFAULT_WINTER_END);
    created.setSummerWorkStart(DEFAULT_SUMMER_START);
    created.setSummerWorkEnd(DEFAULT_SUMMER_END);
    created.setLateGraceMinutes(DEFAULT_GRACE_MINUTES);
    created.setEarlyLeaveGraceMinutes(DEFAULT_GRACE_MINUTES);
    created.setOutingMaxMinutes(DEFAULT_OUTING_MAX_MINUTES);
    created.setLateEnabled(DEFAULT_RULE_ENABLED);
    created.setEarlyLeaveEnabled(DEFAULT_RULE_ENABLED);
    created.setOutingOvertimeEnabled(DEFAULT_RULE_ENABLED);
    created.setMissingCheckoutEnabled(DEFAULT_RULE_ENABLED);
    created.setEnabledStatus("ACTIVE");
    created.setCreatedBy(currentStaffId);
    seasonRuleMapper.insert(created);
    return created;
  }

  private AttendanceSeasonRuleResponse toSeasonRuleResponse(AttendanceSeasonRule rule) {
    AttendanceSeasonRuleResponse response = new AttendanceSeasonRuleResponse();
    response.setWinterWorkStart(normalizeClock(rule == null ? null : rule.getWinterWorkStart(), DEFAULT_WINTER_START));
    response.setWinterWorkEnd(normalizeClock(rule == null ? null : rule.getWinterWorkEnd(), DEFAULT_WINTER_END));
    response.setSummerWorkStart(normalizeClock(rule == null ? null : rule.getSummerWorkStart(), DEFAULT_SUMMER_START));
    response.setSummerWorkEnd(normalizeClock(rule == null ? null : rule.getSummerWorkEnd(), DEFAULT_SUMMER_END));
    response.setLateGraceMinutes(sanitizePositiveOrDefault(rule == null ? null : rule.getLateGraceMinutes(), DEFAULT_GRACE_MINUTES));
    response.setEarlyLeaveGraceMinutes(sanitizePositiveOrDefault(rule == null ? null : rule.getEarlyLeaveGraceMinutes(), DEFAULT_GRACE_MINUTES));
    response.setOutingMaxMinutes(sanitizePositiveOrDefault(rule == null ? null : rule.getOutingMaxMinutes(), DEFAULT_OUTING_MAX_MINUTES));
    response.setLateEnabled(normalizeEnabledFlag(rule == null ? null : rule.getLateEnabled()));
    response.setEarlyLeaveEnabled(normalizeEnabledFlag(rule == null ? null : rule.getEarlyLeaveEnabled()));
    response.setOutingOvertimeEnabled(normalizeEnabledFlag(rule == null ? null : rule.getOutingOvertimeEnabled()));
    response.setMissingCheckoutEnabled(normalizeEnabledFlag(rule == null ? null : rule.getMissingCheckoutEnabled()));
    response.setEnabledStatus(normalizeEnabledStatus(rule == null ? null : rule.getEnabledStatus()));
    return response;
  }

  private SeasonalWorkWindow resolveSeasonalWindow(YearMonth month, AttendanceSeasonRule rule) {
    int m = month.getMonthValue();
    boolean winter = m == 11 || m == 12 || m <= 3;
    String start = winter ? normalizeClock(rule.getWinterWorkStart(), DEFAULT_WINTER_START) : normalizeClock(rule.getSummerWorkStart(), DEFAULT_SUMMER_START);
    String end = winter ? normalizeClock(rule.getWinterWorkEnd(), DEFAULT_WINTER_END) : normalizeClock(rule.getSummerWorkEnd(), DEFAULT_SUMMER_END);
    return new SeasonalWorkWindow(
        winter ? "WINTER" : "SUMMER",
        parseTime(start, winter ? DEFAULT_WINTER_START : DEFAULT_SUMMER_START),
        parseTime(end, winter ? DEFAULT_WINTER_END : DEFAULT_SUMMER_END),
        sanitizePositiveOrDefault(rule.getLateGraceMinutes(), DEFAULT_GRACE_MINUTES),
        sanitizePositiveOrDefault(rule.getEarlyLeaveGraceMinutes(), DEFAULT_GRACE_MINUTES),
        sanitizePositiveOrDefault(rule.getOutingMaxMinutes(), DEFAULT_OUTING_MAX_MINUTES),
        normalizeEnabledFlag(rule.getLateEnabled()) == 1,
        normalizeEnabledFlag(rule.getEarlyLeaveEnabled()) == 1,
        normalizeEnabledFlag(rule.getOutingOvertimeEnabled()) == 1,
        normalizeEnabledFlag(rule.getMissingCheckoutEnabled()) == 1);
  }

  private LocalTime parseTime(String value, String fallback) {
    try {
      return LocalTime.parse(normalizeClock(value, fallback), TIME_FORMAT);
    } catch (Exception ignore) {
      return LocalTime.parse(fallback, TIME_FORMAT);
    }
  }

  private String normalizeClock(String value, String fallback) {
    String source = value == null ? fallback : value.trim();
    if (source.isBlank()) {
      source = fallback;
    }
    try {
      LocalTime time = LocalTime.parse(source, TIME_FORMAT);
      return time.format(TIME_FORMAT);
    } catch (Exception ignore) {
      return fallback;
    }
  }

  private int sanitizePositiveOrDefault(Integer value, int fallback) {
    if (value == null || value < 0) {
      return fallback;
    }
    return value;
  }

  private int normalizeEnabledFlag(Integer value) {
    if (value == null) {
      return DEFAULT_RULE_ENABLED;
    }
    return value == 0 ? 0 : 1;
  }

  private String normalizeEnabledStatus(String value) {
    if (value == null || value.isBlank()) {
      return "ACTIVE";
    }
    String normalized = value.trim().toUpperCase();
    if (!"ACTIVE".equals(normalized) && !"INACTIVE".equals(normalized)) {
      return "ACTIVE";
    }
    return normalized;
  }

  private String resolveWeekLabel(DayOfWeek dayOfWeek) {
    return switch (dayOfWeek) {
      case MONDAY -> "周一";
      case TUESDAY -> "周二";
      case WEDNESDAY -> "周三";
      case THURSDAY -> "周四";
      case FRIDAY -> "周五";
      case SATURDAY -> "周六";
      case SUNDAY -> "周日";
    };
  }

  private String extractLeaveType(String formData) {
    if (formData == null || formData.isBlank()) {
      return "请假";
    }
    try {
      Map<String, Object> map = objectMapper.readValue(formData, new TypeReference<Map<String, Object>>() {});
      String raw = safe(map.get("leaveType"));
      if (raw.isBlank()) {
        return "请假";
      }
      return switch (raw.toUpperCase()) {
        case "SICK" -> "病假";
        case "ANNUAL" -> "年假";
        case "PERSONAL" -> "事假";
        case "OFF_IN_LIEU" -> "调休";
        default -> raw;
      };
    } catch (Exception ignore) {
      return "请假";
    }
  }

  private String safe(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private int safeInt(Integer value) {
    return value == null ? 0 : Math.max(value, 0);
  }

  private TodayStatus resolveTodayStatus(Long staffId, Long orgId) {
    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();
    OaApproval currentLeave = approvalMapper.selectOne(Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(OaApproval::getApplicantId, staffId)
        .eq(OaApproval::getApprovalType, "LEAVE")
        .eq(OaApproval::getStatus, "APPROVED")
        .le(OaApproval::getStartTime, now)
        .ge(OaApproval::getEndTime, now)
        .last("LIMIT 1"));
    if (currentLeave != null) {
      return new TodayStatus("ON_LEAVE", "请假中");
    }
    AttendanceRecord todayRecord = attendanceMapper.selectOne(Wrappers.lambdaQuery(AttendanceRecord.class)
        .eq(AttendanceRecord::getIsDeleted, 0)
        .eq(orgId != null, AttendanceRecord::getOrgId, orgId)
        .eq(AttendanceRecord::getStaffId, staffId)
        .eq(AttendanceRecord::getWorkDate, today)
        .orderByDesc(AttendanceRecord::getUpdateTime)
        .last("LIMIT 1"));
    if (todayRecord == null) {
      return new TodayStatus("NOT_CHECKED_IN", "未打卡");
    }
    if (todayRecord.getLunchBreakStartTime() != null
        && (todayRecord.getLunchBreakEndTime() == null
        || todayRecord.getLunchBreakEndTime().isBefore(todayRecord.getLunchBreakStartTime()))) {
      return new TodayStatus("LUNCH_BREAK", "午休中");
    }
    if (todayRecord.getOutingStartTime() != null
        && (todayRecord.getOutingEndTime() == null
        || todayRecord.getOutingEndTime().isBefore(todayRecord.getOutingStartTime()))) {
      return new TodayStatus("OUTING", "外出中");
    }
    if (todayRecord.getCheckInTime() != null && todayRecord.getCheckOutTime() == null) {
      return new TodayStatus("ON_DUTY", "在岗");
    }
    if (todayRecord.getCheckOutTime() != null) {
      return new TodayStatus("OFF_DUTY", "已下班");
    }
    return new TodayStatus("NOT_CHECKED_IN", "未打卡");
  }

  private record SeasonalWorkWindow(
      String seasonType,
      LocalTime start,
      LocalTime end,
      int lateGraceMinutes,
      int earlyLeaveGraceMinutes,
      int outingMaxMinutes,
      boolean lateEnabled,
      boolean earlyLeaveEnabled,
      boolean outingOvertimeEnabled,
      boolean missingCheckoutEnabled) {}

  private record TodayStatus(String code, String label) {}
}
