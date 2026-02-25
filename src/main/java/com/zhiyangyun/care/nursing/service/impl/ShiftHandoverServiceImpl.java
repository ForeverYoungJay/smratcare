package com.zhiyangyun.care.nursing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.nursing.entity.ShiftHandover;
import com.zhiyangyun.care.nursing.mapper.ShiftHandoverMapper;
import com.zhiyangyun.care.nursing.model.ShiftHandoverRequest;
import com.zhiyangyun.care.nursing.model.ShiftHandoverResponse;
import com.zhiyangyun.care.nursing.service.ShiftHandoverService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ShiftHandoverServiceImpl implements ShiftHandoverService {
  private final ShiftHandoverMapper handoverMapper;
  private final StaffMapper staffMapper;

  public ShiftHandoverServiceImpl(ShiftHandoverMapper handoverMapper, StaffMapper staffMapper) {
    this.handoverMapper = handoverMapper;
    this.staffMapper = staffMapper;
  }

  @Override
  public ShiftHandoverResponse create(ShiftHandoverRequest request) {
    ShiftHandover handover = new ShiftHandover();
    fillEntity(handover, request);
    handover.setCreatedBy(request.getCreatedBy());
    handoverMapper.insert(handover);
    return toResponse(handover, Collections.emptyMap());
  }

  @Override
  public ShiftHandoverResponse update(Long id, ShiftHandoverRequest request) {
    ShiftHandover handover = handoverMapper.selectById(id);
    if (handover == null || !request.getTenantId().equals(handover.getTenantId())) {
      return null;
    }
    fillEntity(handover, request);
    handoverMapper.updateById(handover);
    return toResponse(handover, Collections.emptyMap());
  }

  @Override
  public ShiftHandoverResponse get(Long id, Long tenantId) {
    ShiftHandover handover = handoverMapper.selectById(id);
    if (handover == null || (tenantId != null && !tenantId.equals(handover.getTenantId()))) {
      return null;
    }
    Set<Long> staffIds = new HashSet<>();
    if (handover.getFromStaffId() != null) {
      staffIds.add(handover.getFromStaffId());
    }
    if (handover.getToStaffId() != null) {
      staffIds.add(handover.getToStaffId());
    }
    Map<Long, StaffAccount> staffMap = staffIds.isEmpty() ? Collections.emptyMap()
        : staffMapper.selectBatchIds(staffIds).stream()
            .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));
    return toResponse(handover, staffMap);
  }

  @Override
  public IPage<ShiftHandoverResponse> page(Long tenantId, long pageNo, long pageSize, LocalDate dateFrom,
      LocalDate dateTo, String shiftCode, String status) {
    var wrapper = Wrappers.lambdaQuery(ShiftHandover.class)
        .eq(ShiftHandover::getIsDeleted, 0)
        .eq(tenantId != null, ShiftHandover::getTenantId, tenantId)
        .eq(shiftCode != null && !shiftCode.isBlank(), ShiftHandover::getShiftCode, shiftCode)
        .eq(status != null && !status.isBlank(), ShiftHandover::getStatus, status)
        .ge(dateFrom != null, ShiftHandover::getDutyDate, dateFrom)
        .le(dateTo != null, ShiftHandover::getDutyDate, dateTo)
        .orderByDesc(ShiftHandover::getDutyDate)
        .orderByDesc(ShiftHandover::getCreateTime);
    IPage<ShiftHandover> page = handoverMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);

    Set<Long> staffIds = page.getRecords().stream()
        .flatMap(item -> java.util.stream.Stream.of(item.getFromStaffId(), item.getToStaffId()))
        .filter(id -> id != null && id > 0)
        .collect(Collectors.toSet());
    Map<Long, StaffAccount> staffMap = staffIds.isEmpty() ? Collections.emptyMap()
        : staffMapper.selectBatchIds(staffIds).stream()
            .collect(Collectors.toMap(StaffAccount::getId, s -> s, (a, b) -> a));

    IPage<ShiftHandoverResponse> resp = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    resp.setRecords(page.getRecords().stream().map(item -> toResponse(item, staffMap)).toList());
    return resp;
  }

  @Override
  public void delete(Long id, Long tenantId) {
    ShiftHandover handover = handoverMapper.selectById(id);
    if (handover != null && tenantId != null && tenantId.equals(handover.getTenantId())) {
      handover.setIsDeleted(1);
      handoverMapper.updateById(handover);
    }
  }

  private void fillEntity(ShiftHandover handover, ShiftHandoverRequest request) {
    handover.setTenantId(request.getTenantId());
    handover.setOrgId(request.getOrgId());
    handover.setDutyDate(request.getDutyDate());
    handover.setShiftCode(request.getShiftCode());
    handover.setFromStaffId(request.getFromStaffId());
    handover.setToStaffId(request.getToStaffId());
    handover.setSummary(request.getSummary());
    handover.setRiskNote(request.getRiskNote());
    handover.setTodoNote(request.getTodoNote());
    handover.setStatus(request.getStatus() == null ? "DRAFT" : request.getStatus());
    handover.setHandoverTime(request.getHandoverTime());
    handover.setConfirmTime(request.getConfirmTime());
  }

  private ShiftHandoverResponse toResponse(ShiftHandover handover, Map<Long, StaffAccount> staffMap) {
    ShiftHandoverResponse response = new ShiftHandoverResponse();
    response.setId(handover.getId());
    response.setTenantId(handover.getTenantId());
    response.setOrgId(handover.getOrgId());
    response.setDutyDate(handover.getDutyDate());
    response.setShiftCode(handover.getShiftCode());
    response.setFromStaffId(handover.getFromStaffId());
    response.setToStaffId(handover.getToStaffId());
    response.setSummary(handover.getSummary());
    response.setRiskNote(handover.getRiskNote());
    response.setTodoNote(handover.getTodoNote());
    response.setStatus(handover.getStatus());
    response.setHandoverTime(handover.getHandoverTime());
    response.setConfirmTime(handover.getConfirmTime());

    StaffAccount fromStaff = handover.getFromStaffId() == null ? null : staffMap.get(handover.getFromStaffId());
    if (fromStaff == null && handover.getFromStaffId() != null) {
      fromStaff = staffMapper.selectById(handover.getFromStaffId());
    }
    response.setFromStaffName(fromStaff == null ? null : fromStaff.getRealName());

    StaffAccount toStaff = handover.getToStaffId() == null ? null : staffMap.get(handover.getToStaffId());
    if (toStaff == null && handover.getToStaffId() != null) {
      toStaff = staffMapper.selectById(handover.getToStaffId());
    }
    response.setToStaffName(toStaff == null ? null : toStaff.getRealName());
    return response;
  }
}
