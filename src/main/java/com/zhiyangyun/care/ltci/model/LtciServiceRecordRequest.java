package com.zhiyangyun.care.ltci.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

/** 录入长护险护理服务记录。 */
@Data
public class LtciServiceRecordRequest {
  @NotNull
  private Long elderId;
  private Long benefitId;
  private Long packageId;
  @NotNull
  private LocalDate serviceDate;
  private String itemCode;
  private String itemName;
  private Integer quantity;
  /** 费用，单位：分。 */
  @NotNull
  private Long fee;
  private Long operatorId;
  private String signUrl;
  private String remark;
}
