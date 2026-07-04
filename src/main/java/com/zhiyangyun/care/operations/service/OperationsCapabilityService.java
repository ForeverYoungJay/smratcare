package com.zhiyangyun.care.operations.service;

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
import java.util.List;

public interface OperationsCapabilityService {
  OperationsCapabilityMapResponse capabilityMap();

  OperationsServiceReportResponse serviceReport(String month);

  OperationsFamilyServiceResponse familyService();

  OperationsIntelligenceResponse intelligence();

  OperationsSafetyRiskResponse safetyRisk();

  OperationsWorkforceResponse workforce();

  OperationsStaffMobileResponse staffMobile();

  List<StaffMobileTask> staffMobileTasks(String module);

  StaffMobileTask staffMobileTaskDetail(String id);

  StaffMobileReceipt completeStaffMobileTask(String id, StaffMobileCompleteTaskRequest request);

  List<StaffMobileTaskReceiptView> staffMobileTaskReceipts(String module);

  List<StaffMobileSchedule> staffMobileSchedule();

  List<StaffMobileHandover> staffMobileHandovers();

  StaffMobileHandover submitStaffMobileHandover(StaffMobileHandoverRequest request);

  List<StaffMobilePatrolPoint> staffMobilePatrolPoints();

  StaffMobileReceipt submitStaffMobilePatrolScan(StaffMobilePatrolScanRequest request);

  StaffMobileReceipt submitStaffMobileIncident(StaffMobileIncidentRequest request);

  List<StaffMobileIncidentView> staffMobileIncidents(String status, String level);

  OperationsLogisticsResponse logistics();

  OperationsMarketingResponse marketing();
}
