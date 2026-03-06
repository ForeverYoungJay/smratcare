# RBAC 闭环矩阵（6部门 + 3层级 + SYS_ADMIN）

适用机构：弋阳龟峰颐养中心  
目标：实现“员工可作业、部长可管理、院长/管理层可统筹、`SYS_ADMIN` 全域可控”的权限闭环。

## 1. 角色分层

- `*_EMPLOYEE`：部门员工（执行层）
- `*_MINISTER`：部门部长（管理层）
- `DIRECTOR`：院长/管理层（全院经营管理）
- `SYS_ADMIN`：系统超管（系统与组织级全权限）

系统兼容规则：

- 任一部门角色自动兼容 `STAFF` 能力（前后端归一化）。
- `DIRECTOR`、`SYS_ADMIN` 自动兼容 `ADMIN` 能力。
- 路由 `roles=['ADMIN']` 在模块内默认解释为“对应部门部长及以上”。

## 2. 六部门角色模板

- 医疗部：`MEDICAL_EMPLOYEE` / `MEDICAL_MINISTER`
- 护理部：`NURSING_EMPLOYEE` / `NURSING_MINISTER`
- 财务部：`FINANCE_EMPLOYEE` / `FINANCE_MINISTER`
- 后勤部：`LOGISTICS_EMPLOYEE` / `LOGISTICS_MINISTER`
- 市场部：`MARKETING_EMPLOYEE` / `MARKETING_MINISTER`
- 行政人事部：`HR_EMPLOYEE` / `HR_MINISTER`

## 3. 能力矩阵（建议基线）

| 能力类别 | 员工（`*_EMPLOYEE`） | 部长（`*_MINISTER`） | `DIRECTOR` | `SYS_ADMIN` |
|---|---|---|---|---|
| 本部门列表/查询 | ✅ | ✅ | ✅ | ✅ |
| 本部门新增/编辑 | ✅（受业务字段限制） | ✅ | ✅ | ✅ |
| 本部门审核/审批 | ❌ | ✅ | ✅ | ✅ |
| 本部门删除/作废 | ❌（或仅软删除本人草稿） | ✅ | ✅ | ✅ |
| 跨部门查看 | ❌ | ❌（默认） | ✅ | ✅ |
| 组织架构/角色管理 | ❌ | ❌（默认） | ✅（按机构策略） | ✅ |
| 系统配置/字典 | ❌ | ❌（默认） | ✅（可配置） | ✅ |

## 4. 模块归属建议

| 模块 | 默认归属 | 员工可见 | 部长可见 | 说明 |
|---|---|---|---|---|
| 医护健康 (`/medical-care`, `/health`, `/medical`) | 医疗部 | ✅ | ✅ | 医疗部长可管理，员工作业 |
| 照护管理 (`/care`, `/nursing`) | 护理部 | ✅ | ✅ | 护理部长可审批 |
| 财务中心 (`/finance`) | 财务部 | ✅ | ✅ | 财务部长可审核和结算 |
| 后勤与物资 (`/logistics`, `/store`, `/material`, `/inventory`) | 后勤部 | ✅ | ✅ | 物资主数据建议部长维护 |
| 营销管理 (`/marketing`, `/crm`) | 市场部 | ✅ | ✅ | 部长可看报表与转化 |
| 行政人事 (`/hr`, `/oa`, `/schedule`, `/attendance`) | 行政人事部 | ✅ | ✅ | 部长可看全员排班考勤与审批 |
| 评估管理 (`/assessment`) | 医疗+护理 | ✅（医护） | ✅（医护部长） | 避免无关部门进入 |
| 系统管理 (`/system`) | 管理层 | ❌ | ❌（默认） | 建议 `DIRECTOR` + `SYS_ADMIN` |

## 5. 后端网关提权（兼容期策略）

为兼容历史 `@PreAuthorize("hasRole('ADMIN')")`，网关仅对“部长及以上”按模块前缀临时补 `ADMIN`：

- 人事模块：`/api/admin/hr`、`/api/hr`、`/api/schedule`、`/api/attendance`
- 人事组织接口：`/api/admin/staff*`、`/api/admin/departments`、`/api/admin/family`
- 护理模块：`/api/nursing`、`/api/care`
- 医疗模块：`/api/medical-care`、`/api/health`、`/api/medical`
- 医护共管：`/api/assessment`、`/api/elder/lifecycle/medical-outing*`、`/api/elder/lifecycle/death-register*`
- 财务模块：`/api/finance`
- 后勤模块：`/api/logistics`、`/api/store`、`/api/material`、`/api/inventory`、`/api/admin/product*`、`/api/admin/disease*`
- 市场模块：`/api/marketing`、`/api/crm`

补充说明（本轮）：

- 营销接口已从旧角色表达（`MANAGER/OPERATOR`）迁移到新模型（`MARKETING_EMPLOYEE` / `MARKETING_MINISTER`）。
- 财务费用管理控制器已从泛 `ADMIN/STAFF` 收敛到财务部门角色（员工/部长）+ 管理层。
- 评估控制器已从泛 `ADMIN/STAFF` 收敛到医护部门角色（医疗/护理员工与部长）+ 管理层。
- OA 任务与长者生命周期遗留接口已从泛 `ADMIN/STAFF` 收敛为显式部门角色集合。
- 后端鉴权表达式已清理 `hasRole('ADMIN')` 与 `hasAnyRole('ADMIN','STAFF')` 的遗留写法，统一为显式部门角色或管理层角色组合。
- 为兼容历史账号，旧营销角色自动映射：`OPERATOR -> MARKETING_EMPLOYEE`、`MANAGER -> MARKETING_MINISTER`（前后端归一化同时生效）。

## 6. 上线核对清单

- 后端遗留 `@PreAuthorize("hasRole('ADMIN')")` 已完成替换为显式 `hasAnyRole(...)`（本轮覆盖 `auth/hr/nursing/finance/schedule/store/survey/assessment/elder/report/crm` 关键控制器）。
- 执行 Flyway（含 `V132__rbac_roles_templates_and_sys_admin.sql`）。
- 给测试账号分配六部门的员工/部长角色各一组。
- 回归验证四类账号：
  - 部门员工：能看能做，不能审批/删除关键单据。
  - 部门部长：可管理本部门并审批。
  - `DIRECTOR`：可跨部门访问管理。
  - `SYS_ADMIN`：全域可访问。
- 重新登录获取新 JWT（角色归一化在 token 鉴权链中生效）。
