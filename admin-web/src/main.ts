import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'ant-design-vue/dist/reset.css'
import './styles/global.less'
import App from './App.vue'
import router from './router'
import { setupAntd } from './plugins/antd'
import { setupEcharts } from './plugins/echarts'
import { setupVxeTable } from './plugins/vxeTable'
import { setupFullCalendar } from './plugins/fullcalendar'
import { setupEditor } from './plugins/editor'
import { setupFlow } from './plugins/flow'
import { setupAntdx } from './plugins/antdx'
import { permission } from './directives'

const app = createApp(App)
app.use(createPinia())
app.use(router)
setupAntd(app)
setupEcharts(app)
setupVxeTable(app)
setupFullCalendar(app)
setupEditor(app)
setupFlow(app)
setupAntdx(app)
app.directive('permission', permission)
app.mount('#app')
