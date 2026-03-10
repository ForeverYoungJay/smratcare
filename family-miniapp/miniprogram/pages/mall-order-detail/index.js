const {
  getMallOrderDetail,
  cancelMallOrder,
  refundMallOrder
} = require('../../services/family');

function safeNumber(value, fallback = 0) {
  const n = Number(value);
  return Number.isFinite(n) ? n : fallback;
}

Page({
  data: {
    orderId: 0,
    loading: false,
    acting: false,
    detail: null,
    actionReason: ''
  },
  onLoad(query) {
    const orderId = safeNumber(query && query.orderId, 0);
    this.setData({ orderId });
  },
  async onPullDownRefresh() {
    try {
      await this.loadDetail();
    } finally {
      wx.stopPullDownRefresh();
    }
  },
  async onShow() {
    if (!getApp().ensureLogin()) {
      return;
    }
    await this.loadDetail();
  },
  currentSummary() {
    const detail = this.data.detail || {};
    return detail.summary || null;
  },
  onReasonInput(e) {
    this.setData({ actionReason: e.detail.value });
  },
  async loadDetail() {
    if (!this.data.orderId) {
      wx.showToast({ title: '订单ID无效', icon: 'none' });
      return;
    }
    this.setData({ loading: true });
    try {
      const detail = await getMallOrderDetail(this.data.orderId);
      this.setData({ detail: detail || null });
    } catch (error) {
      wx.showToast({ title: error.message || '订单详情加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },
  async onCancelOrder() {
    if (this.data.acting || !this.data.orderId) {
      return;
    }
    const summary = this.currentSummary();
    if (!summary || !summary.canCancel) {
      wx.showToast({ title: (summary && summary.cancelHint) || '当前订单不可取消', icon: 'none' });
      return;
    }
    const confirm = await new Promise((resolve) => {
      wx.showModal({
        title: '确认取消订单',
        content: '取消后订单将关闭，是否继续？',
        success: resolve,
        fail: () => resolve({ confirm: false })
      });
    });
    if (!confirm || !confirm.confirm) {
      return;
    }
    this.setData({ acting: true });
    try {
      const actionResp = await cancelMallOrder(this.data.orderId, {
        reason: (this.data.actionReason || '').trim() || '家属端取消订单'
      });
      if (actionResp && actionResp.success === false) {
        throw new Error(actionResp.message || (actionResp.cancelHint || '当前订单不可取消'));
      }
      getApp().globalData.mallOrdersDirty = true;
      wx.showToast({ title: (actionResp && actionResp.message) || '订单已取消', icon: 'success' });
      await this.loadDetail();
    } catch (error) {
      wx.showToast({ title: error.message || '取消失败，请稍后重试', icon: 'none' });
    } finally {
      this.setData({ acting: false });
    }
  },
  async onRefundOrder() {
    if (this.data.acting || !this.data.orderId) {
      return;
    }
    const summary = this.currentSummary();
    if (!summary || !summary.canRefund) {
      wx.showToast({ title: (summary && summary.refundHint) || '当前订单不可退款', icon: 'none' });
      return;
    }
    const confirm = await new Promise((resolve) => {
      wx.showModal({
        title: '确认申请退款',
        content: '将发起退款处理，是否继续？',
        success: resolve,
        fail: () => resolve({ confirm: false })
      });
    });
    if (!confirm || !confirm.confirm) {
      return;
    }
    this.setData({ acting: true });
    try {
      const actionResp = await refundMallOrder(this.data.orderId, {
        reason: (this.data.actionReason || '').trim() || '家属端申请退款'
      });
      if (actionResp && actionResp.success === false) {
        throw new Error(actionResp.message || (actionResp.refundHint || '当前订单不可退款'));
      }
      getApp().globalData.mallOrdersDirty = true;
      wx.showToast({ title: (actionResp && actionResp.message) || '退款申请已提交', icon: 'success' });
      await this.loadDetail();
    } catch (error) {
      wx.showToast({ title: error.message || '退款失败，请稍后重试', icon: 'none' });
    } finally {
      this.setData({ acting: false });
    }
  }
});
