package com.zhiyangyun.care.crm.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class MarketingPlanResponse {
  @JsonSerialize(using = ToStringSerializer.class)
  private Long id;
  private String moduleType;
  private String title;
  private String summary;
  private String content;
  private String quarterLabel;
  private String target;
  private String owner;
  private Integer priority;
  private String status;
  private LocalDate effectiveDate;
  private List<Long> linkedDepartmentIds;
  private List<String> linkedDepartmentNames;
  private String latestApprovalStatus;
  private String latestApprovalRemark;
  private LocalDateTime latestApprovalTime;
  private Long totalStaffCount;
  private Long readCount;
  private Long unreadCount;
  private Long agreeCount;
  private Long improveCount;
  private Boolean currentUserRead;
  private String currentUserAction;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
