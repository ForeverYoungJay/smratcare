package com.zhiyangyun.care.store.model.admin;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class ElderDiseaseRequest {
  @NotEmpty
  private List<@NotNull Long> diseaseIds;
}
