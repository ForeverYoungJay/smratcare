const { clearAuth } = require('./auth');

function toQueryString(data = {}) {
  const pairs = Object.keys(data)
    .filter((key) => data[key] !== undefined && data[key] !== null && data[key] !== '')
    .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(data[key])}`);
  return pairs.length ? `?${pairs.join('&')}` : '';
}

function buildError(message, extras = {}) {
  const error = new Error(message || '请求失败');
  Object.keys(extras).forEach((key) => {
    error[key] = extras[key];
  });
  return error;
}

function handleAuthFailure(app, message) {
  clearAuth();
  if (app && app.globalData) {
    app.globalData.token = '';
    app.globalData.familyUser = null;
    app.globalData.selectedElderId = null;
  }
  const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : [];
  const current = pages.length > 0 ? pages[pages.length - 1] : null;
  if (!current || current.route !== 'pages/login/index') {
    wx.showToast({ title: message || '登录已失效，请重新登录', icon: 'none' });
    wx.reLaunch({ url: '/pages/login/index' });
  }
}

function request({ url, method = 'GET', data = {}, auth = true, timeout = 10000 }) {
  const app = getApp();
  const headers = {
    'Content-Type': 'application/json'
  };
  if (auth && app.globalData.token) {
    headers.Authorization = `Bearer ${app.globalData.token}`;
  }
  const upperMethod = method.toUpperCase();
  const reqUrl = `${app.globalData.baseUrl}${url}${upperMethod === 'GET' ? toQueryString(data) : ''}`;

  return new Promise((resolve, reject) => {
    wx.request({
      url: reqUrl,
      method: upperMethod,
      data: upperMethod === 'GET' ? undefined : data,
      header: headers,
      timeout,
      success: (res) => {
        const payload = res.data;
        if (res.statusCode === 401 || res.statusCode === 403) {
          handleAuthFailure(app, '登录已失效，请重新登录');
          reject(buildError('登录已失效，请重新登录', {
            code: 'AUTH_REQUIRED',
            statusCode: res.statusCode
          }));
          return;
        }
        if (res.statusCode < 200 || res.statusCode >= 300) {
          reject(buildError((payload && payload.message) || `服务暂时不可用（${res.statusCode}）`, {
            code: 'HTTP_ERROR',
            statusCode: res.statusCode
          }));
          return;
        }
        if (!payload || typeof payload !== 'object') {
          reject(buildError('服务响应格式不正确', {
            code: 'INVALID_RESPONSE',
            statusCode: res.statusCode
          }));
          return;
        }
        if (payload.code !== 0) {
          if (payload.code === 401 || payload.code === 403) {
            handleAuthFailure(app, payload.message || '登录已失效，请重新登录');
            reject(buildError(payload.message || '登录已失效，请重新登录', {
              code: 'AUTH_REQUIRED',
              statusCode: res.statusCode,
              businessCode: payload.code
            }));
            return;
          }
          reject(buildError(payload.message || '请求失败', {
            code: 'BUSINESS_ERROR',
            statusCode: res.statusCode,
            businessCode: payload.code
          }));
          return;
        }
        resolve(payload.data);
      },
      fail: (err) => {
        const errMsg = err && err.errMsg ? err.errMsg : '网络请求失败';
        const isTimeout = errMsg.includes('timeout');
        reject(buildError(isTimeout ? '请求超时，请检查网络后重试' : errMsg, {
          code: isTimeout ? 'TIMEOUT' : 'NETWORK_ERROR'
        }));
      }
    });
  });
}

function uploadFile({ url, filePath, name = 'file', formData = {}, auth = true, timeout = 60000 }) {
  const app = getApp();
  const headers = {};
  if (auth && app.globalData.token) {
    headers.Authorization = `Bearer ${app.globalData.token}`;
  }
  const reqUrl = `${app.globalData.baseUrl}${url}`;
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: reqUrl,
      filePath,
      name,
      formData,
      header: headers,
      timeout,
      success: (res) => {
        if (res.statusCode === 401 || res.statusCode === 403) {
          handleAuthFailure(app, '登录已失效，请重新登录');
          reject(buildError('登录已失效，请重新登录', {
            code: 'AUTH_REQUIRED',
            statusCode: res.statusCode
          }));
          return;
        }
        if (res.statusCode < 200 || res.statusCode >= 300) {
          reject(buildError(`上传失败（${res.statusCode}）`, {
            code: 'HTTP_ERROR',
            statusCode: res.statusCode
          }));
          return;
        }
        if (!res || typeof res.data !== 'string') {
          reject(buildError('上传响应格式不正确', {
            code: 'INVALID_RESPONSE',
            statusCode: res.statusCode
          }));
          return;
        }
        let payload;
        try {
          payload = JSON.parse(res.data);
        } catch (error) {
          reject(buildError('上传响应解析失败', {
            code: 'INVALID_RESPONSE',
            statusCode: res.statusCode
          }));
          return;
        }
        if (!payload || typeof payload !== 'object') {
          reject(buildError('上传响应格式不正确', {
            code: 'INVALID_RESPONSE',
            statusCode: res.statusCode
          }));
          return;
        }
        if (payload.code !== 0) {
          reject(buildError(payload.message || '上传失败', {
            code: 'BUSINESS_ERROR',
            statusCode: res.statusCode,
            businessCode: payload.code
          }));
          return;
        }
        resolve(payload.data);
      },
      fail: (err) => {
        const errMsg = err && err.errMsg ? err.errMsg : '文件上传失败';
        const isTimeout = errMsg.includes('timeout');
        reject(buildError(isTimeout ? '上传超时，请稍后重试' : errMsg, {
          code: isTimeout ? 'TIMEOUT' : 'NETWORK_ERROR'
        }));
      }
    });
  });
}

module.exports = {
  request,
  uploadFile
};
