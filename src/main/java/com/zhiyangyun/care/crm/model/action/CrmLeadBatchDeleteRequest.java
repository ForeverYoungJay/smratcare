package com.zhiyangyun.care.crm.model.action;

import java.util.List;
import lombok.Data;

@Data
public class CrmLeadBatchDeleteRequest {
  private List<Long> ids;
}
