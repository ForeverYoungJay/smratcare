package com.zhiyangyun.care.finance.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

/** 按房间登记抄表读数；用电量与电费由后端推导。 */
@Data
public class ElectricityReadingRequest {
  @NotNull
  @Pattern(regexp = "\\d{4}-\\d{2}", message = "账期格式应为 yyyy-MM")
  private String billMonth;
  @NotNull
  private Long roomId;
  /** 不传则自动取上月同房间的本期读数。 */
  @DecimalMin(value = "0", message = "上期读数不能为负数")
  private BigDecimal previousReading;
  @NotNull
  @DecimalMin(value = "0", message = "本期读数不能为负数")
  private BigDecimal currentReading;
  /** ROOM / PER_RESIDENT，默认 ROOM。 */
  @Size(max = 24)
  private String shareMode;
  @Size(max = 200)
  private String remark;
}
