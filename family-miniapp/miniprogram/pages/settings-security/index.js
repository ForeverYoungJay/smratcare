const { getSecuritySettings, updateSecuritySettings, setSecurityPassword } = require('../../services/family');

const SCOPE_OPTIONS = [
  '仅子女可查看完整健康数据',
  '直系家属可查看完整健康数据',
  '全部授权家属可查看完整健康数据'
];

function normalizeSetting(raw = {}) {
  return {
    verifyHealthData: raw.verifyHealthData !== false,
    verifyMedicalRecords: raw.verifyMedicalRecords !== false,
    verifyReports: raw.verifyReports !== false,
    verifyWithSmsCode: false,
    verifyWithPassword: true,
    hasIndependentPassword: !!raw.hasIndependentPassword,
    maskSensitiveData: raw.maskSensitiveData !== false,
    visibleScope: raw.visibleScope || SCOPE_OPTIONS[0]
  };
}

function resolveScopeIndex(scope) {
  const idx = SCOPE_OPTIONS.findIndex((item) => item === scope);
  return idx >= 0 ? idx : 0;
}

Page({
  data: {
    setting: normalizeSetting(),
    scopeOptions: SCOPE_OPTIONS,
    scopeIndex: 0,
    loading: false
  },
  async onShow() {
    getApp().ensureLogin();
    await this.loadSettings();
  },
  async loadSettings() {
    this.setData({ loading: true });
    try {
      const raw = await getSecuritySettings();
      const setting = normalizeSetting(raw);
      this.setData({
        setting,
        scopeIndex: resolveScopeIndex(setting.visibleScope)
      });
    } catch (error) {
      wx.showToast({ title: '加载设置失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },
  async onSwitch(e) {
    const field = e.currentTarget.dataset.field;
    if (!field) {
      return;
    }
    const value = !!e.detail.value;
    const nextSetting = { ...this.data.setting, [field]: value };
    this.setData({ setting: nextSetting });
    await this.persist({ [field]: value });
  },
  async onScopeChange(e) {
    const scopeIndex = Number(e.detail.value);
    const visibleScope = this.data.scopeOptions[scopeIndex] || this.data.scopeOptions[0];
    this.setData({
      scopeIndex,
      setting: { ...this.data.setting, visibleScope }
    });
    await this.persist({ visibleScope });
  },
  async persist(payload) {
    try {
      const next = await updateSecuritySettings(payload);
      const setting = normalizeSetting(next);
      this.setData({
        setting,
        scopeIndex: resolveScopeIndex(setting.visibleScope)
      });
    } catch (error) {
      wx.showToast({ title: '保存失败，请稍后重试', icon: 'none' });
      await this.loadSettings();
    }
  },
  async onSetPassword() {
    const modalRes = await new Promise((resolve) => {
      wx.showModal({
        title: '设置独立密码',
        editable: true,
        placeholderText: '请输入6-32位密码',
        confirmText: '保存',
        cancelText: '取消',
        success: resolve,
        fail: () => resolve({ confirm: false })
      });
    });
    if (!modalRes || !modalRes.confirm) {
      return;
    }
    const password = (modalRes.content || '').trim();
    if (password.length < 6) {
      wx.showToast({ title: '密码至少6位', icon: 'none' });
      return;
    }
    try {
      await setSecurityPassword(password);
      wx.showToast({ title: '独立密码已设置', icon: 'none' });
      await this.loadSettings();
    } catch (error) {
      wx.showToast({ title: error.message || '设置失败', icon: 'none' });
    }
  }
});
