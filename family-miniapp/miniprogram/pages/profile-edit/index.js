const { getProfile, updateProfile } = require('../../services/family');

Page({
  data: {
    profile: {
      realName: '',
      phone: '',
      address: '',
      preferredContact: ''
    }
  },
  async onShow() {
    getApp().ensureLogin();
    const profile = await getProfile();
    this.setData({ profile });
  },
  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [`profile.${field}`]: e.detail.value });
  },
  async save() {
    await updateProfile(this.data.profile);
    wx.showToast({ title: '已保存', icon: 'success' });
  }
});
