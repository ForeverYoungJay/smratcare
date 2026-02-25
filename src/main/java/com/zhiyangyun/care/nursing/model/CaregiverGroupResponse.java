package com.zhiyangyun.care.nursing.model;

import java.util.List;
import lombok.Data;

@Data
public class CaregiverGroupResponse {
  private Long id;
  private Long tenantId;
  private Long orgId;
  private String name;
  private Long leaderStaffId;
  private String leaderStaffName;
  private List<Long> memberStaffIds;
  private Integer enabled;
  private String remark;
}
