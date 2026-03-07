package com.zhiyangyun.care.store.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("inventory_outbound_sheet")
public class InventoryOutboundSheet {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long orgId;

  private String outboundNo;

  private String receiverName;

  private Long elderId;

  private String contractNo;

  private String applyDept;

  private Long operatorStaffId;

  private String operatorName;

  private String status;

  private String remark;

  private Long confirmStaffId;

  private LocalDateTime confirmTime;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
