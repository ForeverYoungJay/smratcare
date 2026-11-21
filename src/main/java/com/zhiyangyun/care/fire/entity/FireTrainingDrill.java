package com.zhiyangyun.care.fire.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** 消防培训与演练 */
@Data
@TableName("fire_training_drill")
public class FireTrainingDrill {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private String drillType;
  private String topic;
  private LocalDate plannedDate;
  private LocalDate heldDate;
  private String organizerName;
  private Integer participantCount;
  private String attendees;
  private String summary;
  private String nextPlan;
  private String fileUrl;
  private String status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private Integer isDeleted;
}
