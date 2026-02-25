package com.zhiyangyun.care.oa.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OaGroupSettingRequest {
  @NotBlank
  private String groupName;

  private String groupType;

  private Long leaderId;

  private String leaderName;

  private Integer memberCount;

  private String status;

  private String remark;
}
