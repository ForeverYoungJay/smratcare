const { getHandovers, submitHandover } = require('../../services/staff');
const { enqueue, isNetworkError } = require('../../utils/offline-queue');
const { saveDraft, loadDraft, clearDraft } = require('../../utils/form-draft');

const DRAFT_PAGE = 'staff-handover';
// 交接补充不绑定具体任务，草稿统一挂在 default 键下。
const DRAFT_ID = 'default';

Page({
  data: {
    handovers: [],
    note: '',
    loading: false,
    submitting: false,
    loadError: '',
    submitError: '',
    submitSuccess: ''
  },
  onLoad() {
    this.restoreFormDraft();
  },
  onShow() {
    getApp().ensureLogin();
    this.loadData();
  },
  onHide() {
    this.saveFormDraft();
  },
  onUnload() {
    this.saveFormDraft();
  },
  async loadData() {
    this.setData({ loading: true, loadError: '' });
    try {
      this.setData({ handovers: await getHandovers() });
    } catch (error) {
      this.setData({ loadError: error.message || '交接记录加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },
  saveFormDraft() {
    if (!this.data.note.trim()) return;
    saveDraft(DRAFT_PAGE, DRAFT_ID, { note: this.data.note });
  },
  scheduleDraftSave() {
    if (this._draftTimer) clearTimeout(this._draftTimer);
    this._draftTimer = setTimeout(() => {
      this.saveFormDraft();
    }, 500);
  },
  restoreFormDraft() {
    const draft = loadDraft(DRAFT_PAGE, DRAFT_ID);
    if (!draft || !draft.note) return;
    this.setData({ note: draft.note });
    wx.showToast({ title: '已恢复未提交草稿', icon: 'none' });
  },
  onNoteInput(e) {
    this.setData({
      note: e.detail.value,
      submitError: '',
      submitSuccess: ''
    });
    this.scheduleDraftSave();
  },
  async submitNote() {
    if (!this.data.note.trim() || this.data.submitting) {
      wx.showToast({ title: '请先填写交接内容', icon: 'none' });
      return;
    }
    this.setData({ submitting: true, submitError: '', submitSuccess: '' });
    const payload = {
      title: '移动端交接补充',
      content: this.data.note.trim(),
      time: '刚刚',
      owner: '当前员工'
    };
    try {
      const item = await submitHandover(payload);
      clearDraft(DRAFT_PAGE, DRAFT_ID);
      this.setData({
        handovers: [item].concat(this.data.handovers),
        note: '',
        submitSuccess: '交接内容已提交，下一班次可继续查看。'
      });
      wx.showToast({ title: '已提交交接', icon: 'success' });
    } catch (error) {
      if (isNetworkError(error)) {
        enqueue('handover', payload);
        clearDraft(DRAFT_PAGE, DRAFT_ID);
        this.setData({ note: '' });
        wx.showToast({ title: '已离线暂存，网络恢复后自动提交', icon: 'none' });
      } else {
        this.setData({ submitError: error.message || '提交失败，请稍后重试' });
        wx.showToast({ title: error.message || '提交失败', icon: 'none' });
      }
    } finally {
      this.setData({ submitting: false });
    }
  }
});
