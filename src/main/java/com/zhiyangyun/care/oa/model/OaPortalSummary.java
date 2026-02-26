package com.zhiyangyun.care.oa.model;

import com.zhiyangyun.care.oa.entity.OaNotice;
import com.zhiyangyun.care.oa.entity.OaTodo;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class OaPortalSummary {
  private List<OaNotice> notices;
  private List<OaTodo> todos;
  private Long openTodoCount;
  private Long overdueTodoCount;
  private Long pendingApprovalCount;
  private Long ongoingTaskCount;
  private Long submittedReportCount;
  private Long todayScheduleCount;
  private Long attendanceAbnormalCount;
  private Long leavePendingCount;
  private Long reimbursePendingCount;
  private Long purchasePendingCount;
  private Long contractPendingCount;
  private Long myExpenseCount;
  private Long deptExpenseCount;
  private Long invoiceFolderCount;
  private Long documentCount;
  private Long inventoryLowStockCount;
  private Long approvalTimeoutCount;
  private Long elderAbnormalCount;
  private Long healthAbnormalCount;
  private Long incidentOpenCount;
  private Long materialTransferDraftCount;
  private Long materialPurchaseDraftCount;
  private Long inHospitalElderCount;
  private Long suggestionCount;
  private List<WorkflowTodoItem> workflowTodos;
  private List<MarketingChannelItem> marketingChannels;
  private List<CollaborationGanttItem> collaborationGantt;
  private List<SuggestionItem> latestSuggestions;

  @Data
  public static class WorkflowTodoItem {
    private String code;
    private String name;
    private Long count;
    private String route;
  }

  @Data
  public static class MarketingChannelItem {
    private String source;
    private Long leadCount;
    private Long contractCount;
  }

  @Data
  public static class CollaborationGanttItem {
    private Long taskId;
    private String title;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer progress;
  }

  @Data
  public static class SuggestionItem {
    private Long id;
    private String content;
    private String proposerName;
    private String status;
    private LocalDateTime createTime;
  }
}
