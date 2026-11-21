# 管理端全部岗位：布局与使用流程优化

日期：2026-11-16。承接[上一轮页面整合与布局统一](/Users/yang/smartcare/docs/page-audit-2026-11-15/LAYOUT_STANDARDIZATION.md)。本轮代码位于当前工作区，未发布到生产。

## 本轮结果

| 改进 | 具体实现 |
|---|---|
| 压缩顶部 | 公共顶栏高度与应用页签间距收紧；手机顶栏改成一行；普通和场景页头收紧。合并页面将原操作区通过Teleport放到主标题同行，保留原组件上下文、权限指令、按钮事件和上传组件。 |
| 常用查询 | 通过原字段标签稳定排序：人事突出员工/关键词及部门/月份；财务、医务和护理突出长者/关键词及日期/账期；市场突出关键词和负责人/日期。默认两个常用条件，其余仍可展开，模型、字段、日期格式和处理函数保留。 |
| 主要操作 | 其他模块又有50个来源页面的新增、导入等操作从查询区移到页首；复用原有“更多操作”，保留禁用、确认及权限逻辑。表单内部的提交操作、弹窗和有局部变量的循环内容不进行此类搬移。 |
| 表格重点 | 所有共享DataTable突出姓名、金额、状态、风险和期限；金额使用等宽数字。保留列顺序、临床字段、自定义渲染、分页、排序与选择回调。临床等非合并页面没有新增自动隐藏列规则。 |
| 统计展示 | 统一紧凑统计条、文字对比度和金额单位；其他岗位继续保留各自的业务指标、异常提醒与图表，不改变指标计算。 |
| 办理后引导 | 增加可关闭的结果提示，成功后提供明确下一步。入口按角色、页面授权和细分权限过滤，并在点击时重新核验；离开页面清除，页面卸载/停用后迟到的异步结果不显示到其他页面。 |

## 覆盖口径

扫描管理端`admin-web/src/views`下339个Vue视图文件，包含业务页面、包装页面、复用视图及少量演示/登录等页面，不能将339理解为339个独立菜单或339条真实业务交易。公共外壳覆盖登录后的管理端岗位；本轮工作范围包括管理员、财务、市场、人事、医务、护理、后勤、服务、安全/保安、行政及相关共享页面。家属小程序是另一客户端，本轮未改动。

扫描时直接使用公共PageContainer的文件299个、SearchForm 126个、QueryBar 78个、DataTable 116个、ActionToolbar 130个，含统一统计条的87个。包装页面可以继承子页面的布局，因此直接使用数不能相加。所有文件清单见[源码覆盖记录](/Users/yang/smartcare/docs/page-audit-2026-11-16/layout-coverage.json)。

新增结果引导接入账号资料、员工建档、收费登记、入住办理、护理任务生成、用药设置、服务预约和送餐计划创建八个明确的成功分支。员工原有保存并跳转流程、市场签约后的联动办理面板继续沿用，未用统一提示替换这些业务流程。普通维护页面保留原保存结果；没有为不需要下一步的删除/导出强加引导。

## 关键代码

- [公共外壳](/Users/yang/smartcare/admin-web/src/layouts/BasicLayout.vue)、[顶栏](/Users/yang/smartcare/admin-web/src/layouts/components/GlobalHeader.vue)、[同页工作区](/Users/yang/smartcare/admin-web/src/views/FunctionWorkspace.vue)、[页面容器](/Users/yang/smartcare/admin-web/src/components/PageContainer.vue)。
- [查询字段排序](/Users/yang/smartcare/admin-web/src/components/queryFields.ts)、[查询组件](/Users/yang/smartcare/admin-web/src/components/QueryBar.vue)。
- [表格与关键列样式](/Users/yang/smartcare/admin-web/src/components/DataTable.vue)、[列处理](/Users/yang/smartcare/admin-web/src/workspaces/columns.ts)、[公共样式](/Users/yang/smartcare/admin-web/src/styles/business-layout.less)。
- [结果引导](/Users/yang/smartcare/admin-web/src/components/CompletionGuide.vue)、[成功分支接入](/Users/yang/smartcare/admin-web/src/composables/useCompletionGuide.ts)。

## 验证

| 检查 | 结果 |
|---|---|
| 全量单元测试 | 当前工作区47个测试文件、366项通过 |
| TypeScript检查 | 通过 |
| ESLint | 通过 |
| 生产构建 | 通过；现有依赖仍有大型分包提示 |
| 导航检查 | 637条路由、366个链接，0个悬空链接 |
| 补丁格式 | `git diff --check`通过 |
| 隔离浏览器回归 | 47个场景，未捕获页面脚本异常，所检查页面无根视口横向溢出 |

测试覆盖保留字段和模型、原VNode及按钮事件、禁用状态、权限指令、选择回调、关键列、旧链接、细分页面授权、结果引导入口过滤与撤权、迟到结果不影响其他页面。浏览器在前轮场景基础上补充护理任务、服务预定、用药设置、送餐计划、保安夜巡、中医评估，以及模拟护理任务成功/失败后的结果引导。成功可进入下一步，离开页面提示消失，失败不显示成功引导；手机验证包含首页和财务、人事、市场查询页。

[47个场景结果](/Users/yang/smartcare/output/all-role-layout-2026-11-16/browser-results.json) · [可复跑脚本](/Users/yang/smartcare/output/all-role-layout-2026-11-16/browser-check.cjs)。脚本使用本地5174端口和已安装的Playwright；接口统一拦截为隔离模拟数据，模拟生成不会执行真实业务。

| 截图（模拟数据） | 预览 |
|---|---|
| 标题与操作同行 | [员工档案与合同](/Users/yang/smartcare/output/all-role-layout-2026-11-16/hr-profile.png) |
| 查询重点与窄屏 | [财务异常桌面](/Users/yang/smartcare/output/all-role-layout-2026-11-16/finance-issue.png) / [390px](/Users/yang/smartcare/output/all-role-layout-2026-11-16/finance-query-mobile.png) |
| 护理与医务 | [今日任务](/Users/yang/smartcare/output/all-role-layout-2026-11-16/nursing-today.png) / [用药设置](/Users/yang/smartcare/output/all-role-layout-2026-11-16/medication.png) |
| 后勤与保安 | [送餐计划](/Users/yang/smartcare/output/all-role-layout-2026-11-16/delivery.png) / [夜间巡查](/Users/yang/smartcare/output/all-role-layout-2026-11-16/fire-patrol.png) |
| 办理后的下一步 | [任务生成结果](/Users/yang/smartcare/output/all-role-layout-2026-11-16/completion-guide.png) |

## 业务边界

保留原接口参数、数据校验、确认流程和服务端权限；本轮未修改数据库或后端授权策略。自动化验证覆盖前端接线、展示和权限回归，不等同于所有真实岗位逐项业务验收。未执行生产收退款、关账、签约、医嘱执行或审批交易，未部署生产。
