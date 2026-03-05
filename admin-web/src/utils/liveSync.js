const LIVE_SYNC_EVENT = 'smratcare:live-sync';
const CHANNEL_NAME = 'smratcare-live-sync';
const topicRules = [
    { pattern: /^\/api\/elder\//, topics: ['elder'] },
    { pattern: /^\/api\/elder\/lifecycle\//, topics: ['elder', 'lifecycle', 'bed', 'finance', 'care', 'dining'] },
    { pattern: /^\/api\/bed\//, topics: ['bed', 'elder'] },
    { pattern: /^\/api\/room\//, topics: ['bed', 'elder'] },
    { pattern: /^\/api\/asset\//, topics: ['bed'] },
    { pattern: /^\/api\/schedule\//, topics: ['care', 'hr'] },
    { pattern: /^\/api\/attendance\//, topics: ['hr', 'oa'] },
    { pattern: /^\/api\/nursing\//, topics: ['care', 'elder'] },
    { pattern: /^\/api\/vital\//, topics: ['health', 'elder'] },
    { pattern: /^\/api\/assessment\//, topics: ['health', 'elder'] },
    { pattern: /^\/api\/life\/dining\//, topics: ['dining', 'elder'] },
    { pattern: /^\/api\/life\//, topics: ['dining', 'elder'] },
    { pattern: /^\/api\/finance\//, topics: ['finance', 'elder'] },
    { pattern: /^\/api\/card\//, topics: ['finance', 'elder'] },
    { pattern: /^\/api\/care\//, topics: ['care', 'elder'] },
    { pattern: /^\/api\/health\//, topics: ['health', 'elder'] },
    { pattern: /^\/api\/dining\//, topics: ['dining', 'elder'] },
    { pattern: /^\/api\/material\//, topics: ['logistics', 'finance'] },
    { pattern: /^\/api\/marketing\//, topics: ['marketing', 'elder'] },
    { pattern: /^\/api\/oa\//, topics: ['oa'] },
    { pattern: /^\/api\/admin\/hr\//, topics: ['hr'] },
    { pattern: /^\/api\/admin\/roles/, topics: ['system', 'hr'] },
    { pattern: /^\/api\/hr\//, topics: ['hr'] },
    { pattern: /^\/api\/rbac\//, topics: ['system', 'hr'] },
    { pattern: /^\/api\/logistics\//, topics: ['logistics'] },
    { pattern: /^\/api\/storage\//, topics: ['logistics'] },
    { pattern: /^\/api\/inventory\//, topics: ['logistics'] }
];
function isBrowser() {
    return typeof window !== 'undefined';
}
function normalizeUrl(url) {
    if (!url)
        return '';
    const clean = url.replace(/^https?:\/\/[^/]+/i, '');
    return clean.split('?')[0] || clean;
}
export function inferLiveSyncTopics(url) {
    const cleanUrl = normalizeUrl(url);
    const topics = new Set();
    topicRules.forEach((rule) => {
        if (rule.pattern.test(cleanUrl)) {
            rule.topics.forEach((topic) => topics.add(topic));
        }
    });
    if (!topics.size)
        topics.add('system');
    return Array.from(topics);
}
export function emitLiveSync(payload) {
    if (!isBrowser())
        return;
    window.dispatchEvent(new CustomEvent(LIVE_SYNC_EVENT, { detail: payload }));
    if (typeof BroadcastChannel !== 'undefined') {
        const channel = new BroadcastChannel(CHANNEL_NAME);
        channel.postMessage(payload);
        channel.close();
    }
}
export function subscribeLiveSync(listener) {
    if (!isBrowser())
        return () => { };
    const onWindowEvent = (event) => {
        const detail = event.detail;
        if (detail)
            listener(detail);
    };
    window.addEventListener(LIVE_SYNC_EVENT, onWindowEvent);
    let channel = null;
    if (typeof BroadcastChannel !== 'undefined') {
        channel = new BroadcastChannel(CHANNEL_NAME);
        channel.onmessage = (event) => {
            const payload = event.data;
            if (payload)
                listener(payload);
        };
    }
    return () => {
        window.removeEventListener(LIVE_SYNC_EVENT, onWindowEvent);
        if (channel)
            channel.close();
    };
}
