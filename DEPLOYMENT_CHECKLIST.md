# 部署前检查清单

## 配置与安全
- [ ] `application-prod.yml` 已配置并通过环境变量注入敏感信息
- [ ] `JWT_SECRET` 已替换为高强度随机值（不小于 32 字符）
- [ ] 生产环境关闭测试数据导入与调试日志

## 数据库与缓存
- [ ] MySQL 已初始化（UTF‑8）
- [ ] 数据库连接账号权限最小化（非 root 账号）
- [ ] Redis 可用，黑名单功能正常

## 构建与发布
- [ ] `mvn test` 全绿
- [ ] `mvn -DskipTests package` 生成 `target/care-task-1.0.0.jar`
- [ ] `docker-compose.prod.yml` 环境变量已填

## 运行验证
- [ ] 服务启动成功，日志无 ERROR
- [ ] 关键接口冒烟：登录、护理任务、商城预览/下单、生命体征
- [ ] OpenAPI 可访问：`/v3/api-docs`

## 备份与监控
- [ ] MySQL 备份策略（每日/每周）
- [ ] 关键指标监控（连接池、错误率、慢查询）
