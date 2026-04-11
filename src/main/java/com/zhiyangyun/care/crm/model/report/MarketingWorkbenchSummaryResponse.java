package com.zhiyangyun.care.crm.model.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class MarketingWorkbenchSummaryResponse {
  private Funnel funnel = new Funnel();
  private Followup followup = new Followup();
  private BedSales bedSales = new BedSales();
  private Contract contract = new Contract();
  private Callback callback = new Callback();
  private Performance performance = new Performance();
  private Medical medical = new Medical();
  private Plan plan = new Plan();
  private Risk risk = new Risk();
  private List<ChannelTop> channelTop5 = new ArrayList<>();
  private long channelUnknownCount;
  private long channelMonthDeals;
  private String dateFrom;
  private String dateTo;
  private LocalDateTime generatedAt;

  @Data
  public static class Funnel {
    private long todayConsultCount;
    private long evaluationCount;
    private long pendingSignCount;
    private long pendingAdmissionCount;
    private long monthDealCount;
    private double monthConversionRate;
  }

  @Data
  public static class Followup {
    private long todayDue;
    private long overdue;
    private long highIntentCount;
    private long lockExpiringCount;
  }

  @Data
  public static class BedSales {
    private long emptyCount;
    private long lockCount;
    private long reservedUnsignedCount;
    private long premiumEmptyCount;
  }

  @Data
  public static class Contract {
    private long pendingSignCount;
    private long renewalDueCount;
    private long changePendingCount;
    private BigDecimal monthAmount = BigDecimal.ZERO;
  }

  @Data
  public static class Callback {
    private long checkinCount;
    private long trialCount;
    private long dischargeCount;
    private double score;
  }

  @Data
  public static class Performance {
    private long monthDealCount;
    private BigDecimal monthAmount = BigDecimal.ZERO;
    private long rankNo;
    private double timelyRate;
  }

  @Data
  public static class Medical {
    private long todayCount;
    private long referCount;
    private long unassignedCount;
  }

  @Data
  public static class Plan {
    private long speechCount;
    private long policyCount;
    private long pendingApprovalCount;
    private long rejectedCount;
    private BigDecimal totalBudgetAmount = BigDecimal.ZERO;
    private long totalActualLeadCount;
    private long totalActualContractCount;
  }

  @Data
  public static class Risk {
    private long overdueFollowupCount;
    private long lockUnsignedCount;
    private long highIntentNoEvalCount;
    private long channelDropCount;
  }

  @Data
  public static class ChannelTop {
    private String source;
    private long leadCount;
    private String contractRate;
  }
}
