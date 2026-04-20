import type { App } from 'vue'

const publicPaths = new Set(['/home', '/enterprise', '/admin', '/login', '/403'])

let adminPluginsPromise: Promise<void> | null = null

export function isAdminRoute(path: string) {
  return !publicPaths.has(path)
}

export function ensureAdminPlugins(app: App) {
  if (!adminPluginsPromise) {
    adminPluginsPromise = (async () => {
      const [
        { setupEcharts },
        { setupVxeTable },
        { setupFullCalendar },
        { setupEditor },
        { setupFlow },
        { setupAntdx }
      ] = await Promise.all([
        import('./echarts'),
        import('./vxeTable'),
        import('./fullcalendar'),
        import('./editor'),
        import('./flow'),
        import('./antdx')
      ])

      setupEcharts(app)
      setupVxeTable(app)
      setupFullCalendar(app)
      setupEditor(app)
      setupFlow(app)
      setupAntdx(app)
    })().catch((error) => {
      adminPluginsPromise = null
      throw error
    })
  }

  return adminPluginsPromise
}
