const { bindElder } = require('../../services/family');

Page({
  data: {
    loading: false,
    relations: ['子女', '配偶', '兄弟姐妹', '其他亲属'],
    relationIndex: 0,
    form: {
      elderId: '',
      relation: '子女',
      isPrimary: 1,
      remark: ''
    }
  },
  onShow() {
    getApp().ensureLogin();
  },
  parseElderId(rawText) {
    const text = String(rawText || '').trim();
    if (!text) {
      return null;
    }
    const queryMatch = text.match(/[?&]elderId=(\d+)/i);
    if (queryMatch && queryMatch[1]) {
      return Number(queryMatch[1]);
    }
    const pathMatch = text.match(/elder\/(\d+)/i);
    if (pathMatch && pathMatch[1]) {
      return Number(pathMatch[1]);
    }
    if (/^\d+$/.test(text)) {
      return Number(text);
    }
    return null;
  },
  scanCode() {
    wx.scanCode({
      onlyFromCamera: true,
      success: (res) => {
        const elderId = this.parseElderId(res.result);
        if (!elderId) {
          wx.showToast({ title: '二维码不包含老人ID', icon: 'none' });
          return;
        }
        this.setData({ 'form.elderId': String(elderId) });
        wx.showToast({ title: `识别成功：${elderId}`, icon: 'none' });
      },
      fail: () => {
        wx.showToast({ title: '扫码已取消', icon: 'none' });
      }
    });
  },
  onElderIdInput(e) {
    this.setData({ 'form.elderId': e.detail.value });
  },
  onRelationChange(e) {
    const idx = Number(e.detail.value);
    this.setData({
      relationIndex: idx,
      'form.relation': this.data.relations[idx]
    });
  },
  onPrimaryChange(e) {
    this.setData({ 'form.isPrimary': e.detail.value ? 1 : 0 });
  },
  onRemarkInput(e) {
    this.setData({ 'form.remark': e.detail.value });
  },
  async onBind() {
    const { elderId, relation, isPrimary, remark } = this.data.form;
    if (!elderId) {
      wx.showToast({ title: '请填写老人ID', icon: 'none' });
      return;
    }
    this.setData({ loading: true });
    try {
      await bindElder({
        elderId: Number(elderId),
        relation,
        isPrimary,
        remark
      });
      getApp().globalData.selectedElderId = Number(elderId);
      wx.showToast({ title: '绑定成功', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 500);
    } catch (error) {
      wx.showToast({ title: '绑定失败，请核对信息', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  }
});
