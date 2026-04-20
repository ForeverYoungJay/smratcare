import { createApp } from 'vue'
import { createPinia } from 'pinia'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import 'ant-design-vue/dist/reset.css'
import './styles/global.less'
import EnterpriseHome from './views/EnterpriseHome.vue'
import { setupAntd } from './plugins/antd'

dayjs.locale('zh-cn')

const app = createApp(EnterpriseHome)
app.use(createPinia())
setupAntd(app)
app.mount('#home-app')
