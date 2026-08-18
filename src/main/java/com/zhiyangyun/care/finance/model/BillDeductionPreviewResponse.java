package com.zhiyangyun.care.finance.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** 收款登记前的抵扣可用额度预览：应收 − 长护险 − 折扣 − 消费券 = 实收现金。 */
@Data
public class BillDeductionPreviewResponse {
  private Long billId;
  private Long elderId;
  private String elderName;
  private String billMonth;
  /** 账单应收总额。 */
  private BigDecimal totalAmount = BigDecimal.ZERO;
  /** 已抵账合计（含历史实收与抵扣）。 */
  private BigDecimal settledAmount = BigDecimal.ZERO;
  /** 本次可抵账上限。 */
  private BigDecimal outstandingAmount = BigDecimal.ZERO;
  /** 长护险统筹本月可抵扣余额。 */
  private BigDecimal ltciAvailableAmount = BigDecimal.ZERO;
  /** 长护险本月统筹支付总额（结算单口径）。 */
  private BigDecimal ltciFundPayAmount = BigDecimal.ZERO;
  /** 本月已登记的长护险抵扣。 */
  private BigDecimal ltciUsedAmount = BigDecimal.ZERO;
  private String ltciHint;
  private List<FinanceVoucherView> usableVouchers = new ArrayList<>();
}
