import { createApp, createSSRApp } from 'vue'
import 'ant-design-vue/dist/reset.css'
import './styles/home.less'
import EnterpriseHome from './views/EnterpriseHome.vue'
const prerendered = document.getElementById('home-app')?.hasAttribute('data-prerendered')
const app = prerendered ? createSSRApp(EnterpriseHome) : createApp(EnterpriseHome)
app.mount('#home-app')
