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
  const payPackage = params.payPackage || params.packageValue || params.package || '';
  if (!params.timeStamp || !params.nonceStr || !payPackage || !params.paySign) {
    return Promise.reject(new Error('微信支付参数不完整，请检查支付配置'));
  }
  return new Promise((resolve, reject) => {
    wx.requestPayment({
      timeStamp: params.timeStamp,
      nonceStr: params.nonceStr,
      package: payPackage,
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

function canUseMockFallback() {
  const app = getApp();
  return !!(app
    && app.globalData
    && app.globalData.useMockFallback
    && typeof app.isLocalDevEnvironment === 'function'
    && app.isLocalDevEnvironment());
}

function canUseManualRechargeFallback() {
  const app = getApp();
  return !!(app
    && typeof app.canUseManualRechargeFallback === 'function'
    && app.canUseManualRechargeFallback());
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
    loading: false,
    loadError: '',
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
    allowManualFallback: false,
    partialWarning: '',
    runtimeNotice: ''
  },
  onLoad(query) {
    const amount = Number(decodeURIComponent((query && query.amount) || ''));
    if (Number.isFinite(amount) && amount > 0) {
      const amountText = String(amount);
      this.setData({
        rechargeAmount: amountText,
        selectedPreset: PRESET_AMOUNTS.includes(amountText) ? amountText : ''
      });
    }
  },
  async onShow() {
    getApp().ensureLogin();
    this.setData({
      allowManualFallback: canUseManualRechargeFallback(),
      runtimeNotice: getApp().globalData.runtimeNotice || ''
    });
    await this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '', partialWarning: '' });
    try {
      const results = await Promise.allSettled([
        getBillSummary({ elderId: null }),
        getBillHistory({ elderId: null }),
        getRechargeOrders({ elderId: null, pageNo: 1, pageSize: 10 }),
        getPaymentGuard({ elderId: null })
      ]);
      const [summaryResult, historyResult, rechargeOrdersResult, guardResult] = results;
      if (summaryResult.status !== 'fulfilled') {
        throw summaryResult.reason;
      }
      const warnings = [];
      if (historyResult.status !== 'fulfilled' && !canUseMockFallback()) {
        warnings.push('历史账单');
      }
      if (rechargeOrdersResult.status !== 'fulfilled' && !canUseMockFallback()) {
        warnings.push('充值订单');
      }
      if (guardResult.status !== 'fulfilled' && !canUseMockFallback()) {
        warnings.push('支付保障');
      }
      const summary = summaryResult.value;
      const history = historyResult.status === 'fulfilled' ? historyResult.value : [];
      const rechargeOrders = rechargeOrdersResult.status === 'fulfilled' ? rechargeOrdersResult.value : [];
      const guard = guardResult.status === 'fulfilled' ? guardResult.value : null;
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
        previewBalance: previewBalance.toFixed(2),
        partialWarning: warnings.length ? `以下信息暂未同步成功：${warnings.join('、')}。可稍后下拉刷新重试。` : ''
      });
    } catch (error) {
      this.setData({
        loadError: error.message || '支付信息加载失败，请稍后重试',
        summary: null,
        guard: null,
        history: [],
        rechargeOrders: [],
        abnormalOrders: [],
        filteredOrders: []
      });
    } finally {
      this.setData({ loading: false });
    }
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
    const res = await rechargeBalance(amount, { elderId: null, method: 'MANUAL_FALLBACK', remark: '预下单失败回退人工充值' });
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
    if (!this.data.summary) {
      wx.showToast({ title: '账单尚未加载完成', icon: 'none' });
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
    const prepayPrompt = await new Promise((resolve) => {
      wx.showModal({
        title: '确认发起微信支付',
        content: '支付成功后余额会实时更新。如取消支付，可在“最近充值订单”中继续查看状态。',
        confirmText: '继续支付',
        cancelText: '取消',
        success: (res) => resolve(!!(res && res.confirm)),
        fail: () => resolve(false)
      });
    });
    if (!prepayPrompt) {
      return;
    }
    this.setData({ paying: true });
    wx.showLoading({ title: '创建支付单', mask: true });
    let outTradeNo = '';
    try {
      const loginCode = await wxLogin();
      const prepay = await createWechatRechargePrepay(amount, {
        elderId: null,
        loginCode,
        description: '家属端账户充值'
      });
      if (!prepay || !prepay.outTradeNo) {
        throw new Error('预下单失败');
      }
      outTradeNo = prepay.outTradeNo;
      wx.hideLoading();

      try {
        await wxRequestPayment(prepay);
      } catch (payError) {
        if (payError && payError.errMsg && payError.errMsg.includes('cancel')) {
          this.promptPendingOrder(outTradeNo);
          return;
        }
        throw payError;
      }

      const paidOrder = await this.waitRechargePaid(prepay.outTradeNo);
      if (paidOrder) {
        wx.showToast({ title: `充值成功 ¥${amount}`, icon: 'success' });
      } else {
        this.promptProcessingOrder(outTradeNo);
      }
    } catch (error) {
      if (this.data.allowManualFallback) {
        await this.doManualRecharge(amount);
      } else {
        this.promptRechargeFailure(error, outTradeNo);
      }
    } finally {
      wx.hideLoading();
      this.setData({ paying: false });
      await this.loadData();
    }
  },
  promptPendingOrder(outTradeNo) {
    wx.showModal({
      title: '支付未完成',
      content: '本次支付已取消，订单已保留为待支付状态。你可以在订单详情中查看状态或重新发起充值。',
      confirmText: '查看订单',
      cancelText: '知道了',
      success: (res) => {
        if (res.confirm && outTradeNo) {
          this.openOrderDetail(outTradeNo);
        }
      }
    });
  },
  promptProcessingOrder(outTradeNo) {
    wx.showModal({
      title: '支付确认中',
      content: '微信支付已提交，系统暂未确认到账。到账通常有延迟，可在订单详情中刷新查看最新状态。',
      confirmText: '查看订单',
      cancelText: '稍后再看',
      success: (res) => {
        if (res.confirm && outTradeNo) {
          this.openOrderDetail(outTradeNo);
        }
      }
    });
  },
  promptRechargeFailure(error, outTradeNo) {
    const message = (error && error.message) || '支付发起失败，请稍后重试';
    if (!outTradeNo) {
      wx.showToast({ title: message, icon: 'none' });
      return;
    }
    wx.showModal({
      title: '支付未成功',
      content: `${message}。订单已记录，可在订单详情查看原因并重新发起充值。`,
      confirmText: '查看订单',
      cancelText: '知道了',
      success: (res) => {
        if (res.confirm) {
          this.openOrderDetail(outTradeNo);
        }
      }
    });
  },
  openOrderDetail(outTradeNo) {
    wx.navigateTo({
      url: `/pages/recharge-order/index?outTradeNo=${encodeURIComponent(outTradeNo)}`
    });
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
  retryLoad() {
    this.loadData();
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
      await toggleAutoPay(enabled, authorizeConfirmed, { elderId: null });
      this.setData({ 'summary.autoPayEnabled': enabled });
      wx.showToast({ title: enabled ? '自动扣费已开启' : '自动扣费已关闭', icon: 'none' });
    } catch (error) {
      wx.showToast({ title: error.message || '设置失败', icon: 'none' });
      this.setData({ 'summary.autoPayEnabled': !enabled });
    }
  }
});
