const { getPatrolPoints, submitPatrolScan } = require('../../services/staff');

Page({
  data: {
    points: [],
    scannedText: '',
    loadError: ''
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    this.setData({ loadError: '' });
    try {
      this.setData({ points: await getPatrolPoints() });
    } catch (error) {
      this.setData({ loadError: error.message || '巡检点加载失败' });
    }
  },
  scanCode() {
    wx.scanCode({
      onlyFromCamera: false,
      success: (res) => this.markScanned(res.result || '已扫码'),
      fail: () => wx.showToast({ title: '扫码已取消', icon: 'none' })
    });
  },
  simulateScan(e) {
    this.markScanned(e.currentTarget.dataset.id);
  },
  async markScanned(value) {
    const points = this.data.points.map((item) => (
      item.id === value || value.indexOf(item.id) >= 0
        ? { ...item, status: '已扫码' }
        : item
    ));
    const matched = points.find((item) => item.status === '已扫码' && (item.id === value || value.indexOf(item.id) >= 0));
    try {
      await submitPatrolScan({
        pointId: matched ? matched.id : value,
        scanText: value
      });
      this.setData({ points, scannedText: value });
      wx.showToast({ title: '已记录巡检', icon: 'success' });
    } catch (error) {
      wx.showToast({ title: error.message || '巡检提交失败', icon: 'none' });
    }
  }
});
