package com.zhiyangyun.care.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class CrmLeadRequest {
  private Long tenantId;
  private Long orgId;
  private String name;
  private String phone;
  private String consultantName;
  private String consultantPhone;
  private String elderName;
  private String elderPhone;
  private Integer gender;
  private Integer age;
  private String consultDate;
  private String consultType;
  private String mediaChannel;
  private String infoSource;
  private String receptionistName;
  private String homeAddress;
  private String marketerName;
  private Long ownerStaffId;
  private String ownerStaffName;
  private String followupStatus;
  private String referralChannel;
  private String invalidTime;
  private String idCardNo;
  private String reservationRoomNo;
  private Long reservationBedId;
  private String reservationChannel;
  private String reservationStatus;
  private Integer refunded;
  private java.math.BigDecimal reservationAmount;
  private String paymentTime;
  private String orgName;
  private String source;
  private String customerTag;
  private Integer status = 0;
  private Integer contractSignedFlag;
  private String contractSignedAt;
  private String contractNo;
  private String contractStatus;
  private String flowStage;
  private String currentOwnerDept;
  private String contractExpiryDate;
  private Integer smsSendCount;
  private String nextFollowDate;
  private String remark;
  private Long createdBy;
  private Boolean allowDuplicate;

  @JsonIgnore
  @AssertTrue(message = "请填写线索姓名、咨询人姓名或老人姓名")
  public boolean isLeadNamePresent() {
    return hasText(name) || hasText(consultantName) || hasText(elderName);
  }

  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }
}
