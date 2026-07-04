package com.zhiyangyun.care.compliance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.common.web.RequestTraceContext;
import com.zhiyangyun.care.compliance.entity.SensitiveAccessLog;
import com.zhiyangyun.care.compliance.mapper.SensitiveAccessLogMapper;
import com.zhiyangyun.care.compliance.model.SensitiveAccessSummary;
import com.zhiyangyun.care.compliance.service.SensitiveAccessService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class SensitiveAccessServiceImpl implements SensitiveAccessService {
  private static final Logger log = LoggerFactory.getLogger(SensitiveAccessServiceImpl.class);

  private final SensitiveAccessLogMapper mapper;

  public SensitiveAccessServiceImpl(SensitiveAccessLogMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void record(String action, String dataCategory, String targetType, Long targetId,
                     String targetName, String fields, String purpose, boolean success) {
    try {
      Long orgId = AuthContext.getOrgId();
      if (orgId == null || action == null || dataCategory == null) {
        return;
      }
      SensitiveAccessLog record = new SensitiveAccessLog();
      record.setOrgId(orgId);
      record.setActorId(AuthContext.getStaffId());
      record.setActorName(AuthContext.getUsername());
      record.setActorRole(String.join(",", AuthContext.getRoleCodes()));
      record.setAction(action);
      record.setDataCategory(dataCategory);
      record.setTargetType(targetType);
      record.setTargetId(targetId);
      record.setTargetName(targetName);
      record.setFields(fields);
      record.setPurpose(purpose);
      record.setResult(success ? "SUCCESS" : "DENIED");
      record.setRequestId(RequestTraceContext.getRequestId());
      HttpServletRequest request = currentRequest();
      if (request != null) {
        record.setIp(resolveIp(request));
        String ua = request.getHeader("User-Agent");
        record.setUserAgent(ua != null && ua.length() > 255 ? ua.substring(0, 255) : ua);
      }
      record.setIsDeleted(0);
      mapper.insert(record);
    } catch (Exception ex) {
      // 审计留痕不得影响主业务流程
      log.warn("[Compliance] 敏感访问留痕失败: {}", ex.getMessage());
    }
  }

  @Override
  public IPage<SensitiveAccessLog> page(Long orgId, Long actorId, String dataCategory, String action,
                                        LocalDateTime start, LocalDateTime end, long pageNo, long pageSize) {
    var wrapper = Wrappers.lambdaQuery(SensitiveAccessLog.class)
        .eq(SensitiveAccessLog::getIsDeleted, 0)
        .eq(orgId != null, SensitiveAccessLog::getOrgId, orgId)
        .eq(actorId != null, SensitiveAccessLog::getActorId, actorId)
        .eq(dataCategory != null && !dataCategory.isBlank(), SensitiveAccessLog::getDataCategory, dataCategory)
        .eq(action != null && !action.isBlank(), SensitiveAccessLog::getAction, action)
        .ge(start != null, SensitiveAccessLog::getCreateTime, start)
        .le(end != null, SensitiveAccessLog::getCreateTime, end)
        .orderByDesc(SensitiveAccessLog::getCreateTime);
    return mapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
  }

  @Override
  public SensitiveAccessSummary summary(Long orgId, int days) {
    int window = days <= 0 ? 30 : Math.min(days, 365);
    LocalDateTime start = LocalDateTime.now().minusDays(window);
    List<SensitiveAccessLog> logs = mapper.selectList(Wrappers.lambdaQuery(SensitiveAccessLog.class)
        .eq(SensitiveAccessLog::getIsDeleted, 0)
        .eq(orgId != null, SensitiveAccessLog::getOrgId, orgId)
        .ge(SensitiveAccessLog::getCreateTime, start));

    SensitiveAccessSummary summary = new SensitiveAccessSummary();
    summary.setDays(window);
    summary.setTotalAccess(logs.size());
    summary.setExportCount(logs.stream().filter(l -> "EXPORT".equalsIgnoreCase(l.getAction())).count());
    summary.setDeniedCount(logs.stream().filter(l -> "DENIED".equalsIgnoreCase(l.getResult())).count());
    summary.setDistinctActors(logs.stream().map(SensitiveAccessLog::getActorId).filter(java.util.Objects::nonNull).distinct().count());
    summary.setDistinctTargets(logs.stream()
        .filter(l -> l.getTargetType() != null && l.getTargetId() != null)
        .map(l -> l.getTargetType() + ":" + l.getTargetId()).distinct().count());

    summary.setByCategory(logs.stream().collect(Collectors.groupingBy(
        l -> l.getDataCategory() == null ? "UNKNOWN" : l.getDataCategory(),
        LinkedHashMap::new, Collectors.counting())));
    summary.setByAction(logs.stream().collect(Collectors.groupingBy(
        l -> l.getAction() == null ? "UNKNOWN" : l.getAction(),
        LinkedHashMap::new, Collectors.counting())));

    Map<Long, Long> actorCounts = logs.stream()
        .filter(l -> l.getActorId() != null)
        .collect(Collectors.groupingBy(SensitiveAccessLog::getActorId, Collectors.counting()));
    Map<Long, String> actorNames = logs.stream()
        .filter(l -> l.getActorId() != null)
        .collect(Collectors.toMap(SensitiveAccessLog::getActorId, l -> l.getActorName() == null ? "" : l.getActorName(),
            (a, b) -> a));
    summary.setTopActors(actorCounts.entrySet().stream()
        .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
        .limit(10)
        .map(e -> {
          SensitiveAccessSummary.ActorAccessCount item = new SensitiveAccessSummary.ActorAccessCount();
          item.setActorId(e.getKey());
          item.setActorName(actorNames.get(e.getKey()));
          item.setCount(e.getValue());
          return item;
        })
        .collect(Collectors.toList()));
    return summary;
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
