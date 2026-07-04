package com.zhiyangyun.care.operations.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class OperationsWorkforceResponse {
  private String generatedAt;
  private String status;
  private Integer workforceIndex;
  private String workforceLevel;
  private List<WorkforceMetric> metrics = new ArrayList<>();
  private List<WorkforceRisk> risks = new ArrayList<>();
  private List<WorkforceRoleLoad> roleLoads = new ArrayList<>();
  private List<WorkforceAction> actions = new ArrayList<>();
  private List<WorkforceFlow> flows = new ArrayList<>();

  @Data
  public static class WorkforceMetric {
    private String key;
    private String label;
    private String value;
    private String helper;
    private String tone;
    private String routePath;
  }

  @Data
  public static class WorkforceRisk {
    private String key;
    private String title;
    private String owner;
    private String level;
    private String routePath;
    private List<String> signals = new ArrayList<>();
    private List<String> controls = new ArrayList<>();
  }

  @Data
  public static class WorkforceRoleLoad {
    private String key;
    private String title;
    private String coverage;
    private String status;
    private String routePath;
    private List<String> signals = new ArrayList<>();
  }

  @Data
  public static class WorkforceAction {
    private String title;
    private String owner;
    private String priority;
    private String routePath;
    private String description;
  }

  @Data
  public static class WorkforceFlow {
    private String key;
    private String title;
    private String status;
    private String routePath;
    private List<String> steps = new ArrayList<>();
  }
}
