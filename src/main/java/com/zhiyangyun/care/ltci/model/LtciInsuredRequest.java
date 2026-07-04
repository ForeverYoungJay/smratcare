package com.zhiyangyun.care.ltci.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

/** 长护险参保登记。 */
@Data
public class LtciInsuredRequest {
  @NotNull
  private Long elderId;
  @NotBlank
  private String insuredNo;
  private String idCard;
  private String cityCode;
  private LocalDate startDate;
  private LocalDate endDate;
  private String remark;
}
