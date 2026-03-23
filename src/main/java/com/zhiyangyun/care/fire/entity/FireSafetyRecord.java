package com.zhiyangyun.care.fire.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;

@Data
@TableName("fire_safety_record")
public class FireSafetyRecord {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private String recordType;

  private String title;

  private String location;

  private String inspectorName;

  private LocalDateTime checkTime;

  private String status;

  private String issueDescription;

  private String actionTaken;

  private LocalDate nextCheckDate;

  private Boolean dailyReminderEnabled;

  private LocalTime dailyReminderTime;

  private Long sourceRecordId;

  private Integer checkRound;

  private String qrToken;

  private LocalDateTime qrGeneratedAt;

  private LocalDateTime scanCompletedAt;

  private String dutyRecord;

  private LocalDateTime handoverPunchTime;

  private String equipmentBatchNo;

  private LocalDate productProductionDate;

  private LocalDate productExpiryDate;

  private Integer checkCycleDays;

  private String equipmentUpdateNote;

  private String equipmentAgingDisposal;

  @JsonIgnore
  @TableField("image_urls")
  private String imageUrlsText;

  @TableField(exist = false)
  private List<String> imageUrls;

  private String thirdPartyMaintenanceFileUrl;

  private String purchaseContractFileUrl;

  private LocalDate contractStartDate;

  private LocalDate contractEndDate;

  @JsonIgnore
  @TableField("purchase_document_urls")
  private String purchaseDocumentUrlsText;

  @TableField(exist = false)
  private List<String> purchaseDocumentUrls;

  private Long createdBy;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
