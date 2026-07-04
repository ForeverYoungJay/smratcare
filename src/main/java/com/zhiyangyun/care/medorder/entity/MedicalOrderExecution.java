package com.zhiyangyun.care.medorder.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("medical_order_execution")
public class MedicalOrderExecution {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long orderId;
  private Long elderId;
  private LocalDateTime planTime;
  private LocalDateTime execTime;
  private Long executorId;
  private String executorName;
  /** PENDING 待执行 / DONE 已执行 / SKIPPED 跳过。 */
  private String status;
  private String result;
  private Long dispenseRecordId;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
