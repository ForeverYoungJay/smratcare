package com.zhiyangyun.care.crm.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrmLeadResponse {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long id;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long tenantId;
  @JsonSerialize(using = ToStringSerializer.class)
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
  private LocalDateTime invalidTime;
  private String idCardNo;
  private String reservationRoomNo;
  @JsonSerialize(using = ToStringSerializer.class)
  private Long reservationBedId;
  private String reservationChannel;
  private String reservationStatus;
  private Integer refunded;
  private java.math.BigDecimal reservationAmount;
  private LocalDateTime paymentTime;
  private String orgName;
  private String source;
  private String customerTag;
  private Integer status;
  private Integer contractSignedFlag;
  private LocalDateTime contractSignedAt;
  private String contractNo;
  private String contractStatus;
  private String flowStage;
  private String currentOwnerDept;
  private String contractExpiryDate;
  private Integer smsSendCount;
  private String nextFollowDate;
  private String remark;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
