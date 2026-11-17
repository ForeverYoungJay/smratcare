# 市场部：功能、页面信息与排版盘点

日期：2026-11-15。基于当前工作区源码，未改业务页面。

## 阅读口径

“页面入口”按有页面组件的非隐藏路由统计；同一组件可承载不同场景。隐藏兼容入口见兼容清单。以下字段为源码声明的可显示信息，并非同一账号、同一状态下同时出现；弹层内容与默认列表分开记录。动态列、权限、数据状态仍以运行时为准。

本分册记录 40 个页面入口。

## 页面总表

| 页面 | 地址 | 功能及主要信息 | 排版结构 | 重复关联 |
|---|---|---|---|---|
| 市场部工作台 | /marketing/workbench | 顶部经营指标与部门指标；时间范围/自动刷新/快照；漏斗、风险、快捷操作、床位/合同/回访等销售运营区块。 | 山景页头内指标 → 第二组部门指标 → 日期控制条 → 漏斗/风险/快捷操作三列 → 后续床位与跟进区；窄屏转单列。 | D20 |
| 全部线索 | /marketing/leads/all | 统一线索池，页内咨询/意向/待回访视图；客户与长者联系方式、来源、负责人、阶段、跟进；新增/分配/转意向/转预订/回访/轨迹。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | — |
| 意向客户 | /marketing/leads/intent | 意向客户视图：长者姓名/电话、信息来源、标签、负责人及跟进内容；分配、转预订、回访等。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | D08 |
| 失效客户 | /marketing/leads/invalid | 失效客户视图：客户、联系方式、来源、失效时间及负责人；恢复和轨迹等。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | D09 |
| 黑名单 | /marketing/leads/blacklist | 失效客户模板的 blacklist 场景；黑名单筛选及恢复操作，并非独立客户主档。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | — |
| 渠道不明客户 | /marketing/leads/unknown-source | 咨询模板的 source_unknown 场景；渠道缺失客户及来源补全。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | — |
| 外来就医转线索（来自医护模块） | /marketing/leads/medical-transfer | 咨询模板的 source_medical 场景；来自医护的潜客，复用咨询字段和线索操作。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | — |
| 客户互动记录 | /marketing/interactions/records | 客户互动/回访模板：客户、电话、来源、负责人、回访计划、日期、内容、结果、评分；新增计划与执行、轨迹。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | — |
| 待回访提醒 | /marketing/interactions/due | callback + follow_today；待回访客户列表。与今日跟进默认查询相同，导航选择态和标题不同。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | D07 |
| 今日跟进计划 | /marketing/interactions/today | callback + follow_today；今日跟进列表，与待回访提醒共用默认查询条件。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | D07 |
| 逾期未跟进 | /marketing/interactions/overdue | callback + follow_overdue；逾期回访客户、负责人、计划时间、执行结果与跟进操作。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | — |
| 咨询阶段 | /marketing/funnel/consultation | 咨询客户同模板视图：咨询人/长者、联系方式、咨询日期/类型、媒体渠道、负责人；推进意向。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | — |
| 评估阶段 | /marketing/funnel/evaluation | intent 模板视图：与意向客户使用相同 mode，无额外评估任务 props；默认数据及操作高度重叠。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | D08 |
| 签约阶段 | /marketing/funnel/signing | 合同清单的 pending_sign 状态预设；流程阶段、责任部门、负责人、超时预警与下一步；合同编辑、附件、最终签署和轨迹。 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | D12 |
| 入住阶段 | /marketing/funnel/admission | 合同清单的 pending_bed_select 预设；合同、长者、责任部门、选床/入住办理与流程轨迹。 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 流失阶段 | /marketing/funnel/lost | invalid 模板视图：与失效客户相同 mode，无额外流失原因视图参数。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | D09 |
| 预约参观 | /marketing/reservation/visits | 潜客参观预约时间、联系人/电话、人数、接待人、状态、结果、取消原因、下次跟进；新增、签到及结果登记。 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 床位预定记录 | /marketing/reservation/records | 预订客户模板：姓名/身份证、定金额度、房号、付款时间、退款、预约渠道和状态。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | — |
| 锁床管理 | /marketing/reservation/lock | 预订模板 lock_bed 场景：锁床客户及预订/付款/房号；操作沿用预订模板。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | — |
| 预定到期提醒 | /marketing/reservation/expiring | 预订模板 expiring_lock 场景：即将到期锁床/预订；沿用预订字段和动作。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | — |
| 床态全景（销售视角） | /marketing/reservation/panorama | RoomPanorama 仅包装 BedMap；房态/床位图及相关筛选、占用和床位操作，具体能力受权限控制，不是另写的销售图。 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 待签约合同 | /marketing/contracts/pending | pending_sign 合同预设，显式关闭默认流程阶段预设；合同号、长者、电话、负责人、优惠、阶段、责任部门、状态、超时与下一步。 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | D12 |
| 已签合同 | /marketing/contracts/signed | signed 合同状态预设；合同基本信息、流程轨迹、附件及后续变更联动。 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 续签提醒 | /marketing/contracts/renewal | 合同到期与续签：房号、长者/电话、负责人、有效期、倒计时、短信次数；提醒规则与短信任务。 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 合同变更 | /marketing/contracts/change | 合同号、长者、签约房号、到期日、合同与审批状态、变更备注；编辑及审批流转。 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 合同附件 | /marketing/contracts/attachments | 合同号、长者、电话、流程阶段；附件类型、文件名、大小、链接、上传时间与预览。 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | D17 |
| 入住后回访 | /marketing/callback/checkin | callback 模板 + checkin：入住后回访计划、执行、结果与评分。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | — |
| 试住回访 | /marketing/callback/trial | callback 模板 + trial：试住回访计划、执行、结果与评分。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | — |
| 退住回访 | /marketing/callback/discharge | callback 模板 + discharge：退住回访计划、执行、结果与评分。 | 页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。 | — |
| 回访质量评分 | /marketing/callback/score | 姓名/日期/低分筛选；平均分、低分数、待复访、已完成；电话、来源、回访日期、评分、风险和备注。 | 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 转化率统计 | /marketing/reports/conversion | 日期/渠道/跟进人；咨询、意向、预订、签约数量；漏斗转化、阶段与占比。 | 数据表格 → 图表；表格列/操作由脚本或子组件定义。 | — |
| 咨询统计 | /marketing/reports/consultation | 天数或日期/渠道；新增线索趋势、日期、新增量、咨询及意向阶段量。 | 数据表格 → 图表；表格列/操作由脚本或子组件定义。 | — |
| 不明渠道统计 | /marketing/reports/unknown-channel | 缺失来源、缺失回访日期、非标准来源数量；渠道、线索、签约、转化率。 | 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 跟进统计 | /marketing/reports/followup | 日期/渠道；总线索、咨询、意向、逾期回访；阶段数量占比及缺失渠道/回访日期等质量信号。 | 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 回访统计 | /marketing/reports/callback | 日期/渠道；今日待回访、逾期、已完成；姓名、电话、来源、回访日期、状态与备注。 | 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 渠道评估 | /marketing/reports/channel | 日期；渠道线索数、预订数、签约数，渠道贡献图表和清单。 | 数据表格 → 图表；表格列/操作由脚本或子组件定义。 | — |
| 销售人员业绩 | /marketing/reports/sales-performance | 负责人/流程阶段；签约数、可识别签约金额、转化率、跟进及时率和排名。 | 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 渠道贡献排名 | /marketing/reports/channel-rank | 日期；渠道排名、线索数、签约数、签约贡献率、渠道转化率。 | 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 经营快照 | /marketing/reports/snapshots | 快照类型/数量；快照日期、统计起止、生成时间/人员；详情含快照键和指标JSON。 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 营销方案与审批 | /marketing/plan | 营销方案/话术/政策等内容类型；季度、标题、目标、来源标签、预算、责任/联动部门、目标与实际、转化、审批、阅读确认和改进意见。 | 数据表格 → 页签 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |

## 逐页字段、操作和实现依据

每页给出筛选、列表列、弹层字段和动作。脚本列定义按变量名列出：其中可能包含导出列、子表列或非当前场景列，不能把它们相加作为首屏列数。静态文字用于补充指标/分区信息；动态表达式保留原名，便于回查。

### 1. 市场部工作台 `/marketing/workbench`

顶部经营指标与部门指标；时间范围/自动刷新/快照；漏斗、风险、快捷操作、床位/合同/回访等销售运营区块。

- 导航归属：营销管理 > 市场部工作台。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：山景页头内指标 → 第二组部门指标 → 日期控制条 → 漏斗/风险/快捷操作三列 → 后续床位与跟进区；窄屏转单列。
- 关联重复项：D20。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/Workbench.vue](/Users/yang/smartcare/admin-web/src/views/marketing/Workbench.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 112px、height: 1、height: 1.55、height: 1.08、height: 1.7、grid-template-columns: minmax(0, 1.7fr) minmax(320px, 1.15fr) minmax(300px, 1fr)、height: 1.5、min-height: 338px、grid-template-columns: minmax(220px, 0.95fr) minmax(0, 1.35fr)、min-height: 250px、height: 250px、grid-template-columns: 36px minmax(0, 1fr) auto、height: 36px、min-height: 102px、height: 44px、grid-template-columns: repeat(6, minmax(0, 1fr))、min-height: 116px、height: 48px、grid-template-columns: minmax(180px, 1.05fr) minmax(0, 0.95fr)、height: 190px、grid-template-columns: 10px 1fr auto、height: 10px、grid-template-columns: minmax(0, 1.8fr) minmax(290px, 0.9fr)、min-height: 620px、min-height: 38px、grid-template-columns: minmax(220px, 1.8fr) minmax(110px, 0.8fr) minmax(110px, 0.8fr) minmax(90px, 0.6fr) minmax(90px, 0.6fr)、min-height: 30px、min-height: 0、grid-template-columns: 1fr、grid-template-columns: repeat(3, minmax(0, 1fr))、grid-template-columns: repeat(2, minmax(0, 1fr))。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：今日新增咨询、今日咨询线索、今日待跟进、今天需要联系、待评估合同、签约前评估未完成、待签约客户、继续推进合同、转化率统计、跟进统计、渠道评估、咨询统计、回访统计、经营快照、机会池、风险项、本月成交、线索新增、初步沟通、合同待评估、预定锁床、签约入住、待回访超期、预定即将到期、锁床未签约、新增线索、记录跟进、安排回访、预定床位、锁定床位、发起合同、合同变更、签后回访、线索总量、预定中、锁床中、签约中、本月入住、可用、入住中、今日待回访客户、逾期未跟进客户、待签合同、锁床即将到期、待跟进（今天）、回访超期、3天内需回访、签约前待评估、待审批方案、话术库维护、季度政策沉淀、咨询、预定、锁床。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：统计范围、自动刷新、快照 条、更新于、今日销售重点、销售运营工作台、围绕漏斗、风险、床位、合同和回访，把当下最该处理的销售动作集中到一屏。、本月、线索漏斗、查看转化率、风险、风险预警、查看明细、快捷操作、查看方案审批、床位与预定、床位与预定概况、进入床态看板、任务台、运营任务台、把今天最需要推进的营销动作收在一起，优先级从高到低处理。、任务内容、业务阶段、提醒口径、优先级、处理动作、提醒、跟进任务提醒、查看全部、渠道、渠道表现、%、营销活动、营销方案进度、快照、经营快照、更多。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：立即刷新、查看快照。

**承载内容的子组件：[admin-web/src/components/workbench/DepartmentHomeOverview.vue](/Users/yang/smartcare/admin-web/src/components/workbench/DepartmentHomeOverview.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：min-height: 148px、height: 100%、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 124px、height: 28px、height: 1、height: 1.4、grid-template-columns: repeat(5, 1fr)、grid-template-columns: auto 1fr auto、min-height: 62px、height: 22px、min-height: 68px、grid-template-columns: repeat(2, 1fr)、grid-template-columns: 1fr、grid-template-columns: 40px 1fr auto。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：常用业务。
- 页签/分区：今天的重点一屏掌握、按顺序处理更清晰、按业务分类直达。
- 指标和说明静态文字：工作台、›、更新于、项可用功能、核心流程、最近使用、全部功能、仅展示当前账号可进入的页面。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新数据。

### 2. 全部线索 `/marketing/leads/all`

统一线索池，页内咨询/意向/待回访视图；客户与长者联系方式、来源、负责人、阶段、跟进；新增/分配/转意向/转预订/回访/轨迹。

- 导航归属：营销管理 > 客户线索管理 > 全部线索。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "pipeline", "title": "线索跟进池", "subTitle": "咨询、意向、待回访统一管理，支持转预订与放弃"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 3. 意向客户 `/marketing/leads/intent`

意向客户视图：长者姓名/电话、信息来源、标签、负责人及跟进内容；分配、转预订、回访等。

- 导航归属：营销管理 > 客户线索管理 > 意向客户。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "intent", "title": "意向客户", "subTitle": "重点意向客户持续跟进"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：D08。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 4. 失效客户 `/marketing/leads/invalid`

失效客户视图：客户、联系方式、来源、失效时间及负责人；恢复和轨迹等。

- 导航归属：营销管理 > 客户线索管理 > 失效客户。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "invalid", "title": "失效用户", "subTitle": "流失与失效线索归档管理"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：D09。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 5. 黑名单 `/marketing/leads/blacklist`

失效客户模板的 blacklist 场景；黑名单筛选及恢复操作，并非独立客户主档。

- 导航归属：营销管理 > 客户线索管理 > 黑名单。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "invalid", "title": "黑名单", "subTitle": "黑名单客户集中处置与恢复", "scenario": "blacklist"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 6. 渠道不明客户 `/marketing/leads/unknown-source`

咨询模板的 source_unknown 场景；渠道缺失客户及来源补全。

- 导航归属：营销管理 > 客户线索管理 > 渠道不明客户。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "consultation", "title": "渠道不明客户", "subTitle": "缺失渠道的线索预警与补全", "scenario": "source_unknown"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 7. 外来就医转线索（来自医护模块） `/marketing/leads/medical-transfer`

咨询模板的 source_medical 场景；来自医护的潜客，复用咨询字段和线索操作。

- 导航归属：营销管理 > 客户线索管理 > 外来就医转线索（来自医护模块）。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "consultation", "title": "外来就医转线索", "subTitle": "来自医护模块的潜客自动流入", "scenario": "source_medical"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 8. 客户互动记录 `/marketing/interactions/records`

客户互动/回访模板：客户、电话、来源、负责人、回访计划、日期、内容、结果、评分；新增计划与执行、轨迹。

- 导航归属：营销管理 > 客户互动中心 > 客户互动记录。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "callback", "title": "客户互动记录", "subTitle": "销售跟进、回访计划与执行轨迹统一收口"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 9. 待回访提醒 `/marketing/interactions/due`

callback + follow_today；待回访客户列表。与今日跟进默认查询相同，导航选择态和标题不同。

- 导航归属：营销管理 > 客户互动中心 > 待回访提醒。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "callback", "title": "待回访提醒", "subTitle": "待执行回访任务清单", "scenario": "follow_today"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：D07。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 10. 今日跟进计划 `/marketing/interactions/today`

callback + follow_today；今日跟进列表，与待回访提醒共用默认查询条件。

- 导航归属：营销管理 > 客户互动中心 > 今日跟进计划。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "callback", "title": "今日跟进计划", "subTitle": "今日需完成的营销跟进", "scenario": "follow_today"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：D07。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 11. 逾期未跟进 `/marketing/interactions/overdue`

callback + follow_overdue；逾期回访客户、负责人、计划时间、执行结果与跟进操作。

- 导航归属：营销管理 > 客户互动中心 > 逾期未跟进。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "callback", "title": "逾期未跟进", "subTitle": "逾期未跟进客户风险池", "scenario": "follow_overdue"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 12. 咨询阶段 `/marketing/funnel/consultation`

咨询客户同模板视图：咨询人/长者、联系方式、咨询日期/类型、媒体渠道、负责人；推进意向。

- 导航归属：营销管理 > 线索阶段管理（销售漏斗） > 咨询阶段。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "consultation", "title": "咨询阶段", "subTitle": "咨询线索池：新增咨询、分配跟进与来源分析"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 13. 评估阶段 `/marketing/funnel/evaluation`

intent 模板视图：与意向客户使用相同 mode，无额外评估任务 props；默认数据及操作高度重叠。

- 导航归属：营销管理 > 线索阶段管理（销售漏斗） > 评估阶段。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "intent", "title": "评估阶段", "subTitle": "高意向客户评估推进与评估任务闭环"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：D08。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 14. 签约阶段 `/marketing/funnel/signing`

合同清单的 pending_sign 状态预设；流程阶段、责任部门、负责人、超时预警与下一步；合同编辑、附件、最终签署和轨迹。

- 导航归属：营销管理 > 线索阶段管理（销售漏斗） > 签约阶段。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"statusPreset": "pending_sign", "title": "签约阶段", "subTitle": "待签署合同清单与签约推进"}。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：D12。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/ContractSigning.vue](/Users/yang/smartcare/admin-web/src/views/marketing/ContractSigning.vue)**

- 筛选/主页面表单：合同编号、姓名、联系电话、负责人、优惠政策、流程阶段、仅看我负责、责任部门、只看超时、超时排序。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：合同编号、姓名、流程阶段、当前责任部门、联系电话、负责人、优惠政策、合同状态、超时预警、下一步。
- 脚本列 `attachmentColumns`：预览、类型、文件名、文件链接、文件类型、文件大小、上传时间、操作。
- 表格列绑定：columns、attachmentColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 72vh、height: 1.2、height: 1.5、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 88px、min-height: 76px、height: 1.7、min-height: 172px、height: 1.6、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：合同签约、营销部、评估部、合同编号、姓名、流程阶段、当前责任部门、联系电话、负责人、优惠政策、合同状态、超时预警、下一步、预览、类型、文件名、文件链接、文件类型、文件大小、上传时间、操作、配偶、子女、孙辈、兄弟姐妹、监护人、亲属、朋友、其他、长者管理、账单账务、后勤协同、医护健康、合同尚未审批通过。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：流程阶段看板、更新时间：、-、合同编号、合同状态、流程阶段、负责人、签约日期、合同有效期止、优惠政策、未配置、姓名、联系电话、身份证号、性别 / 年龄、/、生日、护理等级、风险预担、家庭地址、手机号、身份证、基础疾病、暂无疾病信息、性别。
- 弹层标题：contractModalTitle、合同附件（病历/医保/户口等）、attachmentPreviewTitle、最终签署、签约联动完成、合同流程轨迹。
- 弹层表单字段：合同编号、合同状态、流程阶段、签约日期、合同有效期止、负责人、优惠政策（最多 2 条）、年龄、生日、身份证识别、护理等级、风险预担、基础疾病信息、关系、主联系人、备注、签署备注。
- 可读操作文案：刷新看板、搜 索、清 空、新增合同、查看、轨迹、编辑、入住评估、附件、入住办理、最终签署、删除、更多、快捷新增疾病、新增家属、上传附件、预览。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 15. 入住阶段 `/marketing/funnel/admission`

合同清单的 pending_bed_select 预设；合同、长者、责任部门、选床/入住办理与流程轨迹。

- 导航归属：营销管理 > 线索阶段管理（销售漏斗） > 入住阶段。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"statusPreset": "pending_bed_select", "title": "入住阶段", "subTitle": "待办理入住合同与选床办理联动"}。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/ContractSigning.vue](/Users/yang/smartcare/admin-web/src/views/marketing/ContractSigning.vue)**

- 筛选/主页面表单：合同编号、姓名、联系电话、负责人、优惠政策、流程阶段、仅看我负责、责任部门、只看超时、超时排序。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：合同编号、姓名、流程阶段、当前责任部门、联系电话、负责人、优惠政策、合同状态、超时预警、下一步。
- 脚本列 `attachmentColumns`：预览、类型、文件名、文件链接、文件类型、文件大小、上传时间、操作。
- 表格列绑定：columns、attachmentColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 72vh、height: 1.2、height: 1.5、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 88px、min-height: 76px、height: 1.7、min-height: 172px、height: 1.6、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：合同签约、营销部、评估部、合同编号、姓名、流程阶段、当前责任部门、联系电话、负责人、优惠政策、合同状态、超时预警、下一步、预览、类型、文件名、文件链接、文件类型、文件大小、上传时间、操作、配偶、子女、孙辈、兄弟姐妹、监护人、亲属、朋友、其他、长者管理、账单账务、后勤协同、医护健康、合同尚未审批通过。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：流程阶段看板、更新时间：、-、合同编号、合同状态、流程阶段、负责人、签约日期、合同有效期止、优惠政策、未配置、姓名、联系电话、身份证号、性别 / 年龄、/、生日、护理等级、风险预担、家庭地址、手机号、身份证、基础疾病、暂无疾病信息、性别。
- 弹层标题：contractModalTitle、合同附件（病历/医保/户口等）、attachmentPreviewTitle、最终签署、签约联动完成、合同流程轨迹。
- 弹层表单字段：合同编号、合同状态、流程阶段、签约日期、合同有效期止、负责人、优惠政策（最多 2 条）、年龄、生日、身份证识别、护理等级、风险预担、基础疾病信息、关系、主联系人、备注、签署备注。
- 可读操作文案：刷新看板、搜 索、清 空、新增合同、查看、轨迹、编辑、入住评估、附件、入住办理、最终签署、删除、更多、快捷新增疾病、新增家属、上传附件、预览。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 16. 流失阶段 `/marketing/funnel/lost`

invalid 模板视图：与失效客户相同 mode，无额外流失原因视图参数。

- 导航归属：营销管理 > 线索阶段管理（销售漏斗） > 流失阶段。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "invalid", "title": "流失阶段", "subTitle": "流失客户归因分析与二次激活"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：D09。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 17. 预约参观 `/marketing/reservation/visits`

潜客参观预约时间、联系人/电话、人数、接待人、状态、结果、取消原因、下次跟进；新增、签到及结果登记。

- 导航归属：营销管理 > 预定与床态联动 > 预约参观。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/VisitAppointments.vue](/Users/yang/smartcare/admin-web/src/views/marketing/VisitAppointments.vue)**

- 筛选/主页面表单：客户/电话、预约状态、预约日期。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：客户/联系人、预约时间、人数、接待负责人、状态、参观结果/取消原因、下次跟进、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：待到访、已到访、参观完成、已取消、未到访、客户/联系人、预约时间、人数、接待负责人、状态、参观结果/取消原因、下次跟进、操作。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：form.id ? '编辑参观预约' : '新增参观预约'、填写参观结果、取消参观预约。
- 弹层表单字段：客户线索、预约参观时间、来访联系人、联系电话、来访人数、接待负责人、预约备注、参观结果、下次跟进日期、补充备注、取消原因。
- 可读操作文案：查询、重置、新增预约、编辑、登记到访、填写参观结果、取消预约、到访、填结果、取消。

### 18. 床位预定记录 `/marketing/reservation/records`

预订客户模板：姓名/身份证、定金额度、房号、付款时间、退款、预约渠道和状态。

- 导航归属：营销管理 > 预定与床态联动 > 床位预定记录。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "reservation", "title": "预订管理", "subTitle": "已预订客户与签约前准备"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 19. 锁床管理 `/marketing/reservation/lock`

预订模板 lock_bed 场景：锁床客户及预订/付款/房号；操作沿用预订模板。

- 导航归属：营销管理 > 预定与床态联动 > 锁床管理。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "reservation", "title": "锁床管理", "subTitle": "锁床客户与锁床状态跟踪", "scenario": "lock_bed"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 20. 预定到期提醒 `/marketing/reservation/expiring`

预订模板 expiring_lock 场景：即将到期锁床/预订；沿用预订字段和动作。

- 导航归属：营销管理 > 预定与床态联动 > 预定到期提醒。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "reservation", "title": "预定到期提醒", "subTitle": "锁床临期与预定超时预警", "scenario": "expiring_lock"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 21. 床态全景（销售视角） `/marketing/reservation/panorama`

RoomPanorama 仅包装 BedMap；房态/床位图及相关筛选、占用和床位操作，具体能力受权限控制，不是另写的销售图。

- 导航归属：营销管理 > 预定与床态联动 > 床态全景（销售视角）。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/RoomPanorama.vue](/Users/yang/smartcare/admin-web/src/views/marketing/RoomPanorama.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/bed/Map.vue](/Users/yang/smartcare/admin-web/src/views/bed/Map.vue)**

- 筛选/主页面表单：床位号、房间号、老人、状态、区域、房型、床型。
- 模板直接声明的表格列：头像、姓名、生日、家庭住址、备注。
- CSS布局声明（含响应式分支及局部图表）：height:220px"/>`)   win.print()   win.close()、grid-template-columns: 1fr auto、grid-template-columns: 110px 1fr、min-height: 110px、grid-template-columns: repeat(auto-fill, minmax(180px, 1fr))、height: 24px、height: 1、height: 1.6、min-height: 220px、height: 30px、height: 1.7、grid-template-columns: 1fr、min-height: 72px。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：可点击楼栋头部与楼层条快速聚焦，不同楼层结构不会再混排、间。
- 弹层标题：床位与老人详情、`房间详情 · ${roomCurrent?.roomNo ／／ '-'}`。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：搜索、清空、床位管理、3D 床态全景、清除楼层筛选、关闭、历史补录入住、合同入住办理、打印床位二维码。

### 22. 待签约合同 `/marketing/contracts/pending`

pending_sign 合同预设，显式关闭默认流程阶段预设；合同号、长者、电话、负责人、优惠、阶段、责任部门、状态、超时与下一步。

- 导航归属：营销管理 > 合同管理 > 待签约合同。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"statusPreset": "pending_sign", "title": "待签约合同", "subTitle": "待签署合同统一处理：查看、入住办理、最终签署", "disableDefaultFlowStagePreset": true}。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：D12。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/ContractSigning.vue](/Users/yang/smartcare/admin-web/src/views/marketing/ContractSigning.vue)**

- 筛选/主页面表单：合同编号、姓名、联系电话、负责人、优惠政策、流程阶段、仅看我负责、责任部门、只看超时、超时排序。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：合同编号、姓名、流程阶段、当前责任部门、联系电话、负责人、优惠政策、合同状态、超时预警、下一步。
- 脚本列 `attachmentColumns`：预览、类型、文件名、文件链接、文件类型、文件大小、上传时间、操作。
- 表格列绑定：columns、attachmentColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 72vh、height: 1.2、height: 1.5、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 88px、min-height: 76px、height: 1.7、min-height: 172px、height: 1.6、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：合同签约、营销部、评估部、合同编号、姓名、流程阶段、当前责任部门、联系电话、负责人、优惠政策、合同状态、超时预警、下一步、预览、类型、文件名、文件链接、文件类型、文件大小、上传时间、操作、配偶、子女、孙辈、兄弟姐妹、监护人、亲属、朋友、其他、长者管理、账单账务、后勤协同、医护健康、合同尚未审批通过。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：流程阶段看板、更新时间：、-、合同编号、合同状态、流程阶段、负责人、签约日期、合同有效期止、优惠政策、未配置、姓名、联系电话、身份证号、性别 / 年龄、/、生日、护理等级、风险预担、家庭地址、手机号、身份证、基础疾病、暂无疾病信息、性别。
- 弹层标题：contractModalTitle、合同附件（病历/医保/户口等）、attachmentPreviewTitle、最终签署、签约联动完成、合同流程轨迹。
- 弹层表单字段：合同编号、合同状态、流程阶段、签约日期、合同有效期止、负责人、优惠政策（最多 2 条）、年龄、生日、身份证识别、护理等级、风险预担、基础疾病信息、关系、主联系人、备注、签署备注。
- 可读操作文案：刷新看板、搜 索、清 空、新增合同、查看、轨迹、编辑、入住评估、附件、入住办理、最终签署、删除、更多、快捷新增疾病、新增家属、上传附件、预览。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 23. 已签合同 `/marketing/contracts/signed`

signed 合同状态预设；合同基本信息、流程轨迹、附件及后续变更联动。

- 导航归属：营销管理 > 合同管理 > 已签合同。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"statusPreset": "signed", "title": "已签合同", "subTitle": "已签署合同集中查询、补录与变更协同"}。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/ContractSigning.vue](/Users/yang/smartcare/admin-web/src/views/marketing/ContractSigning.vue)**

- 筛选/主页面表单：合同编号、姓名、联系电话、负责人、优惠政策、流程阶段、仅看我负责、责任部门、只看超时、超时排序。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：合同编号、姓名、流程阶段、当前责任部门、联系电话、负责人、优惠政策、合同状态、超时预警、下一步。
- 脚本列 `attachmentColumns`：预览、类型、文件名、文件链接、文件类型、文件大小、上传时间、操作。
- 表格列绑定：columns、attachmentColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 72vh、height: 1.2、height: 1.5、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 88px、min-height: 76px、height: 1.7、min-height: 172px、height: 1.6、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：合同签约、营销部、评估部、合同编号、姓名、流程阶段、当前责任部门、联系电话、负责人、优惠政策、合同状态、超时预警、下一步、预览、类型、文件名、文件链接、文件类型、文件大小、上传时间、操作、配偶、子女、孙辈、兄弟姐妹、监护人、亲属、朋友、其他、长者管理、账单账务、后勤协同、医护健康、合同尚未审批通过。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：流程阶段看板、更新时间：、-、合同编号、合同状态、流程阶段、负责人、签约日期、合同有效期止、优惠政策、未配置、姓名、联系电话、身份证号、性别 / 年龄、/、生日、护理等级、风险预担、家庭地址、手机号、身份证、基础疾病、暂无疾病信息、性别。
- 弹层标题：contractModalTitle、合同附件（病历/医保/户口等）、attachmentPreviewTitle、最终签署、签约联动完成、合同流程轨迹。
- 弹层表单字段：合同编号、合同状态、流程阶段、签约日期、合同有效期止、负责人、优惠政策（最多 2 条）、年龄、生日、身份证识别、护理等级、风险预担、基础疾病信息、关系、主联系人、备注、签署备注。
- 可读操作文案：刷新看板、搜 索、清 空、新增合同、查看、轨迹、编辑、入住评估、附件、入住办理、最终签署、删除、更多、快捷新增疾病、新增家属、上传附件、预览。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 24. 续签提醒 `/marketing/contracts/renewal`

合同到期与续签：房号、长者/电话、负责人、有效期、倒计时、短信次数；提醒规则与短信任务。

- 导航归属：营销管理 > 合同管理 > 续签提醒。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/ContractManagement.vue](/Users/yang/smartcare/admin-web/src/views/marketing/ContractManagement.vue)**

- 筛选/主页面表单：合同编号、姓名、联系电话、负责人。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：合同编号、签约房号、姓名、性别、年龄、联系电话、负责人、签约时间、合同有效期、域状态、发送次数、倒计时、所属机构。
- 脚本列 `smsTaskColumns`：任务ID、手机号、模板、状态、计划发送时间、发送时间、结果、操作。
- 表格列绑定：columns、smsTaskColumns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：合同编号、签约房号、姓名、性别、年龄、联系电话、负责人、签约时间、合同有效期、域状态、发送次数、倒计时、所属机构、任务ID、手机号、模板、状态、计划发送时间、发送时间、结果、操作。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：-。
- 弹层标题：短信任务记录、合同到期提醒规则。
- 弹层表单字段：启用到期提醒、提前提醒天数、计划发送时间、短信内容。
- 可读操作文案：搜 索、清 空、导出当前页、提醒规则设置、短信、批量发短信、删除、批量删除、短信任务记录、立即发送。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 25. 合同变更 `/marketing/contracts/change`

合同号、长者、签约房号、到期日、合同与审批状态、变更备注；编辑及审批流转。

- 导航归属：营销管理 > 合同管理 > 合同变更。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/ContractChange.vue](/Users/yang/smartcare/admin-web/src/views/marketing/ContractChange.vue)**

- 筛选/主页面表单：合同编号、长者姓名、审批状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：合同编号、长者姓名、签约房号、到期日期、审批状态、合同状态、变更备注。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：合同编号、长者姓名、签约房号、到期日期、审批状态、合同状态、变更备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：编辑合同变更。
- 弹层表单字段：合同编号、长者姓名、签约房号、到期日期、变更说明。
- 可读操作文案：查询、重置、编辑变更、发起变更、提交审批、批量提交审批、审批通过、批量审批通过、审批驳回、批量审批驳回、取消、保存变更。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 26. 合同附件 `/marketing/contracts/attachments`

合同号、长者、电话、流程阶段；附件类型、文件名、大小、链接、上传时间与预览。

- 导航归属：营销管理 > 合同管理 > 合同附件。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：D17。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/ContractAttachments.vue](/Users/yang/smartcare/admin-web/src/views/marketing/ContractAttachments.vue)**

- 筛选/主页面表单：合同编号、长者姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `contractColumns`：合同编号、长者姓名、联系电话、流程阶段、附件。
- 脚本列 `attachmentColumns`：预览、类型、文件名、文件链接、大小、上传时间。
- 表格列绑定：contractColumns、attachmentColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 72vh。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：合同编号、长者姓名、联系电话、流程阶段、附件、预览、类型、文件名、文件链接、大小、上传时间。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：附件清单、previewTitle。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、重置、查看附件、导出所选附件索引、导出合同索引、预览。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 27. 入住后回访 `/marketing/callback/checkin`

callback 模板 + checkin：入住后回访计划、执行、结果与评分。

- 导航归属：营销管理 > 签后回访管理 > 入住后回访。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "callback", "callbackType": "checkin", "title": "入住后回访", "subTitle": "入住后客户回访计划、执行与风险跟踪"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 28. 试住回访 `/marketing/callback/trial`

callback 模板 + trial：试住回访计划、执行、结果与评分。

- 导航归属：营销管理 > 签后回访管理 > 试住回访。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "callback", "callbackType": "trial", "title": "试住回访", "subTitle": "试住客户回访安排与转化促进跟踪"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 29. 退住回访 `/marketing/callback/discharge`

callback 模板 + discharge：退住回访计划、执行、结果与评分。

- 导航归属：营销管理 > 签后回访管理 > 退住回访。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"mode": "callback", "callbackType": "discharge", "title": "退住回访", "subTitle": "退住客户回访与原因沉淀闭环"}。
- 排版：页头 → 工作区说明/数量/勾选状态 → 指标与警告 → 多条件筛选（部分视图有切换）→ 工具条 → 横向表格与分页 → 编辑/执行/轨迹弹层；条件按 mode 改变。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue)**

- 筛选/主页面表单：线索视图、咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、咨询类型、媒体渠道、信息来源、客户标签、负责人、姓名、客户姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.08、height: 1.7、min-height: 36px、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 108px、height: 1、min-height: 42px、height: 8px、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：回访、咨询人姓名、联系电话、老人姓名、老人联系电话、性别、年龄、咨询日期、咨询类型、媒体渠道、信息来源、接待人、负责人、家庭地址、所属机构、操作、回访状态、回访内容、回访结果、推荐渠道、客户标签、姓名、身份证号、定金额度、预订房号、收款时间、是否退款、预约渠道、状态、序号、失效时间、客户姓名、跟进负责人、计划执行时间、计划标题、新增、编辑、客户轨迹、分配负责人、设为意向、放弃客户、删除、转预订、设置客户标签、添加回访计划、退款切换、恢复客户、执行回访、设今日回访、顺延3天、标记已回访、当前咨询池、总线索数、缺失来源、非标准来源、当前意向客户、待回访总量、逾期回访、缺少下次回访、当前预订客户、已签约、待签约预订、退款预订、失效客户、咨询客户、意向客户、预订客户、今日到期、轨迹、分配、转意向、放弃、回访计划、恢复、补计划、确认仍要保存这条客户线索？、确认转为意向客户？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：营销工作区、工作区、当前列表 条、已勾选 条、列表、筛选、批量动作和单条推进都在这张表里完成，减少来回切页。、（由其他员工负责，请联系主管确认）。
- 弹层标题：modalTitle、新增回访计划、执行回访计划、分配线索负责人、客户轨迹。
- 弹层表单字段：咨询人姓名、联系电话、老人姓名、老人联系电话、咨询日期、客户来源、咨询内容/备注、性别、年龄、咨询类型、媒体渠道、接待人、家庭地址、负责人、客户标签、预订房号、预订金额、收款时间、预约渠道、计划执行时间、回访状态、计划标题、回访类型、回访内容、跟进人、执行备注、满意度评分、回访结果、下次回访日期、分配说明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 30. 回访质量评分 `/marketing/callback/score`

姓名/日期/低分筛选；平均分、低分数、待复访、已完成；电话、来源、回访日期、评分、风险和备注。

- 导航归属：营销管理 > 签后回访管理 > 回访质量评分。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/CallbackQuality.vue](/Users/yang/smartcare/admin-web/src/views/marketing/CallbackQuality.vue)**

- 筛选/主页面表单：姓名、开始日期、结束日期、仅低分。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：姓名、电话、来源、回访日期、评分、风险等级、备注。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：姓名、电话、来源、回访日期、评分、风险等级、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、重置、入住后回访、试住回访、退住回访、逾期未跟进。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 31. 转化率统计 `/marketing/reports/conversion`

日期/渠道/跟进人；咨询、意向、预订、签约数量；漏斗转化、阶段与占比。

- 导航归属：营销管理 > 销售报表中心 > 转化率统计。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["MARKETING_MINISTER", "DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 图表；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/report/Conversion.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/Conversion.vue)**

- 筛选/主页面表单：开始日期、结束日期、渠道、跟进人。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：阶段、数量、相对总线索占比。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 320px" />         </a-card>       </a-col>       <a-col :xs="24" :xl="10">         <a-card class="card-elevated conversion-insight-card" :bordered="false">           <template #title>阶段观察</template>           <div class="insight-item">             <span>咨询转意向</span>             <strong>{{ report.intentRate.toFixed(1)、height: 1.2、height: 1.8、grid-template-columns: repeat(2, minmax(148px, 1fr))、height: 1、min-height: 134px、height: 100%、min-height: 100%、height: 1.6、grid-template-columns: 1fr 1fr、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：咨询总量、意向总量、预订总量、签约总量、阶段、数量、相对总线索占比、咨询、意向、预订、签约。
- 页签/分区：把咨询、意向、预订到签约的每一段损耗都看清楚。
- 指标和说明静态文字：转化洞察、统一查看阶段数量、转化率和当前筛选口径，方便从渠道、时间和负责人三个维度定位问题。、总线索、签约转化率、%、咨询转意向、判断咨询接待与资格筛选质量、意向转预订、观察评估推进、锁床动作与回访执行、预订转签约、反映合同推进、床位确认与签约闭环效率。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、重置、导出CSV、查看明细。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 32. 咨询统计 `/marketing/reports/consultation`

天数或日期/渠道；新增线索趋势、日期、新增量、咨询及意向阶段量。

- 导航归属：营销管理 > 销售报表中心 > 咨询统计。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["MARKETING_MINISTER", "DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 图表；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/report/Consultation.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/Consultation.vue)**

- 筛选/主页面表单：天数、开始日期、结束日期、渠道。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：日期、新增线索、咨询阶段、意向阶段。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 280px" />     </a-card>     <a-card class="card-elevated" :bordered="false">       <a-table :columns="columns" :data-source="rows" row-key="date" :pagination="false">         <template #bodyCell="{ column, record。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：日期、新增线索、咨询阶段、意向阶段。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、导出CSV。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 33. 不明渠道统计 `/marketing/reports/unknown-channel`

缺失来源、缺失回访日期、非标准来源数量；渠道、线索、签约、转化率。

- 导航归属：营销管理 > 销售报表中心 > 不明渠道统计。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["MARKETING_MINISTER", "DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/report/UnknownChannel.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/UnknownChannel.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：渠道名称、线索数、签约数、转化率。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：渠道名称、线索数、签约数、转化率。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、去渠道不明客户池、去渠道评估。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 34. 跟进统计 `/marketing/reports/followup`

日期/渠道；总线索、咨询、意向、逾期回访；阶段数量占比及缺失渠道/回访日期等质量信号。

- 导航归属：营销管理 > 销售报表中心 > 跟进统计。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["MARKETING_MINISTER", "DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/report/Followup.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/Followup.vue)**

- 筛选/主页面表单：开始日期、结束日期、渠道。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：阶段、数量、占比。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：总线索、咨询中、意向中、逾期回访、阶段、数量、占比。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、导出CSV、标准化渠道数据。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 35. 回访统计 `/marketing/reports/callback`

日期/渠道；今日待回访、逾期、已完成；姓名、电话、来源、回访日期、状态与备注。

- 导航归属：营销管理 > 销售报表中心 > 回访统计。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["MARKETING_MINISTER", "DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/report/Callback.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/Callback.vue)**

- 筛选/主页面表单：开始日期、结束日期、渠道。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：姓名、电话、来源、回访日期、状态、备注。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：姓名、电话、来源、回访日期、状态、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、导出CSV。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 36. 渠道评估 `/marketing/reports/channel`

日期；渠道线索数、预订数、签约数，渠道贡献图表和清单。

- 导航归属：营销管理 > 销售报表中心 > 渠道评估。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["MARKETING_MINISTER", "DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 图表；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/report/Channel.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/Channel.vue)**

- 筛选/主页面表单：开始日期、结束日期。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：来源渠道、线索数、预订数、签约数。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 280px" />     </a-card>     <a-card class="card-elevated" :bordered="false">       <a-table :columns="columns" :data-source="rows" row-key="source" :pagination="false">         <template #bodyCell="{ column, record。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：来源渠道、线索数、预订数、签约数。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、导出CSV。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 37. 销售人员业绩 `/marketing/reports/sales-performance`

负责人/流程阶段；签约数、可识别签约金额、转化率、跟进及时率和排名。

- 导航归属：营销管理 > 销售报表中心 > 销售人员业绩。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["MARKETING_MINISTER", "DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/report/SalesPerformance.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/SalesPerformance.vue)**

- 筛选/主页面表单：负责人、流程阶段。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：排名、负责人、签约数、签约金额、转化率、跟进及时率。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：排名、负责人、签约数、签约金额、转化率、跟进及时率。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、重置。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 38. 渠道贡献排名 `/marketing/reports/channel-rank`

日期；渠道排名、线索数、签约数、签约贡献率、渠道转化率。

- 导航归属：营销管理 > 销售报表中心 > 渠道贡献排名。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["MARKETING_MINISTER", "DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/report/ChannelRank.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/ChannelRank.vue)**

- 筛选/主页面表单：开始日期、结束日期。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：排名、渠道、线索数、签约数、签约贡献率、渠道转化率。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：排名、渠道、线索数、签约数、签约贡献率、渠道转化率。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 39. 经营快照 `/marketing/reports/snapshots`

快照类型/数量；快照日期、统计起止、生成时间/人员；详情含快照键和指标JSON。

- 导航归属：营销管理 > 销售报表中心 > 经营快照。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["MARKETING_MINISTER", "DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/report/Snapshots.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/Snapshots.vue)**

- 筛选/主页面表单：快照类型、最近条数。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：快照类型、快照日期、窗口开始、窗口结束、生成时间、生成人、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.6。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：快照类型、快照日期、窗口开始、窗口结束、生成时间、生成人、操作。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：快照明细。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、重置、查看明细。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 40. 营销方案与审批 `/marketing/plan`

营销方案/话术/政策等内容类型；季度、标题、目标、来源标签、预算、责任/联动部门、目标与实际、转化、审批、阅读确认和改进意见。

- 导航归属：营销管理 > 营销方案与审批。
- 路由角色约束：[["MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["MARKETING_MINISTER", "DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 页签 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/marketingRoutes.ts](/Users/yang/smartcare/admin-web/src/router/marketingRoutes.ts)。

**页面：[admin-web/src/views/marketing/MarketingPlan.vue](/Users/yang/smartcare/admin-web/src/views/marketing/MarketingPlan.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `policyColumns`：季度、标题、运营目标、来源标签、预算、实际线索、实际签约、签约转化、责任部门、状态、审批状态、审批备注、阅读。
- 脚本列 `receiptColumns`：姓名、已读时间、确认动作、改进详情。
- 表格列绑定：policyColumns、receiptColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：min-height: 252px、min-height: 56px、height: 1.6、height: 1.7。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：季度、标题、运营目标、来源标签、预算、实际线索、实际签约、签约转化、责任部门、状态、审批状态、审批备注、阅读、姓名、已读时间、确认动作、改进详情、审批通过、确认驳回当前方案吗？。
- 页签/分区：营销话术库、季度运营政策。
- 指标和说明静态文字：优先级：、生效：、预算：、来源标签：、实际线索：、实际签约： · 转化、单线索成本：、单签约成本：、阅读： /、同意： · 改进：、已读 / · 同意 · 改进。
- 弹层标题：editingId ? '编辑营销方案' : '新增营销方案'、方案详情、confirmForm.action === 'AGREE' ? '确认同意方案' : '提交改进建议'。
- 弹层表单字段：模块类型、状态、标题、摘要、季度、责任部门、生效日期、活动编码、来源标签、预算金额、联动负责部门、运营目标、目标线索数、目标预定数、目标签约数、优先级、内容详情、确认动作、改进详情。
- 可读操作文案：新增方案、查看详情、编辑、流程操作、删除、详情、提交审批、审批通过、驳回、停用、刷新、我已同意、提交改进。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

## 相关重复项

### D07 待回访提醒 / 今日跟进计划

- 判断：默认查询相同。
- 依据：两者 callback + follow_today；查询构造均为 followupDueOnly=true、followupDateTo=今天。标题和导航选择态不同。
- 后续整理方向：统一跟进列表；若业务要区分今天与全部到期，应先定义不同日期条件。

### D08 意向客户 / 评估阶段

- 判断：默认场景高度重叠。
- 依据：同一 LeadScenarioPage，mode=intent；仅标题/副标题不同，未传额外评估参数。
- 后续整理方向：评估作为客户推进状态或真实评估待办，不重复铺一套意向清单。

### D09 失效客户 / 流失阶段

- 判断：默认场景高度重叠。
- 依据：同一模板 mode=invalid，仅标题/副标题不同。
- 后续整理方向：统一失效客户，流失原因作为字段或分组。

### D12 签约阶段 / 待签约合同

- 判断：相同对象但有真实预设差异。
- 依据：都 pending_sign，但后者 disableDefaultFlowStagePreset=true；不能直接认定完全重复。
- 后续整理方向：合同中心用阶段筛选保留差异。

### D17 合同附件 / 长者合同与票据

- 判断：同资料不同职责。
- 依据：市场负责合同附件办理；长者侧为资料与合同信息查询。
- 后续整理方向：保留读写职责差异，统一资料来源与名称。

### D20 多个首页/驾驶舱

- 判断：导航和指标层层重复。
- 依据：总览、个人、部门、行政、驾驶舱重复承担入口和摘要；口径及受众并非完全一样。
- 后续整理方向：明确每类首页唯一职责，摘要点击下钻而非再次进入导航。
