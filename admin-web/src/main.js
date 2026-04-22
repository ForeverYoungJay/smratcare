import { createApp } from 'vue';
import { createPinia } from 'pinia';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import 'ant-design-vue/dist/reset.css';
import './styles/global.less';
import App from './App.vue';
import router from './router';
import { setupAntd } from './plugins/antd';
import { ensureAdminPlugins, isAdminRoute } from './plugins/adminPlugins';
import { permission, permissionCode } from './directives';
dayjs.locale('zh-cn');
const app = createApp(App);
app.use(createPinia());
app.use(router);
setupAntd(app);
app.directive('permission', permission);
app.directive('permission-code', permissionCode);
router.beforeEach(async (to) => {
    if (isAdminRoute(to.path)) {
        await ensureAdminPlugins(app);
    }
});
app.mount('#app');
