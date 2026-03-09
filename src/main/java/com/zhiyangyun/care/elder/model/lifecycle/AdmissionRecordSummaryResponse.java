package com.zhiyangyun.care.elder.model.lifecycle;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class AdmissionRecordSummaryResponse {
  private long totalCount;
  private long inHospitalCount;
  private long leaveCount;
  private long dischargedCount;
  private long otherStatusCount;

  private long withContractCount;
  private long withoutContractCount;
  private long todayAdmissionCount;

  private List<AdmissionRecordDimensionItem> buildingDistribution = List.of();
  private List<AdmissionRecordDimensionItem> floorDistribution = List.of();

  private LocalDateTime generatedAt;
}
