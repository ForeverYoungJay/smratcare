const { getFamilyBindings, removeFamilyBinding } = require('../../services/family');

Page({
  data: {
    list: []
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    const list = await getFamilyBindings();
    this.setData({ list: list || [] });
  },
  addBinding() {
    wx.navigateTo({ url: '/pages/bind/index' });
  },
  async removeBinding(e) {
    const elderId = Number(e.currentTarget.dataset.id);
    await removeFamilyBinding(elderId);
    wx.showToast({ title: '已解除绑定', icon: 'none' });
    this.loadData();
  }
});
