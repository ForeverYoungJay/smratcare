package com.zhiyangyun.care.ltci.model;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

/** 依据评估等级核定长护险待遇。 */
@Data
public class LtciBenefitRequest {
  @NotNull
  private Long insuredId;
  @NotNull
  private Long elderId;
  /** 关联的生效评估记录 id（用于带出失能等级）。 */
  private Long assessmentId;
  private Integer disabilityLevel;
  /** INSTITUTION 机构 / HOME 居家 / DEVICE 辅具。 */
  private String benefitType;
  /** 日限额，单位：分。 */
  @NotNull
  private Long dailyQuota;
  /** 统筹支付比例，0~1。 */
  @NotNull
  private BigDecimal payRatio;
  private LocalDate validStart;
  private LocalDate validEnd;
  private String remark;
}
