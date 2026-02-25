package com.zhiyangyun.care.life.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("dining_prep_zone")
public class DiningPrepZone {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long tenantId;

  private Long orgId;

  private String zoneCode;

  private String zoneName;

  private String kitchenArea;

  private Integer capacity;

  private String managerName;

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
