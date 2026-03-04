package com.zhiyangyun.care.logistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("logistics_equipment_archive")
public class LogisticsEquipmentArchive {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private String equipmentCode;

  private String equipmentName;

  private String category;

  private String brand;

  private String modelNo;

  private String serialNo;

  private LocalDate purchaseDate;

  private LocalDate warrantyUntil;

  private String location;

  private String maintainerName;

  private LocalDateTime lastMaintainedAt;

  private LocalDateTime nextMaintainedAt;

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
