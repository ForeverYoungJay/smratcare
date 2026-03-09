export function resolveCareError(error, fallback = '操作失败') {
    const payload = error?.response?.data || error;
    if (!payload)
        return fallback;
    if (typeof payload === 'string')
        return payload || fallback;
    if (Array.isArray(payload?.errors) && payload.errors.length) {
        return String(payload.errors[0] || fallback);
    }
    if (payload?.errors && typeof payload.errors === 'object') {
        const first = Object.values(payload.errors)[0];
        if (Array.isArray(first) && first.length) {
            return String(first[0] || fallback);
        }
        if (typeof first === 'string' && first) {
            return first;
        }
    }
    if (typeof payload?.detail === 'string' && payload.detail)
        return payload.detail;
    if (typeof payload?.msg === 'string' && payload.msg)
        return payload.msg;
    if (typeof payload?.message === 'string' && payload.message)
        return payload.message;
    if (typeof payload?.error === 'string' && payload.error)
        return payload.error;
    if (typeof error?.message === 'string' && error.message)
        return error.message;
    return fallback;
}
