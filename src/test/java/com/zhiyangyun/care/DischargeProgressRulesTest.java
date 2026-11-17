package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischargeApply;
import com.zhiyangyun.care.elder.model.lifecycle.DischargeProgressRules;
import com.zhiyangyun.care.elder.model.lifecycle.ResidenceLifecycleConstants;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DischargeProgressRulesTest {
  private final LocalDateTime now = LocalDateTime.of(2026, 11, 16, 10, 0);

  private ElderDischargeApply approved() {
    ElderDischargeApply apply = new ElderDischargeApply();
    apply.setStatus(ResidenceLifecycleConstants.DISCHARGE_APPLY_APPROVED);
    apply.setAutoDischargeStatus(ResidenceLifecycleConstants.AUTO_DISCHARGE_PENDING_SETTLEMENT);
    return apply;
  }

  @Test
  void stepsMustFollowOrder() {
    ElderDischargeApply apply = approved();
    assertThrows(IllegalStateException.class, () -> DischargeProgressRules.complete(apply, "ITEM_HANDOVER", "a", now));
    assertThrows(IllegalStateException.class, () -> DischargeProgressRules.assertReadyForRelease(apply));

    DischargeProgressRules.complete(apply, "care_handover", "护士长", now);
    assertEquals("护士长", apply.getCareHandoverByName());
    assertThrows(IllegalStateException.class, () -> DischargeProgressRules.complete(apply, "CARE_HANDOVER", "a", now));
    assertThrows(IllegalStateException.class, () -> DischargeProgressRules.assertReadyForRelease(apply));

    DischargeProgressRules.complete(apply, "ITEM_HANDOVER", "前台", now);
    assertNotNull(apply.getItemHandoverTime());
    assertDoesNotThrow(() -> DischargeProgressRules.assertReadyForRelease(apply));

    // 归档需财务结清并释放床位（auto_discharge_status=SUCCESS）
    assertThrows(IllegalStateException.class, () -> DischargeProgressRules.complete(apply, "ARCHIVE", "a", now));
    apply.setAutoDischargeStatus(ResidenceLifecycleConstants.AUTO_DISCHARGE_SUCCESS);
    DischargeProgressRules.complete(apply, "ARCHIVE", "档案员", now);
    assertEquals(now, apply.getArchiveTime());
  }

  @Test
  void rejectsUnapprovedApplyAndUnknownStep() {
    ElderDischargeApply pending = new ElderDischargeApply();
    pending.setStatus(ResidenceLifecycleConstants.DISCHARGE_APPLY_PENDING);
    assertThrows(IllegalStateException.class, () -> DischargeProgressRules.complete(pending, "CARE_HANDOVER", "a", now));
    assertThrows(IllegalArgumentException.class, () -> DischargeProgressRules.complete(approved(), "FOO", "a", now));
    assertDoesNotThrow(() -> DischargeProgressRules.assertReadyForRelease(null));
  }
}
