package com.zhiyangyun.care.baseconfig.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class BaseDataImportRequest {
  @NotBlank
  private String configGroup;
  @NotEmpty
  private List<BaseDataImportItem> items;
}
