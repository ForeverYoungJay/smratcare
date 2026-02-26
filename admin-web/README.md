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

## Docker
构建前端镜像：
```bash
docker build -t smartcare-admin:local ./admin-web
```

单独运行（需可访问后端 `backend:8080`）：
```bash
docker run --rm -p 5173:80 smartcare-admin:local
```

说明：
- 镜像使用多阶段构建：`node:20-alpine` 构建，`nginx:1.27-alpine` 运行。
- `nginx.conf` 会将 `/api/` 和 `/uploads/` 反向代理到 `http://backend:8080`。
- 若非 compose 环境运行，请确保容器内能解析 `backend` 主机名，或调整 `nginx.conf` 的 upstream。

## 说明
- 登录接口：`POST /api/auth/login`
- 家属登录接口：`POST /api/auth/family/login`（后端字段：`orgId`、`phone`、`verifyCode`）
- Token 保存在 localStorage，并由 Axios 请求拦截器自动携带
- 401 自动跳转登录页
