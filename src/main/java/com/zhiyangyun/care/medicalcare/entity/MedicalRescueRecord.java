package com.zhiyangyun.care.medicalcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/** 抢救记录单。 */
@Data
@TableName("medical_rescue_record")
public class MedicalRescueRecord {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long eventId;
  private Long elderId;
  /** 时间线明细 JSON，如 [{"time":"10:02","action":"胸外按压"}]。 */
  private String timelineJson;
  /** 参与人员。 */
  private String participants;
  /** 抢救用药。 */
  private String drugsUsed;
  /** 抢救措施。 */
  private String measures;
  /** SUCCESS 抢救成功 / TRANSFERRED 转院 / FAILED 无效。 */
  private String result;
  private String resultNote;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Long recorderId;
  private String recorderName;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
