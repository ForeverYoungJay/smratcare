package com.zhiyangyun.care.hr.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HrRecruitmentNeedResponse {
  private Long id;
  private String title;
  private String positionName;
  private String departmentName;
  private Integer headcount;
  private LocalDate requiredDate;
  private String scene;
  private String status;
  private String remark;
  private String applicantName;
  private LocalDateTime createTime;
  private String candidateName;
  private String contactPhone;
  private String resumeUrl;
  private String intentionStatus;
  private LocalDate followUpDate;
  private String offerStatus;
  private LocalDate onboardDate;
  private String salary;
  private String probationPeriod;
  private String workLocation;
  private String shiftType;
  private String checklistJson;
  private String signedFilesJson;
  private String accountPermissionJson;
  private String issuedItemsJson;
  private String mentorName;
  private String probationGoal;
  private String regularizationStatus;
  private String recommendationNote;
  private String offboardingType;
  private LocalDate lastWorkDate;
  private LocalDate handoverDeadline;
  private String resignationReason;
  private String resignationReportUrl;
  private String handoverItemsJson;
  private String assetRecoveryJson;
  private String permissionRecycleJson;
  private String financeSettlementNote;
  private String exitArchiveJson;
  private String formData;
}
