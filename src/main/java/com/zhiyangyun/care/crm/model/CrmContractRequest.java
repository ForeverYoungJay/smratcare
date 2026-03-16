package com.zhiyangyun.care.crm.model;

import lombok.Data;

@Data
public class CrmContractRequest {
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
  private String contractSignedAt;
  private String contractExpiryDate;
  private String contractStatus;
  private String flowStage;
  private String currentOwnerDept;
  private String orgName;
  private String status;
  private String changeWorkflowStatus;
  private String changeWorkflowRemark;
  private String remark;
  private Integer smsSendCount;
}
