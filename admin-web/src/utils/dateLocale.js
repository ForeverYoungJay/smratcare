const ZH_CN_DATE_TIME_FORMATTER = new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
});
const ZH_CN_DATE_FORMATTER = new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
});
function normalizeDateInput(value) {
    if (value == null || value === '')
        return null;
    const date = value instanceof Date ? value : new Date(value);
    if (Number.isNaN(date.getTime()))
        return null;
    return date;
}
export function formatChineseDateTime(value, fallback = '') {
    const date = normalizeDateInput(value);
    if (!date)
        return fallback;
    return ZH_CN_DATE_TIME_FORMATTER.format(date);
}
export function formatChineseDate(value, fallback = '') {
    const date = normalizeDateInput(value);
    if (!date)
        return fallback;
    return ZH_CN_DATE_FORMATTER.format(date);
}
