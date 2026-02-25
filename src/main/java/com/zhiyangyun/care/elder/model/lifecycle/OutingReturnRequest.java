package com.zhiyangyun.care.elder.model.lifecycle;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OutingReturnRequest {
  private LocalDateTime actualReturnTime;
  private String remark;
}
