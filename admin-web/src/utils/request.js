import axios from 'axios';
import { message } from 'ant-design-vue';
import router from '../router';
import { getToken, clearToken, clearRoles, clearPermissions } from './auth';
import { emitLiveSync, inferLiveSyncTopics } from './liveSync';
const request = axios.create({
    baseURL: '/',
    timeout: 45000
});
let lastForbiddenToastAt = 0;
let lastForbiddenToastKey = '';
const FORBIDDEN_TOAST_COOLDOWN_MS = 2500;
function resolveErrorMessage(payload, fallback = '请求失败') {
    if (!payload)
        return fallback;
    if (typeof payload === 'string')
        return payload || fallback;
    if (typeof payload?.msg === 'string' && payload.msg)
        return payload.msg;
    if (typeof payload?.message === 'string' && payload.message)
        return payload.message;
    if (typeof payload?.error === 'string' && payload.error)
        return payload.error;
    return fallback;
}
function resolveAxiosErrorMessage(error) {
    const rawMessage = String(error?.message || '');
    const isTimeout = error?.code === 'ECONNABORTED' || /timeout/i.test(rawMessage);
    if (isTimeout) {
        return '请求超时，系统正在处理中，请稍后重试';
    }
    if (/network error/i.test(rawMessage)) {
        return '网络异常，请检查网络后重试';
    }
    return resolveErrorMessage(error?.response?.data, rawMessage || '请求失败');
}
request.interceptors.request.use((config) => {
    const token = getToken();
    if (token) {
        config.headers = config.headers || {};
        config.headers.Authorization = `Bearer ${token}`;
    }
    config.headers = config.headers || {};
    if (!config.headers['Content-Type']) {
        config.headers['Content-Type'] = 'application/json;charset=UTF-8';
    }
    return config;
});
request.interceptors.response.use((response) => {
    const method = String(response.config?.method || 'GET').toUpperCase();
    const url = String(response.config?.url || '');
    const data = response.data;
    if (data && typeof data.code !== 'undefined') {
        if (data.code !== 0) {
            const cfg = (response.config || {});
            if (!cfg.silentError) {
                message.error(resolveErrorMessage(data));
            }
            return Promise.reject(data);
        }
        if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
            emitLiveSync({
                topics: inferLiveSyncTopics(url),
                method,
                url,
                at: Date.now()
            });
        }
        return data.data;
    }
    if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
        emitLiveSync({
            topics: inferLiveSyncTopics(url),
            method,
            url,
            at: Date.now()
        });
    }
    return data;
}, (error) => {
    const status = error?.response?.status;
    const url = String(error?.config?.url || '');
    const cfg = (error?.config || {});
    const method = String(error?.config?.method || 'GET').toUpperCase();
    const rawMessage = String(error?.message || '');
    const isTimeoutOrNetwork = error?.code === 'ECONNABORTED' || /timeout|network error/i.test(rawMessage);
    const silentError = Boolean(cfg.silentError);
    const silent403 = Boolean(cfg.silent403 || cfg.silentError);
    const forceErrorToast = Boolean(cfg.forceErrorToast);
    const isLoginRequest = url.includes('/api/auth/login') || url.includes('/api/auth/family/login');
    if (status === 401) {
        clearToken();
        clearRoles();
        clearPermissions();
        router.push('/login');
        return Promise.reject(error);
    }
    if (isLoginRequest) {
        return Promise.reject(error);
    }
    if (status === 403) {
        if (silent403) {
            return Promise.reject(error);
        }
        const payload = error?.response?.data;
        const rawMessage = resolveErrorMessage(payload, '当前账号无该操作权限（403）');
        const displayMessage = /access denied/i.test(rawMessage)
            ? '当前账号无该操作权限（403），请联系管理员开通。'
            : rawMessage;
        const now = Date.now();
        const key = `${url}|${displayMessage}`;
        if (key !== lastForbiddenToastKey || now - lastForbiddenToastAt > FORBIDDEN_TOAST_COOLDOWN_MS) {
            message.warning(displayMessage);
            lastForbiddenToastAt = now;
            lastForbiddenToastKey = key;
        }
        return Promise.reject(error);
    }
    if (silentError) {
        return Promise.reject(error);
    }
    // 自动刷新类 GET 请求在超时/弱网下容易形成提示风暴，默认静默处理
    if (!forceErrorToast && method === 'GET' && isTimeoutOrNetwork) {
        return Promise.reject(error);
    }
    message.error(resolveAxiosErrorMessage(error));
    return Promise.reject(error);
});
export default request;
export function fetchPage(url, params, config) {
    const merged = { ...(params || {}) };
    if (merged.pageNo != null && merged.page == null) {
        merged.page = merged.pageNo;
    }
    if (merged.pageSize != null && merged.size == null) {
        merged.size = merged.pageSize;
    }
    if (merged.sortOrder != null && merged.order == null) {
        merged.order = merged.sortOrder;
    }
    return request.get(url, { params: merged, ...(config || {}) }).then((res) => {
        if (res && Array.isArray(res.list)) {
            return res;
        }
        if (res && Array.isArray(res.records)) {
            return {
                list: res.records,
                total: Number(res.total || 0),
                pageNo: Number(res.current || 1),
                pageSize: Number(res.size || params?.pageSize || 10)
            };
        }
        if (Array.isArray(res)) {
            return {
                list: res,
                total: res.length,
                pageNo: Number(params?.pageNo || 1),
                pageSize: Number(params?.pageSize || res.length || 10)
            };
        }
        return {
            list: [],
            total: 0,
            pageNo: Number(params?.pageNo || 1),
            pageSize: Number(params?.pageSize || 10)
        };
    });
}
