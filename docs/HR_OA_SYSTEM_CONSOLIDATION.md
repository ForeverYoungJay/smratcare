# 行政管理 / 人事行政工作台 / 系统管理 合并落地说明

## 目标

- 消除 `/oa` 与 `/hr/oa` 的重复业务入口，统一 OA 业务入口到 `/oa/*`。
- 收敛员工档案重复入口，统一到 `/hr/profile/basic`。
- 保留历史链接可访问（兼容重定向），避免上线后书签和外链失效。
- 强化前端跨页面数据同步，保证组织与账号变更后工作台实时刷新。

## 本次已落地改造

### 1) 路由与菜单收敛

- OA 保留业务入口：`/oa/*`。
- `/hr/oa/*` 改为兼容路由（隐藏）并重定向到标准 OA 路由。
- 原 HR 中与 OA 重叠的菜单不再显示。
- HR 内部新增可见分组：`/hr/compliance/*`（制度与合规，承载原 HR 专属制度页面）。
- 同组件多菜单再收敛一层：改为“主路由 + scene 参数”，其余路径仅保留隐藏兼容跳转。
  - 招聘：`/hr/recruitment/needs?scene=*`
  - 审批：`/hr/attendance/leave-approval?scene=*`
  - 培训：`/hr/development/records?scene=*`
  - 绩效：`/hr/performance/reports?scene=*`
  - 费用报销：`/hr/expense/records?scene=*`

### 2) 员工档案入口统一

- `/oa/staff` 改为兼容重定向到 `/hr/profile/basic`（隐藏）。
- `/hr/staff` 改为兼容重定向到 `/hr/profile/basic`（隐藏）。
- `/hr/training` 改为兼容重定向到 `/hr/development/records`（隐藏）。
- `/oa/training`、`/oa/reward-punishment` 改为兼容重定向到 HR 标准入口（隐藏）。
- `/system/staff` 改为兼容重定向到 `/hr/profile/account-access`（隐藏）。

### 3) 工作台跳转统一

- 人事行政工作台中 OA 快捷入口已统一到 `/oa/*` 标准路径。
- 制度类快捷入口统一到 `/hr/compliance/*`。
- 顶部“账号与赋权”已调整到人事入口 `/hr/profile/account-access`。

### 4) 人事权限重发（账号/密码/领导设置）

- 新增人事专属入口：`/hr/profile/account-access`（页面复用账号管理页）。
- 用于人事完成：
  - 账号新建与初始密码设置
  - 直接领导/间接领导设置
  - 账号角色与监管链调整
- 显式角色授权：`HR_EMPLOYEE` / `HR_MINISTER` / `DIRECTOR` / `SYS_ADMIN` / `ADMIN`。

### 5) 数据同步增强

- LiveSync 主题规则新增：
  - `/api/admin/staff-roles*` -> `system/hr/oa`
  - `/api/admin/staff*` -> `system/hr/oa`
  - `/api/admin/departments*` -> `system/hr/oa`
- 目的：账号、角色、组织架构变更后，系统管理、人事工作台、OA 相关页面均可触发刷新。

## 兼容策略

- 兼容期建议：30 天。
- 兼容期内保留旧路径重定向，不在菜单展示。
- 兼容期结束后可删除以下路由分支：
  - `/hr/oa/*` 兼容路由组
  - `/hr/recruitment/*`（除 `needs`）
  - `/hr/attendance/*`（除 `leave-approval`）
  - `/hr/expense/*`（除 `records`、`meal-fee`、`electricity-fee`、`approval-flow`）
  - `/hr/development/*`（除 `records`）
  - `/hr/performance/*`（除 `reports`、`scoring-rules`、`reward-punishment`）
  - `/oa/staff`
  - `/hr/staff`
  - `/hr/training`
  - `/system/staff`
  - `/oa/training`
  - `/oa/reward-punishment`

## 回归测试清单

### 菜单与导航

- 角色 `ADMIN` 登录后：
  - `/oa` 菜单可见，且包含通知/任务/待办/审批/文档等。
  - `HR` 菜单不再出现 OA 重复菜单，仅保留“制度与合规”。
  - `HR` 菜单中的招聘/审批/培训/绩效为单主入口，场景通过 `scene` 参数切换。
- 访问旧链接：
  - `/hr/oa/notices` 能跳到 `/oa/notice`。
  - `/hr/oa/tasks` 能跳到 `/oa/work-execution/task`。
  - `/oa/staff`、`/hr/staff` 能跳到 `/hr/profile/basic`。
  - `/system/staff` 能跳到 `/hr/profile/account-access`。
  - `/hr/recruitment/candidates`、`/hr/attendance/shift-change`、`/hr/development/plans`、`/hr/performance/nursing` 均能跳转到主入口并带 `scene`。
  - `/hr/expense/training-reimburse`、`/hr/expense/subsidy`、`/hr/expense/salary-subsidy` 均能跳转到 `/hr/expense/records?scene=*`。

### 业务功能

- HR 工作台卡片跳转均可打开目标页面。
- `/hr/profile/account-access` 的“账号与赋权”页面可由人事正常新建账号、设置密码与领导关系。
- OA 页面增删改后，相关页面自动刷新（同标签页与跨标签页）。

### 权限

- 非 `ADMIN` 账号访问系统管理与兼容路由时仍受原权限控制。
- 隐藏路由仅用于兼容，不应在菜单树中展示。

## 发布步骤

1. 合并代码并执行前端构建。
2. 冒烟验证以上“回归测试清单”。
3. 发布公告：说明 OA 入口统一到 `/oa/*`，HR 侧制度页面迁至 `/hr/compliance/*`。
4. 30 天后根据访问日志移除兼容路由。
