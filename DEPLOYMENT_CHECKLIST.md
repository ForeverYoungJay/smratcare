# 部署前检查清单

## 配置与安全
- [ ] `docker-compose.yml` 已配套 `.env` 并使用真实环境变量
- [ ] 本机/CI 已通过 `./scripts/check_runtime_requirements.sh`
- [ ] `JWT_SECRET` 已替换为 32 位以上强随机字符串
- [ ] 数据库密码和 root 密码均已替换默认值
- [ ] 生产环境不暴露 MySQL/Redis 到公网
- [ ] 正式环境禁用 `mock fallback / 本地免登录 / 人工充值回退`

## 数据库与迁移
- [ ] 已确认初始化方式（`docker/mysql/init` 与 Flyway）
- [ ] 已确认是否保留 `docker/mysql/init/2_data.sql`（生产建议移除或改为空文件）
- [ ] 首次部署后检查 Flyway 执行状态与 schema 版本
- [ ] 已验证字符集为 `utf8mb4`

## 构建与发布
- [ ] 后端测试通过：`mvn test`
- [ ] 后端镜像可构建（`Dockerfile`）
- [ ] 前端镜像可构建（`admin-web/Dockerfile`）
- [ ] `docker compose up -d --build` 可正常拉起全部容器
- [ ] 已使用 `.env.prod.example` 生成生产 `.env`，并填入真实微信/短信/OSS 参数

## 运行验证
- [ ] `/api/auth/login` 返回 `code=0`（有效账号）
- [ ] 前端首页可访问并可正常调用 `/api/**`
- [ ] 上传与访问 `/uploads/**` 正常
- [ ] OpenAPI 可访问：`/v3/api-docs`
- [ ] 家属端能力状态页显示正式能力，不存在 `MOCK/OFF/BIND_REQUIRED` 的未处理高风险项

## 运维与恢复
- [ ] MySQL 已配置备份策略（全量 + 增量）
- [ ] 容器日志采集与错误告警已配置
- [ ] 已准备回滚方案（镜像版本或 Git tag）
