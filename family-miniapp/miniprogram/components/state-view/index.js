// 通用状态组件：统一加载骨架 / 错误重试 / 空态三种缺省态，供各页复用。
// 仅负责“非正常内容态”的展示；正常内容仍由页面自身 wx:if 控制。
Component({
  options: {
    // 允许页面/全局样式类与设计变量作用到组件内部。
    styleIsolation: 'apply-shared'
  },
  properties: {
    loading: { type: Boolean, value: false },
    // 错误信息：非空字符串即视为错误态。
    error: { type: String, value: '' },
    empty: { type: Boolean, value: false },
    emptyText: { type: String, value: '暂无数据' },
    emptyHint: { type: String, value: '' },
    // 加载骨架占位行数。
    skeletonRows: { type: Number, value: 3 },
    retryText: { type: String, value: '重新加载' }
  },
  data: {
    rows: [0, 1, 2]
  },
  observers: {
    skeletonRows(count) {
      const total = Math.max(1, Math.min(6, Number(count) || 3));
      this.setData({ rows: Array.from({ length: total }, (_, index) => index) });
    }
  },
  methods: {
    onRetry() {
      this.triggerEvent('retry');
    }
  }
});
