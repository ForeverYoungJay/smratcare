package com.zhiyangyun.care.operations.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("operations_staff_task_receipt")
public class StaffMobileTaskReceipt {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long tenantId;
  private Long orgId;
  private Long staffId;
  private String taskId;
  private String taskModule;
  private String taskTitle;
  private String resident;
  private String room;
  private String remark;
  private String scanText;
  private String checkedItems;
  private String photoUrls;
  private String voiceUrl;
  private Integer voiceDurationSec;
  private LocalDateTime receiptTime;
  private String status;
  private Long createdBy;
  @TableField("create_time")
  private LocalDateTime createTime;
  @TableField("update_time")
  private LocalDateTime updateTime;
  @TableField("is_deleted")
  private Integer isDeleted;
}
