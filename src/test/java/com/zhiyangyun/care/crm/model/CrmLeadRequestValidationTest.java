package com.zhiyangyun.care.crm.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class CrmLeadRequestValidationTest {
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void rejectsLeadWithoutAnyName() {
    CrmLeadRequest request = new CrmLeadRequest();

    assertEquals("请填写线索姓名、咨询人姓名或老人姓名",
        validator.validate(request).iterator().next().getMessage());
  }

  @Test
  void acceptsAnySupportedNameField() {
    CrmLeadRequest request = new CrmLeadRequest();
    request.setElderName("张阿姨");

    assertTrue(validator.validate(request).isEmpty());
  }
}
