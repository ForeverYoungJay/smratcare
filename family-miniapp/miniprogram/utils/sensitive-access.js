const { getSecuritySettings } = require('../services/family');

const VERIFY_TTL_MS = 10 * 60 * 1000;

function cacheKey(scene) {
  return `sensitive_verify_${scene || 'default'}`;
}

function isVerified(scene) {
  const expireAt = Number(wx.getStorageSync(cacheKey(scene)) || 0);
  return expireAt > Date.now();
}

function markVerified(scene) {
  wx.setStorageSync(cacheKey(scene), Date.now() + VERIFY_TTL_MS);
}

function openVerifyPage(scene, title) {
  return new Promise((resolve) => {
    wx.navigateTo({
      url: `/pages/security-verify/index?scene=${encodeURIComponent(scene)}&title=${encodeURIComponent(title || '敏感信息验证')}`,
      events: {
        verified() {
          markVerified(scene);
          resolve(true);
        },
        cancelled() {
          resolve(false);
        }
      },
      fail() {
        resolve(false);
      }
    });
  });
}

async function ensureSensitiveAccess(options = {}) {
  const scene = options.scene || 'default';
  const title = options.title || '敏感信息验证';
  const settingKey = options.settingKey || '';

  const settings = await getSecuritySettings();
  const needVerify = settingKey ? !!settings[settingKey] : true;
  if (!needVerify) {
    return true;
  }
  if (isVerified(scene)) {
    return true;
  }
  return openVerifyPage(scene, title);
}

module.exports = {
  ensureSensitiveAccess
};
