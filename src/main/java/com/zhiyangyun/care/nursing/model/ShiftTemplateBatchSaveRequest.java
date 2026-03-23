package com.zhiyangyun.care.nursing.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class ShiftTemplateBatchSaveRequest {
  @NotBlank
  private String name;
  private Integer enabled = 1;
  private String remark;
  private Integer replaceExisting = 1;
  @NotEmpty
  private List<ShiftTemplateRuleRequest> items;
}
