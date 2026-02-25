package com.zhiyangyun.care.crm.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrmLeadResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String name;
  private String phone;
  private String source;
  private String customerTag;
  private Integer status;
  private Integer contractSignedFlag;
  private LocalDateTime contractSignedAt;
  private String contractNo;
  private String nextFollowDate;
  private String remark;
  private LocalDateTime createTime;
}
