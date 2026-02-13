package com.zhiyangyun.care.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import com.zhiyangyun.care.oa.model.OaApprovalRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oa/approval")
public class OaApprovalController {
  private final OaApprovalMapper approvalMapper;

  public OaApprovalController(OaApprovalMapper approvalMapper) {
    this.approvalMapper = approvalMapper;
  }

  @GetMapping("/page")
  public Result<IPage<OaApproval>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String type) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(OaApproval.class)
        .eq(OaApproval::getIsDeleted, 0)
        .eq(orgId != null, OaApproval::getOrgId, orgId)
        .eq(status != null && !status.isBlank(), OaApproval::getStatus, status)
        .eq(type != null && !type.isBlank(), OaApproval::getApprovalType, type)
        .orderByDesc(OaApproval::getCreateTime);
    return Result.ok(approvalMapper.selectPage(new Page<>(pageNo, pageSize), wrapper));
  }

  @PostMapping
  public Result<OaApproval> create(@Valid @RequestBody OaApprovalRequest request) {
    Long orgId = AuthContext.getOrgId();
    OaApproval approval = new OaApproval();
    approval.setTenantId(orgId);
    approval.setOrgId(orgId);
    approval.setApprovalType(request.getApprovalType());
    approval.setTitle(request.getTitle());
    approval.setApplicantId(request.getApplicantId());
    approval.setApplicantName(request.getApplicantName());
    approval.setAmount(request.getAmount());
    approval.setStartTime(request.getStartTime());
    approval.setEndTime(request.getEndTime());
    approval.setFormData(request.getFormData());
    approval.setStatus(request.getStatus() == null ? "PENDING" : request.getStatus());
    approval.setRemark(request.getRemark());
    approval.setCreatedBy(AuthContext.getStaffId());
    approvalMapper.insert(approval);
    return Result.ok(approval);
  }

  @PutMapping("/{id}")
  public Result<OaApproval> update(@PathVariable Long id, @Valid @RequestBody OaApprovalRequest request) {
    OaApproval approval = approvalMapper.selectById(id);
    if (approval == null) {
      return Result.ok(null);
    }
    approval.setApprovalType(request.getApprovalType());
    approval.setTitle(request.getTitle());
    approval.setApplicantId(request.getApplicantId());
    approval.setApplicantName(request.getApplicantName());
    approval.setAmount(request.getAmount());
    approval.setStartTime(request.getStartTime());
    approval.setEndTime(request.getEndTime());
    approval.setFormData(request.getFormData());
    approval.setStatus(request.getStatus());
    approval.setRemark(request.getRemark());
    approvalMapper.updateById(approval);
    return Result.ok(approval);
  }

  @PutMapping("/{id}/approve")
  public Result<OaApproval> approve(@PathVariable Long id) {
    OaApproval approval = approvalMapper.selectById(id);
    if (approval == null) {
      return Result.ok(null);
    }
    approval.setStatus("APPROVED");
    approvalMapper.updateById(approval);
    return Result.ok(approval);
  }

  @PutMapping("/{id}/reject")
  public Result<OaApproval> reject(@PathVariable Long id, @RequestParam(required = false) String remark) {
    OaApproval approval = approvalMapper.selectById(id);
    if (approval == null) {
      return Result.ok(null);
    }
    approval.setStatus("REJECTED");
    approval.setRemark(remark);
    approvalMapper.updateById(approval);
    return Result.ok(approval);
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    OaApproval approval = approvalMapper.selectById(id);
    if (approval != null) {
      approval.setIsDeleted(1);
      approvalMapper.updateById(approval);
    }
    return Result.ok(null);
  }
}
