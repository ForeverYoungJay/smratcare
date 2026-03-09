const {
  getBillSummary,
  getBillHistory,
  getPaymentGuard,
  toggleAutoPay,
  rechargeBalance,
  createWechatRechargePrepay,
  getRechargeOrder,
  getRechargeOrders
} = require('../../services/family');

const PRESET_AMOUNTS = ['200', '500', '1000', '2000'];
const ORDER_FILTERS = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '待支付' },
  { key: 'paid', label: '已支付' },
  { key: 'abnormal', label: '异常' }
];

function wxLogin() {
  return new Promise((resolve, reject) => {
    wx.login({
      success: (res) => {
        if (res.code) {
          resolve(res.code);
          return;
        }
        reject(new Error('登录态无效'));
      },
      fail: reject
    });
  });
}

function wxRequestPayment(params) {
  return new Promise((resolve, reject) => {
    wx.requestPayment({
      timeStamp: params.timeStamp,
      nonceStr: params.nonceStr,
      package: params.payPackage,
      signType: params.signType || 'RSA',
      paySign: params.paySign,
      success: resolve,
      fail: reject
    });
  });
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function toMoney(value) {
  const num = Number(value || 0);
  if (Number.isNaN(num)) {
    return 0;
  }
  return Number(num.toFixed(2));
}

function matchOrderFilter(order, filterKey) {
  const status = (order && order.status) || '';
  if (filterKey === 'pending') {
    return status === 'PREPAY_CREATED' || status === 'INIT';
  }
  if (filterKey === 'paid') {
    return status === 'PAID';
  }
  if (filterKey === 'abnormal') {
    return status === 'CLOSED' || status === 'FAILED';
  }
  return true;
}

Page({
  data: {
    summary: null,
    guard: null,
    history: [],
    rechargeAmount: '500',
    presetAmounts: PRESET_AMOUNTS,
    selectedPreset: '500',
    rechargeOrders: [],
    filteredOrders: [],
    abnormalOrders: [],
    orderFilters: ORDER_FILTERS,
    orderFilter: 'all',
    previewBalance: '0.00',
    paying: false,
    allowManualFallback: false
  },
  async onShow() {
    getApp().ensureLogin();
    this.setData({ allowManualFallback: !!getApp().globalData.allowManualRechargeFallback });
    await this.loadData();
  },
  async loadData() {
    const [summary, history, rechargeOrders, guard] = await Promise.all([
      getBillSummary(),
      getBillHistory(),
      getRechargeOrders({ pageNo: 1, pageSize: 10 }),
      getPaymentGuard()
    ]);
    const orderList = rechargeOrders || [];
    const abnormalOrders = orderList.filter((item) => item && item.status !== 'PAID');
    const filteredOrders = orderList.filter((item) => matchOrderFilter(item, this.data.orderFilter));
    const previewBalance = toMoney((summary && summary.accountBalance) || 0) + toMoney(this.data.rechargeAmount || 0);
    const selectedPreset = PRESET_AMOUNTS.includes(this.data.rechargeAmount) ? this.data.rechargeAmount : '';
    this.setData({
      summary,
      guard,
      history: history || [],
      rechargeOrders: orderList,
      abnormalOrders,
      filteredOrders,
      selectedPreset,
      previewBalance: previewBalance.toFixed(2)
    });
  },
  onRechargeInput(e) {
    const rechargeAmount = e.detail.value;
    this.setData({
      rechargeAmount,
      selectedPreset: '',
      previewBalance: (toMoney((this.data.summary && this.data.summary.accountBalance) || 0) + toMoney(rechargeAmount)).toFixed(2)
    });
  },
  pickPreset(e) {
    const amount = e.currentTarget.dataset.amount || '500';
    this.setData({
      rechargeAmount: amount,
      selectedPreset: amount,
      previewBalance: (toMoney((this.data.summary && this.data.summary.accountBalance) || 0) + toMoney(amount)).toFixed(2)
    });
  },
  switchOrderFilter(e) {
    const orderFilter = e.currentTarget.dataset.filter;
    if (!orderFilter || orderFilter === this.data.orderFilter) {
      return;
    }
    const filteredOrders = (this.data.rechargeOrders || []).filter((item) => matchOrderFilter(item, orderFilter));
    this.setData({ orderFilter, filteredOrders });
  },
  async waitRechargePaid(outTradeNo) {
    for (let i = 0; i < 6; i += 1) {
      const order = await getRechargeOrder(outTradeNo);
      if (order && order.status === 'PAID') {
        return order;
      }
      await sleep(1200);
    }
    return null;
  },
  async doManualRecharge(amount) {
    const res = await rechargeBalance(amount, { method: 'MANUAL_FALLBACK', remark: '预下单失败回退人工充值' });
    if (!res) {
      wx.showToast({ title: '充值失败，请重试', icon: 'none' });
      return;
    }
    wx.showToast({ title: `充值成功 ¥${amount}`, icon: 'success' });
  },
  confirmLargeRecharge(amount) {
    if (amount < 2000) {
      return Promise.resolve(true);
    }
    return new Promise((resolve) => {
      wx.showModal({
        title: '大额充值确认',
        content: `本次将充值 ¥${amount.toFixed(2)}，请确认金额无误后继续。`,
        confirmText: '确认充值',
        cancelText: '再想想',
        success: (res) => resolve(!!res.confirm),
        fail: () => resolve(false)
      });
    });
  },
  async rechargeNow() {
    if (this.data.paying) {
      return;
    }
    const amount = Number(this.data.rechargeAmount || 0);
    if (Number.isNaN(amount) || amount <= 0) {
      wx.showToast({ title: '请输入正确充值金额', icon: 'none' });
      return;
    }
    const confirmed = await this.confirmLargeRecharge(amount);
    if (!confirmed) {
      return;
    }
    this.setData({ paying: true });
    wx.showLoading({ title: '创建支付单', mask: true });
    try {
      const loginCode = await wxLogin();
      const prepay = await createWechatRechargePrepay(amount, {
        loginCode,
        description: '家属端账户充值'
      });
      if (!prepay || !prepay.outTradeNo) {
        throw new Error('预下单失败');
      }

      try {
        await wxRequestPayment(prepay);
      } catch (payError) {
        if (payError && payError.errMsg && payError.errMsg.includes('cancel')) {
          wx.showToast({ title: '已取消支付', icon: 'none' });
          return;
        }
        throw payError;
      }

      const paidOrder = await this.waitRechargePaid(prepay.outTradeNo);
      if (paidOrder) {
        wx.showToast({ title: `充值成功 ¥${amount}`, icon: 'success' });
      } else {
        wx.showToast({ title: '支付处理中，请稍后刷新', icon: 'none' });
      }
    } catch (error) {
      if (this.data.allowManualFallback) {
        await this.doManualRecharge(amount);
      } else {
        wx.showToast({ title: error.message || '支付发起失败，请稍后重试', icon: 'none' });
      }
    } finally {
      wx.hideLoading();
      this.setData({ paying: false });
      await this.loadData();
    }
  },
  toOrderDetail(e) {
    const outTradeNo = e.currentTarget.dataset.tradeNo;
    if (!outTradeNo) {
      return;
    }
    wx.navigateTo({
      url: `/pages/recharge-order/index?outTradeNo=${encodeURIComponent(outTradeNo)}`
    });
  },
  toFirstAbnormalOrder() {
    const first = (this.data.abnormalOrders || [])[0];
    if (!first || !first.outTradeNo) {
      wx.showToast({ title: '暂无异常订单', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: `/pages/recharge-order/index?outTradeNo=${encodeURIComponent(first.outTradeNo)}`
    });
  },
  goPaymentGuard() {
    wx.navigateTo({ url: '/pages/payment-guard/index' });
  },
  async onAutoPaySwitch(e) {
    const enabled = !!e.detail.value;
    let authorizeConfirmed = false;
    if (enabled && !(this.data.summary && this.data.summary.autoPayEnabled)) {
      authorizeConfirmed = await new Promise((resolve) => {
        wx.showModal({
          title: '开启自动扣费',
          content: '开启后将优先使用账户余额结算待缴账单，是否确认授权？',
          confirmText: '确认授权',
          cancelText: '取消',
          success: (res) => resolve(!!res.confirm),
          fail: () => resolve(false)
        });
      });
      if (!authorizeConfirmed) {
        this.setData({ 'summary.autoPayEnabled': false });
        return;
      }
    }
    try {
      await toggleAutoPay(enabled, authorizeConfirmed);
      this.setData({ 'summary.autoPayEnabled': enabled });
      wx.showToast({ title: enabled ? '自动扣费已开启' : '自动扣费已关闭', icon: 'none' });
    } catch (error) {
      wx.showToast({ title: error.message || '设置失败', icon: 'none' });
      this.setData({ 'summary.autoPayEnabled': !enabled });
    }
  }
});
