package com.zhiyangyun.care.oa.model;

import java.util.List;
import lombok.Data;

@Data
public class OaBatchIdsRequest {
  private List<Long> ids;
}
