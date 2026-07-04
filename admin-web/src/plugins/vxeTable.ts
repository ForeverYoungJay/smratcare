import type { App } from 'vue'
// vxe-table 4.6+ 将 loading / tooltip / 图标等 UI 组件拆分到 vxe-pc-ui，
// 必须先注册 VxeUI，否则会出现「缺少 vxe-loading / vxe-tooltip 组件」等警告，
// 导致表格 loading 态与列提示失效。
import VxeUIAll from 'vxe-pc-ui'
import 'vxe-pc-ui/lib/style.css'
import VxeUITable from 'vxe-table'
import 'vxe-table/lib/style.css'

export function setupVxeTable(app: App) {
  app.use(VxeUIAll)
  app.use(VxeUITable)
}
