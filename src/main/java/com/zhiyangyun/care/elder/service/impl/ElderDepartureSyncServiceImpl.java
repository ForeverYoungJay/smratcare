package com.zhiyangyun.care.elder.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.model.ElderLifecycleStatus;
import com.zhiyangyun.care.elder.service.ElderDepartureSyncService;
import com.zhiyangyun.care.entity.CareTaskDaily;
import com.zhiyangyun.care.health.entity.HealthMedicationSetting;
import com.zhiyangyun.care.health.entity.HealthMedicationTask;
import com.zhiyangyun.care.health.mapper.HealthMedicationSettingMapper;
import com.zhiyangyun.care.health.mapper.HealthMedicationTaskMapper;
import com.zhiyangyun.care.mapper.CareTaskDailyMapper;
import com.zhiyangyun.care.nursing.entity.ServiceBooking;
import com.zhiyangyun.care.nursing.entity.ServicePlan;
import com.zhiyangyun.care.nursing.mapper.ServiceBookingMapper;
import com.zhiyangyun.care.nursing.mapper.ServicePlanMapper;
import com.zhiyangyun.care.standard.entity.ElderPackage;
import com.zhiyangyun.care.standard.mapper.ElderPackageMapper;
import com.zhiyangyun.care.store.entity.StoreOrder;
import com.zhiyangyun.care.store.mapper.StoreOrderMapper;
import com.zhiyangyun.care.visit.entity.VisitBooking;
import com.zhiyangyun.care.visit.mapper.VisitBookingMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElderDepartureSyncServiceImpl implements ElderDepartureSyncService {
  private static final String TASK_CANCELLED = "CANCELLED";
  private static final String MEDICATION_CANCELLED = "CANCELLED";
  private static final String PLAN_INACTIVE = "INACTIVE";
  private static final String BOOKING_CANCELLED = "CANCELLED";
  private static final int ELDER_PACKAGE_INACTIVE = 0;
  private static final int ORDER_CREATED = 1;
  private static final int ORDER_CANCELLED = 4;
  private static final int ORDER_REFUNDED = 5;
  private static final int PAY_REVERSED = 2;
  private static final int VISIT_CANCELLED = 2;

  private final StoreOrderMapper storeOrderMapper;
  private final VisitBookingMapper visitBookingMapper;
  private final ServicePlanMapper servicePlanMapper;
  private final ServiceBookingMapper serviceBookingMapper;
  private final ElderPackageMapper elderPackageMapper;
  private final CareTaskDailyMapper careTaskDailyMapper;
  private final HealthMedicationTaskMapper healthMedicationTaskMapper;
  private final HealthMedicationSettingMapper healthMedicationSettingMapper;

  public ElderDepartureSyncServiceImpl(
      StoreOrderMapper storeOrderMapper,
      VisitBookingMapper visitBookingMapper,
      ServicePlanMapper servicePlanMapper,
      ServiceBookingMapper serviceBookingMapper,
      ElderPackageMapper elderPackageMapper,
      CareTaskDailyMapper careTaskDailyMapper,
      HealthMedicationTaskMapper healthMedicationTaskMapper,
      HealthMedicationSettingMapper healthMedicationSettingMapper) {
    this.storeOrderMapper = storeOrderMapper;
    this.visitBookingMapper = visitBookingMapper;
    this.servicePlanMapper = servicePlanMapper;
    this.serviceBookingMapper = serviceBookingMapper;
    this.elderPackageMapper = elderPackageMapper;
    this.careTaskDailyMapper = careTaskDailyMapper;
    this.healthMedicationTaskMapper = healthMedicationTaskMapper;
    this.healthMedicationSettingMapper = healthMedicationSettingMapper;
  }

  @Override
  @Transactional
  public void syncAfterLifecycleChange(ElderProfile elder, String beforeLifecycleStatus, String targetLifecycleStatus, Long operatorId) {
    if (elder == null || elder.getId() == null) {
      return;
    }
    String normalizedTarget = ElderLifecycleStatus.normalize(targetLifecycleStatus);
    String normalizedBefore = ElderLifecycleStatus.normalize(beforeLifecycleStatus);
    if (!isDepartureLifecycle(normalizedTarget) || normalizedTarget.equals(normalizedBefore)) {
      return;
    }
    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();

    cancelPendingStoreOrders(elder, now);
    cancelPendingVisits(elder, today, now);
    deactivateServicePlans(elder);
    cancelPendingServiceBookings(elder, now, operatorId);
    deactivateElderPackages(elder, today);
    cancelPendingCareTasks(elder);
    deactivateMedicationSettings(elder, today);
    cancelPendingMedicationTasks(elder, today);
  }

  private void cancelPendingStoreOrders(ElderProfile elder, LocalDateTime now) {
    List<StoreOrder> orders = storeOrderMapper.selectList(Wrappers.lambdaQuery(StoreOrder.class)
        .eq(StoreOrder::getIsDeleted, 0)
        .eq(StoreOrder::getOrgId, elder.getOrgId())
        .eq(StoreOrder::getElderId, elder.getId())
        .eq(StoreOrder::getOrderStatus, ORDER_CREATED));
    for (StoreOrder order : orders) {
      order.setOrderStatus(ORDER_CANCELLED);
      order.setPayStatus(PAY_REVERSED);
      order.setUpdateTime(now);
      storeOrderMapper.updateById(order);
    }
  }

  private void cancelPendingVisits(ElderProfile elder, LocalDate today, LocalDateTime now) {
    List<VisitBooking> bookings = visitBookingMapper.selectList(Wrappers.lambdaQuery(VisitBooking.class)
        .eq(VisitBooking::getIsDeleted, 0)
        .eq(VisitBooking::getOrgId, elder.getOrgId())
        .eq(VisitBooking::getElderId, elder.getId())
        .eq(VisitBooking::getStatus, 0)
        .ge(VisitBooking::getVisitDate, today));
    for (VisitBooking booking : bookings) {
      booking.setStatus(VISIT_CANCELLED);
      booking.setRemark(appendRemark(booking.getRemark(), "老人已离院，系统自动取消探视预约"));
      booking.setUpdateTime(now);
      visitBookingMapper.updateById(booking);
    }
  }

  private void deactivateServicePlans(ElderProfile elder) {
    List<ServicePlan> plans = servicePlanMapper.selectList(Wrappers.lambdaQuery(ServicePlan.class)
        .eq(ServicePlan::getIsDeleted, 0)
        .eq(ServicePlan::getOrgId, elder.getOrgId())
        .eq(ServicePlan::getElderId, elder.getId())
        .eq(ServicePlan::getStatus, "ACTIVE"));
    for (ServicePlan plan : plans) {
      plan.setStatus(PLAN_INACTIVE);
      plan.setRemark(appendRemark(plan.getRemark(), "老人已离院，系统自动停用服务计划"));
      servicePlanMapper.updateById(plan);
    }
  }

  private void cancelPendingServiceBookings(ElderProfile elder, LocalDateTime now, Long operatorId) {
    List<ServiceBooking> bookings = serviceBookingMapper.selectList(Wrappers.lambdaQuery(ServiceBooking.class)
        .eq(ServiceBooking::getIsDeleted, 0)
        .eq(ServiceBooking::getOrgId, elder.getOrgId())
        .eq(ServiceBooking::getElderId, elder.getId())
        .in(ServiceBooking::getStatus, "BOOKED", "OPEN", "PENDING")
        .ge(ServiceBooking::getBookingTime, now.minusMinutes(1)));
    for (ServiceBooking booking : bookings) {
      booking.setStatus(BOOKING_CANCELLED);
      booking.setCancelReason("老人已离院，系统自动取消");
      booking.setRemark(appendRemark(booking.getRemark(), "自动取消"));
      if (booking.getCreatedBy() == null) {
        booking.setCreatedBy(operatorId);
      }
      serviceBookingMapper.updateById(booking);
    }
  }

  private void deactivateElderPackages(ElderProfile elder, LocalDate today) {
    List<ElderPackage> packages = elderPackageMapper.selectList(Wrappers.lambdaQuery(ElderPackage.class)
        .eq(ElderPackage::getIsDeleted, 0)
        .eq(ElderPackage::getOrgId, elder.getOrgId())
        .eq(ElderPackage::getElderId, elder.getId())
        .eq(ElderPackage::getStatus, 1));
    for (ElderPackage item : packages) {
      item.setStatus(ELDER_PACKAGE_INACTIVE);
      if (item.getEndDate() == null || item.getEndDate().isAfter(today)) {
        item.setEndDate(today);
      }
      item.setRemark(appendRemark(item.getRemark(), "老人已离院，系统自动停用服务包"));
      elderPackageMapper.updateById(item);
    }
  }

  private void cancelPendingCareTasks(ElderProfile elder) {
    List<CareTaskDaily> tasks = careTaskDailyMapper.selectList(Wrappers.lambdaQuery(CareTaskDaily.class)
        .eq(CareTaskDaily::getIsDeleted, 0)
        .eq(CareTaskDaily::getOrgId, elder.getOrgId())
        .eq(CareTaskDaily::getElderId, elder.getId())
        .in(CareTaskDaily::getStatus, "TODO", "PENDING", "ASSIGNED"));
    for (CareTaskDaily task : tasks) {
      task.setStatus(TASK_CANCELLED);
      careTaskDailyMapper.updateById(task);
    }
  }

  private void deactivateMedicationSettings(ElderProfile elder, LocalDate today) {
    List<HealthMedicationSetting> settings = healthMedicationSettingMapper.selectList(Wrappers.lambdaQuery(HealthMedicationSetting.class)
        .eq(HealthMedicationSetting::getIsDeleted, 0)
        .eq(HealthMedicationSetting::getOrgId, elder.getOrgId())
        .eq(HealthMedicationSetting::getElderId, elder.getId())
        .and(w -> w.isNull(HealthMedicationSetting::getEndDate).or().ge(HealthMedicationSetting::getEndDate, today)));
    for (HealthMedicationSetting setting : settings) {
      setting.setEndDate(today);
      setting.setRemark(appendRemark(setting.getRemark(), "老人已离院，系统自动截止用药计划"));
      healthMedicationSettingMapper.updateById(setting);
    }
  }

  private void cancelPendingMedicationTasks(ElderProfile elder, LocalDate today) {
    List<HealthMedicationTask> tasks = healthMedicationTaskMapper.selectList(Wrappers.lambdaQuery(HealthMedicationTask.class)
        .eq(HealthMedicationTask::getIsDeleted, 0)
        .eq(HealthMedicationTask::getOrgId, elder.getOrgId())
        .eq(HealthMedicationTask::getElderId, elder.getId())
        .eq(HealthMedicationTask::getStatus, "PENDING")
        .ge(HealthMedicationTask::getTaskDate, today));
    for (HealthMedicationTask task : tasks) {
      task.setStatus(MEDICATION_CANCELLED);
      task.setRemark(appendRemark(task.getRemark(), "老人已离院，系统自动取消用药任务"));
      healthMedicationTaskMapper.updateById(task);
    }
  }

  private boolean isDepartureLifecycle(String lifecycleStatus) {
    return ElderLifecycleStatus.DISCHARGED.equals(lifecycleStatus)
        || ElderLifecycleStatus.DECEASED.equals(lifecycleStatus);
  }

  private String appendRemark(String remark, String suffix) {
    if (suffix == null || suffix.isBlank()) {
      return remark;
    }
    if (remark == null || remark.isBlank()) {
      return suffix;
    }
    return remark + "；" + suffix;
  }
}
