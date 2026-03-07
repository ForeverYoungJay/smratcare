# 初始化账号矩阵（6部门 × 3角色 + ADMIN/SYS_ADMIN）

> 适用范围：`docker/mysql/init/2_data.sql` 的新库初始化数据（默认密码均为 `123456`）。

## 1) 管理账号

| 用户名 | 姓名 | 部门 | 角色 |
| --- | --- | --- | --- |
| `admin` | Admin | 行政人事部（HR） | `ADMIN` + `SYS_ADMIN` |

## 2) 六部门账号（每部门：员工 / 部长 / 院长管理层）

| 部门 | 员工账号 | 部长账号 | 管理层账号（DIRECTOR） |
| --- | --- | --- | --- |
| 护理部 | `nursing_emp` | `nursing_minister` | `nursing_director` |
| 医务部 | `medical_emp` | `medical_minister` | `medical_director` |
| 财务部 | `finance_emp` | `finance_minister` | `finance_director` |
| 后勤部 | `logistics_emp` | `logistics_minister` | `logistics_director` |
| 行政人事部 | `hr_emp` | `hr_minister` | `hr_director` |
| 市场部 | `marketing_emp` | `marketing_minister` | `marketing_director` |

## 3) 角色编码对照

- 护理：`NURSING_EMPLOYEE` / `NURSING_MINISTER`
- 医务：`MEDICAL_EMPLOYEE` / `MEDICAL_MINISTER`
- 财务：`FINANCE_EMPLOYEE` / `FINANCE_MINISTER`
- 后勤：`LOGISTICS_EMPLOYEE` / `LOGISTICS_MINISTER`
- 行政人事：`HR_EMPLOYEE` / `HR_MINISTER`
- 市场：`MARKETING_EMPLOYEE` / `MARKETING_MINISTER`
- 全院管理层：`DIRECTOR`
- 系统管理：`ADMIN`、`SYS_ADMIN`

## 4) 存量库升级说明

- `V146__admin_department_to_hr.sql`：修复 `admin` 默认部门到 HR。
- `V147__seed_demo_staff_for_6_departments_3_roles.sql`：为示例机构补齐 6 部门 3 角色账号及绑定（幂等）。

## 5) 验证建议

- 静态验证初始化 SQL 完整性：

```bash
./scripts/rbac_seed_audit.sh
```

- 权限表达式回归审计：

```bash
./scripts/rbac_audit.sh
```
