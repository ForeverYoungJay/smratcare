# 页面精简验证记录

后续统一布局的代码覆盖、测试与截图见[布局统一实施记录](/Users/yang/smartcare/docs/page-audit-2026-11-15/LAYOUT_STANDARDIZATION.md)。

日期：2026-11-15。对应 [精简方案与实施结果](/Users/yang/smartcare/docs/page-audit-2026-11-15/SIMPLIFICATION_PLAN.md)。本轮修改位于当前工作区，未发布到生产。

## 自动检查

| 检查 | 命令 | 结果 |
|---|---|---|
| 全量前端单元测试 | `npm --prefix admin-web run test:unit -- --reporter=dot` | 41 个测试文件，351 项通过 |
| TypeScript 检查 | `npm --prefix admin-web run typecheck` | 通过 |
| ESLint | `npm --prefix admin-web run lint` | 通过 |
| 生产构建 | `npm --prefix admin-web run build` | 通过；现有大型依赖分包仍有体积提示 |
| 导航检查 | `npm --prefix admin-web run check:navigation` | 当前工作区637条路由、366个链接，0个悬空链接 |
| 补丁格式 | `git diff --check` | 通过 |

测试覆盖的重点：

- 财务、市场、人事原有非隐藏组件页面均有归属；只单独保留部门工作台和营销方案。
- 152个来源入口保留对应页面权限路径，旧地址进入同一主页面的正确内容，并保留人员、长者、账期和锚点。
- 只获授权查看社保的HR员工不能通过手工修改`view`进入劳动合同、账号设置。
- 业务管理员不能借合并页面获得系统角色维护、安全策略配置权限。
- 原“收费登记”窄授权不因隐藏旧页而被删除，也不扩大为入住账单权限。
- 新部门首页不在自定义授权中时，登录回退到已授权个人工作台或业务主页面。
- 财务报表身份、Excel分支、合同不同流程预设、HR场景参数保留。
- 财务结算简洁列保留应收、押金抵扣、退款、补缴、风险、状态和操作。
- 搜索仍可用“押金”等旧功能名找到对应主页面，但不索引未授权的私有视图。
- 岗位菜单去重，保留至多两层导航；同页切换不会新增应用页签。

测试源码：[页面整合与权限](/Users/yang/smartcare/admin-web/src/workspaces/catalog.test.ts)、[关键列保留](/Users/yang/smartcare/admin-web/src/workspaces/columns.test.ts)、[岗位导航](/Users/yang/smartcare/admin-web/src/layouts/jobNavigation.test.ts)。

## 浏览器检查

使用本地Vite与独立Playwright浏览器上下文；接口被隔离模拟数据拦截，不向生产接口发送请求。主要桌面尺寸为1365×900，窄屏为390×844。

共16个场景，未捕获页面JavaScript异常，未发现根视口横向溢出；表格内部按需横向滚动不视作根视口溢出。

| 场景 | 观察结果 |
|---|---|
| 财务工作台 | 一个页面标题、四项指标、优先办理队列、财务岗位导航 |
| 市场工作台 | 四项指标与销售办理入口，移除多层漏斗/图表堆叠 |
| 人事工作台 | 岗位指标与授权办理队列，无全量功能目录 |
| 旧收费登记链接 | 进入账单与缴费页的在住视图；常用列/完整费用构成可切换 |
| 旧漏斗评估链接 | 进入客户管理页的意向视图；两个默认筛选可展开更多条件 |
| 人事劳动合同 | 进入员工档案与合同主页面，显示合同视图 |
| 市场合同办理 | 合同阶段、筛选和列表正常渲染 |
| 业务管理员经营总览 | 四项指标、待办、风险；经营明细与生日关怀折叠 |
| 原角色管理链接 | 进入组织与权限页面的角色视图 |
| 来源渠道字典 | 进入基础数据页面；去掉重复统计卡与分组列；更多操作仍能打开导入模板等功能 |
| 系统管理员首页 | 配置入口卡与可折叠说明，无部门业务指标 |
| 原Excel报表链接 | 在财务报表同页进入Excel导出分支 |
| 资金账户切押金 | 主地址和主标题保持同一功能，仅切换办理内容 |
| 390px财务首页 | 指标两列、办理事项单列，根视口无横向溢出 |
| 窄授权HR查看社保 | 正常打开；无账号、劳动合同等私有选择项 |
| 窄授权HR伪造账号视图 | 路由返回403 |

[完整浏览器观察记录](/Users/yang/smartcare/output/workspace-simplification-2026-11-15/browser-results.json)。

## 界面预览

以下截图均为隔离模拟数据，不是机构实际经营数据。

| 页面 | 预览 |
|---|---|
| 财务工作台 | [桌面](/Users/yang/smartcare/output/workspace-simplification-2026-11-15/finance-home.png) / [窄屏](/Users/yang/smartcare/output/workspace-simplification-2026-11-15/finance-mobile.png) |
| 账单与缴费 | [预览](/Users/yang/smartcare/output/workspace-simplification-2026-11-15/bills.png) |
| 客户管理 | [预览](/Users/yang/smartcare/output/workspace-simplification-2026-11-15/customers.png) |
| 市场工作台 | [预览](/Users/yang/smartcare/output/workspace-simplification-2026-11-15/marketing-home.png) |
| 人事工作台 | [预览](/Users/yang/smartcare/output/workspace-simplification-2026-11-15/hr-home.png) |
| 员工档案与合同 | [预览](/Users/yang/smartcare/output/workspace-simplification-2026-11-15/hr-profile.png) |
| 业务管理员 | [预览](/Users/yang/smartcare/output/workspace-simplification-2026-11-15/business-admin.png) |
| 系统管理员 | [预览](/Users/yang/smartcare/output/workspace-simplification-2026-11-15/system.png) |
| 组织与权限 | [预览](/Users/yang/smartcare/output/workspace-simplification-2026-11-15/organization.png) |
| 基础数据 | [预览](/Users/yang/smartcare/output/workspace-simplification-2026-11-15/dictionary.png) |

## 业务验收边界

这轮验证覆盖入口、布局、字段呈现、旧链接兼容和前端权限，不代替真实业务账号的收退款、关账、审批、合同签署及外部对账验收。现有后端接口、数据库、账本和审批校验未因页面整理而修改。
