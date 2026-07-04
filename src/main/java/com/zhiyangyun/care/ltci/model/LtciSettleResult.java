package com.zhiyangyun.care.ltci.model;

import lombok.Data;

/** 结算计算结果（金额单位：分）。 */
@Data
public class LtciSettleResult {
  private long totalFee;
  private long fundPay;
  private long selfPay;
  private long overQuota;
  private long quotaCap;
}
