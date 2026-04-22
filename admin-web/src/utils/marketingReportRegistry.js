const reportRouteMap = {
    conversion: {
        path: '/marketing/reports/conversion',
        cacheKey: 'conversion'
    },
    followup: {
        path: '/marketing/reports/followup',
        cacheKey: 'followup'
    },
    channel: {
        path: '/marketing/reports/channel',
        cacheKey: 'channel'
    },
    consultation: {
        path: '/marketing/reports/consultation',
        cacheKey: 'consultation'
    },
    callback: {
        path: '/marketing/reports/callback',
        cacheKey: ({ hasCache }) => (hasCache('callback-checkin') ? 'callback-checkin' : 'callback-all')
    },
    'unknown-channel': {
        path: '/marketing/reports/unknown-channel',
        cacheKey: 'channel'
    },
    'channel-rank': {
        path: '/marketing/reports/channel-rank',
        cacheKey: 'channel-rank'
    },
    snapshots: {
        path: '/marketing/reports/snapshots',
        cacheKey: 'snapshots'
    },
    'sales-performance': {
        path: '/marketing/reports/sales-performance',
        cacheKey: 'sales-performance'
    }
};
export function getMarketingReportRoute(entry, hasCache) {
    const config = reportRouteMap[entry];
    const cacheKey = typeof config.cacheKey === 'function' ? config.cacheKey({ hasCache }) : config.cacheKey;
    return {
        path: config.path,
        cacheKey
    };
}
