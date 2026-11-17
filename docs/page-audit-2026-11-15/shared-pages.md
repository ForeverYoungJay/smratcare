# 共享工作台、行政、长者及统计：功能、页面信息与排版盘点

日期：2026-11-15。基于当前工作区源码，未改业务页面。

## 阅读口径

“页面入口”按有页面组件的非隐藏路由统计；同一组件可承载不同场景。隐藏兼容入口见兼容清单。以下字段为源码声明的可显示信息，并非同一账号、同一状态下同时出现；弹层内容与默认列表分开记录。动态列、权限、数据状态仍以运行时为准。

本分册记录 69 个页面入口。

## 页面总表

| 页面 | 地址 | 功能及主要信息 | 排版结构 | 重复关联 |
|---|---|---|---|---|
| 经营总览 | /portal | 经营总览：入住/服务/回款/风险、收费押金电费、生日、最近操作、收入趋势、健康安全预警、快捷入口与协同待办。 | 数据表格 → 弹窗/抽屉 → 图表；表格列/操作由脚本或子组件定义。 | D20 |
| 全部功能 | /function-map | 可访问页面的功能地图、分类和搜索；本质为导航页，不维护一套新业务数据。 | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | — |
| 我的工作台 | /workbench/overview | 个人工作台：角色提示、日期/打卡、关键指标、待办、快捷入口、公告、日程、协同、消息和部门业务入口。 | 部门通用首页；信息按模板纵向排列。 | D20 |
| 我的待办 | /workbench/todo | 待办总量与状态/来源分类；标题、负责人、截止时间、状态；自动更新、联动、提醒方式和新增处理。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 我的信息 | /workbench/my-info | 本人资料与账号信息，详见页内个人信息子组件；不是全员档案管理。 | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | — |
| 我的考勤与请假 | /workbench/attendance | 个人或授权员工的月度考勤/请假/行政日程；签到签退、迟到早退、外出、异常判定；另含季节规则设置区域。 | 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 我的日程 | /workbench/schedule | 个人、部门、日常与协同日历；标题、时间、紧急程度、状态、协同成员/部门、周期、颜色及冲突策略。 | 筛选卡片 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 我的总结 | /workbench/reports | 日/周/月/年总结统一列表：标题、周期、总结人、状态；概述、完成工作、问题风险、下一步。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D19 |
| 待我审批 | /workbench/approvals | 复用审批中心组件；审批类型/角色/状态/视角；申请、当前环节/审批人、金额、意见、轮次和业务扩展表单。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D14 |
| 在院服务总览 | /elder/in-hospital-overview | 该入口主要内容由业务子组件提供，见逐页明细。 | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | — |
| 床态全景 | /elder/bed-panorama | 头像、姓名、生日、家庭住址、备注、操作；长者床态指挥舱、当前焦点、床位调度、空间层级、当前范围、当前动作、流程状态、楼栋 / 楼层、空间导航、间房 / 张床位、间房 · 张床位、/、床位状态、快速过滤 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 长者列表 | /elder/list | 长者 / 床位、状态、护理等级、年龄 / 生日、风险 / 关注、身份证（脱敏）、履约阶段、操作；当前列表 位、已启用筛选 项、已选 位、档案操作需要单选一位长者、/、常规、岁 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 入住评估 | /elder/assessment/ability/admission | 该入口主要内容由业务子组件提供，见逐页明细。 | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | — |
| 评估登记 | /elder/assessment/ability/registration | 该入口主要内容由业务子组件提供，见逐页明细。 | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | — |
| 持续评估 | /elder/assessment/ability/continuous | 该入口主要内容由业务子组件提供，见逐页明细。 | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | — |
| 评估档案 | /elder/assessment/ability/archive | 该入口主要内容由业务子组件提供，见逐页明细。 | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | — |
| 其他量表评估 | /elder/assessment/other-scale | 该入口主要内容由业务子组件提供，见逐页明细。 | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | — |
| 认知能力评估 | /elder/assessment/cognitive | 该入口主要内容由业务子组件提供，见逐页明细。 | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | — |
| 自理能力评估 | /elder/assessment/self-care | 该入口主要内容由业务子组件提供，见逐页明细。 | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | — |
| 合同与票据 | /elder/contracts-invoices | 来源、资料类型、文件名、大小、时间；归档完整度（规则引擎）、% | 数据表格；表格列/操作由脚本或子组件定义。 | D17 |
| 入住办理 | /elder/admission-processing | 老人姓名、合同号、收费开始日期、押金（元）、入住状态、备注、操作员姓名、登记时间；入住进度一图看清、办理入住、跨部门协同流程、分床、押金账户、合同信息与二维码一步办理完成。、入住记录筛选统计、更新时间：、入住空间分布占比、人 · | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 变更总览 | /elder/status-change/center | 当前联动待办总量、回执更新时间 | 弹窗/抽屉；信息按模板纵向排列。 | — |
| 外出登记 | /elder/status-change/outing | 老人、外出日期、预计返院、实际返院、外出事由、请假条、状态；- | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 来访登记 | /elder/status-change/visit-register | 来访人、关系、联系电话、老人姓名、居住楼层、房号、来访时间、预约时段、预约码、访客人数、车牌、状态 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 退住申请 | /elder/status-change/discharge-apply | 老人姓名、申请日期、计划退住日期、申请原因、证明附件、后续流程状态、后续流程说明、关联退住单号、审核人、审核备注、审核状态、操作；-、申请原因、证明附件 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 试住登记 | /elder/status-change/trial-stay | 老人姓名、试住开始日期、试住结束日期、来源渠道、试住套餐、意向等级、状态、护理等级、备注 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 外出就医登记 | /elder/status-change/medical-outing | 老人、外出日期、预计返院、实际返院、医院、科室、初步诊断、就医原因、状态 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 死亡登记 | /elder/status-change/death-register | 老人、死亡日期、死亡时间、地点、死亡原因、证明编号、上报人、上报时间、作废时间、状态、备注 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 退院结算 | /elder/status-change/discharge-settlement | 该入口主要内容由业务子组件提供，见逐页明细。 | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | — |
| 行政中心 | /oa/overview | 未完成待办、需跟进的协同事项、审批处理中、待推进的审批单、进行中任务、执行中的行政任务 | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | D20 |
| 家属服务中心 | /oa/family-service | 家属端：、管理端：、· | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | — |
| 家属账号与绑定 | /oa/family-users | 姓名、电话、身份证、关联人数、状态、操作；家属账号数、当前筛选结果下的账号总量、正常账号、可继续绑定老人和接收通知、已绑定老人、按当前页家属账号汇总的绑定人数、待补绑定账号、尚未绑定任何老人的家属账号 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 任务管理 | /oa/work-execution/task | 标题、负责人、开始时间、结束时间、优先级、状态；单条操作、批量操作、批量完成仅对进行中生效 | 筛选卡片 → 页签 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 家属反馈与沟通 | /oa/work-execution/family-communication | 状态、家属、联系方式、沟通内容、提交时间、操作；待处理、处理中、已处理 | 筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 日总结 | /oa/work-execution/daily-report | 该入口主要内容由业务子组件提供，见逐页明细。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D19 |
| 周总结 | /oa/work-execution/weekly-report | 该入口主要内容由业务子组件提供，见逐页明细。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D19 |
| 月总结 | /oa/work-execution/monthly-report | 该入口主要内容由业务子组件提供，见逐页明细。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D19 |
| 年总结 | /oa/work-execution/yearly-report | 该入口主要内容由业务子组件提供，见逐页明细。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D19 |
| 会员生日 | /oa/life/birthday | 老人姓名、出生日期、下次生日、剩余天数、届时年龄、房间号、床位号、操作 | 筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 房间打扫 | /oa/life/room-cleaning | 房间号、保洁员、计划日期、完成时间、状态、操作 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 维修管理 | /oa/life/maintenance | 房间号、故障类型、报修人、维修负责人、优先级、状态、总成本(元)、报修时间、操作 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 卡务管理 | /oa/card/account | 老人、卡号、余额、信用额度、状态、挂失、操作 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 充值记录 | /oa/card/recharge | 时间、老人、卡号、充值金额、余额、备注 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D18 |
| 消费记录 | /oa/card/consume | 时间、老人、卡号、消费金额、余额、备注 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 知识库 | /oa/knowledge | 标题、分类、标签、正文摘要、作者、状态、发布时间；已勾选 条，批量状态流转按当前状态自动过滤 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 通知公告 | /oa/notice | 标题、发布人、发布时间、状态；当前列表 条、已勾选 条、当前列表、按当前筛选范围统计、已发布、面向员工可见的正式公告、草稿、仍待编辑或发布、已选草稿、可直接批量发布、已勾选 条，批量发布仅对“草稿”生效 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 活动中心 | /oa/activity-center/plan | 活动、时间、组织人、地点、风险等级、预算、状态、操作；养老活动不再只是一张日历，而是一条可追踪的执行链。、活动策划、支持活动方案上传、费用审批、执行签到、照片留存与复盘归档，按钮会根据状态自动变化。、待提交方案、审批中、执行中、已完成、基础信息、保存后默认状态为“待提交方案”，后续再进入详情页上传方案并发起审批。、预算信息、· ·、活动类型、组织人、参与对象 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 活动记录 | /oa/activity-center/records | 日期、活动名称、组织者、地点、状态、操作 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 满意度调查 | /oa/activity-center/survey-manage | 模板编码、模板名称、对象、状态、发布联动校验、开始日期、结束日期、计分、编码、题目、题型、必答、权重；已勾选 条 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 调查统计 | /oa/activity-center/survey-stats | 提交ID、模板ID、对象类型、对象ID、提交人、得分、提交时间 | 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 审批中心 | /oa/approval | 类型、标题、申请人、当前环节、当前审批人、审批轮次、金额、状态、审批意见、年度请假次数、操作；审批详情、申请说明、审批流程、审批总单、覆盖当前筛选口径与工作视角、待审批、优先处理待我审批与超时任务、超时待审、需要尽快推进，避免流程积压、批量可处理、快捷操作、单条处理、批量处理、审批工作台 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D14 |
| 跨部门协同流程 | /oa/staff-collaboration | 从签约到正式入住、待审批流程、超时 · 请假待审、物资申领、员工移动端提交的领用/补给申请、员工待办、今日到期 · 逾期、工作日报、今日已提交 · 草稿、员工建议、处理中 · 最近提交、体征补录、今日异常 · 最新、入住协同 | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | — |
| 文档中心 | /oa/document | 文件名、档案夹、大小、上传人、上传时间、备注；已勾选 条 | 页头 → 左侧档案夹树 + 右侧筛选/文件列表 → 上传与档案夹编辑弹层。 | — |
| 入住统计 | /stats/check-in | 月份、入住、离院、净增长 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 消费统计 | /stats/consumption | 老人姓名、消费金额 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 老人信息统计 | /stats/elder-info | 维度、项、人数 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 机构月运营详情 | /stats/org/monthly-operation | 机构、月份、入住、离院、营收、已使用床位、总床位、床位使用率(%)；机构营收对比、容量压力机构、机构月运营趋势、机构月运营明细、月份范围：、机构范围：、可视记录： /、记录条数、最高营收机构、最高床位压力 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 老人出入统计 | /stats/org/elder-flow | 月份、入住人数、离院人数 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 床位使用统计 | /stats/org/bed-usage | 该入口主要内容由业务子组件提供，见逐页明细。 | 弹窗/抽屉；信息按模板纵向排列。 | — |
| 月运营收入统计 | /stats/monthly-revenue | 月份、收入金额、标签 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 管理驾驶舱 | /stats/operations | 风险指数、人力指数、后勤指数、转化指数 | 页签；信息按模板纵向排列。 | D20 |
| 经营驾驶舱 | /stats/executive-cockpit | 日期、入住率(%)、在住长者、营收(元)、回款率(%)、告警响应率(%) | 数据表格；表格列/操作由脚本或子组件定义。 | D20 |
| 经营驾驶舱大屏 | /stats/executive-bi-screen | 机构、入住率(%)、在住人数、本月营收(元)、回款率(%)、护理完成率(%)、告警达标率(%)、家属满意度(分)、指标、分类、单位、口径说明；智养云 · 经营驾驶舱、统计月、% | 数据表格 → 弹窗/抽屉 → 图表；表格列/操作由脚本或子组件定义。 | — |
| 敏感数据审计 | /stats/sensitive-access-audit | 时间、操作人、角色、动作、数据类别、对象、字段、用途、结果、IP | 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 安全策略配置 | /stats/security-policy | 开启后，用户名密码校验通过还需短信验证码（发送到员工手机号）才能登录、开启后，长者/家属的身份证、手机号等在接口返回时自动打码（豁免角色可看全文） | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | — |
| 导出审计 | /stats/export-audit | 时间、操作人、角色、模块、范围、用途、行数、状态、导出时间、IP | 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 老人出入报表 | /stats/elder-flow-report | #、日期、事件类型、老人姓名、备注；已选 条 | 数据表格 → 弹窗/抽屉；信息按模板纵向排列。 | — |
| 智能排班工作台 | /ai/schedule | 方案名称、周期、状态、缺口、生成时间、操作 | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 健康风险预测看板 | /ai/risk | 长者、风险类型、评分、等级、评估时间、告警、操作、因子、取值、得分；因子明细、近30天趋势、- | 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |

## 逐页字段、操作和实现依据

每页给出筛选、列表列、弹层字段和动作。脚本列定义按变量名列出：其中可能包含导出列、子表列或非当前场景列，不能把它们相加作为首屏列数。静态文字用于补充指标/分区信息；动态表达式保留原名，便于回查。

### 1. 经营总览 `/portal`

经营总览：入住/服务/回款/风险、收费押金电费、生日、最近操作、收入趋势、健康安全预警、快捷入口与协同待办。

- 导航归属：经营总览。
- 路由角色约束：[["DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉 → 图表；表格列/操作由脚本或子组件定义。
- 关联重复项：D20。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/Portal.vue](/Users/yang/smartcare/admin-web/src/views/Portal.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `quickActions`：新增长者、新增护理记录、处理预警、创建工单、收费登记、数据报表。
- 脚本列 `birthdayColumns`：长者姓名、下次生日、剩余天数、届时年龄、房间 / 床位、操作。
- 表格列绑定：birthdayColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: repeat(4, minmax(0, 1fr))、grid-template-columns: minmax(0, 1.25fr) minmax(360px, 0.75fr)、grid-template-columns: minmax(0, 1.15fr) minmax(0, 0.85fr)、grid-template-columns: repeat(3, minmax(0, 1fr))、height: 1.7、height: 1.2、min-height: 132px、height: 1.6、height: 1、height: 4px、grid-template-columns: minmax(0, 0.82fr) minmax(0, 1.18fr) auto、height: 34px、height: 240px、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：入住率、服务完成、本月回款、账单、押金、护理费统一跟踪、未闭环风险、体检异常、未闭环事故、维修超时工单、入住转化、合同收费、人力排班、家属体验、照护执行 SLA、健康复核、安全事件、后勤保障、咨询到入住、能力评估、护理计划执行、健康用药、家属连接、经营复盘、智能风险预警、AI护理摘要、IoT设备接入、体检异常 · 未闭环事故 · 维修超时工单、待处理事项、在住 / 空床、在住长者 / 空置床位、本月收入 / 欠费、本月收费与回款，可下钻欠费与结算、今日护理任务、护理执行与巡诊跟踪、员工在岗数、排班与人力在岗、设备在线率、待设备遥测接入、高风险长者、重点关注跌倒、离床、异常波动等高风险对象。、体检异常未闭环、聚焦体温、血压、血氧等异常与跟进中记录。、跌倒 / 离床 / 呼叫预警、行为类预警统一收束到处置视线，不再散落在不同模块。、异常生命体征、待处理预警列表、护理记录、计划与执行、待审批事项、行政、财务、人事审批、待处理维修 / 后勤工单、维修、巡检与后勤保障、待回访客户、咨询线索与营销回访、新增长者、新增护理记录、处理预警、创建工单、收费登记、数据报表、长者姓名、下次生日、剩余天数、届时年龄、房间 / 床位、操作、近期生日关怀节奏平稳、房间号、床位号、开放待办、进行中任务、任务推进与跨部门执行、今日日程、个人、部门与协同安排、生日待办、关怀类提醒与后续处理、逾期待办、待我审批、生日关怀待办、协同日程。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：员工入口、先进入个人工作台、打卡、待办、审批、日程和岗位任务都从个人视角开始处理。、管理入口、再看机构经营与风险、院长、主管和管理员适合从经营首页或月报继续下钻。、本月经营闭环、入住、回款、人效与客户转化、风险闭环、护理、健康、后勤与审批 SLA。
- 弹层标题：birthdayModalTitle。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：进入工作台、刷新首页、经营月报、待办中心、我的日程、打印名单、进入完整生日页面。

### 2. 全部功能 `/function-map`

可访问页面的功能地图、分类和搜索；本质为导航页，不维护一套新业务数据。

- 导航归属：全部功能。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/FunctionMap.vue](/Users/yang/smartcare/admin-web/src/views/FunctionMap.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(auto-fill, minmax(280px, 1fr))、height: 1.5。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 3. 我的工作台 `/workbench/overview`

个人工作台：角色提示、日期/打卡、关键指标、待办、快捷入口、公告、日程、协同、消息和部门业务入口。

- 导航归属：工作台 > 我的工作台。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：部门通用首页；信息按模板纵向排列。
- 关联重复项：D20。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/workbench/WorkbenchHome.vue](/Users/yang/smartcare/admin-web/src/views/workbench/WorkbenchHome.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：height: 'auto',   fixedWeekCount: false,   headerToolbar: false,   dayMaxEventRows: 2,   events: calendarRows.value.map((task) => ({     id: String(task.id),     title: task.title,     start: task.startTime ／／ task.endTime,     end: task.endTime ／／ task.startTime,     color: task.urgency === 'EMERGENCY' ? 'var(--danger)' : task.calendarType === 'COLLAB' ? 'var(--warning)' : 'var(--primary)',     classNames: [       selectedDate.value.isSame(task.startTime ／／ task.endTime ／／ '', 'day') ? 'workbench-calendar-event--focus' : '',       task.status === 'DONE' ? 'workbench-calendar-event--done' : ''     ].filter(Boolean)、min-height: 210px、height: 100%、height: 28px、height: 1.65、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 122px、height: 50px、height: 1.45、grid-template-columns: repeat(12, minmax(0, 1fr))、height: 1.25、min-height: 30px、min-height: 43px、height: 16px、min-height: 96px、height: 6px、min-height: 54px、height: 34px、grid-template-columns: 54px minmax(0, 1fr) auto、min-height: 42px、grid-template-columns: repeat(3, minmax(0, 1fr))、min-height: 47px、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr auto、grid-template-columns: minmax(0, 1.35fr) minmax(280px, 0.95fr)、grid-template-columns: 1fr、min-height: 0、min-height: 106px、height: 35px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：护理执行、收款、退款与账单差异、协同事项、先处理审批、对账、排班和跨部门协同、我的护理任务、医护任务与用药提醒、我的工单、维修、巡检与保障任务、今日待办、查看需要处理的事项、超时任务、需要优先处理、风险提醒、健康异常待复核、今日日程、全部、今日到期、已逾期、长者档案、护理记录、健康监测、排班管理、服务工单、物资申领、审批中心、报修维护、当月在岗、请假天数、考勤异常、护理、医务、异常复核、待执行医嘱、今日巡诊、医嘱与用药、我的护理执行、待执行护理任务、健康异常提醒、财务、待审核费用、欠费与对账、收银与收费、票据与交班、我的人事行政、行政人事、人力资源中心、考勤与班组、我的待办、优先处理、开放待办、逾期提醒、我的审批、审批、待审批事项、超时审批、我的客户跟进、市场、待回访客户、营销渠道概览、后勤、待处理工单、逾期工单。
- 页签/分区：我的待办、常用入口、通知公告、今日工作日程、跨部门协同、我的消息、关注、继续处理今天的工作。
- 指标和说明静态文字：·、最近刷新、( )、项进行中的协同任务、待审批、今日日程、逾期待办、：、岗位业务明细、当日安排、项。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：重试、刷新数据、查看全部、全部功能、全部、完整日历、查看流程、更多、查看完整日历、上月、今天、下月、前往全部功能。

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

### 4. 我的待办 `/workbench/todo`

待办总量与状态/来源分类；标题、负责人、截止时间、状态；自动更新、联动、提醒方式和新增处理。

- 导航归属：工作台 > 我的待办。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/oa/Todo.vue](/Users/yang/smartcare/admin-web/src/views/oa/Todo.vue)**

- 筛选/主页面表单：关键字、状态、来源、自动刷新、自动更新周期、自动清理已完成。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：标题、来源、负责人、截止时间、状态。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：标题、来源、负责人、截止时间、状态、待处理、已完成、审批流待办、生日提醒、普通待办、行政事务、审批跟进、营销跟进、长者服务、后勤维护、合同签约、入住评估、入住办理、审批流程、营销线索、文档管理、合同、长者、审批单、文档资料、系统站内、短信提醒、站内 + 短信。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：总待办、已勾选 条、逾期、单条操作、批量操作、其中生日提醒 条、保存后同步到任务日历（自动去重）。
- 弹层标题：待办。
- 弹层表单字段：标题、待办类型、关联模块（可搜索）、关联对象（可搜索）、提醒方式、截止时间、负责人（可搜索）、内容、联动设置、状态。
- 可读操作文案：新增待办、转任务日历、导出CSV、编辑、完成、延后、忽略生日、删除、批量完成、批量延后、批量忽略生日、批量删除、查看逾期待办、查看审批待办、保存策略、立即执行清理、查看来源。

### 5. 我的信息 `/workbench/my-info`

本人资料与账号信息，详见页内个人信息子组件；不是全员档案管理。

- 导航归属：工作台 > 我的信息。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/oa/MyInfo.vue](/Users/yang/smartcare/admin-web/src/views/oa/MyInfo.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.3fr) minmax(320px, 0.9fr)、height: 1.15、height: 1.8、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: repeat(auto-fit, minmax(360px, 1fr))、grid-template-columns: repeat(3, minmax(0, 1fr))、height: 1.6、grid-template-columns: minmax(160px, 1.1fr) repeat(auto-fit, minmax(140px, 1fr))、grid-template-columns: 1fr。
- 页签/分区：把入职、试用期、合同、绩效、报销和电费放到同一条时间线上。、员工关键信息对比。
- 指标和说明静态文字：个人概览、普通员工默认查看自己；管理员可以选择多名员工并行对比，快速发现谁即将转正、谁合同将到期、谁费用压力更高。、当前月份、查看人数、待处理报销、逾期任务、·、入职时间、距离试用期、距离签合同、我的职责任务、逾期 · 本月完成、我的绩效、积分 · 可兑现、我的报销、本月 笔 · 待审、我的电费、最多展示 4 条待处理任务、对比模式、重点看试用期天数、合同签署天数、绩效分、电费与报销额的差异。。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：恢复为我自己。

### 6. 我的考勤与请假 `/workbench/attendance`

个人或授权员工的月度考勤/请假/行政日程；签到签退、迟到早退、外出、异常判定；另含季节规则设置区域。

- 导航归属：工作台 > 我的考勤与请假。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/oa/AttendanceLeave.vue](/Users/yang/smartcare/admin-web/src/views/oa/AttendanceLeave.vue)**

- 筛选/主页面表单：查看员工、月份、自动刷新、快捷操作、冬季上班、冬季下班、夏季上班、夏季下班、迟到宽限、早退宽限、外出超时阈值、迟到判定、早退判定、外出超时判定、缺签退判定。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `dayColumns`：日期、星期、打卡时间、状态、请假信息、午休/外出、行政日程、异常判定。
- 表格列绑定：dayColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.2。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：日期、星期、打卡时间、状态、请假信息、午休/外出、行政日程、异常判定。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：分钟、-、正常、当前员工：、当前状态：、当季考勤时间：、当前状态无需再次打卡。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、重置、上班打卡、下班打卡、打印当前员工、导出月报、填写请假、开始午休、结束午休、开始外出、结束外出、保存规则。

### 7. 我的日程 `/workbench/schedule`

个人、部门、日常与协同日历；标题、时间、紧急程度、状态、协同成员/部门、周期、颜色及冲突策略。

- 导航归属：工作台 > 我的日程。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/oa/Calendar.vue](/Users/yang/smartcare/admin-web/src/views/oa/Calendar.vue)**

- 筛选/主页面表单：日历种类、紧急程度、状态、日期范围。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `items`：元旦、劳动节、国庆节、年终总结日。
- CSS布局声明（含响应式分支及局部图表）：height: 'auto',   headerToolbar: {     left: 'prev,next today',     center: 'title',     right: 'dayGridMonth,dayGridWeek,dayGridDay'、grid-template-columns: minmax(0, 1.6fr) minmax(320px, 0.82fr)、height: 1.6、grid-template-columns: repeat(7, minmax(0, 1fr))、grid-template-columns: repeat(4, minmax(0, 1fr))、height: 12px、height: 10px、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: repeat(3, minmax(0, 1fr))、height: 32px、grid-template-columns: 64px 18px minmax(0, 1fr)、height: 18px、height: calc(100% + 34px)、height: 26px、height: 56px、height: 1.7、min-height: 124px、height: 28px、grid-template-columns: 1fr、min-height: 92px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：进行中、已完成、低、中、高、个人、部门工作、日常计划、协同日历、常规、紧急（红色）、每天、每周、每月、基础办公、行政日常、协同会议、后勤排班、专项检查、提示后可继续、发现冲突即阻止、忽略冲突直接保存、元旦、劳动节、国庆节、年终总结日、个人计划、今日、紧急、节假日、生日提醒、全部视图、恢复完整月历、今天、只看今日安排、本周、聚焦本周节奏、本月、查看整月排布、待完成、只看未完成事项、优先处理高风险、协同、查看跨部门安排、确认将该日程标记为已完成？、确认删除该日程？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：项、总安排、待完成、提醒项、紧急项、当日安排、直接浏览这一天最需要处理的事项、·、这一天目前比较空、可以直接新增个人计划、日常计划或协同安排。、当日日程、负责人：、协同：、这一天还没有安排、目前是空白时段，可以直接从上方快速创建基础办公、日常计划或协同会议。、保存前可先预检，策略生效：。
- 弹层标题：selectedDate ? `${selectedDate.format('YYYY-MM-DD')} 日程详情` : '日程详情'、modalTitle。
- 弹层表单字段：标题、开始时间、结束时间、优先级、日历类型、紧急程度、计划分类、颜色策略、冲突策略、协同部门（可多选）、协同成员、开启周期、频率、间隔、生成次数、备注。
- 可读操作文案：回到今天、任务管理、新增日程、今天、刷新、显示全部、展开详情、给这一天新建日程、新增该日程、基础办公、行政日常、协同会议、编辑、完成、删除、立即预检冲突、定位、查看全部。

### 8. 我的总结 `/workbench/reports`

日/周/月/年总结统一列表：标题、周期、总结人、状态；概述、完成工作、问题风险、下一步。

- 导航归属：工作台 > 我的总结。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D19。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/oa/WorkReport.vue](/Users/yang/smartcare/admin-web/src/views/oa/WorkReport.vue)**

- 筛选/主页面表单：类型、状态、日期范围。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：标题、类型、总结日期、总结人、状态。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：标题、类型、总结日期、总结人、状态、日总结、周总结、月总结、年总结、草稿、已提交。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：form.id ? '编辑总结' : '新增总结'。
- 弹层表单字段：标题、总结类型、总结日期、周期范围、工作概述、已完成工作、问题与风险、下一步计划、总结人、状态。
- 可读操作文案：新增总结、编辑、提交、删除、批量提交、批量删除。

### 9. 待我审批 `/workbench/approvals`

复用审批中心组件；审批类型/角色/状态/视角；申请、当前环节/审批人、金额、意见、轮次和业务扩展表单。

- 导航归属：工作台 > 待我审批。
- 路由角色约束：[["NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D14。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/oa/Approval.vue](/Users/yang/smartcare/admin-web/src/views/oa/Approval.vue)**

- 筛选/主页面表单：关键字、类型、审批角色、状态、视角、自动刷新、高级筛选。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：类型、标题、申请人、当前环节、当前审批人、审批轮次、金额、状态、审批意见、年度请假次数、操作。
- 表格列绑定：previewColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.6fr) minmax(320px, 1fr)、grid-template-columns: 88px minmax(0, 1fr)、height: 1.6、grid-template-columns: repeat(4, minmax(0, 1fr))、grid-template-columns: 1fr、grid-template-columns: repeat(2, minmax(0, 1fr))。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：类型、标题、申请人、当前环节、当前审批人、审批轮次、金额、状态、审批意见、年度请假次数、操作、全部、待审批、已通过、超时、请假、加班、报销、采购、收入证明、银行卡申请、积分兑现金、物资申领、用章申请、换班申请、营销方案审批、部门主管、人事、财务、院长、仓库、行政、归档、已驳回、申请创建、审批通过、审批驳回、流程总耗时、确认删除该审批？、确认通过该审批？、确认批量通过？、确认批量删除？。
- 页签/分区：审批详情、申请说明、审批流程。
- 指标和说明静态文字：审批总单、覆盖当前筛选口径与工作视角、待审批、优先处理待我审批与超时任务、超时待审、需要尽快推进，避免流程积压、批量可处理、快捷操作、单条处理、批量处理、审批工作台、当前视角为 ，优先处理超时待审、催办单和需要你立即决策的事项。、已勾选 条、审批单列表、支持批量审批、时间线查看、驳回后重提和角色责任追踪。、工单号：、申请提交 → → 完成归档。
- 弹层标题：`审批时间线 · ${timelineRecord?.title ／／ ''}`、审批、rejectDialogIds.length > 1 ? `批量驳回（${rejectDialogIds.length} 条）` : '驳回审批'。
- 弹层表单字段：类型、标题、申请人、金额、状态、开始时间、结束时间、请假类型、是否影响排班、交接人、请假证明上传、报销类别、是否有预算、发票张数、采购品类、仓库是否已核库存、是否超预算、物资类型、低库存自动触发采购、证明编号、行政盖章方式、兑换积分、可兑现金额(元)、附件上传、事件证明人（院内）、事件证明人（院外）、补充证明上传、扩展表单(JSON，可选)、备注、驳回原因。
- 可读操作文案：新增审批、导出CSV、编辑、同意、驳回、驳回后重提、查看时间线、删除、批量同意、批量驳回、批量催办、批量删除、查看超时待审、查看待我审批、详情、更多、查看完整时间线 →、查看管理制度、查看制度文档、上传证明、上传附件、上传补充证明。

### 10. 在院服务总览 `/elder/in-hospital-overview`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 在院服务总览。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/elder/resident360/InHospitalOverview.vue](/Users/yang/smartcare/admin-web/src/views/elder/resident360/InHospitalOverview.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：height: 1.65、min-height: 30px。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：立即刷新、返回状态变更中心、查看超时任务。

### 11. 床态全景 `/elder/bed-panorama`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 床态全景。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/elder/resident360/BedPanorama.vue](/Users/yang/smartcare/admin-web/src/views/elder/resident360/BedPanorama.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：头像、姓名、生日、家庭住址、备注、操作。
- CSS布局声明（含响应式分支及局部图表）：min-height: calc(100vh - 128px)、height: 360px、min-height: 620px、min-height: 0、height: 100%、height: 44px、height: 1、grid-template-columns: repeat(5, minmax(0, 1fr))、min-height: 62px、min-height: 48px、height: 10px、height: 9px、max-height: none、height: 8px、grid-template-columns: minmax(240px, 1fr) minmax(360px, 1.5fr) 180px、grid-template-columns: 10px minmax(0, 1fr)、grid-template-areas:     "dot label"     "dot value"、grid-template-columns: 160px minmax(0, 1fr) 190px、grid-template-columns: 80px minmax(0, 1fr)、grid-template-columns: minmax(0, 1fr) auto、grid-template-columns: repeat(2, minmax(0, 1fr))、min-height: 220px、max-height: 250px、grid-template-columns: repeat(4, minmax(0, 1fr))、grid-template-columns: repeat(6, minmax(0, 1fr))、grid-template-columns: minmax(0, 1fr) 330px、grid-template-columns: 28px minmax(0, 1fr)、height: 28px、grid-template-columns: repeat(3, minmax(0, 1fr))、height: 1.6、grid-template-columns: minmax(240px, 1fr) minmax(420px, 1.4fr)、grid-template-columns: 1fr、min-height: 78px、min-height: 520px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：全部、仅空床、仅入住、仅异常、全部风险、高风险、中风险、低风险、总床位、床位占用率、在住长者、高风险长者、待处理提醒、维修床位、空床、在住、异常、维修、当前床态运行平稳、空床位、占床长者、离床观察、需关注长者、维修设备、睡眠稳定、占床床位、AI关注、实时风险提醒、体温、心率、血压、血氧、近次体征快照、高风险与异常提醒、夜间离床观察、待清洁床位、值班协同与应急广播、风险床位、房态视图、重置筛选、床位管理、查看空床、平面房态、办理入住、锁床预定、维修登记、生成清洁提醒、长者中心、状态变更、评估档案、合同票据、床态趋势、夜巡观察、响应效率、在线状态、夜间离床统计、夜巡观察参考、护理响应效率、风险任务闭环、风险热力趋势、守护在线状态、24小时床态趋势、占床与空床节奏。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：长者床态指挥舱、当前焦点、床位调度、空间层级、当前范围、当前动作、流程状态、楼栋 / 楼层、空间导航、间房 / 张床位、间房 · 张床位、/、床位状态、快速过滤、风险筛选、重点照护、运营口径、占床与在住、床态分布、运营概览、AI 指挥建议、优先动作、床位详情、长者口径、当前风险、照护状态、床位与健康摘要、楼栋楼层、24h异常、次、风险来源、最近评估、最近提醒、护理关注、快捷操作、照护动作、选择床位查看长者详情、右侧将展示长者档案、健康状态、体征快照与护理操作入口。、异常提醒、外出 / 夜巡 / 清洁 / 应急通知、条待关注事件。
- 弹层标题：床位详情、`房间详情 · ${selectedRoom?.roomNo ／／ '-'}`。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：‹ 床位详情、全部操作、收起 ›、异常提醒 条、打开长者档案、一键分配床位、查看评估档案、合同与票据、状态变更中心、扫码执行（定位今日任务）、生成提醒并进入任务中心、详情、账单。

### 12. 长者列表 `/elder/list`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 长者列表。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/elder/List.vue](/Users/yang/smartcare/admin-web/src/views/elder/List.vue)**

- 筛选/主页面表单：姓名、身份证、床位号、房间号、护理等级。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：长者 / 床位、状态、护理等级、年龄 / 生日、风险 / 关注、身份证（脱敏）、履约阶段、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height:220px"/>`)   win.document.write(`<div style="margin-top:8px、min-height: 32px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：长者 / 床位、状态、护理等级、年龄 / 生日、风险 / 关注、身份证（脱敏）、履约阶段、操作、在住、试住、意向、外出、外出就医、待退住、已退住、已身故、确认删除长者档案？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：当前列表 位、已启用筛选 项、已选 位、档案操作需要单选一位长者、/、常规、岁。
- 弹层标题：换床、绑定家属、老人二维码、`长者详情 · ${quickDetailRow?.fullName ／／ ''}`、`编辑档案 · ${quickEditRow?.fullName ／／ ''}`。
- 弹层表单字段：选择床位、开始日期、家属、关系、主联系人、姓名、联系电话、护理等级、入住日期、家庭住址、备注。
- 可读操作文案：导出名单、新建长者、全部、详情、编辑、换床、退住申请、绑定家属、打印二维码、删除、取消选择、清空筛选、长者中心、更多、关闭、打开完整档案页、完整编辑页。

### 13. 入住评估 `/elder/assessment/ability/admission`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 评估管理 > 能力评估管理 > 入住评估。
- 路由角色约束：[["MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/assessment/Admission.vue](/Users/yang/smartcare/admin-web/src/views/assessment/Admission.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 14. 评估登记 `/elder/assessment/ability/registration`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 评估管理 > 能力评估管理 > 评估登记。
- 路由角色约束：[["MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/assessment/Registration.vue](/Users/yang/smartcare/admin-web/src/views/assessment/Registration.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 15. 持续评估 `/elder/assessment/ability/continuous`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 评估管理 > 能力评估管理 > 持续评估。
- 路由角色约束：[["MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/assessment/Continuous.vue](/Users/yang/smartcare/admin-web/src/views/assessment/Continuous.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 16. 评估档案 `/elder/assessment/ability/archive`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 评估管理 > 能力评估管理 > 评估档案。
- 路由角色约束：[["MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/assessment/Archive.vue](/Users/yang/smartcare/admin-web/src/views/assessment/Archive.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 17. 其他量表评估 `/elder/assessment/other-scale`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 评估管理 > 其他量表评估。
- 路由角色约束：[["MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/assessment/OtherScale.vue](/Users/yang/smartcare/admin-web/src/views/assessment/OtherScale.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 18. 认知能力评估 `/elder/assessment/cognitive`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 评估管理 > 认知能力评估。
- 路由角色约束：[["MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/assessment/Cognitive.vue](/Users/yang/smartcare/admin-web/src/views/assessment/Cognitive.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 19. 自理能力评估 `/elder/assessment/self-care`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 评估管理 > 自理能力评估。
- 路由角色约束：[["MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/assessment/SelfCare.vue](/Users/yang/smartcare/admin-web/src/views/assessment/SelfCare.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 20. 合同与票据 `/elder/contracts-invoices`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 合同与票据。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D17。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/elder/resident360/ContractsInvoices.vue](/Users/yang/smartcare/admin-web/src/views/elder/resident360/ContractsInvoices.vue)**

- 筛选/主页面表单：长者、资料类型、关键字。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：来源、资料类型、文件名、大小、时间。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：全部、病历资料、医保复印件、户口复印件、合同附件、其他附件、评估报告、来源、资料类型、文件名、大小、时间。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：归档完整度（规则引擎）、%。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：返回状态变更中心、联动核对账单、刷新、导出清单、加载资料、搜索、清空。

### 21. 入住办理 `/elder/admission-processing`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 入住办理。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/elder/Admission.vue](/Users/yang/smartcare/admin-web/src/views/elder/Admission.vue)**

- 筛选/主页面表单：老人姓名、收费开始日期、操作员姓名、合同号（自动回填）、初始余额(押金)、楼栋、楼层、房间、入住方式、床位、备注、合同号、入住状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：老人姓名、合同号、收费开始日期、押金（元）、入住状态、备注、操作员姓名、登记时间。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(5, minmax(0, 1fr))、min-height: 92px、height: 27px、height: 1.5、min-height: 88px、height: 1.1、height: 8px、height: 100%、grid-template-columns: 1fr、min-height: 0、grid-template-columns: auto 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：老人基础信息、家属信息、基础疾病信息、合同信息、身份证/资料附件、筛选结果、在院、请假、离院、有合同号、其他状态、老人姓名、合同号、收费开始日期、押金（元）、入住状态、备注、操作员姓名、登记时间。
- 页签/分区：入住进度一图看清、办理入住。
- 指标和说明静态文字：跨部门协同流程、分床、押金账户、合同信息与二维码一步办理完成。、入住记录筛选统计、更新时间：、入住空间分布占比、人 ·。
- 弹层标题：提交确认。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：返回状态变更中心、查看后勤交接任务、提交入住办理、搜索、清空、导出、•。

### 22. 变更总览 `/elder/status-change/center`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 入住状态变更 > 变更总览。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/elder/resident360/StatusChangeCenter.vue](/Users/yang/smartcare/admin-web/src/views/elder/resident360/StatusChangeCenter.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：min-height: 136px、height: 1、height: 1.5。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：状态监测、任务触发、跨部门处置、闭环完成、OA协同、待办、待审批、进行中、医护处置、护理待办、用药待审、巡检待办、后勤执行、今日任务、维修待办、配送未完成、逾期待办、未闭环事故、护理超时、异常巡检未闭环、事故处理中、维修逾期、打开OA任务中心、打开OA审批中心、打开OA待办中心、打开医护统一任务、打开医护工作台、打开医护服务中心、打开后勤任务中心、打开后勤工作台、打开房态图。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：当前联动待办总量、回执更新时间。
- 弹层标题：activeReceiptTitle。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、外出登记、来访登记、试住登记、外出就医登记、退住申请、死亡登记。

### 23. 外出登记 `/elder/status-change/outing`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 入住状态变更 > 外出登记。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/elder/Outing.vue](/Users/yang/smartcare/admin-web/src/views/elder/Outing.vue)**

- 筛选/主页面表单：老人、状态、计划外出时间。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：老人、外出日期、预计返院、实际返院、外出事由、请假条、状态。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：老人、外出日期、预计返院、实际返院、外出事由、请假条、状态、确认删除外出记录？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：-。
- 弹层标题：新增外出登记。
- 弹层表单字段：老人、外出日期、预计返院时间、陪同人、外出事由、请假条照片、备注。
- 可读操作文案：新增外出、搜索、清空、批准外出申请、返院登记、删除、拍照/上传请假条。

### 24. 来访登记 `/elder/status-change/visit-register`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 入住状态变更 > 来访登记。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/elder/VisitRegister.vue](/Users/yang/smartcare/admin-web/src/views/elder/VisitRegister.vue)**

- 筛选/主页面表单：预约日期、老人姓名、来访状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：来访人、关系、联系电话、老人姓名、居住楼层、房号、来访时间、预约时段、预约码、访客人数、车牌、状态。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.8。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：来访人、关系、联系电话、老人姓名、居住楼层、房号、来访时间、预约时段、预约码、访客人数、车牌、状态、确认取消来访预约？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：editingId ? '编辑来访预约' : '新增来访预约'。
- 弹层表单字段：老人、已绑定家属、来访人姓名、联系电话、与长者关系、来访时间、访客人数、车牌号、备注。
- 可读操作文案：新增来访、搜索、清空、登记到访、打印凭条、编辑、取消预约。

### 25. 退住申请 `/elder/status-change/discharge-apply`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 入住状态变更 > 退住申请。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/elder/DischargeApply.vue](/Users/yang/smartcare/admin-web/src/views/elder/DischargeApply.vue)**

- 筛选/主页面表单：老人姓名、关键词、状态、计划退住日期。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：老人姓名、申请日期、计划退住日期、申请原因、证明附件、后续流程状态、后续流程说明、关联退住单号、审核人、审核备注、审核状态、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：老人姓名、申请日期、计划退住日期、申请原因、证明附件、后续流程状态、后续流程说明、关联退住单号、审核人、审核备注、审核状态、操作、确认删除退住申请？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：-、申请原因、证明附件。
- 弹层标题：新增退住申请、reviewStatus === 'APPROVED' ? '通过申请' : '驳回申请'。
- 弹层表单字段：老人、计划退住日期、审核清单、审核备注。
- 可读操作文案：搜索、清空、导出、新增申请、审核、退住费用审核、退院结算、驳回、删除、更多、上传附件、查看。

### 26. 试住登记 `/elder/status-change/trial-stay`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 入住状态变更 > 试住登记。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/elder/TrialStay.vue](/Users/yang/smartcare/admin-web/src/views/elder/TrialStay.vue)**

- 筛选/主页面表单：老人姓名、关键词、状态、试住开始日期。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：老人姓名、试住开始日期、试住结束日期、来源渠道、试住套餐、意向等级、状态、护理等级、备注。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：老人姓名、试住开始日期、试住结束日期、来源渠道、试住套餐、意向等级、状态、护理等级、备注、确认删除试住登记？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：editId ? '编辑试住登记' : '新增试住登记'。
- 弹层表单字段：老人、开始日期、结束日期、来源渠道、试住套餐、意向等级、状态、护理等级、备注。
- 可读操作文案：导出、新增试住、搜索、清空、编辑、删除。

### 27. 外出就医登记 `/elder/status-change/medical-outing`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 入住状态变更 > 外出就医登记。
- 路由角色约束：[["MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/elder/MedicalOuting.vue](/Users/yang/smartcare/admin-web/src/views/elder/MedicalOuting.vue)**

- 筛选/主页面表单：老人、状态、关键词。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：老人、外出日期、预计返院、实际返院、医院、科室、初步诊断、就医原因、状态。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：老人、外出日期、预计返院、实际返院、医院、科室、初步诊断、就医原因、状态、确认删除外出就医记录？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：新增外出就医登记。
- 弹层表单字段：老人、外出就医日期、预计返院时间、就医医院、就诊科室、初步诊断、陪同人、就医原因、备注。
- 可读操作文案：导出、新增外出就医、搜索、清空、返院登记、删除。

### 28. 死亡登记 `/elder/status-change/death-register`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 入住状态变更 > 死亡登记。
- 路由角色约束：[["MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/elder/DeathRegister.vue](/Users/yang/smartcare/admin-web/src/views/elder/DeathRegister.vue)**

- 筛选/主页面表单：老人、状态、关键词。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：老人、死亡日期、死亡时间、地点、死亡原因、证明编号、上报人、上报时间、作废时间、状态、备注。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：老人、死亡日期、死亡时间、地点、死亡原因、证明编号、上报人、上报时间、作废时间、状态、备注、确认删除死亡登记？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：editingId ? '更正死亡登记' : '新增死亡登记'。
- 弹层表单字段：老人、死亡日期、死亡时间、地点、死亡原因、死亡证明编号、上报人、上报时间、备注。
- 可读操作文案：搜索、清空、导出、新增死亡登记、更正、作废、删除。

### 29. 退院结算 `/elder/status-change/discharge-settlement`

主要展示信息见以下模板分区。

- 导航归属：长者管理 > 入住状态变更 > 退院结算。
- 路由角色约束：本路由未显式声明，仍受统一访问策略约束。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/elder/resident360/DischargeSettlement.vue](/Users/yang/smartcare/admin-web/src/views/elder/resident360/DischargeSettlement.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查看账单、费用明细、确认退院登记。

### 30. 行政中心 `/oa/overview`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 行政中心。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：D20。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/oa/AdminCenter.vue](/Users/yang/smartcare/admin-web/src/views/oa/AdminCenter.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(auto-fit, minmax(200px, 1fr))、height: 1.1。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未完成待办、需跟进的协同事项、审批处理中、待推进的审批单、进行中任务、执行中的行政任务。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/ModuleHub.vue](/Users/yang/smartcare/admin-web/src/views/ModuleHub.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：height: 1.1、height: 1.6、height: 8px、grid-template-columns: repeat(auto-fill, minmax(240px, 1fr))、height: 1.5。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：常用功能。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：个分区、项功能、→。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 31. 家属服务中心 `/oa/family-service`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 家属服务中心。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/operations/FamilyServiceCenter.vue](/Users/yang/smartcare/admin-web/src/views/operations/FamilyServiceCenter.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(5, minmax(0, 1fr))、grid-template-columns: minmax(0, 1.35fr) minmax(340px, 0.65fr)、grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 128px、min-height: 148px、height: 1.6、height: 1.1、height: 1.7、grid-template-columns: repeat(3, minmax(0, 1fr))、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：家属端：、管理端：、·。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、家属账号、充值台账、运行健康、反馈与沟通、探访登记。

### 32. 家属账号与绑定 `/oa/family-users`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 家属账号与绑定。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/elder/Family.vue](/Users/yang/smartcare/admin-web/src/views/elder/Family.vue)**

- 筛选/主页面表单：姓名、电话、关联老人、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：姓名、电话、身份证、关联人数、状态、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(4, minmax(0, 1fr))、min-height: 128px、height: 1.7、height: 1.1、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：姓名、电话、身份证、关联人数、状态、操作、确认解除绑定。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：家属账号数、当前筛选结果下的账号总量、正常账号、可继续绑定老人和接收通知、已绑定老人、按当前页家属账号汇总的绑定人数、待补绑定账号、尚未绑定任何老人的家属账号。
- 弹层标题：绑定老人、userForm.id ? '编辑家属账号' : '新增家属账号'、已绑定老人。
- 弹层表单字段：老人、关系、主联系人、姓名、手机号、身份证号、状态。
- 可读操作文案：刷新、新增家属、搜索、重置、查看绑定、绑定老人、更多、老人详情、沟通、解除绑定。

### 33. 任务管理 `/oa/work-execution/task`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 协同办公 > 任务管理。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 页签 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/oa/Task.vue](/Users/yang/smartcare/admin-web/src/views/oa/Task.vue)**

- 筛选/主页面表单：关键字、状态、自动刷新。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：标题、负责人、开始时间、结束时间、优先级、状态。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 'auto',   events: calendarRows.value.map((task) => ({     id: String(task.id),     title: `${task.title。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：标题、负责人、开始时间、结束时间、优先级、状态、进行中、已完成、低、普通、高、个人日历、工作日历、日常计划、协同日历、常规、紧急、每天、每周、每月、基础办公、行政日常、会议安排、跨部门协同、院长督办。
- 页签/分区：任务列表、日历视图。
- 指标和说明静态文字：单条操作、批量操作、批量完成仅对进行中生效。
- 弹层标题：任务。
- 弹层表单字段：任务标题、任务描述、负责人（可搜索）、优先级、日历类型、紧急程度、任务分类（可搜索）、协同成员（可搜索/可多选）、开始时间、结束时间、周期任务、周期规则、周期间隔、状态。
- 可读操作文案：新增任务、查看日历、导出CSV、编辑、完成、删除、批量完成、批量删除、查看逾期任务、查看高优任务、返回列表、刷新日历。

### 34. 家属反馈与沟通 `/oa/work-execution/family-communication`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 协同办公 > 家属反馈与沟通。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/oa/FamilyCommunication.vue](/Users/yang/smartcare/admin-web/src/views/oa/FamilyCommunication.vue)**

- 筛选/主页面表单：关键字、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：状态、家属、联系方式、沟通内容、提交时间、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.6。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：状态、家属、联系方式、沟通内容、提交时间、操作。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：待处理、处理中、已处理。
- 弹层标题：处理家属反馈。
- 弹层表单字段：处理状态、给家属的处理反馈。
- 可读操作文案：刷新、标记处理中、处理反馈、关闭、处理中、反馈。

### 35. 日总结 `/oa/work-execution/daily-report`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 协同办公 > 日总结。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D19。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/oa/DailyReport.vue](/Users/yang/smartcare/admin-web/src/views/oa/DailyReport.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/oa/components/WorkReportBoard.vue](/Users/yang/smartcare/admin-web/src/views/oa/components/WorkReportBoard.vue)**

- 筛选/主页面表单：关键字、状态、日期范围。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：标题、总结日期、总结人、状态。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：工作总结、标题、总结日期、总结人、状态、草稿、已提交。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：form.id ? `编辑${title}` : `新增${title}`。
- 弹层表单字段：标题、总结日期、工作概述、已完成工作、问题与风险、下一步计划、总结人、状态。
- 可读操作文案：新增、编辑、提交、删除、批量提交、批量删除。

### 36. 周总结 `/oa/work-execution/weekly-report`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 协同办公 > 周总结。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D19。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/oa/WeeklyReport.vue](/Users/yang/smartcare/admin-web/src/views/oa/WeeklyReport.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/oa/components/WorkReportBoard.vue](/Users/yang/smartcare/admin-web/src/views/oa/components/WorkReportBoard.vue)**

- 筛选/主页面表单：关键字、状态、日期范围。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：标题、总结日期、总结人、状态。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：工作总结、标题、总结日期、总结人、状态、草稿、已提交。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：form.id ? `编辑${title}` : `新增${title}`。
- 弹层表单字段：标题、总结日期、工作概述、已完成工作、问题与风险、下一步计划、总结人、状态。
- 可读操作文案：新增、编辑、提交、删除、批量提交、批量删除。

### 37. 月总结 `/oa/work-execution/monthly-report`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 协同办公 > 月总结。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D19。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/oa/MonthlyReport.vue](/Users/yang/smartcare/admin-web/src/views/oa/MonthlyReport.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/oa/components/WorkReportBoard.vue](/Users/yang/smartcare/admin-web/src/views/oa/components/WorkReportBoard.vue)**

- 筛选/主页面表单：关键字、状态、日期范围。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：标题、总结日期、总结人、状态。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：工作总结、标题、总结日期、总结人、状态、草稿、已提交。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：form.id ? `编辑${title}` : `新增${title}`。
- 弹层表单字段：标题、总结日期、工作概述、已完成工作、问题与风险、下一步计划、总结人、状态。
- 可读操作文案：新增、编辑、提交、删除、批量提交、批量删除。

### 38. 年总结 `/oa/work-execution/yearly-report`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 协同办公 > 年总结。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D19。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/oa/YearlyReport.vue](/Users/yang/smartcare/admin-web/src/views/oa/YearlyReport.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

**承载内容的子组件：[admin-web/src/views/oa/components/WorkReportBoard.vue](/Users/yang/smartcare/admin-web/src/views/oa/components/WorkReportBoard.vue)**

- 筛选/主页面表单：关键字、状态、日期范围。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：标题、总结日期、总结人、状态。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：工作总结、标题、总结日期、总结人、状态、草稿、已提交。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：form.id ? `编辑${title}` : `新增${title}`。
- 弹层表单字段：标题、总结日期、工作概述、已完成工作、问题与风险、下一步计划、总结人、状态。
- 可读操作文案：新增、编辑、提交、删除、批量提交、批量删除。

### 39. 会员生日 `/oa/life/birthday`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 生活服务 > 会员生日。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/life/Birthday.vue](/Users/yang/smartcare/admin-web/src/views/life/Birthday.vue)**

- 筛选/主页面表单：关键词、月份、未来天数、年龄下限。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：老人姓名、出生日期、下次生日、剩余天数、届时年龄、房间号、床位号、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：老人姓名、出生日期、下次生日、剩余天数、届时年龄、房间号、床位号、操作。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：本月清单、未来7天、80+统计、90+统计、导出、打印年度表、长者档案、创建活动、物资准备。

### 40. 房间打扫 `/oa/life/room-cleaning`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 生活服务 > 房间打扫。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/life/RoomCleaning.vue](/Users/yang/smartcare/admin-web/src/views/life/RoomCleaning.vue)**

- 筛选/主页面表单：状态、关键词。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：房间号、保洁员、计划日期、完成时间、状态、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：房间号、保洁员、计划日期、完成时间、状态、操作、待处理、已完成。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：form.id ? '编辑打扫任务' : '新增打扫任务'。
- 弹层表单字段：房间、计划日期、保洁员、状态、备注。
- 可读操作文案：新增打扫任务、编辑、完成、删除。

### 41. 维修管理 `/oa/life/maintenance`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 生活服务 > 维修管理。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/life/Maintenance.vue](/Users/yang/smartcare/admin-web/src/views/life/Maintenance.vue)**

- 筛选/主页面表单：关键词。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：房间号、故障类型、报修人、维修负责人、优先级、状态、总成本(元)、报修时间、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：min-height: 32px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：房间号、故障类型、报修人、维修负责人、优先级、状态、总成本(元)、报修时间、操作、待处理、处理中、已完成、已取消、低、普通、高、确认标记该维修单为已完工？、确认删除报修单？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：form.id ? '编辑报修' : '新增报修'。
- 弹层表单字段：房间、报修人、维修负责人、故障类型、优先级、故障描述、人工成本(元)、物料成本(元)、总成本(元)、状态、备注。
- 可读操作文案：新增报修、全部、逾期未完（ 天+）、接单、完工、编辑、删除。

### 42. 卡务管理 `/oa/card/account`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 一卡通 > 卡务管理。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/card/Account.vue](/Users/yang/smartcare/admin-web/src/views/card/Account.vue)**

- 筛选/主页面表单：状态、关键词。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：老人、卡号、余额、信用额度、状态、挂失、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：老人、卡号、余额、信用额度、状态、挂失、操作、正常、冻结、注销、否、是。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：form.id ? '编辑卡务' : '开卡'。
- 弹层表单字段：老人、卡号、状态、挂失、备注。
- 可读操作文案：开卡、编辑、删除。

### 43. 充值记录 `/oa/card/recharge`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 一卡通 > 充值记录。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D18。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/card/Recharge.vue](/Users/yang/smartcare/admin-web/src/views/card/Recharge.vue)**

- 筛选/主页面表单：关键词。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：时间、老人、卡号、充值金额、余额、备注。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：时间、老人、卡号、充值金额、余额、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：一卡通充值。
- 弹层表单字段：卡账户、充值金额、备注。
- 可读操作文案：发起充值。

### 44. 消费记录 `/oa/card/consume`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 一卡通 > 消费记录。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/card/Consume.vue](/Users/yang/smartcare/admin-web/src/views/card/Consume.vue)**

- 筛选/主页面表单：关键词。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：时间、老人、卡号、消费金额、余额、备注。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：时间、老人、卡号、消费金额、余额、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：一卡通消费。
- 弹层表单字段：卡账户、消费金额、备注。
- 可读操作文案：登记消费。

### 45. 知识库 `/oa/knowledge`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 知识库。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/oa/Knowledge.vue](/Users/yang/smartcare/admin-web/src/views/oa/Knowledge.vue)**

- 筛选/主页面表单：关键词、分类、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：标题、分类、标签、正文摘要、作者、状态、发布时间。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：标题、分类、标签、正文摘要、作者、状态、发布时间、草稿、已发布、已过期、已归档。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条，批量状态流转按当前状态自动过滤。
- 弹层标题：form.id ? '编辑文章' : '新增文章'。
- 弹层表单字段：标题、分类、标签、作者、状态、上传附件（Word/PDF）、过期时间、正文（可选，正文或附件至少一项）、附件地址、附件名称、备注。
- 可读操作文案：新增文章、编辑、发布、设为过期、归档、批量发布、批量过期、批量归档、导出CSV、删除、批量删除、上传 Word / PDF。

### 46. 通知公告 `/oa/notice`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 通知公告。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/oa/Notice.vue](/Users/yang/smartcare/admin-web/src/views/oa/Notice.vue)**

- 筛选/主页面表单：关键词、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：标题、发布人、发布时间、状态。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：标题、发布人、发布时间、状态、草稿、已发布。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：当前列表 条、已勾选 条、当前列表、按当前筛选范围统计、已发布、面向员工可见的正式公告、草稿、仍待编辑或发布、已选草稿、可直接批量发布、已勾选 条，批量发布仅对“草稿”生效。
- 弹层标题：公告。
- 弹层表单字段：标题、内容、状态。
- 可读操作文案：新增公告、编辑、发布、批量发布、导出CSV、删除、批量删除、清空筛选。

### 47. 活动中心 `/oa/activity-center/plan`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 活动与文化 > 活动中心。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/oa/ActivityPlan.vue](/Users/yang/smartcare/admin-web/src/views/oa/ActivityPlan.vue)**

- 筛选/主页面表单：关键词、活动类型、状态、组织人、日期范围。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：活动、时间、组织人、地点、风险等级、预算、状态、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.3fr) minmax(280px, 0.9fr)、height: 1.2、height: 1.8、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: repeat(4, minmax(0, 1fr))、grid-template-columns: 36px minmax(0, 1fr)、height: 36px、grid-template-columns: repeat(auto-fill, minmax(140px, 1fr))、height: 110px、grid-template-columns: 1fr、grid-template-columns: 1fr 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：生日活动、康复训练、文娱互动、节日活动、家属联动、外出参访、草稿、待提交方案、审批中、已批准、执行中、已完成、已驳回、已取消、低、中、高、全部、活动、时间、组织人、地点、风险等级、预算、状态、操作、护士长审核、财务审批、院长审批。
- 页签/分区：养老活动不再只是一张日历，而是一条可追踪的执行链。。
- 指标和说明静态文字：活动策划、支持活动方案上传、费用审批、执行签到、照片留存与复盘归档，按钮会根据状态自动变化。、待提交方案、审批中、执行中、已完成、基础信息、保存后默认状态为“待提交方案”，后续再进入详情页上传方案并发起审批。、预算信息、· ·、活动类型、组织人、参与对象、人数预估、风险等级、医护支持、预算、当前审批人、审批人：、时间：、意见：、支持活动当天边执行边补照片。。
- 弹层标题：plannerForm.id ? '编辑活动' : '新增活动'、活动方案详情页。
- 弹层表单字段：活动名称、活动类型、活动时间、地点、组织人、参与对象、选择老人（支持标签）、参与标签、人数预估、是否需要医护人员、风险等级、预算金额、费用说明、计划备注、方案详情、上传 Word / PDF 方案、实际人数、签到名单、活动照片、备注记录、实际花费、效果评价、老人反馈、风险情况、活动报告。
- 可读操作文案：新增计划、查看详情、导出 CSV、查看 / 编辑、编辑、开始活动、完成、上传方案附件、保存方案、通过、驳回、上传照片、移除、保存执行信息、生成活动报告、保存复盘。

### 48. 活动记录 `/oa/activity-center/records`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 活动与文化 > 活动记录。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/life/Activity.vue](/Users/yang/smartcare/admin-web/src/views/life/Activity.vue)**

- 筛选/主页面表单：日期范围、关键词、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：日期、活动名称、组织者、地点、状态、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：日期、活动名称、组织者、地点、状态、操作、计划中、已完成、已取消。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：活动。
- 弹层表单字段：活动名称、日期、开始时间、结束时间、地点、组织者、内容、状态、备注。
- 可读操作文案：新增活动、编辑、删除。

### 49. 满意度调查 `/oa/activity-center/survey-manage`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 活动与文化 > 满意度调查。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/survey/Template.vue](/Users/yang/smartcare/admin-web/src/views/survey/Template.vue)**

- 筛选/主页面表单：关键字、对象、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：模板编码、模板名称、对象、状态、发布联动校验、开始日期、结束日期、计分。
- 脚本列 `questionColumns`：编码、题目、题型、必答、权重。
- 表格列绑定：columns、questionColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 280px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：模板编码、模板名称、对象、状态、发布联动校验、开始日期、结束日期、计分、编码、题目、题型、必答、权重、提示。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：模板、配置题目、家属端问卷二维码。
- 弹层表单字段：模板编码、模板名称、适用对象、状态、开始日期、结束日期、匿名、计分、总分、描述、问卷内容。
- 可读操作文案：新增模板、查询、重置、编辑、配置题目、二维码、发布校验、家属端预览、行政统计、绩效榜、发布、停用、批量发布、批量停用、删除、批量删除、搜索、打开链接。

### 50. 调查统计 `/oa/activity-center/survey-stats`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 活动与文化 > 调查统计。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/survey/Stats.vue](/Users/yang/smartcare/admin-web/src/views/survey/Stats.vue)**

- 筛选/主页面表单：模板、日期。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：提交ID、模板ID、对象类型、对象ID、提交人、得分、提交时间。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(5, minmax(0, 1fr))。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：提交ID、模板ID、对象类型、对象ID、提交人、得分、提交时间。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、重置。

### 51. 审批中心 `/oa/approval`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 审批中心。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D14。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/oa/Approval.vue](/Users/yang/smartcare/admin-web/src/views/oa/Approval.vue)**

- 筛选/主页面表单：关键字、类型、审批角色、状态、视角、自动刷新、高级筛选。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：类型、标题、申请人、当前环节、当前审批人、审批轮次、金额、状态、审批意见、年度请假次数、操作。
- 表格列绑定：previewColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.6fr) minmax(320px, 1fr)、grid-template-columns: 88px minmax(0, 1fr)、height: 1.6、grid-template-columns: repeat(4, minmax(0, 1fr))、grid-template-columns: 1fr、grid-template-columns: repeat(2, minmax(0, 1fr))。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：类型、标题、申请人、当前环节、当前审批人、审批轮次、金额、状态、审批意见、年度请假次数、操作、全部、待审批、已通过、超时、请假、加班、报销、采购、收入证明、银行卡申请、积分兑现金、物资申领、用章申请、换班申请、营销方案审批、部门主管、人事、财务、院长、仓库、行政、归档、已驳回、申请创建、审批通过、审批驳回、流程总耗时、确认删除该审批？、确认通过该审批？、确认批量通过？、确认批量删除？。
- 页签/分区：审批详情、申请说明、审批流程。
- 指标和说明静态文字：审批总单、覆盖当前筛选口径与工作视角、待审批、优先处理待我审批与超时任务、超时待审、需要尽快推进，避免流程积压、批量可处理、快捷操作、单条处理、批量处理、审批工作台、当前视角为 ，优先处理超时待审、催办单和需要你立即决策的事项。、已勾选 条、审批单列表、支持批量审批、时间线查看、驳回后重提和角色责任追踪。、工单号：、申请提交 → → 完成归档。
- 弹层标题：`审批时间线 · ${timelineRecord?.title ／／ ''}`、审批、rejectDialogIds.length > 1 ? `批量驳回（${rejectDialogIds.length} 条）` : '驳回审批'。
- 弹层表单字段：类型、标题、申请人、金额、状态、开始时间、结束时间、请假类型、是否影响排班、交接人、请假证明上传、报销类别、是否有预算、发票张数、采购品类、仓库是否已核库存、是否超预算、物资类型、低库存自动触发采购、证明编号、行政盖章方式、兑换积分、可兑现金额(元)、附件上传、事件证明人（院内）、事件证明人（院外）、补充证明上传、扩展表单(JSON，可选)、备注、驳回原因。
- 可读操作文案：新增审批、导出CSV、编辑、同意、驳回、驳回后重提、查看时间线、删除、批量同意、批量驳回、批量催办、批量删除、查看超时待审、查看待我审批、详情、更多、查看完整时间线 →、查看管理制度、查看制度文档、上传证明、上传附件、上传补充证明。

### 52. 跨部门协同流程 `/oa/staff-collaboration`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 跨部门协同流程。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/oa/StaffCollaborationCenter.vue](/Users/yang/smartcare/admin-web/src/views/oa/StaffCollaborationCenter.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(7, minmax(0, 1fr))、height: 28px、grid-template-columns: repeat(4, minmax(0, 1fr))、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: repeat(6, minmax(0, 1fr))、min-height: 138px、height: 1.7、height: 1.1、grid-template-columns: repeat(3, minmax(0, 1fr))、height: 1.6、min-height: 124px、grid-template-columns: 1fr。
- 页签/分区：从签约到正式入住。
- 指标和说明静态文字：待审批流程、超时 · 请假待审、物资申领、员工移动端提交的领用/补给申请、员工待办、今日到期 · 逾期、工作日报、今日已提交 · 草稿、员工建议、处理中 · 最近提交、体征补录、今日异常 · 最新、入住协同、选择具体长者后，可在入住办理页查看实时节点、负责人和处理状态。、· ·、· 截止、·、今日补录记录、待关注异常体征、正常记录、优先处理今日异常体征、把员工现场补录的异常血压、血氧、体温集中到健康数据页继续跟进。、查看今日全部体征补录、快速核对现场回执是否已经同步到后台并形成台账。、处理请假调班、回到审批中心继续审核员工请假、调班和跨班次申请。、处理物资申领、衔接仓库审核、出库确认和低库存补货闭环。、查看考勤异常、进入考勤与请假联动页，继续核对迟到、早退、外出和请假状态。当前视角异常 次。、清理待办积压、统一处理员工端同步过来的待办、审批提醒和值班提醒。、处理员工建议、集中跟进流程优化、设备物资、护理协同和培训排班建议。、处理异常体征、把员工补录发现的异常生命体征转入健康数据台账，继续研判、留痕和复核。。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、员工移动端中心、现场台账、审批中心、进入入住办理 →、请假、调班、物资申领、待办中心、工作总结、员工补录、今日异常、员工建议中心、反馈处理台、查看现场回执。

### 53. 文档中心 `/oa/document`

主要展示信息见以下模板分区。

- 导航归属：行政管理 > 文档中心。
- 路由角色约束：[["STAFF", "HR_EMPLOYEE", "HR_MINISTER", "MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "FINANCE_EMPLOYEE", "FINANCE_MINISTER", "LOGISTICS_EMPLOYEE", "LOGISTICS_MINISTER", "MARKETING_EMPLOYEE", "MARKETING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：页头 → 左侧档案夹树 + 右侧筛选/文件列表 → 上传与档案夹编辑弹层。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/administrationRoutes.ts](/Users/yang/smartcare/admin-web/src/router/administrationRoutes.ts)。

**页面：[admin-web/src/views/oa/Document.vue](/Users/yang/smartcare/admin-web/src/views/oa/Document.vue)**

- 筛选/主页面表单：关键词、文件夹可见性、区域、当前档案夹。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：文件名、档案夹、大小、上传人、上传时间、备注。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：min-height: 680px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：长者入住合规档案、医疗护理记录档案、事故与应急档案、监管与经营报表、权限与操作日志、文件名、档案夹、大小、上传人、上传时间、备注、全部文档、确认删除档案夹？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：文档、folderForm.id ? '编辑档案夹' : '新增档案夹'。
- 弹层表单字段：上传文件（Word/PDF）、文件名、档案夹、上传人、文件地址、大小、备注、档案夹名称、上级档案夹、排序、状态、可见性、区域编码。
- 可读操作文案：刷新、新增、重命名、删除、新增文档、编辑、导出CSV、批量删除、新建档案夹、补齐、新增档案夹、上传 Word / PDF。

### 54. 入住统计 `/stats/check-in`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 入住统计。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/stats/CheckInStats.vue](/Users/yang/smartcare/admin-web/src/views/stats/CheckInStats.vue)**

- 筛选/主页面表单：起始月份、结束月份、机构ID、月份筛选、打印备注、快捷区间。
- 模板直接声明的表格列：月份、入住、离院、净增长。
- CSS布局声明（含响应式分支及局部图表）：height: 320px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：月份、入住、离院、净增长、净增长目标、离院/入住比上限(%)、当前在院目标、当前没有命中的月份数据、净增长走势、入住量观察、离院/入住比、当前在院达成、查看出入报表、查看老人画像、返回看板、当前在院。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：打印列设置。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、导出汇总、更多 ▾、近3月、近6月、本月。

### 55. 消费统计 `/stats/consumption`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 消费统计。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/stats/ConsumptionStats.vue](/Users/yang/smartcare/admin-web/src/views/stats/ConsumptionStats.vue)**

- 筛选/主页面表单：起始月份、结束月份、机构ID、院内老人、打印备注、快捷区间。
- 模板直接声明的表格列：老人姓名、消费金额。
- CSS布局声明（含响应式分支及局部图表）：height: 320px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：老人姓名、消费金额、总消费目标(元)、账单消费占比目标(%)、Top1 消费提醒线(元)、当前时间范围内暂无消费数据、总消费走势、账单消费占比、账单与商城结构、Top1 消费提醒、查看收入统计、查看老人画像、返回看板、总消费、账单占比、Top10 排行、账单消费、商城消费。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：打印列设置。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、导出统计、更多 ▾、近3月、近6月、本月。

### 56. 老人信息统计 `/stats/elder-info`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 老人信息统计。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/stats/ElderInfoStats.vue](/Users/yang/smartcare/admin-web/src/views/stats/ElderInfoStats.vue)**

- 筛选/主页面表单：机构ID、打印维度、对象筛选、打印备注。
- 模板直接声明的表格列：维度、项、人数。
- CSS布局声明（含响应式分支及局部图表）：height: 280px、min-height: 128px、height: 1.6。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：维度、项、人数、在院人数目标、离院人数关注线、高龄占比提醒线(%)、当前维度下没有画像结果、老人总数、在院达成、离院关注、高龄结构、查看入住统计、查看出入报表、返回看板、高龄占比、高龄分群、重度护理分群、女性老人分群、在院活跃分群、老人信息统计。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：打印列设置。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、导出汇总、更多 ▾。

### 57. 机构月运营详情 `/stats/org/monthly-operation`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 机构统计 > 机构月运营详情。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/stats/OrgMonthlyOperation.vue](/Users/yang/smartcare/admin-web/src/views/stats/OrgMonthlyOperation.vue)**

- 筛选/主页面表单：起始月份、结束月份、机构ID、机构名称、打印备注。
- 模板直接声明的表格列：机构、月份、入住、离院、营收、已使用床位、总床位、床位使用率(%)。
- CSS布局声明（含响应式分支及局部图表）：height: 320px、grid-template-columns: repeat(3, minmax(0, 1fr))、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：机构、月份、入住、离院、营收、已使用床位、总床位、床位使用率(%)、月营收目标(元)、床位使用率目标(%)、当前筛选下没有机构运营数据、营收走势、床位使用率、入住动能、机构样本、查看收入趋势、查看床位使用、查看机构出入。
- 页签/分区：机构营收对比、容量压力机构、机构月运营趋势、机构月运营明细。
- 指标和说明静态文字：月份范围：、机构范围：、可视记录： /、记录条数、最高营收机构、最高床位压力。
- 弹层标题：打印列设置。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、导出报表、更多 ▾。

### 58. 老人出入统计 `/stats/org/elder-flow`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 机构统计 > 老人出入统计。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/stats/OrgElderFlow.vue](/Users/yang/smartcare/admin-web/src/views/stats/OrgElderFlow.vue)**

- 筛选/主页面表单：起始月份、结束月份、机构ID、月份筛选、打印备注、快捷区间。
- 模板直接声明的表格列：月份、入住人数、离院人数。
- CSS布局声明（含响应式分支及局部图表）：height: 320px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：月份、入住人数、离院人数、月入住目标、离院占比关注线(%)、当前时间范围没有出入趋势数据、最新月入住、最新月离院、观察窗口、累计出入平衡、查看出入明细、查看入住统计、返回看板、入住、离院。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：打印列设置。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、导出报表、更多 ▾、近3月、近6月、本月。

### 59. 床位使用统计 `/stats/org/bed-usage`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 机构统计 > 床位使用统计。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"]]。
- 场景参数：无路由 props。
- 排版：弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/stats/OrgBedUsage.vue](/Users/yang/smartcare/admin-web/src/views/stats/OrgBedUsage.vue)**

- 筛选/主页面表单：机构ID、打印对象、打印备注。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：height: 360px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：总床位、已使用床位、空闲床位、维护床位、使用率(%)、空闲率(%)、床位使用率上限(%)、空闲率目标(%)、维护床位提醒线、床位使用率、空闲率、容量概览、查看出入报表、查看机构经营、返回看板、已使用、空闲、维护、床位使用统计。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：打印列设置。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、导出汇总、更多 ▾。

### 60. 月运营收入统计 `/stats/monthly-revenue`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 月运营收入统计。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/stats/MonthlyRevenueStats.vue](/Users/yang/smartcare/admin-web/src/views/stats/MonthlyRevenueStats.vue)**

- 筛选/主页面表单：起始月份、结束月份、机构ID、打印备注。
- 模板直接声明的表格列：月份、收入金额、标签。
- CSS布局声明（含响应式分支及局部图表）：height: 320px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：期间总收入目标(元)、月均收入目标(元)、最近环比底线(%)、当前窗口没有收入样本、期间总收入、月均收入、最近环比、最新月份收入、查看机构经营、查看消费结构、返回看板、收入、月运营收入统计、月份、收入金额。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：metricTitle。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出趋势、打印报告、重置、刷新、查看明细。

### 61. 管理驾驶舱 `/stats/operations`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 管理驾驶舱。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"]]。
- 场景参数：无路由 props。
- 排版：页签；信息按模板纵向排列。
- 关联重复项：D20。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/operations/OperationsDashboard.vue](/Users/yang/smartcare/admin-web/src/views/operations/OperationsDashboard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：min-height: 120px、height: 1.2、height: 1.4、height: 1.6、grid-template-columns: repeat(auto-fit, minmax(180px, 1fr))、min-height: 106px、height: 1、height: 100%、grid-template-columns: repeat(auto-fit, minmax(240px, 1fr))、min-height: 112px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：床位收费、床态、合同、账单、欠费、服务质量、安全风险、风险指数与处置动作、人力排班、班次覆盖与岗位负荷、营销转化、线索、参观、评估、签约。
- 页签/分区：总览、服务质量、安全风险、人力排班、后勤保障、营销转化、家属服务、智能化。
- 指标和说明静态文字：风险指数、人力指数、后勤指数、转化指数。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、查看模块。

### 62. 经营驾驶舱 `/stats/executive-cockpit`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 经营驾驶舱。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"], ["DIRECTOR", "SYS_ADMIN", "ADMIN", "MEDICAL_MINISTER", "NURSING_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "MARKETING_MINISTER", "HR_MINISTER"]]。
- 场景参数：无路由 props。
- 排版：数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D20。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/cockpit/ExecutiveCockpit.vue](/Users/yang/smartcare/admin-web/src/views/cockpit/ExecutiveCockpit.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：日期、入住率(%)、在住长者、营收(元)、回款率(%)、告警响应率(%)。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：日期、入住率(%)、在住长者、营收(元)、回款率(%)、告警响应率(%)。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：重算今日、刷新。

### 63. 经营驾驶舱大屏 `/stats/executive-bi-screen`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 经营驾驶舱大屏。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"], ["DIRECTOR", "SYS_ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉 → 图表；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/cockpit/ExecutiveBiScreen.vue](/Users/yang/smartcare/admin-web/src/views/cockpit/ExecutiveBiScreen.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `compareColumns`：机构、入住率(%)、在住人数、本月营收(元)、回款率(%)、护理完成率(%)、告警达标率(%)、家属满意度(分)。
- 脚本列 `definitionColumns`：指标、分类、单位、口径说明。
- 表格列绑定：compareColumns、definitionColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：min-height: 100%、grid-template-columns: repeat(auto-fit, minmax(200px, 1fr))、grid-template-columns: repeat(auto-fit, minmax(360px, 1fr))、grid-template-columns: repeat(auto-fit, minmax(300px, 1fr))、height: 12px、height: 280px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：入住率%、人数、入住率(%)、在住人数、元、回款率%、当月营收累计(元)、当日回款(元)、回款率(%)、护理工单完成率(%)、告警响应达标率(%)、机构、本月营收(元)、护理完成率(%)、告警达标率(%)、家属满意度(分)、指标、分类、单位、口径说明。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：智养云 · 经营驾驶舱、统计月、%。
- 弹层标题：指标口径登记。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：重算汇总、刷新、指标口径。

### 64. 敏感数据审计 `/stats/sensitive-access-audit`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 敏感数据审计。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"], ["SYS_ADMIN", "DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/compliance/SensitiveAccessAudit.vue](/Users/yang/smartcare/admin-web/src/views/compliance/SensitiveAccessAudit.vue)**

- 筛选/主页面表单：数据类别、动作、操作人ID、日期。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：时间、操作人、角色、动作、数据类别、对象、字段、用途、结果、IP。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：时间、操作人、角色、动作、数据类别、对象、字段、用途、结果。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、查询、重置。

### 65. 安全策略配置 `/stats/security-policy`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 安全策略配置。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"], ["SYS_ADMIN"]]。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/compliance/SecurityPolicyConfig.vue](/Users/yang/smartcare/admin-web/src/views/compliance/SecurityPolicyConfig.vue)**

- 筛选/主页面表单：管理端双因子登录（2FA）、仅对以下角色启用 2FA（不选 = 全部角色）、密码有效期（天，0 = 不启用）、登录失败锁定次数（0 = 不启用）、锁定时长（分钟）、会话超时（分钟，0 = 使用系统默认）、敏感字段脱敏展示、脱敏豁免角色（可查看全文）、日志留存天数。
- 模板直接声明的表格列：未在该层模板中声明。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：系统管理员 SYS_ADMIN、管理员 ADMIN、院长/管理层 DIRECTOR、医务部长 MEDICAL_MINISTER、护理部长 NURSING_MINISTER、财务部长 FINANCE_MINISTER、后勤部长 LOGISTICS_MINISTER、市场部长 MARKETING_MINISTER、人事部长 HR_MINISTER、医务员工 MEDICAL_EMPLOYEE、护理员工 NURSING_EMPLOYEE、财务员工 FINANCE_EMPLOYEE、后勤员工 LOGISTICS_EMPLOYEE、市场员工 MARKETING_EMPLOYEE、人事员工 HR_EMPLOYEE。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：开启后，用户名密码校验通过还需短信验证码（发送到员工手机号）才能登录、开启后，长者/家属的身份证、手机号等在接口返回时自动打码（豁免角色可看全文）。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：保存策略。

### 66. 导出审计 `/stats/export-audit`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 导出审计。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"], ["SYS_ADMIN", "DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/compliance/ExportAudit.vue](/Users/yang/smartcare/admin-web/src/views/compliance/ExportAudit.vue)**

- 筛选/主页面表单：模块、状态、日期。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：时间、操作人、角色、模块、范围、用途、行数、状态、导出时间、IP。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：时间、操作人、角色、模块、范围、用途、行数、状态、导出时间。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、查询、重置。

### 67. 老人出入报表 `/stats/elder-flow-report`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 老人出入报表。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/stats/ElderFlowReport.vue](/Users/yang/smartcare/admin-web/src/views/stats/ElderFlowReport.vue)**

- 筛选/主页面表单：开始日期、结束日期、事件类型、机构ID、老人姓名、院内老人、快捷区间。
- 模板直接声明的表格列：#、日期、事件类型、老人姓名、备注。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：日期、事件类型、老人姓名、备注、匹配记录目标、离院占比关注线(%)、当前筛选下没有出入院记录、匹配总记录、入院记录、离院记录、当前页加载、查看入住统计、查看老人画像、查看机构出入趋势、姓名关键字、离院占比、批量操作、打印确认。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已选 条。
- 弹层标题：打印列设置。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：查询、导出报表、更多 ▾、近7天、近30天、近90天、全选本页、勾选全部入院、勾选全部离院、清空勾选、批量打印勾选、批量导出勾选。

### 68. 智能排班工作台 `/ai/schedule`

主要展示信息见以下模板分区。

- 导航归属：AI 智能提效 > 智能排班工作台。
- 路由角色约束：[["MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["NURSING_MINISTER", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/ai/SmartScheduleWorkbench.vue](/Users/yang/smartcare/admin-web/src/views/ai/SmartScheduleWorkbench.vue)**

- 筛选/主页面表单：方案名称、排班周期、班次（不选=全部启用班次）、参与员工（不选=全机构在职员工）、备注。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `proposalColumns`：方案名称、周期、状态、缺口、生成时间、操作。
- 表格列绑定：proposalColumns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：方案名称、周期、状态、缺口、生成时间、操作。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：调整班次、排班约束配置。
- 弹层表单字段：日期、员工、班次、每人每周最大工时、连续上班天数上限、夜班后强制休一天、避开已批准请假、工时均衡权重、夜班公平权重、班次资质要求 JSON（{"班次编码":["角色编码"]}）。
- 可读操作文案：生成排班方案、排班约束配置、刷新方案列表、一键采纳、移除该班次、取消、保存调整。

### 69. 健康风险预测看板 `/ai/risk`

主要展示信息见以下模板分区。

- 导航归属：AI 智能提效 > 健康风险预测看板。
- 路由角色约束：[["MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["MEDICAL_EMPLOYEE", "MEDICAL_MINISTER", "NURSING_EMPLOYEE", "NURSING_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/ai/RiskPredictionBoard.vue](/Users/yang/smartcare/admin-web/src/views/ai/RiskPredictionBoard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：长者、风险类型、评分、等级、评估时间、告警、操作。
- 脚本列 `factorColumns`：因子、取值、得分。
- 表格列绑定：columns、factorColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 120px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：跌倒风险、压疮风险、再入院风险、长者、风险类型、评分、等级、评估时间、告警、操作、因子、取值、得分。
- 页签/分区：因子明细、近30天趋势。
- 指标和说明静态文字：-。
- 弹层标题：drawerTitle、风险评分模型配置。
- 弹层表单字段：启用、规则 JSON。
- 可读操作文案：全员重算、评分模型配置、保存该模型。

## 相关重复项

### D14 待我审批 / 审批中心

- 判断：共享审批组件，权限入口不同。
- 依据：复用 Approval；个人入口有管理角色约束，OA父级广泛开放，具体操作还受页内权限。
- 后续整理方向：统一审批业务入口，保留本人/部门/全部等授权视角。

### D17 合同附件 / 长者合同与票据

- 判断：同资料不同职责。
- 依据：市场负责合同附件办理；长者侧为资料与合同信息查询。
- 后续整理方向：保留读写职责差异，统一资料来源与名称。

### D18 预存充值 / 账户流水 / 一卡通充值

- 判断：相似金额概念，暂不判同一账本。
- 依据：分别可能为长者资金账户和卡账户；仅界面字段相似不足以合并。
- 后续整理方向：先核对资金归属、交易关联和会计口径，再决定收敛。

### D19 日周月年总结 / 我的总结

- 判断：同类工作按周期拆入口。
- 依据：个人总结已支持四类周期；各独立周期页可能保留管理范围差异。
- 后续整理方向：以统一总结中心的周期与范围筛选替代重复菜单。

### D20 多个首页/驾驶舱

- 判断：导航和指标层层重复。
- 依据：总览、个人、部门、行政、驾驶舱重复承担入口和摘要；口径及受众并非完全一样。
- 后续整理方向：明确每类首页唯一职责，摘要点击下钻而非再次进入导航。
