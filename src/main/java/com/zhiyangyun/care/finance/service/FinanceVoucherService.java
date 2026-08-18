package com.zhiyangyun.care.finance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.finance.model.FinanceVoucherIssueRequest;
import com.zhiyangyun.care.finance.model.FinanceVoucherUsageView;
import com.zhiyangyun.care.finance.model.FinanceVoucherView;
import com.zhiyangyun.care.finance.model.PaymentVoucherUse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 消费券的发放、作废、可用查询与核销/退回。 */
public interface FinanceVoucherService {

  IPage<FinanceVoucherView> page(long pageNo, long pageSize, Long elderId, String status, String keyword);

  List<FinanceVoucherView> issue(FinanceVoucherIssueRequest request, Long operatorStaffId);

  void revoke(Long voucherId, String reason, Long operatorStaffId);

  /** 某长者在指定账单金额与日期下可用的券（含机构通用券）。 */
  List<FinanceVoucherView> listUsable(Long elderId, BigDecimal billAmount, LocalDate onDate);

  /**
   * 在收款事务内核销消费券，返回实际核销总额。
   * 校验：归属、状态、有效期、剩余额度、使用门槛与是否允许拆分。
   */
  BigDecimal consume(
      List<PaymentVoucherUse> uses,
      Long orgId,
      Long elderId,
      BigDecimal billTotalAmount,
      Long billMonthlyId,
      Long paymentRecordId,
      Long operatorStaffId);

  /** 退回一笔收款已核销的券额度，返回退回总额。 */
  BigDecimal release(Long paymentRecordId, Long operatorStaffId, String remark);

  List<FinanceVoucherUsageView> usageList(Long voucherId);
}
