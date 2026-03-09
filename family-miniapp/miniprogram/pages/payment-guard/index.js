const { getPaymentGuard } = require('../../services/family');

function normalizeStatusClass(status) {
  if (status === 'PAID') return 'status-paid';
  if (status === 'PREPAY_CREATED') return 'status-pending';
  if (status === 'CLOSED') return 'status-closed';
  return 'status-failed';
}

function normalizeStatusText(statusText, status) {
  if (statusText) {
    return statusText;
  }
  if (status === 'PAID') return '已支付';
  if (status === 'PREPAY_CREATED') return '待支付';
  if (status === 'CLOSED') return '已关闭';
  if (status === 'FAILED') return '支付失败';
  return '处理中';
}

Page({
  data: {
    guard: null,
    loading: false
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  onPullDownRefresh() {
    this.loadData(true);
  },
  async loadData(fromPullDown = false) {
    this.setData({ loading: true });
    try {
      const guard = await getPaymentGuard();
      const recentOrders = (guard.recentOrders || []).map((item) => ({
        ...item,
        statusText: normalizeStatusText(item.statusText, item.status),
        statusClass: normalizeStatusClass(item.status)
      }));
      this.setData({
        guard: {
          ...guard,
          recentOrders
        }
      });
    } finally {
      this.setData({ loading: false });
      if (fromPullDown) {
        wx.stopPullDownRefresh();
      }
    }
  },
  toPayment() {
    wx.navigateTo({ url: '/pages/payment/index' });
  },
  goOrderDetail(e) {
    const outTradeNo = e.currentTarget.dataset.tradeNo;
    if (!outTradeNo) {
      return;
    }
    wx.navigateTo({
      url: `/pages/recharge-order/index?outTradeNo=${encodeURIComponent(outTradeNo)}`
    });
  },
  copyTradeNo(e) {
    const outTradeNo = e.currentTarget.dataset.tradeNo;
    if (!outTradeNo) {
      return;
    }
    wx.setClipboardData({
      data: outTradeNo,
      success: () => wx.showToast({ title: '订单号已复制', icon: 'success' })
    });
  }
});
