package com.zhiyangyun.care.crm.model;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrmLeadRequest {
  private Long tenantId;
  private Long orgId;
  @NotBlank
  private String name;
  private String phone;
  private String source;
  private String customerTag;
  private Integer status = 0;
  private Integer contractSignedFlag;
  private LocalDateTime contractSignedAt;
  private String contractNo;
  private String nextFollowDate;
  private String remark;
  private Long createdBy;
}
