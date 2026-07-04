package com.zhiyangyun.care.operations.model;

import java.util.List;
import lombok.Data;

@Data
public class OperationsServiceReportResponse {
  private String month;
  private String generatedAt;
  private String reportTitle;
  private String status;
  private List<ReportMetric> metrics;
  private List<QualityDomain> qualityDomains;
  private List<ComplianceArchive> complianceArchives;
  private List<StandardProcess> standardProcesses;
  private List<ReportAction> monthlyActions;

  @Data
  public static class ReportMetric {
    private String key;
    private String label;
    private String value;
    private String helper;
    private String status;
    private String routePath;
  }

  @Data
  public static class QualityDomain {
    private String key;
    private String title;
    private String owner;
    private String status;
    private String routePath;
    private List<String> checkpoints;
    private List<String> evidence;
  }

  @Data
  public static class ComplianceArchive {
    private String key;
    private String title;
    private String status;
    private String routePath;
    private List<String> requiredDocuments;
    private List<String> missingOrNext;
  }

  @Data
  public static class StandardProcess {
    private String key;
    private String title;
    private String status;
    private String routePath;
    private List<String> steps;
  }

  @Data
  public static class ReportAction {
    private String title;
    private String priority;
    private String owner;
    private String routePath;
    private String dueHint;
  }
}
