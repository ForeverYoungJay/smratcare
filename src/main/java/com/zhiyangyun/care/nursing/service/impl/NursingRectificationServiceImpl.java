package com.zhiyangyun.care.nursing.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.nursing.entity.NursingRectification;
import com.zhiyangyun.care.nursing.mapper.NursingRectificationMapper;
import com.zhiyangyun.care.nursing.model.NursingRectificationRequest;
import com.zhiyangyun.care.nursing.model.NursingRectificationReviewRequest;
import com.zhiyangyun.care.nursing.service.NursingRectificationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class NursingRectificationServiceImpl implements NursingRectificationService {
  private final NursingRectificationMapper rectificationMapper;

  public NursingRectificationServiceImpl(NursingRectificationMapper rectificationMapper) {
    this.rectificationMapper = rectificationMapper;
  }

  @Override
  public IPage<NursingRectification> page(Long tenantId, long pageNo, long pageSize, String status, String keyword,
      boolean overdue) {
    var wrapper = Wrappers.lambdaQuery(NursingRectification.class)
        .eq(NursingRectification::getIsDeleted, 0)
        .eq(NursingRectification::getTenantId, tenantId)
        .eq(status != null && !status.isBlank(), NursingRectification::getStatus, status)
        .lt(overdue, NursingRectification::getDueDate, LocalDate.now())
        .ne(overdue, NursingRectification::getStatus, "DONE")
        .orderByAsc(NursingRectification::getDueDate)
        .orderByDesc(NursingRectification::getCreateTime);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(NursingRectification::getTitle, keyword).or()
          .like(NursingRectification::getOwnerName, keyword).or()
          .like(NursingRectification::getSource, keyword));
    }
    return rectificationMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  public NursingRectification create(Long tenantId, NursingRectificationRequest request, Long operatorId) {
    NursingRectification row = new NursingRectification();
    row.setTenantId(tenantId);
    row.setOrgId(tenantId);
    row.setStatus("OPEN");
    row.setCreatedBy(operatorId);
    apply(row, request);
    rectificationMapper.insert(row);
    return row;
  }

  @Override
  public NursingRectification update(Long id, Long tenantId, NursingRectificationRequest request, Long operatorId,
      boolean manager) {
    NursingRectification row = load(id, tenantId);
    if (row == null) {
      return null;
    }
    if ("DONE".equals(row.getStatus())) {
      throw new IllegalStateException("当前状态不允许该操作");
    }
    if (manager) {
      apply(row, request);
    } else if (operatorId != null && operatorId.equals(row.getOwnerId())) {
      // 负责人只能补充整改证据并推进状态
      if (request.getEvidence() != null) {
        row.setEvidence(request.getEvidence().trim());
      }
      if (request.getStatus() != null && !request.getStatus().isBlank()) {
        row.setStatus(request.getStatus());
      }
    } else {
      throw new AccessDeniedException("仅整改负责人或护士长可修改");
    }
    rectificationMapper.updateById(row);
    return row;
  }

  @Override
  public NursingRectification review(Long id, Long tenantId, NursingRectificationReviewRequest request,
      Long operatorId, String operatorName) {
    NursingRectification row = load(id, tenantId);
    if (row == null) {
      return null;
    }
    if (!"PENDING_REVIEW".equals(row.getStatus())) {
      throw new IllegalStateException("当前状态不允许该操作");
    }
    boolean passed = Boolean.TRUE.equals(request.getPassed());
    row.setStatus(passed ? "DONE" : "IN_PROGRESS");
    row.setClosedAt(passed ? LocalDateTime.now() : null);
    row.setReviewResult(request.getReviewResult());
    row.setReviewerId(operatorId);
    row.setReviewerName(operatorName);
    rectificationMapper.updateById(row);
    return row;
  }

  private void apply(NursingRectification row, NursingRectificationRequest request) {
    if (request.getTitle() != null && !request.getTitle().isBlank()) {
      row.setTitle(request.getTitle().trim());
    }
    if (request.getSource() != null) {
      row.setSource(request.getSource().trim());
    }
    if (request.getOwnerId() != null) {
      row.setOwnerId(request.getOwnerId());
      row.setOwnerName(request.getOwnerName());
    }
    if (request.getDueDate() != null) {
      row.setDueDate(request.getDueDate());
    }
    if (request.getEvidence() != null) {
      row.setEvidence(request.getEvidence().trim());
    }
    if (request.getStatus() != null && !request.getStatus().isBlank()) {
      row.setStatus(request.getStatus());
    }
  }

  private NursingRectification load(Long id, Long tenantId) {
    NursingRectification row = rectificationMapper.selectById(id);
    if (row == null || !row.getTenantId().equals(tenantId)
        || (row.getIsDeleted() != null && row.getIsDeleted() == 1)) {
      return null;
    }
    return row;
  }
}
