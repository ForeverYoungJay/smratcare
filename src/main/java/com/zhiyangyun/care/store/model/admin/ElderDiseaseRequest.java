package com.zhiyangyun.care.store.model.admin;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class ElderDiseaseRequest {
  @NotEmpty
  private List<Long> diseaseIds;
}
