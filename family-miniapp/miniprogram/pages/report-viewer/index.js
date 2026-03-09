const { getAssessmentReports } = require('../../services/family');
const { ensureSensitiveAccess } = require('../../utils/sensitive-access');

function resolveReportUrl(url) {
  if (!url) {
    return '';
  }
  if (/^https?:\/\//i.test(url)) {
    return url;
  }
  const app = getApp();
  const base = (app.globalData.baseUrl || '').replace(/\/$/, '');
  if (!base) {
    return '';
  }
  return `${base}${url.startsWith('/') ? '' : '/'}${url}`;
}

function downloadFile(url) {
  return new Promise((resolve, reject) => {
    wx.downloadFile({
      url,
      success: (res) => {
        if (res.statusCode === 200 && res.tempFilePath) {
          resolve(res.tempFilePath);
          return;
        }
        reject(new Error(`download failed: ${res.statusCode}`));
      },
      fail: reject
    });
  });
}

function openDocument(filePath) {
  return new Promise((resolve, reject) => {
    wx.openDocument({
      filePath,
      showMenu: true,
      success: resolve,
      fail: reject
    });
  });
}

Page({
  data: {
    report: null,
    opening: false
  },
  async onLoad(query) {
    getApp().ensureLogin();
    const granted = await ensureSensitiveAccess({
      scene: 'assessment-reports',
      settingKey: 'verifyReports',
      title: '评估报告访问验证'
    });
    if (!granted) {
      wx.showToast({ title: '已取消访问', icon: 'none' });
      setTimeout(() => {
        const pages = getCurrentPages();
        if (pages.length > 1) {
          wx.navigateBack();
          return;
        }
        wx.reLaunch({ url: '/pages/assessment-report/index' });
      }, 160);
      return;
    }
    const id = Number(query.id || 0);
    const list = await getAssessmentReports();
    const found = (list || []).find((item) => item.id === id) || null;
    const report = found
      ? {
        ...found,
        score: found.score || '--',
        risk: found.risk || '--'
      }
      : null;
    this.setData({ report });
  },
  async openReport() {
    const report = this.data.report;
    if (!report) {
      wx.showToast({ title: '报告不存在', icon: 'none' });
      return;
    }
    const fileUrl = resolveReportUrl(report.fileUrl);
    if (!fileUrl) {
      wx.showToast({ title: '暂无可查看文件', icon: 'none' });
      return;
    }

    this.setData({ opening: true });
    wx.showLoading({ title: '打开中', mask: true });
    try {
      const tempFilePath = await downloadFile(fileUrl);
      await openDocument(tempFilePath);
    } catch (error) {
      wx.showToast({ title: '打开失败，请稍后重试', icon: 'none' });
    } finally {
      wx.hideLoading();
      this.setData({ opening: false });
    }
  },
  copyLink() {
    const report = this.data.report;
    if (!report || !report.fileUrl) {
      wx.showToast({ title: '暂无链接可复制', icon: 'none' });
      return;
    }
    const url = resolveReportUrl(report.fileUrl);
    if (!url) {
      wx.showToast({ title: '请先配置后端地址', icon: 'none' });
      return;
    }
    wx.setClipboardData({
      data: url,
      success: () => wx.showToast({ title: '链接已复制', icon: 'none' })
    });
  },
  navigateBack() {
    wx.navigateBack();
  }
});
