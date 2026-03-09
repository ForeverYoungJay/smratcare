const TOKEN_KEY = 'smartcare_family_token';
const USER_KEY = 'smartcare_family_user';

function setAuth(token, user) {
  wx.setStorageSync(TOKEN_KEY, token || '');
  wx.setStorageSync(USER_KEY, user || null);
}

function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || '';
}

function getUser() {
  return wx.getStorageSync(USER_KEY) || null;
}

function clearAuth() {
  wx.removeStorageSync(TOKEN_KEY);
  wx.removeStorageSync(USER_KEY);
}

module.exports = {
  setAuth,
  getToken,
  getUser,
  clearAuth
};
