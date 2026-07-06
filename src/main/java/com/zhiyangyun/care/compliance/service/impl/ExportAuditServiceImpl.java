package com.zhiyangyun.care.compliance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.common.web.RequestTraceContext;
import com.zhiyangyun.care.compliance.entity.ExportLog;
import com.zhiyangyun.care.compliance.mapper.ExportLogMapper;
import com.zhiyangyun.care.compliance.model.ExportConfirmRequest;
import com.zhiyangyun.care.compliance.model.ExportTicketResponse;
import com.zhiyangyun.care.compliance.service.ExportAuditService;
import com.zhiyangyun.care.compliance.service.SensitiveAccessService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class ExportAuditServiceImpl implements ExportAuditService {

  /** 一次性导出凭证有效期（分钟）。 */
  private static final int TICKET_TTL_MINUTES = 10;

  private final ExportLogMapper mapper;
  private final SensitiveAccessService sensitiveAccessService;

  public ExportAuditServiceImpl(ExportLogMapper mapper, SensitiveAccessService sensitiveAccessService) {
    this.mapper = mapper;
    this.sensitiveAccessService = sensitiveAccessService;
  }

  @Override
  public ExportTicketResponse confirm(ExportConfirmRequest request) {
    Long orgId = AuthContext.getOrgId();
    if (orgId == null) {
      throw new IllegalArgumentException("无法识别当前机构，禁止导出");
    }
    if (request == null || request.getModule() == null || request.getModule().isBlank()) {
      throw new IllegalArgumentException("导出模块不能为空");
    }
    if (request.getPurpose() == null || request.getPurpose().isBlank()) {
      throw new IllegalArgumentException("请填写导出用途");
    }
    LocalDateTime now = LocalDateTime.now();
    ExportLog record = new ExportLog();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    record.setActorId(AuthContext.getStaffId());
    record.setActorName(AuthContext.getUsername());
    record.setActorRole(String.join(",", AuthContext.getRoleCodes()));
    record.setModule(normalizeModule(request.getModule()));
    record.setScope(truncate(request.getScope(), 500));
    record.setPurpose(truncate(request.getPurpose().trim(), 255));
    record.setExportTicket("EXP-" + UUID.randomUUID());
    record.setStatus("PENDING");
    record.setExpiresAt(now.plusMinutes(TICKET_TTL_MINUTES));
    HttpServletRequest httpRequest = currentRequest();
    if (httpRequest != null) {
      record.setIp(resolveIp(httpRequest));
      record.setUserAgent(truncate(httpRequest.getHeader("User-Agent"), 255));
    }
    record.setRequestId(RequestTraceContext.getRequestId());
    record.setIsDeleted(0);
    mapper.insert(record);

    ExportTicketResponse response = new ExportTicketResponse();
    response.setExportTicket(record.getExportTicket());
    response.setExpiresAt(record.getExpiresAt());
    return response;
  }

  @Override
  public void complete(String exportTicket, Long rowCount) {
    if (exportTicket == null || exportTicket.isBlank()) {
      throw new IllegalArgumentException("导出凭证不能为空");
    }
    ExportLog record = mapper.selectOne(Wrappers.lambdaQuery(ExportLog.class)
        .eq(ExportLog::getExportTicket, exportTicket.trim())
        .eq(ExportLog::getIsDeleted, 0)
        .last("LIMIT 1"));
    if (record == null) {
      throw new IllegalArgumentException("导出凭证不存在，请重新确认导出");
    }
    Long staffId = AuthContext.getStaffId();
    if (record.getActorId() != null && !Objects.equals(record.getActorId(), staffId)) {
      throw new IllegalArgumentException("导出凭证仅限申请人本人使用");
    }
    if (!"PENDING".equalsIgnoreCase(record.getStatus())) {
      throw new IllegalArgumentException("导出凭证已使用或失效，请重新确认导出");
    }
    LocalDateTime now = LocalDateTime.now();
    if (record.getExpiresAt() != null && record.getExpiresAt().isBefore(now)) {
      record.setStatus("EXPIRED");
      mapper.updateById(record);
      throw new IllegalArgumentException("导出凭证已过期，请重新确认导出");
    }
    record.setStatus("USED");
    record.setUsedAt(now);
    if (rowCount != null && rowCount >= 0) {
      record.setRowCount(rowCount > Integer.MAX_VALUE ? Integer.MAX_VALUE : rowCount.intValue());
    }
    mapper.updateById(record);
    // 同步记入敏感访问审计（EXPORT），保持"谁导出了什么"一处可查
    sensitiveAccessService.record("EXPORT", record.getModule(), "EXPORT_LOG", record.getId(),
        record.getModule(), null, record.getPurpose(), true);
  }

  @Override
  public IPage<ExportLog> page(Long orgId, String module, String status, LocalDateTime start, LocalDateTime end,
                               long pageNo, long pageSize) {
    var wrapper = Wrappers.lambdaQuery(ExportLog.class)
        .eq(ExportLog::getIsDeleted, 0)
        .eq(orgId != null, ExportLog::getOrgId, orgId)
        .eq(module != null && !module.isBlank(), ExportLog::getModule, module)
        .eq(status != null && !status.isBlank(), ExportLog::getStatus, status)
        .ge(start != null, ExportLog::getCreateTime, start)
        .le(end != null, ExportLog::getCreateTime, end)
        .orderByDesc(ExportLog::getCreateTime);
    return mapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  private static String normalizeModule(String module) {
    String value = module.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_\\-]", "");
    if (value.isEmpty()) {
      throw new IllegalArgumentException("导出模块格式不正确");
    }
    return value.length() > 64 ? value.substring(0, 64) : value;
  }

  private static String truncate(String value, int maxLength) {
    if (value == null) {
      return null;
    }
    return value.length() > maxLength ? value.substring(0, maxLength) : value;
  }

  private static HttpServletRequest currentRequest() {
    var attrs = RequestContextHolder.getRequestAttributes();
    if (attrs instanceof ServletRequestAttributes servletAttrs) {
      return servletAttrs.getRequest();
    }
    return null;
  }

  private static String resolveIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
