package com.zhiyangyun.care.oa.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class OaMyInfoItemResponse {
  private Long staffId;
  private String staffNo;
  private String staffName;
  private String username;
  private String phone;
  private Long departmentId;
  private String departmentName;
  private String jobTitle;
  private LocalDate hireDate;
  private LocalDate probationEndDate;
  private Long probationRemainingDays;
  private LocalDate contractSignDueDate;
  private Long contractSignRemainingDays;
  private String salarySource;
  private BigDecimal currentMonthSalary;
  private BigDecimal previousMonthSalary;
  private BigDecimal nextMonthSalary;
  private Integer taskOpenCount;
  private Integer taskOverdueCount;
  private Integer taskCompletedThisMonth;
  private List<String> dutyTasks;
  private Double performanceAvgScore;
  private Integer performancePointsBalance;
  private Integer performanceRedeemableCash;
  private Integer performanceSuccessCount;
  private Integer reimbursePendingCount;
  private Integer reimburseMonthCount;
  private BigDecimal reimburseMonthAmount;
  private String electricityMonth;
  private BigDecimal electricityAmount;
  private String meterNo;
  private String dormitoryLabel;
}
