function toQueryString(data = {}) {
  const pairs = Object.keys(data)
    .filter((key) => data[key] !== undefined && data[key] !== null && data[key] !== '')
    .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(data[key])}`);
  return pairs.length ? `?${pairs.join('&')}` : '';
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
        if (!payload || typeof payload !== 'object') {
          reject(new Error('服务响应格式不正确'));
          return;
        }
        if (payload.code !== 0) {
          reject(new Error(payload.message || '请求失败'));
          return;
        }
        resolve(payload.data);
      },
      fail: (err) => {
        reject(new Error(err && err.errMsg ? err.errMsg : '网络请求失败'));
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
        if (!res || typeof res.data !== 'string') {
          reject(new Error('上传响应格式不正确'));
          return;
        }
        let payload;
        try {
          payload = JSON.parse(res.data);
        } catch (error) {
          reject(new Error('上传响应解析失败'));
          return;
        }
        if (!payload || typeof payload !== 'object') {
          reject(new Error('上传响应格式不正确'));
          return;
        }
        if (payload.code !== 0) {
          reject(new Error(payload.message || '上传失败'));
          return;
        }
        resolve(payload.data);
      },
      fail: (err) => {
        reject(new Error(err && err.errMsg ? err.errMsg : '文件上传失败'));
      }
    });
  });
}

module.exports = {
  request,
  uploadFile
};
