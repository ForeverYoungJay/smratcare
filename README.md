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

说明：当前后端对 `verifyCode` 仅保留占位校验逻辑。

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

## 备注
- 上传文件默认映射目录：`output/uploads`（容器内 `/app/uploads`）
- 合同签署与附件上传统一在“营销管理-合同签约/合同到期管理”完成；“长者管理Resident 360-合同与票据”为只读展示页
- 月账单生成默认仅对已签署合同长者生效，未签署合同长者会被跳过
- 默认 JWT 密钥仅用于开发环境，生产必须替换为高强度随机值
