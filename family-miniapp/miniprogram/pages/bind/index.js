const { bindElder } = require('../../services/family');

function normalizeIdCard(value) {
  return String(value || '').trim().replace(/\s+/g, '').toUpperCase();
}

function isValidIdCard(value) {
  return /(^\d{15}$)|(^\d{17}[0-9X]$)/.test(normalizeIdCard(value));
}

Page({
  data: {
    loading: false,
    relations: ['子女', '配偶', '兄弟姐妹', '其他亲属'],
    relationIndex: 0,
    form: {
      elderIdCardNo: '',
      elderIdLegacy: '',
      relation: '子女',
      isPrimary: 1,
      remark: ''
    }
  },
  onShow() {
    getApp().ensureLogin();
  },
  parseBindToken(rawText) {
    const text = String(rawText || '').trim();
    if (!text) {
      return { elderId: null, elderIdCardNo: '' };
    }

    const idCardParam = text.match(/[?&](idCardNo|elderIdCardNo)=([^&#]+)/i);
    if (idCardParam && idCardParam[2]) {
      return {
        elderId: null,
        elderIdCardNo: normalizeIdCard(decodeURIComponent(idCardParam[2]))
      };
    }

    const idCardMatch = text.match(/(^|[^0-9A-Za-z])(\d{17}[0-9Xx]|\d{15})([^0-9A-Za-z]|$)/);
    if (idCardMatch && idCardMatch[2]) {
      return {
        elderId: null,
        elderIdCardNo: normalizeIdCard(idCardMatch[2])
      };
    }

    const elderIdParam = text.match(/[?&]elderId=(\d+)/i);
    if (elderIdParam && elderIdParam[1]) {
      return {
        elderId: Number(elderIdParam[1]),
        elderIdCardNo: ''
      };
    }

    const pathMatch = text.match(/elder\/(\d+)/i);
    if (pathMatch && pathMatch[1]) {
      return {
        elderId: Number(pathMatch[1]),
        elderIdCardNo: ''
      };
    }

    if (/^\d+$/.test(text)) {
      return {
        elderId: Number(text),
        elderIdCardNo: ''
      };
    }

    return { elderId: null, elderIdCardNo: '' };
  },
  scanCode() {
    wx.scanCode({
      onlyFromCamera: true,
      success: (res) => {
        const parsed = this.parseBindToken(res.result);
        if (parsed.elderIdCardNo) {
          this.setData({
            'form.elderIdCardNo': parsed.elderIdCardNo,
            'form.elderIdLegacy': ''
          });
          wx.showToast({ title: '已识别身份证号', icon: 'none' });
          return;
        }
        if (parsed.elderId) {
          this.setData({
            'form.elderIdLegacy': String(parsed.elderId)
          });
          wx.showToast({ title: '识别到旧版二维码，已兼容处理', icon: 'none' });
          return;
        }
        wx.showToast({ title: '二维码未包含身份证信息', icon: 'none' });
      },
      fail: () => {
        wx.showToast({ title: '扫码已取消', icon: 'none' });
      }
    });
  },
  onIdCardInput(e) {
    this.setData({ 'form.elderIdCardNo': normalizeIdCard(e.detail.value) });
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
    const { elderIdCardNo, elderIdLegacy, relation, isPrimary, remark } = this.data.form;
    const idCard = normalizeIdCard(elderIdCardNo);
    const legacyId = Number(elderIdLegacy);
    if (!idCard && !legacyId) {
      wx.showToast({ title: '请填写老人身份证号', icon: 'none' });
      return;
    }
    if (idCard && !isValidIdCard(idCard)) {
      wx.showToast({ title: '身份证号格式不正确', icon: 'none' });
      return;
    }
    this.setData({ loading: true });
    try {
      const resp = await bindElder({
        elderIdCardNo: idCard || undefined,
        elderId: legacyId || undefined,
        relation,
        isPrimary,
        remark
      });
      getApp().globalData.selectedElderId = resp && resp.elderId ? Number(resp.elderId) : null;
      wx.showToast({ title: '绑定成功', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 500);
    } catch (error) {
      wx.showToast({ title: error.message || '绑定失败，请核对信息', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  }
});
