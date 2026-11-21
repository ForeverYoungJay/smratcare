# 人事部：功能、页面信息与排版盘点

日期：2026-11-15。基于当前工作区源码，未改业务页面。

## 阅读口径

“页面入口”按有页面组件的非隐藏路由统计；同一组件可承载不同场景。隐藏兼容入口见兼容清单。以下字段为源码声明的可显示信息，并非同一账号、同一状态下同时出现；弹层内容与默认列表分开记录。动态列、权限、数据状态仍以运行时为准。

本分册记录 36 个页面入口。

## 页面总表

| 页面 | 地址 | 功能及主要信息 | 排版结构 | 重复关联 |
|---|---|---|---|---|
| 行政人事部工作台 | /hr/overview | 普通员工：通知公告、制度文档、我的考勤入口；管理者：待批请假、考勤异常、合同临期、在职人数。下方还有核心流程、最近使用与全部功能目录。 | 山景页头 → 部门指标 → 核心流程 → 最近使用 → 分类功能目录；目录默认开启。 | D20 |
| 招聘办理台 | /hr/recruitment/needs | 统一招聘办理台，scene 切换需求、岗位、候选人、面试、录用/入职、资料及离职；涉及状态、候选人、联系方式、意向/提醒、录用通知、转正、交接等。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 入职向导 | /hr/profile/onboarding | 入职步骤向导：员工主档→账号/角色/领导→劳动合同→附件归档，并提供合同打印入口。主要负责组织办理步骤。 | 办理步骤导航与当前员工信息 → 各步骤办理区域/子页面入口。 | — |
| 员工基本信息 | /hr/profile/basic | 员工主档：工号、姓名、手机、岗位、用工类型、生日、入离职、劳动合同、资质/证书、紧急联系人、社保及流程；抽屉含账号建档与有效权限查看。 | 筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | D15 |
| 劳动合同管理 | /hr/profile/contracts | 员工劳动合同号、类型、状态、起止日期、剩余天数、附件；维护合同。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D15 |
| 账号与领导设置 | /hr/profile/account-access | 账号、职工号、姓名、部门、角色、直接/间接领导、监管状态、手机、锁定状态；密码设置与角色分配。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 合同模板库 | /hr/profile/contract-templates | 合同模板名称、上传人、上传时间、大小、链接；上传和维护模板。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 合同打印 | /hr/profile/contract-print | 合同号、员工号/姓名/手机/岗位、合同类型/状态、开始与到期日期；打印。 | 筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。 | D15 |
| 证书上传 | /hr/profile/certificates | 员工证书名称/编号、颁发机构、颁发日、到期日、剩余天数；新增证书。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D05 |
| 档案附件 | /hr/profile/attachments | 档案附件名称、上传人、时间、文件大小、链接、备注；上传归档。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 合同到期提醒 | /hr/profile/contract-reminders | 合同临期清单：合同号、员工、岗位、类型/状态、有效期、剩余天数；预警天数及包含过期筛选。 | 筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。 | D15 |
| 社保到期/未办理 | /hr/profile/social-security-reminders | 入职日期、社保状态、办理流程、企业购买、月费用、开始日期、最近记账月、提醒天数/日期；发起企业社保申请与办理确认。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 排班管理 | /hr/attendance/schemes | 行政值班方案名称、日期范围、时段、每日人数、人员、节日主值班人；生成、导入和查看排班记录。 | 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 班组设置 | /hr/attendance/groups | 直接复用护理“护工小组”页：小组名称、组长、成员、启停、备注及成员资料/打印。人事菜单名“班组设置”与页内标题不一致。 | 弹窗/抽屉；信息按模板纵向排列。 | — |
| 排班日历 | /hr/attendance/calendar | 直接复用护理排班页：员工、日期、班次、起止、来源方案、状态；新增/编辑排班、换班申请与确认。 | 筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 请假审批 | /hr/attendance/leave-approval | 统一考勤审批场景：关键字/状态；类型、场景、申请人、起止、状态、备注；请假/换班等通过 scene 切换。 | 筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 考勤记录 | /hr/attendance/records | 员工签到/签退、状态、异常与核验；关键字、日期、状态筛选。 | 筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 考勤异常 | /hr/attendance/abnormal | 迟到、早退、缺卡、缺勤等异常；签到/签退、核验状态、核验备注及核验操作。 | 筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 门禁对接 | /hr/attendance/access-control | 员工签到、签退、考勤状态和门禁结果；关键词、状态和日期筛选。 | 筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 一卡通数据对接 | /hr/attendance/card-sync | 实际展示长者、卡号、卡状态、挂失、开卡时间、最近充值及同步状态，业务对象并非员工。 | 筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 规章制度库 | /hr/compliance/policies | 规章制度文档名称、目录、上传人、更新时间、链接；上传制度、备注和维护。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 制度更新预警 | /hr/compliance/policy-alerts | 制度名称、目录、上传人、上传时间、未更新天数、预警等级；超期阈值。 | 筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 费用报销台 | /hr/expense/records | 费用报销列表：标题、申请人、金额、状态、申请时间区间、备注；scene 区分培训报销、补贴和工资补贴等。 | 筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 员工宿舍 | /hr/expense/dormitory | 员工住宿台账和楼栋/房间基础配置；部门、在离职、床位占用/冲突、电表、用餐方案；编辑和批量生成房间。 | 筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | D16 |
| 宿舍房态图 | /hr/expense/dormitory-map | 同一住宿信息的房态图视图：楼栋、楼层、房间、容量、入住/占床；含员工住宿和用餐方案编辑抽屉。 | 页头 → 多条件筛选 → 楼栋/楼层房态卡片 → 入住/方案编辑抽屉。 | D16 |
| 员工餐费 | /hr/expense/meal-fee | 员工用餐方案、月份、费用金额、财务同步状态；宿舍和用餐方案维护及月度同步。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D16 |
| 员工电费 | /hr/expense/electricity-fee | 宿舍、电表、员工、费用月、金额、财务同步；电费上传预览与宿舍联动编辑。 | 筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | D16 |
| 报销审批流 | /hr/expense/approval-flow | 费用类型、场景、标题、申请人、金额、状态、申请时间、备注；可新建审批单。页面并非纯审批流规则配置。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 培训记录 | /hr/development/records | 培训记录/证书档案双页签；部门员工、年度、课程、讲师、机构、日期、学时、出勤、成绩、测试、考证、证书状态和附件；个人培训档案。 | 筛选卡片 → 数据表格 → 页签 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 证书管理 | /hr/development/certificates | 与“证书上传”完全复用同一页面，无路由场景区别。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D05 |
| 证书到期提醒 | /hr/development/certificate-reminders | 证书临期清单：员工、证书/编号、机构、到期日、剩余天数；预警天数与过期筛选。 | 筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 评分规则配置 | /hr/performance/scoring-rules | 护理模板的基础积分、评分权重、可疑扣减、失败积分、启停；复用积分规则页。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D06 |
| 绩效报表 | /hr/performance/reports | 员工分类/排名/勋章、任务量、完成/失败/可疑、满意度、周期积分、余额、可兑换现金；兑换记录与申请。 | 筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。 | — |
| 奖惩记录 | /hr/performance/reward-punishment | 员工、奖惩类型/级别、标题、发生日、金额、积分、状态；新增编辑与作废。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 积分台账 | /hr/incentive/ledger | 员工积分变动台账：变动类型、积分、余额、来源、时间、备注；手工调整。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 积分规则 | /hr/incentive/rules | 与“评分规则配置”相同 PointsRule，包含护理模板、基础积分、权重和扣减。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | D06 |

## 逐页字段、操作和实现依据

每页给出筛选、列表列、弹层字段和动作。脚本列定义按变量名列出：其中可能包含导出列、子表列或非当前场景列，不能把它们相加作为首屏列数。静态文字用于补充指标/分区信息；动态表达式保留原名，便于回查。

### 1. 行政人事部工作台 `/hr/overview`

普通员工：通知公告、制度文档、我的考勤入口；管理者：待批请假、考勤异常、合同临期、在职人数。下方还有核心流程、最近使用与全部功能目录。

- 导航归属：人力资源 > 行政人事部工作台。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：山景页头 → 部门指标 → 核心流程 → 最近使用 → 分类功能目录；目录默认开启。
- 关联重复项：D20。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrCenter.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrCenter.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：通知公告、了解最新工作安排、制度与文档、查询业务规范、我的考勤、核对个人考勤与请假、请假待审批、需要及时处理、考勤异常、待核对异常记录、合同临期、预警周期内到期、在职人数、当前在岗员工。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

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

### 2. 招聘办理台 `/hr/recruitment/needs`

统一招聘办理台，scene 切换需求、岗位、候选人、面试、录用/入职、资料及离职；涉及状态、候选人、联系方式、意向/提醒、录用通知、转正、交接等。

- 导航归属：人力资源 > 招聘管理 > 招聘办理台。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrRecruitmentNeeds.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrRecruitmentNeeds.vue)**

- 筛选/主页面表单：关键字、状态、用人意向、年度、部门。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `common`：事项标题、状态、申请人、创建时间。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：待处理、进行中、已通过、已驳回、已完成、同意用人、考虑中（提醒）、不考虑、待发放、已发放、已确认、待申请、部门推荐中、HR复核中、负责人审批中、已转正、未通过、员工主动离职、协商解除、辞退、合同到期不续签、事项标题、状态、申请人、创建时间、候选人、联系方式、用人意向、提醒日期、提醒状态、简历、姓名、部门、岗位、到岗时间、录用通知、转正状态、到岗日期、资料清单、离职类型、最后工作日、交接截止、离职原因、需求标题、人数、期望到岗、确认删除。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：-。
- 弹层标题：drawerTitle。
- 弹层表单字段：事项标题、状态、候选人姓名、联系方式、用人意向、提醒日期、简历上传 (Word/PDF)、姓名、部门、岗位、到岗时间、薪资、试用期、工作地点、班次、录用通知状态、资料收集清单、签署文件、系统与权限开通、物资发放签领、带教师傅、试用期目标、转正建议、转正状态、离职类型、离职原因、最后工作日、交接截止、离职报告上传、交接清单、资产回收、权限回收计划、财务结算与证明、归档与风险标记、到岗日期、资料清单（可多选）、备注。
- 可读操作文案：一键建档案夹(年度/部门)、批量进行中、批量完成、批量驳回、导出 CSV、导出 Excel、下载办理单、打印办理单、新增、标准模板填充、编辑、删除、查看简历、上传简历、上传报告、取消、提交。

### 3. 入职向导 `/hr/profile/onboarding`

入职步骤向导：员工主档→账号/角色/领导→劳动合同→附件归档，并提供合同打印入口。主要负责组织办理步骤。

- 导航归属：人力资源 > 员工档案 > 入职向导。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：办理步骤导航与当前员工信息 → 各步骤办理区域/子页面入口。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrOnboardingWizard.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrOnboardingWizard.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr)、height: 220px、height: 1.08、height: 1.8、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 74px minmax(0, 1fr)、height: 74px、height: 1.7、height: 1.9、height: 8px、grid-template-columns: 1fr、height: 58px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：建立员工主档、新增人员 / 录入基础资料 / 生成 staffId、切换员工、账号与监管配置、登录账号 / 角色 / 直接领导 / 间接领导、维护劳动合同、合同类型 / 状态 / 起止日期 / 合同编号、归档附件材料、其他附件 / 补充证明 / 归档资料、合同打印。
- 页签/分区：把新增人员、建档、签合同、上传附件收成一条清晰的路径。。
- 指标和说明静态文字：入职办理流程、我们把分散页面重新编排成 4 个步骤。先建立员工主档，再继续补齐账号与监管、合同、附件；每一步都能直接跳过去并自动打开对应操作。、当前对象、完成进度、%、角色 / 监管、合同、合同附件、份、其他附件、建员工主档、补账号监管、维护合同、上传附件。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：开始新增人员、进入档案中心。

### 4. 员工基本信息 `/hr/profile/basic`

员工主档：工号、姓名、手机、岗位、用工类型、生日、入离职、劳动合同、资质/证书、紧急联系人、社保及流程；抽屉含账号建档与有效权限查看。

- 导航归属：人力资源 > 员工档案 > 员工基本信息。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"title": "员工基本信息", "subTitle": "员工档案中心 / 员工基本信息"}。
- 排版：筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：D15。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/Staff.vue](/Users/yang/smartcare/admin-web/src/views/hr/Staff.vue)**

- 筛选/主页面表单：关键字、部门、角色。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：工号、姓名、生日、手机号、岗位、用工类型、合同编号、合同到期、离职日期、状态。
- 脚本列 `effectivePermissionColumns`：角色、来源、页面权限。
- 表格列绑定：columns、effectivePermissionColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：人力资源、工号、姓名、生日、手机号、岗位、用工类型、合同编号、合同到期、离职日期、状态、在职、离职、全职、兼职、外包、固定期限、无固定期限、实习协议、劳务协议、待签署、已生效、续签处理中、已到期、已终止、待办理、办理中、已参保、暂停缴纳、待发起、待院长审核、待财务办理、已驳回、已停缴、角色、来源、页面权限、职工生日台账。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条、暂无角色绑定、当前没有生效页面权限、无页面权限。
- 弹层标题：drawerTitle、员工离职、员工最终生效页面权限。
- 弹层表单字段：建档方式、登录账号、初始密码、工号、姓名、岗位/角色、部门、手机号、账号状态、员工、用工类型、合同编号、合同类型、合同状态、合同开始、合同结束、入职日期、资质等级、证书编号、紧急联系人、紧急联系电话、生日、状态、离职日期、离职原因、社保状态、参保开始日期、提醒天数、企业购买社保、发送院长审核、每月社保费用、社保流程、最近记账月份、社保备注、备注、员工ID。
- 可读操作文案：入职向导、新增人员、编辑档案、查看、生效页面权限、复职、批量复职、打印生日台账、离职、批量离职、重新生成职工号、取消、保存、保存并设置账号、保存并维护合同、保存并上传附件。

### 5. 劳动合同管理 `/hr/profile/contracts`

员工劳动合同号、类型、状态、起止日期、剩余天数、附件；维护合同。

- 导航归属：人力资源 > 员工档案 > 劳动合同管理。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D15。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrProfileContracts.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrProfileContracts.vue)**

- 筛选/主页面表单：关键字、合同状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：工号、员工姓名、手机号、岗位、用工类型、合同编号、合同类型、合同状态、合同开始、合同结束、剩余天数、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：待签署、已生效、续签处理中、已到期、已终止、固定期限、无固定期限、实习协议、劳务协议、工号、员工姓名、手机号、岗位、用工类型、合同编号、合同类型、合同状态、合同开始、合同结束、剩余天数、操作。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：维护劳动合同。
- 弹层表单字段：员工、岗位、合同编号、合同类型、合同状态、合同开始日期、合同结束日期、合同附件。
- 可读操作文案：导出 CSV、导出 Excel、维护合同、上传合同附件。

### 6. 账号与领导设置 `/hr/profile/account-access`

账号、职工号、姓名、部门、角色、直接/间接领导、监管状态、手机、锁定状态；密码设置与角色分配。

- 导航归属：人力资源 > 员工档案 > 账号与领导设置。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：{"title": "账号与领导设置", "subTitle": "人事创建账号、初始化密码与上下级监管关系"}。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/Staff.vue](/Users/yang/smartcare/admin-web/src/views/Staff.vue)**

- 筛选/主页面表单：关键字、部门、角色。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：职工号、账号、姓名、部门、直接领导、间接领导、监管状态、手机号、状态、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(4, minmax(0, 1fr))、grid-template-columns: minmax(0, 1fr)、height: 1.5、grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：员工管理、职工号、账号、姓名、部门、直接领导、间接领导、监管状态、手机号、状态、操作、启用、已锁定、员工账号密码单、登录账号、当前密码、密码更新时间。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：当前员工、监管异常、已锁定、已分配角色、监管异常视图、规则：员工到部长，部长到院长层级、当前来自新增人员流程、继续补充账号与监管链、保存后可回到档案中心继续处理、优先按角色的上级领导角色推荐；未配置时，自动回退为“员工→本部门部长，部长→院长层级”。、暂无可查看密码快照，请先重置密码后再查看。。
- 弹层标题：drawerTitle、分配角色、账号密码。
- 弹层表单字段：form.id ? '重置密码（可选）' : '初始密码'、直接领导（一级监管）、间接领导（二级监管）、快速修复、状态、角色。
- 可读操作文案：导出异常、监管异常、新增员工主档、编辑、推荐修复、查看密码、打印账号密码、分配角色、一键锁定、解除锁定、去基本信息、一键推荐领导、取消、保存、关闭。

### 7. 合同模板库 `/hr/profile/contract-templates`

合同模板名称、上传人、上传时间、大小、链接；上传和维护模板。

- 导航归属：人力资源 > 员工档案 > 合同模板库。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrProfileTemplates.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrProfileTemplates.vue)**

- 筛选/主页面表单：关键字。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：模板名称、上传人、上传时间、大小(B)、链接。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：模板名称、上传人、上传时间、大小(B)、链接。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：-。
- 弹层标题：新增合同模板。
- 弹层表单字段：模板名称、上传模板、文件链接、文件大小(B)、备注。
- 可读操作文案：导出 CSV、导出 Excel、新增模板、上传合同模板、取消、保存。

### 8. 合同打印 `/hr/profile/contract-print`

合同号、员工号/姓名/手机/岗位、合同类型/状态、开始与到期日期；打印。

- 导航归属：人力资源 > 员工档案 > 合同打印。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D15。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrContractPrint.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrContractPrint.vue)**

- 筛选/主页面表单：关键字、合同状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：合同编号、工号、员工姓名、手机号、岗位、合同类型、合同状态、开始日期、到期日期、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：待签署、已生效、续签处理中、已到期、已终止、合同编号、工号、员工姓名、手机号、岗位、合同类型、合同状态、开始日期、到期日期、操作。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出 CSV、导出 Excel、打印。

### 9. 证书上传 `/hr/profile/certificates`

员工证书名称/编号、颁发机构、颁发日、到期日、剩余天数；新增证书。

- 导航归属：人力资源 > 员工档案 > 证书上传。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D05。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrProfileCertificates.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrProfileCertificates.vue)**

- 筛选/主页面表单：关键字、有效期范围。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：员工、证书名称、证书编号、颁发机构、颁发日期、到期日期、剩余天数。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：员工、证书名称、证书编号、颁发机构、颁发日期、到期日期、剩余天数。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：天。
- 弹层标题：新增证书。
- 弹层表单字段：员工、证书名称、证书编号、颁发机构、颁发日期、到期日期、备注。
- 可读操作文案：导出 CSV、导出 Excel、新增证书、取消、保存。

### 10. 档案附件 `/hr/profile/attachments`

档案附件名称、上传人、时间、文件大小、链接、备注；上传归档。

- 导航归属：人力资源 > 员工档案 > 档案附件。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrProfileAttachments.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrProfileAttachments.vue)**

- 筛选/主页面表单：关键字。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：附件名称、上传人、上传时间、大小(B)、链接。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：附件名称、上传人、上传时间、大小(B)、链接。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：-。
- 弹层标题：新增其他附件。
- 弹层表单字段：附件名称、上传文件、附件链接、文件大小(B)、备注。
- 可读操作文案：导出 CSV、导出 Excel、新增附件、上传其他附件、取消、保存。

### 11. 合同到期提醒 `/hr/profile/contract-reminders`

合同临期清单：合同号、员工、岗位、类型/状态、有效期、剩余天数；预警天数及包含过期筛选。

- 导航归属：人力资源 > 员工档案 > 合同到期提醒。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D15。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrContractReminder.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrContractReminder.vue)**

- 筛选/主页面表单：关键字、预警天数、包含已过期。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：合同编号、工号、员工姓名、手机号、岗位、合同类型、合同状态、生效开始、到期日期、剩余天数。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：待签署、已生效、续签处理中、已到期、已终止、合同编号、工号、员工姓名、手机号、岗位、合同类型、合同状态、生效开始、到期日期、剩余天数。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：天。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出 CSV、导出 Excel。

### 12. 社保到期/未办理 `/hr/profile/social-security-reminders`

入职日期、社保状态、办理流程、企业购买、月费用、开始日期、最近记账月、提醒天数/日期；发起企业社保申请与办理确认。

- 导航归属：人力资源 > 员工档案 > 社保到期/未办理。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrSocialSecurityReminder.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrSocialSecurityReminder.vue)**

- 筛选/主页面表单：关键字、提醒范围、社保状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：工号、员工姓名、手机号、岗位、入职日期、社保状态、流程状态、企业购买、月费用、参保开始、最近记账月、提醒天数、提醒日期、提醒范围、剩余天数、备注、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：全部、已到提醒日、7日内将提醒、未办理、已参保、待办理、办理中、暂停缴纳、工号、员工姓名、手机号、岗位、入职日期、社保状态、流程状态、企业购买、月费用、参保开始、最近记账月、提醒天数、提醒日期、提醒范围、剩余天数、备注、操作、确认已完成社保办理？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：天。
- 弹层标题：发起企业社保申请。
- 弹层表单字段：员工、申请企业购买社保、发送院长审核、每月社保费用、社保备注。
- 可读操作文案：生成本月社保记账、导出 CSV、导出 Excel、发起申请、院长通过、办理完成、生成本月记账、驳回。

### 13. 排班管理 `/hr/attendance/schemes`

行政值班方案名称、日期范围、时段、每日人数、人员、节日主值班人；生成、导入和查看排班记录。

- 导航归属：人力资源 > 考勤与班组 > 排班管理。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrAttendanceSchemes.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrAttendanceSchemes.vue)**

- 筛选/主页面表单：方案名称、排班日期范围、值班时段、每日需要人数、参与值班人员、节日日期、节日主要值班人、补充说明。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：日期、值班时段、人数、班次人员、是否节日、主要值班人、备注。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：日期、值班时段、人数、班次人员、是否节日、主要值班人、备注、行政值班排班表、节日。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：至、排班预览。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：添加节日日期、自动生成排班表、保存并实施、加载已实施排班、保存草稿、重置、上传 CSV 排班记录、下载 CSV、打印。

### 14. 班组设置 `/hr/attendance/groups`

直接复用护理“护工小组”页：小组名称、组长、成员、启停、备注及成员资料/打印。人事菜单名“班组设置”与页内标题不一致。

- 导航归属：人力资源 > 考勤与班组 > 班组设置。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：弹窗/抽屉；信息按模板纵向排列。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/care/CaregiverGroups.vue](/Users/yang/smartcare/admin-web/src/views/care/CaregiverGroups.vue)**

- 筛选/主页面表单：关键字、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：height: 68px、height: 1.2、grid-template-columns: repeat(auto-fit, minmax(320px, 1fr))、grid-template-columns: repeat(auto-fit, minmax(132px, 1fr))、height: 42px、height: 54px、min-height: 58px、grid-template-columns: repeat(auto-fit, minmax(280px, 1fr))、height: 66px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：启用、停用、确认删除该护工小组？。
- 页签/分区：弋阳龟峰颐养中心护工小组明细表。
- 指标和说明静态文字：护理班组台账、为班组长准备的现场工具页。这里既能管理编组，也能临时维护护工优点、特长，并按需要生成带照片或电话的打印版台账。、组长：、成员： 人、支持一键维护整组护工的优点与特长。、选择打印内容：。
- 弹层标题：护工小组、profileDrawerTitle、快捷打印 ／ 护工小组明细。
- 弹层表单字段：小组名称、组长、成员、启用状态、备注、优点、特长。
- 可读操作文案：新增小组、快捷打印、查询、重置、编辑小组、一键编辑优点/特长、删除、保存本组信息、生成打印表。

### 15. 排班日历 `/hr/attendance/calendar`

直接复用护理排班页：员工、日期、班次、起止、来源方案、状态；新增/编辑排班、换班申请与确认。

- 导航归属：人力资源 > 考勤与班组 > 排班日历。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/care/ShiftCalendar.vue](/Users/yang/smartcare/admin-web/src/views/care/ShiftCalendar.vue)**

- 筛选/主页面表单：员工、状态、日期范围。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：员工姓名、值班日期、班次、开始、结束、来源方案、状态、操作。
- 脚本列 `swapColumns`：申请人、申请班次、目标员工、目标班次、状态、对方确认、操作。
- 表格列绑定：columns、swapColumns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：员工姓名、值班日期、班次、开始、结束、来源方案、状态、操作、申请人、申请班次、目标员工、目标班次、对方确认、确认删除该排班记录吗？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：form.id ? '编辑排班' : '新增排班'、申请换班。
- 弹层表单字段：员工、员工姓名、值班日期、班次编码、开始时间、结束时间、来源方案、状态、我的班次、对调员工、对调班次、申请说明。
- 可读操作文案：新增排班、刷新换班申请、编辑、申请换班、删除、同意、查看审批、拒绝。

### 16. 请假审批 `/hr/attendance/leave-approval`

统一考勤审批场景：关键字/状态；类型、场景、申请人、起止、状态、备注；请假/换班等通过 scene 切换。

- 导航归属：人力资源 > 考勤与班组 > 请假审批。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrAttendanceApprovalPage.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrAttendanceApprovalPage.vue)**

- 筛选/主页面表单：关键字、审批状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：标题、审批类型、场景、申请人、状态、开始时间、结束时间、创建时间、备注。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：待审批、已通过、已驳回、标题、审批类型、场景、申请人、状态、开始时间、结束时间、创建时间、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出 CSV、导出 Excel。

### 17. 考勤记录 `/hr/attendance/records`

员工签到/签退、状态、异常与核验；关键字、日期、状态筛选。

- 导航归属：人力资源 > 考勤与班组 > 考勤记录。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrAttendanceRecords.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrAttendanceRecords.vue)**

- 筛选/主页面表单：关键字、状态、日期区间。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：员工、签到时间、签退时间、状态、异常、核验。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：员工、签到时间、签退时间、状态、异常、核验。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出 CSV、导出 Excel。

### 18. 考勤异常 `/hr/attendance/abnormal`

迟到、早退、缺卡、缺勤等异常；签到/签退、核验状态、核验备注及核验操作。

- 导航归属：人力资源 > 考勤与班组 > 考勤异常。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrAttendanceAbnormal.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrAttendanceAbnormal.vue)**

- 筛选/主页面表单：员工关键字、异常状态、日期范围。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：员工、签到时间、签退时间、异常状态、核验状态、核验备注、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：迟到、早退、缺卡、缺勤、员工、签到时间、签退时间、异常状态、核验状态、核验备注、操作、员工姓名。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出 CSV、导出 Excel、标记已核验。

### 19. 门禁对接 `/hr/attendance/access-control`

员工签到、签退、考勤状态和门禁结果；关键词、状态和日期筛选。

- 导航归属：人力资源 > 考勤与班组 > 门禁对接。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrAccessControl.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrAccessControl.vue)**

- 筛选/主页面表单：关键字、状态、日期范围。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：员工、签到时间、签退时间、考勤状态、门禁结果。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：员工、签到时间、签退时间、考勤状态、门禁结果。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出 CSV、导出 Excel。

### 20. 一卡通数据对接 `/hr/attendance/card-sync`

实际展示长者、卡号、卡状态、挂失、开卡时间、最近充值及同步状态，业务对象并非员工。

- 导航归属：人力资源 > 考勤与班组 > 一卡通数据对接。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrCardSync.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrCardSync.vue)**

- 筛选/主页面表单：关键字、卡状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：长者、卡号、卡状态、挂失、开卡时间、最近充值、同步状态。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：长者、卡号、卡状态、挂失、开卡时间、最近充值、同步状态。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出 CSV、导出 Excel。

### 21. 规章制度库 `/hr/compliance/policies`

规章制度文档名称、目录、上传人、更新时间、链接；上传制度、备注和维护。

- 导航归属：人力资源 > 制度与合规 > 规章制度库。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrPolicies.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrPolicies.vue)**

- 筛选/主页面表单：关键字、目录。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：文档名称、目录、上传人、更新时间、链接。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：文档名称、目录、上传人、更新时间、链接。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：-。
- 弹层标题：新增规章制度。
- 弹层表单字段：制度名称、所属目录、上传文件、更新时间、备注。
- 可读操作文案：导出 CSV、导出 Excel、新增制度、刷新、上传制度文件、取消、保存。

### 22. 制度更新预警 `/hr/compliance/policy-alerts`

制度名称、目录、上传人、上传时间、未更新天数、预警等级；超期阈值。

- 导航归属：人力资源 > 制度与合规 > 制度更新预警。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrPolicyAlerts.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrPolicyAlerts.vue)**

- 筛选/主页面表单：关键字、超期天数。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：制度文档、目录、上传人、上传时间、未更新天数、预警等级。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：制度文档、目录、上传人、上传时间、未更新天数、预警等级。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出 CSV、导出 Excel。

### 23. 费用报销台 `/hr/expense/records`

费用报销列表：标题、申请人、金额、状态、申请时间区间、备注；scene 区分培训报销、补贴和工资补贴等。

- 导航归属：人力资源 > 薪酬与费用 > 费用报销台。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrExpenseScenePage.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrExpenseScenePage.vue)**

- 筛选/主页面表单：关键字、审批状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：标题、申请人、金额、状态、申请开始时间、申请结束时间、创建时间、备注。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：待审批、已通过、已驳回、标题、申请人、金额、状态、申请开始时间、申请结束时间、创建时间、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出 CSV、导出 Excel。

### 24. 员工宿舍 `/hr/expense/dormitory`

员工住宿台账和楼栋/房间基础配置；部门、在离职、床位占用/冲突、电表、用餐方案；编辑和批量生成房间。

- 导航归属：人力资源 > 薪酬与费用 > 员工宿舍。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：D16。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrDormitoryManagement.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrDormitoryManagement.vue)**

- 筛选/主页面表单：关键字、部门、员工状态、住宿状态、楼栋、房间。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：员工、部门、状态、宿舍 / 电表、用餐方案、备注、更新时间、操作。
- 脚本列 `roomConfigColumns`：宿舍房间、标准床位 / 当前占用、排序号、状态、备注、更新时间、操作。
- 表格列绑定：columns、roomConfigColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.7fr) minmax(320px, 1fr)、height: 1.3、height: 1.7、height: 100%、height: 1.6、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：仅在职、仅离职、已住宿、已分配床位、待分配床位、床位冲突、未住宿、启用、停用、字母楼栋（A栋/B栋）、数字楼栋（1栋/2栋）、员工、部门、状态、宿舍 / 电表、用餐方案、备注、更新时间、操作、宿舍房间、标准床位 / 当前占用、排序号。
- 页签/分区：把员工入住台账和宿舍基础配置放在同一个入口统一维护、楼栋 / 房间基础配置。
- 指标和说明静态文字：宿舍管理台、支持床位分配、电表维护、冲突检查，以及楼栋/楼层/房间标准床位数配置，房态图会直接使用这里的基础数据补齐空床位。、电表：、维护标准床位容量后，宿舍房态图会自动补齐空床位，未住满房间也能显示完整房态。、/、床、已占用 · 空床、是否住宿舍。
- 弹层标题：drawerTitle、roomConfigDrawerTitle、批量生成宿舍房间。
- 弹层表单字段：员工、宿舍楼栋、宿舍房间、床位号、电表编号、方案状态、用餐方案、备注、楼层、房间号、标准床位数、排序号、状态、楼栋命名、楼栋前缀、起始栋号、生成几栋、起始楼层、每栋几层、每层几间、起始房号、房间号分隔符。
- 可读操作文案：新增住宿安排、批量生成房间、宿舍房态图、查看员工餐费、查看员工电费、打开房态图、刷新统计、编辑、餐费、电费、刷新房间配置、查询房间、重置、删除、取消、保存宿舍信息、保存房间配置、开始生成。

### 25. 宿舍房态图 `/hr/expense/dormitory-map`

同一住宿信息的房态图视图：楼栋、楼层、房间、容量、入住/占床；含员工住宿和用餐方案编辑抽屉。

- 导航归属：人力资源 > 薪酬与费用 > 宿舍房态图。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：页头 → 多条件筛选 → 楼栋/楼层房态卡片 → 入住/方案编辑抽屉。
- 关联重复项：D16。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrDormitoryMap.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrDormitoryMap.vue)**

- 筛选/主页面表单：关键字、部门、员工状态、住宿状态、楼栋、房间。
- 模板直接声明的表格列：未在该层模板中声明。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.7fr) minmax(320px, 1fr)、height: 1.3、height: 1.7、grid-template-columns: repeat(auto-fit, minmax(360px, 1fr))、grid-template-columns: 88px minmax(0, 1fr)、grid-template-columns: repeat(auto-fill, minmax(120px, 1fr))、min-height: 72px、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：仅在职、仅离职、已住宿、已分配床位、待分配床位、床位冲突、未住宿、启用、停用。
- 页签/分区：把宿舍基础配置和员工入住信息合并成完整房态图。
- 指标和说明静态文字：宿舍全景、已配置的宿舍房间会完整展示标准床位，未住满的房间自动补齐空床位；待分配、冲突和超配房间也会在同一张图上高亮显示。、点击楼栋和楼层标签可快速聚焦到对应宿舍区域、间、是否住宿舍。
- 弹层标题：drawerTitle。
- 弹层表单字段：员工、宿舍楼栋、宿舍房间、床位号、电表编号、方案状态、用餐方案、备注。
- 可读操作文案：返回宿舍台账、新增住宿安排、清除筛选、取消、保存宿舍信息。

### 26. 员工餐费 `/hr/expense/meal-fee`

员工用餐方案、月份、费用金额、财务同步状态；宿舍和用餐方案维护及月度同步。

- 导航归属：人力资源 > 薪酬与费用 > 员工餐费。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D16。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrExpenseMealFee.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrExpenseMealFee.vue)**

- 筛选/主页面表单：费用月份、关键字、财务同步。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：员工、部门、用餐方案、宿舍房间、月份、金额、财务同步、更新时间、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.6fr) minmax(320px, 1fr)、height: 1.2、grid-template-columns: repeat(3, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：待同步、已同步、员工、部门、用餐方案、宿舍房间、月份、金额、财务同步、更新时间、操作。
- 页签/分区：员工用餐方案、月度扣缴、财务同步放在一页处理。
- 指标和说明静态文字：餐费结算台、按月份生成员工餐费账单，自动汇总早餐/中餐/晚餐方案，支持一键同步财务与打印台账。、本月账单数、已加载餐费记录、本月金额、月度自动扣缴总额、待同步财务、仍需推送财务审批、早餐、中餐、晚餐、宿舍联动。
- 弹层标题：员工用餐与宿舍方案。
- 弹层表单字段：员工、宿舍楼栋、宿舍房间、床位号、电表编号、备注。
- 可读操作文案：维护用餐方案、生成本月餐费、同步财务、一键打印、导出 CSV、导出 Excel、调整方案、同步、取消、保存方案。

### 27. 员工电费 `/hr/expense/electricity-fee`

宿舍、电表、员工、费用月、金额、财务同步；电费上传预览与宿舍联动编辑。

- 导航归属：人力资源 > 薪酬与费用 > 员工电费。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：D16。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrExpenseElectricityFee.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrExpenseElectricityFee.vue)**

- 筛选/主页面表单：费用月份、关键字、财务同步。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：员工、部门、宿舍 / 电表、费用月份、金额、财务同步、更新时间、备注、操作。
- 脚本列 `importColumns`：工号、员工姓名、金额、楼栋、房间、床位、电表号、备注。
- 表格列绑定：columns、importColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: minmax(0, 1.6fr) minmax(320px, 1fr)、height: 1.2、grid-template-columns: repeat(3, minmax(0, 1fr))、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：待同步、已同步、员工、部门、宿舍 / 电表、费用月份、金额、财务同步、更新时间、备注、操作、工号、员工姓名、楼栋、房间、床位、电表号。
- 页签/分区：员工宿舍、电表、电费上传与财务推送统一处理。
- 指标和说明静态文字：宿舍水电台账、支持按月批量上传员工电费金额，自动回写宿舍与电表信息，并一键同步财务或打印下载。、已导入人数、当前筛选电费记录、月度电费、上传后的总金额、待同步财务、未推送到财务、电表：、费用月份、待导入条数、总金额、是否住宿舍。
- 弹层标题：电费上传预览、宿舍联动信息。
- 弹层表单字段：员工、宿舍楼栋、宿舍房间、床位号、电表号、备注。
- 可读操作文案：下载模板、上传电费 CSV、同步财务、一键打印、导出 CSV、导出 Excel、宿舍信息、同步、取消、确认导入、保存宿舍信息。

### 28. 报销审批流 `/hr/expense/approval-flow`

费用类型、场景、标题、申请人、金额、状态、申请时间、备注；可新建审批单。页面并非纯审批流规则配置。

- 导航归属：人力资源 > 薪酬与费用 > 报销审批流。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrExpenseApprovalFlow.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrExpenseApprovalFlow.vue)**

- 筛选/主页面表单：关键字、审批状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：费用类型、标题、申请人、金额、状态、申请开始、申请结束、创建时间、备注。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：待审批、已通过、已驳回、培训费用、补贴、工资补贴、餐费、电费、费用类型、标题、申请人、金额、状态、申请开始、申请结束、创建时间、备注。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：新建报销审批。
- 弹层表单字段：费用类型、场景、标题、金额、状态、申请区间、备注。
- 可读操作文案：导出 CSV、导出 Excel、新建报销申请、取消、提交。

### 29. 培训记录 `/hr/development/records`

培训记录/证书档案双页签；部门员工、年度、课程、讲师、机构、日期、学时、出勤、成绩、测试、考证、证书状态和附件；个人培训档案。

- 导航归属：人力资源 > 培训发展 > 培训记录。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格 → 页签 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/Training.vue](/Users/yang/smartcare/admin-web/src/views/hr/Training.vue)**

- 筛选/主页面表单：部门、员工、年度、课程/关键字、时间范围。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `archiveTrainingColumns`：培训名称、培训日期、学时、讲师、测试结果、证书状态。
- 脚本列 `archiveCertificateColumns`：证书名称、证书编号、颁发机构、颁发日期、到期日期。
- 表格列绑定：columns、archiveTrainingColumns、archiveCertificateColumns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：待执行、已完成、未完成、出勤、迟到、缺勤、请假、优秀、通过、未通过、否、是、无需证书、待生成、已生成、已发放、年度、部门、培训名称、培训类别、讲师、培训日期、学时、考证要求、状态、附件、员工姓名、工号、所属部门、出勤状态、成绩、测试结果、证书状态、证书名称、证书编号、颁发机构、颁发日期、到期日期。
- 页签/分区：培训记录、证书档案。
- 指标和说明静态文字：已勾选 条、-。
- 弹层标题：drawerTitle、个人培训档案。
- 弹层表单字段：所属部门、年度、员工、培训名称、培训类别、讲师、培训机构、培训日期、出勤状态、成绩、测试结果、学时、要求考证、证书状态、证书编号、状态、附件、备注。
- 可读操作文案：编辑、个人培训档案、导出 Excel、导出 PDF、删除、批量删除、清空筛选、上传附件、取消、保存。

### 30. 证书管理 `/hr/development/certificates`

与“证书上传”完全复用同一页面，无路由场景区别。

- 导航归属：人力资源 > 培训发展 > 证书管理。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D05。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrProfileCertificates.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrProfileCertificates.vue)**

- 筛选/主页面表单：关键字、有效期范围。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：员工、证书名称、证书编号、颁发机构、颁发日期、到期日期、剩余天数。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：员工、证书名称、证书编号、颁发机构、颁发日期、到期日期、剩余天数。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：天。
- 弹层标题：新增证书。
- 弹层表单字段：员工、证书名称、证书编号、颁发机构、颁发日期、到期日期、备注。
- 可读操作文案：导出 CSV、导出 Excel、新增证书、取消、保存。

### 31. 证书到期提醒 `/hr/development/certificate-reminders`

证书临期清单：员工、证书/编号、机构、到期日、剩余天数；预警天数与过期筛选。

- 导航归属：人力资源 > 培训发展 > 证书到期提醒。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/HrCertificateReminders.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrCertificateReminders.vue)**

- 筛选/主页面表单：关键字、预警天数、包含已过期。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：员工、证书名称、证书编号、颁发机构、到期日期、剩余天数。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(auto-fit, minmax(160px, 1fr))、height: 1.2。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：员工、证书名称、证书编号、颁发机构、到期日期、剩余天数。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已过期、需立即补证或暂停高风险岗位、7天内到期、优先安排续证材料和培训、30天内到期、纳入本月资质合规清单、提醒范围、当前筛选预警天数、天。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：导出 CSV、导出 Excel。

### 32. 评分规则配置 `/hr/performance/scoring-rules`

护理模板的基础积分、评分权重、可疑扣减、失败积分、启停；复用积分规则页。

- 导航归属：人力资源 > 绩效管理 > 评分规则配置。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D06。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/PointsRule.vue](/Users/yang/smartcare/admin-web/src/views/hr/PointsRule.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：模板、基础积分、评分权重、可疑扣减、失败积分、状态。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：模板、基础积分、评分权重、可疑扣减、失败积分、状态、启用、停用、确认删除积分规则？、确认批量删除积分规则？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：drawerTitle。
- 弹层表单字段：护理模板、基础积分、评分权重、可疑扣减、失败积分、状态、备注。
- 可读操作文案：新增规则、编辑、启用、停用、删除、批量启用、批量停用、批量删除、取消、保存。

### 33. 绩效报表 `/hr/performance/reports`

员工分类/排名/勋章、任务量、完成/失败/可疑、满意度、周期积分、余额、可兑换现金；兑换记录与申请。

- 导航归属：人力资源 > 绩效管理 > 绩效报表。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 数据表格 → 弹窗/抽屉；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/Performance.vue](/Users/yang/smartcare/admin-web/src/views/hr/Performance.vue)**

- 筛选/主页面表单：员工、员工类别、日期范围、排序。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：总排名、分组排名、员工、类别、勋章、任务量、完成、综合满意度、周期积分、积分余额、可兑现金(元)。
- 脚本列 `redeemColumns`：申请时间、兑换员工、发起人、兑换积分、兑换现金(元)、状态。
- 表格列绑定：columns、redeemColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：height: 1.2。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：按积分、按任务量、护理人员、行政人员、运营人员、总排名、分组排名、员工、类别、勋章、任务量、完成、综合满意度、周期积分、积分余额、可兑现金(元)、申请时间、兑换员工、发起人、兑换积分、兑换现金(元)、状态。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：积分兑现金申请。
- 弹层表单字段：兑换员工、兑换积分、可兑换现金(元)、备注。
- 可读操作文案：积分兑现金、刷新。

### 34. 奖惩记录 `/hr/performance/reward-punishment`

员工、奖惩类型/级别、标题、发生日、金额、积分、状态；新增编辑与作废。

- 导航归属：人力资源 > 绩效管理 > 奖惩记录。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/oa/RewardPunishment.vue](/Users/yang/smartcare/admin-web/src/views/oa/RewardPunishment.vue)**

- 筛选/主页面表单：员工、类型、状态、关键字。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：员工、类型、级别、标题、发生日期、金额、积分、状态。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：员工、类型、级别、标题、发生日期、金额、积分、状态、奖励、惩罚、生效、已作废。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：form.id ? '编辑奖惩' : '新增奖惩'。
- 弹层表单字段：员工、类型、级别、状态、标题、原因、金额、积分、发生日期、备注。
- 可读操作文案：新增记录、编辑、删除、批量删除。

### 35. 积分台账 `/hr/incentive/ledger`

员工积分变动台账：变动类型、积分、余额、来源、时间、备注；手工调整。

- 导航归属：人力资源 > 积分激励 > 积分台账。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/Points.vue](/Users/yang/smartcare/admin-web/src/views/hr/Points.vue)**

- 筛选/主页面表单：员工、日期范围。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：员工、类型、变动、余额、来源、时间、备注。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：员工、类型、变动、余额、来源、时间、备注、奖励、扣减、调整。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：积分调整。
- 弹层表单字段：员工、调整类型、积分数量、备注。
- 可读操作文案：积分调整。

### 36. 积分规则 `/hr/incentive/rules`

与“评分规则配置”相同 PointsRule，包含护理模板、基础积分、权重和扣减。

- 导航归属：人力资源 > 积分激励 > 积分规则。
- 路由角色约束：[["HR_EMPLOYEE", "HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：D06。
- 路由依据：[admin-web/src/router/hrRoutes.ts](/Users/yang/smartcare/admin-web/src/router/hrRoutes.ts)。

**页面：[admin-web/src/views/hr/PointsRule.vue](/Users/yang/smartcare/admin-web/src/views/hr/PointsRule.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：模板、基础积分、评分权重、可疑扣减、失败积分、状态。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：模板、基础积分、评分权重、可疑扣减、失败积分、状态、启用、停用、确认删除积分规则？、确认批量删除积分规则？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：已勾选 条。
- 弹层标题：drawerTitle。
- 弹层表单字段：护理模板、基础积分、评分权重、可疑扣减、失败积分、状态、备注。
- 可读操作文案：新增规则、编辑、启用、停用、删除、批量启用、批量停用、批量删除、取消、保存。

## 相关重复项

### D05 证书上传 / 证书管理

- 判断：确定重复入口。
- 依据：同一 HrProfileCertificates，无不同 props 或路由读取。
- 后续整理方向：保留证书档案入口，临期作为视图。

### D06 评分规则配置 / 积分规则

- 判断：确定重复入口。
- 依据：同一 PointsRule，无不同 props 或路由读取。
- 后续整理方向：保留一个规则维护入口。

### D15 员工主档 / 劳动合同 / 合同打印 / 临期提醒

- 判断：主档与任务视图重复展示。
- 依据：员工、合同号、类型、起止、状态多处展示，但编辑/打印/预警用途不同。
- 后续整理方向：合同维护归员工档案；临期提醒作为待办入口。

### D16 宿舍台账 / 房态图 / 餐费 / 电费

- 判断：共享基础信息，多处可编辑。
- 依据：多个页面包含员工住宿、电表、用餐方案编辑。图与台账不是完全重复页面。
- 后续整理方向：统一基础资料维护位置；费用页只带入资料并跳转修改。

### D20 多个首页/驾驶舱

- 判断：导航和指标层层重复。
- 依据：总览、个人、部门、行政、驾驶舱重复承担入口和摘要；口径及受众并非完全一样。
- 后续整理方向：明确每类首页唯一职责，摘要点击下钻而非再次进入导航。
