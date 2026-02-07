package com.zhiyangyun.care.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.store.entity.StoreOrder;
import com.zhiyangyun.care.store.mapper.StoreOrderMapper;
import com.zhiyangyun.care.store.model.OrderPreviewRequest;
import com.zhiyangyun.care.store.model.OrderPreviewResponse;
import com.zhiyangyun.care.store.model.OrderSubmitResponse;
import com.zhiyangyun.care.store.service.StoreOrderService;
import jakarta.validation.Valid;
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

  public StoreOrderController(StoreOrderService storeOrderService, StoreOrderMapper storeOrderMapper) {
    this.storeOrderService = storeOrderService;
    this.storeOrderMapper = storeOrderMapper;
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
  public Result<IPage<StoreOrder>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    Long orgId = AuthContext.getOrgId();
    LambdaQueryWrapper<StoreOrder> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(orgId != null, StoreOrder::getOrgId, orgId)
        .eq(StoreOrder::getIsDeleted, 0);
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(StoreOrder::getOrderNo, keyword)
          .or().like(StoreOrder::getElderId, keyword));
    }
    IPage<StoreOrder> page = storeOrderMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return Result.ok(page);
  }

  @GetMapping("/{id}")
  public Result<StoreOrder> get(@PathVariable Long id) {
    return Result.ok(storeOrderMapper.selectById(id));
  }
}
