package com.zhiyangyun.care.store.model.admin;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class ForbiddenTagsRequest {
  @NotNull
  private List<Long> tagIds;
}
