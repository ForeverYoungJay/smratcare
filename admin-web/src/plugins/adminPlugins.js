const publicPaths = new Set(['/home', '/enterprise', '/admin', '/login', '/403']);
let adminPluginsPromise = null;
export function isAdminRoute(path) {
    return !publicPaths.has(path);
}
export function ensureAdminPlugins(app) {
    if (!adminPluginsPromise) {
        adminPluginsPromise = (async () => {
            const [{ setupEcharts }, { setupVxeTable }, { setupFullCalendar }, { setupEditor }, { setupFlow }, { setupAntdx }] = await Promise.all([
                import('./echarts'),
                import('./vxeTable'),
                import('./fullcalendar'),
                import('./editor'),
                import('./flow'),
                import('./antdx')
            ]);
            setupEcharts(app);
            setupVxeTable(app);
            setupFullCalendar(app);
            setupEditor(app);
            setupFlow(app);
            setupAntdx(app);
        })().catch((error) => {
            adminPluginsPromise = null;
            throw error;
        });
    }
    return adminPluginsPromise;
}
