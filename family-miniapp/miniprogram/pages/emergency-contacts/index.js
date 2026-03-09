const { getEmergencyContacts } = require('../../services/family');

Page({
  data: {
    contacts: []
  },
  async onShow() {
    getApp().ensureLogin();
    const contacts = await getEmergencyContacts();
    this.setData({ contacts: contacts || [] });
  },
  call(e) {
    wx.makePhoneCall({ phoneNumber: e.currentTarget.dataset.phone });
  }
});
