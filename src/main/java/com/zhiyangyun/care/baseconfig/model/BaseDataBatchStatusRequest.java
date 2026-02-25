package com.zhiyangyun.care.baseconfig.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class BaseDataBatchStatusRequest {
  @NotEmpty
  private List<Long> ids;
  @NotNull
  private Integer status;
}
