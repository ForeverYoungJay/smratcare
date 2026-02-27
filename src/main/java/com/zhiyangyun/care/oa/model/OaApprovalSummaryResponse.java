package com.zhiyangyun.care.oa.model;

import lombok.Data;

@Data
public class OaApprovalSummaryResponse {
  private long totalCount;
  private long pendingCount;
  private long approvedCount;
  private long rejectedCount;
  private long timeoutPendingCount;
  private long leavePendingCount;
  private long reimbursePendingCount;
  private long purchasePendingCount;
}
