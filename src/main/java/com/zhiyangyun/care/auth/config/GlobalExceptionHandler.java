package com.zhiyangyun.care.auth.config;

import com.zhiyangyun.care.auth.model.Result;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BadCredentialsException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public Result<Void> handleBadCredentials(BadCredentialsException ex) {
    log.warn("Bad credentials");
    return Result.error(401, "用户名或密码错误");
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

  @ExceptionHandler(BindException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleBindException(BindException ex) {
    log.warn("Bind validation error: {}", ex.getMessage());
    return Result.error(400, resolveBindingMessage(ex));
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

  @ExceptionHandler({
      TypeMismatchException.class,
      MissingServletRequestParameterException.class,
      MissingServletRequestPartException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleBadRequestParameter(Exception ex) {
    log.warn("Bad request parameter: {}", ex.getMessage());
    return Result.error(400, "请求参数不正确");
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result<Void> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
    log.warn("Upload exceeded max size: {}", ex.getMessage());
    return Result.error(400, "上传文件大小超过限制");
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
          .body(Result.error(500, "服务处理失败，请稍后重试"));
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

  @ExceptionHandler(DuplicateKeyException.class)
  public ResponseEntity<Result<Void>> handleDuplicateKey(DuplicateKeyException ex) {
    log.warn("Duplicate key violation: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Result.error(400, resolveDataIntegrityMessage(ex)));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Result<Void>> handleNoResourceFound(NoResourceFoundException ex) {
    log.warn("Resource not found: {}", ex.getResourcePath());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(Result.error(404, "请求资源不存在"));
  }

  @ExceptionHandler(AsyncRequestNotUsableException.class)
  public void handleAsyncRequestNotUsable(AsyncRequestNotUsableException ex) {
    log.warn("Client disconnected before response completed: {}", ex.getMessage());
  }

  @ExceptionHandler(IOException.class)
  public void handleIoException(IOException ex) throws IOException {
    if (isClientAbort(ex)) {
      log.warn("Client connection aborted: {}", ex.getMessage());
      return;
    }
    throw ex;
  }

  @ExceptionHandler(BadSqlGrammarException.class)
  public ResponseEntity<Result<Void>> handleBadSqlGrammar(BadSqlGrammarException ex) {
    log.error("Database schema not ready", ex);
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(Result.error(503, "数据库结构未完成升级，请先执行最新迁移"));
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Result<Void> handleOther(Exception ex) {
    log.error("Unhandled exception", ex);
    return Result.error(500, "服务异常，请稍后重试");
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

  private String resolveBindingMessage(BindException ex) {
    Set<String> messages = new LinkedHashSet<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      messages.add(defaultText(error.getDefaultMessage(), error.getField() + " 参数不正确"));
    }
    for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
      messages.add(defaultText(error.getDefaultMessage(), "请求参数不正确"));
    }
    return messages.isEmpty() ? "请求参数不正确" : String.join("；", messages);
  }

  private String resolveDataIntegrityMessage(Throwable ex) {
    Throwable current = ex;
    String raw = null;
    while (current != null) {
      if (current.getMessage() != null && !current.getMessage().isBlank()) {
        raw = current.getMessage();
        break;
      }
      current = current.getCause();
    }
    raw = defaultText(raw, ex == null ? null : ex.getMessage());
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
    if (normalized.contains("uk_room_org_room_no")) {
      return "房间号已存在，请调整后重试";
    }
    if (normalized.contains("uk_staff_org_username")) {
      return "账号或手机号已存在，请调整后重试";
    }
    if (normalized.contains("uk_family_user_org_phone")) {
      return "手机号已存在，请调整后重试";
    }
    if (normalized.contains("duplicate entry")) {
      return "数据已存在，请勿重复提交";
    }
    return "数据保存失败，请检查是否存在重复或无效数据";
  }

  private boolean isClientAbort(Throwable ex) {
    Throwable current = ex;
    while (current != null) {
      String name = current.getClass().getName();
      String message = current.getMessage();
      if (name.contains("ClientAbortException")) {
        return true;
      }
      if (message != null && message.toLowerCase(Locale.ROOT).contains("broken pipe")) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }

  private String defaultText(String value, String fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return value.trim();
  }
}
