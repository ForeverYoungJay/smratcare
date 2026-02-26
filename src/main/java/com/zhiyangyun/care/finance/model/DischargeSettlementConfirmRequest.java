package com.zhiyangyun.care.finance.model;

import lombok.Data;

@Data
public class DischargeSettlementConfirmRequest {
  private String action;
  private String signerName;
  private String remark;
}
