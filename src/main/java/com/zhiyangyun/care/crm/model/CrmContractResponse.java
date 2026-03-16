package com.zhiyangyun.care.crm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrmContractResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long leadId;
  private Long elderId;
  private String contractNo;
  private String name;
  private String phone;
  private String idCardNo;
  private String homeAddress;
  private String reservationRoomNo;
  private Long reservationBedId;
  private String elderName;
  private String elderPhone;
  private Integer gender;
  private Integer age;
  private String marketerName;
  private LocalDateTime contractSignedAt;
  private LocalDate contractExpiryDate;
  private String contractStatus;
  private String flowStage;
  private String currentOwnerDept;
  private String orgName;
  private String status;
  private String changeWorkflowStatus;
  private String changeWorkflowRemark;
  private Integer smsSendCount;
  private String remark;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
