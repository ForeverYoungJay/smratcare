# 财务部：功能、页面信息与排版盘点

日期：2026-11-15。基于当前工作区源码，未改业务页面。

## 阅读口径

“页面入口”按有页面组件的非隐藏路由统计；同一组件可承载不同场景。隐藏兼容入口见兼容清单。以下字段为源码声明的可显示信息，并非同一账号、同一状态下同时出现；弹层内容与默认列表分开记录。动态列、权限、数据状态仍以运行时为准。

本分册记录 52 个页面入口。

## 页面总表

| 页面 | 地址 | 功能及主要信息 | 排版结构 | 重复关联 |
|---|---|---|---|---|
| 财务部工作台 | /finance/workbench | 今日收款、待审批、欠费与对账指标；搜索；四组业务通道；建议工作顺序；营收结构；风险信号；待办与高频配置入口。 | 山景页头 → 部门指标 → 左右指挥区 → 四业务卡 → 双列流程/建议 → 营收/风险 → 待办/配置，多层纵向堆叠。 | D20 |
| 收银台 | /finance/payments/cashier-desk | 业务日期、支付方式；四步收银流程；当日收款流水；票据待办；退款修正；交班检查单。 | 数据表格；信息按模板纵向排列。 | — |
| 收费登记 | /finance/payments/register | 当前月在住账单：长者、护理等级、应收总额和费用构成、已付、欠费、支付方式、状态；登记/修改收款与历史。 | 筛选卡片 → 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | D01 |
| 收款流水 | /finance/payments/records | 日期与支付方式、账单筛选；账单ID、金额、方式、时间、操作人、外部流水号、备注。 | 筛选卡片 → 数据表格；信息按模板纵向排列。 | — |
| 发票与收据 | /finance/fees/payment-and-invoice | 日期、长者、支付方式、发票关联状态；流水笔数/金额、未关联笔数/金额；收据号、风险、打印。 | 数据表格；信息按模板纵向排列。 | D10 |
| 退款与冲正 | /finance/payments/refund-reversal | 月份；作废账单与收款修正两类记录；原金额/修正金额、原因、审批单、审批状态及审批备注。 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 日结与交班 | /finance/payments/shift-close | 业务日期；收款笔数、缺票据、修正和对账异常；当天流水；四项交班检查及打印。 | 数据表格；信息按模板纵向排列。 | — |
| 入住账单 | /finance/bills/in-admission | 新入住首期账单查询、生成与缴费；费用构成、已付/欠费、状态、收款历史。默认锁定 ADMISSION 场景。 | 筛选卡片 → 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 在住周期账单 | /finance/bills/in-resident | 当前月在住周期账单、费用构成、已付/欠费、收款登记与历史。默认锁定 RESIDENT 场景。 | 筛选卡片 → 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | D01 |
| 账单查询 | /finance/bills/detail-query | 按月份、长者、收款方式、账单场景查账；护理/床位/保险费用、总额、已付、欠费和收款记录。 | 筛选卡片 → 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 长者账户列表 | /finance/accounts/list | 账户数量/总余额/押金与预收汇总；长者余额、押金、预收、积分、信用额度、预警阈值、状态；调整和设置。 | 筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 账户流水 | /finance/accounts/ledger | 流水数、收入/支出合计、未识别充值方式；长者、方向、金额、结余、资金类型、来源、充值方式与时间。 | 筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。 | D18 |
| 预存充值 | /finance/prepaid-recharge | 长者充值表单（金额、方式、时间、备注）；今日充值、低余额人数、欠费人数/金额；押金及账户流水入口。 | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | D18 |
| 消费券管理 | /finance/consumer-voucher | 长者/状态/关键词筛选；可用券数和余额；券号、名称、面额、剩余额度、门槛、有效期、发放及核销流水。 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 电费管理 | /finance/electricity-fee | 账期、楼栋房间、缴费状态；读数、用量、电价、电费、分摊；电表登记及电价生效设置。 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 押金管理 | /finance/deposit-management | 长者/状态；在押余额、未缴清人数、扣退汇总；应缴、实缴、差额、扣退与台账；押金标准设置。 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 余额预警规则 | /finance/accounts/warning-rules | 余额阈值；欠费、待充值、合同临期风险摘要；通知对象、联动入口及规则配置快照。 | 数据表格；信息按模板纵向排列。 | — |
| 入住费用审核 | /finance/admission-fee-audit | 长者、入住记录、费用总额、押金、审核状态、审核备注；新建、通过与驳回。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 消费登记 | /finance/flows/consumption | 日期、类别、风险筛选；流水数/总额/高风险/来源缺失；长者、金额、来源、备注；新增消费。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 医护费用流水 | /finance/flows/medical | 医护费用消费流水；日期/长者；今日及本月金额、待处理/异常；来源类型和来源ID。category=MEDICINE。 | 数据表格；信息按模板纵向排列。 | — |
| 医护费用异常 | /finance/flows/medical-errors | 医护费用异常场景的汇总和消费表格；moduleKey=MEDICAL_ERRORS；与普通医护流水不是仅换标题。 | 数据表格；信息按模板纵向排列。 | — |
| 餐饮扣费流水 | /finance/flows/dining | 餐饮消费扣费流水，category=DINING；长者、日期、金额、来源与备注。 | 数据表格；信息按模板纵向排列。 | — |
| 物资/后勤收费流水 | /finance/flows/logistics | 物资/后勤收费场景，category=OTHER；长者、日期、金额、来源与备注。 | 数据表格；信息按模板纵向排列。 | — |
| 费用调整单 | /finance/flows/adjustments | 费用调整场景汇总及列表，moduleKey=ADJUSTMENTS；联动审批、退住审核。 | 数据表格；信息按模板纵向排列。 | — |
| 公共费用计算 | /finance/allocation/public-cost | 月度分摊项目、总金额、人数、人均金额、长者、状态；新建分摊、预览、抄表校验与回滚。 | 筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | D02 |
| 分摊入账任务 | /finance/allocation/tasks | 与“公共费用计算”挂载相同 MonthlyAllocation，无路由场景区分；展示同一组分摊记录与操作。 | 筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | D02 |
| 欠费与提醒中心 | /finance/bills/follow-up | “自动提醒/催缴跟进”双页签；提醒记录、跟进对象、欠费金额、跟进责任与处理情况，分别由子组件承载。 | 页签 → 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 自动扣费与催缴 | /finance/bills/auto-deduct | 自动扣费成功/失败/待处理与风险金额；长者、账单月、应扣金额、余额、失败原因和处理建议。 | 数据表格；信息按模板纵向排列。 | D03 |
| 自动扣费异常 | /finance/bills/auto-deduct-errors | 同“自动扣费与催缴”组件与默认查询，仍展示扣费异常列表。 | 数据表格；信息按模板纵向排列。 | D03 |
| 对账异常处理 | /finance/reconcile/exception | 日期、异常类型、风险；异常数/金额；时间、长者、异常描述、建议与联动处理。 | 数据表格；信息按模板纵向排列。 | D11 |
| 财务一致性巡检 | /finance/reconcile/ledger-health | 账单、收款、流水一致性检查；问题总量、明细/已付不一致；期望金额、实际金额及异常描述。 | 数据表格；信息按模板纵向排列。 | D11 |
| 异常修复中心 | /finance/reconcile/issue-center | 跨来源财务异常队列；风险、状态、责任人、时限、处理动作、复核结论、备注及处理留痕。 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | D11 |
| 退住审核 | /finance/discharge/review | 长者、退住申请、费用项、应收应付、风险等级、审批状态与备注；新建及审核。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 退住结算 | /finance/discharge/settlement | 退住应收、押金抵扣、退款、补缴；押金处理、对账、床位释放、前台/护理签字、财务退款和状态。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D04 |
| 结算单打印 | /finance/discharge/print-sign | 同退住结算组件；包含相同结算台账和签字/退款/打印操作，没有独立打印视图预设。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D04 |
| 结算状态回写 | /finance/discharge/status-sync | 结算完成后的状态一致性；结算单、长者状态、档案床位/实际占床、异常与单条/批量回写。 | 数据表格；信息按模板纵向排列。 | — |
| 对账中心 | /finance/reconcile/center | 日收款/退款/净收款；未核销、重复冲正、发票未关联；异常分级明细、对账历史及生成对账。 | 数据表格；信息按模板纵向排列。 | — |
| 发票对账 | /finance/reconcile/invoice | 收款笔数、发票已关联/未关联、缺票据金额；长者、金额、支付方式、收据号与关联状态。 | 数据表格；信息按模板纵向排列。 | D10 |
| 月结进度 | /finance/reconcile/month-close | 月份；账单、收款、未结账单、缺票据、修正、一致性问题；检查链路、关账/反锁与跨期申请。 | 弹窗/抽屉；信息按模板纵向排列。 | — |
| 总收支 | /finance/reports/overall | 总收支与现金流：净营收、开账、实收、退款、商城销售、欠费；趋势、费用科目、房间与欠费排行。 | 页头/报表页签 → 区间筛选 → 指标 → 双列图表与排行 → 明细；Excel 路由为专用导出视图。 | D13 |
| 营收结构 | /finance/reports/revenue-structure | 营收结构Top、净营收与退款趋势、重点费用科目、房间收支、欠费长者排行。 | 页头/报表页签 → 区间筛选 → 指标 → 双列图表与排行 → 明细；Excel 路由为专用导出视图。 | D13 |
| 楼层房间收支 | /finance/reports/floor-room-profit | 房间收支Top、欠费风险分布；费用科目、房间及欠费长者表格。 | 页头/报表页签 → 区间筛选 → 指标 → 双列图表与排行 → 明细；Excel 路由为专用导出视图。 | D13 |
| 入住与消费 | /finance/reports/occupancy-consumption | 房间营收Top、消费类目结构；房间、费用科目和欠费长者明细。 | 页头/报表页签 → 区间筛选 → 指标 → 双列图表与排行 → 明细；Excel 路由为专用导出视图。 | D13 |
| Excel 报表 | /finance/reports/excel | 同一报表组件内的Excel导出视图：代养费登记表、押金台账、电费月报、催缴清单；不是空白导出按钮页。 | 页头/报表页签 → 区间筛选 → 指标 → 双列图表与排行 → 明细；Excel 路由为专用导出视图。 | D13 |
| 机构月运营 | /finance/reports/monthly-ops | 净营收与实收趋势、营收结构；费用科目、房间、欠费长者；机构月运营视图。 | 页头/报表页签 → 区间筛选 → 指标 → 双列图表与排行 → 明细；Excel 路由为专用导出视图。 | D13 |
| 主数据配置中心 | /finance/config/master-data | 科目/计费规则/支付渠道/待审批配置数量；配置导航、近期配置、月度快照、变更影响模拟。 | 数据表格；信息按模板纵向排列。 | — |
| 费用科目 | /finance/allocation/subjects | 科目键、科目名称、默认单价或比例、生效月份、启停与维护。 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 计费规则 | /finance/bills/rules | 生效月、关键词；配置键、配置值、状态、备注；计费规则维护及打印。 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 分摊规则 | /finance/allocation/rules | 分摊规则类型、规则键/值、生效月份、状态、备注。 | 数据表格；信息按模板纵向排列。 | — |
| 支付渠道 | /finance/config/payment-channels | 渠道编码与名称、收款户名、收款账号、启停和维护。 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 审批与权限 | /finance/config/approval-flow | 减免/退款/冲正审批角色与级数；模板、审批数量与金额、健康检查和配置快照。 | 数据表格；信息按模板纵向排列。 | — |
| 配置变更记录 | /finance/config/change-log | 日期、动作、风险筛选；时间、操作人、实体、变更详情；变更量/可回滚/高风险与回滚。 | 数据表格；信息按模板纵向排列。 | — |

## 逐页字段、操作和实现依据

每页给出筛选、列表列、弹层字段和动作。脚本列定义按变量名列出：其中可能包含导出列、子表列或非当前场景列，不能把它们相加作为首屏列数。静态文字用于补充指标/分区信息；动态表达式保留原名，便于回查。

### 1. 财务部工作台 `/finance/workbench`

今日收款、待审批、欠费与对账指标；搜索；四组业务通道；建议工作顺序；营收结构；风险信号；待办与高频配置入口。

- 导航归属：财务运营中心 > 财务部工作台。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：山景页头 → 部门指标 → 左右指挥区 → 四业务卡 → 双列流程/建议 → 营收/风险 → 待办/配置，多层纵向堆叠。
- 关联重复项：D20。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/FinanceWorkbench.vue](/Users/yang/smartcare/admin-web/src/views/finance/FinanceWorkbench.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.9fr)、height: 1.12、height: 1.7、grid-template-columns: repeat(2, minmax(0, 1fr))、height: 1.2、height: 1.1、grid-template-columns: repeat(3, minmax(0, 1fr))、height: 1.6、grid-template-columns: minmax(0, 1.1fr) minmax(0, 0.9fr)、min-height: 46px、height: 280px、grid-template-columns: repeat(auto-fit, minmax(150px, 1fr))、height: 1.5、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：今日收款、当前业务日期实收、待审批事项、退款、折扣与结算、欠费提醒、需催缴跟进的长者、对账待办、账务差异待处理、异常修复、欠费推进、月结收口、收款闭环、票据补齐、异常回捞、今日入账、欠费规模、待审批、巡检问题、待审批减免单、待审批退款、待审核退住结算、异常工单待处理、催缴提醒到期、已锁账月份、收费、票据与交班、账单、欠费与余额、退款、修正与异常、对账、报表与月结、先处理今日收款、再看欠费与余额、当天异常当天清、最后做对账和月结、今日收款与票据、先看今天的收款、票据缺口和交班动作，保证一线财务闭环。、收款方式、开票金额、票据未关联、进入收银台、收费登记、票据处理、日结交班、账单与欠费、把在住账单、欠费催缴、低余额预警和续约风险放在一起看。、低余额账户、即将到期合同、自动扣费失败、在住账单、欠费跟进、余额预警、高风险催缴、自动扣费异常、退款、冲正与异常、把退款审批、作废修正和一致性问题放到一条处理链里。、重复/冲正待完成、异常工单、退款/冲正、异常修复中心、一致性巡检、退款复核、账单修正、对账与月结、把日对账、发票核对、锁账状态和月结推进集中到一个入口。、发票未关联、异常处理、月结提醒、对账中心、月结进度、发票对账、账单已收未核销、月结阻塞。
- 页签/分区：按业务阶段进入功能组、今天建议这样处理、本月营收结构、财务一致性与风险信号、今日待办与主管关注项、高频动作与配置入口。
- 指标和说明静态文字：业务日期、当前视角、待审批、净营收、Top 房间、%、巡检问题、高优先级洞察、待审批事项、费用科目、计费规则、支付渠道、待审批配置。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：财务搜索、刷新数据、进入收银台、查看在住账单、处理异常、查看月结、查看报表、巡检明细、配置中心。

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

### 2. 收银台 `/finance/payments/cashier-desk`

业务日期、支付方式；四步收银流程；当日收款流水；票据待办；退款修正；交班检查单。

- 导航归属：财务运营中心 > 收费与收款 > 收银台。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/CashierDesk.vue](/Users/yang/smartcare/admin-web/src/views/finance/CashierDesk.vue)**

- 筛选/主页面表单：业务日期、收款方式。
- 模板直接声明的表格列：账单ID、金额、方式、操作人、收款时间、外部流水号、备注、操作。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.2fr) minmax(320px, 1fr)、height: 1.12、height: 1.7、grid-template-columns: repeat(4, minmax(0, 1fr))、height: 42px、height: 1.6、min-height: 132px、height: 1.1、grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.82fr)、height: 1.2、grid-template-columns: repeat(auto-fit, minmax(160px, 1fr))、grid-template-columns: 1fr、grid-template-columns: repeat(2, minmax(0, 1fr))。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：今天、昨天、月初、登记收款、补齐票据、复核修正、打印交班、票据关联、退款/冲正复核、收款流水检查、收银交班单、账单ID、金额、方式、收款时间、操作人、备注。
- 页签/分区：一线财务只需要盯住今天这四步。、当日收款流水、票据待办、退款与修正、交班检查单。
- 指标和说明静态文字：先收款，再补票据，再复核修正，最后打印交班单。页面不再让你来回找模块。、当日收款、笔流水、待补票据、待补齐、待复核修正、需确认、交班状态、清除筛选、全部方式、·。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、重置、全部收款方式、收费登记、票据处理、退款/冲正、打印交班单、全部流水、查看账单、导出、全部修正。

### 3. 收费登记 `/finance/payments/register`

当前月在住账单：长者、护理等级、应收总额和费用构成、已付、欠费、支付方式、状态；登记/修改收款与历史。

- 导航归属：财务运营中心 > 收费与收款 > 收费登记。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：D01。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/ResidentBillPayment.vue](/Users/yang/smartcare/admin-web/src/views/finance/ResidentBillPayment.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/finance/BillCenter.vue](/Users/yang/smartcare/admin-web/src/views/finance/BillCenter.vue)**

- 筛选/主页面表单：月份、老人姓名、收款方式、账单场景。
- 模板直接声明的表格列：账单月份、老人、护理级别、总额、护理费、床位费、保险费、已付、欠费、收款方式、状态、操作、金额、方式、收款时间、备注。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(4, minmax(0, 1fr))、grid-template-columns: minmax(0, 1.35fr) minmax(0, 1fr)、height: 1.5、height: 54px、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：月账单中心、确认标记为无效账单？、收款历史、金额、方式、收款时间、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：当前账单数、按当前筛选条件实时统计、欠费笔数、需优先安排催缴与跟进、欠费金额、帮助财务快速识别风险账单、已收金额、当前页收款完成率 %、账单生成与收款工作台、先锁定账期和长者，再完成生成、收款登记、异常识别与历史追溯。、全院风险摘要、当前页工作提示、条账单， 条已作废、支持直接查看详情、登记收款、修正最近收款与查看消费明细。、账单列表、保留原有账单业务逻辑，优化筛选、风险识别和收款操作的工作顺序。、剩余额度 元、应收余额、抵扣合计、-、（长护险 · 折扣 · 消费券 ）、实收现金、本次抵账合计、登记后剩余欠费。
- 弹层标题：收款历史、生成账单、activePaymentId ? '修改收款' : '登记收款'。
- 弹层表单字段：账单月份、折扣减免、减免原因、消费券抵扣、实收现金、方式、收款时间、备注。
- 可读操作文案：长者总览、长者账户、导出CSV、生成账单、查看、登记收款、改最近收款、收款历史、消费明细、无效账单、导出历史、打印历史、修改、关闭、按建议 填入。

### 4. 收款流水 `/finance/payments/records`

日期与支付方式、账单筛选；账单ID、金额、方式、时间、操作人、外部流水号、备注。

- 导航归属：财务运营中心 > 收费与收款 > 收款流水。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/PaymentRecords.vue](/Users/yang/smartcare/admin-web/src/views/finance/PaymentRecords.vue)**

- 筛选/主页面表单：开始日期、结束日期、收款方式、账单ID。
- 模板直接声明的表格列：账单ID、金额、方式、收款时间、操作人、外部流水号、备注、操作。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(3, minmax(0, 1fr))、grid-template-columns: 1fr。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：收款笔数、当前页实际收款记录条数、当前页金额、便于财务核对当页流水、记录总数、支持跨账期持续追溯、收款流水工作台、按时间、方式与账单编号核对实际收款记录，支持快速回到对应账单继续排查。、收款记录列表、支持按账单回看收款动作、操作人和外部流水号，便于财务核账与追踪。。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出CSV、查看账单。

### 5. 发票与收据 `/finance/fees/payment-and-invoice`

日期、长者、支付方式、发票关联状态；流水笔数/金额、未关联笔数/金额；收据号、风险、打印。

- 导航归属：财务运营中心 > 收费与收款 > 发票与收据。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：D10。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/InvoiceReceiptManagement.vue](/Users/yang/smartcare/admin-web/src/views/finance/InvoiceReceiptManagement.vue)**

- 筛选/主页面表单：日期、支付方式、关联状态、长者、打印备注。
- 模板直接声明的表格列：收款时间、长者、金额、支付方式、收据号、发票关联、风险级别、备注、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：发票/收据管理、收款时间、长者、金额、支付方式、收据号、发票关联、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、仅看未关联、仅看已关联、导出CSV、打印当前结果、重置、查看账单、去关联。

### 6. 退款与冲正 `/finance/payments/refund-reversal`

月份；作废账单与收款修正两类记录；原金额/修正金额、原因、审批单、审批状态及审批备注。

- 导航归属：财务运营中心 > 收费与收款 > 退款与冲正。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/PaymentRefundReversal.vue](/Users/yang/smartcare/admin-web/src/views/finance/PaymentRefundReversal.vue)**

- 筛选/主页面表单：月份。
- 模板直接声明的表格列：发生时间、账单ID、长者、账单金额、说明、备注、审批单、审批、操作、金额、修正内容。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：提交退款/冲正审批。
- 弹层表单字段：审批原因、审批备注。
- 可读操作文案：查询、导出、重置、查看账单。

### 7. 日结与交班 `/finance/payments/shift-close`

业务日期；收款笔数、缺票据、修正和对账异常；当天流水；四项交班检查及打印。

- 导航归属：财务运营中心 > 收费与收款 > 日结与交班。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/ShiftClose.vue](/Users/yang/smartcare/admin-web/src/views/finance/ShiftClose.vue)**

- 筛选/主页面表单：业务日期。
- 模板直接声明的表格列：账单ID、金额、方式、收款时间、操作人、备注。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: 1.1fr 1fr、height: 1.18、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：收款流水已核对、票据关联完整、退款/冲正已复核、对账异常已清零。
- 页签/分区：交班前只看今天该收口的四件事。。
- 指标和说明静态文字：收款流水、票据关联、退款修正和对账异常缺一不可，避免第二天继续挂账。。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新交班摘要、重新生成对账、收银台。

### 8. 入住账单 `/finance/bills/in-admission`

新入住首期账单查询、生成与缴费；费用构成、已付/欠费、状态、收款历史。默认锁定 ADMISSION 场景。

- 导航归属：财务运营中心 > 账单与账户 > 入住账单。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/AdmissionBillPayment.vue](/Users/yang/smartcare/admin-web/src/views/finance/AdmissionBillPayment.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/finance/BillCenter.vue](/Users/yang/smartcare/admin-web/src/views/finance/BillCenter.vue)**

- 筛选/主页面表单：月份、老人姓名、收款方式、账单场景。
- 模板直接声明的表格列：账单月份、老人、护理级别、总额、护理费、床位费、保险费、已付、欠费、收款方式、状态、操作、金额、方式、收款时间、备注。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(4, minmax(0, 1fr))、grid-template-columns: minmax(0, 1.35fr) minmax(0, 1fr)、height: 1.5、height: 54px、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：月账单中心、确认标记为无效账单？、收款历史、金额、方式、收款时间、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：当前账单数、按当前筛选条件实时统计、欠费笔数、需优先安排催缴与跟进、欠费金额、帮助财务快速识别风险账单、已收金额、当前页收款完成率 %、账单生成与收款工作台、先锁定账期和长者，再完成生成、收款登记、异常识别与历史追溯。、全院风险摘要、当前页工作提示、条账单， 条已作废、支持直接查看详情、登记收款、修正最近收款与查看消费明细。、账单列表、保留原有账单业务逻辑，优化筛选、风险识别和收款操作的工作顺序。、剩余额度 元、应收余额、抵扣合计、-、（长护险 · 折扣 · 消费券 ）、实收现金、本次抵账合计、登记后剩余欠费。
- 弹层标题：收款历史、生成账单、activePaymentId ? '修改收款' : '登记收款'。
- 弹层表单字段：账单月份、折扣减免、减免原因、消费券抵扣、实收现金、方式、收款时间、备注。
- 可读操作文案：长者总览、长者账户、导出CSV、生成账单、查看、登记收款、改最近收款、收款历史、消费明细、无效账单、导出历史、打印历史、修改、关闭、按建议 填入。

### 9. 在住周期账单 `/finance/bills/in-resident`

当前月在住周期账单、费用构成、已付/欠费、收款登记与历史。默认锁定 RESIDENT 场景。

- 导航归属：财务运营中心 > 账单与账户 > 在住周期账单。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：D01。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/ResidentBillPayment.vue](/Users/yang/smartcare/admin-web/src/views/finance/ResidentBillPayment.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/finance/BillCenter.vue](/Users/yang/smartcare/admin-web/src/views/finance/BillCenter.vue)**

- 筛选/主页面表单：月份、老人姓名、收款方式、账单场景。
- 模板直接声明的表格列：账单月份、老人、护理级别、总额、护理费、床位费、保险费、已付、欠费、收款方式、状态、操作、金额、方式、收款时间、备注。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(4, minmax(0, 1fr))、grid-template-columns: minmax(0, 1.35fr) minmax(0, 1fr)、height: 1.5、height: 54px、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：月账单中心、确认标记为无效账单？、收款历史、金额、方式、收款时间、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：当前账单数、按当前筛选条件实时统计、欠费笔数、需优先安排催缴与跟进、欠费金额、帮助财务快速识别风险账单、已收金额、当前页收款完成率 %、账单生成与收款工作台、先锁定账期和长者，再完成生成、收款登记、异常识别与历史追溯。、全院风险摘要、当前页工作提示、条账单， 条已作废、支持直接查看详情、登记收款、修正最近收款与查看消费明细。、账单列表、保留原有账单业务逻辑，优化筛选、风险识别和收款操作的工作顺序。、剩余额度 元、应收余额、抵扣合计、-、（长护险 · 折扣 · 消费券 ）、实收现金、本次抵账合计、登记后剩余欠费。
- 弹层标题：收款历史、生成账单、activePaymentId ? '修改收款' : '登记收款'。
- 弹层表单字段：账单月份、折扣减免、减免原因、消费券抵扣、实收现金、方式、收款时间、备注。
- 可读操作文案：长者总览、长者账户、导出CSV、生成账单、查看、登记收款、改最近收款、收款历史、消费明细、无效账单、导出历史、打印历史、修改、关闭、按建议 填入。

### 10. 账单查询 `/finance/bills/detail-query`

按月份、长者、收款方式、账单场景查账；护理/床位/保险费用、总额、已付、欠费和收款记录。

- 导航归属：财务运营中心 > 账单与账户 > 账单查询。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"title": "账单详情查询", "subTitle": "按老人、月份检索账单后进入详情页", "defaultCurrentMonth": true}。
- 排版：筛选卡片 → 数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/BillCenter.vue](/Users/yang/smartcare/admin-web/src/views/finance/BillCenter.vue)**

- 筛选/主页面表单：月份、老人姓名、收款方式、账单场景。
- 模板直接声明的表格列：账单月份、老人、护理级别、总额、护理费、床位费、保险费、已付、欠费、收款方式、状态、操作、金额、方式、收款时间、备注。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(4, minmax(0, 1fr))、grid-template-columns: minmax(0, 1.35fr) minmax(0, 1fr)、height: 1.5、height: 54px、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：月账单中心、确认标记为无效账单？、收款历史、金额、方式、收款时间、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：当前账单数、按当前筛选条件实时统计、欠费笔数、需优先安排催缴与跟进、欠费金额、帮助财务快速识别风险账单、已收金额、当前页收款完成率 %、账单生成与收款工作台、先锁定账期和长者，再完成生成、收款登记、异常识别与历史追溯。、全院风险摘要、当前页工作提示、条账单， 条已作废、支持直接查看详情、登记收款、修正最近收款与查看消费明细。、账单列表、保留原有账单业务逻辑，优化筛选、风险识别和收款操作的工作顺序。、剩余额度 元、应收余额、抵扣合计、-、（长护险 · 折扣 · 消费券 ）、实收现金、本次抵账合计、登记后剩余欠费。
- 弹层标题：收款历史、生成账单、activePaymentId ? '修改收款' : '登记收款'。
- 弹层表单字段：账单月份、折扣减免、减免原因、消费券抵扣、实收现金、方式、收款时间、备注。
- 可读操作文案：长者总览、长者账户、导出CSV、生成账单、查看、登记收款、改最近收款、收款历史、消费明细、无效账单、导出历史、打印历史、修改、关闭、按建议 填入。

### 11. 长者账户列表 `/finance/accounts/list`

账户数量/总余额/押金与预收汇总；长者余额、押金、预收、积分、信用额度、预警阈值、状态；调整和设置。

- 导航归属：财务运营中心 > 账单与账户 > 长者账户列表。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/Account.vue](/Users/yang/smartcare/admin-web/src/views/finance/Account.vue)**

- 筛选/主页面表单：老人姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：老人、余额、押金、预收、积分余额、信用额度、预警阈值、状态、更新时间、操作。
- 脚本列 `warningColumns`：老人、余额、预警阈值、备注。
- 表格列绑定：columns、warningColumns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：老人、余额、押金、预收、积分余额、信用额度、预警阈值、状态、更新时间、操作、备注、充值、扣费、自动抵扣、启用、停用。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：余额调整、余额预警、账户设置。
- 弹层表单字段：老人姓名、方向、资金类型、金额、备注、信用额度、预警阈值、状态。
- 可读操作文案：余额预警、余额调整、查看流水、账户设置。

### 12. 账户流水 `/finance/accounts/ledger`

流水数、收入/支出合计、未识别充值方式；长者、方向、金额、结余、资金类型、来源、充值方式与时间。

- 导航归属：财务运营中心 > 账单与账户 > 账户流水。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D18。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/AccountLog.vue](/Users/yang/smartcare/admin-web/src/views/finance/AccountLog.vue)**

- 筛选/主页面表单：老人姓名。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：老人、方向、金额、余额、资金类型、押金结余、预收结余、来源、充值方式、充值时间、时间、备注。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：老人、方向、金额、余额、资金类型、押金结余、预收结余、来源、充值方式、充值时间、时间、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：打印当前流水。

### 13. 预存充值 `/finance/prepaid-recharge`

长者充值表单（金额、方式、时间、备注）；今日充值、低余额人数、欠费人数/金额；押金及账户流水入口。

- 导航归属：财务运营中心 > 账单与账户 > 预存充值。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：D18。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/PrepaidRecharge.vue](/Users/yang/smartcare/admin-web/src/views/finance/PrepaidRecharge.vue)**

- 筛选/主页面表单：老人、充值金额(元)、充值方式、充值时间、备注。
- 模板直接声明的表格列：未在该层模板中声明。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：确认大额充值？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：低余额账户、欠费账单、确认充值、查看账户流水。

### 14. 消费券管理 `/finance/consumer-voucher`

长者/状态/关键词筛选；可用券数和余额；券号、名称、面额、剩余额度、门槛、有效期、发放及核销流水。

- 导航归属：财务运营中心 > 账单与账户 > 消费券管理。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/ConsumerVoucher.vue](/Users/yang/smartcare/admin-web/src/views/finance/ConsumerVoucher.vue)**

- 筛选/主页面表单：长者、状态、关键词。
- 模板直接声明的表格列：券号、券名称、绑定长者、面额、剩余额度、使用门槛、有效期、状态、备注、操作、时间、方向、金额、长者、账单月份、收款记录。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：关闭后该券必须一次用完剩余额度、选中 N 位长者会各自生成 1 张独立券号的券。
- 弹层标题：发放消费券、`核销流水 · ${activeVoucher?.voucherNo ／／ ''}`。
- 弹层表单字段：券名称、面额（元）、使用门槛（账单应收满，0 为不限）、有效期、允许拆分使用、发放对象、发放来源、备注。
- 可读操作文案：查询、重置、发放消费券、核销流水、作废。

### 15. 电费管理 `/finance/electricity-fee`

账期、楼栋房间、缴费状态；读数、用量、电价、电费、分摊；电表登记及电价生效设置。

- 导航归属：财务运营中心 > 账单与账户 > 电费管理。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/ElectricityFee.vue](/Users/yang/smartcare/admin-web/src/views/finance/ElectricityFee.vue)**

- 筛选/主页面表单：账期、楼栋、房间号、缴费状态。
- 模板直接声明的表格列：楼栋、楼层、房间、上期读数、本期读数、用电量(度)、电价、电费(元)、分摊方式、缴费状态、备注、操作、生效日期、电价(元/度)。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：人 · 人均、已自动带出上一账期的本期读数，可修正、用电量、度、电价、元/度、电费、元。
- 弹层标题：`登记电表读数 · ${dayjs(query.month).format('YYYY年MM月')}`、电价设置。
- 弹层表单字段：房间、上期读数、本期读数、分摊方式、备注、电价（元/度）、生效日期。
- 可读操作文案：查询、重置、登记读数、电价设置、改读数。

### 16. 押金管理 `/finance/deposit-management`

长者/状态；在押余额、未缴清人数、扣退汇总；应缴、实缴、差额、扣退与台账；押金标准设置。

- 导航归属：财务运营中心 > 账单与账户 > 押金管理。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/DepositManagement.vue](/Users/yang/smartcare/admin-web/src/views/finance/DepositManagement.vue)**

- 筛选/主页面表单：长者、状态。
- 模板直接声明的表格列：长者、护理等级、房间 / 床位、应缴标准、已缴、应缴差额、已扣、已退、在押余额、状态、操作、时间、类型、金额、操作后余额、方式、原因、备注、名称、金额(元)、生效日期。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：txnTitle、`押金台账 · ${activeRow?.elderName ／／ ''}`、押金标准。
- 弹层表单字段：长者、金额（元）、发生时间、收款方式、原因、备注、名称、护理等级、金额、生效日。
- 可读操作文案：查询、重置、押金缴纳、押金标准、详情、缴纳、扣款、退还、关闭、按当前护理等级刷新应缴、取消编辑、编辑、删除。

### 17. 余额预警规则 `/finance/accounts/warning-rules`

余额阈值；欠费、待充值、合同临期风险摘要；通知对象、联动入口及规则配置快照。

- 导航归属：财务运营中心 > 账单与账户 > 余额预警规则。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/BalanceWarningRules.vue](/Users/yang/smartcare/admin-web/src/views/finance/BalanceWarningRules.vue)**

- 筛选/主页面表单：账户余额预警阈值（元）。
- 模板直接声明的表格列：维度、数量、金额（元）、配置键、配置值、生效月份、状态、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、导出、导入CSV、查看变更记录、保存阈值、保存通知配置、低余额账户、自动催缴管理、返回财务工作台。

### 18. 入住费用审核 `/finance/admission-fee-audit`

长者、入住记录、费用总额、押金、审核状态、审核备注；新建、通过与驳回。

- 导航归属：财务运营中心 > 账单与账户 > 入住费用审核。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/AdmissionFeeAudit.vue](/Users/yang/smartcare/admin-web/src/views/finance/AdmissionFeeAudit.vue)**

- 筛选/主页面表单：关键字、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：老人、费用总额、押金金额、状态、审核备注、创建时间、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：待审核、已通过、已驳回、老人、费用总额、押金金额、状态、审核备注、创建时间、操作、确认驳回该审核单？、确认通过该审核单？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：新建入住费用审核。
- 弹层表单字段：老人、入院记录ID、费用总额、押金金额、备注。
- 可读操作文案：新建审核单、通过、驳回。

### 19. 消费登记 `/finance/flows/consumption`

日期、类别、风险筛选；流水数/总额/高风险/来源缺失；长者、金额、来源、备注；新增消费。

- 导航归属：财务运营中心 > 消费与分摊 > 消费登记。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/ConsumptionRegister.vue](/Users/yang/smartcare/admin-web/src/views/finance/ConsumptionRegister.vue)**

- 筛选/主页面表单：开始日期、结束日期、类别、风险等级、老人。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：老人、消费日期、金额、类别、来源类型、风险等级、备注、创建时间。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：老人、消费日期、金额、类别、来源类型、风险等级、备注、创建时间、手工、商城、医护、账单收款、餐饮消费、床位消费、护理消费、押金消费、药品消费、其他消费。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：新增消费登记。
- 弹层表单字段：老人、消费日期、金额、类别、来源类型、备注。
- 可读操作文案：仅看高风险、导出、打印当前结果、新增消费。

### 20. 医护费用流水 `/finance/flows/medical`

医护费用消费流水；日期/长者；今日及本月金额、待处理/异常；来源类型和来源ID。category=MEDICINE。

- 导航归属：财务运营中心 > 消费与分摊 > 医护费用流水。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"moduleKey": "MEDICAL_FLOW", "moduleName": "医护费用流水", "description": "按医嘱、用药、治疗与检查动作汇总收费流水", "category": "MEDICINE", "links": [{"label": "查看医护费用异常", "to": "/finance/flows/medical-errors"}, {"label": "查看费用调整单", "to": "/finance/flows/adjustments"}]}。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/FlowCenter.vue](/Users/yang/smartcare/admin-web/src/views/finance/FlowCenter.vue)**

- 筛选/主页面表单：日期范围、长者、打印备注。
- 模板直接声明的表格列：消费日期、长者、金额、分类、来源类型、来源ID、备注、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：消费日期、长者、金额、分类、来源类型、来源ID、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、重置、导出CSV、打印当前、消费明细、异常处理。

### 21. 医护费用异常 `/finance/flows/medical-errors`

医护费用异常场景的汇总和消费表格；moduleKey=MEDICAL_ERRORS；与普通医护流水不是仅换标题。

- 导航归属：财务运营中心 > 消费与分摊 > 医护费用异常。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"moduleKey": "MEDICAL_ERRORS", "moduleName": "医护费用异常", "description": "重复计费、缺少医嘱关联、异常修正与重算", "category": "MEDICINE", "links": [{"label": "查看医护费用流水", "to": "/finance/flows/medical"}, {"label": "查看费用调整单", "to": "/finance/flows/adjustments"}]}。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/FlowCenter.vue](/Users/yang/smartcare/admin-web/src/views/finance/FlowCenter.vue)**

- 筛选/主页面表单：日期范围、长者、打印备注。
- 模板直接声明的表格列：消费日期、长者、金额、分类、来源类型、来源ID、备注、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：消费日期、长者、金额、分类、来源类型、来源ID、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、重置、导出CSV、打印当前、消费明细、异常处理。

### 22. 餐饮扣费流水 `/finance/flows/dining`

餐饮消费扣费流水，category=DINING；长者、日期、金额、来源与备注。

- 导航归属：财务运营中心 > 消费与分摊 > 餐饮扣费流水。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"moduleKey": "DINING_FLOW", "moduleName": "餐饮扣费流水", "description": "积分点餐扣费、餐饮补扣与异常追踪", "category": "DINING", "links": [{"label": "查看消费流水", "to": "/finance/flows/consumption"}, {"label": "查看在住账单", "to": "/finance/bills/in-resident"}]}。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/FlowCenter.vue](/Users/yang/smartcare/admin-web/src/views/finance/FlowCenter.vue)**

- 筛选/主页面表单：日期范围、长者、打印备注。
- 模板直接声明的表格列：消费日期、长者、金额、分类、来源类型、来源ID、备注、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：消费日期、长者、金额、分类、来源类型、来源ID、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、重置、导出CSV、打印当前、消费明细、异常处理。

### 23. 物资/后勤收费流水 `/finance/flows/logistics`

物资/后勤收费场景，category=OTHER；长者、日期、金额、来源与备注。

- 导航归属：财务运营中心 > 消费与分摊 > 物资/后勤收费流水。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"moduleKey": "LOGISTICS_FLOW", "moduleName": "物资/后勤收费流水", "description": "物资与后勤额外收费项目的自动入账视图", "category": "OTHER", "links": [{"label": "查看消费流水", "to": "/finance/flows/consumption"}, {"label": "查看经营报表", "to": "/finance/reports/overall"}]}。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/FlowCenter.vue](/Users/yang/smartcare/admin-web/src/views/finance/FlowCenter.vue)**

- 筛选/主页面表单：日期范围、长者、打印备注。
- 模板直接声明的表格列：消费日期、长者、金额、分类、来源类型、来源ID、备注、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：消费日期、长者、金额、分类、来源类型、来源ID、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、重置、导出CSV、打印当前、消费明细、异常处理。

### 24. 费用调整单 `/finance/flows/adjustments`

费用调整场景汇总及列表，moduleKey=ADJUSTMENTS；联动审批、退住审核。

- 导航归属：财务运营中心 > 消费与分摊 > 费用调整单。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"moduleKey": "ADJUSTMENTS", "moduleName": "费用调整单", "description": "减免、补录、改价调整申请与审批跟踪", "links": [{"label": "查看审批中心", "to": "/oa/approval?module=finance&status=pending"}, {"label": "查看退住审核", "to": "/finance/discharge/review?status=pending"}]}。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/FlowCenter.vue](/Users/yang/smartcare/admin-web/src/views/finance/FlowCenter.vue)**

- 筛选/主页面表单：日期范围、长者、打印备注。
- 模板直接声明的表格列：消费日期、长者、金额、分类、来源类型、来源ID、备注、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：消费日期、长者、金额、分类、来源类型、来源ID、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、重置、导出CSV、打印当前、消费明细、异常处理。

### 25. 公共费用计算 `/finance/allocation/public-cost`

月度分摊项目、总金额、人数、人均金额、长者、状态；新建分摊、预览、抄表校验与回滚。

- 导航归属：财务运营中心 > 消费与分摊 > 公共费用计算。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：D02。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/MonthlyAllocation.vue](/Users/yang/smartcare/admin-web/src/views/finance/MonthlyAllocation.vue)**

- 筛选/主页面表单：月份、状态、老人、打印备注。
- 模板直接声明的表格列：老人姓名、分摊金额。
- 脚本列 `columns`：月份、分摊项目、总金额、分摊老人、目标人数、人均金额、状态、备注、创建时间、操作。
- 脚本列 `meterColumns`：#、楼栋、楼层、房间号、上期、本期、用量、级别、原因。
- 表格列绑定：columns、meterColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.5。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：已确认、已取消、月份、分摊项目、总金额、分摊老人、目标人数、人均金额、状态、备注、创建时间、操作、楼栋、楼层、房间号、上期、本期、用量、级别、原因、确认回滚该分摊记录？、月分摊费。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：（同步时间： ）、建议先预览后确认创建。
- 弹层标题：新建月分摊、分摊预览、月抄表数据校验。
- 弹层表单字段：分摊月份、分摊项目、分摊总金额、房间定位选人、分摊老人、自动核算人数、自动核算人均金额、备注、分摊预览、月份、异常波动阈值（度）、抄表数据（每行：房间号,上期读数,本期读数,楼栋,楼层,备注）。
- 可读操作文案：导出当前、打印当前、抄表校验、新建分摊、回滚、立即同步、预览分摊明细。

### 26. 分摊入账任务 `/finance/allocation/tasks`

与“公共费用计算”挂载相同 MonthlyAllocation，无路由场景区分；展示同一组分摊记录与操作。

- 导航归属：财务运营中心 > 消费与分摊 > 分摊入账任务。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：D02。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/MonthlyAllocation.vue](/Users/yang/smartcare/admin-web/src/views/finance/MonthlyAllocation.vue)**

- 筛选/主页面表单：月份、状态、老人、打印备注。
- 模板直接声明的表格列：老人姓名、分摊金额。
- 脚本列 `columns`：月份、分摊项目、总金额、分摊老人、目标人数、人均金额、状态、备注、创建时间、操作。
- 脚本列 `meterColumns`：#、楼栋、楼层、房间号、上期、本期、用量、级别、原因。
- 表格列绑定：columns、meterColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.5。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：已确认、已取消、月份、分摊项目、总金额、分摊老人、目标人数、人均金额、状态、备注、创建时间、操作、楼栋、楼层、房间号、上期、本期、用量、级别、原因、确认回滚该分摊记录？、月分摊费。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：（同步时间： ）、建议先预览后确认创建。
- 弹层标题：新建月分摊、分摊预览、月抄表数据校验。
- 弹层表单字段：分摊月份、分摊项目、分摊总金额、房间定位选人、分摊老人、自动核算人数、自动核算人均金额、备注、分摊预览、月份、异常波动阈值（度）、抄表数据（每行：房间号,上期读数,本期读数,楼栋,楼层,备注）。
- 可读操作文案：导出当前、打印当前、抄表校验、新建分摊、回滚、立即同步、预览分摊明细。

### 27. 欠费与提醒中心 `/finance/bills/follow-up`

“自动提醒/催缴跟进”双页签；提醒记录、跟进对象、欠费金额、跟进责任与处理情况，分别由子组件承载。

- 导航归属：财务运营中心 > 欠费与异常 > 欠费与提醒中心。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：页签 → 数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/CollectionCenter.vue](/Users/yang/smartcare/admin-web/src/views/finance/CollectionCenter.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：自动提醒、催缴跟进。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/finance/components/ReminderPanel.vue](/Users/yang/smartcare/admin-web/src/views/finance/components/ReminderPanel.vue)**

- 筛选/主页面表单：状态、类型。
- 模板直接声明的表格列：生成时间、类型、提醒、说明、账期、金额 / 数量、状态、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：下月代养费、押金未缴清、电费未缴。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、重置、立即检查生成、一键全部处理、处理该类全部、去处理、标记已处理。

**承载内容的子组件：[admin-web/src/views/finance/components/CollectionFollowUpPanel.vue](/Users/yang/smartcare/admin-web/src/views/finance/components/CollectionFollowUpPanel.vue)**

- 筛选/主页面表单：月份、风险等级、跟进阶段。
- 模板直接声明的表格列：长者、起始账期、最近账期、欠费金额、账户余额、拖欠月数、风险、阶段、最近跟进、责任人、承诺付款日、下次提醒、最近跟进时间、最近收款、合同到期、跟进理由、建议动作、操作。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: 1.1fr 1fr、height: 1.18、grid-template-columns: 1fr。
- 页签/分区：把欠费、低余额和续约临界放进同一张跟进清单。。
- 指标和说明静态文字：每位长者只保留一行，优先看谁最需要今天联系，而不是按账单散落处理。。
- 弹层标题：催缴跟进记录。
- 弹层表单字段：跟进阶段、处理状态、承诺付款日、责任人、联系人、联系渠道、联系结果、下次提醒、下一步动作、跟进备注。
- 可读操作文案：查询、导出、重置、跟进、详情。

### 28. 自动扣费与催缴 `/finance/bills/auto-deduct`

自动扣费成功/失败/待处理与风险金额；长者、账单月、应扣金额、余额、失败原因和处理建议。

- 导航归属：财务运营中心 > 欠费与异常 > 自动扣费与催缴。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：D03。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/AutoDebitManagement.vue](/Users/yang/smartcare/admin-web/src/views/finance/AutoDebitManagement.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：账单月、长者、应扣金额、账户余额、风险等级、失败原因、处理建议、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：自动扣费异常列表、账单月、长者、应扣金额、账户余额、风险等级、失败原因、处理建议。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、导出CSV、打印当前结果、低余额账户、对账异常、一致性巡检、查看账户、转人工收款。

### 29. 自动扣费异常 `/finance/bills/auto-deduct-errors`

同“自动扣费与催缴”组件与默认查询，仍展示扣费异常列表。

- 导航归属：财务运营中心 > 欠费与异常 > 自动扣费异常。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：D03。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/AutoDebitManagement.vue](/Users/yang/smartcare/admin-web/src/views/finance/AutoDebitManagement.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：账单月、长者、应扣金额、账户余额、风险等级、失败原因、处理建议、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：自动扣费异常列表、账单月、长者、应扣金额、账户余额、风险等级、失败原因、处理建议。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、导出CSV、打印当前结果、低余额账户、对账异常、一致性巡检、查看账户、转人工收款。

### 30. 对账异常处理 `/finance/reconcile/exception`

日期、异常类型、风险；异常数/金额；时间、长者、异常描述、建议与联动处理。

- 导航归属：财务运营中心 > 欠费与异常 > 对账异常处理。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：D11。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/ReconcileException.vue](/Users/yang/smartcare/admin-web/src/views/finance/ReconcileException.vue)**

- 筛选/主页面表单：日期、异常类型、风险等级。
- 模板直接声明的表格列：发生时间、异常类型、风险等级、长者、涉及金额、异常描述、处理建议、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：对账异常处理、发生时间、异常类型、风险等级、长者、涉及金额、异常描述、处理建议。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、仅看重复收款、仅看发票未关联、导出CSV、打印当前、财务一致性巡检、返回对账中心、查看账单、去处理。

### 31. 财务一致性巡检 `/finance/reconcile/ledger-health`

账单、收款、流水一致性检查；问题总量、明细/已付不一致；期望金额、实际金额及异常描述。

- 导航归属：财务运营中心 > 欠费与异常 > 财务一致性巡检。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：D11。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/LedgerHealth.vue](/Users/yang/smartcare/admin-web/src/views/finance/LedgerHealth.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：异常类型、账单ID、收款ID、老人、期望金额、实际金额、异常描述、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：财务一致性巡检、异常类型、账单ID、收款ID、老人、期望金额、实际金额、异常描述。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新巡检、导出CSV、打印当前、返回对账中心、查看账单、去处理。

### 32. 异常修复中心 `/finance/reconcile/issue-center`

跨来源财务异常队列；风险、状态、责任人、时限、处理动作、复核结论、备注及处理留痕。

- 导航归属：财务运营中心 > 欠费与异常 > 异常修复中心。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：D11。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/FinanceIssueCenter.vue](/Users/yang/smartcare/admin-web/src/views/finance/FinanceIssueCenter.vue)**

- 筛选/主页面表单：日期、风险等级、来源。
- 模板直接声明的表格列：发生时间、来源模块、异常类型、风险等级、处理状态、责任人、处理时限、最近处理时间、长者、涉及金额、异常描述、处理建议、操作。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: 1.1fr 1fr、height: 1.18、grid-template-columns: 1fr。
- 页签/分区：把分散在多个菜单里的财务异常压平到一个队列里。。
- 指标和说明静态文字：按风险和来源聚焦处理，减少“先找问题在哪个页面”的切换成本。。
- 弹层标题：异常处理留痕。
- 弹层表单字段：处理状态、下一步动作、责任人、处理时限、复核结论、处理备注。
- 可读操作文案：查询、导出、重置、留痕、账单。

### 33. 退住审核 `/finance/discharge/review`

长者、退住申请、费用项、应收应付、风险等级、审批状态与备注；新建及审核。

- 导航归属：财务运营中心 > 结算与月结 > 退住审核。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/DischargeFeeAudit.vue](/Users/yang/smartcare/admin-web/src/views/finance/DischargeFeeAudit.vue)**

- 筛选/主页面表单：老人、状态、风险等级。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：老人、申请ID、费用项、退住费用设置、应收应付、风险等级、状态、审核备注、创建时间、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：待审核、已通过、已驳回、老人、申请ID、费用项、退住费用设置、应收应付、风险等级、状态、审核备注、创建时间、操作、确认驳回该审核单？、确认通过该审核单？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：新建退住费用审核。
- 弹层表单字段：老人、退住申请ID、应收应付金额、费用项、退住费用设置、备注。
- 可读操作文案：仅看高风险、新建审核单、通过、驳回。

### 34. 退住结算 `/finance/discharge/settlement`

退住应收、押金抵扣、退款、补缴；押金处理、对账、床位释放、前台/护理签字、财务退款和状态。

- 导航归属：财务运营中心 > 结算与月结 > 退住结算。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D04。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/DischargeSettlement.vue](/Users/yang/smartcare/admin-web/src/views/finance/DischargeSettlement.vue)**

- 筛选/主页面表单：老人姓名、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：退住明细单号、老人、费用项、退住费用设置、应收金额、押金抵扣、应退款、需补缴、押金处理、自动对账、床位释放、风险等级、前台签字、护理部签字、财务退款时间、状态、创建时间、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：待确认、已结算、已取消、退住明细单号、老人、费用项、退住费用设置、应收金额、押金抵扣、应退款、需补缴、押金处理、自动对账、床位释放、风险等级、前台签字、护理部签字、财务退款时间、状态、创建时间、操作、确认执行财务退款？、确认前台签字？、确认护理部签字？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：新建退住结算单。
- 弹层表单字段：老人、退住申请ID、应收金额、费用项、退住费用设置、备注。
- 可读操作文案：新建结算单、前台签字、护理部签字、财务退款。

### 35. 结算单打印 `/finance/discharge/print-sign`

同退住结算组件；包含相同结算台账和签字/退款/打印操作，没有独立打印视图预设。

- 导航归属：财务运营中心 > 结算与月结 > 结算单打印。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D04。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/DischargeSettlement.vue](/Users/yang/smartcare/admin-web/src/views/finance/DischargeSettlement.vue)**

- 筛选/主页面表单：老人姓名、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：退住明细单号、老人、费用项、退住费用设置、应收金额、押金抵扣、应退款、需补缴、押金处理、自动对账、床位释放、风险等级、前台签字、护理部签字、财务退款时间、状态、创建时间、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：待确认、已结算、已取消、退住明细单号、老人、费用项、退住费用设置、应收金额、押金抵扣、应退款、需补缴、押金处理、自动对账、床位释放、风险等级、前台签字、护理部签字、财务退款时间、状态、创建时间、操作、确认执行财务退款？、确认前台签字？、确认护理部签字？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：新建退住结算单。
- 弹层表单字段：老人、退住申请ID、应收金额、费用项、退住费用设置、备注。
- 可读操作文案：新建结算单、前台签字、护理部签字、财务退款。

### 36. 结算状态回写 `/finance/discharge/status-sync`

结算完成后的状态一致性；结算单、长者状态、档案床位/实际占床、异常与单条/批量回写。

- 导航归属：财务运营中心 > 结算与月结 > 结算状态回写。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/DischargeStatusSync.vue](/Users/yang/smartcare/admin-web/src/views/finance/DischargeStatusSync.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：结算单ID、老人、结算时间、异常类型、异常描述、老人状态、档案床位ID、占用床位ID、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：确认执行该条状态回写？、确认执行全部状态回写？、结算完成状态回写、结算单ID、老人姓名、结算时间、异常类型、异常描述、老人状态、档案床位ID、占用床位ID。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新巡检、执行全部回写、导出CSV、打印当前、查看退住结算、查看房态图、看结算、执行回写。

### 37. 对账中心 `/finance/reconcile/center`

日收款/退款/净收款；未核销、重复冲正、发票未关联；异常分级明细、对账历史及生成对账。

- 导航归属：财务运营中心 > 结算与月结 > 对账中心。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/Reconcile.vue](/Users/yang/smartcare/admin-web/src/views/finance/Reconcile.vue)**

- 筛选/主页面表单：日期、打印备注、开始日期、结束日期。
- 模板直接声明的表格列：发生时间、异常类型、长者、金额、等级、异常描述、操作、对账日期、收款总额、退款总额、净收款、是否异常、备注、生成时间。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：对账历史、对账日期、收款总额、退款总额、净收款、是否异常、备注、生成时间。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：生成对账、重置、仅看高风险、全部风险、异常处理、查看账单、去处理、查询、导出、打印当前。

### 38. 发票对账 `/finance/reconcile/invoice`

收款笔数、发票已关联/未关联、缺票据金额；长者、金额、支付方式、收据号与关联状态。

- 导航归属：财务运营中心 > 结算与月结 > 发票对账。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：D10。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/ReconcileInvoice.vue](/Users/yang/smartcare/admin-web/src/views/finance/ReconcileInvoice.vue)**

- 筛选/主页面表单：日期、支付方式、关联状态、长者。
- 模板直接声明的表格列：收款时间、长者、金额、支付方式、收据号、发票关联、备注、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：发票对账、收款时间、长者、金额、支付方式、收据号、发票关联、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、重置、导出CSV、打印当前、返回对账中心、查看账单、去补关联。

### 39. 月结进度 `/finance/reconcile/month-close`

月份；账单、收款、未结账单、缺票据、修正、一致性问题；检查链路、关账/反锁与跨期申请。

- 导航归属：财务运营中心 > 结算与月结 > 月结进度。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/FinanceMonthClose.vue](/Users/yang/smartcare/admin-web/src/views/finance/FinanceMonthClose.vue)**

- 筛选/主页面表单：月份。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: 1.1fr 1fr、height: 1.18、min-height: 236px、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：财务月结单、步骤、状态、数量、说明。
- 页签/分区：财务月结不再靠口头确认，而是直接看到哪一步卡住。。
- 指标和说明静态文字：这页把本月账单、票据、异常修复和催缴收口状态压成一张进度图。、当前数量。
- 弹层标题：月结关账、反锁账、跨期处理申请。
- 弹层表单字段：关账备注、反锁原因、补充说明、动作类型、申请原因。
- 可读操作文案：刷新摘要、导出月结单、打印月结单、发起关账、跨期申请、异常修复中心、催缴跟进、反锁账。

### 40. 总收支 `/finance/reports/overall`

总收支与现金流：净营收、开账、实收、退款、商城销售、欠费；趋势、费用科目、房间与欠费排行。

- 导航归属：财务运营中心 > 经营分析 > 总收支。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：页头/报表页签 → 区间筛选 → 指标 → 双列图表与排行 → 明细；Excel 路由为专用导出视图。
- 关联重复项：D13。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/Report.vue](/Users/yang/smartcare/admin-web/src/views/finance/Report.vue)**

- 筛选/主页面表单：起始月份、结束月份、开始日期、结束日期、物品关键字、楼栋、楼层。
- 模板直接声明的表格列：pageMeta.leftTableLabel、金额、编码、pageMeta.rightTableLabel、长者、欠费金额、类目、消费额、成本、利润、利润率。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr)、grid-template-columns: repeat(auto-fit, minmax(170px, 1fr))、height: 1.6、grid-template-columns: repeat(6, minmax(0, 1fr))、min-height: 126px、height: 1.12、grid-template-columns: repeat(2, minmax(0, 1fr))、height: 1.2、height: 320px、height: 280px、grid-template-columns: repeat(auto-fit, minmax(150px, 1fr))、grid-template-columns: 1fr、grid-template-columns: repeat(3, minmax(0, 1fr))。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：总收支、营收结构、楼层房间、入住消费、月运营、Excel 报表、本月、近3个月、近6个月、近12个月、楼层房间收支、入住与消费、机构月运营、总收支与现金流、净营收、开账金额、实收金额、退款金额、商城销售、欠费金额、退款、房间收支、房间营收、费用结构、实收、销售额、消费额、占比(%)、关键字消费额、关键字占比。
- 页签/分区：常用跳转、类目消费比例与盈利分析、关键字消费波动线、全类目盈利情况。
- 指标和说明静态文字：当前范围、至、当前类目筛选：、净营收、开账金额、实收金额、退款金额、返回工作台、回到今日任务首页继续处理收费与对账。、查看异常修复、从报表直接跳去处理差异和挂账问题。、欠费催缴、把欠费排行直接转成催缴跟进动作。、房间经营详情、到房态图定位房间，单间的收入与净额在房间详情页里看。、区间总消费、关键字消费额、关键字消费占比、%。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、重置、清除类目筛选、查询、查看全部类目、导出盈利报表、导出趋势、打印。

**承载内容的子组件：[admin-web/src/views/finance/components/ExcelReportPanel.vue](/Users/yang/smartcare/admin-web/src/views/finance/components/ExcelReportPanel.vue)**

- 筛选/主页面表单：账期、押金状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `reports`：代养费登记表、押金台账、电费月报、欠费催缴清单。
- CSS布局声明（含响应式分支及局部图表）：height: 1.6。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：代养费登记表、押金台账、电费月报、欠费催缴清单。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出。

### 41. 营收结构 `/finance/reports/revenue-structure`

营收结构Top、净营收与退款趋势、重点费用科目、房间收支、欠费长者排行。

- 导航归属：财务运营中心 > 经营分析 > 营收结构。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：页头/报表页签 → 区间筛选 → 指标 → 双列图表与排行 → 明细；Excel 路由为专用导出视图。
- 关联重复项：D13。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/Report.vue](/Users/yang/smartcare/admin-web/src/views/finance/Report.vue)**

- 筛选/主页面表单：起始月份、结束月份、开始日期、结束日期、物品关键字、楼栋、楼层。
- 模板直接声明的表格列：pageMeta.leftTableLabel、金额、编码、pageMeta.rightTableLabel、长者、欠费金额、类目、消费额、成本、利润、利润率。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr)、grid-template-columns: repeat(auto-fit, minmax(170px, 1fr))、height: 1.6、grid-template-columns: repeat(6, minmax(0, 1fr))、min-height: 126px、height: 1.12、grid-template-columns: repeat(2, minmax(0, 1fr))、height: 1.2、height: 320px、height: 280px、grid-template-columns: repeat(auto-fit, minmax(150px, 1fr))、grid-template-columns: 1fr、grid-template-columns: repeat(3, minmax(0, 1fr))。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：总收支、营收结构、楼层房间、入住消费、月运营、Excel 报表、本月、近3个月、近6个月、近12个月、楼层房间收支、入住与消费、机构月运营、总收支与现金流、净营收、开账金额、实收金额、退款金额、商城销售、欠费金额、退款、房间收支、房间营收、费用结构、实收、销售额、消费额、占比(%)、关键字消费额、关键字占比。
- 页签/分区：常用跳转、类目消费比例与盈利分析、关键字消费波动线、全类目盈利情况。
- 指标和说明静态文字：当前范围、至、当前类目筛选：、净营收、开账金额、实收金额、退款金额、返回工作台、回到今日任务首页继续处理收费与对账。、查看异常修复、从报表直接跳去处理差异和挂账问题。、欠费催缴、把欠费排行直接转成催缴跟进动作。、房间经营详情、到房态图定位房间，单间的收入与净额在房间详情页里看。、区间总消费、关键字消费额、关键字消费占比、%。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、重置、清除类目筛选、查询、查看全部类目、导出盈利报表、导出趋势、打印。

**承载内容的子组件：[admin-web/src/views/finance/components/ExcelReportPanel.vue](/Users/yang/smartcare/admin-web/src/views/finance/components/ExcelReportPanel.vue)**

- 筛选/主页面表单：账期、押金状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `reports`：代养费登记表、押金台账、电费月报、欠费催缴清单。
- CSS布局声明（含响应式分支及局部图表）：height: 1.6。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：代养费登记表、押金台账、电费月报、欠费催缴清单。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出。

### 42. 楼层房间收支 `/finance/reports/floor-room-profit`

房间收支Top、欠费风险分布；费用科目、房间及欠费长者表格。

- 导航归属：财务运营中心 > 经营分析 > 楼层房间收支。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：页头/报表页签 → 区间筛选 → 指标 → 双列图表与排行 → 明细；Excel 路由为专用导出视图。
- 关联重复项：D13。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/Report.vue](/Users/yang/smartcare/admin-web/src/views/finance/Report.vue)**

- 筛选/主页面表单：起始月份、结束月份、开始日期、结束日期、物品关键字、楼栋、楼层。
- 模板直接声明的表格列：pageMeta.leftTableLabel、金额、编码、pageMeta.rightTableLabel、长者、欠费金额、类目、消费额、成本、利润、利润率。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr)、grid-template-columns: repeat(auto-fit, minmax(170px, 1fr))、height: 1.6、grid-template-columns: repeat(6, minmax(0, 1fr))、min-height: 126px、height: 1.12、grid-template-columns: repeat(2, minmax(0, 1fr))、height: 1.2、height: 320px、height: 280px、grid-template-columns: repeat(auto-fit, minmax(150px, 1fr))、grid-template-columns: 1fr、grid-template-columns: repeat(3, minmax(0, 1fr))。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：总收支、营收结构、楼层房间、入住消费、月运营、Excel 报表、本月、近3个月、近6个月、近12个月、楼层房间收支、入住与消费、机构月运营、总收支与现金流、净营收、开账金额、实收金额、退款金额、商城销售、欠费金额、退款、房间收支、房间营收、费用结构、实收、销售额、消费额、占比(%)、关键字消费额、关键字占比。
- 页签/分区：常用跳转、类目消费比例与盈利分析、关键字消费波动线、全类目盈利情况。
- 指标和说明静态文字：当前范围、至、当前类目筛选：、净营收、开账金额、实收金额、退款金额、返回工作台、回到今日任务首页继续处理收费与对账。、查看异常修复、从报表直接跳去处理差异和挂账问题。、欠费催缴、把欠费排行直接转成催缴跟进动作。、房间经营详情、到房态图定位房间，单间的收入与净额在房间详情页里看。、区间总消费、关键字消费额、关键字消费占比、%。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、重置、清除类目筛选、查询、查看全部类目、导出盈利报表、导出趋势、打印。

**承载内容的子组件：[admin-web/src/views/finance/components/ExcelReportPanel.vue](/Users/yang/smartcare/admin-web/src/views/finance/components/ExcelReportPanel.vue)**

- 筛选/主页面表单：账期、押金状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `reports`：代养费登记表、押金台账、电费月报、欠费催缴清单。
- CSS布局声明（含响应式分支及局部图表）：height: 1.6。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：代养费登记表、押金台账、电费月报、欠费催缴清单。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出。

### 43. 入住与消费 `/finance/reports/occupancy-consumption`

房间营收Top、消费类目结构；房间、费用科目和欠费长者明细。

- 导航归属：财务运营中心 > 经营分析 > 入住与消费。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：页头/报表页签 → 区间筛选 → 指标 → 双列图表与排行 → 明细；Excel 路由为专用导出视图。
- 关联重复项：D13。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/Report.vue](/Users/yang/smartcare/admin-web/src/views/finance/Report.vue)**

- 筛选/主页面表单：起始月份、结束月份、开始日期、结束日期、物品关键字、楼栋、楼层。
- 模板直接声明的表格列：pageMeta.leftTableLabel、金额、编码、pageMeta.rightTableLabel、长者、欠费金额、类目、消费额、成本、利润、利润率。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr)、grid-template-columns: repeat(auto-fit, minmax(170px, 1fr))、height: 1.6、grid-template-columns: repeat(6, minmax(0, 1fr))、min-height: 126px、height: 1.12、grid-template-columns: repeat(2, minmax(0, 1fr))、height: 1.2、height: 320px、height: 280px、grid-template-columns: repeat(auto-fit, minmax(150px, 1fr))、grid-template-columns: 1fr、grid-template-columns: repeat(3, minmax(0, 1fr))。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：总收支、营收结构、楼层房间、入住消费、月运营、Excel 报表、本月、近3个月、近6个月、近12个月、楼层房间收支、入住与消费、机构月运营、总收支与现金流、净营收、开账金额、实收金额、退款金额、商城销售、欠费金额、退款、房间收支、房间营收、费用结构、实收、销售额、消费额、占比(%)、关键字消费额、关键字占比。
- 页签/分区：常用跳转、类目消费比例与盈利分析、关键字消费波动线、全类目盈利情况。
- 指标和说明静态文字：当前范围、至、当前类目筛选：、净营收、开账金额、实收金额、退款金额、返回工作台、回到今日任务首页继续处理收费与对账。、查看异常修复、从报表直接跳去处理差异和挂账问题。、欠费催缴、把欠费排行直接转成催缴跟进动作。、房间经营详情、到房态图定位房间，单间的收入与净额在房间详情页里看。、区间总消费、关键字消费额、关键字消费占比、%。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、重置、清除类目筛选、查询、查看全部类目、导出盈利报表、导出趋势、打印。

**承载内容的子组件：[admin-web/src/views/finance/components/ExcelReportPanel.vue](/Users/yang/smartcare/admin-web/src/views/finance/components/ExcelReportPanel.vue)**

- 筛选/主页面表单：账期、押金状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `reports`：代养费登记表、押金台账、电费月报、欠费催缴清单。
- CSS布局声明（含响应式分支及局部图表）：height: 1.6。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：代养费登记表、押金台账、电费月报、欠费催缴清单。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出。

### 44. Excel 报表 `/finance/reports/excel`

同一报表组件内的Excel导出视图：代养费登记表、押金台账、电费月报、催缴清单；不是空白导出按钮页。

- 导航归属：财务运营中心 > 经营分析 > Excel 报表。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：页头/报表页签 → 区间筛选 → 指标 → 双列图表与排行 → 明细；Excel 路由为专用导出视图。
- 关联重复项：D13。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/Report.vue](/Users/yang/smartcare/admin-web/src/views/finance/Report.vue)**

- 筛选/主页面表单：起始月份、结束月份、开始日期、结束日期、物品关键字、楼栋、楼层。
- 模板直接声明的表格列：pageMeta.leftTableLabel、金额、编码、pageMeta.rightTableLabel、长者、欠费金额、类目、消费额、成本、利润、利润率。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr)、grid-template-columns: repeat(auto-fit, minmax(170px, 1fr))、height: 1.6、grid-template-columns: repeat(6, minmax(0, 1fr))、min-height: 126px、height: 1.12、grid-template-columns: repeat(2, minmax(0, 1fr))、height: 1.2、height: 320px、height: 280px、grid-template-columns: repeat(auto-fit, minmax(150px, 1fr))、grid-template-columns: 1fr、grid-template-columns: repeat(3, minmax(0, 1fr))。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：总收支、营收结构、楼层房间、入住消费、月运营、Excel 报表、本月、近3个月、近6个月、近12个月、楼层房间收支、入住与消费、机构月运营、总收支与现金流、净营收、开账金额、实收金额、退款金额、商城销售、欠费金额、退款、房间收支、房间营收、费用结构、实收、销售额、消费额、占比(%)、关键字消费额、关键字占比。
- 页签/分区：常用跳转、类目消费比例与盈利分析、关键字消费波动线、全类目盈利情况。
- 指标和说明静态文字：当前范围、至、当前类目筛选：、净营收、开账金额、实收金额、退款金额、返回工作台、回到今日任务首页继续处理收费与对账。、查看异常修复、从报表直接跳去处理差异和挂账问题。、欠费催缴、把欠费排行直接转成催缴跟进动作。、房间经营详情、到房态图定位房间，单间的收入与净额在房间详情页里看。、区间总消费、关键字消费额、关键字消费占比、%。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、重置、清除类目筛选、查询、查看全部类目、导出盈利报表、导出趋势、打印。

**承载内容的子组件：[admin-web/src/views/finance/components/ExcelReportPanel.vue](/Users/yang/smartcare/admin-web/src/views/finance/components/ExcelReportPanel.vue)**

- 筛选/主页面表单：账期、押金状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `reports`：代养费登记表、押金台账、电费月报、欠费催缴清单。
- CSS布局声明（含响应式分支及局部图表）：height: 1.6。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：代养费登记表、押金台账、电费月报、欠费催缴清单。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出。

### 45. 机构月运营 `/finance/reports/monthly-ops`

净营收与实收趋势、营收结构；费用科目、房间、欠费长者；机构月运营视图。

- 导航归属：财务运营中心 > 经营分析 > 机构月运营。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：页头/报表页签 → 区间筛选 → 指标 → 双列图表与排行 → 明细；Excel 路由为专用导出视图。
- 关联重复项：D13。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/Report.vue](/Users/yang/smartcare/admin-web/src/views/finance/Report.vue)**

- 筛选/主页面表单：起始月份、结束月份、开始日期、结束日期、物品关键字、楼栋、楼层。
- 模板直接声明的表格列：pageMeta.leftTableLabel、金额、编码、pageMeta.rightTableLabel、长者、欠费金额、类目、消费额、成本、利润、利润率。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.1fr) minmax(320px, 0.9fr)、grid-template-columns: repeat(auto-fit, minmax(170px, 1fr))、height: 1.6、grid-template-columns: repeat(6, minmax(0, 1fr))、min-height: 126px、height: 1.12、grid-template-columns: repeat(2, minmax(0, 1fr))、height: 1.2、height: 320px、height: 280px、grid-template-columns: repeat(auto-fit, minmax(150px, 1fr))、grid-template-columns: 1fr、grid-template-columns: repeat(3, minmax(0, 1fr))。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：总收支、营收结构、楼层房间、入住消费、月运营、Excel 报表、本月、近3个月、近6个月、近12个月、楼层房间收支、入住与消费、机构月运营、总收支与现金流、净营收、开账金额、实收金额、退款金额、商城销售、欠费金额、退款、房间收支、房间营收、费用结构、实收、销售额、消费额、占比(%)、关键字消费额、关键字占比。
- 页签/分区：常用跳转、类目消费比例与盈利分析、关键字消费波动线、全类目盈利情况。
- 指标和说明静态文字：当前范围、至、当前类目筛选：、净营收、开账金额、实收金额、退款金额、返回工作台、回到今日任务首页继续处理收费与对账。、查看异常修复、从报表直接跳去处理差异和挂账问题。、欠费催缴、把欠费排行直接转成催缴跟进动作。、房间经营详情、到房态图定位房间，单间的收入与净额在房间详情页里看。、区间总消费、关键字消费额、关键字消费占比、%。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、重置、清除类目筛选、查询、查看全部类目、导出盈利报表、导出趋势、打印。

**承载内容的子组件：[admin-web/src/views/finance/components/ExcelReportPanel.vue](/Users/yang/smartcare/admin-web/src/views/finance/components/ExcelReportPanel.vue)**

- 筛选/主页面表单：账期、押金状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `reports`：代养费登记表、押金台账、电费月报、欠费催缴清单。
- CSS布局声明（含响应式分支及局部图表）：height: 1.6。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：代养费登记表、押金台账、电费月报、欠费催缴清单。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出。

### 46. 主数据配置中心 `/finance/config/master-data`

科目/计费规则/支付渠道/待审批配置数量；配置导航、近期配置、月度快照、变更影响模拟。

- 导航归属：财务运营中心 > 规则与配置 > 主数据配置中心。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/FinanceMasterData.vue](/Users/yang/smartcare/admin-web/src/views/finance/FinanceMasterData.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：配置键、配置值、生效月份、状态、备注、月份、配置数、最近更新时间、影响模块、影响数量、影响说明。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：费用科目、支付渠道、账单计费、分摊规则、财务主数据配置中心、配置键、配置值、生效月份、状态、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：管理房费/护理费/医护费/水电电视网络等科目、管理渠道启停、收款户名与账户号、配置减免/退款/冲正审批角色与审批级数。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、导出当前、打印当前、计费规则入口、余额预警规则、费用科目字典、缴费渠道/收款账户、权限与审批流配置、配置变更记录、进入维护、模拟影响范围。

### 47. 费用科目 `/finance/allocation/subjects`

科目键、科目名称、默认单价或比例、生效月份、启停与维护。

- 导航归属：财务运营中心 > 规则与配置 > 费用科目。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/FeeSubjectDictionary.vue](/Users/yang/smartcare/admin-web/src/views/finance/FeeSubjectDictionary.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：科目键、科目名称、默认单价/比例、生效月份、状态、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：确认保存费用科目？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：费用科目。
- 弹层表单字段：科目编码（可选，自动补前缀 FEE_SUBJECT_）、科目名称、默认单价/比例、生效月份、启用。
- 可读操作文案：查询、新增科目、批量启用、批量停用、导出、导入CSV、查看变更记录、编辑。

### 48. 计费规则 `/finance/bills/rules`

生效月、关键词；配置键、配置值、状态、备注；计费规则维护及打印。

- 导航归属：财务运营中心 > 规则与配置 > 计费规则。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/BillingRulesConfig.vue](/Users/yang/smartcare/admin-web/src/views/finance/BillingRulesConfig.vue)**

- 筛选/主页面表单：生效月份、关键字、打印备注。
- 模板直接声明的表格列：配置键、配置值、生效月份、状态、备注、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：账单模板/计费规则、配置键、配置值、生效月份、状态、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：计费规则。
- 弹层表单字段：配置键、配置值、生效月份、状态、备注。
- 可读操作文案：查询、新增规则、批量启用、批量停用、导出、打印当前、导入CSV、查看变更记录、应用标准规则模板、应用护理分级模板、应用水电分摊模板、编辑。

### 49. 分摊规则 `/finance/allocation/rules`

分摊规则类型、规则键/值、生效月份、状态、备注。

- 导航归属：财务运营中心 > 规则与配置 > 分摊规则。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/AllocationRules.vue](/Users/yang/smartcare/admin-web/src/views/finance/AllocationRules.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：规则类型、规则键、规则值、生效月份、状态、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、初始化默认电费模板、分摊与公共费用。

### 50. 支付渠道 `/finance/config/payment-channels`

渠道编码与名称、收款户名、收款账号、启停和维护。

- 导航归属：财务运营中心 > 规则与配置 > 支付渠道。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/PaymentChannelConfig.vue](/Users/yang/smartcare/admin-web/src/views/finance/PaymentChannelConfig.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：渠道编码、渠道名称、收款户名、收款账号、状态、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：确认保存缴费渠道？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：缴费渠道。
- 弹层表单字段：渠道编码、渠道名称、收款户名、收款账号、启用。
- 可读操作文案：刷新、新增渠道、批量启用、批量停用、导出、导入CSV、查看变更记录、编辑。

### 51. 审批与权限 `/finance/config/approval-flow`

减免/退款/冲正审批角色与级数；模板、审批数量与金额、健康检查和配置快照。

- 导航归属：财务运营中心 > 规则与配置 > 审批与权限。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/FinanceApprovalFlowConfig.vue](/Users/yang/smartcare/admin-web/src/views/finance/FinanceApprovalFlowConfig.vue)**

- 筛选/主页面表单：减免审批角色、退款审批角色、冲正审批角色、减免审批级数、退款审批级数、冲正审批级数。
- 模板直接声明的表格列：维度、数量、金额（元）、配置键、配置值、生效月份、备注。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：确认保存审批角色配置？、确认保存审批级数配置？、确认应用审批模板？、权限与审批流配置、配置键、配置值、生效月份、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、导出、打印当前、导入CSV、查看变更记录、保存角色配置、保存级数配置、审批中心（财务）、对账异常处理、查看配置变更记录、返回财务工作台、标准模板（推荐）、严格模板、快速模板。

### 52. 配置变更记录 `/finance/config/change-log`

日期、动作、风险筛选；时间、操作人、实体、变更详情；变更量/可回滚/高风险与回滚。

- 导航归属：财务运营中心 > 规则与配置 > 配置变更记录。
- 路由角色约束：[["FINANCE_EMPLOYEE", "FINANCE_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/financeRoutes.ts](/Users/yang/smartcare/admin-web/src/router/financeRoutes.ts)。

**页面：[admin-web/src/views/finance/FinanceConfigChangeLog.vue](/Users/yang/smartcare/admin-web/src/views/finance/FinanceConfigChangeLog.vue)**

- 筛选/主页面表单：开始日期、结束日期、关键字、动作类型、风险等级、打印备注。
- 模板直接声明的表格列：时间、操作人、动作、风险等级、实体类型、实体ID、变更详情、操作。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：确认回滚该配置变更？、财务配置变更记录、时间、操作人、动作、风险等级、实体类型、实体ID、变更详情。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、重置、导出、打印当前、回滚。

## 相关重复项

### D01 收费登记 / 在住周期账单

- 判断：确定重复入口。
- 依据：同一 ResidentBillPayment，无不同 props；内部同为 RESIDENT 账单。
- 后续整理方向：可保留一个账单入口，登记收款作为页内动作。

### D02 公共费用计算 / 分摊入账任务

- 判断：确定重复入口。
- 依据：同一 MonthlyAllocation；未读取路由名/路径区分场景。
- 后续整理方向：一个月度分摊页面承载计算、预览、入账状态。

### D03 自动扣费与催缴 / 自动扣费异常

- 判断：确定重复入口。
- 依据：同一 AutoDebitManagement，无不同 props；使用同一异常列表逻辑。
- 后续整理方向：一个自动扣费页面用状态筛选。

### D04 退住结算 / 结算单打印

- 判断：确定重复入口。
- 依据：同一 DischargeSettlement；仅读取长者/申请/openCreate 查询，不按这两个路径区分。
- 后续整理方向：打印留在结算详情操作中。

### D10 发票与收据 / 发票对账

- 判断：同类信息重复，操作侧重不同。
- 依据：都显示收款与发票关联状态、未关联金额；发票收据页额外承担票据/打印。
- 后续整理方向：共用票据队列，业务入口可保留不同预设。

### D11 异常处理 / 一致性巡检 / 异常修复中心

- 判断：汇总与来源明细重叠。
- 依据：异常修复中心已整合跨来源队列；来源页仍有独立诊断数据。不能推定数据完全相同。
- 后续整理方向：以修复中心为处理入口，来源明细作为下钻。

### D13 六个财务报表入口

- 判断：同模板不同维度。
- 依据：Report 根据 route.name 改变 reportKey、图表、标题和Excel分支。
- 后续整理方向：一个报表中心保留维度页签，不按组件相同直接删除。

### D18 预存充值 / 账户流水 / 一卡通充值

- 判断：相似金额概念，暂不判同一账本。
- 依据：分别可能为长者资金账户和卡账户；仅界面字段相似不足以合并。
- 后续整理方向：先核对资金归属、交易关联和会计口径，再决定收敛。

### D20 多个首页/驾驶舱

- 判断：导航和指标层层重复。
- 依据：总览、个人、部门、行政、驾驶舱重复承担入口和摘要；口径及受众并非完全一样。
- 后续整理方向：明确每类首页唯一职责，摘要点击下钻而非再次进入导航。
