package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhiyangyun.care.elder.controller.adm.ElderAdmissionProgressController;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderAdmissionProgress;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

/** 入住办理进度：每位长者只保留一条，退回补充必须写原因，恢复办理时清掉原因。 */
@SpringBootTest
@ActiveProfiles("test")
class ElderAdmissionProgressControllerTest {
  private static final Long ORG_ID = 9861L;

  @Autowired
  private ElderAdmissionProgressController controller;

  @Autowired
  private ElderMapper elderMapper;

  @BeforeEach
  void mockAuth() {
    var auth = new UsernamePasswordAuthenticationToken(
        "9861", "N/A", List.of(new SimpleGrantedAuthority("ROLE_NURSING_MINISTER")));
    auth.setDetails(Map.of("orgId", ORG_ID, "username", "progress-tester"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void clearAuth() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void upserts_one_row_per_elder_and_tracks_returned_state() {
    ElderProfile elder = new ElderProfile();
    elder.setTenantId(ORG_ID);
    elder.setOrgId(ORG_ID);
    elder.setFullName("进度测试长者");
    elderMapper.insert(elder);

    ElderAdmissionProgressController.ProgressRequest request = new ElderAdmissionProgressController.ProgressRequest();
    request.setElderId(elder.getId());
    request.setCurrentStep(2);
    request.setDraftJson("{\"depositAmount\":3000}");
    controller.save(request);

    request.setStatus("returned");
    assertThrows(IllegalArgumentException.class, () -> controller.save(request));

    request.setReturnReason("缺身份证复印件");
    request.setCurrentStep(1);
    controller.save(request);

    List<ElderAdmissionProgress> returned = controller.list(null, "RETURNED").getData();
    assertEquals(1, returned.size());
    assertEquals(1, returned.get(0).getCurrentStep());
    assertEquals("缺身份证复印件", returned.get(0).getReturnReason());
    assertEquals("{\"depositAmount\":3000}", returned.get(0).getDraftJson());

    request.setStatus(null);
    controller.save(request);
    List<ElderAdmissionProgress> rows = controller.list(elder.getId(), null).getData();
    assertEquals(1, rows.size());
    assertEquals("DRAFT", rows.get(0).getStatus());
    assertNull(rows.get(0).getReturnReason());
  }
}
