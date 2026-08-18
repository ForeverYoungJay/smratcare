package com.zhiyangyun.care.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.finance.entity.FinanceElectricityPrice;
import com.zhiyangyun.care.finance.model.ElectricityMonthSummary;
import com.zhiyangyun.care.finance.model.ElectricityPriceRequest;
import com.zhiyangyun.care.finance.model.ElectricityReadingRequest;
import com.zhiyangyun.care.finance.model.ElectricityReadingView;
import com.zhiyangyun.care.finance.service.ElectricityFeeService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/electricity")
@PreAuthorize("hasAnyRole('FINANCE_EMPLOYEE','FINANCE_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class ElectricityFeeController {
  private final ElectricityFeeService electricityFeeService;
  private final AuditLogService auditLogService;

  public ElectricityFeeController(ElectricityFeeService electricityFeeService, AuditLogService auditLogService) {
    this.electricityFeeService = electricityFeeService;
    this.auditLogService = auditLogService;
  }

  @GetMapping("/prices")
  public Result<List<FinanceElectricityPrice>> prices() {
    return Result.ok(electricityFeeService.priceList());
  }

  @PostMapping("/prices")
  public Result<FinanceElectricityPrice> savePrice(@Valid @RequestBody ElectricityPriceRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    FinanceElectricityPrice price = electricityFeeService.savePrice(request, staffId);
    auditLogService.record(
        orgId, orgId, staffId, AuthContext.getUsername(),
        "FIN_ELECTRICITY_PRICE_SAVE", "FINANCE_ELECTRICITY_PRICE", price.getId(),
        "设置电价 " + price.getUnitPrice() + " 元/度 生效自 " + price.getEffectiveFrom());
    return Result.ok(price);
  }

  @GetMapping("/page")
  public Result<IPage<ElectricityReadingView>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String billMonth,
      @RequestParam(required = false) String building,
      @RequestParam(required = false) String roomNo,
      @RequestParam(required = false) String payStatus) {
    return Result.ok(electricityFeeService.page(pageNo, pageSize, billMonth, building, roomNo, payStatus));
  }

  @GetMapping("/summary")
  public Result<ElectricityMonthSummary> summary(@RequestParam(required = false) String billMonth) {
    return Result.ok(electricityFeeService.summary(billMonth));
  }

  @GetMapping("/previous-reading")
  public Result<Map<String, BigDecimal>> previousReading(
      @RequestParam(required = false) String billMonth,
      @RequestParam Long roomId) {
    return Result.ok(Map.of(
        "previousReading", electricityFeeService.suggestPreviousReading(billMonth, roomId)));
  }

  @PostMapping("/readings")
  public Result<ElectricityReadingView> saveReading(@Valid @RequestBody ElectricityReadingRequest request) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    ElectricityReadingView view = electricityFeeService.saveReading(request, staffId);
    auditLogService.record(
        orgId, orgId, staffId, AuthContext.getUsername(),
        "FIN_ELECTRICITY_READING_SAVE", "FINANCE_ELECTRICITY_READING", view.getId(),
        "登记电表读数 " + view.getBillMonth() + " 房间=" + view.getRoomNo()
            + " 上期=" + view.getPreviousReading() + " 本期=" + view.getCurrentReading()
            + " 用电量=" + view.getUsageAmount() + " 电费=" + view.getFeeAmount());
    return Result.ok(view);
  }

  @PostMapping("/readings/{readingId}/pay-status")
  public Result<ElectricityReadingView> updatePayStatus(
      @PathVariable Long readingId,
      @RequestParam boolean paid,
      @RequestParam(required = false) String payRemark) {
    Long orgId = AuthContext.getOrgId();
    Long staffId = AuthContext.getStaffId();
    ElectricityReadingView view = electricityFeeService.updatePayStatus(readingId, paid, payRemark, staffId);
    auditLogService.record(
        orgId, orgId, staffId, AuthContext.getUsername(),
        "FIN_ELECTRICITY_PAY_STATUS", "FINANCE_ELECTRICITY_READING", readingId,
        "电费标记" + (paid ? "已缴" : "未缴") + " 房间=" + view.getRoomNo() + " 账期=" + view.getBillMonth());
    return Result.ok(view);
  }
}
