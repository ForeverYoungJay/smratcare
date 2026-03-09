const { getRechargeOrder, getEmergencyContacts } = require('../../services/family');

function resolveStatusClass(status) {
  if (status === 'PAID') {
    return 'status-paid';
  }
  if (status === 'PREPAY_CREATED') {
    return 'status-pending';
  }
  if (status === 'CLOSED') {
    return 'status-closed';
  }
  if (status === 'FAILED') {
    return 'status-failed';
  }
  return 'status-default';
}

function buildTimeline(order) {
  if (!order) {
    return [];
  }
  const items = [
    {
      id: `${order.outTradeNo}-created`,
      time: order.createTime || '--',
      title: '订单创建',
      desc: `已发起${order.channel || '微信支付'}充值申请`
    }
  ];
  if (order.status === 'PREPAY_CREATED') {
    items.push({
      id: `${order.outTradeNo}-pending`,
      time: order.createTime || '--',
      title: '等待支付',
      desc: '请在微信支付弹窗完成付款，超时订单将自动关闭'
    });
  } else if (order.status === 'PAID') {
    items.push({
      id: `${order.outTradeNo}-paid`,
      time: order.paidTime || '--',
      title: '充值到账',
      desc: '金额已计入老人账户余额，可用于服务消费'
    });
  } else if (order.status === 'CLOSED') {
    items.push({
      id: `${order.outTradeNo}-closed`,
      time: order.paidTime || order.createTime || '--',
      title: '订单已关闭',
      desc: '可能因支付超时或交易关闭，请重新发起充值'
    });
  } else if (order.status === 'FAILED') {
    items.push({
      id: `${order.outTradeNo}-failed`,
      time: order.paidTime || order.createTime || '--',
      title: '支付失败',
      desc: '系统未确认到账，请联系责任护士或客服协助处理'
    });
  }
  if (order.remark) {
    items.push({
      id: `${order.outTradeNo}-remark`,
      time: order.paidTime || order.createTime || '--',
      title: '处理备注',
      desc: order.remark
    });
  }
  return items;
}

Page({
  data: {
    outTradeNo: '',
    order: null,
    timeline: [],
    statusClass: 'status-default',
    contactPhone: '',
    contactName: '',
    loading: false
  },
  onLoad(query) {
    const outTradeNo = decodeURIComponent(query.outTradeNo || '');
    this.setData({ outTradeNo });
  },
  async onShow() {
    getApp().ensureLogin();
    await this.loadOrder(true);
  },
  async loadOrder(withLoading = false) {
    if (!this.data.outTradeNo) {
      return;
    }
    if (withLoading) {
      wx.showLoading({ title: '加载中', mask: true });
    }
    this.setData({ loading: true });
    try {
      const [order, contacts] = await Promise.all([
        getRechargeOrder(this.data.outTradeNo),
        getEmergencyContacts()
      ]);
      const fallbackContact = (contacts || []).find((item) => (item.role || '').includes('护士'))
        || (contacts || [])[0]
        || null;
      this.setData({
        order: order || null,
        timeline: buildTimeline(order),
        statusClass: resolveStatusClass(order ? order.status : ''),
        contactPhone: fallbackContact ? fallbackContact.phone : '',
        contactName: fallbackContact ? fallbackContact.name : '责任护士'
      });
    } finally {
      if (withLoading) {
        wx.hideLoading();
      }
      this.setData({ loading: false });
    }
  },
  async refreshOrder() {
    await this.loadOrder(true);
    if (!this.data.order) {
      wx.showToast({ title: '订单不存在', icon: 'none' });
      return;
    }
    wx.showToast({ title: '状态已刷新', icon: 'none' });
  },
  copyOrderNo() {
    if (!this.data.outTradeNo) {
      return;
    }
    wx.setClipboardData({
      data: this.data.outTradeNo,
      success: () => wx.showToast({ title: '订单号已复制', icon: 'none' })
    });
  },
  contactNurse() {
    if (!this.data.contactPhone) {
      wx.showToast({ title: '暂无联系电话', icon: 'none' });
      return;
    }
    wx.makePhoneCall({
      phoneNumber: this.data.contactPhone
    });
  },
  backToPayment() {
    const pages = getCurrentPages();
    if (pages.length > 1) {
      wx.navigateBack();
      return;
    }
    wx.navigateTo({ url: '/pages/payment/index' });
  }
});
