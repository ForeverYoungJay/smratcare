package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.elder.controller.FamilyController;
import com.zhiyangyun.care.elder.entity.ElderFamily;
import com.zhiyangyun.care.elder.mapper.ElderFamilyMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.model.FamilyBindRequest;
import com.zhiyangyun.care.elder.service.FamilyService;
import com.zhiyangyun.care.family.config.FamilyPortalProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class FamilyLegacyApiSunsetTest {

  @Mock
  private FamilyService familyService;

  private FamilyPortalProperties properties;
  private FamilyController controller;

  @BeforeEach
  void setUp() {
    properties = new FamilyPortalProperties();
    controller = new FamilyController(
        familyService,
        mock(ElderFamilyMapper.class),
        mock(ElderMapper.class),
        properties);
  }

  @Test
  void bindElder_shouldReturnGoneWhenLegacyDisabled() {
    properties.setLegacyApiEnabled(false);
    properties.setLegacyApiSunsetDate("2099-12-31");

    FamilyBindRequest request = new FamilyBindRequest();
    request.setElderId(101L);
    request.setRelation("女儿");

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> controller.bindElder(request, new MockHttpServletResponse()));
    assertEquals(410, ex.getStatusCode().value());
  }

  @Test
  void myElders_shouldReturnGoneWhenSunsetReached() {
    properties.setLegacyApiEnabled(true);
    properties.setLegacyApiSunsetDate("2000-01-01");

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
        () -> controller.myElders(new MockHttpServletResponse()));
    assertEquals(410, ex.getStatusCode().value());
  }

  @Test
  void bindElder_shouldMarkDeprecationHeadersBeforeSunset() {
    properties.setLegacyApiEnabled(true);
    properties.setLegacyApiSunsetDate("2099-12-31");

    ElderFamily relation = new ElderFamily();
    relation.setId(900L);
    relation.setElderId(101L);
    relation.setFamilyUserId(200L);
    relation.setRelation("女儿");
    when(familyService.bindElder(any())).thenReturn(relation);

    FamilyBindRequest request = new FamilyBindRequest();
    request.setElderId(101L);
    request.setRelation("女儿");

    MockHttpServletResponse response = new MockHttpServletResponse();
    Result<ElderFamily> result = controller.bindElder(request, response);

    assertNotNull(result);
    assertEquals(0, result.getCode());
    assertEquals("true", response.getHeader("Deprecation"));
    assertEquals("2099-12-31", response.getHeader("Sunset"));
    assertNotNull(response.getHeader("Link"));
    assertEquals("Use family aggregation APIs", response.getHeader("X-Deprecated-Reason"));
  }
}
