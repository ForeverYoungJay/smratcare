package com.zhiyangyun.care.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("drug_batch")
public class DrugBatch {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long drugId;
  private String batchNo;
  private LocalDate expireDate;
  private LocalDate productionDate;
  private Integer quantity;
  /** 进价，单位：分。 */
  private Long purchasePrice;
  private String supplier;
  private LocalDateTime inboundAt;
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
