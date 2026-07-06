// 通用照片墙：现场留痕照片的选择 / 预览 / 删除，供医护、护理、异常上报等表单复用。
// 组件只维护照片路径列表（本地临时路径或已上传 http 地址），选择/删除后通过 change 事件回传，
// 上传时机仍由页面在提交时决定。
Component({
  options: {
    styleIsolation: 'apply-shared'
  },
  properties: {
    photos: { type: Array, value: [] },
    // 最多可保留的照片数量。
    max: { type: Number, value: 8 },
    // 单次选择的上限。
    perPick: { type: Number, value: 4 },
    // 只读模式：仅展示与预览，不显示新增/删除。
    readonly: { type: Boolean, value: false }
  },
  methods: {
    choosePhoto() {
      const current = this.data.photos || [];
      const remain = this.data.max - current.length;
      if (remain <= 0) {
        wx.showToast({ title: `最多可上传${this.data.max}张`, icon: 'none' });
        return;
      }
      wx.chooseImage({
        count: Math.min(this.data.perPick, remain),
        sizeType: ['compressed'],
        sourceType: ['camera', 'album'],
        success: (res) => {
          const next = current.concat(res.tempFilePaths || []).slice(0, this.data.max);
          this.triggerEvent('change', { photos: next });
        }
      });
    },
    previewPhoto(e) {
      const urls = this.data.photos || [];
      if (!urls.length) return;
      wx.previewImage({ urls, current: e.currentTarget.dataset.url || urls[0] });
    },
    removePhoto(e) {
      const index = Number(e.currentTarget.dataset.index);
      const next = (this.data.photos || []).filter((_, i) => i !== index);
      this.triggerEvent('change', { photos: next });
    }
  }
});
