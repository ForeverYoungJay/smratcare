package com.zhiyangyun.care.crm.model;

import java.time.LocalDateTime;
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
  private String followupStatus;
  private String referralChannel;
  private String invalidTime;
  private String idCardNo;
  private String reservationRoomNo;
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
  private LocalDateTime contractSignedAt;
  private String contractNo;
  private String contractStatus;
  private String contractExpiryDate;
  private Integer smsSendCount;
  private String nextFollowDate;
  private String remark;
  private Long createdBy;
}
