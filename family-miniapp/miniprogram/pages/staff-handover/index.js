const { getHandovers, submitHandover } = require('../../services/staff');

Page({
  data: {
    handovers: [],
    note: '',
    submitting: false,
    loadError: ''
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  async loadData() {
    this.setData({ loadError: '' });
    try {
      this.setData({ handovers: await getHandovers() });
    } catch (error) {
      this.setData({ loadError: error.message || '交接记录加载失败' });
    }
  },
  onNoteInput(e) {
    this.setData({ note: e.detail.value });
  },
  async submitNote() {
    if (!this.data.note.trim() || this.data.submitting) {
      wx.showToast({ title: '请先填写交接内容', icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    try {
      const item = await submitHandover({
        title: '移动端交接补充',
        content: this.data.note,
        time: '刚刚',
        owner: '当前员工'
      });
      this.setData({
        handovers: [item].concat(this.data.handovers),
        note: ''
      });
      wx.showToast({ title: '已提交交接', icon: 'success' });
    } catch (error) {
      wx.showToast({ title: error.message || '提交失败', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
