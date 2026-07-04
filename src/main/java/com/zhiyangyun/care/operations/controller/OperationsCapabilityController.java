package com.zhiyangyun.care.operations.controller;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.operations.model.OperationsCapabilityMapResponse;
import com.zhiyangyun.care.operations.model.OperationsFamilyServiceResponse;
import com.zhiyangyun.care.operations.model.OperationsIntelligenceResponse;
import com.zhiyangyun.care.operations.model.OperationsLogisticsResponse;
import com.zhiyangyun.care.operations.model.OperationsMarketingResponse;
import com.zhiyangyun.care.operations.model.OperationsSafetyRiskResponse;
import com.zhiyangyun.care.operations.model.OperationsServiceReportResponse;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileCompleteTaskRequest;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileHandover;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileHandoverRequest;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileIncidentRequest;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileIncidentView;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobilePatrolPoint;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobilePatrolScanRequest;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileReceipt;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileSchedule;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileTask;
import com.zhiyangyun.care.operations.model.OperationsStaffMobileResponse.StaffMobileTaskReceiptView;
import com.zhiyangyun.care.operations.model.OperationsWorkforceResponse;
import com.zhiyangyun.care.operations.service.OperationsCapabilityService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operations")
public class OperationsCapabilityController {
  private final OperationsCapabilityService operationsCapabilityService;

  public OperationsCapabilityController(OperationsCapabilityService operationsCapabilityService) {
    this.operationsCapabilityService = operationsCapabilityService;
  }

  @GetMapping("/capability-map")
  public Result<OperationsCapabilityMapResponse> capabilityMap() {
    return Result.ok(operationsCapabilityService.capabilityMap());
  }

  @GetMapping("/service-report")
  public Result<OperationsServiceReportResponse> serviceReport(@RequestParam(required = false) String month) {
    return Result.ok(operationsCapabilityService.serviceReport(month));
  }

  @GetMapping("/family-service")
  public Result<OperationsFamilyServiceResponse> familyService() {
    return Result.ok(operationsCapabilityService.familyService());
  }

  @GetMapping("/intelligence")
  public Result<OperationsIntelligenceResponse> intelligence() {
    return Result.ok(operationsCapabilityService.intelligence());
  }

  @GetMapping("/safety-risk")
  public Result<OperationsSafetyRiskResponse> safetyRisk() {
    return Result.ok(operationsCapabilityService.safetyRisk());
  }

  @GetMapping("/workforce")
  public Result<OperationsWorkforceResponse> workforce() {
    return Result.ok(operationsCapabilityService.workforce());
  }

  @GetMapping("/staff-mobile")
  public Result<OperationsStaffMobileResponse> staffMobile() {
    return Result.ok(operationsCapabilityService.staffMobile());
  }

  @GetMapping("/staff-mobile/tasks")
  public Result<List<StaffMobileTask>> staffMobileTasks(@RequestParam(required = false) String module) {
    return Result.ok(operationsCapabilityService.staffMobileTasks(module));
  }

  @GetMapping("/staff-mobile/tasks/{id}")
  public Result<StaffMobileTask> staffMobileTaskDetail(@PathVariable String id) {
    return Result.ok(operationsCapabilityService.staffMobileTaskDetail(id));
  }

  @PostMapping("/staff-mobile/tasks/{id}/complete")
  public Result<StaffMobileReceipt> completeStaffMobileTask(
      @PathVariable String id,
      @RequestBody(required = false) StaffMobileCompleteTaskRequest request) {
    return Result.ok(operationsCapabilityService.completeStaffMobileTask(id, request));
  }

  @GetMapping("/staff-mobile/receipts")
  public Result<List<StaffMobileTaskReceiptView>> staffMobileTaskReceipts(@RequestParam(required = false) String module) {
    return Result.ok(operationsCapabilityService.staffMobileTaskReceipts(module));
  }

  @GetMapping("/staff-mobile/schedule")
  public Result<List<StaffMobileSchedule>> staffMobileSchedule() {
    return Result.ok(operationsCapabilityService.staffMobileSchedule());
  }

  @GetMapping("/staff-mobile/handovers")
  public Result<List<StaffMobileHandover>> staffMobileHandovers() {
    return Result.ok(operationsCapabilityService.staffMobileHandovers());
  }

  @PostMapping("/staff-mobile/handovers")
  public Result<StaffMobileHandover> submitStaffMobileHandover(
      @RequestBody(required = false) StaffMobileHandoverRequest request) {
    return Result.ok(operationsCapabilityService.submitStaffMobileHandover(request));
  }

  @GetMapping("/staff-mobile/patrol-points")
  public Result<List<StaffMobilePatrolPoint>> staffMobilePatrolPoints() {
    return Result.ok(operationsCapabilityService.staffMobilePatrolPoints());
  }

  @PostMapping("/staff-mobile/patrol-scan")
  public Result<StaffMobileReceipt> submitStaffMobilePatrolScan(
      @RequestBody(required = false) StaffMobilePatrolScanRequest request) {
    return Result.ok(operationsCapabilityService.submitStaffMobilePatrolScan(request));
  }

  @PostMapping("/staff-mobile/incidents")
  public Result<StaffMobileReceipt> submitStaffMobileIncident(
      @RequestBody(required = false) StaffMobileIncidentRequest request) {
    return Result.ok(operationsCapabilityService.submitStaffMobileIncident(request));
  }

  @GetMapping("/staff-mobile/incidents")
  public Result<List<StaffMobileIncidentView>> staffMobileIncidents(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String level) {
    return Result.ok(operationsCapabilityService.staffMobileIncidents(status, level));
  }

  @GetMapping("/logistics")
  public Result<OperationsLogisticsResponse> logistics() {
    return Result.ok(operationsCapabilityService.logistics());
  }

  @GetMapping("/marketing")
  public Result<OperationsMarketingResponse> marketing() {
    return Result.ok(operationsCapabilityService.marketing());
  }
}
