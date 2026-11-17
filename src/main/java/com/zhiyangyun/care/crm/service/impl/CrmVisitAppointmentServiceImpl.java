package com.zhiyangyun.care.crm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.entity.CrmVisitAppointment;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import com.zhiyangyun.care.crm.mapper.CrmVisitAppointmentMapper;
import com.zhiyangyun.care.crm.model.visit.CrmVisitAppointmentCancelRequest;
import com.zhiyangyun.care.crm.model.visit.CrmVisitAppointmentCompleteRequest;
import com.zhiyangyun.care.crm.model.visit.CrmVisitAppointmentRequest;
import com.zhiyangyun.care.crm.model.visit.CrmVisitAppointmentResponse;
import com.zhiyangyun.care.crm.service.CrmVisitAppointmentService;
import com.zhiyangyun.care.common.web.PageGuard;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrmVisitAppointmentServiceImpl implements CrmVisitAppointmentService {
  private final CrmVisitAppointmentMapper appointmentMapper;
  private final CrmLeadMapper leadMapper;

  public CrmVisitAppointmentServiceImpl(CrmVisitAppointmentMapper appointmentMapper, CrmLeadMapper leadMapper) {
    this.appointmentMapper = appointmentMapper;
    this.leadMapper = leadMapper;
  }

  @Override
  public IPage<CrmVisitAppointmentResponse> page(Long tenantId, Long staffId, boolean manager, long pageNo, long pageSize,
      String keyword, String status, String dateFrom, String dateTo, Long leadId) {
    var query = Wrappers.lambdaQuery(CrmVisitAppointment.class)
        .eq(CrmVisitAppointment::getTenantId, tenantId)
        .eq(CrmVisitAppointment::getIsDeleted, 0)
        .eq(leadId != null, CrmVisitAppointment::getLeadId, leadId);
    if (!manager) {
      query.and(w -> w.eq(CrmVisitAppointment::getOwnerStaffId, staffId)
          .or().eq(CrmVisitAppointment::getCreatedBy, staffId));
    }
    if (status != null && !status.isBlank()) query.eq(CrmVisitAppointment::getStatus, status.trim().toUpperCase());
    if (dateFrom != null && !dateFrom.isBlank()) {
      query.ge(CrmVisitAppointment::getAppointmentTime, parseDate(dateFrom, "开始日期").atStartOfDay());
    }
    if (dateTo != null && !dateTo.isBlank()) {
      query.le(CrmVisitAppointment::getAppointmentTime, parseDate(dateTo, "结束日期").atTime(LocalTime.MAX));
    }
    List<Long> matchingLeadIds = findMatchingLeadIds(tenantId, keyword);
    if (keyword != null && !keyword.isBlank()) {
      String text = keyword.trim();
      query.and(w -> w.like(CrmVisitAppointment::getVisitorName, text)
          .or().like(CrmVisitAppointment::getVisitorPhone, text)
          .or().like(CrmVisitAppointment::getOwnerStaffName, text)
          .or(matchingLeadIds.isEmpty(), x -> x.eq(CrmVisitAppointment::getId, -1L))
          .or(!matchingLeadIds.isEmpty(), x -> x.in(CrmVisitAppointment::getLeadId, matchingLeadIds)));
    }
    Page<CrmVisitAppointment> entityPage = new Page<>(PageGuard.pageNo(pageNo), PageGuard.pageSize(pageSize));
    entityPage.addOrder(OrderItem.asc("appointment_time"));
    IPage<CrmVisitAppointment> selected = appointmentMapper.selectPage(entityPage, query);
    Map<Long, CrmLead> leads = loadLeads(tenantId, selected.getRecords());
    return selected.convert(item -> toResponse(item, leads.get(item.getLeadId())));
  }

  @Override
  @Transactional
  public CrmVisitAppointmentResponse create(Long tenantId, Long orgId, Long staffId, boolean manager,
      CrmVisitAppointmentRequest request) {
    CrmLead lead = requireLead(tenantId, staffId, manager, request.getLeadId());
    LocalDateTime appointmentTime = parseDateTime(request.getAppointmentTime(), "预约参观时间");
    long duplicate = appointmentMapper.selectCount(Wrappers.lambdaQuery(CrmVisitAppointment.class)
        .eq(CrmVisitAppointment::getTenantId, tenantId)
        .eq(CrmVisitAppointment::getLeadId, lead.getId())
        .eq(CrmVisitAppointment::getAppointmentTime, appointmentTime)
        .eq(CrmVisitAppointment::getIsDeleted, 0)
        .ne(CrmVisitAppointment::getStatus, "CANCELLED"));
    if (duplicate > 0) throw new IllegalStateException("该客户在相同时间已有参观预约，请勿重复创建");

    CrmVisitAppointment item = new CrmVisitAppointment();
    item.setTenantId(tenantId);
    item.setOrgId(orgId);
    item.setLeadId(lead.getId());
    item.setAppointmentTime(appointmentTime);
    item.setVisitorName(request.getVisitorName().trim());
    item.setVisitorPhone(blankToNull(request.getVisitorPhone()));
    item.setPartySize(request.getPartySize() == null ? 1 : request.getPartySize());
    item.setOwnerStaffId(lead.getOwnerStaffId() == null ? staffId : lead.getOwnerStaffId());
    item.setOwnerStaffName(firstNonBlank(request.getOwnerStaffName(), lead.getOwnerStaffName(), lead.getMarketerName()));
    item.setStatus("SCHEDULED");
    item.setRemark(blankToNull(request.getRemark()));
    item.setCreatedBy(staffId);
    appointmentMapper.insert(item);
    markLeadScheduled(lead, appointmentTime.toLocalDate());
    return toResponse(item, lead);
  }

  @Override
  @Transactional
  public CrmVisitAppointmentResponse update(Long tenantId, Long staffId, boolean manager, Long id,
      CrmVisitAppointmentRequest request) {
    CrmVisitAppointment item = requireAppointment(tenantId, staffId, manager, id);
    if (!"SCHEDULED".equals(item.getStatus())) throw new IllegalStateException("只有待到访预约可以编辑");
    CrmLead lead = requireLead(tenantId, staffId, manager, request.getLeadId());
    item.setLeadId(lead.getId());
    item.setAppointmentTime(parseDateTime(request.getAppointmentTime(), "预约参观时间"));
    item.setVisitorName(request.getVisitorName().trim());
    item.setVisitorPhone(blankToNull(request.getVisitorPhone()));
    item.setPartySize(request.getPartySize() == null ? 1 : request.getPartySize());
    item.setOwnerStaffId(lead.getOwnerStaffId() == null ? staffId : lead.getOwnerStaffId());
    item.setOwnerStaffName(firstNonBlank(request.getOwnerStaffName(), lead.getOwnerStaffName(), lead.getMarketerName()));
    item.setRemark(blankToNull(request.getRemark()));
    appointmentMapper.updateById(item);
    markLeadScheduled(lead, item.getAppointmentTime().toLocalDate());
    return toResponse(item, lead);
  }

  @Override
  @Transactional
  public CrmVisitAppointmentResponse arrive(Long tenantId, Long staffId, boolean manager, Long id) {
    CrmVisitAppointment item = requireAppointment(tenantId, staffId, manager, id);
    if (!"SCHEDULED".equals(item.getStatus())) throw new IllegalStateException("只有待到访预约可以登记到访");
    item.setStatus("ARRIVED");
    item.setArrivedAt(LocalDateTime.now());
    appointmentMapper.updateById(item);
    return toResponse(item, leadMapper.selectById(item.getLeadId()));
  }

  @Override
  @Transactional
  public CrmVisitAppointmentResponse complete(Long tenantId, Long staffId, boolean manager, Long id,
      CrmVisitAppointmentCompleteRequest request) {
    CrmVisitAppointment item = requireAppointment(tenantId, staffId, manager, id);
    if (!"ARRIVED".equals(item.getStatus())) throw new IllegalStateException("请先登记客户到访，再填写参观结果");
    item.setStatus("COMPLETED");
    item.setCompletedAt(LocalDateTime.now());
    item.setVisitResult(request.getVisitResult().trim());
    item.setNextFollowDate(parseOptionalDate(request.getNextFollowDate(), "下次跟进日期"));
    if (request.getRemark() != null) item.setRemark(blankToNull(request.getRemark()));
    appointmentMapper.updateById(item);
    CrmLead lead = leadMapper.selectById(item.getLeadId());
    if (lead != null) {
      lead.setFollowupStatus("参观完成");
      if (item.getNextFollowDate() != null) lead.setNextFollowDate(item.getNextFollowDate());
      lead.setRemark(appendRemark(lead.getRemark(), "参观结果：" + item.getVisitResult()));
      leadMapper.updateById(lead);
    }
    return toResponse(item, lead);
  }

  @Override
  @Transactional
  public CrmVisitAppointmentResponse cancel(Long tenantId, Long staffId, boolean manager, Long id,
      CrmVisitAppointmentCancelRequest request) {
    CrmVisitAppointment item = requireAppointment(tenantId, staffId, manager, id);
    if ("COMPLETED".equals(item.getStatus()) || "CANCELLED".equals(item.getStatus())) {
      throw new IllegalStateException("已完成或已取消的预约不能再次取消");
    }
    item.setStatus("CANCELLED");
    item.setCancelReason(request.getCancelReason().trim());
    appointmentMapper.updateById(item);
    return toResponse(item, leadMapper.selectById(item.getLeadId()));
  }

  private CrmVisitAppointment requireAppointment(Long tenantId, Long staffId, boolean manager, Long id) {
    CrmVisitAppointment item = appointmentMapper.selectById(id);
    if (item == null || !tenantId.equals(item.getTenantId()) || Integer.valueOf(1).equals(item.getIsDeleted())
        || (!manager && !staffId.equals(item.getOwnerStaffId()) && !staffId.equals(item.getCreatedBy()))) {
      throw new IllegalArgumentException("参观预约不存在或无权限访问");
    }
    return item;
  }

  private CrmLead requireLead(Long tenantId, Long staffId, boolean manager, Long leadId) {
    CrmLead lead = leadMapper.selectById(leadId);
    boolean accessible = lead != null && tenantId.equals(lead.getTenantId()) && !Integer.valueOf(1).equals(lead.getIsDeleted())
        && (manager || staffId.equals(lead.getOwnerStaffId())
        || (lead.getOwnerStaffId() == null && staffId.equals(lead.getCreatedBy())));
    if (!accessible) throw new IllegalArgumentException("客户线索不存在或无权限访问");
    return lead;
  }

  private void markLeadScheduled(CrmLead lead, LocalDate date) {
    lead.setNextFollowDate(date);
    lead.setFollowupStatus("已预约参观");
    leadMapper.updateById(lead);
  }

  private List<Long> findMatchingLeadIds(Long tenantId, String keyword) {
    if (keyword == null || keyword.isBlank()) return List.of();
    String text = keyword.trim();
    return leadMapper.selectList(Wrappers.lambdaQuery(CrmLead.class)
            .select(CrmLead::getId)
            .eq(CrmLead::getTenantId, tenantId)
            .eq(CrmLead::getIsDeleted, 0)
            .and(w -> w.like(CrmLead::getName, text).or().like(CrmLead::getElderName, text)
                .or().like(CrmLead::getPhone, text).or().like(CrmLead::getElderPhone, text)))
        .stream().map(CrmLead::getId).toList();
  }

  private Map<Long, CrmLead> loadLeads(Long tenantId, List<CrmVisitAppointment> items) {
    List<Long> ids = items.stream().map(CrmVisitAppointment::getLeadId).distinct().toList();
    if (ids.isEmpty()) return Map.of();
    Map<Long, CrmLead> result = new LinkedHashMap<>();
    leadMapper.selectList(Wrappers.lambdaQuery(CrmLead.class)
        .eq(CrmLead::getTenantId, tenantId).in(CrmLead::getId, ids))
        .forEach(lead -> result.put(lead.getId(), lead));
    return result;
  }

  private CrmVisitAppointmentResponse toResponse(CrmVisitAppointment item, CrmLead lead) {
    CrmVisitAppointmentResponse response = new CrmVisitAppointmentResponse();
    response.setId(item.getId());
    response.setLeadId(item.getLeadId());
    response.setLeadName(lead == null ? null : lead.getName());
    response.setElderName(lead == null ? null : lead.getElderName());
    response.setAppointmentTime(item.getAppointmentTime());
    response.setVisitorName(item.getVisitorName());
    response.setVisitorPhone(item.getVisitorPhone());
    response.setPartySize(item.getPartySize());
    response.setOwnerStaffId(item.getOwnerStaffId());
    response.setOwnerStaffName(item.getOwnerStaffName());
    response.setStatus(item.getStatus());
    response.setArrivedAt(item.getArrivedAt());
    response.setCompletedAt(item.getCompletedAt());
    response.setVisitResult(item.getVisitResult());
    response.setCancelReason(item.getCancelReason());
    response.setNextFollowDate(item.getNextFollowDate());
    response.setRemark(item.getRemark());
    response.setCreateTime(item.getCreateTime());
    return response;
  }

  private LocalDateTime parseDateTime(String value, String label) {
    try {
      return LocalDateTime.parse(value.trim().replace(' ', 'T'));
    } catch (DateTimeParseException ex) {
      throw new IllegalArgumentException(label + "格式不正确");
    }
  }

  private LocalDate parseDate(String value, String label) {
    try {
      return LocalDate.parse(value.trim());
    } catch (DateTimeParseException ex) {
      throw new IllegalArgumentException(label + "格式不正确");
    }
  }

  private LocalDate parseOptionalDate(String value, String label) {
    return value == null || value.isBlank() ? null : parseDate(value, label);
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private String firstNonBlank(String... values) {
    for (String value : values) if (value != null && !value.isBlank()) return value.trim();
    return null;
  }

  private String appendRemark(String current, String addition) {
    String combined = current == null || current.isBlank() ? addition : current.trim() + "；" + addition;
    return combined.length() <= 255 ? combined : combined.substring(combined.length() - 255);
  }
}
