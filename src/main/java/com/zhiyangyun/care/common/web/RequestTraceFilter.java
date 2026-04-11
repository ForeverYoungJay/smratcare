package com.zhiyangyun.care.common.web;

import com.zhiyangyun.care.auth.security.AuthContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestTraceFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(RequestTraceFilter.class);
  private static final String TRACE_HEADER = "X-Request-Id";
  private static final String TRACE_MDC_KEY = "requestId";

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return uri == null || !uri.startsWith("/api/");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String requestId = resolveRequestId(request);
    long start = System.currentTimeMillis();
    RequestTraceContext.setRequestId(requestId);
    MDC.put(TRACE_MDC_KEY, requestId);
    response.setHeader(TRACE_HEADER, requestId);
    try {
      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - start;
      logRequest(request, response, duration, requestId);
      MDC.remove(TRACE_MDC_KEY);
      RequestTraceContext.clear();
    }
  }

  private void logRequest(HttpServletRequest request, HttpServletResponse response, long duration, String requestId) {
    int status = response.getStatus();
    Long staffId = AuthContext.getStaffId();
    Long orgId = AuthContext.getOrgId();
    String username = AuthContext.getUsername();
    if (status >= 500) {
      log.error("request failed requestId={} method={} path={} status={} durationMs={} staffId={} orgId={} username={}",
          requestId, request.getMethod(), request.getRequestURI(), status, duration, staffId, orgId, username);
      return;
    }
    if (status >= 400) {
      log.warn("request completed requestId={} method={} path={} status={} durationMs={} staffId={} orgId={} username={}",
          requestId, request.getMethod(), request.getRequestURI(), status, duration, staffId, orgId, username);
      return;
    }
    log.info("request completed requestId={} method={} path={} status={} durationMs={} staffId={} orgId={} username={}",
        requestId, request.getMethod(), request.getRequestURI(), status, duration, staffId, orgId, username);
  }

  private String resolveRequestId(HttpServletRequest request) {
    String incoming = request.getHeader(TRACE_HEADER);
    if (StringUtils.hasText(incoming)) {
      String normalized = incoming.trim();
      if (normalized.length() <= 64) {
        return normalized;
      }
    }
    return UUID.randomUUID().toString().replace("-", "");
  }
}
