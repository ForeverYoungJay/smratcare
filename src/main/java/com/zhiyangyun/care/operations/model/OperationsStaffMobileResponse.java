package com.zhiyangyun.care.operations.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class OperationsStaffMobileResponse {
  private String generatedAt;
  private String status;
  private Integer mobileIndex;
  private String mobileLevel;
  private List<StaffMobileMetric> metrics = new ArrayList<>();
  private List<StaffMobileRuntimeCard> runtimeCards = new ArrayList<>();
  private List<StaffMobileRoleCard> roleCards = new ArrayList<>();
  private List<StaffMobileWorkflow> workflows = new ArrayList<>();
  private List<StaffMobileAction> actions = new ArrayList<>();

  @Data
  public static class StaffMobileMetric {
    private String key;
    private String label;
    private String value;
    private String helper;
    private String tone;
    private String routePath;
  }

  @Data
  public static class StaffMobileRuntimeCard {
    private String key;
    private String title;
    private String value;
    private String status;
    private String helper;
    private String routePath;
  }

  @Data
  public static class StaffMobileRoleCard {
    private String key;
    private String title;
    private String owner;
    private String status;
    private String routePath;
    private List<String> scenarios = new ArrayList<>();
    private List<String> mobileNeeds = new ArrayList<>();
  }

  @Data
  public static class StaffMobileWorkflow {
    private String key;
    private String title;
    private String status;
    private String routePath;
    private List<String> steps = new ArrayList<>();
  }

  @Data
  public static class StaffMobileAction {
    private String title;
    private String owner;
    private String priority;
    private String routePath;
    private String description;
  }

  @Data
  public static class StaffMobileTask {
    private String id;
    private String module;
    private String title;
    private String resident;
    private String room;
    private String time;
    private String status;
    private String priority;
    private String actionText;
    private Boolean evidenceRequired;
    private List<String> checklist = new ArrayList<>();
    private String route;
  }

  @Data
  public static class StaffMobileSchedule {
    private String date;
    private String shift;
    private String time;
    private String area;
    private String status;
  }

  @Data
  public static class StaffMobileHandover {
    private String id;
    private String time;
    private String title;
    private String owner;
    private String content;
  }

  @Data
  public static class StaffMobilePatrolPoint {
    private String id;
    private String name;
    private String location;
    private String status;
  }

  @Data
  public static class StaffMobileReceipt {
    private String id;
    private String status;
    private String message;
    private String submittedAt;
  }

  @Data
  public static class StaffMobileTaskReceiptView {
    private String id;
    private String taskId;
    private String module;
    private String moduleText;
    private String taskTitle;
    private String resident;
    private String room;
    private String remark;
    private String scanText;
    private String checkedItems;
    private List<String> photos = new ArrayList<>();
    private String voiceUrl;
    private Integer voiceDurationSec;
    private String receiptTime;
    private String status;
    private String adminRoute;
    private String taskRoute;
  }

  @Data
  public static class StaffMobileCompleteTaskRequest {
    private String remark;
    private String scanText;
    private Map<String, Boolean> checkedMap;
    private List<String> photos = new ArrayList<>();
    private String voiceUrl;
    private Integer voiceDurationSec;
  }

  @Data
  public static class StaffMobileHandoverRequest {
    private String title;
    private String content;
    private String owner;
  }

  @Data
  public static class StaffMobileIncidentRequest {
    private String incidentType;
    private String level;
    private String resident;
    private String room;
    private String description;
    private String scanText;
    private List<String> photos = new ArrayList<>();
    private String voiceUrl;
    private Integer voiceDurationSec;
  }

  @Data
  public static class StaffMobileIncidentView {
    private String id;
    private String incidentType;
    private String level;
    private String resident;
    private String location;
    private String description;
    private String actionTaken;
    private String scanText;
    private List<String> photos = new ArrayList<>();
    private String voiceUrl;
    private Integer voiceDurationSec;
    private String incidentTime;
    private String status;
    private String adminRoute;
  }

  @Data
  public static class StaffMobilePatrolScanRequest {
    private String pointId;
    private String scanText;
    private String remark;
  }
}
