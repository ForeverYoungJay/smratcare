package com.zhiyangyun.care.elder.model.lifecycle;

import com.zhiyangyun.care.elder.entity.lifecycle.ElderDischargeApply;
import java.time.LocalDateTime;

/** 退住六步办理的顺序规则：审批 → 护理/医务交接 → 物品及房间交接 → 费用结清(财务退款并释放床位) → 档案归档。 */
public final class DischargeProgressRules {
  public static final String CARE_HANDOVER = "CARE_HANDOVER";
  public static final String ITEM_HANDOVER = "ITEM_HANDOVER";
  public static final String ARCHIVE = "ARCHIVE";

  private DischargeProgressRules() {
  }

  /** 校验前置步骤并写入完成时间与确认人；不落库。 */
  public static void complete(ElderDischargeApply apply, String step, String operatorName, LocalDateTime now) {
    String normalized = step == null ? "" : step.trim().toUpperCase();
    if (!ResidenceLifecycleConstants.DISCHARGE_APPLY_APPROVED.equals(apply.getStatus())) {
      throw new IllegalStateException("退住申请未审核通过，不能办理后续步骤");
    }
    switch (normalized) {
      case CARE_HANDOVER -> {
        requireNotDone(apply.getCareHandoverTime());
        apply.setCareHandoverTime(now);
        apply.setCareHandoverByName(operatorName);
      }
      case ITEM_HANDOVER -> {
        requireNotDone(apply.getItemHandoverTime());
        if (apply.getCareHandoverTime() == null) {
          throw new IllegalStateException("请先完成护理/医务交接");
        }
        apply.setItemHandoverTime(now);
        apply.setItemHandoverByName(operatorName);
      }
      case ARCHIVE -> {
        requireNotDone(apply.getArchiveTime());
        if (!ResidenceLifecycleConstants.AUTO_DISCHARGE_SUCCESS.equals(apply.getAutoDischargeStatus())) {
          throw new IllegalStateException("请先完成费用结清与床位释放后再归档");
        }
        apply.setArchiveTime(now);
        apply.setArchiveByName(operatorName);
      }
      default -> throw new IllegalArgumentException("步骤仅支持 CARE_HANDOVER/ITEM_HANDOVER/ARCHIVE");
    }
  }

  /** 财务退款会把长者置为已退住并释放床位，之前必须完成交接。apply 为空表示未关联退住申请，不做限制。 */
  public static void assertReadyForRelease(ElderDischargeApply apply) {
    if (apply == null) {
      return;
    }
    if (apply.getCareHandoverTime() == null || apply.getItemHandoverTime() == null) {
      throw new IllegalStateException("请先在「费用与退住」完成护理/医务交接与物品及房间交接，再结清费用并释放床位");
    }
  }

  private static void requireNotDone(LocalDateTime doneTime) {
    if (doneTime != null) {
      throw new IllegalStateException("该步骤已完成");
    }
  }
}
