package com.zhiyangyun.care.medorder.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("medical_order")
public class MedicalOrder {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private Long emrId;
  /** LONG_TERM 长期 / TEMPORARY 临时。 */
  private String orderType;
  /** DRUG 药疗 / NURSING 护理 / EXAM 检查 / DIET 饮食。 */
  private String category;
  private String content;
  private Long drugId;
  private String dosage;
  /** QD/BID/TID/QID/Q8H/Q12H/QOD/PRN/ST。 */
  private String frequency;
  private String route;
  private Integer quantityPerTime;
  private LocalDateTime startTime;
  private LocalDateTime stopTime;
  private Long doctorId;
  private String doctorName;
  private String status;
  private String priority;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
