package com.zhiyangyun.care.ltci;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zhiyangyun.care.ltci.model.LtciSettleResult;
import com.zhiyangyun.care.ltci.service.LtciSettlementCalculator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** 长护险月度结算算法单测（金额单位：分）。 */
class LtciSettlementCalculatorTest {

  /** 限额内：全额进入统筹计算。 */
  @Test
  void withinQuota() {
    LtciSettleResult r = LtciSettlementCalculator.calculate(10000, 500, 30, new BigDecimal("0.70"));
    assertEquals(0, r.getOverQuota());
    assertEquals(7000, r.getFundPay());
    assertEquals(3000, r.getSelfPay());
    assertEquals(10000, r.getFundPay() + r.getSelfPay());
  }

  /** 超限额：超出部分全额个人自付，统筹只按限额基数计。 */
  @Test
  void overQuota() {
    LtciSettleResult r = LtciSettlementCalculator.calculate(20000, 500, 30, new BigDecimal("0.70"));
    assertEquals(15000, r.getQuotaCap());
    assertEquals(5000, r.getOverQuota());
    assertEquals(10500, r.getFundPay());   // 15000 * 0.7
    assertEquals(9500, r.getSelfPay());    // 20000 - 10500
  }

  /** 日限额<=0 视为不封顶。 */
  @Test
  void uncapped() {
    LtciSettleResult r = LtciSettlementCalculator.calculate(10000, 0, 30, new BigDecimal("0.80"));
    assertEquals(0, r.getOverQuota());
    assertEquals(8000, r.getFundPay());
    assertEquals(2000, r.getSelfPay());
  }

  /** 四舍五入到整分。 */
  @Test
  void roundingHalfUp() {
    LtciSettleResult r = LtciSettlementCalculator.calculate(10001, 100000, 30, new BigDecimal("0.70"));
    assertEquals(7001, r.getFundPay());    // 10001 * 0.7 = 7000.7 -> 7001
    assertEquals(3000, r.getSelfPay());
  }

  /** 支付比例超过 1 时按 1 处理，基金不超过总额。 */
  @Test
  void ratioClampedToOne() {
    LtciSettleResult r = LtciSettlementCalculator.calculate(8000, 100000, 30, new BigDecimal("1.50"));
    assertEquals(8000, r.getFundPay());
    assertEquals(0, r.getSelfPay());
  }

  /** 服务天数为 0 且有日限额：限额基数为 0，全部个人自付。 */
  @Test
  void zeroServiceDays() {
    LtciSettleResult r = LtciSettlementCalculator.calculate(5000, 500, 0, new BigDecimal("0.70"));
    assertEquals(0, r.getQuotaCap());
    assertEquals(5000, r.getOverQuota());
    assertEquals(0, r.getFundPay());
    assertEquals(5000, r.getSelfPay());
  }
}
