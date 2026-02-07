import { createApp } from 'vue';
import Antd from 'ant-design-vue';
import zhCN from 'ant-design-vue/es/locale/zh_CN';
import 'ant-design-vue/dist/reset.css';
import './styles.css';
import App from './App.vue';
import router from './router';

const app = createApp(App);
app.use(router);
app.use(Antd, { locale: zhCN });
app.mount('#app');
