package com.zhiyangyun.care.fire.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 消防楼栋与巡查点位 */
@Data
@TableName("fire_location")
public class FireLocation {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private String building;
  private String floorName;
  private String area;
  private String pointCode;
  private String riskLevel;
  private Integer keyPart;
  private Integer enabled;
  private String remark;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private Integer isDeleted;
}
