package com.zhiyangyun.care.medicalcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 120急救事件（发起→呼叫→送医→返回/住院→关闭）。 */
@Data
@TableName("medical_emergency_event")
public class MedicalEmergencyEvent {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private String elderName;
  /** 预留 IoT SOS 告警关联。 */
  private Long alertId;
  private LocalDateTime eventTime;
  private String location;
  private String symptom;
  /** 120 呼叫时间。 */
  private LocalDateTime callTime;
  private Long callOperatorId;
  private String callOperatorName;
  private String hospitalName;
  /** 陪同人。 */
  private String escortName;
  /** 送医出发时间。 */
  private LocalDateTime departTime;
  /** 转归：RETURNED 返回 / HOSPITALIZED 住院 / DECEASED 死亡 / CANCELED 取消。 */
  private String outcome;
  private LocalDateTime outcomeTime;
  private String outcomeNote;
  private Long reporterId;
  private String reporterName;
  /** INITIATED / CALLED / TRANSFERRED / RETURNED / HOSPITALIZED / CLOSED。 */
  private String status;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
