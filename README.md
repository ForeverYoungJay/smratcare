# 智养云（SmartCare）

智慧养老机构一体化管理系统，前后端分离，覆盖长者生命周期、护理、健康、营销、财务、OA、物资与库存等业务域。

## 技术栈
- 后端：Java 17、Spring Boot 3.2.6、Spring Security、MyBatis-Plus、Flyway、Redis、MySQL 8
- 前端：Vue 3、Vite、TypeScript、Pinia、Vue Router、Ant Design Vue
- 部署：Docker / Docker Compose

## 仓库结构
- `src/main/java`：后端业务代码
- `src/main/resources/db/migration`：MySQL Flyway 迁移脚本（后端数据库演进基准）
- `src/test`：后端测试（H2 + SpringBootTest）
- `admin-web`：管理后台前端
- `docker-compose.yml`：唯一 Docker 编排文件（MySQL/Redis/后端/前端）
- `.env.example`：环境变量模板

## 本地开发

### 1) 启动依赖与服务（推荐）
首次使用先准备环境变量（可直接使用默认值）：
```bash
cp .env.example .env
```

启动服务：
```bash
docker compose up -d --build
```

默认端口：
- 前端：`http://localhost`
- 后端：`http://localhost:8080`
- MySQL：`127.0.0.1:3307`
- Redis：`127.0.0.1:6379`

### 2) 分开启动（可选）
仅启动数据库和缓存：
```bash
docker compose up -d mysql redis
```

本地启动后端：
```bash
mvn spring-boot:run
```

本地启动前端：
```bash
cd admin-web
npm install
npm run dev
```
本地 `npm run dev` 默认端口仍为 `5173`，仅 `docker compose` 映射为 `80`。

## 数据库说明
- 后端运行时会执行 Flyway（`src/main/resources/db/migration`）。
- MySQL init 脚本只在 **MySQL 新数据卷首次初始化** 时执行：
  - `docker-compose.yml`：`1_schema.sql` + `2_data.sql`
- 如需重置 MySQL 初始化状态：
```bash
docker compose down -v
docker compose up -d
```

## 常用命令
后端测试：
```bash
mvn test
```

后端打包：
```bash
mvn -DskipTests package
```

前端构建：
```bash
cd admin-web
npm run build
```

Flyway 版本冲突检查：
```bash
./scripts/check_flyway_versions.sh
```

项目一键静态自检（Flyway 版本冲突 + 前端导航路由）：
```bash
./scripts/project_self_check.sh
```

RBAC 权限模型审计（旧权限表达式回归检查）：
```bash
./scripts/rbac_audit.sh
```

RBAC 初始化账号/部门/角色静态审计（6部门×3角色）：
```bash
./scripts/rbac_seed_audit.sh
```

RBAC 权限回归冒烟（菜单规则+接口403/越权）：
```bash
./scripts/rbac_regression_smoke.sh
```

RBAC 一级菜单矩阵导出（按角色查看可访问菜单）：
```bash
./scripts/rbac_menu_matrix.sh
```

家属聚合接口回归（Postman + HTTP）：
- `postman_family_aggregation_regression.collection.json`
- `docs/FAMILY_AGGREGATION_REGRESSION.http`

家属链路运维巡检（短信/通知/充值健康概览）：
```bash
./scripts/family_ops_health_check.sh
```

企业首页发布前内容检查（占位信息/链接/联系方式）：
```bash
./scripts/enterprise_home_content_check.sh
```

企业首页运营待办导出（更新节奏/占位风险/本周任务）：
```bash
./scripts/enterprise_ops_todo.sh
```

企业首页内容时效巡检（更新时间/复核日期/新闻新鲜度）：
```bash
./scripts/enterprise_home_freshness_check.sh
```

Flyway 状态巡检（只检查，不修复）：
```bash
./scripts/flyway_status.sh
```

Flyway 一键自检+修复（检查失败迁移、清理 success=0、重启后端）：
```bash
./scripts/flyway_self_heal.sh
```

如使用自定义 env/compose 文件：
```bash
./scripts/flyway_self_heal.sh --env-file .env.local.prod -f docker-compose.prod.yml
```

## 接口与鉴权
登录接口：
- `POST /api/auth/login`
- `POST /api/auth/family/sms-code/send`
- `POST /api/auth/family/sms-code/verify`
- `POST /api/auth/family/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`

管理员登录请求示例：
```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}'
```

管理员登录请求字段（`LoginRequest`）：
- `username`：`string`，必填
- `password`：`string`，必填

管理员登录响应字段（`LoginResponse`）：
- `token`：`string`
- `roles`：`string[]`
- `permissions`：`string[]`
- `staffInfo`：对象（`id/orgId/departmentId/username/realName/phone/status`）

角色模型（新增）：
- 超级管理员：`SYS_ADMIN`（自动具备 `ADMIN` 能力）
- 管理层：`DIRECTOR`（自动具备 `ADMIN` 能力）
- 六部门角色模板：`MEDICAL_*`、`NURSING_*`、`FINANCE_*`、`LOGISTICS_*`、`MARKETING_*`、`HR_*`
- 兼容规则：任一部门角色自动具备 `STAFF` 能力；模块内 `ADMIN` 兼容默认收敛为“对应部门部长及以上”
- 权限可视化入口：系统管理 → `权限总览`（路由：`/system/permission-overview`）

家属登录请求字段（`FamilyLoginRequest`）：
- `orgId`：`number`，必填
- `phone`：`string`，必填
- `verifyCode`：`string`，必填

家属短信验证码发送请求（`FamilySmsCodeSendRequest`）：
- `orgId`：`number`，必填
- `phone`：`string`，必填
- `scene`：`string`，选填（默认 `LOGIN`）

说明：
- `/api/auth/family/login` 已切换为真实 OTP 校验，默认验证码有效期 5 分钟。
- 小程序敏感信息二次验证可使用：
  - `POST /api/family/security/sms-code/send`
  - `POST /api/family/security/sms-code/verify`

统一响应结构：
- `code`：`number`（`0` 表示成功）
- `message`：`string`
- `data`：业务数据

OpenAPI：
- JSON：`http://localhost:8080/v3/api-docs`
- YAML：`http://localhost:8080/v3/api-docs.yaml`

## 部署文档
- 部署检查清单：`DEPLOYMENT_CHECKLIST.md`
- 测试执行说明：`TESTING.md`
- 物资中心合并说明：`docs/MATERIAL_CENTER_MERGE.md`
- RBAC 闭环矩阵：`docs/RBAC_CLOSURE_MATRIX.md`
- RBAC 验收清单：`docs/RBAC_ACCEPTANCE_CHECKLIST.md`
- RBAC 自动化回归：`docs/RBAC_REGRESSION_AUTOMATION.md`
- RBAC 菜单矩阵导出：`docs/RBAC_MENU_MATRIX.md`
- 初始化账号矩阵：`docs/INIT_STAFF_MATRIX.md`
- 企业首页内容治理：`docs/ENTERPRISE_HOME_CONTENT_GOVERNANCE.md`
- 家属旧接口下线计划：`docs/FAMILY_LEGACY_API_SUNSET_PLAN.md`
- 家属支付/OSS 配置说明：`docs/FAMILY_PAYMENT_OSS_WECHAT_CONFIG.md`

## 备注
- 上传文件默认映射目录：`output/uploads`（容器内 `/app/uploads`）
- 文件存储支持 `local/oss` 两种 provider；评估报告 PDF 可上传至 OSS 并通过家属聚合接口返回真实 URL
- 家属微信支付链路：`/api/family/payment/wechat/prepay` + `/api/family/payment/wechat/notify` + `/api/family/payment/recharge-orders/*`
- 家属微信通知链路：`family_notify_log`（投递日志）+ `FamilyWechatNotifyRetryScheduler`（失败重试）
- 家属微信通知 openId 绑定：`/api/family/wechat/notify/bind-openid`（建议登录后调用，提升真实投递成功率）
- 家属链路状态总览：`/api/family/capabilities/status`（短信/通知/支付/安全/旧接口下线进度）
- 管理员运维巡检接口：`/api/admin/family/ops/health`（默认近24小时短信/通知/充值链路健康）
- 家属支付增强接口：`/api/family/payment/guard`（支付保障总览） + `/api/family/todo-center`（消息/缴费/日程待办聚合）
- 家属运营增强接口：`/api/family/dashboard/weekly-brief`（周报摘要） + `/api/family/communication/templates`（沟通快捷模板）
- 家属沟通增强：`/api/family/communication/messages` 支持 `voice` 消息附带 `mediaUrl/mediaName/mediaDurationSec/transcript`
- 家属互动增强：`/api/family/activity-albums/{id}/comments` 支持活动评论列表与发布
- 家属安全增强：`/api/family/security/password/set` + `/api/family/security/password/verify` 支持独立密码二次验证
- 家属反馈闭环：`/api/family/feedback` 支持 `feedbackType`（评价/投诉）+ `/api/family/feedback/records` 状态追踪
- 家属评估增强：`/api/family/assessment-reports/generate-ai` 可生成 AI 中医体质/心血管评估
- 家属支付增强：`/api/family/payment/auto-pay` 首次开启需 `authorizeConfirmed=true`，并由定时任务执行余额自动结算
- 家属提效增强：`/api/family/messages/read-all`（消息一键已读） + `/api/family/dashboard/weekly-brief/history`（周报历史）
- 家属预警增强：`/api/family/messages/summary`（未读分层/类型统计/紧急摘要）
- 家属消息筛选增强：`/api/family/messages/page` 支持 `level/type/unreadOnly` 过滤参数
- 家属待办闭环增强：`/api/family/todo-center/action`（DONE/SNOOZE/UNDO）+ 小程序待办中心操作按钮
- 管理端家属绑定已迁移：`/api/admin/family/relations/bind`（替代旧 `/api/family/bindElder` 的后台使用场景）
- 微信回调入账已增加幂等处理与流水来源索引（Flyway: `V154__elder_account_log_source_index.sql`）
- 微信支付回调已增加签名校验后的防重放拦截（时间窗+报文指纹），避免重复通知导致重复业务处理
- 启动时会对家属支付与 OSS 关键配置做告警检查（`FamilyPortalStartupValidator`）
- 合同签署与附件上传统一在“营销管理-合同签约/合同到期管理”完成；“长者管理Resident 360-合同与票据”为只读展示页
- 月账单生成默认仅对已签署合同长者生效，未签署合同长者会被跳过
- 默认 JWT 密钥仅用于开发环境，生产必须替换为高强度随机值
