import { createSSRApp } from 'vue'
import { renderToString } from 'vue/server-renderer'
import EnterpriseHome from './views/EnterpriseHome.vue'

export function renderHome() {
  return renderToString(createSSRApp(EnterpriseHome))
}
