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
- `docker-compose.yml`：本地一键启动（MySQL/Redis/后端/前端）
- `docker-compose.prod.yml`：生产 compose（环境变量注入）
- `docker-compose.aliyun.yml`：阿里云 ECS 场景 compose

## 本地开发

### 1) 启动依赖与服务（推荐）
```bash
docker compose up -d --build
```

默认端口：
- 前端：`http://localhost:5173`
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

## 数据库说明
- 后端运行时会执行 Flyway（`src/main/resources/db/migration`）。
- MySQL init 脚本只在 **MySQL 新数据卷首次初始化** 时执行：
  - 本地 `docker-compose.yml`：`1_schema.sql` + `2_data.sql`
  - 生产 `docker-compose.prod.yml` / `docker-compose.aliyun.yml`：仅 `1_schema.sql`
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
- 阿里云部署：`DEPLOY_ALIYUN.md`
- 部署检查清单：`DEPLOYMENT_CHECKLIST.md`
- 测试执行说明：`TESTING.md`
- 物资中心合并说明：`docs/MATERIAL_CENTER_MERGE.md`

## 备注
- 上传文件默认映射目录：`output/uploads`（容器内 `/app/uploads`）
- 合同签署与附件上传统一在“营销管理-合同签约/合同管理”完成；“长者管理-合同与票据”为只读展示页
- 月账单生成默认仅对已签署合同长者生效，未签署合同长者会被跳过
- 默认 JWT 密钥仅用于开发环境，生产必须替换为高强度随机值
