package com.zhiyangyun.care.nursing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.entity.CareTaskExecuteLog;
import com.zhiyangyun.care.entity.CareTaskTemplate;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.mapper.CareTaskExecuteLogMapper;
import com.zhiyangyun.care.mapper.CareTaskTemplateMapper;
import com.zhiyangyun.care.nursing.entity.ServiceBooking;
import com.zhiyangyun.care.nursing.entity.ServicePlan;
import com.zhiyangyun.care.nursing.mapper.ServiceBookingMapper;
import com.zhiyangyun.care.nursing.mapper.ServicePlanMapper;
import com.zhiyangyun.care.nursing.model.NursingRecordItem;
import com.zhiyangyun.care.nursing.service.NursingRecordService;
import com.zhiyangyun.care.standard.entity.ServiceItem;
import com.zhiyangyun.care.standard.mapper.ServiceItemMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class NursingRecordServiceImpl implements NursingRecordService {
  private final CareTaskExecuteLogMapper executeLogMapper;
  private final CareTaskDailyMapper dailyMapper;
  private final CareTaskTemplateMapper templateMapper;
  private final ServiceBookingMapper bookingMapper;
  private final ServicePlanMapper planMapper;
  private final ServiceItemMapper serviceItemMapper;
  private final ElderMapper elderMapper;
  private final StaffMapper staffMapper;

  public NursingRecordServiceImpl(CareTaskExecuteLogMapper executeLogMapper, CareTaskDailyMapper dailyMapper,
      CareTaskTemplateMapper templateMapper, ServiceBookingMapper bookingMapper, ServicePlanMapper planMapper,
      ServiceItemMapper serviceItemMapper, ElderMapper elderMapper, StaffMapper staffMapper) {
    this.executeLogMapper = executeLogMapper;
    this.dailyMapper = dailyMapper;
    this.templateMapper = templateMapper;
    this.bookingMapper = bookingMapper;
    this.planMapper = planMapper;
    this.serviceItemMapper = serviceItemMapper;
    this.elderMapper = elderMapper;
    this.staffMapper = staffMapper;
  }

  @Override
  public IPage<NursingRecordItem> page(Long tenantId, long pageNo, long pageSize, LocalDateTime timeFrom,
      LocalDateTime timeTo, Long elderId, Long staffId, String keyword) {
    var executeWrapper = Wrappers.lambdaQuery(CareTaskExecuteLog.class)
        .eq(CareTaskExecuteLog::getIsDeleted, 0)
        .eq(tenantId != null, CareTaskExecuteLog::getTenantId, tenantId)
        .eq(elderId != null, CareTaskExecuteLog::getElderId, elderId)
        .eq(staffId != null, CareTaskExecuteLog::getStaffId, staffId)
        .ge(timeFrom != null, CareTaskExecuteLog::getExecuteTime, timeFrom)
        .le(timeTo != null, CareTaskExecuteLog::getExecuteTime, timeTo);
    List<CareTaskExecuteLog> executeLogs = executeLogMapper.selectList(executeWrapper);

    var bookingWrapper = Wrappers.lambdaQuery(ServiceBooking.class)
        .eq(ServiceBooking::getIsDeleted, 0)
        .eq(tenantId != null, ServiceBooking::getTenantId, tenantId)
        .eq(elderId != null, ServiceBooking::getElderId, elderId)
        .eq(staffId != null, ServiceBooking::getAssignedStaffId, staffId)
        .ge(timeFrom != null, ServiceBooking::getBookingTime, timeFrom)
        .le(timeTo != null, ServiceBooking::getBookingTime, timeTo);
    List<ServiceBooking> bookings = bookingMapper.selectList(bookingWrapper);

    Set<Long> elderIds = new java.util.HashSet<>();
    elderIds.addAll(executeLogs.stream().map(CareTaskExecuteLog::getElderId).filter(id -> id != null && id > 0).collect(Collectors.toSet()));
    elderIds.addAll(bookings.stream().map(ServiceBooking::getElderId).filter(id -> id != null && id > 0).collect(Collectors.toSet()));

    Set<Long> staffIds = new java.util.HashSet<>();
    staffIds.addAll(executeLogs.stream().map(CareTaskExecuteLog::getStaffId).filter(id -> id != null && id > 0).collect(Collectors.toSet()));
    staffIds.addAll(bookings.stream().map(ServiceBooking::getAssignedStaffId).filter(id -> id != null && id > 0).collect(Collectors.toSet()));

    List<Long> taskDailyIds = executeLogs.stream().map(CareTaskExecuteLog::getTaskDailyId).filter(id -> id != null && id > 0).distinct().toList();
    Map<Long, CareTaskDaily> dailyMap = taskDailyIds.isEmpty() ? Collections.emptyMap()
        : dailyMapper.selectBatchIds(taskDailyIds).stream().collect(Collectors.toMap(CareTaskDaily::getId, d -> d, (a, b) -> a));

    Set<Long> templateIds = dailyMap.values().stream().map(CareTaskDaily::getTemplateId)
        .filter(id -> id != null && id > 0).collect(Collectors.toSet());
    Map<Long, CareTaskTemplate> templateMap = templateIds.isEmpty() ? Collections.emptyMap()
        : templateMapper.selectBatchIds(templateIds).stream().collect(Collectors.toMap(CareTaskTemplate::getId, t -> t, (a, b) -> a));

    Set<Long> planIds = bookings.stream().map(ServiceBooking::getPlanId).filter(id -> id != null && id > 0).collect(Collectors.toSet());
    Map<Long, ServicePlan> planMap = planIds.isEmpty() ? Collections.emptyMap()
        : planMapper.selectBatchIds(planIds).stream().collect(Collectors.toMap(ServicePlan::getId, p -> p, (a, b) -> a));

    Set<Long> itemIds = bookings.stream().map(ServiceBooking::getServiceItemId).filter(id -> id != null && id > 0).collect(Collectors.toSet());
    Map<Long, ServiceItem> itemMap = itemIds.isEmpty() ? Collections.emptyMap()
        : serviceItemMapper.selectBatchIds(itemIds).stream().collect(Collectors.toMap(ServiceItem::getId, s -> s, (a, b) -> a));

    Map<Long, ElderProfile> elderMap = elderIds.isEmpty() ? Collections.emptyMap()
        : elderMapper.selectBatchIds(elderIds).stream().collect(Collectors.toMap(ElderProfile::getId, e -> e, (a, b) -> a));
    Map<Long, StaffAccount> staffMap = staffIds.isEmpty() ? Collections.emptyMap()
        : staffMapper.selectBatchIds(staffIds).stream().collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));

    List<NursingRecordItem> all = new ArrayList<>();

    for (CareTaskExecuteLog log : executeLogs) {
      NursingRecordItem item = new NursingRecordItem();
      item.setRecordType("CARE_TASK");
      item.setSourceId(log.getId());
      item.setRecordTime(log.getExecuteTime());
      item.setElderId(log.getElderId());
      ElderProfile elder = elderMap.get(log.getElderId());
      item.setElderName(elder == null ? null : elder.getFullName());
      item.setStaffId(log.getStaffId());
      StaffAccount staff = staffMap.get(log.getStaffId());
      item.setStaffName(staff == null ? null : staff.getRealName());
      CareTaskDaily daily = dailyMap.get(log.getTaskDailyId());
      CareTaskTemplate template = daily == null ? null : templateMap.get(daily.getTemplateId());
      item.setServiceName(template == null ? "护理任务" : template.getTaskName());
      item.setStatus(log.getResultStatus() != null && log.getResultStatus() == 1 ? "DONE" : "FAILED");
      item.setSuccessFlag(log.getResultStatus() != null && log.getResultStatus() == 1 ? 1 : 0);
      item.setRemark(log.getRemark());
      all.add(item);
    }

    for (ServiceBooking booking : bookings) {
      NursingRecordItem item = new NursingRecordItem();
      item.setRecordType("SERVICE_BOOKING");
      item.setSourceId(booking.getId());
      item.setRecordTime(booking.getBookingTime());
      item.setElderId(booking.getElderId());
      ElderProfile elder = elderMap.get(booking.getElderId());
      item.setElderName(elder == null ? null : elder.getFullName());
      item.setStaffId(booking.getAssignedStaffId());
      StaffAccount staff = staffMap.get(booking.getAssignedStaffId());
      item.setStaffName(staff == null ? null : staff.getRealName());
      ServiceItem serviceItem = itemMap.get(booking.getServiceItemId());
      item.setServiceName(serviceItem == null ? null : serviceItem.getName());
      item.setPlanId(booking.getPlanId());
      ServicePlan plan = planMap.get(booking.getPlanId());
      item.setPlanName(plan == null ? null : plan.getPlanName());
      item.setStatus(booking.getStatus());
      item.setSuccessFlag("DONE".equalsIgnoreCase(booking.getStatus()) ? 1 : 0);
      item.setRemark(booking.getRemark());
      all.add(item);
    }

    if (keyword != null && !keyword.isBlank()) {
      String kw = keyword.toLowerCase();
      all = all.stream().filter(item ->
          contains(item.getElderName(), kw) || contains(item.getStaffName(), kw) || contains(item.getServiceName(), kw)
              || contains(item.getPlanName(), kw)).toList();
    }

    all.sort(Comparator.comparing(NursingRecordItem::getRecordTime, Comparator.nullsLast(Comparator.reverseOrder())));

    long total = all.size();
    int fromIdx = (int) ((pageNo - 1) * pageSize);
    int toIdx = Math.min(fromIdx + (int) pageSize, all.size());
    List<NursingRecordItem> pageRecords = fromIdx >= all.size() ? List.of() : all.subList(fromIdx, toIdx);

    IPage<NursingRecordItem> resp = new Page<>(pageNo, pageSize, total);
    resp.setRecords(pageRecords);
    return resp;
  }

  private boolean contains(String value, String keyword) {
    return value != null && value.toLowerCase().contains(keyword);
  }
}
