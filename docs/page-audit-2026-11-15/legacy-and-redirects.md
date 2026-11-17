# 隐藏页面、兼容链接和分组跳转

隐藏不等于无权限；分组跳转不等于一个独立业务页面。下面三类分别登记，未把它们计入主表的非隐藏页面入口数。

## 隐藏且仍挂载组件的页面

| 模块 | 名称 | 路径 | 组件 |
|---|---|---|---|
| shared | Dashboard | /dashboard | [admin-web/src/views/Dashboard.vue](/Users/yang/smartcare/admin-web/src/views/Dashboard.vue) |
| shared | 长者导航 | /elder | [admin-web/src/views/ModuleHub.vue](/Users/yang/smartcare/admin-web/src/views/ModuleHub.vue) |
| shared | 长者总览 | /elder/resident-360 | [admin-web/src/views/elder/resident360/Resident360.vue](/Users/yang/smartcare/admin-web/src/views/elder/resident360/Resident360.vue) |
| shared | 量表模板 | /elder/assessment/template | [admin-web/src/views/assessment/Template.vue](/Users/yang/smartcare/admin-web/src/views/assessment/Template.vue) |
| shared | 新建老人 | /elder/create | [admin-web/src/views/elder/Create.vue](/Users/yang/smartcare/admin-web/src/views/elder/Create.vue) |
| shared | 编辑老人 | /elder/edit/:id | [admin-web/src/views/elder/Edit.vue](/Users/yang/smartcare/admin-web/src/views/elder/Edit.vue) |
| shared | 老人详情 | /elder/detail/:id | [admin-web/src/views/elder/Detail.vue](/Users/yang/smartcare/admin-web/src/views/elder/Detail.vue) |
| shared | CRM线索(旧) | /elder/crm | [admin-web/src/views/elder/Crm.vue](/Users/yang/smartcare/admin-web/src/views/elder/Crm.vue) |
| shared | 事故登记 | /elder/incident | [admin-web/src/views/life/Incident.vue](/Users/yang/smartcare/admin-web/src/views/life/Incident.vue) |
| shared | 退住登记 | /elder/discharge | [admin-web/src/views/elder/Discharge.vue](/Users/yang/smartcare/admin-web/src/views/elder/Discharge.vue) |
| shared | 变更记录 | /elder/change-log | [admin-web/src/views/elder/ChangeLog.vue](/Users/yang/smartcare/admin-web/src/views/elder/ChangeLog.vue) |
| marketing | 营销导航 | /marketing | [admin-web/src/views/ModuleHub.vue](/Users/yang/smartcare/admin-web/src/views/ModuleHub.vue) |
| marketing | 合同签约（兼容） | /marketing/contract-signing | [admin-web/src/views/marketing/ContractSigning.vue](/Users/yang/smartcare/admin-web/src/views/marketing/ContractSigning.vue) |
| marketing | 合同到期管理（兼容） | /marketing/contract-management | [admin-web/src/views/marketing/ContractManagement.vue](/Users/yang/smartcare/admin-web/src/views/marketing/ContractManagement.vue) |
| marketing | 房态全景（兼容） | /marketing/room-panorama | [admin-web/src/views/marketing/RoomPanorama.vue](/Users/yang/smartcare/admin-web/src/views/marketing/RoomPanorama.vue) |
| finance | 财务导航 | /finance | [admin-web/src/views/ModuleHub.vue](/Users/yang/smartcare/admin-web/src/views/ModuleHub.vue) |
| finance | 财务搜索 | /finance/search | [admin-web/src/views/finance/FinanceSearch.vue](/Users/yang/smartcare/admin-web/src/views/finance/FinanceSearch.vue) |
| finance | 账单详情 | /finance/bill/:billId | [admin-web/src/views/finance/BillDetail.vue](/Users/yang/smartcare/admin-web/src/views/finance/BillDetail.vue) |
| hr | 绩效看板 | /hr/performance-board | [admin-web/src/views/hr/Performance.vue](/Users/yang/smartcare/admin-web/src/views/hr/Performance.vue) |
| system | 基础数据导航 | /base-config | [admin-web/src/views/ModuleHub.vue](/Users/yang/smartcare/admin-web/src/views/ModuleHub.vue) |
| system | 系统管理首页 | /system | [admin-web/src/views/System/SystemAdminHome.vue](/Users/yang/smartcare/admin-web/src/views/System/SystemAdminHome.vue) |
| system | 菜单预览 | /system/menu-preview | [admin-web/src/views/System/MenuPreview.vue](/Users/yang/smartcare/admin-web/src/views/System/MenuPreview.vue) |
| system | 高级表格 | /demo/vxe-table | [admin-web/src/views/demo/VxeTableDemo.vue](/Users/yang/smartcare/admin-web/src/views/demo/VxeTableDemo.vue) |
| system | 排班日历 | /demo/calendar | [admin-web/src/views/demo/CalendarDemo.vue](/Users/yang/smartcare/admin-web/src/views/demo/CalendarDemo.vue) |
| system | 报表图表 | /demo/charts | [admin-web/src/views/demo/ChartsDemo.vue](/Users/yang/smartcare/admin-web/src/views/demo/ChartsDemo.vue) |
| system | 富文本编辑 | /demo/editor | [admin-web/src/views/demo/EditorDemo.vue](/Users/yang/smartcare/admin-web/src/views/demo/EditorDemo.vue) |
| system | 流程图 | /demo/flow | [admin-web/src/views/demo/FlowDemo.vue](/Users/yang/smartcare/admin-web/src/views/demo/FlowDemo.vue) |
| system | 智能助手 | /demo/antdx-chat | [admin-web/src/views/demo/AntdxChatDemo.vue](/Users/yang/smartcare/admin-web/src/views/demo/AntdxChatDemo.vue) |
| administration | 家属服务健康中心 | /oa/family-service-health | [admin-web/src/views/operations/FamilyServiceHealthCenter.vue](/Users/yang/smartcare/admin-web/src/views/operations/FamilyServiceHealthCenter.vue) |
| administration | 家属充值台账 | /oa/family-recharge | [admin-web/src/views/operations/FamilyRechargeLedgerCenter.vue](/Users/yang/smartcare/admin-web/src/views/operations/FamilyRechargeLedgerCenter.vue) |
| administration | 分组设置（兼容） | /oa/group-settings | [admin-web/src/views/oa/GroupSettings.vue](/Users/yang/smartcare/admin-web/src/views/oa/GroupSettings.vue) |
| administration | 员工建议中心 | /oa/employee-suggestions | [admin-web/src/views/oa/EmployeeSuggestionCenter.vue](/Users/yang/smartcare/admin-web/src/views/oa/EmployeeSuggestionCenter.vue) |
| analytics | 统计导航 | /stats | [admin-web/src/views/ModuleHub.vue](/Users/yang/smartcare/admin-web/src/views/ModuleHub.vue) |
| analytics | 服务质量与合规 | /stats/service-compliance | [admin-web/src/views/operations/ServiceComplianceCenter.vue](/Users/yang/smartcare/admin-web/src/views/operations/ServiceComplianceCenter.vue) |
| analytics | 安全风险 | /stats/safety-risk | [admin-web/src/views/operations/SafetyRiskCenter.vue](/Users/yang/smartcare/admin-web/src/views/operations/SafetyRiskCenter.vue) |
| analytics | 员工人力 | /stats/workforce | [admin-web/src/views/operations/WorkforceCenter.vue](/Users/yang/smartcare/admin-web/src/views/operations/WorkforceCenter.vue) |
| analytics | 员工移动端 | /stats/staff-mobile | [admin-web/src/views/operations/StaffMobileCenter.vue](/Users/yang/smartcare/admin-web/src/views/operations/StaffMobileCenter.vue) |
| analytics | 员工现场执行台账 | /stats/staff-mobile-ledger | [admin-web/src/views/operations/StaffMobileLedgerCenter.vue](/Users/yang/smartcare/admin-web/src/views/operations/StaffMobileLedgerCenter.vue) |
| analytics | 后勤保障 | /stats/logistics | [admin-web/src/views/operations/LogisticsCenter.vue](/Users/yang/smartcare/admin-web/src/views/operations/LogisticsCenter.vue) |
| analytics | 营销转化 | /stats/marketing | [admin-web/src/views/operations/MarketingCenter.vue](/Users/yang/smartcare/admin-web/src/views/operations/MarketingCenter.vue) |
| analytics | 智能运营 | /stats/intelligence | [admin-web/src/views/operations/IntelligenceCenter.vue](/Users/yang/smartcare/admin-web/src/views/operations/IntelligenceCenter.vue) |
| logistics | 后勤导航 | /logistics | [admin-web/src/views/ModuleHub.vue](/Users/yang/smartcare/admin-web/src/views/ModuleHub.vue) |
| logistics | 房间详情 | /logistics/assets/room-detail | [admin-web/src/views/bed/RoomDetail.vue](/Users/yang/smartcare/admin-web/src/views/bed/RoomDetail.vue) |
| nursing | 护理看板 | /care/dashboard | [admin-web/src/views/care/Dashboard.vue](/Users/yang/smartcare/admin-web/src/views/care/Dashboard.vue) |
| nursing | 护理套餐 | /care/care-packages | [admin-web/src/views/care/CarePackages.vue](/Users/yang/smartcare/admin-web/src/views/care/CarePackages.vue) |
| nursing | 套餐明细 | /care/package-items | [admin-web/src/views/care/PackageItems.vue](/Users/yang/smartcare/admin-web/src/views/care/PackageItems.vue) |
| nursing | 老人套餐 | /care/elder-packages | [admin-web/src/views/care/ElderPackages.vue](/Users/yang/smartcare/admin-web/src/views/care/ElderPackages.vue) |
| nursing | 套餐任务生成 | /care/task-generate | [admin-web/src/views/care/TaskGenerate.vue](/Users/yang/smartcare/admin-web/src/views/care/TaskGenerate.vue) |
| nursing | 防作弊审计 | /care/audit | [admin-web/src/views/care/Audit.vue](/Users/yang/smartcare/admin-web/src/views/care/Audit.vue) |
| nursing | 护工扫码执行 | /care/workbench/qr | [admin-web/src/views/care/workbench/ScanExecute.vue](/Users/yang/smartcare/admin-web/src/views/care/workbench/ScanExecute.vue) |
| nursing | 服务预约 | /care/workbench/task | [admin-web/src/views/care/workbench/ServiceBooking.vue](/Users/yang/smartcare/admin-web/src/views/care/workbench/ServiceBooking.vue) |
| medical | 药品字典 | /health/medication/drug-dictionary | [admin-web/src/views/health/DrugDictionary.vue](/Users/yang/smartcare/admin-web/src/views/health/DrugDictionary.vue) |
| medical | 药品缴存 | /health/medication/drug-deposit | [admin-web/src/views/health/DrugDeposit.vue](/Users/yang/smartcare/admin-web/src/views/health/DrugDeposit.vue) |
| medical | 用药设置 | /health/medication/medication-setting | [admin-web/src/views/health/MedicationSetting.vue](/Users/yang/smartcare/admin-web/src/views/health/MedicationSetting.vue) |
| medical | 用药登记 | /health/medication/medication-registration | [admin-web/src/views/health/MedicationRegistration.vue](/Users/yang/smartcare/admin-web/src/views/health/MedicationRegistration.vue) |
| medical | 剩余用药 | /health/medication/medication-remaining | [admin-web/src/views/health/MedicationRemaining.vue](/Users/yang/smartcare/admin-web/src/views/health/MedicationRemaining.vue) |
| medical | 健康档案 | /health/management/archive | [admin-web/src/views/health/HealthArchive.vue](/Users/yang/smartcare/admin-web/src/views/health/HealthArchive.vue) |
| medical | 健康数据 | /health/management/data | [admin-web/src/views/health/HealthData.vue](/Users/yang/smartcare/admin-web/src/views/health/HealthData.vue) |
| medical | 健康巡检 | /health/inspection | [admin-web/src/views/health/Inspection.vue](/Users/yang/smartcare/admin-web/src/views/health/Inspection.vue) |
| medical | 护理日志 | /health/nursing-log | [admin-web/src/views/health/NursingLog.vue](/Users/yang/smartcare/admin-web/src/views/health/NursingLog.vue) |
| medical | 医护照护工作台 | /medical-care/workbench | [admin-web/src/views/medical/Workbench.vue](/Users/yang/smartcare/admin-web/src/views/medical/Workbench.vue) |
| medical | 医嘱执行风险总览 | /medical-care/order-risk-overview | [admin-web/src/views/medical/MedicalOrderCenter.vue](/Users/yang/smartcare/admin-web/src/views/medical/MedicalOrderCenter.vue) |
| safety | 消防导航 | /fire | [admin-web/src/views/ModuleHub.vue](/Users/yang/smartcare/admin-web/src/views/ModuleHub.vue) |
| safety | 长护险导航 | /ltci | [admin-web/src/views/ModuleHub.vue](/Users/yang/smartcare/admin-web/src/views/ModuleHub.vue) |
| service | 卡务管理 | /card/account | [admin-web/src/views/card/Account.vue](/Users/yang/smartcare/admin-web/src/views/card/Account.vue) |
| service | 充值记录 | /card/recharge | [admin-web/src/views/card/Recharge.vue](/Users/yang/smartcare/admin-web/src/views/card/Recharge.vue) |
| service | 消费记录 | /card/consume | [admin-web/src/views/card/Consume.vue](/Users/yang/smartcare/admin-web/src/views/card/Consume.vue) |
| service | 会员生日 | /life/birthday | [admin-web/src/views/life/Birthday.vue](/Users/yang/smartcare/admin-web/src/views/life/Birthday.vue) |
| service | 膳食计划 | /life/meal-plan | [admin-web/src/views/life/MealPlan.vue](/Users/yang/smartcare/admin-web/src/views/life/MealPlan.vue) |
| service | 活动管理 | /life/activity | [admin-web/src/views/life/Activity.vue](/Users/yang/smartcare/admin-web/src/views/life/Activity.vue) |
| service | 事故登记 | /life/incident | [admin-web/src/views/life/Incident.vue](/Users/yang/smartcare/admin-web/src/views/life/Incident.vue) |
| service | 基础健康记录 | /life/health-basic | [admin-web/src/views/life/HealthBasic.vue](/Users/yang/smartcare/admin-web/src/views/life/HealthBasic.vue) |
| service | 房间打扫 | /life/room-cleaning | [admin-web/src/views/life/RoomCleaning.vue](/Users/yang/smartcare/admin-web/src/views/life/RoomCleaning.vue) |
| service | 维修管理 | /life/maintenance | [admin-web/src/views/life/Maintenance.vue](/Users/yang/smartcare/admin-web/src/views/life/Maintenance.vue) |
| service | 题库管理 | /survey/question-bank | [admin-web/src/views/survey/QuestionBank.vue](/Users/yang/smartcare/admin-web/src/views/survey/QuestionBank.vue) |
| service | 模板管理 | /survey/template | [admin-web/src/views/survey/Template.vue](/Users/yang/smartcare/admin-web/src/views/survey/Template.vue) |
| service | 问卷填写 | /survey/submit | [admin-web/src/views/survey/Submit.vue](/Users/yang/smartcare/admin-web/src/views/survey/Submit.vue) |
| service | 问卷统计 | /survey/stats | [admin-web/src/views/survey/Stats.vue](/Users/yang/smartcare/admin-web/src/views/survey/Stats.vue) |
| service | 绩效榜 | /survey/performance | [admin-web/src/views/survey/Performance.vue](/Users/yang/smartcare/admin-web/src/views/survey/Performance.vue) |

## 隐藏重定向

| 模块 | 名称 | 旧路径 | 目的地 |
|---|---|---|---|
| marketing | 客户互动中心（兼容） | /marketing/followup | "/marketing/interactions/records" |
| marketing | records | /marketing/followup/records | "/marketing/interactions/records" |
| marketing | due | /marketing/followup/due | "/marketing/interactions/due" |
| marketing | today | /marketing/followup/today | "/marketing/interactions/today" |
| marketing | overdue | /marketing/followup/overdue | "/marketing/interactions/overdue" |
| marketing | 销售跟进（兼容） | /marketing/sales | "/marketing/leads/all" |
| marketing | pipeline | /marketing/sales/pipeline | "/marketing/leads/all" |
| marketing | consultation | /marketing/sales/consultation | "/marketing/leads/all?tab=consultation" |
| marketing | intent | /marketing/sales/intent | "/marketing/leads/intent" |
| marketing | reservation | /marketing/sales/reservation | "/marketing/reservation/records" |
| marketing | invalid | /marketing/sales/invalid | "/marketing/leads/invalid" |
| marketing | callback | /marketing/sales/callback | "/marketing/interactions/due" |
| finance | 财务提醒中心（已并入欠费与提醒中心） | /finance/reminder-center | {"path": "/finance/bills/follow-up", "query": {"tab": "reminder"}} |
| finance | Excel 报表（已并入经营分析） | /finance/excel-reports | "/finance/reports/excel" |
| finance | 账户与预存（兼容） | /finance/accounts | "/finance/accounts/list" |
| finance | 账单与账户（兼容） | /finance/bills | "/finance/bills/in-resident" |
| finance | 消费与分摊（兼容） | /finance/flows | "/finance/flows/consumption" |
| finance | 收费与票据（兼容） | /finance/fees | "/finance/fees/payment-and-invoice" |
| finance | 公共费用分摊（兼容） | /finance/allocation | "/finance/allocation/public-cost" |
| finance | 欠费与异常（兼容） | /finance/reconcile | "/finance/reconcile/center" |
| finance | 入住账单缴费（兼容） | /finance/admission-bill-payment | "/finance/bills/in-admission" |
| finance | 在住账单缴费（兼容） | /finance/resident-bill-payment | "/finance/bills/in-resident" |
| finance | 月分摊费（兼容） | /finance/monthly-allocation | "/finance/allocation/public-cost" |
| finance | 退住费用审核（兼容） | /finance/discharge-fee-audit | "/finance/discharge/review" |
| finance | 财务结算（兼容） | /finance/finance-settlement | "/finance/reconcile/center" |
| finance | 退住结算（兼容） | /finance/discharge-settlement | "/finance/discharge/settlement" |
| finance | 医护费用流水（兼容） | /finance/medical-care-ledger | "/finance/flows/medical" |
| finance | 消费登记（兼容） | /finance/consumption-register | "/finance/flows/consumption" |
| finance | 财务报表（兼容） | /finance/report | "/finance/reports/overall" |
| finance | 老人账户（兼容） | /finance/account | "/finance/accounts/list" |
| finance | 账户流水（兼容） | /finance/account-log | "/finance/accounts/ledger" |
| finance | bill | /finance/bill | "/finance/bills/in-resident" |
| finance | reconcile-center | /finance/reconcile-center | "/finance/reconcile/center" |
| finance | reconcile-invoice | /finance/reconcile-invoice | "/finance/reconcile/invoice" |
| finance | reconcile-exception | /finance/reconcile-exception | "/finance/reconcile/exception" |
| hr | 人事行政工作台（兼容） | /hr/workbench | {"path": "/hr/overview", "query": {}, "hash": ""} |
| hr | 岗位发布（兼容） | /hr/recruitment/job-posting | "/hr/recruitment/needs?scene=job-posting" |
| hr | 候选人库（兼容） | /hr/recruitment/candidates | "/hr/recruitment/needs?scene=candidates" |
| hr | 面试管理（兼容） | /hr/recruitment/interviews | "/hr/recruitment/needs?scene=interviews" |
| hr | 入职办理（兼容） | /hr/recruitment/onboarding | "/hr/recruitment/needs?scene=onboarding" |
| hr | 入职资料收集（兼容） | /hr/recruitment/materials | "/hr/recruitment/needs?scene=materials" |
| hr | 退职办理（兼容） | /hr/recruitment/offboarding | "/hr/recruitment/needs?scene=offboarding" |
| hr | 培训记录（兼容） | /hr/profile/training-records | "/hr/development/records?scene=records" |
| hr | 调班申请（兼容） | /hr/attendance/shift-change | "/hr/attendance/leave-approval?scene=shift-change" |
| hr | 加班申请（兼容） | /hr/attendance/overtime | "/hr/attendance/leave-approval?scene=overtime" |
| hr | 行政协同（兼容） | /hr/oa | "/oa/notice" |
| hr | 通知管理（兼容） | /hr/oa/notices | "/oa/notice" |
| hr | 任务管理（兼容） | /hr/oa/tasks | "/oa/work-execution/task" |
| hr | 工作执行（兼容） | /hr/oa/execution | "/oa/todo" |
| hr | 活动计划（兼容） | /hr/oa/activity-plan | "/oa/activity-plan" |
| hr | 知识库（兼容） | /hr/oa/knowledge | "/oa/knowledge" |
| hr | 分组设置（兼容） | /hr/oa/groups | "/oa/group-settings" |
| hr | 规章制度库（兼容） | /hr/oa/policies | "/hr/compliance/policies" |
| hr | 制度更新预警（兼容） | /hr/oa/policy-alerts | "/hr/compliance/policy-alerts" |
| hr | 外出培训费用报销（兼容） | /hr/expense/training-reimburse | "/hr/expense/records?scene=training-reimburse" |
| hr | 补贴申请（兼容） | /hr/expense/subsidy | "/hr/expense/records?scene=subsidy" |
| hr | 工资补贴记录（兼容） | /hr/expense/salary-subsidy | "/hr/expense/records?scene=salary-subsidy" |
| hr | 培训报名（兼容） | /hr/development/enrollments | "/hr/development/records?scene=enrollments" |
| hr | 培训签到（兼容） | /hr/development/signin | "/hr/development/records?scene=signin" |
| hr | 护理绩效（兼容） | /hr/performance/nursing | "/hr/performance/reports?scene=nursing" |
| hr | 销售绩效（兼容） | /hr/performance/sales | "/hr/performance/reports?scene=sales" |
| hr | 行政绩效（兼容） | /hr/performance/admin | "/hr/performance/reports?scene=admin" |
| hr | 绩效生成（兼容） | /hr/performance/generation | "/hr/performance/reports?scene=generation" |
| hr | 员工档案（兼容） | /hr/staff | "/hr/profile/basic" |
| hr | 培训管理（兼容） | /hr/training | "/hr/development/records" |
| hr | 积分管理（兼容） | /hr/points | {"path": "/hr/incentive/ledger", "query": {}, "hash": ""} |
| hr | 积分规则（兼容） | /hr/points-rule | {"path": "/hr/incentive/rules", "query": {}, "hash": ""} |
| system | 楼栋管理 | /base-config/residence/building-management | "/logistics/assets/bed-management" |
| system | index | /base-config/index | "/base-config/elder-type" |
| system | 机构管理（已下沉） | /system/org-manage | "/system/site-config?tab=profile" |
| system | 机构介绍 | /system/org-manage/intro | "/system/site-config?tab=profile" |
| system | 机构动态 | /system/org-manage/news | "/system/site-config?tab=profile" |
| system | 生活娱乐 | /system/org-manage/life | "/system/site-config?tab=profile" |
| system | APP版本管理（已下沉） | /system/app-version | "/system/site-config?tab=profile" |
| system | 系统字典（并入基础数据配置） | /system/dict | "/base-config/elder-type" |
| system | 留言管理 | /system/message | "/system/site-config?tab=consult" |
| system | 员工管理（兼容） | /system/staff | "/hr/profile/account-access" |
| system | 权限总览（已并入角色管理） | /system/permission-overview | "/system/role" |
| administration | 行政日历 / 协同日历 | /oa/work-execution/calendar | {"path": "/workbench/schedule", "query": {}, "hash": ""} |
| administration | 考勤与请假（兼容） | /oa/work-execution/attendance-leave | "/oa/attendance-leave" |
| administration | 员工档案（兼容） | /oa/staff | "/hr/profile/basic" |
| administration | 满意度问卷（兼容） | /oa/survey | "/oa/activity-center/survey-manage" |
| administration | 问卷管理（兼容） | /oa/survey/manage | {"path": "/oa/activity-center/survey-manage", "query": {}, "hash": ""} |
| administration | 问卷统计（兼容） | /oa/survey/stats | {"path": "/oa/activity-center/survey-stats", "query": {}, "hash": ""} |
| administration | 活动管理（兼容） | /oa/activity | {"path": "/oa/activity-center/records", "query": {}, "hash": ""} |
| administration | 活动计划（兼容） | /oa/activity-plan | {"path": "/oa/activity-center/plan", "query": {}, "hash": ""} |
| administration | 奖惩管理（兼容） | /oa/reward-punishment | "/hr/performance/reward-punishment" |
| administration | 培训管理（兼容） | /oa/training | "/hr/development/records" |
| administration | 门户与待办（兼容） | /oa/portal | {"path": "/workbench", "query": {}, "hash": ""} |
| administration | 我的信息（兼容） | /oa/my-info | {"path": "/workbench/my-info", "query": {}, "hash": ""} |
| administration | 待办事项（兼容） | /oa/todo | {"path": "/workbench/todo", "query": {}, "hash": ""} |
| administration | 考勤与请假（兼容） | /oa/attendance-leave | {"path": "/workbench/attendance", "query": {}, "hash": ""} |
| administration | 工作总结（兼容） | /oa/work-report | {"path": "/workbench/reports", "query": {}, "hash": ""} |
| logistics | 楼栋管理 | /logistics/assets/building-management | "/logistics/assets/bed-management" |
| logistics | 楼层管理 | /logistics/assets/floor-management | "/logistics/assets/bed-management" |
| logistics | 房间管理 | /logistics/assets/room-management | "/logistics/assets/bed-management" |
| logistics | 楼栋平面图（已并入房态图） | /logistics/assets/floor-plan | {"name": "LogisticsRoomStateMap", "query": {"view": "plan"}} |
| logistics | 床态全景 | /logistics/assets/bed-panorama | "/elder/bed-panorama" |
| nursing | 照护任务看板 | /care/workbench/task-board | {"path": "/medical-care/care-task-board", "query": {}} |
| medical | 健康服务 | /health | "/medical-care/center" |
| medical | 用药服务 | /health/medication | "/health/medication/drug-dictionary" |
| medical | 健康管理 | /health/management | "/health/management/archive" |
| medical | drug-dictionary | /health/drug-dictionary | "/health/medication/drug-dictionary" |
| medical | drug-deposit | /health/drug-deposit | "/health/medication/drug-deposit" |
| medical | medication-setting | /health/medication-setting | "/health/medication/medication-setting" |
| medical | medication-registration | /health/medication-registration | "/health/medication/medication-registration" |
| medical | medication-remaining | /health/medication-remaining | "/health/medication/medication-remaining" |
| medical | archive | /health/archive | "/health/management/archive" |
| medical | data | /health/data | "/health/management/data" |
| service | 一卡通管理 | /card | "/card/account" |
| service | 生活与健康 | /life | "/life/meal-plan" |
| service | 餐饮管理（兼容） | /dining | "/logistics/dining/dish" |
| service | 菜品管理 | /dining/dish | "/logistics/dining/dish" |
| service | 点餐 | /dining/order | "/logistics/dining/order" |
| service | 订餐统计 | /dining/stats | "/logistics/dining/stats" |
| service | 采购计划单 | /dining/procurement-plan | "/logistics/dining/procurement-plan" |
| service | 食谱管理 | /dining/recipe | "/logistics/dining/recipe" |
| service | 分区备餐 | /dining/prep-zone | "/logistics/dining/prep-zone" |
| service | 送餐区域 | /dining/delivery-area | "/logistics/dining/delivery-area" |
| service | 送餐记录 | /dining/delivery-record | "/logistics/dining/delivery-plan" |
| service | 评估管理 | /assessment | "/elder/assessment/ability/admission" |
| service | 能力评估管理 | /assessment/ability | "/elder/assessment/ability/admission" |
| service | AssessmentAdmissionLegacy | /assessment/ability/admission | "/elder/assessment/ability/admission" |
| service | AssessmentRegistrationLegacy | /assessment/ability/registration | "/elder/assessment/ability/registration" |
| service | AssessmentContinuousLegacy | /assessment/ability/continuous | "/elder/assessment/ability/continuous" |
| service | AssessmentArchiveLegacy | /assessment/ability/archive | "/elder/assessment/ability/archive" |
| service | AssessmentOtherScaleLegacy | /assessment/other-scale | "/elder/assessment/other-scale" |
| service | AssessmentTemplateLegacy | /assessment/template | "/elder/assessment/template" |
| service | AssessmentCognitiveLegacy | /assessment/cognitive | "/elder/assessment/cognitive" |
| service | AssessmentSelfCareLegacy | /assessment/self-care | "/elder/assessment/self-care" |
| service | admission | /assessment/admission | "/elder/assessment/ability/admission" |
| service | registration | /assessment/registration | "/elder/assessment/ability/registration" |
| service | continuous | /assessment/continuous | "/elder/assessment/ability/continuous" |
| service | archive | /assessment/archive | "/elder/assessment/ability/archive" |
| service | 问卷与持续改进 | /survey | "/survey/question-bank" |

## 非隐藏的分组默认跳转（不是新增业务页面）

| 模块 | 名称 | 分组路径 | 默认目的地 |
|---|---|---|---|
| shared | 工作台 | /workbench | "/workbench/overview" |
| shared | 评估管理 | /elder/assessment | "/elder/assessment/ability/admission" |
| shared | 能力评估管理 | /elder/assessment/ability | "/elder/assessment/ability/admission" |
| shared | 入住状态变更 | /elder/status-change | "/elder/status-change/center" |
| marketing | 客户线索管理 | /marketing/leads | "/marketing/leads/all" |
| marketing | 客户互动中心 | /marketing/interactions | "/marketing/interactions/records" |
| marketing | 线索阶段管理（销售漏斗） | /marketing/funnel | "/marketing/funnel/consultation" |
| marketing | 预定与床态联动 | /marketing/reservation | "/marketing/reservation/visits" |
| marketing | 合同管理 | /marketing/contracts | "/marketing/contracts/pending" |
| marketing | 签后回访管理 | /marketing/callback | "/marketing/callback/checkin" |
| marketing | 销售报表中心 | /marketing/reports | "/marketing/reports/conversion" |
| finance | 收费与收款 | /finance/payments | "/finance/payments/cashier-desk" |
| finance | 账单与账户 | /finance/billing | "/finance/bills/in-resident" |
| finance | 消费与分摊 | /finance/sources | "/finance/flows/consumption" |
| finance | 欠费与异常 | /finance/risk | "/finance/bills/follow-up" |
| finance | 结算与月结 | /finance/discharge | "/finance/discharge/review" |
| finance | 经营分析 | /finance/reports | "/finance/reports/overall" |
| finance | 规则与配置 | /finance/config | "/finance/config/master-data" |
| hr | 人力资源 | /hr | "/hr/overview" |
| hr | 招聘管理 | /hr/recruitment | "/hr/recruitment/needs" |
| hr | 员工档案 | /hr/profile | "/hr/profile/social-security-reminders" |
| hr | 考勤与班组 | /hr/attendance | "/hr/attendance/schemes" |
| hr | 制度与合规 | /hr/compliance | "/hr/compliance/policies" |
| hr | 薪酬与费用 | /hr/expense | "/hr/expense/records?scene=training-reimburse" |
| hr | 培训发展 | /hr/development | "/hr/development/records" |
| hr | 培训计划 | /hr/development/plans | "/hr/development/records?scene=plans" |
| hr | 绩效管理 | /hr/performance | "/hr/performance/reports" |
| hr | 积分激励 | /hr/incentive | "/hr/incentive/ledger" |
| system | 营销 | /base-config/marketing | "/base-config/marketing/customer-tag" |
| system | 入住 | /base-config/residence | "/base-config/residence/bed-type" |
| system | 床位类型 | /base-config/residence/bed-type | "/logistics/assets/bed-type-config" |
| system | 房间类型 | /base-config/residence/room-type | "/logistics/assets/room-type-config" |
| system | 区域设置 | /base-config/residence/area-settings | "/logistics/assets/area-config" |
| system | 社区 | /base-config/community | "/base-config/community/maintenance-category" |
| administration | 行政管理 | /oa | "/oa/overview" |
| administration | 协同办公 | /oa/work-execution | "/oa/work-execution/task" |
| administration | 生活服务 | /oa/life | "/oa/life/birthday" |
| administration | 一卡通 | /oa/card | "/oa/card/account" |
| administration | 活动与文化 | /oa/activity-center | "/oa/activity-center/plan" |
| analytics | 机构统计 | /stats/org | "/stats/org/monthly-operation" |
| analytics | AI 智能提效 | /ai | "/ai/schedule" |
| logistics | 资产与床态管理 | /logistics/assets | "/logistics/assets/room-state-map" |
| logistics | 餐饮成本统计 | /logistics/dining/cost-stats | "/logistics/dining/stats?metric=cost" |
| logistics | 报修登记 | /logistics/maintenance/report | "/logistics/assets/maintenance-record" |
| logistics | 维修派单 | /logistics/maintenance/dispatch | "/logistics/assets/maintenance-record?status=OPEN" |
| logistics | 维修进度 | /logistics/maintenance/progress | "/logistics/assets/maintenance-record?status=PROCESSING" |
| logistics | 库存金额统计 | /logistics/reports/stock-amount | "/logistics/storage/stock-amount" |
| logistics | 采购统计 | /logistics/reports/purchase | "/logistics/storage/purchase" |
| logistics | 物资消耗统计 | /logistics/reports/consume | "/logistics/storage/outbound" |
| logistics | 餐饮成本统计 | /logistics/reports/dining-cost | "/logistics/dining/stats?metric=cost" |
| nursing | 护理部业务 | /care | "/care/home" |
| nursing | 人员管理 | /care/staff | "/care/staff/caregiver-info" |
| nursing | 服务管理 | /care/service | "/care/service/service-items" |
| nursing | 排班管理 | /care/scheduling | "/care/scheduling/shift-templates" |
| nursing | 交接班 | /care/scheduling/handovers | {"path": "/medical-care/handovers", "query": {}} |
| medical | 医护健康服务 | /medical-care | "/medical-care/center" |