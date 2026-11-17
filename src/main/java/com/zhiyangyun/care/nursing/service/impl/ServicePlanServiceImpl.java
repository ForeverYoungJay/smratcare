package com.zhiyangyun.care.nursing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.nursing.entity.CareLevel;
import com.zhiyangyun.care.nursing.entity.ServicePlan;
import com.zhiyangyun.care.nursing.mapper.CareLevelMapper;
import com.zhiyangyun.care.nursing.entity.ServicePlanRevision;
import com.zhiyangyun.care.nursing.mapper.ServicePlanMapper;
import com.zhiyangyun.care.nursing.mapper.ServicePlanRevisionMapper;
import com.zhiyangyun.care.nursing.model.ServicePlanRequest;
import com.zhiyangyun.care.nursing.model.ServicePlanResponse;
import com.zhiyangyun.care.nursing.service.ServicePlanService;
import com.zhiyangyun.care.standard.entity.ServiceItem;
import com.zhiyangyun.care.standard.mapper.ServiceItemMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicePlanServiceImpl implements ServicePlanService {
  private final ServicePlanMapper servicePlanMapper;
  private final ElderMapper elderMapper;
  private final CareLevelMapper careLevelMapper;
  private final ServiceItemMapper serviceItemMapper;
  private final StaffMapper staffMapper;
  private final ServicePlanRevisionMapper revisionMapper;
  private final ObjectMapper objectMapper;

  public ServicePlanServiceImpl(ServicePlanMapper servicePlanMapper, ElderMapper elderMapper, CareLevelMapper careLevelMapper,
      ServiceItemMapper serviceItemMapper, StaffMapper staffMapper, ServicePlanRevisionMapper revisionMapper,
      ObjectMapper objectMapper) {
    this.revisionMapper = revisionMapper;
    this.objectMapper = objectMapper;
    this.servicePlanMapper = servicePlanMapper;
    this.elderMapper = elderMapper;
    this.careLevelMapper = careLevelMapper;
    this.serviceItemMapper = serviceItemMapper;
    this.staffMapper = staffMapper;
  }

  @Override
  public ServicePlanResponse create(ServicePlanRequest request) {
    ServicePlan plan = new ServicePlan();
    fillEntity(plan, request);
    plan.setCreatedBy(request.getCreatedBy());
    plan.setRevision(1);
    plan.setApprovalStatus("PENDING");
    servicePlanMapper.insert(plan);
    return buildResponse(plan, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
  }

  @Override
  @Transactional
  public ServicePlanResponse update(Long id, ServicePlanRequest request, Long operatorId, String operatorName) {
    ServicePlan plan = servicePlanMapper.selectById(id);
    if (plan == null || !request.getTenantId().equals(plan.getTenantId())) {
      return null;
    }
    if (isPublished(plan)) {
      ServicePlanRevision revision = new ServicePlanRevision();
      revision.setTenantId(plan.getTenantId());
      revision.setPlanId(plan.getId());
      revision.setRevision(plan.getRevision() == null ? 1 : plan.getRevision());
      try {
        revision.setSnapshotJson(objectMapper.writeValueAsString(plan));
      } catch (JsonProcessingException ex) {
        throw new IllegalStateException("护理计划快照生成失败", ex);
      }
      revision.setChangeReason(request.getChangeReason());
      revision.setChangedBy(operatorId);
      revision.setChangedByName(operatorName);
      revisionMapper.insert(revision);
      plan.setRevision(revision.getRevision() + 1);
    }
    // 任何修改都要重新审核；审核信息只对应当前版本，上一版的审核人见历史版本
    plan.setApprovalStatus("PENDING");
    plan.setApprovedBy(null);
    plan.setApprovedAt(null);
    plan.setRejectReason(null);
    fillEntity(plan, request);
    servicePlanMapper.updateById(plan);
    clearReview(plan.getId(), true);
    return buildResponse(plan, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
  }

  @Override
  public ServicePlanResponse get(Long id, Long tenantId) {
    ServicePlan plan = servicePlanMapper.selectById(id);
    if (plan == null || (tenantId != null && !tenantId.equals(plan.getTenantId()))) {
      return null;
    }
    return buildResponse(plan, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
  }

  @Override
  public IPage<ServicePlanResponse> page(Long tenantId, long pageNo, long pageSize, Long elderId, String status,
      String keyword) {
    var wrapper = Wrappers.lambdaQuery(ServicePlan.class)
        .eq(ServicePlan::getIsDeleted, 0)
        .eq(tenantId != null, ServicePlan::getTenantId, tenantId)
        .eq(elderId != null, ServicePlan::getElderId, elderId)
        .eq(status != null && !status.isBlank(), ServicePlan::getStatus, status)
        .orderByDesc(ServicePlan::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.like(ServicePlan::getPlanName, keyword);
    }

    IPage<ServicePlan> page = servicePlanMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);

    Set<Long> elderIds = page.getRecords().stream().map(ServicePlan::getElderId)
        .filter(id -> id != null && id > 0)
        .collect(Collectors.toSet());
    Set<Long> careLevelIds = page.getRecords().stream().map(ServicePlan::getCareLevelId)
        .filter(id -> id != null && id > 0)
        .collect(Collectors.toSet());
    Set<Long> itemIds = page.getRecords().stream().map(ServicePlan::getServiceItemId)
        .filter(id -> id != null && id > 0)
        .collect(Collectors.toSet());
    Set<Long> staffIds = page.getRecords().stream().map(ServicePlan::getDefaultStaffId)
        .filter(id -> id != null && id > 0)
        .collect(Collectors.toSet());

    Map<Long, ElderProfile> elderMap = elderIds.isEmpty() ? Collections.emptyMap()
        : elderMapper.selectBatchIds(elderIds).stream().collect(Collectors.toMap(ElderProfile::getId, e -> e, (a, b) -> a));
    Map<Long, CareLevel> careLevelMap = careLevelIds.isEmpty() ? Collections.emptyMap()
        : careLevelMapper.selectBatchIds(careLevelIds).stream().collect(Collectors.toMap(CareLevel::getId, c -> c, (a, b) -> a));
    Map<Long, ServiceItem> serviceItemMap = itemIds.isEmpty() ? Collections.emptyMap()
        : serviceItemMapper.selectBatchIds(itemIds).stream().collect(Collectors.toMap(ServiceItem::getId, s -> s, (a, b) -> a));
    Map<Long, StaffAccount> staffMap = staffIds.isEmpty() ? Collections.emptyMap()
        : staffMapper.selectBatchIdsSafe(staffIds).stream().collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));

    IPage<ServicePlanResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream()
        .map(item -> buildResponse(item, elderMap, careLevelMap, serviceItemMap, staffMap))
        .toList());
    return resp;
  }

  @Override
  public List<ServicePlanResponse> list(Long tenantId, String status) {
    var wrapper = Wrappers.lambdaQuery(ServicePlan.class)
        .eq(ServicePlan::getIsDeleted, 0)
        .eq(tenantId != null, ServicePlan::getTenantId, tenantId)
        .eq(status != null && !status.isBlank(), ServicePlan::getStatus, status)
        .orderByDesc(ServicePlan::getCreateTime);
    return servicePlanMapper.selectList(wrapper).stream()
        .map(item -> buildResponse(item, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()))
        .toList();
  }

  @Override
  public ServicePlanResponse approve(Long id, Long tenantId, Long operatorId) {
    ServicePlan plan = loadPending(id, tenantId);
    if (plan == null) {
      return null;
    }
    plan.setApprovalStatus("PUBLISHED");
    plan.setApprovedBy(operatorId);
    plan.setApprovedAt(LocalDateTime.now());
    plan.setRejectReason(null);
    servicePlanMapper.updateById(plan);
    clearReview(plan.getId(), false);
    return get(id, tenantId);
  }

  @Override
  public ServicePlanResponse reject(Long id, Long tenantId, String reason) {
    ServicePlan plan = loadPending(id, tenantId);
    if (plan == null) {
      return null;
    }
    plan.setApprovalStatus("REJECTED");
    plan.setRejectReason(reason);
    servicePlanMapper.updateById(plan);
    return get(id, tenantId);
  }

  @Override
  public List<ServicePlanRevision> revisions(Long id, Long tenantId) {
    return revisionMapper.selectList(Wrappers.lambdaQuery(ServicePlanRevision.class)
        .eq(ServicePlanRevision::getTenantId, tenantId)
        .eq(ServicePlanRevision::getPlanId, id)
        .orderByDesc(ServicePlanRevision::getRevision)
        .orderByDesc(ServicePlanRevision::getCreateTime));
  }

  /** updateById 会跳过 null 字段，清空审核信息要单独写 */
  private void clearReview(Long id, boolean approval) {
    var wrapper = Wrappers.lambdaUpdate(ServicePlan.class).eq(ServicePlan::getId, id).set(ServicePlan::getRejectReason, null);
    if (approval) {
      wrapper.set(ServicePlan::getApprovedBy, null).set(ServicePlan::getApprovedAt, null);
    }
    servicePlanMapper.update(null, wrapper);
  }

  private ServicePlan loadPending(Long id, Long tenantId) {
    ServicePlan plan = servicePlanMapper.selectById(id);
    if (plan == null || tenantId == null || !tenantId.equals(plan.getTenantId())) {
      return null;
    }
    if (!"PENDING".equals(plan.getApprovalStatus())) {
      throw new IllegalStateException("当前状态不允许该操作");
    }
    return plan;
  }

  private boolean isPublished(ServicePlan plan) {
    return plan.getApprovalStatus() == null || "PUBLISHED".equals(plan.getApprovalStatus());
  }

  @Override
  public void delete(Long id, Long tenantId) {
    ServicePlan plan = servicePlanMapper.selectById(id);
    if (plan != null && tenantId != null && tenantId.equals(plan.getTenantId())) {
      plan.setIsDeleted(1);
      servicePlanMapper.updateById(plan);
    }
  }

  private void fillEntity(ServicePlan plan, ServicePlanRequest request) {
    plan.setTenantId(request.getTenantId());
    plan.setOrgId(request.getOrgId());
    plan.setElderId(request.getElderId());
    plan.setCareLevelId(request.getCareLevelId());
    plan.setServiceItemId(request.getServiceItemId());
    plan.setPlanName(request.getPlanName());
    plan.setCycleType(request.getCycleType() == null ? "DAILY" : request.getCycleType());
    plan.setFrequency(request.getFrequency() == null ? 1 : request.getFrequency());
    plan.setStartDate(request.getStartDate());
    plan.setEndDate(request.getEndDate());
    plan.setPreferredTime(request.getPreferredTime());
    plan.setDefaultStaffId(request.getDefaultStaffId());
    plan.setStatus(request.getStatus() == null ? "ACTIVE" : request.getStatus());
    plan.setRemark(request.getRemark());
  }

  private ServicePlanResponse buildResponse(ServicePlan plan,
      Map<Long, ElderProfile> elderMap,
      Map<Long, CareLevel> careLevelMap,
      Map<Long, ServiceItem> serviceItemMap,
      Map<Long, StaffAccount> staffMap) {
    ServicePlanResponse response = new ServicePlanResponse();
    response.setId(plan.getId());
    response.setTenantId(plan.getTenantId());
    response.setOrgId(plan.getOrgId());
    response.setElderId(plan.getElderId());
    response.setCareLevelId(plan.getCareLevelId());
    response.setServiceItemId(plan.getServiceItemId());
    response.setPlanName(plan.getPlanName());
    response.setCycleType(plan.getCycleType());
    response.setFrequency(plan.getFrequency());
    response.setStartDate(plan.getStartDate());
    response.setEndDate(plan.getEndDate());
    response.setPreferredTime(plan.getPreferredTime());
    response.setDefaultStaffId(plan.getDefaultStaffId());
    response.setStatus(plan.getStatus());
    response.setRemark(plan.getRemark());
    response.setRevision(plan.getRevision());
    response.setApprovalStatus(plan.getApprovalStatus());
    response.setApprovedBy(plan.getApprovedBy());
    response.setApprovedAt(plan.getApprovedAt());
    response.setRejectReason(plan.getRejectReason());

    ElderProfile elder = elderMap.get(plan.getElderId());
    if (elder == null && plan.getElderId() != null) {
      elder = elderMapper.selectById(plan.getElderId());
    }
    response.setElderName(elder == null ? null : elder.getFullName());

    CareLevel careLevel = careLevelMap.get(plan.getCareLevelId());
    if (careLevel == null && plan.getCareLevelId() != null) {
      careLevel = careLevelMapper.selectById(plan.getCareLevelId());
    }
    response.setCareLevelName(careLevel == null ? null : careLevel.getLevelName());

    ServiceItem serviceItem = serviceItemMap.get(plan.getServiceItemId());
    if (serviceItem == null && plan.getServiceItemId() != null) {
      serviceItem = serviceItemMapper.selectById(plan.getServiceItemId());
    }
    response.setServiceItemName(serviceItem == null ? null : serviceItem.getName());

    StaffAccount staff = staffMap.get(plan.getDefaultStaffId());
    if (staff == null && plan.getDefaultStaffId() != null) {
      staff = staffMapper.selectById(plan.getDefaultStaffId());
    }
    response.setDefaultStaffName(staff == null ? null : staff.getRealName());
    return response;
  }
}
