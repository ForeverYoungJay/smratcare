# 智养云管理后台（前端）

## 技术栈
- Vue 3 + Vite + TypeScript
- Pinia + Vue Router
- Ant Design Vue
- Axios

## 开发
```
cd admin-web
npm install
npm run dev
```

## 说明
- 登录接口：`POST /api/auth/login`
- Token 保存在 localStorage，并由 Axios 请求拦截器自动携带
- 401 自动跳转登录页
