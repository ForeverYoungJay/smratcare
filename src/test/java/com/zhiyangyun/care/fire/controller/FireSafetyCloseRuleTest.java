package com.zhiyangyun.care.fire.controller;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zhiyangyun.care.fire.entity.FireSafetyRecord;
import org.junit.jupiter.api.Test;

class FireSafetyCloseRuleTest {
  private static FireSafetyRecord record(String issue, String action) {
    FireSafetyRecord record = new FireSafetyRecord();
    record.setIssueDescription(issue);
    record.setActionTaken(action);
    return record;
  }

  @Test
  void aRecordWithAnIssueNeedsHandlingBeforeItCanBeClosed() {
    assertThatThrownBy(() -> FireSafetyRecordController.requireHandledBeforeClose(record("B栋防火门闭门器异常", " ")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatCode(() -> FireSafetyRecordController.requireHandledBeforeClose(record("B栋防火门闭门器异常", "已更换闭门器并复核")))
        .doesNotThrowAnyException();
    assertThatCode(() -> FireSafetyRecordController.requireHandledBeforeClose(record(null, null)))
        .doesNotThrowAnyException();
  }

  @Test
  void onlyManagersCloseRecordsThatHadAnIssueButRoutineRecordsCanBeSavedClosed() {
    assertThatThrownBy(() -> FireSafetyRecordController.requireCloseAllowed(record("配电间门未关", "已关门"), false))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatCode(() -> FireSafetyRecordController.requireCloseAllowed(record("配电间门未关", "已关门"), true))
        .doesNotThrowAnyException();
    assertThatCode(() -> FireSafetyRecordController.requireCloseAllowed(record(null, null), false))
        .doesNotThrowAnyException();
  }
}
