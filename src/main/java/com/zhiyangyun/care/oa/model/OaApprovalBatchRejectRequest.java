package com.zhiyangyun.care.oa.model;

import java.util.List;
import lombok.Data;

@Data
public class OaApprovalBatchRejectRequest {
  private List<Long> ids;
  private String remark;
}
