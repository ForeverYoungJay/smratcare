package com.zhiyangyun.care.store.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("`order`")
public class StoreOrder {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private Long orgId;

  private String orderNo;

  private Long elderId;

  private BigDecimal totalAmount;

  private Integer pointsUsed;

  private BigDecimal payableAmount;

  private Integer payStatus;

  private LocalDateTime payTime;

  private Integer orderStatus;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField("update_time")
  private LocalDateTime updateTime;

  @TableField("is_deleted")
  private Integer isDeleted;
}
