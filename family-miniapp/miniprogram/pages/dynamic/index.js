const ENTRIES = [
  { icon: '📖', name: '护理日志', desc: '每日护理项目、执行人员与备注', path: '/pages/care-log/index' },
  { icon: '📷', name: '活动相册', desc: '文娱活动纪实，可点赞评论', path: '/pages/activity-album/index' },
  { icon: '🍚', name: '膳食日历', desc: '每日三餐、膳食标签与进食情况', path: '/pages/diet-calendar/index' },
  { icon: '❤️', name: '健康数据', desc: '血压、心率等体征趋势', path: '/pages/health/index' },
  { icon: '🚶', name: '外出记录', desc: '外出行程、陪同人员与归院状态', path: '/pages/outing-record/index' },
  { icon: '📅', name: '今日日程', desc: '长者当日照护与活动安排', path: '/pages/schedule/index' }
];

Page({
  data: {
    entries: ENTRIES
  },
  onShow() {
    getApp().ensureLogin();
  },
  go(e) {
    const path = e.currentTarget.dataset.path;
    if (path) {
      wx.navigateTo({ url: path });
    }
  }
});
