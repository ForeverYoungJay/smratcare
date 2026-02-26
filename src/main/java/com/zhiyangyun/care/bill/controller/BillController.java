package com.zhiyangyun.care.bill.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.bill.model.BillItemDetail;
import com.zhiyangyun.care.bill.model.BillDetailResponse;
import com.zhiyangyun.care.bill.model.BillGenerateResponse;
import com.zhiyangyun.care.bill.model.BillMonthlyView;
import com.zhiyangyun.care.bill.service.BillService;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.finance.entity.PaymentRecord;
import com.zhiyangyun.care.finance.mapper.PaymentRecordMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bill")
public class BillController {
  private final BillService billService;
  private final BillMonthlyMapper billMonthlyMapper;
  private final ElderMapper elderMapper;
  private final PaymentRecordMapper paymentRecordMapper;

  public BillController(BillService billService,
      BillMonthlyMapper billMonthlyMapper,
      ElderMapper elderMapper,
      PaymentRecordMapper paymentRecordMapper) {
    this.billService = billService;
    this.billMonthlyMapper = billMonthlyMapper;
    this.elderMapper = elderMapper;
    this.paymentRecordMapper = paymentRecordMapper;
  }

  @PostMapping("/generate")
  public BillGenerateResponse generate(
      @RequestParam @Pattern(regexp = "\\d{4}-\\d{2}") String month) {
    return billService.generateMonthlyBills(month);
  }

  @GetMapping("/{elderId}")
  public BillDetailResponse detail(
      @PathVariable Long elderId,
      @RequestParam @Pattern(regexp = "\\d{4}-\\d{2}") String month) {
    return billService.getBillDetail(elderId, month);
  }

  @GetMapping("/page")
  public Result<IPage<BillMonthlyView>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String month,
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    var wrapper = Wrappers.lambdaQuery(BillMonthly.class)
        .eq(BillMonthly::getIsDeleted, 0)
        .eq(orgId != null, BillMonthly::getOrgId, orgId);
    if (month != null && !month.isBlank()) {
      wrapper.eq(BillMonthly::getBillMonth, month);
    }
    if (elderId != null) {
      wrapper.eq(BillMonthly::getElderId, elderId);
    }
    if (keyword != null && !keyword.isBlank()) {
      List<Long> elderIds = elderMapper.selectList(
              Wrappers.lambdaQuery(ElderProfile.class)
                  .eq(ElderProfile::getIsDeleted, 0)
                  .eq(orgId != null, ElderProfile::getOrgId, orgId)
                  .like(ElderProfile::getFullName, keyword))
          .stream().map(ElderProfile::getId).toList();
      if (elderIds.isEmpty()) {
        return Result.ok(new Page<>(pageNo, pageSize, 0));
      }
      wrapper.in(BillMonthly::getElderId, elderIds);
    }
    wrapper.orderByDesc(BillMonthly::getBillMonth);
    IPage<BillMonthly> page = billMonthlyMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> elderIds = page.getRecords().stream()
        .map(BillMonthly::getElderId)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderMapper.selectList(
            Wrappers.lambdaQuery(ElderProfile.class)
                .in(ElderProfile::getId, elderIds))
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, Function.identity(), (a, b) -> a));

    Map<Long, String> payMethodMap = new HashMap<>();
    if (!page.getRecords().isEmpty()) {
      List<Long> billIds = page.getRecords().stream().map(BillMonthly::getId).toList();
      List<PaymentRecord> payRecords = paymentRecordMapper.selectList(
          Wrappers.lambdaQuery(PaymentRecord.class)
              .eq(PaymentRecord::getIsDeleted, 0)
              .in(PaymentRecord::getBillMonthlyId, billIds)
              .orderByDesc(PaymentRecord::getPaidAt)
              .orderByDesc(PaymentRecord::getCreateTime));
      for (PaymentRecord payRecord : payRecords) {
        if (payMethodMap.containsKey(payRecord.getBillMonthlyId())) {
          continue;
        }
        payMethodMap.put(payRecord.getBillMonthlyId(), payRecord.getPayMethod());
      }
    }

    IPage<BillMonthlyView> viewPage = new Page<>(pageNo, pageSize);
    viewPage.setTotal(page.getTotal());
    viewPage.setRecords(page.getRecords().stream().map(record -> {
      BillMonthlyView view = new BillMonthlyView();
      view.setId(record.getId());
      view.setOrgId(record.getOrgId());
      view.setElderId(record.getElderId());
      ElderProfile elder = elderMap.get(record.getElderId());
      view.setElderName(elder == null ? null : elder.getFullName());
      view.setCareLevel(elder == null ? null : elder.getCareLevel());
      view.setBillMonth(record.getBillMonth());
      view.setTotalAmount(record.getTotalAmount());
      Map<String, BigDecimal> feeMap = calculateFees(record.getId());
      view.setNursingFee(feeMap.getOrDefault("NURSING", BigDecimal.ZERO));
      view.setBedFee(feeMap.getOrDefault("BED", BigDecimal.ZERO));
      view.setInsuranceFee(feeMap.getOrDefault("INSURANCE", BigDecimal.ZERO));
      view.setPaidAmount(record.getPaidAmount());
      view.setOutstandingAmount(record.getOutstandingAmount());
      view.setLastPayMethod(payMethodMap.get(record.getId()));
      view.setStatus(record.getStatus());
      view.setCreateTime(record.getCreateTime());
      view.setUpdateTime(record.getUpdateTime());
      return view;
    }).toList());
    return Result.ok(viewPage);
  }

  private Map<String, BigDecimal> calculateFees(Long billId) {
    BillDetailResponse detail = billService.getBillDetailById(billId);
    Map<String, BigDecimal> map = new HashMap<>();
    List<BillItemDetail> items = detail == null ? new ArrayList<>() : detail.getItems();
    for (BillItemDetail item : items) {
      if (item == null || item.getItemType() == null) {
        continue;
      }
      map.merge(item.getItemType().toUpperCase(), item.getAmount() == null ? BigDecimal.ZERO : item.getAmount(), BigDecimal::add);
    }
    return map;
  }
}
