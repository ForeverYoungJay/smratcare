# Phase 4：岗位导航与工作台验收报告

验收日期：2026-08-23  
验收环境：`http://127.0.0.1:8081`，生产构建前端容器 + 本地后端与数据库。

## 1. 角色浏览器矩阵

| 角色 | 默认首页 | 岗位首页 | 权限隔离 | 刷新恢复 | 结果 |
| --- | --- | --- | --- | --- | --- |
| NURSING_EMPLOYEE | `/workbench/overview` | 我的今日护理 | 无审批、无其他部门，越权 403 | 通过 | 通过 |
| NURSING_MINISTER | `/workbench/overview` | 护理部今日运行 | 有本部门审批，无其他部门 | 通过 | 通过 |
| MEDICAL_EMPLOYEE | `/workbench/overview` | 我的今日医务 | 无审批、无其他部门，越权 403 | 通过 | 通过 |
| MEDICAL_MINISTER | `/workbench/overview` | 医务部今日运行 | 有本部门审批，无其他部门 | 通过 | 通过 |
| FINANCE_EMPLOYEE | `/workbench/overview` | 今日收费与交班 | 无审批、无其他部门，越权 403 | 通过 | 通过 |
| FINANCE_MINISTER | `/workbench/overview` | 财务部今日运行 | 有本部门审批，无其他部门 | 通过 | 通过 |
| LOGISTICS_EMPLOYEE | `/workbench/overview` | 今日后勤保障 | 无审批、无其他部门，越权 403 | 通过 | 通过 |
| LOGISTICS_MINISTER | `/workbench/overview` | 后勤部今日运行 | 有本部门审批，无其他部门 | 通过 | 通过 |
| HR_EMPLOYEE | `/workbench/overview` | 我的行政人事协作 | 请假审批直访 403 | 通过 | 通过 |
| HR_MINISTER | `/workbench/overview` | 行政人事部今日运行 | 有本部门审批，无其他部门 | 通过 | 通过 |
| MARKETING_EMPLOYEE | `/workbench/overview` | 今日客户跟进 | 渠道报表直访 403 | 通过 | 通过 |
| MARKETING_MINISTER | `/workbench/overview` | 市场部今日运行 | 有本部门审批和报表 | 通过 | 通过 |
| DIRECTOR | `/portal` | 院长运营指挥台 | 无系统配置菜单，`/system` 为 403 | 通过 | 通过 |
| ADMIN | `/portal` | 机构经营与安全总览 | `/system/role` 为 403 | 通过 | 通过 |
| SYS_ADMIN | `/system` | 系统管理首页 | 无六部门业务菜单，`/care/today` 为 403 | 通过 | 通过 |

浏览器测试使用临时 QA 账号；验收结束后账号和角色关联已全部删除。

## 2. 导航、搜索与兼容性

- 岗位导航一级菜单不超过 9 个、最多两层，由 `jobNavigation.test.ts` 全角色覆盖。
- “查看全部授权功能”仅改变当前导航视图；刷新后恢复岗位导航，且不能扩大直接 URL 权限。
- 护理部长搜索“系统管理”无结果；搜索“护理记录”可进入 `/care/service/nursing-records`。
- 工作台“今日待办”可进入 `/workbench/todo`。
- `/oa/todo?status=OVERDUE#mine` 正确跳转至 `/workbench/todo?status=OVERDUE#mine`。
- `/material/inbound?source=bookmark` 正确跳转至 `/logistics/storage/inbound?source=bookmark`。
- 未知地址显示 404，不会被误判为 403。

## 3. 布局

- 1280×720 桌面办公视口：无横向溢出，侧栏、页面内容和系统管理入口正常。
- 688×843 窄屏视口：无横向溢出，工作台和导航可用。
- 浏览器验收工具固定桌面视口为 1280×720，无法直接设置 1366×768；1280 宽度属于更严格的桌面下界，并命中与 1366 相同的响应式规则。

## 4. 自动化验证

- `npm run test:unit`：32 个测试文件、150 项测试通过。
- `npm run check:navigation`：634 条路由、397 个链接、0 个未解析链接。
- `npm run build`：生产构建通过。
- `npm run lint`：仓库尚未配置实际 lint 工具，脚本仅输出 `no lint configured`。
- `npm run typecheck`：仍有公共请求响应类型、既有查询模型和历史测试类型等存量诊断；Phase 3B/4 修改文件未新增诊断。

## 5. 结论

Phase 4 的岗位首页、菜单、搜索、直接访问、旧链接、刷新恢复和布局验收通过。当前剩余项属于既有全仓 TypeScript 与 lint 基础设施技术债，不影响本次岗位导航与工作台功能验收。
