const { getSecuritySettings } = require('../services/family');

const VERIFY_TTL_MS = 10 * 60 * 1000;
const CANCEL_SUPPRESS_MS = 1500;

function cacheKey(scene) {
  return `sensitive_verify_${scene || 'default'}`;
}

function globalCacheKey() {
  return 'sensitive_verify_all';
}

function cancelKey(scene) {
  return `sensitive_verify_cancel_${scene || 'default'}`;
}

function isVerified(scene) {
  const expireAt = Number(wx.getStorageSync(cacheKey(scene)) || 0);
  const globalExpireAt = Number(wx.getStorageSync(globalCacheKey()) || 0);
  return expireAt > Date.now() || globalExpireAt > Date.now();
}

function markVerified(scene) {
  const expireAt = Date.now() + VERIFY_TTL_MS;
  wx.setStorageSync(cacheKey(scene), expireAt);
  wx.setStorageSync(globalCacheKey(), expireAt);
}

function markCancelled(scene) {
  wx.setStorageSync(cancelKey(scene), Date.now() + CANCEL_SUPPRESS_MS);
}

function clearCancelled(scene) {
  wx.removeStorageSync(cancelKey(scene));
}

function isRecentlyCancelled(scene) {
  const expireAt = Number(wx.getStorageSync(cancelKey(scene)) || 0);
  if (expireAt > Date.now()) {
    return true;
  }
  if (expireAt) {
    wx.removeStorageSync(cancelKey(scene));
  }
  return false;
}

function openVerifyPage(scene, title) {
  return new Promise((resolve) => {
    wx.navigateTo({
      url: `/pages/security-verify/index?scene=${encodeURIComponent(scene)}&title=${encodeURIComponent(title || '敏感信息验证')}`,
      events: {
        verified() {
          clearCancelled(scene);
          markVerified(scene);
          resolve(true);
        },
        cancelled() {
          markCancelled(scene);
          resolve(false);
        }
      },
      fail() {
        markCancelled(scene);
        resolve(false);
      }
    });
  });
}

async function ensureSensitiveAccess(options = {}) {
  const scene = options.scene || 'default';
  const title = options.title || '敏感信息验证';
  const settingKey = options.settingKey || '';
  const force = !!options.force;

  const settings = await getSecuritySettings();
  const needVerify = settingKey ? !!settings[settingKey] : true;
  if (!needVerify) {
    return true;
  }
  if (isVerified(scene)) {
    return true;
  }
  if (force) {
    clearCancelled(scene);
  } else if (isRecentlyCancelled(scene)) {
    return false;
  }
  return openVerifyPage(scene, title);
}

module.exports = {
  ensureSensitiveAccess
};
