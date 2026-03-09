const { getServiceOrders } = require('../../services/family');

Page({
  data: {
    orders: []
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    const orders = await getServiceOrders();
    this.setData({ orders: orders || [] });
  }
});
