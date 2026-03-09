const { getServiceCatalog, createServiceOrder } = require('../../services/family');

Page({
  data: {
    catalog: []
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    const catalog = await getServiceCatalog();
    this.setData({ catalog: catalog || [] });
  },
  async orderNow(e) {
    const serviceId = Number(e.currentTarget.dataset.id);
    const app = getApp();
    const order = await createServiceOrder(serviceId, app.globalData.selectedElderId, '家属端下单');
    if (!order) {
      wx.showToast({ title: '下单失败', icon: 'none' });
      return;
    }
    wx.showToast({ title: '下单成功', icon: 'success' });
  }
});
