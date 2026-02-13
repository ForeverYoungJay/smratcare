package com.zhiyangyun.care.store.service;

import com.zhiyangyun.care.store.model.OrderCancelRequest;
import com.zhiyangyun.care.store.model.OrderPreviewRequest;
import com.zhiyangyun.care.store.model.OrderPreviewResponse;
import com.zhiyangyun.care.store.model.OrderRefundRequest;
import com.zhiyangyun.care.store.model.OrderFulfillRequest;
import com.zhiyangyun.care.store.model.OrderDetailResponse;
import com.zhiyangyun.care.store.model.OrderSubmitResponse;

public interface StoreOrderService {
  OrderPreviewResponse preview(OrderPreviewRequest request);

  OrderSubmitResponse submit(OrderPreviewRequest request);

  void cancel(OrderCancelRequest request);

  void refund(OrderRefundRequest request);

  void fulfill(OrderFulfillRequest request);

  OrderDetailResponse getOrderDetail(Long orderId);
}
