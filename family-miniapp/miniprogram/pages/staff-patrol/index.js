const { getPatrolPoints, submitPatrolScan } = require('../../services/staff');

Page({
  data: {
    loading: false,
    submitting: false,
    points: [],
    scannedText: '',
    loadError: '',
    submitError: '',
    submitSuccess: ''
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '', submitError: '' });
    try {
      this.setData({ points: await getPatrolPoints() });
    } catch (error) {
      this.setData({ loadError: error.message || '巡检点加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  scanCode() {
    if (this.data.submitting) {
      return;
    }
    wx.scanCode({
      onlyFromCamera: false,
      success: (res) => this.markScanned(res.result || '已扫码'),
      fail: () => wx.showToast({ title: '扫码已取消', icon: 'none' })
    });
  },
  simulateScan(e) {
    if (this.data.submitting) {
      return;
    }
    this.markScanned(e.currentTarget.dataset.id);
  },
  async markScanned(value) {
    if (this.data.submitting) {
      return;
    }
    const scanText = String(value || '');
    const points = this.data.points.map((item) => (
      item.id === scanText || scanText.indexOf(item.id) >= 0
        ? { ...item, status: '已扫码' }
        : item
    ));
    const matched = points.find((item) => item.status === '已扫码' && (item.id === scanText || scanText.indexOf(item.id) >= 0));
    this.setData({ submitting: true, submitError: '', submitSuccess: '' });
    try {
      await submitPatrolScan({
        pointId: matched ? matched.id : scanText,
        scanText
      });
      this.setData({
        points,
        scannedText: scanText,
        submitSuccess: `巡检点 ${matched ? matched.name : scanText} 已完成扫码留痕。`
      });
      wx.showToast({ title: '已记录巡检', icon: 'success' });
    } catch (error) {
      this.setData({ submitError: error.message || '巡检提交失败，请稍后重试' });
      wx.showToast({ title: error.message || '巡检提交失败', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
