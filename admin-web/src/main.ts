import { createApp } from 'vue'
import { createPinia } from 'pinia'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import 'ant-design-vue/dist/reset.css'
import './styles/global.less'
import App from './App.vue'
import router from './router'
import { setupAntd } from './plugins/antd'
import { setupVxeTable } from './plugins/vxeTable'
import { permission, permissionCode } from './directives'

dayjs.locale('zh-cn')

const app = createApp(App)
app.use(createPinia())
app.use(router)
setupAntd(app)
setupVxeTable(app)
app.directive('permission', permission)
app.directive('permission-code', permissionCode)
app.mount('#app')
