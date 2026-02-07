package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zhiyangyun.care.store.model.admin.ForbiddenTagsResponse;
import com.zhiyangyun.care.store.model.OrderPreviewRequest;
import com.zhiyangyun.care.store.model.OrderStatus;
import com.zhiyangyun.care.store.service.DiseaseRuleService;
import com.zhiyangyun.care.store.service.StoreOrderService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DiseaseRuleServiceTest {
  @Autowired
  private DiseaseRuleService diseaseRuleService;

  @Autowired
  private StoreOrderService storeOrderService;

  @Test
  void forbidden_tags_save_and_load() {
    diseaseRuleService.saveForbiddenTags(1L, 1001L, List.of(2001L));
    ForbiddenTagsResponse response = diseaseRuleService.getForbiddenTags(1L, 1001L);
    assertNotNull(response);
    assertTrue(response.getSelectedTagIds().contains(2001L));
  }

  @Test
  void rule_effects_preview_immediately() {
    diseaseRuleService.saveForbiddenTags(1L, 1001L, List.of(2001L));

    var request = new OrderPreviewRequest();
    request.setElderId(4001L);
    request.setProductId(6001L);
    request.setQty(1);

    var preview = storeOrderService.preview(request);
    assertEquals(OrderStatus.BLOCKED.name(), preview.getStatus());
    assertFalse(preview.isAllowed());
  }

  @Test
  void elder_disease_update_and_read() {
    diseaseRuleService.saveElderDiseases(1L, 4003L, List.of(1002L));
    List<Long> ids = diseaseRuleService.getElderDiseaseIds(1L, 4003L);
    assertTrue(ids.contains(1002L));
  }
}
