package com.zhiyangyun.care.ltci.service;

import com.zhiyangyun.care.ltci.model.LtciSettleResult;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 长护险月度结算计算（纯逻辑，金额单位：分）。
 *
 * <p>规则：限额基数 = 日限额 × 服务天数；统筹按限额内金额乘支付比例，超限额部分全额个人自付。
 * <pre>
 *   quotaCap  = dailyQuota × serviceDays        (dailyQuota<=0 视为不封顶)
 *   base      = min(totalFee, quotaCap)
 *   overQuota = max(0, totalFee - quotaCap)
 *   fundPay   = round(base × payRatio)
 *   selfPay   = totalFee - fundPay              (含超限额部分)
 * </pre>
 */
public final class LtciSettlementCalculator {
  private LtciSettlementCalculator() {}

  public static LtciSettleResult calculate(long totalFee, long dailyQuota, int serviceDays,
      BigDecimal payRatio) {
    long total = Math.max(0L, totalFee);
    int days = Math.max(0, serviceDays);
    BigDecimal ratio = payRatio == null ? BigDecimal.ZERO : payRatio;
    if (ratio.signum() < 0) {
      ratio = BigDecimal.ZERO;
    }
    if (ratio.compareTo(BigDecimal.ONE) > 0) {
      ratio = BigDecimal.ONE;
    }

    boolean uncapped = dailyQuota <= 0L;
    long quotaCap = uncapped ? total : Math.multiplyExact(dailyQuota, (long) days);
    long base = Math.min(total, quotaCap);
    long overQuota = uncapped ? 0L : Math.max(0L, total - quotaCap);

    long fundPay = BigDecimal.valueOf(base).multiply(ratio)
        .setScale(0, RoundingMode.HALF_UP).longValueExact();
    if (fundPay > total) {
      fundPay = total;
    }
    long selfPay = total - fundPay;

    LtciSettleResult r = new LtciSettleResult();
    r.setTotalFee(total);
    r.setQuotaCap(quotaCap);
    r.setOverQuota(overQuota);
    r.setFundPay(fundPay);
    r.setSelfPay(selfPay);
    return r;
  }
}
