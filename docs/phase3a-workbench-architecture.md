# Phase 3A：岗位工作台架构

## 目标

工作台只回答三个问题：当前岗位是谁、今天应先处理什么、这些数字来自哪个真实业务接口。路由权限仍是最终访问边界，工作台不会通过切换“视角”扩大可见范围。

## 角色解析

`admin-web/src/workbench/model.ts` 是工作台岗位语义的唯一入口。它复用现有角色协议并按以下优先级解析：

1. `SYS_ADMIN`
2. `DIRECTOR`
3. `ADMIN`
4. 六部门部长
5. 六部门员工
6. 兼容工作人员

部门岗位进一步区分 `EMPLOYEE` 与 `MINISTER`，并提供部门名称、工作对象名称、部门主入口和管理视角标记。旧 `OPERATOR`、`MANAGER` 仍通过现有角色兼容规则映射到市场岗位。

## 数据适配

`admin-web/src/workbench/dataSources.ts` 将六个现有汇总接口转换成统一的指标快照：

| 部门 | 真实数据源 |
| --- | --- |
| 护理部 | `/api/care/tasks/summary` |
| 医务部 | `/api/medical-care/workbench/summary` |
| 财务部 | `/api/finance/workbench/overview` |
| 后勤部 | `/api/logistics/workbench/summary` |
| 行政人事部 | `/api/admin/hr/workbench/summary` |
| 市场部 | `/api/marketing/report/workbench-summary` |

加载前必须通过对应页面的统一路由权限检查。结果明确区分 `ready`、`empty`、`forbidden` 和 `error`；缺失值显示为 `--`，不会补造演示数字。

## Phase 3B 扩展规则

- 每个部门在现有适配器上增加员工版和部长版指标组合，不重新判断角色字符串。
- 指标入口必须先通过 `resolveRouteAccess`，不可访问的指标不渲染。
- 优先复用现有汇总、分页和任务接口；缺少后端能力时使用明确空状态并记录 TODO。
- 通用个人区块（待办、日程、考勤、最近访问）继续复用，部门页面只实现业务差异。
- 员工版聚焦本人任务和工作对象；部长版增加审批、异常、分配、人员执行和部门指标。
