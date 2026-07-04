package com.zhiyangyun.care.elder.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ElderCareProfileResponse {
  private Long elderId;
  private String elderName;
  private String currentCareLevel;
  private String suggestedCareLevel;
  private String recommendationReason;
  private Boolean adjustmentRequired = false;
  private List<String> riskTags = new ArrayList<>();
  private List<AssessmentSnapshot> latestAssessments = new ArrayList<>();
  private List<CareLevelChangeSnapshot> changeLogs = new ArrayList<>();

  @Data
  public static class AssessmentSnapshot {
    private Long id;
    private String assessmentType;
    private String levelCode;
    private BigDecimal score;
    private LocalDate assessmentDate;
    private String status;
    private String resultSummary;
    private String suggestion;
  }

  @Data
  public static class CareLevelChangeSnapshot {
    private Long id;
    private String beforeValue;
    private String afterValue;
    private String reason;
    private LocalDateTime createTime;
  }
}
