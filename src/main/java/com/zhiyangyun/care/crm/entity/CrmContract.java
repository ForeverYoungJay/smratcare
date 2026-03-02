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
@TableName("crm_contract")
public class CrmContract {
  @TableId(type = IdType.ASSIGN_ID)
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
  private String status;
  private LocalDateTime signedAt;
  private LocalDate effectiveFrom;
  private LocalDate effectiveTo;
  private String marketerName;
  private String snapshotJson;
  private String reservationRoomNo;
  private Long reservationBedId;
  private String elderName;
  private String elderPhone;
  private Integer gender;
  private Integer age;
  private String contractStatus;
  private String flowStage;
  private String currentOwnerDept;
  private String orgName;
  private Integer smsSendCount;
  private String remark;
  private Long createdBy;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
