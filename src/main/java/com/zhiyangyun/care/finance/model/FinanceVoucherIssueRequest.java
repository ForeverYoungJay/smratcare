package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/** 发放消费券：elderIds 为空表示发放机构通用券。 */
@Data
public class FinanceVoucherIssueRequest {
  @NotBlank
  @Size(max = 100)
  private String voucherName;
  @NotNull
  @DecimalMin(value = "0.01", message = "券面额必须大于 0")
  private BigDecimal faceAmount;
  private BigDecimal minBillAmount;
  private Boolean allowSplit;
  private LocalDate validFrom;
  private LocalDate validTo;
  @Size(max = 32)
  private String source;
  @Size(max = 500)
  private String remark;
  private List<Long> elderIds;
}
