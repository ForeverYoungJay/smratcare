const DAILY_KEY_PREFIX = 'staff_subscribe_daily_';

function collectTemplateIds(scene) {
  const app = typeof getApp === 'function' ? getApp() : null;
  const templates = (app && app.globalData && app.globalData.subscribeTemplates) || {};
  const scenes = Array.isArray(scene) ? scene : [scene];
  return scenes
    .map((key) => templates[key])
    .filter((id) => typeof id === 'string' && id.trim())
    .slice(0, 3);
}

/**
 * 请求订阅消息授权。模板 ID 为空串时静默跳过（见 app.js globalData.subscribeTemplates 注释）。
 * 任何失败都静默处理，不阻塞业务流程。
 */
function requestSubscribe(scene) {
  return new Promise((resolve) => {
    const tmplIds = collectTemplateIds(scene);
    if (!tmplIds.length || typeof wx.requestSubscribeMessage !== 'function') {
      resolve(null);
      return;
    }
    wx.requestSubscribeMessage({
      tmplIds,
      success: (res) => resolve(res || null),
      fail: () => resolve(null)
    });
  });
}

/**
 * 同一场景每天最多请求一次（用 storage 记录），用于首页等自动触发的入口。
 */
function requestSubscribeOncePerDay(scene) {
  const today = new Date().toISOString().slice(0, 10);
  const key = `${DAILY_KEY_PREFIX}${Array.isArray(scene) ? scene.join('_') : scene}`;
  try {
    if (wx.getStorageSync(key) === today) {
      return Promise.resolve(null);
    }
    wx.setStorageSync(key, today);
  } catch (error) {
    // Storage unavailable: fall through and still try once.
  }
  return requestSubscribe(scene);
}

module.exports = {
  requestSubscribe,
  requestSubscribeOncePerDay
};
