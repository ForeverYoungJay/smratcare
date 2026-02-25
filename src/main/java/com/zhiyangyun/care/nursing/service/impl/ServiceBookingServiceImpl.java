package com.zhiyangyun.care.nursing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.nursing.entity.ServiceBooking;
import com.zhiyangyun.care.nursing.entity.ServicePlan;
import com.zhiyangyun.care.nursing.mapper.ServiceBookingMapper;
import com.zhiyangyun.care.nursing.mapper.ServicePlanMapper;
import com.zhiyangyun.care.nursing.model.ServiceBookingRequest;
import com.zhiyangyun.care.nursing.model.ServiceBookingResponse;
import com.zhiyangyun.care.nursing.service.ServiceBookingService;
import com.zhiyangyun.care.standard.entity.ServiceItem;
import com.zhiyangyun.care.standard.mapper.ServiceItemMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ServiceBookingServiceImpl implements ServiceBookingService {
  private final ServiceBookingMapper serviceBookingMapper;
  private final ElderMapper elderMapper;
  private final ServicePlanMapper servicePlanMapper;
  private final ServiceItemMapper serviceItemMapper;
  private final StaffMapper staffMapper;

  public ServiceBookingServiceImpl(ServiceBookingMapper serviceBookingMapper, ElderMapper elderMapper,
      ServicePlanMapper servicePlanMapper, ServiceItemMapper serviceItemMapper, StaffMapper staffMapper) {
    this.serviceBookingMapper = serviceBookingMapper;
    this.elderMapper = elderMapper;
    this.servicePlanMapper = servicePlanMapper;
    this.serviceItemMapper = serviceItemMapper;
    this.staffMapper = staffMapper;
  }

  @Override
  public ServiceBookingResponse create(ServiceBookingRequest request) {
    ServiceBooking booking = new ServiceBooking();
    fillEntity(booking, request);
    booking.setCreatedBy(request.getCreatedBy());
    serviceBookingMapper.insert(booking);
    return toResponse(booking, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
  }

  @Override
  public ServiceBookingResponse update(Long id, ServiceBookingRequest request) {
    ServiceBooking booking = serviceBookingMapper.selectById(id);
    if (booking == null || !request.getTenantId().equals(booking.getTenantId())) {
      return null;
    }
    fillEntity(booking, request);
    serviceBookingMapper.updateById(booking);
    return toResponse(booking, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
  }

  @Override
  public ServiceBookingResponse get(Long id, Long tenantId) {
    ServiceBooking booking = serviceBookingMapper.selectById(id);
    if (booking == null || (tenantId != null && !tenantId.equals(booking.getTenantId()))) {
      return null;
    }
    return toResponse(booking, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
  }

  @Override
  public IPage<ServiceBookingResponse> page(Long tenantId, long pageNo, long pageSize, LocalDateTime timeFrom,
      LocalDateTime timeTo, String status, Long elderId) {
    var wrapper = Wrappers.lambdaQuery(ServiceBooking.class)
        .eq(ServiceBooking::getIsDeleted, 0)
        .eq(tenantId != null, ServiceBooking::getTenantId, tenantId)
        .eq(status != null && !status.isBlank(), ServiceBooking::getStatus, status)
        .eq(elderId != null, ServiceBooking::getElderId, elderId)
        .ge(timeFrom != null, ServiceBooking::getBookingTime, timeFrom)
        .le(timeTo != null, ServiceBooking::getBookingTime, timeTo)
        .orderByDesc(ServiceBooking::getBookingTime);

    IPage<ServiceBooking> page = serviceBookingMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);

    Set<Long> elderIds = page.getRecords().stream().map(ServiceBooking::getElderId)
        .filter(id -> id != null && id > 0)
        .collect(Collectors.toSet());
    Set<Long> planIds = page.getRecords().stream().map(ServiceBooking::getPlanId)
        .filter(id -> id != null && id > 0)
        .collect(Collectors.toSet());
    Set<Long> itemIds = page.getRecords().stream().map(ServiceBooking::getServiceItemId)
        .filter(id -> id != null && id > 0)
        .collect(Collectors.toSet());
    Set<Long> staffIds = page.getRecords().stream().map(ServiceBooking::getAssignedStaffId)
        .filter(id -> id != null && id > 0)
        .collect(Collectors.toSet());

    Map<Long, ElderProfile> elderMap = elderIds.isEmpty() ? Collections.emptyMap()
        : elderMapper.selectBatchIds(elderIds).stream().collect(Collectors.toMap(ElderProfile::getId, e -> e, (a, b) -> a));
    Map<Long, ServicePlan> planMap = planIds.isEmpty() ? Collections.emptyMap()
        : servicePlanMapper.selectBatchIds(planIds).stream().collect(Collectors.toMap(ServicePlan::getId, p -> p, (a, b) -> a));
    Map<Long, ServiceItem> itemMap = itemIds.isEmpty() ? Collections.emptyMap()
        : serviceItemMapper.selectBatchIds(itemIds).stream().collect(Collectors.toMap(ServiceItem::getId, s -> s, (a, b) -> a));
    Map<Long, StaffAccount> staffMap = staffIds.isEmpty() ? Collections.emptyMap()
        : staffMapper.selectBatchIds(staffIds).stream().collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));

    IPage<ServiceBookingResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream()
        .map(item -> toResponse(item, elderMap, planMap, itemMap, staffMap))
        .toList());
    return resp;
  }

  @Override
  public int generateFromPlans(Long tenantId, LocalDate date, boolean force) {
    if (date == null) {
      return 0;
    }
    var wrapper = Wrappers.lambdaQuery(ServicePlan.class)
        .eq(ServicePlan::getIsDeleted, 0)
        .eq(tenantId != null, ServicePlan::getTenantId, tenantId)
        .eq(ServicePlan::getStatus, "ACTIVE")
        .le(ServicePlan::getStartDate, date)
        .and(w -> w.isNull(ServicePlan::getEndDate).or().ge(ServicePlan::getEndDate, date));
    var plans = servicePlanMapper.selectList(wrapper);

    int created = 0;
    for (ServicePlan plan : plans) {
      if (!matchCycle(plan, date)) {
        continue;
      }
      LocalTime preferred = plan.getPreferredTime() == null ? LocalTime.of(9, 0) : plan.getPreferredTime();
      int frequency = plan.getFrequency() == null || plan.getFrequency() <= 0 ? 1 : plan.getFrequency();
      for (int i = 0; i < frequency; i++) {
        LocalDateTime bookingTime = LocalDateTime.of(date, preferred).plusHours(i);
        boolean exists = serviceBookingMapper.selectCount(Wrappers.lambdaQuery(ServiceBooking.class)
            .eq(ServiceBooking::getIsDeleted, 0)
            .eq(ServiceBooking::getTenantId, plan.getTenantId())
            .eq(ServiceBooking::getPlanId, plan.getId())
            .eq(ServiceBooking::getElderId, plan.getElderId())
            .eq(ServiceBooking::getServiceItemId, plan.getServiceItemId())
            .eq(ServiceBooking::getBookingTime, bookingTime)) > 0;
        if (exists && !force) {
          continue;
        }
        if (exists) {
          continue;
        }
        ServiceItem item = serviceItemMapper.selectById(plan.getServiceItemId());
        ServiceBooking booking = new ServiceBooking();
        booking.setTenantId(plan.getTenantId());
        booking.setOrgId(plan.getOrgId());
        booking.setElderId(plan.getElderId());
        booking.setPlanId(plan.getId());
        booking.setServiceItemId(plan.getServiceItemId());
        booking.setBookingTime(bookingTime);
        booking.setExpectedDuration(item == null ? null : item.getDefaultDuration());
        booking.setAssignedStaffId(plan.getDefaultStaffId());
        booking.setSource("PLAN");
        booking.setStatus("BOOKED");
        booking.setCreatedBy(plan.getCreatedBy());
        serviceBookingMapper.insert(booking);
        created++;
      }
    }
    return created;
  }

  @Override
  public void delete(Long id, Long tenantId) {
    ServiceBooking booking = serviceBookingMapper.selectById(id);
    if (booking != null && tenantId != null && tenantId.equals(booking.getTenantId())) {
      booking.setIsDeleted(1);
      serviceBookingMapper.updateById(booking);
    }
  }

  private boolean matchCycle(ServicePlan plan, LocalDate date) {
    LocalDate start = plan.getStartDate();
    if (start == null) {
      return false;
    }
    String cycle = plan.getCycleType() == null ? "DAILY" : plan.getCycleType();
    if ("WEEKLY".equalsIgnoreCase(cycle)) {
      long days = ChronoUnit.DAYS.between(start, date);
      return days >= 0 && days % 7 == 0;
    }
    if ("MONTHLY".equalsIgnoreCase(cycle)) {
      return date.getDayOfMonth() == start.getDayOfMonth();
    }
    return true;
  }

  private void fillEntity(ServiceBooking booking, ServiceBookingRequest request) {
    booking.setTenantId(request.getTenantId());
    booking.setOrgId(request.getOrgId());
    booking.setElderId(request.getElderId());
    booking.setPlanId(request.getPlanId());
    booking.setServiceItemId(request.getServiceItemId());
    booking.setBookingTime(request.getBookingTime());
    booking.setExpectedDuration(request.getExpectedDuration());
    booking.setAssignedStaffId(request.getAssignedStaffId());
    booking.setSource(request.getSource() == null ? "MANUAL" : request.getSource());
    booking.setStatus(request.getStatus() == null ? "BOOKED" : request.getStatus());
    booking.setCancelReason(request.getCancelReason());
    booking.setRemark(request.getRemark());
  }

  private ServiceBookingResponse toResponse(ServiceBooking booking,
      Map<Long, ElderProfile> elderMap,
      Map<Long, ServicePlan> planMap,
      Map<Long, ServiceItem> itemMap,
      Map<Long, StaffAccount> staffMap) {
    ServiceBookingResponse response = new ServiceBookingResponse();
    response.setId(booking.getId());
    response.setTenantId(booking.getTenantId());
    response.setOrgId(booking.getOrgId());
    response.setElderId(booking.getElderId());
    response.setPlanId(booking.getPlanId());
    response.setServiceItemId(booking.getServiceItemId());
    response.setBookingTime(booking.getBookingTime());
    response.setExpectedDuration(booking.getExpectedDuration());
    response.setAssignedStaffId(booking.getAssignedStaffId());
    response.setSource(booking.getSource());
    response.setStatus(booking.getStatus());
    response.setCancelReason(booking.getCancelReason());
    response.setRemark(booking.getRemark());

    ElderProfile elder = elderMap.get(booking.getElderId());
    if (elder == null && booking.getElderId() != null) {
      elder = elderMapper.selectById(booking.getElderId());
    }
    response.setElderName(elder == null ? null : elder.getFullName());

    ServicePlan plan = planMap.get(booking.getPlanId());
    if (plan == null && booking.getPlanId() != null) {
      plan = servicePlanMapper.selectById(booking.getPlanId());
    }
    response.setPlanName(plan == null ? null : plan.getPlanName());

    ServiceItem serviceItem = itemMap.get(booking.getServiceItemId());
    if (serviceItem == null && booking.getServiceItemId() != null) {
      serviceItem = serviceItemMapper.selectById(booking.getServiceItemId());
    }
    response.setServiceItemName(serviceItem == null ? null : serviceItem.getName());

    StaffAccount staff = staffMap.get(booking.getAssignedStaffId());
    if (staff == null && booking.getAssignedStaffId() != null) {
      staff = staffMapper.selectById(booking.getAssignedStaffId());
    }
    response.setAssignedStaffName(staff == null ? null : staff.getRealName());
    return response;
  }
}
