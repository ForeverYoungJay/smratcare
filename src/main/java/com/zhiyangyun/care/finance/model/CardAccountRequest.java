package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardAccountRequest {
  @NotNull
  private Long elderId;

  private String cardNo;

  private String status;

  private Integer lossFlag;

  private String remark;
}
