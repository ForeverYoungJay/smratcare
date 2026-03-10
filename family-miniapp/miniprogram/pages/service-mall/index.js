const {
  getMallProducts,
  previewMallOrder,
  submitMallOrder,
  getMallOrders
} = require('../../services/family');

function resolveOrderSummary(order = {}) {
  const qty = Number(order.quantity || 1);
  const amount = Number(order.totalAmount || 0);
  return {
    ...order,
    qtyText: `${qty}件`,
    amountText: `¥${amount.toFixed(2)}`
  };
}

Page({
  data: {
    loadingProducts: false,
    loadingOrders: false,
    keyword: '',
    submittingId: '',
    previewingId: '',
    products: [],
    orders: [],
    qtyMap: {}
  },
  onShow() {
    getApp().ensureLogin();
    this.loadAll();
  },
  async loadAll() {
    await Promise.all([this.loadProducts(), this.loadOrders()]);
  },
  async loadProducts() {
    this.setData({ loadingProducts: true });
    try {
      const list = await getMallProducts({ keyword: this.data.keyword, pageNo: 1, pageSize: 60 });
      const qtyMap = { ...this.data.qtyMap };
      const products = (list || []).map((item) => {
        const id = String(item.id);
        if (!qtyMap[id] || qtyMap[id] < 1) {
          qtyMap[id] = 1;
        }
        return {
          ...item,
          priceText: Number(item.price || 0).toFixed(2),
          stockText: Number(item.currentStock || 0)
        };
      });
      this.setData({ products, qtyMap });
    } catch (error) {
      wx.showToast({ title: error.message || '商品加载失败', icon: 'none' });
    } finally {
      this.setData({ loadingProducts: false });
    }
  },
  async loadOrders() {
    this.setData({ loadingOrders: true });
    try {
      const list = await getMallOrders({ pageNo: 1, pageSize: 20 });
      this.setData({ orders: (list || []).map(resolveOrderSummary) });
    } catch (error) {
      wx.showToast({ title: error.message || '订单加载失败', icon: 'none' });
    } finally {
      this.setData({ loadingOrders: false });
    }
  },
  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
  },
  onSearch() {
    this.loadProducts();
  },
  resolveQty(productId) {
    const key = String(productId);
    return Number(this.data.qtyMap[key] || 1);
  },
  setQty(productId, qty) {
    const next = Math.max(1, Math.min(99, Number(qty) || 1));
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
  selectedElderIdOrToast() {
    const elderId = getApp().globalData.selectedElderId;
    if (!elderId) {
      wx.showToast({ title: '请先绑定并选择老人', icon: 'none' });
      return null;
    }
    return Number(elderId);
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
      const elderId = Number(getApp().globalData.selectedElderId);
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
