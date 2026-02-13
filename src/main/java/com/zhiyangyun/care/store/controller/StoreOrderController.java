package com.zhiyangyun.care.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.store.entity.StoreOrder;
import com.zhiyangyun.care.store.entity.ElderProfile;
import com.zhiyangyun.care.store.mapper.ElderProfileMapper;
import com.zhiyangyun.care.store.model.OrderDetailResponse;
import com.zhiyangyun.care.store.model.StoreOrderView;
import com.zhiyangyun.care.store.mapper.StoreOrderMapper;
import com.zhiyangyun.care.store.model.OrderPreviewRequest;
import com.zhiyangyun.care.store.model.OrderPreviewResponse;
import com.zhiyangyun.care.store.model.OrderSubmitResponse;
import com.zhiyangyun.care.store.service.StoreOrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store/order")
public class StoreOrderController {
  private final StoreOrderService storeOrderService;
  private final StoreOrderMapper storeOrderMapper;
  private final ElderProfileMapper elderProfileMapper;

  public StoreOrderController(StoreOrderService storeOrderService, StoreOrderMapper storeOrderMapper,
      ElderProfileMapper elderProfileMapper) {
    this.storeOrderService = storeOrderService;
    this.storeOrderMapper = storeOrderMapper;
    this.elderProfileMapper = elderProfileMapper;
  }

  @PostMapping("/preview")
  public OrderPreviewResponse preview(@Valid @RequestBody OrderPreviewRequest request) {
    return storeOrderService.preview(request);
  }

  @PostMapping("/submit")
  public OrderSubmitResponse submit(@Valid @RequestBody OrderPreviewRequest request) {
    return storeOrderService.submit(request);
  }

  @GetMapping("/page")
  public Result<IPage<StoreOrderView>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer orderStatus,
      @RequestParam(required = false) Integer status) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<StoreOrder> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(orgId != null, StoreOrder::getOrgId, orgId)
        .eq(StoreOrder::getIsDeleted, 0);
    if (keyword != null && !keyword.isBlank()) {
      List<Long> elderIds = elderProfileMapper.selectList(
              Wrappers.lambdaQuery(ElderProfile.class)
                  .eq(ElderProfile::getIsDeleted, 0)
                  .eq(orgId != null, ElderProfile::getOrgId, orgId)
                  .like(ElderProfile::getFullName, keyword))
          .stream().map(ElderProfile::getId).toList();
      wrapper.and(w -> w.like(StoreOrder::getOrderNo, keyword)
          .or().in(!elderIds.isEmpty(), StoreOrder::getElderId, elderIds));
      if (elderIds.isEmpty()) {
        // only keep orderNo keyword filter
      }
    }
    if (orderStatus == null && status != null) {
      orderStatus = status;
    }
    if (orderStatus != null) {
      wrapper.eq(StoreOrder::getOrderStatus, orderStatus);
    }
    IPage<StoreOrder> page = storeOrderMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    List<Long> elderIds = page.getRecords().stream()
        .map(StoreOrder::getElderId)
        .distinct()
        .toList();
    Map<Long, ElderProfile> elderMap = elderIds.isEmpty()
        ? Map.of()
        : elderProfileMapper.selectList(
            Wrappers.lambdaQuery(ElderProfile.class)
                .in(ElderProfile::getId, elderIds))
            .stream()
            .collect(Collectors.toMap(ElderProfile::getId, e -> e, (a, b) -> a));
    IPage<StoreOrderView> viewPage = new Page<>(pageNo, pageSize);
    viewPage.setTotal(page.getTotal());
    viewPage.setRecords(page.getRecords().stream().map(order -> {
      StoreOrderView view = new StoreOrderView();
      view.setId(order.getId());
      view.setOrderNo(order.getOrderNo());
      view.setElderId(order.getElderId());
      ElderProfile elder = elderMap.get(order.getElderId());
      view.setElderName(elder == null ? null : elder.getFullName());
      view.setTotalAmount(order.getTotalAmount());
      view.setPayableAmount(order.getPayableAmount());
      view.setPointsUsed(order.getPointsUsed());
      view.setOrderStatus(order.getOrderStatus());
      view.setPayStatus(order.getPayStatus());
      view.setPayTime(order.getPayTime());
      view.setCreateTime(order.getCreateTime());
      return view;
    }).toList());
    return Result.ok(viewPage);
  }

  @GetMapping("/{id}")
  public Result<OrderDetailResponse> get(@PathVariable Long id) {
    return Result.ok(storeOrderService.getOrderDetail(id));
  }
}
