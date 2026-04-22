export const MARKETING_ROUTE_PATHS = {
    workbench: '/marketing/workbench',
    leads: '/marketing/leads/all',
    interactions: '/marketing/interactions/records',
    funnel: '/marketing/funnel/consultation',
    reservation: '/marketing/reservation/records',
    contracts: '/marketing/contracts/pending',
    callback: '/marketing/callback/checkin',
    reports: '/marketing/reports/conversion',
    plan: '/marketing/plan'
};
export const MARKETING_SECTION_LINKS = [
    { label: '线索', path: MARKETING_ROUTE_PATHS.leads, activePaths: ['/marketing/leads'] },
    { label: '互动中心', path: MARKETING_ROUTE_PATHS.interactions, activePaths: ['/marketing/interactions', '/marketing/followup'] },
    { label: '漏斗', path: MARKETING_ROUTE_PATHS.funnel, activePaths: ['/marketing/funnel'] },
    { label: '预定与床态', path: MARKETING_ROUTE_PATHS.reservation, activePaths: ['/marketing/reservation', '/marketing/room-panorama'] },
    { label: '合同', path: MARKETING_ROUTE_PATHS.contracts, activePaths: ['/marketing/contracts', '/marketing/contract-signing', '/marketing/contract-management'] },
    { label: '签后回访', path: MARKETING_ROUTE_PATHS.callback, activePaths: ['/marketing/callback'] },
    { label: '报表', path: MARKETING_ROUTE_PATHS.reports, activePaths: ['/marketing/reports'] },
    { label: '营销方案', path: MARKETING_ROUTE_PATHS.plan, activePaths: ['/marketing/plan'] }
];
