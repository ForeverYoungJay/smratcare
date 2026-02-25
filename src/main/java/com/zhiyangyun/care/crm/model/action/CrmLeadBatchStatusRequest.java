package com.zhiyangyun.care.crm.model.action;

import java.util.List;
import lombok.Data;

@Data
public class CrmLeadBatchStatusRequest {
  private List<Long> ids;
  private Integer status;
  private String followupStatus;
  private String invalidTime;
}
