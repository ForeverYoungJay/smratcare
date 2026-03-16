package com.zhiyangyun.care.auth.config;

import com.zhiyangyun.care.auth.model.Result;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public Result<Void> handleBadCredentials(BadCredentialsException ex) {
    log.warn("Bad credentials: {}", ex.getMessage());
    return Result.error(401, defaultText(ex.getMessage(), "用户名或密码错误"));
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public Result<Void> handleAccessDenied(AccessDeniedException ex) {
    log.warn("Access denied: {}", ex.getMessage());
    return Result.error(403, "无权限访问该资源");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleValidation(MethodArgumentNotValidException ex) {
    log.warn("Validation error: {}", ex.getMessage());
    return Result.error(400, resolveValidationMessage(ex));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleNotReadable(HttpMessageNotReadableException ex) {
    log.warn("Bad request body: {}", ex.getMessage());
    return Result.error(400, "请求体格式不正确");
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleConstraint(ConstraintViolationException ex) {
    log.warn("Constraint violation: {}", ex.getMessage());
    return Result.error(400, resolveConstraintMessage(ex));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Bad request: {}", ex.getMessage());
    return Result.error(400, defaultText(ex.getMessage(), "请求参数不正确"));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Result<Void>> handleIllegalState(IllegalStateException ex) {
    if (ex.getCause() != null) {
      log.error("Unhandled state exception", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Result.error(500, defaultText(ex.getMessage(), "Server error")));
    }
    log.warn("Business state error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Result.error(400, defaultText(ex.getMessage(), "当前状态不允许执行该操作")));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Result<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    log.warn("Data integrity violation: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Result.error(400, resolveDataIntegrityMessage(ex)));
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Result<Void> handleOther(Exception ex) {
    log.error("Unhandled exception", ex);
    return Result.error(500, "Server error");
  }

  private String resolveValidationMessage(MethodArgumentNotValidException ex) {
    Set<String> messages = new LinkedHashSet<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      messages.add(defaultText(error.getDefaultMessage(), error.getField() + " 参数不正确"));
    }
    for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
      messages.add(defaultText(error.getDefaultMessage(), "请求参数不正确"));
    }
    return messages.isEmpty() ? "请求参数不正确" : String.join("；", messages);
  }

  private String resolveConstraintMessage(ConstraintViolationException ex) {
    return ex.getConstraintViolations().stream()
        .map(violation -> defaultText(violation.getMessage(), "请求参数不正确"))
        .findFirst()
        .orElse("请求参数不正确");
  }

  private String resolveDataIntegrityMessage(DataIntegrityViolationException ex) {
    String raw = defaultText(ex.getMostSpecificCause() == null ? null : ex.getMostSpecificCause().getMessage(),
        ex.getMessage());
    String normalized = raw.toLowerCase(Locale.ROOT);
    if (normalized.contains("uk_building_tenant_code")) {
      return "楼栋编码已存在，请调整后重试";
    }
    if (normalized.contains("uk_building_tenant_name")) {
      return "楼栋名称已存在，请调整后重试";
    }
    if (normalized.contains("uk_room_tenant_room_no")) {
      return "房间号已存在，请调整后重试";
    }
    if (normalized.contains("duplicate entry")) {
      return "数据已存在，请勿重复提交";
    }
    return "数据保存失败，请检查是否存在重复或无效数据";
  }

  private String defaultText(String value, String fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return value.trim();
  }
}
