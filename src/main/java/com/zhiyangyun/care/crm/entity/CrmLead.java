package com.zhiyangyun.care.crm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("crm_lead")
public class CrmLead {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
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
  private LocalDate consultDate;
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
  private LocalDate contractExpiryDate;
  private Integer smsSendCount;
  private LocalDate nextFollowDate;
  private String remark;
  private Long createdBy;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
