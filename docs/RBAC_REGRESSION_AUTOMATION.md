# RBAC 回归自动化（菜单 + 接口 + 越权）

## 目标

针对“6部门 × 3角色 + SYS_ADMIN”做最小可执行回归，覆盖：

- 菜单/路由访问规则（静态断言）
- 核心接口 `403/非403` 权限行为
- 跨部门越权阻断

## 执行前提

- 后端服务可访问：默认 `http://localhost:8080`
- 已初始化演示账号（`docker/mysql/init/2_data.sql`）
- 默认密码：`123456`

## 一键执行

```bash
./scripts/rbac_regression_smoke.sh
```

如后端地址不同：

```bash
BASE_URL=http://127.0.0.1:8080 ./scripts/rbac_regression_smoke.sh
```

## 覆盖用例（关键 3 组）

1. 财务审批权限（`/api/finance/fee/.../review`）
   - `finance_emp` -> `403`
   - `finance_minister` -> 非 `403`

2. 护理执行权限（`/api/nursing/service-plans/page`）
   - `nursing_emp` -> `403`
   - `nursing_minister` -> 非 `403`

3. 市场线索/计划权限（`/api/marketing/plans`）
   - `marketing_emp` 创建 -> `403`
   - `marketing_minister` 创建 -> 非 `403`
   - `finance_emp` 访问营销列表 -> `403`（跨部门越权拦截）

## 说明

- 脚本对“允许访问”的判断是“非 `403`”，因为部分写接口可能因测试数据不完整返回 `400/404`，这仍说明权限已放通。
- 若登录失败，优先检查 Flyway 是否执行到 `V147` 与初始化账号是否存在。
