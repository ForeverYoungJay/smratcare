package com.zhiyangyun.care.visit.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("visit_qrcode_check_log")
public class VisitCheckLog {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long bookingId;
  private LocalDateTime checkTime;
  private Long staffId;
  private String qrcodeToken;
  private Integer resultStatus;
  private String remark;
  @TableField(value = "create_time", fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
  @TableField(value = "is_deleted", fill = FieldFill.INSERT)
  private Integer isDeleted;
}
