package com.zhiyangyun.care.ai.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class AiScheduleProposalResponse {
  private Long id;
  private String title;
  private Long deptId;
  private String deptName;
  private LocalDate dateFrom;
  private LocalDate dateTo;
  private String shiftCodes;
  /** DRAFT/GENERATED/ADOPTED */
  private String status;
  private String scoreJson;
  private Integer unfilledCount;
  private List<String> unfilledSlots;
  private LocalDateTime generatedAt;
  private LocalDateTime adoptedAt;
  private String remark;
  private LocalDateTime createTime;
  private List<AiScheduleProposalItemResponse> items;
}
