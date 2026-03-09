const { getCapabilityStatus, bindWechatNotifyOpenId } = require('../../services/family');

const STATUS_META = {
  READY: { text: '已就绪', cls: 'ok' },
  MOCK: { text: '模拟中', cls: 'warn' },
  BIND_REQUIRED: { text: '待绑定', cls: 'warn' },
  OPTIONAL: { text: '可选开启', cls: 'neutral' },
  DEPRECATED: { text: '已废弃', cls: 'warn' },
  OFFLINE: { text: '已下线', cls: 'danger' },
  OFF: { text: '未开启', cls: 'neutral' }
};
const ALERT_STATUSES = new Set(['OFF', 'MOCK', 'BIND_REQUIRED', 'DEPRECATED']);

function resolveActionType(item = {}) {
  const code = String(item.code || '').toUpperCase();
  const status = String(item.status || '').toUpperCase();
  if (code === 'WECHAT_NOTIFY' && status === 'BIND_REQUIRED') {
    return 'bind_notify';
  }
  if (code === 'SMS_CODE' && (status === 'MOCK' || status === 'OFF')) {
    return 'sms_setup';
  }
  if (code === 'WECHAT_PAY' && status === 'OFF') {
    return 'pay_setup';
  }
  if (code === 'SECURITY_PASSWORD' && status === 'OPTIONAL') {
    return 'security_setup';
  }
  return '';
}

function resolveActionText(item = {}, actionType = '') {
  if (actionType === 'bind_notify') {
    return '立即绑定 >';
  }
  if (actionType === 'sms_setup') {
    return '查看配置 >';
  }
  if (actionType === 'pay_setup') {
    return '查看充值 >';
  }
  if (actionType === 'security_setup') {
    return '开启验证 >';
  }
  return item.actionPath ? '去处理 >' : '';
}

function normalizeItems(items = []) {
  return (items || []).map((item) => {
    const meta = STATUS_META[item.status] || { text: item.status || '未知', cls: 'neutral' };
    const actionType = resolveActionType(item);
    return {
      ...item,
      statusText: meta.text,
      badgeClass: `badge-${meta.cls}`,
      actionType,
      actionText: resolveActionText(item, actionType)
    };
  });
}

function countAlerts(items = []) {
  return (items || []).filter((item) => ALERT_STATUSES.has(String(item.status || '').toUpperCase())).length;
}

Page({
  data: {
    loading: false,
    status: null,
    items: []
  },
  async onShow() {
    getApp().ensureLogin();
    await this.loadData();
  },
  async onPullDownRefresh() {
    await this.loadData(true);
  },
  async loadData(fromPullDown = false) {
    this.setData({ loading: true });
    try {
      const status = await getCapabilityStatus();
      const alertCount = countAlerts(status && status.items ? status.items : []);
      getApp().updateCapabilityAlerts(alertCount);
      this.setData({
        status,
        items: normalizeItems(status && status.items ? status.items : [])
      });
    } catch (error) {
      wx.showToast({ title: error.message || '加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
      if (fromPullDown) {
        wx.stopPullDownRefresh();
      }
    }
  },
  async openAction(e) {
    const dataset = e.currentTarget.dataset || {};
    const path = dataset.path;
    const actionType = dataset.actionType;
    if (!path && !actionType) {
      return;
    }
    if (actionType === 'bind_notify') {
      const confirmed = await this.confirmAction(
        '绑定通知接收',
        '绑定后可接收紧急健康提醒与缴费通知，是否立即绑定？',
        '立即绑定'
      );
      if (confirmed) {
        await this.bindNotifyOpenId();
      }
      return;
    }
    if (actionType === 'sms_setup') {
      const confirmed = await this.confirmAction(
        '短信通道待完善',
        '当前短信能力为模拟/未开启状态，建议前往安全设置完善短信验证码链路配置。',
        '去查看'
      );
      if (confirmed) {
        this.navigateToPath(path || '/pages/settings-security/index');
      }
      return;
    }
    if (actionType === 'pay_setup') {
      const confirmed = await this.confirmAction(
        '微信支付未开启',
        '当前充值为模拟记账流程，建议尽快开启微信支付正式链路。',
        '去充值页'
      );
      if (confirmed) {
        this.navigateToPath(path || '/pages/payment/index');
      }
      return;
    }
    if (actionType === 'security_setup') {
      const confirmed = await this.confirmAction(
        '开启独立密码',
        '启用独立密码后，查看报告与医疗记录会更安全。',
        '去开启'
      );
      if (confirmed) {
        this.navigateToPath(path || '/pages/settings-security/index');
      }
      return;
    }
    this.navigateToPath(path);
  },
  refreshStatus() {
    this.loadData();
  },
  navigateToPath(path) {
    if (!path) {
      return;
    }
    wx.navigateTo({
      url: path,
      fail: () => {
        wx.switchTab({
          url: path,
          fail: () => wx.showToast({ title: '暂不支持跳转', icon: 'none' })
        });
      }
    });
  },
  confirmAction(title, content, confirmText) {
    return new Promise((resolve) => {
      wx.showModal({
        title,
        content,
        confirmText: confirmText || '确定',
        cancelText: '稍后',
        success: (res) => resolve(!!(res && res.confirm)),
        fail: () => resolve(false)
      });
    });
  },
  async bindNotifyOpenId() {
    try {
      const loginCode = await new Promise((resolve, reject) => {
        wx.login({
          success: (res) => {
            if (res && res.code) {
              resolve(res.code);
              return;
            }
            reject(new Error('登录态无效'));
          },
          fail: reject
        });
      });
      const resp = await bindWechatNotifyOpenId({ loginCode });
      wx.showToast({ title: resp && resp.bound ? '绑定成功' : '绑定失败', icon: 'none' });
      await this.loadData();
    } catch (error) {
      wx.showToast({ title: error.message || '绑定失败', icon: 'none' });
    }
  }
});
