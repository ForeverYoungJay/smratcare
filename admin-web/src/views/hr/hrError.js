export function resolveHrError(error, fallback = '操作失败') {
    const payload = error?.response?.data || error;
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
