const { getFamilySurveys, submitFamilySurvey } = require('../../services/family');

Page({
  data: {
    loading: false,
    surveys: [],
    activeId: '',
    answers: {},
    submitting: false
  },
  async onShow() {
    getApp().ensureLogin();
    await this.loadData();
  },
  async loadData() {
    this.setData({ loading: true });
    try {
      const list = (await getFamilySurveys()) || [];
      const surveys = list.map((item) => ({
        ...item,
        questions: (item.questions || []).map((q) => ({
          ...q,
          options: parseOptions(q.optionsJson)
        }))
      }));
      const firstOpen = surveys.find((item) => !item.submitted);
      this.setData({ surveys, activeId: firstOpen ? firstOpen.id : '', answers: {} });
    } finally {
      this.setData({ loading: false });
    }
  },
  openSurvey(e) {
    const id = e.currentTarget.dataset.id;
    const target = this.data.surveys.find((item) => item.id === id);
    if (target && target.submitted) {
      wx.showToast({ title: '该问卷已提交，感谢参与', icon: 'none' });
      return;
    }
    this.setData({ activeId: id, answers: {} });
  },
  chooseOption(e) {
    const { qid, option } = e.currentTarget.dataset;
    this.setData({ [`answers.${qid}`]: option });
  },
  onTextInput(e) {
    const qid = e.currentTarget.dataset.qid;
    this.setData({ [`answers.${qid}`]: e.detail.value });
  },
  async submit() {
    if (this.data.submitting) return;
    const survey = this.data.surveys.find((item) => item.id === this.data.activeId);
    if (!survey) return;
    const answers = [];
    for (const q of survey.questions) {
      const value = (this.data.answers[q.id] || '').trim();
      if (q.required && !value) {
        wx.showToast({ title: `请完成必答题：${q.title}`, icon: 'none' });
        return;
      }
      if (value) answers.push({ questionId: q.id, answerText: value });
    }
    this.setData({ submitting: true });
    try {
      await submitFamilySurvey(survey.id, answers);
      wx.showToast({ title: '提交成功，感谢您的反馈', icon: 'success' });
      await this.loadData();
    } catch (error) {
      wx.showToast({ title: error.message || '提交失败，请稍后重试', icon: 'none' });
    } finally {
      this.setData({ submitting: false });
    }
  }
});

function parseOptions(raw) {
  try {
    const parsed = JSON.parse(raw || '[]');
    return Array.isArray(parsed) ? parsed.map(String) : [];
  } catch (e) {
    return [];
  }
}
