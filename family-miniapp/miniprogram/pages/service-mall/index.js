const {
  getMallProducts,
  previewMallOrder,
  submitMallOrder,
  getMallOrders,
  getFamilyBindings
} = require('../../services/family');

const ORDER_STATUS_TABS = [
  { key: 'ALL', label: '全部' },
  { key: 'PROCESSING', label: '进行中' },
  { key: 'DONE', label: '已完成' },
  { key: 'CLOSED', label: '已关闭' }
];

function safeNumber(value, fallback = 0) {
  const n = Number(value);
  return Number.isFinite(n) ? n : fallback;
}

function resolveOrderSummary(order = {}) {
  const qty = safeNumber(order.quantity, 1);
  const amount = safeNumber(order.totalAmount, 0);
  return {
    ...order,
    qtyText: `${qty}件`,
    amountText: `¥${amount.toFixed(2)}`
  };
}

function resolveOrderTabKey(order = {}) {
  const status = safeNumber(order.orderStatus, -1);
  if (status === 3) {
    return 'DONE';
  }
  if (status === 4 || status === 5) {
    return 'CLOSED';
  }
  return 'PROCESSING';
}

function uniqueCategories(products = []) {
  const set = new Set();
  products.forEach((item) => {
    const category = String(item.category || '').trim();
    if (category) {
      set.add(category);
    }
  });
  return ['全部', ...Array.from(set)];
}

Page({
  data: {
    loadingProducts: false,
    loadingOrders: false,
    loadingBindings: false,
    keyword: '',
    submittingId: '',
    previewingId: '',
    products: [],
    orders: [],
    filteredOrders: [],
    qtyMap: {},
    bindings: [],
    selectedElderId: null,
    selectedElderIndex: 0,
    hasBindings: false,
    categoryOptions: ['全部'],
    categoryIndex: 0,
    orderStatusTabs: ORDER_STATUS_TABS,
    activeOrderTab: 'ALL'
  },
  async onShow() {
    const app = getApp();
    if (!app.ensureLogin()) {
      return;
    }
    if (app.globalData.mallOrdersDirty) {
      await this.loadBindings();
      await this.loadOrders();
      app.globalData.mallOrdersDirty = false;
      return;
    }
    await this.loadBindings();
    await this.loadAll();
    app.globalData.mallOrdersDirty = false;
  },
  async onPullDownRefresh() {
    try {
      await this.loadBindings();
      await this.loadAll();
    } finally {
      wx.stopPullDownRefresh();
    }
  },
  async loadAll() {
    await this.loadProducts();
    if (!this.data.hasBindings) {
      this.setData({ orders: [], filteredOrders: [] });
      return;
    }
    await this.loadOrders();
  },
  async loadBindings() {
    this.setData({ loadingBindings: true });
    try {
      const list = await getFamilyBindings();
      const bindings = (list || []).map((item) => ({
        ...item,
        displayName: `${item.elderName || '老人'} · ${item.relation || '家属'}`
      }));
      const app = getApp();
      const currentSelected = safeNumber(app.globalData.selectedElderId || 0, 0);
      let selectedElderId = currentSelected > 0 ? currentSelected : null;
      let selectedElderIndex = 0;
      if (bindings.length > 0) {
        const idx = bindings.findIndex((item) => safeNumber(item.elderId, 0) === currentSelected);
        if (idx >= 0) {
          selectedElderIndex = idx;
          selectedElderId = safeNumber(bindings[idx].elderId, 0);
        } else {
          const primaryIdx = bindings.findIndex((item) => !!item.isPrimary);
          selectedElderIndex = primaryIdx >= 0 ? primaryIdx : 0;
          selectedElderId = safeNumber(bindings[selectedElderIndex].elderId, 0);
        }
      } else {
        selectedElderId = null;
      }
      app.globalData.selectedElderId = selectedElderId;
      this.setData({
        bindings,
        hasBindings: bindings.length > 0,
        selectedElderId,
        selectedElderIndex
      });
    } catch (error) {
      this.setData({
        bindings: [],
        hasBindings: false,
        selectedElderId: null,
        selectedElderIndex: 0
      });
      wx.showToast({ title: error.message || '老人绑定信息加载失败', icon: 'none' });
    } finally {
      this.setData({ loadingBindings: false });
    }
  },
  onElderChange(e) {
    const idx = safeNumber(e.detail.value, 0);
    const item = this.data.bindings[idx];
    if (!item) {
      return;
    }
    const elderId = safeNumber(item.elderId, 0);
    getApp().globalData.selectedElderId = elderId || null;
    this.setData({
      selectedElderIndex: idx,
      selectedElderId: elderId || null
    });
    this.loadAll();
  },
  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
  },
  onCategoryChange(e) {
    this.setData({ categoryIndex: safeNumber(e.detail.value, 0) });
    this.loadProducts();
  },
  onSearch() {
    this.loadProducts();
  },
  onOrderTabTap(e) {
    const tab = String(e.currentTarget.dataset.key || 'ALL');
    this.setData({ activeOrderTab: tab });
    this.applyOrderFilter();
  },
  goOrderDetail(e) {
    const orderId = safeNumber(e.currentTarget.dataset.id, 0);
    if (!orderId) {
      return;
    }
    wx.navigateTo({
      url: `/pages/mall-order-detail/index?orderId=${orderId}`
    });
  },
  selectedCategory() {
    const category = this.data.categoryOptions[this.data.categoryIndex] || '全部';
    return category === '全部' ? '' : category;
  },
  async loadProducts() {
    this.setData({ loadingProducts: true });
    try {
      const list = await getMallProducts({
        keyword: this.data.keyword,
        category: this.selectedCategory(),
        pageNo: 1,
        pageSize: 80
      });
      const qtyMap = { ...this.data.qtyMap };
      const products = (list || []).map((item) => {
        const id = String(item.id);
        if (!qtyMap[id] || qtyMap[id] < 1) {
          qtyMap[id] = 1;
        }
        return {
          ...item,
          priceText: safeNumber(item.price, 0).toFixed(2),
          stockText: safeNumber(item.currentStock, 0),
          canSubmit: String(item.stockStatus || '').toUpperCase() !== 'OUT'
        };
      });
      const categoryOptions = uniqueCategories(products);
      let categoryIndex = this.data.categoryIndex;
      const currentCategory = this.data.categoryOptions[this.data.categoryIndex] || '全部';
      const nextIdx = categoryOptions.findIndex((item) => item === currentCategory);
      categoryIndex = nextIdx >= 0 ? nextIdx : 0;
      this.setData({ products, qtyMap, categoryOptions, categoryIndex });
    } catch (error) {
      wx.showToast({ title: error.message || '商品加载失败', icon: 'none' });
    } finally {
      this.setData({ loadingProducts: false });
    }
  },
  async loadOrders() {
    if (!this.data.hasBindings) {
      this.setData({ orders: [], filteredOrders: [] });
      return;
    }
    this.setData({ loadingOrders: true });
    try {
      const list = await getMallOrders({ pageNo: 1, pageSize: 40, elderId: this.data.selectedElderId || undefined });
      const orders = (list || []).map(resolveOrderSummary);
      this.setData({ orders });
      this.applyOrderFilter();
    } catch (error) {
      wx.showToast({ title: error.message || '订单加载失败', icon: 'none' });
    } finally {
      this.setData({ loadingOrders: false });
    }
  },
  applyOrderFilter() {
    const active = this.data.activeOrderTab || 'ALL';
    const filteredOrders = (this.data.orders || []).filter((item) => {
      if (active === 'ALL') {
        return true;
      }
      return resolveOrderTabKey(item) === active;
    });
    this.setData({ filteredOrders });
  },
  resolveQty(productId) {
    const key = String(productId);
    return safeNumber(this.data.qtyMap[key], 1);
  },
  setQty(productId, qty) {
    const next = Math.max(1, Math.min(99, safeNumber(qty, 1)));
    this.setData({ [`qtyMap.${String(productId)}`]: next });
  },
  onQtyInput(e) {
    this.setQty(e.currentTarget.dataset.id, e.detail.value);
  },
  decQty(e) {
    const productId = e.currentTarget.dataset.id;
    this.setQty(productId, this.resolveQty(productId) - 1);
  },
  incQty(e) {
    const productId = e.currentTarget.dataset.id;
    this.setQty(productId, this.resolveQty(productId) + 1);
  },
  goBindElder() {
    wx.navigateTo({ url: '/pages/bind/index' });
  },
  selectedElderIdOrToast() {
    const elderId = safeNumber(this.data.selectedElderId, 0) || safeNumber(getApp().globalData.selectedElderId, 0);
    if (!elderId) {
      wx.showToast({ title: '请先绑定并选择老人', icon: 'none' });
      return null;
    }
    return elderId;
  },
  async doPreview(productId) {
    const elderId = this.selectedElderIdOrToast();
    if (!elderId) {
      return null;
    }
    const qty = this.resolveQty(productId);
    return previewMallOrder({
      elderId,
      productId: Number(productId),
      qty
    });
  },
  async onPreview(e) {
    const productId = e.currentTarget.dataset.id;
    const loadingId = String(productId);
    if (this.data.previewingId) {
      return;
    }
    this.setData({ previewingId: loadingId });
    try {
      const preview = await this.doPreview(productId);
      if (!preview) {
        return;
      }
      const reasons = (preview.reasons || []).join('\n');
      const message = preview.allowed
        ? `可下单\n商品：${preview.productName}\n数量：${preview.qty}\n预计积分：${preview.pointsRequired || 0}`
        : `当前不可下单\n原因：${reasons || preview.message || '规则限制'}`;
      wx.showModal({
        title: preview.allowed ? '预检通过' : '预检未通过',
        content: message,
        showCancel: false
      });
    } catch (error) {
      wx.showToast({ title: error.message || '预检失败', icon: 'none' });
    } finally {
      this.setData({ previewingId: '' });
    }
  },
  async onSubmit(e) {
    const productId = e.currentTarget.dataset.id;
    const loadingId = String(productId);
    if (this.data.submittingId) {
      return;
    }
    this.setData({ submittingId: loadingId });
    try {
      const preview = await this.doPreview(productId);
      if (!preview) {
        return;
      }
      if (!preview.allowed) {
        wx.showToast({ title: preview.message || '当前不可下单', icon: 'none' });
        return;
      }
      const confirm = await new Promise((resolve) => {
        wx.showModal({
          title: '确认下单',
          content: `商品：${preview.productName}\n数量：${preview.qty}`,
          success: resolve,
          fail: () => resolve({ confirm: false })
        });
      });
      if (!confirm || !confirm.confirm) {
        return;
      }
      const elderId = this.selectedElderIdOrToast();
      if (!elderId) {
        return;
      }
      const submit = await submitMallOrder({
        elderId,
        productId: Number(productId),
        qty: this.resolveQty(productId)
      });
      if (!submit || !submit.allowed) {
        wx.showToast({ title: (submit && submit.message) || '下单失败', icon: 'none' });
        return;
      }
      wx.showToast({ title: '下单成功', icon: 'success' });
      await this.loadOrders();
      await this.loadProducts();
    } catch (error) {
      wx.showToast({ title: error.message || '下单失败，请稍后重试', icon: 'none' });
    } finally {
      this.setData({ submittingId: '' });
    }
  }
});
