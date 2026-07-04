package com.zhiyangyun.care.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("drug_dispense_record")
public class DrugDispenseRecord {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long drugId;
  private Long batchId;
  private String batchNo;
  private Long elderId;
  private Long orderId;
  /** DISPENSE 发药 / RETURN 退药。 */
  private String dispenseType;
  private Integer quantity;
  private Long operatorId;
  private LocalDateTime dispenseTime;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
