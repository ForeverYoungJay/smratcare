package com.zhiyangyun.care.crm.constant;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.crm.entity.CrmContract;
import com.zhiyangyun.care.crm.entity.CrmLead;
import com.zhiyangyun.care.crm.mapper.CrmContractMapper;
import com.zhiyangyun.care.crm.mapper.CrmLeadMapper;
import java.time.LocalDate;
import java.util.Objects;

/** 线索床位预订是否仍在占床：床态投影与预订冲突校验共用同一口径。 */
public final class CrmReservationRules {
  private CrmReservationRules() {}

  /**
   * 已签约、已失效、已退款或已过锁床期（下次跟进日 = 锁床截止日，早于今天）的预订不再占床，
   * 相当于到期自动释放；记录本身保留，便于追溯。
   */
  public static boolean isActiveReservation(CrmLead lead, LocalDate today) {
    if (lead == null || lead.getReservationBedId() == null) return false;
    if (Objects.equals(lead.getContractSignedFlag(), 1) || Objects.equals(lead.getStatus(), 3)) return false;
    if ("SIGNED".equalsIgnoreCase(lead.getFlowStage())) return false;
    if (Objects.equals(lead.getRefunded(), 1)) return false;
    return lead.getNextFollowDate() == null || !lead.getNextFollowDate().isBefore(today);
  }

  /**
   * 床位是否被“别人”的有效预订或办理中合同占着。线索预订与入住办理选床共用。
   * excludeLeadId / excludeContractId 是本次办理自己的线索与合同，不算冲突。
   */
  public static boolean bedHeldByOthers(CrmLeadMapper leadMapper, CrmContractMapper contractMapper,
      Long tenantId, Long bedId, Long excludeLeadId, Long excludeContractId, LocalDate today) {
    boolean leadHeld = leadMapper.selectList(Wrappers.lambdaQuery(CrmLead.class)
            .eq(CrmLead::getIsDeleted, 0)
            .eq(tenantId != null, CrmLead::getTenantId, tenantId)
            .eq(CrmLead::getReservationBedId, bedId)
            .ne(excludeLeadId != null, CrmLead::getId, excludeLeadId))
        .stream().anyMatch(other -> isActiveReservation(other, today));
    if (leadHeld) return true;
    Long contracts = contractMapper.selectCount(Wrappers.lambdaQuery(CrmContract.class)
        .eq(CrmContract::getIsDeleted, 0)
        .eq(tenantId != null, CrmContract::getTenantId, tenantId)
        .eq(CrmContract::getReservationBedId, bedId)
        .ne(excludeContractId != null, CrmContract::getId, excludeContractId)
        // SQL 里 lead_id <> x 会漏掉 lead_id 为空的合同，所以显式放行 NULL
        .and(excludeLeadId != null, w -> w.ne(CrmContract::getLeadId, excludeLeadId).or().isNull(CrmContract::getLeadId))
        .and(w -> w.in(CrmContract::getFlowStage, "PENDING_BED_SELECT", "PENDING_SIGN")
            .or().in(CrmContract::getStatus, "APPROVED", "PENDING_APPROVAL")));
    return contracts != null && contracts > 0;
  }
}
