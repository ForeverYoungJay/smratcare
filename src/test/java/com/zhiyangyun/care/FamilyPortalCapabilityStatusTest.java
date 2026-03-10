package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.auth.mapper.OrgMapper;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.bill.service.BillService;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderFamilyMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.FamilyUserMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderMedicalOutingRecordMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderOutingRecordMapper;
import com.zhiyangyun.care.family.config.FamilyPortalProperties;
import com.zhiyangyun.care.family.entity.FamilyPortalState;
import com.zhiyangyun.care.family.mapper.FamilyPortalStateMapper;
import com.zhiyangyun.care.family.mapper.FamilyRechargeOrderMapper;
import com.zhiyangyun.care.family.model.FamilyPortalModels;
import com.zhiyangyun.care.family.service.FamilyWechatNotifyService;
import com.zhiyangyun.care.family.service.impl.FamilyPortalServiceImpl;
import com.zhiyangyun.care.finance.mapper.ElderAccountLogMapper;
import com.zhiyangyun.care.finance.mapper.ElderAccountMapper;
import com.zhiyangyun.care.health.mapper.HealthDataRecordMapper;
import com.zhiyangyun.care.health.mapper.HealthNursingLogMapper;
import com.zhiyangyun.care.life.mapper.MealPlanMapper;
import com.zhiyangyun.care.nursing.mapper.ServiceBookingMapper;
import com.zhiyangyun.care.oa.mapper.OaAlbumMapper;
import com.zhiyangyun.care.oa.mapper.OaNoticeMapper;
import com.zhiyangyun.care.oa.mapper.OaSuggestionMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.service.CareTaskService;
import com.zhiyangyun.care.standard.mapper.ServiceItemMapper;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.OrderItemMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import com.zhiyangyun.care.store.mapper.StoreOrderMapper;
import com.zhiyangyun.care.store.service.StoreOrderService;
import com.zhiyangyun.care.visit.service.VisitService;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class FamilyPortalCapabilityStatusTest {

  @Mock
  private FamilyUserMapper familyUserMapper;

  @Mock
  private FamilyPortalStateMapper familyPortalStateMapper;

  @Mock
  private FamilyWechatNotifyService familyWechatNotifyService;

  private FamilyPortalProperties properties;
  private FamilyPortalServiceImpl service;

  @BeforeEach
  void setUp() {
    properties = new FamilyPortalProperties();
    properties.setLegacyApiEnabled(true);
    properties.setLegacyApiSunsetDate("2099-12-31");
    properties.getSmsCode().setEnabled(true);
    properties.getSmsCode().setProvider("aliyun");
    properties.getSmsCode().setDebugReturnCode(false);
    properties.getWechatPay().setEnabled(true);
    properties.getWechatNotify().setEnabled(true);

    service = new FamilyPortalServiceImpl(
        mock(ElderFamilyMapper.class),
        mock(ElderMapper.class),
        familyUserMapper,
        mock(BedMapper.class),
        mock(RoomMapper.class),
        mock(ElderOutingRecordMapper.class),
        mock(ElderMedicalOutingRecordMapper.class),
        mock(HealthDataRecordMapper.class),
        mock(HealthNursingLogMapper.class),
        mock(MealPlanMapper.class),
        mock(OaNoticeMapper.class),
        mock(OaAlbumMapper.class),
        mock(OaSuggestionMapper.class),
        mock(OaTodoMapper.class),
        mock(AssessmentRecordMapper.class),
        mock(BillMonthlyMapper.class),
        mock(BillService.class),
        mock(ElderAccountMapper.class),
        mock(ElderAccountLogMapper.class),
        mock(ServiceItemMapper.class),
        mock(ServiceBookingMapper.class),
        mock(ProductMapper.class),
        mock(InventoryBatchMapper.class),
        mock(StoreOrderMapper.class),
        mock(OrderItemMapper.class),
        mock(StoreOrderService.class),
        mock(VisitService.class),
        mock(CareTaskService.class),
        mock(OrgMapper.class),
        familyPortalStateMapper,
        mock(FamilyRechargeOrderMapper.class),
        properties,
        familyWechatNotifyService,
        mock(PasswordEncoder.class),
        new ObjectMapper());
  }

  @Test
  void capabilityStatus_shouldReturnReadyWhenConfigured() {
    FamilyUser familyUser = new FamilyUser();
    familyUser.setId(2001L);
    familyUser.setOrgId(1L);
    familyUser.setOpenId("oOpenIdReady");
    when(familyUserMapper.selectOne(any())).thenReturn(familyUser);

    FamilyPortalState securityState = new FamilyPortalState();
    securityState.setValueJson("{\"passwordSalt\":\"salt\",\"passwordHash\":\"hash\",\"verifyWithPassword\":true}");
    when(familyPortalStateMapper.selectOne(any())).thenReturn(securityState);

    FamilyPortalModels.CapabilityStatusResponse response = service.getCapabilityStatus(1L, 2001L);

    assertNotNull(response);
    assertTrue(Boolean.TRUE.equals(response.getSmsEnabled()));
    assertEquals("aliyun", response.getSmsProvider());
    assertTrue(Boolean.TRUE.equals(response.getWechatPayEnabled()));
    assertTrue(Boolean.TRUE.equals(response.getWechatNotifyEnabled()));
    assertTrue(Boolean.TRUE.equals(response.getWechatNotifyBound()));
    assertTrue(Boolean.TRUE.equals(response.getSecurityPasswordEnabled()));

    assertEquals("READY", statusOf(response, "SMS_CODE"));
    assertEquals("READY", statusOf(response, "WECHAT_NOTIFY"));
    assertEquals("READY", statusOf(response, "WECHAT_PAY"));
    assertEquals("READY", statusOf(response, "SECURITY_PASSWORD"));
    assertEquals("DEPRECATED", statusOf(response, "LEGACY_API"));
  }

  @Test
  void capabilityStatus_shouldMarkOfflineWhenLegacyDisabled() {
    properties.setLegacyApiEnabled(false);
    properties.getSmsCode().setProvider("mock");
    properties.getWechatPay().setEnabled(false);
    properties.getWechatNotify().setEnabled(true);

    FamilyUser familyUser = new FamilyUser();
    familyUser.setId(2002L);
    familyUser.setOrgId(1L);
    familyUser.setOpenId("");
    when(familyUserMapper.selectOne(any())).thenReturn(familyUser);
    when(familyPortalStateMapper.selectOne(any())).thenReturn(null);

    FamilyPortalModels.CapabilityStatusResponse response = service.getCapabilityStatus(1L, 2002L);

    assertEquals("MOCK", statusOf(response, "SMS_CODE"));
    assertEquals("BIND_REQUIRED", statusOf(response, "WECHAT_NOTIFY"));
    assertEquals("OFF", statusOf(response, "WECHAT_PAY"));
    assertEquals("OPTIONAL", statusOf(response, "SECURITY_PASSWORD"));
    assertEquals("OFFLINE", statusOf(response, "LEGACY_API"));
    assertTrue(Boolean.FALSE.equals(response.getLegacyApiEnabled()));
  }

  private String statusOf(FamilyPortalModels.CapabilityStatusResponse response, String code) {
    return response.getItems().stream()
        .filter(item -> Objects.equals(code, item.getCode()))
        .map(FamilyPortalModels.CapabilityStatusItem::getStatus)
        .findFirst()
        .orElse("");
  }
}
