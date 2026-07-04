package com.zhiyangyun.care.ltci.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("ltci_service_record")
public class LtciServiceRecord {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long elderId;
  private Long benefitId;
  private Long packageId;
  private LocalDate serviceDate;
  private String itemCode;
  private String itemName;
  private Integer quantity;
  /** 费用，单位：分。 */
  private Long fee;
  private Long operatorId;
  private String signUrl;
  private String settleMonth;
  private Integer settled;
  private String remark;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
