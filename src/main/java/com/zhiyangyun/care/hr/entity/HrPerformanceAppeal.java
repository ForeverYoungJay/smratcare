package com.zhiyangyun.care.hr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("hr_performance_appeal")
public class HrPerformanceAppeal {
  @TableId(type = IdType.ASSIGN_ID)
  private Long id;
  private Long orgId;
  private Long staffId;
  private String staffName;
  private String targetType;
  private Long targetId;
  private String reason;
  private String status;
  private String reviewerName;
  private LocalDateTime reviewedAt;
  private String decisionNote;
  private LocalDateTime createTime;
}
