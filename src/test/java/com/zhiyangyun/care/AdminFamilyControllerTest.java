package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.elder.controller.AdminFamilyController;
import com.zhiyangyun.care.elder.entity.ElderFamily;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.mapper.ElderFamilyMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.FamilyUserMapper;
import com.zhiyangyun.care.elder.model.FamilyBindRequest;
import com.zhiyangyun.care.elder.model.FamilyRelationItem;
import com.zhiyangyun.care.elder.service.FamilyService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AdminFamilyControllerTest {

  @Mock
  private FamilyUserMapper familyUserMapper;

  @Mock
  private ElderFamilyMapper elderFamilyMapper;

  @Mock
  private FamilyService familyService;

  private AdminFamilyController controller;

  @BeforeEach
  void setUp() {
    controller = new AdminFamilyController(
        familyUserMapper,
        elderFamilyMapper,
        mock(ElderMapper.class),
        familyService);

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            "9001",
            "N/A",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    Map<String, Object> details = new HashMap<>();
    details.put("orgId", 1L);
    details.put("username", "admin");
    authentication.setDetails(details);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void bindRelation_shouldUseAdminRelationBindApi() {
    FamilyUser familyUser = new FamilyUser();
    familyUser.setId(200L);
    familyUser.setOrgId(1L);
    familyUser.setRealName("李家属");
    familyUser.setPhone("13800138000");
    when(familyUserMapper.selectOne(any())).thenReturn(familyUser);

    ElderFamily relation = new ElderFamily();
    relation.setId(901L);
    relation.setFamilyUserId(200L);
    relation.setElderId(101L);
    relation.setRelation("女儿");
    relation.setIsPrimary(1);
    when(familyService.bindElder(any())).thenReturn(relation);

    FamilyBindRequest request = new FamilyBindRequest();
    request.setFamilyUserId(200L);
    request.setElderId(101L);
    request.setRelation("女儿");
    request.setIsPrimary(1);

    Result<FamilyRelationItem> result = controller.bindRelation(request);

    assertNotNull(result);
    assertEquals(0, result.getCode());
    assertNotNull(result.getData());
    assertEquals(901L, result.getData().getId());
    assertEquals("李家属", result.getData().getRealName());

    ArgumentCaptor<FamilyBindRequest> captor = ArgumentCaptor.forClass(FamilyBindRequest.class);
    verify(familyService).bindElder(captor.capture());
    assertEquals(1L, captor.getValue().getOrgId());
  }

  @Test
  void bindRelation_shouldRejectWhenFamilyUserMissing() {
    when(familyUserMapper.selectOne(any())).thenReturn(null);

    FamilyBindRequest request = new FamilyBindRequest();
    request.setFamilyUserId(9999L);
    request.setElderId(101L);
    request.setRelation("家属");

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> controller.bindRelation(request));
    assertEquals("家属信息不存在或已失效", ex.getMessage());
  }

  @Test
  void removeRelation_shouldSoftDeleteRelation() {
    ElderFamily relation = new ElderFamily();
    relation.setId(777L);
    relation.setOrgId(1L);
    relation.setIsDeleted(0);
    when(elderFamilyMapper.selectById(777L)).thenReturn(relation);

    controller.removeRelation(777L);

    verify(elderFamilyMapper).updateById(relation);
    assertEquals(1, relation.getIsDeleted());
  }
}
