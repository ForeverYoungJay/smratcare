# RBAC 验收清单（6部门 × 3角色 + SYS_ADMIN）

## 1. 测试账号建议

每个部门至少准备 2 个账号：

- 员工：`<dept>_employee_test`
- 部长：`<dept>_minister_test`

全院账号：

- `director_test`（`DIRECTOR`）
- `sys_admin_test`（`SYS_ADMIN`）

部门编码建议：

- 医疗：`medical`
- 护理：`nursing`
- 财务：`finance`
- 后勤：`logistics`
- 市场：`marketing`
- 行政人事：`hr`

## 2. 通用验收步骤

对每个账号执行：

1. 登录后台，确认菜单仅出现应有模块。
2. 随机访问无权限路由，确认跳转 `403`。
3. 调用无权限接口（Postman），确认 `403`。
4. 刷新页面后再次验证菜单与按钮权限一致（避免前端缓存误判）。

## 3. 关键模块验收（建议最小集）

### 3.1 行政人事（`HR_*`）

- 页面：
  - `/hr/workbench` 可见（员工/部长/院长/超管）
  - `/system/*` 仅 `DIRECTOR`/`SYS_ADMIN`/`ADMIN` 可见
- 接口：
  - `/api/schedule/*`：`HR_MINISTER` 及以上可增删改查
  - `/api/attendance/page`：`HR_MINISTER` 及以上可查
  - `/api/admin/staff*`：`HR_MINISTER` 及以上可操作

### 3.2 护理（`NURSING_*`）

- 页面：
  - `/care/*` 员工可见，部长可管理
- 接口：
  - `/api/nursing/*`：`NURSING_MINISTER` 及以上可执行管理接口
  - 关键接口（例如服务计划、班次模板）员工调用应返回 `403`

### 3.3 财务（`FINANCE_*`）

- 页面：
  - `/finance/*` 员工可见，部长可审核
- 接口：
  - `/api/finance/fee/*`：`FINANCE_EMPLOYEE/FINANCE_MINISTER` 可访问
  - 审核/确认接口（`/review`、`/confirm`）仅 `FINANCE_MINISTER` 及以上可操作

### 3.4 市场（`MARKETING_*`）

- 页面：
  - `/marketing/*` 员工可见，部长可管理流程
- 接口：
  - `/api/marketing/plans/*`：员工可查阅、部长可提交流程/审批类操作
  - `/api/marketing/report/*`：员工可看报表，数据修复仅部长及以上

### 3.5 医疗/护理共管（评估与医护状态变更）

- 页面：
  - `/assessment/*` 仅医疗/护理角色可见（含部长）
  - `/elder/status-change/medical-outing`、`/elder/status-change/death-register` 仅医护角色可见
- 接口：
  - `/api/assessment/*`：医护员工可查，模板管理修改仅部长及以上
  - `/api/elder/lifecycle/medical-outing*`：医护员工可建档/返院，删除仅部长及以上
  - `/api/elder/lifecycle/death-register*`：医护员工可建档/更正，作废/删除仅部长及以上

## 4. 角色边界断言（必须通过）

- 任意部门员工不可操作其他部门“管理动作”（审批、删除、组织管理）。
- 任意部门部长可完成本部门管理闭环。
- `DIRECTOR` 可跨部门管理，但不应看到 `SYS_ADMIN` 专属系统能力（如你后续单独划分）。
- `SYS_ADMIN` 可执行全量管理动作。

## 5. 回归建议顺序

1. 先验收菜单与路由（前端）
2. 再验收接口 `403/200`（后端）
3. 最后验收跨模块联动（例如 OA 审批、评估回写、退住审核）

