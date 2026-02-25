package com.zhiyangyun.care.dashboard.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DashboardSummary {
  private long careTasksToday;
  private long abnormalTasksToday;
  private long inventoryAlerts;
  private long unpaidBills;
  private long totalAdmissions;
  private long totalDischarges;
  private long checkInNetIncrease;
  private BigDecimal dischargeToAdmissionRate;
  private BigDecimal totalBillConsumption;
  private BigDecimal totalStoreConsumption;
  private BigDecimal totalConsumption;
  private BigDecimal averageMonthlyConsumption;
  private BigDecimal billConsumptionRatio;
  private BigDecimal storeConsumptionRatio;
  private long inHospitalCount;
  private long dischargedCount;
  private long totalBeds;
  private long occupiedBeds;
  private long availableBeds;
  private BigDecimal bedOccupancyRate;
  private BigDecimal bedAvailableRate;
  private BigDecimal totalRevenue;
  private BigDecimal averageMonthlyRevenue;
  private BigDecimal revenueGrowthRate;
}
