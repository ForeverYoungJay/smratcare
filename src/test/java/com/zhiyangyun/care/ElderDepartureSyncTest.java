package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.model.ElderDepartureType;
import com.zhiyangyun.care.elder.model.ElderLifecycleStatus;
import com.zhiyangyun.care.elder.service.ElderLifecycleStateService;
import com.zhiyangyun.care.health.entity.HealthMedicationSetting;
import com.zhiyangyun.care.health.entity.HealthMedicationTask;
import com.zhiyangyun.care.health.mapper.HealthMedicationSettingMapper;
import com.zhiyangyun.care.health.mapper.HealthMedicationTaskMapper;
import com.zhiyangyun.care.entity.CareTaskDaily;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ElderDepartureSyncTest {
  @Autowired
  private ElderMapper elderMapper;
  @Autowired
  private ElderLifecycleStateService elderLifecycleStateService;
  @Autowired
  private StoreOrderMapper storeOrderMapper;
  @Autowired
  private VisitBookingMapper visitBookingMapper;
  @Autowired
  private ServicePlanMapper servicePlanMapper;
  @Autowired
  private ServiceBookingMapper serviceBookingMapper;
  @Autowired
  private ElderPackageMapper elderPackageMapper;
  @Autowired
  private CareTaskDailyMapper careTaskDailyMapper;
  @Autowired
  private HealthMedicationSettingMapper healthMedicationSettingMapper;
  @Autowired
  private HealthMedicationTaskMapper healthMedicationTaskMapper;

  @Test
  void discharge_sync_cancels_pending_cross_module_records() {
    ElderProfile elder = elderMapper.selectById(4003L);
    elder.setLifecycleStatus(ElderLifecycleStatus.IN_HOSPITAL);
    elderMapper.updateById(elder);

    StoreOrder order = new StoreOrder();
    order.setOrgId(1L);
    order.setOrderNo("TEST-DISCHARGE-ORDER");
    order.setElderId(4003L);
    order.setOrderStatus(1);
    order.setPayStatus(0);
    storeOrderMapper.insert(order);

    VisitBooking visitBooking = new VisitBooking();
    visitBooking.setOrgId(1L);
    visitBooking.setElderId(4003L);
    visitBooking.setFamilyUserId(1L);
    visitBooking.setVisitorName("家属");
    visitBooking.setVisitorPhone("13000000000");
    visitBooking.setVisitorRelation("子女");
    visitBooking.setVisitDate(LocalDate.now().plusDays(1));
    visitBooking.setVisitTime(LocalDateTime.now().plusDays(1));
    visitBooking.setVisitTimeSlot("AM");
    visitBooking.setVisitorCount(1);
    visitBooking.setStatus(0);
    visitBooking.setVerifyCode("VERIFY-DISCHARGE");
    visitBooking.setVisitCode("VISIT-DISCHARGE");
    visitBookingMapper.insert(visitBooking);

    ServicePlan plan = new ServicePlan();
    plan.setTenantId(1L);
    plan.setOrgId(1L);
    plan.setElderId(4003L);
    plan.setPlanName("离院联动测试计划");
    plan.setCycleType("DAILY");
    plan.setFrequency(1);
    plan.setStartDate(LocalDate.now());
    plan.setStatus("ACTIVE");
    servicePlanMapper.insert(plan);

    ServiceBooking serviceBooking = new ServiceBooking();
    serviceBooking.setTenantId(1L);
    serviceBooking.setOrgId(1L);
    serviceBooking.setElderId(4003L);
    serviceBooking.setPlanId(plan.getId());
    serviceBooking.setBookingTime(LocalDateTime.now().plusDays(1));
    serviceBooking.setStatus("BOOKED");
    serviceBookingMapper.insert(serviceBooking);

    ElderPackage elderPackage = new ElderPackage();
    elderPackage.setTenantId(1L);
    elderPackage.setOrgId(1L);
    elderPackage.setElderId(4003L);
    elderPackage.setPackageId(1L);
    elderPackage.setStartDate(LocalDate.now());
    elderPackage.setStatus(1);
    elderPackageMapper.insert(elderPackage);

    HealthMedicationSetting setting = new HealthMedicationSetting();
    setting.setTenantId(1L);
    setting.setOrgId(1L);
    setting.setElderId(4003L);
    setting.setElderName("正常老人");
    setting.setDrugName("维生素");
    setting.setFrequency("QD");
    setting.setStartDate(LocalDate.now());
    healthMedicationSettingMapper.insert(setting);

    HealthMedicationTask medicationTask = new HealthMedicationTask();
    medicationTask.setTenantId(1L);
    medicationTask.setOrgId(1L);
    medicationTask.setElderId(4003L);
    medicationTask.setElderName("正常老人");
    medicationTask.setDrugName("维生素");
    medicationTask.setTaskDate(LocalDate.now().plusDays(1));
    medicationTask.setPlannedTime(LocalDateTime.now().plusDays(1));
    medicationTask.setStatus("PENDING");
    healthMedicationTaskMapper.insert(medicationTask);

    CareTaskDaily careTask = new CareTaskDaily();
    careTask.setTenantId(1L);
    careTask.setOrgId(1L);
    careTask.setElderId(4003L);
    careTask.setTemplateId(300L);
    careTask.setTaskDate(LocalDate.now());
    careTask.setPlanTime(LocalDateTime.now().plusHours(1));
    careTask.setStatus("PENDING");
    careTaskDailyMapper.insert(careTask);

    elderLifecycleStateService.transition(
        elder,
        ElderLifecycleStatus.DISCHARGED,
        ElderDepartureType.NORMAL,
        "TEST_DISCHARGE",
        "test",
        "TEST",
        1L,
        500L);

    assertEquals(4, storeOrderMapper.selectById(order.getId()).getOrderStatus());
    assertEquals(2, visitBookingMapper.selectById(visitBooking.getId()).getStatus());
    assertEquals("INACTIVE", servicePlanMapper.selectById(plan.getId()).getStatus());
    assertEquals("CANCELLED", serviceBookingMapper.selectById(serviceBooking.getId()).getStatus());
    ElderPackage syncedPackage = elderPackageMapper.selectById(elderPackage.getId());
    assertEquals(0, syncedPackage.getStatus());
    assertNotNull(syncedPackage.getEndDate());
    assertEquals("CANCELLED", careTaskDailyMapper.selectById(careTask.getId()).getStatus());
    assertEquals("CANCELLED", healthMedicationTaskMapper.selectById(medicationTask.getId()).getStatus());
    assertEquals(LocalDate.now(), healthMedicationSettingMapper.selectById(setting.getId()).getEndDate());
    assertEquals(1L, careTaskDailyMapper.selectCount(
        Wrappers.lambdaQuery(CareTaskDaily.class)
            .eq(CareTaskDaily::getElderId, 4003L)
            .eq(CareTaskDaily::getStatus, "CANCELLED")));
  }
}
