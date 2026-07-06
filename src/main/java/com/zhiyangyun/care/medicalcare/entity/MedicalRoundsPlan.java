package com.zhiyangyun.care.medicalcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 医生巡诊排班计划。 */
@Data
@TableName("medical_rounds_plan")
public class MedicalRoundsPlan {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private LocalDate planDate;
  /** 时段，如 09:00-11:00。 */
  private String timeSlot;
  private Long doctorId;
  private String doctorName;
  /** 楼层/区域，如 2号楼3层。 */
  private String area;
  /** ALL 全院 / AREA 按区域 / CUSTOM 指定长者。 */
  private String elderScope;
  /** CUSTOM 时的长者ID列表 JSON，如 [1,2,3]。 */
  private String elderIdsJson;
  /** PLANNED / IN_PROGRESS / DONE / CANCELED。 */
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
