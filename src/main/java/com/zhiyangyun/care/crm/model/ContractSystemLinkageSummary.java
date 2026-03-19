package com.zhiyangyun.care.crm.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ContractSystemLinkageSummary {
  private Long elderId;
  private String elderName;
  private Boolean elderArchiveReady;
  private Boolean financeAccountReady;
  private Boolean financeBillReady;
  private Boolean logisticsReady;
  private Boolean medicalRecordReady;
  private String billMonth;
  private LocalDate healthRecordDate;
  private Long billId;
  private Integer billItemCount;
  private BigDecimal billTotalAmount;
  private Boolean medicalInspectionReady;
  private Long starterInspectionId;
  private LocalDate starterInspectionDate;
  private Boolean medicalCareTaskReady;
  private Long starterCareTaskId;
  private LocalDate starterCareTaskDate;
}
