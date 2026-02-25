package com.zhiyangyun.care.nursing.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;

@Data
public class CaregiverGroupRequest {
  private Long tenantId;
  private Long orgId;
  @NotBlank
  private String name;
  private Long leaderStaffId;
  private List<Long> memberStaffIds;
  private Integer enabled = 1;
  private String remark;
  private Long createdBy;
}
