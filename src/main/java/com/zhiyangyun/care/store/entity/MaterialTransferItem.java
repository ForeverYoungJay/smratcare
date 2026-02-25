package com.zhiyangyun.care.store.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("material_transfer_item")
public class MaterialTransferItem {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long orgId;

  private Long transferId;

  private Long productId;

  private String productNameSnapshot;

  private Integer quantity;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
