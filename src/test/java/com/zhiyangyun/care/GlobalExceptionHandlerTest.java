package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zhiyangyun.care.auth.config.GlobalExceptionHandler;
import com.zhiyangyun.care.auth.model.Result;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {
  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void validation_returns_field_message() throws Exception {
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new DummyRequest(), "dummyRequest");
    bindingResult.addError(new FieldError(
        "dummyRequest", "phone", "bad", false, null, null, "手机号格式不正确"));
    MethodParameter parameter =
        new MethodParameter(DummyController.class.getDeclaredMethod("submit", DummyRequest.class), 0);
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

    Result<Void> result = handler.handleValidation(ex);

    assertEquals(400, result.getCode());
    assertEquals("手机号格式不正确", result.getMessage());
  }

  @Test
  void illegal_state_without_cause_returns_bad_request_message() {
    ResponseEntity<Result<Void>> response =
        handler.handleIllegalState(new IllegalStateException("合同尚未完成入住评估，请先完成评估"));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(400, response.getBody().getCode());
    assertEquals("合同尚未完成入住评估，请先完成评估", response.getBody().getMessage());
  }

  @Test
  void illegal_state_with_cause_returns_generic_internal_message() {
    ResponseEntity<Result<Void>> response =
        handler.handleIllegalState(new IllegalStateException("底层 SQL 错误", new RuntimeException("syntax error")));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(500, response.getBody().getCode());
    assertEquals("服务处理失败，请稍后重试", response.getBody().getMessage());
  }

  @Test
  void duplicate_key_returns_friendly_message() {
    ResponseEntity<Result<Void>> response = handler.handleDuplicateKey(
        new DuplicateKeyException("Duplicate entry '1-B3' for key 'building.uk_building_tenant_code'"));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(400, response.getBody().getCode());
    assertEquals("楼栋编码已存在，请调整后重试", response.getBody().getMessage());
  }

  private static final class DummyController {
    private void submit(@Valid DummyRequest request) {
    }
  }

  private static final class DummyRequest {
  }
}
