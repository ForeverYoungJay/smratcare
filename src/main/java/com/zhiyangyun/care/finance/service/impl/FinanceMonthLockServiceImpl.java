package com.zhiyangyun.care.finance.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.audit.entity.AuditLog;
import com.zhiyangyun.care.audit.mapper.AuditLogMapper;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.finance.model.FinanceCrossPeriodApprovalRequest;
import com.zhiyangyun.care.finance.model.FinanceCrossPeriodApprovalResponse;
import com.zhiyangyun.care.finance.model.FinanceMonthLockStatusResponse;
import com.zhiyangyun.care.finance.model.FinanceMonthUnlockRequest;
import com.zhiyangyun.care.finance.service.FinanceMonthLockService;
import com.zhiyangyun.care.oa.entity.OaApproval;
import com.zhiyangyun.care.oa.mapper.OaApprovalMapper;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class FinanceMonthLockServiceImpl implements FinanceMonthLockService {
  private final AuditLogMapper auditLogMapper;
  private final OaApprovalMapper oaApprovalMapper;
  private final ObjectMapper objectMapper;

  public FinanceMonthLockServiceImpl(
      AuditLogMapper auditLogMapper,
      OaApprovalMapper oaApprovalMapper,
      ObjectMapper objectMapper) {
    this.auditLogMapper = auditLogMapper;
    this.oaApprovalMapper = oaApprovalMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public FinanceMonthLockStatusResponse getMonthLockStatus(Long orgId, YearMonth month) {
    FinanceMonthLockStatusResponse response = new FinanceMonthLockStatusResponse();
    response.setMonth(month.toString());
    AuditLog latest = latestMonthLog(orgId, month);
    if (latest == null) {
      response.setLocked(false);
      response.setCloseStatus("OPEN");
      response.setCloseStatusLabel("未关账");
      return response;
    }
    Map<String, Object> detail = readDetail(latest.getDetail());
    String status = stringValue(detail.get("status")).toUpperCase(Locale.ROOT);
    response.setCloseStatus(status);
    response.setCloseStatusLabel(closeStatusLabel(status));
    if ("UNLOCKED".equals(status)) {
      response.setLocked(false);
      response.setUnlockedBy(latest.getActorName());
      response.setUnlockedAt(latest.getCreateTime());
      response.setUnlockReason(stringValue(detail.get("reason")));
      return response;
    }
    response.setLocked("CLOSED".equals(status) || "FORCED_CLOSED".equals(status));
    response.setLockedBy(latest.getActorName());
    response.setLockedAt(latest.getCreateTime());
    return response;
  }

  @Override
  public void assertMonthEditable(Long orgId, YearMonth month, String actionLabel) {
    FinanceMonthLockStatusResponse status = getMonthLockStatus(orgId, month);
    if (!status.isLocked()) {
      return;
    }
    throw new IllegalStateException(
        "账期 " + month + " 已关账，暂不允许" + normalizeText(actionLabel, "修改")
            + "。如需跨期处理，请先申请跨期审批或由主管反锁账。");
  }

  @Override
  public FinanceMonthLockStatusResponse unlockMonth(Long orgId, FinanceMonthUnlockRequest request) {
    YearMonth month = parseMonth(request == null ? null : request.getMonth());
    Map<String, Object> detail = new LinkedHashMap<>();
    detail.put("month", month.toString());
    detail.put("status", "UNLOCKED");
    detail.put("reason", normalizeText(request == null ? null : request.getReason(), ""));
    detail.put("remark", normalizeText(request == null ? null : request.getRemark(), ""));
    insertAuditLog(orgId, "MONTH_CLOSE_UNLOCK", "FINANCE_MONTH_CLOSE", monthEntityId(month), detail);
    return getMonthLockStatus(orgId, month);
  }

  @Override
  public FinanceCrossPeriodApprovalResponse requestCrossPeriodApproval(Long orgId, FinanceCrossPeriodApprovalRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("跨期审批请求不能为空");
    }
    YearMonth month = parseMonth(request.getMonth());
    String requestKey = crossPeriodKey(month, request.getActionType(), request.getBillId(), request.getPaymentId(), request.getElderId());
    OaApproval existing = oaApprovalMapper.selectList(
            Wrappers.lambdaQuery(OaApproval.class)
                .eq(OaApproval::getIsDeleted, 0)
                .eq(orgId != null, OaApproval::getOrgId, orgId)
                .eq(OaApproval::getApprovalType, "FINANCE_CROSS_PERIOD")
                .orderByDesc(OaApproval::getCreateTime))
        .stream()
        .filter(item -> requestKey.equals(crossPeriodKey(item)))
        .filter(item -> "PENDING".equalsIgnoreCase(item.getStatus()))
        .findFirst()
        .orElse(null);

    FinanceCrossPeriodApprovalResponse response = new FinanceCrossPeriodApprovalResponse();
    response.setMonth(month.toString());
    if (existing != null) {
      response.setApprovalId(existing.getId());
      response.setApprovalStatus(existing.getStatus());
      response.setApprovalStatusLabel(approvalStatusLabel(existing.getStatus()));
      response.setApprovalTitle(existing.getTitle());
      response.setMessage("已存在待处理的跨期审批");
      return response;
    }

    OaApproval approval = new OaApproval();
    approval.setTenantId(orgId);
    approval.setOrgId(orgId);
    approval.setApprovalType("FINANCE_CROSS_PERIOD");
    approval.setTitle("财务跨期处理申请 - " + month);
    approval.setApplicantId(AuthContext.getStaffId());
    approval.setApplicantName(AuthContext.getUsername());
    approval.setAmount(BigDecimal.ZERO);
    approval.setStatus("PENDING");
    approval.setRemark(normalizeText(request.getRemark(), ""));
    Map<String, Object> form = new LinkedHashMap<>();
    form.put("financeSource", "CROSS_PERIOD");
    form.put("month", month.toString());
    form.put("actionType", normalizeText(request.getActionType(), ""));
    form.put("billId", request.getBillId());
    form.put("paymentId", request.getPaymentId());
    form.put("elderId", request.getElderId());
    form.put("reason", normalizeText(request.getReason(), ""));
    form.put("remark", normalizeText(request.getRemark(), ""));
    approval.setFormData(toJson(form));
    approval.setCreatedBy(AuthContext.getStaffId());
    oaApprovalMapper.insert(approval);

    response.setApprovalId(approval.getId());
    response.setApprovalStatus(approval.getStatus());
    response.setApprovalStatusLabel(approvalStatusLabel(approval.getStatus()));
    response.setApprovalTitle(approval.getTitle());
    response.setMessage("已提交跨期审批");
    return response;
  }

  private AuditLog latestMonthLog(Long orgId, YearMonth month) {
    return auditLogMapper.selectOne(
        Wrappers.lambdaQuery(AuditLog.class)
            .eq(AuditLog::getOrgId, orgId)
            .eq(AuditLog::getEntityType, "FINANCE_MONTH_CLOSE")
            .eq(AuditLog::getEntityId, monthEntityId(month))
            .orderByDesc(AuditLog::getCreateTime)
            .last("LIMIT 1"));
  }

  private void insertAuditLog(Long orgId, String actionType, String entityType, Long entityId, Map<String, Object> detail) {
    AuditLog log = new AuditLog();
    log.setTenantId(orgId);
    log.setOrgId(orgId);
    log.setActorId(AuthContext.getStaffId());
    log.setActorName(AuthContext.getUsername());
    log.setActionType(actionType);
    log.setEntityType(entityType);
    log.setEntityId(entityId);
    log.setDetail(toJson(detail));
    auditLogMapper.insert(log);
  }

  private YearMonth parseMonth(String month) {
    return month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
  }

  private Long monthEntityId(YearMonth month) {
    return Long.valueOf(month.toString().replace("-", ""));
  }

  private Map<String, Object> readDetail(String detail) {
    if (detail == null || detail.isBlank()) {
      return Map.of();
    }
    try {
      return objectMapper.readValue(detail, new TypeReference<Map<String, Object>>() {});
    } catch (Exception ex) {
      return Map.of();
    }
  }

  private String toJson(Map<String, Object> detail) {
    try {
      return objectMapper.writeValueAsString(detail);
    } catch (Exception ex) {
      return "{}";
    }
  }

  private String approvalStatusLabel(String status) {
    if ("APPROVED".equalsIgnoreCase(status)) return "已通过";
    if ("REJECTED".equalsIgnoreCase(status)) return "已驳回";
    if ("PENDING".equalsIgnoreCase(status)) return "审批中";
    return "未提交";
  }

  private String closeStatusLabel(String status) {
    if ("CLOSED".equalsIgnoreCase(status)) return "已关账";
    if ("FORCED_CLOSED".equalsIgnoreCase(status)) return "强制关账";
    if ("UNLOCKED".equalsIgnoreCase(status)) return "已反锁";
    return "未关账";
  }

  private String crossPeriodKey(YearMonth month, String actionType, Long billId, Long paymentId, Long elderId) {
    return month + "|" + normalizeText(actionType, "") + "|" + stringValue(billId) + "|" + stringValue(paymentId) + "|" + stringValue(elderId);
  }

  private String crossPeriodKey(OaApproval approval) {
    Map<String, Object> form = readDetail(approval.getFormData());
    return crossPeriodKey(
        parseMonth(stringValue(form.get("month"))),
        stringValue(form.get("actionType")),
        toLong(form.get("billId")),
        toLong(form.get("paymentId")),
        toLong(form.get("elderId")));
  }

  private String normalizeText(String text, String fallback) {
    if (text == null || text.isBlank()) {
      return fallback;
    }
    return text.trim();
  }

  private String stringValue(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private Long toLong(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    try {
      return Long.valueOf(String.valueOf(value));
    } catch (Exception ex) {
      return null;
    }
  }
}
