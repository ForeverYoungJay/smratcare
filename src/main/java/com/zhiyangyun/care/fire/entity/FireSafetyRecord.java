package com.zhiyangyun.care.fire.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

  private String qrToken;

  private LocalDateTime qrGeneratedAt;

  private LocalDateTime scanCompletedAt;

  private String dutyRecord;

  private LocalDateTime handoverPunchTime;

  private String equipmentBatchNo;

  private String equipmentUpdateNote;

  private String equipmentAgingDisposal;

  private Long createdBy;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
