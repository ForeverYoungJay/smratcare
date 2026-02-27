package com.zhiyangyun.care.crm.model;

import com.zhiyangyun.care.crm.model.action.CrmContractAttachmentResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CrmContractLinkageResponse {
  private Long leadId;
  private Long elderId;
  private String elderName;
  private String elderPhone;
  private String orgName;
  private String marketerName;

  private String contractNo;
  private String contractStatus;
  private LocalDateTime contractSignedAt;
  private LocalDate contractExpiryDate;

  private LocalDate admissionDate;
  private BigDecimal depositAmount;

  private Integer billCount;
  private BigDecimal billTotalAmount;
  private BigDecimal billPaidAmount;
  private BigDecimal billOutstandingAmount;

  private Integer attachmentCount;
  private List<CrmContractAttachmentResponse> attachments = new ArrayList<>();
}
