# 管理员、财务、市场、人事页面布局统一实施记录

日期：2026-11-15。代码已落在当前工作区，未发布到生产。本记录承接[页面合并方案](/Users/yang/smartcare/docs/page-audit-2026-11-15/SIMPLIFICATION_PLAN.md)，补齐标题、统计、查询、列表和操作的统一布局。

## 统一后的使用方式

| 区域 | 统一规则 | 实现 |
|---|---|---|
| 标题 | 一个功能主标题；原业务视图作为页签或办理内容选择项，同页切换；业务视图不重复显示大标题 | `FunctionWorkspace`、`PageContainer` |
| 页首操作 | 新增、导入、批量处理集中在页首操作区；默认最多两个常用操作，其余进入一层“更多操作”；保留原权限、禁用状态、上传、确认和事件 | `PageContainer`、`ActionToolbar` |
| 看板 | 日常办理使用紧凑统计条，统一字号、间距、单位和文字对比度；大段介绍放入“使用说明” | `business-layout.less`；12处介绍折叠 |
| 查询 | 默认显示前两个条件；其余点“更多筛选”展开；保留字段值、日期格式和原参数；查询、重置并列 | `QueryBar`、`SearchForm`；47个独立查询来源视图接入，50个来源视图沿用共享搜索组件 |
| 查询提交 | 查询按钮与回车使用表单提交，避免同一查询按钮重复调用；新增、保存、关账和重新对账继续显式点击 | 读查询使用原 `finish` 处理函数；营销方案搜索与新增拆开 |
| 列表 | 统一表头和文字密度；沿用前轮常用列/完整信息切换、分页、排序、固定操作列和表内滚动 | `DataTable`、账单表、原Ant/VXE业务表 |
| 批量处理 | 显示已选数量；“取消选择”调用原父页面选择处理函数；恢复完整信息不改变数据和操作 | `DataTable` |
| 手机布局 | 查询条件纵向排列；操作换行；统计条两列；表格内部按需滚动 | 已检查390px首页和三个业务查询页 |

## 覆盖范围

沿用前轮29个功能主页面、152个来源入口的同页整合。在此基础上检查121个业务来源视图和其直接引用的页面组件：财务42、市场23、人事30、管理员与关联共享组件26。包含安全审计、基础数据、官网配置、审批、文档、卡务和工作总结。121是源码视图数，不是菜单页面数；同一组件可以承载多个原入口，包装页面可以继续复用子组件。

本轮检查到97个视图直接使用统一查询组件，55个视图声明页首操作，37个视图有统一统计条；其余按录入表单、只读图表、日历/房态图或组合页面保留原结构，共享标题和操作规则。字段录入表单、审批详情与编辑弹窗继续保留业务校验。

管理员部门/角色页删除重复统计卡，保留顶部总数和状态信息。基础数据的导入、导出和批量启停保持一个“更多操作”入口。审批页保留四项常用统计，将详细分类统计折叠，恢复“查询在前、列表与详情在后”的顺序，保留原审批及批量处理函数。

[机器可读覆盖清单](/Users/yang/smartcare/docs/page-audit-2026-11-15/layout-coverage.json)。下表只标记该源码模板直接使用的布局；“继承/专用”包括组合页面、录入表单、日历及房态图，不代表遗漏。

| 来源视图 | 查询 | 操作 | 统计 |
|---|---|---|---|
| [marketing/Workbench.vue](/Users/yang/smartcare/admin-web/src/views/marketing/Workbench.vue) | 继承/专用 | 页首 | 专用指标/无看板 |
| [marketing/LeadScenarioPage.vue](/Users/yang/smartcare/admin-web/src/views/marketing/LeadScenarioPage.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |
| [marketing/ContractSigning.vue](/Users/yang/smartcare/admin-web/src/views/marketing/ContractSigning.vue) | SearchForm | 工具条 | 专用指标/无看板 |
| [marketing/VisitAppointments.vue](/Users/yang/smartcare/admin-web/src/views/marketing/VisitAppointments.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [marketing/RoomPanorama.vue](/Users/yang/smartcare/admin-web/src/views/marketing/RoomPanorama.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |
| [marketing/ContractManagement.vue](/Users/yang/smartcare/admin-web/src/views/marketing/ContractManagement.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [marketing/ContractChange.vue](/Users/yang/smartcare/admin-web/src/views/marketing/ContractChange.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [marketing/ContractAttachments.vue](/Users/yang/smartcare/admin-web/src/views/marketing/ContractAttachments.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [marketing/CallbackQuality.vue](/Users/yang/smartcare/admin-web/src/views/marketing/CallbackQuality.vue) | QueryBar | 工具条 | 统计条 |
| [marketing/report/Conversion.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/Conversion.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [marketing/report/Consultation.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/Consultation.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [marketing/report/UnknownChannel.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/UnknownChannel.vue) | 继承/专用 | 工具条 | 统计条 |
| [marketing/report/Followup.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/Followup.vue) | QueryBar | 工具条 | 统计条 |
| [marketing/report/Callback.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/Callback.vue) | QueryBar | 工具条 | 统计条 |
| [marketing/report/Channel.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/Channel.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [marketing/report/SalesPerformance.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/SalesPerformance.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [marketing/report/ChannelRank.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/ChannelRank.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [marketing/report/Snapshots.vue](/Users/yang/smartcare/admin-web/src/views/marketing/report/Snapshots.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [marketing/MarketingPlan.vue](/Users/yang/smartcare/admin-web/src/views/marketing/MarketingPlan.vue) | QueryBar | 页首 | 专用指标/无看板 |
| [finance/FinanceWorkbench.vue](/Users/yang/smartcare/admin-web/src/views/finance/FinanceWorkbench.vue) | 继承/专用 | 页首 | 专用指标/无看板 |
| [finance/CashierDesk.vue](/Users/yang/smartcare/admin-web/src/views/finance/CashierDesk.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [finance/ResidentBillPayment.vue](/Users/yang/smartcare/admin-web/src/views/finance/ResidentBillPayment.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |
| [finance/PaymentRecords.vue](/Users/yang/smartcare/admin-web/src/views/finance/PaymentRecords.vue) | SearchForm | 继承/专用 | 专用指标/无看板 |
| [finance/InvoiceReceiptManagement.vue](/Users/yang/smartcare/admin-web/src/views/finance/InvoiceReceiptManagement.vue) | QueryBar | 工具条 | 统计条 |
| [finance/PaymentRefundReversal.vue](/Users/yang/smartcare/admin-web/src/views/finance/PaymentRefundReversal.vue) | QueryBar | 工具条 | 统计条 |
| [finance/ShiftClose.vue](/Users/yang/smartcare/admin-web/src/views/finance/ShiftClose.vue) | QueryBar | 工具条 | 统计条 |
| [finance/AdmissionBillPayment.vue](/Users/yang/smartcare/admin-web/src/views/finance/AdmissionBillPayment.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |
| [finance/BillCenter.vue](/Users/yang/smartcare/admin-web/src/views/finance/BillCenter.vue) | SearchForm | 继承/专用 | 专用指标/无看板 |
| [finance/Account.vue](/Users/yang/smartcare/admin-web/src/views/finance/Account.vue) | SearchForm | 页首 | 统计条 |
| [finance/AccountLog.vue](/Users/yang/smartcare/admin-web/src/views/finance/AccountLog.vue) | SearchForm | 页首 | 统计条 |
| [finance/PrepaidRecharge.vue](/Users/yang/smartcare/admin-web/src/views/finance/PrepaidRecharge.vue) | 继承/专用 | 继承/专用 | 统计条 |
| [finance/ConsumerVoucher.vue](/Users/yang/smartcare/admin-web/src/views/finance/ConsumerVoucher.vue) | QueryBar | 工具条 | 统计条 |
| [finance/ElectricityFee.vue](/Users/yang/smartcare/admin-web/src/views/finance/ElectricityFee.vue) | QueryBar | 工具条 | 统计条 |
| [finance/DepositManagement.vue](/Users/yang/smartcare/admin-web/src/views/finance/DepositManagement.vue) | QueryBar | 工具条 | 统计条 |
| [finance/BalanceWarningRules.vue](/Users/yang/smartcare/admin-web/src/views/finance/BalanceWarningRules.vue) | QueryBar | 工具条 | 统计条 |
| [finance/AdmissionFeeAudit.vue](/Users/yang/smartcare/admin-web/src/views/finance/AdmissionFeeAudit.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [finance/ConsumptionRegister.vue](/Users/yang/smartcare/admin-web/src/views/finance/ConsumptionRegister.vue) | SearchForm | 页首 | 统计条 |
| [finance/FlowCenter.vue](/Users/yang/smartcare/admin-web/src/views/finance/FlowCenter.vue) | QueryBar | 工具条 | 统计条 |
| [finance/MonthlyAllocation.vue](/Users/yang/smartcare/admin-web/src/views/finance/MonthlyAllocation.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [finance/CollectionCenter.vue](/Users/yang/smartcare/admin-web/src/views/finance/CollectionCenter.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |
| [finance/AutoDebitManagement.vue](/Users/yang/smartcare/admin-web/src/views/finance/AutoDebitManagement.vue) | QueryBar | 工具条 | 统计条 |
| [finance/ReconcileException.vue](/Users/yang/smartcare/admin-web/src/views/finance/ReconcileException.vue) | QueryBar | 工具条 | 统计条 |
| [finance/LedgerHealth.vue](/Users/yang/smartcare/admin-web/src/views/finance/LedgerHealth.vue) | QueryBar | 工具条 | 统计条 |
| [finance/FinanceIssueCenter.vue](/Users/yang/smartcare/admin-web/src/views/finance/FinanceIssueCenter.vue) | QueryBar | 工具条 | 统计条 |
| [finance/DischargeFeeAudit.vue](/Users/yang/smartcare/admin-web/src/views/finance/DischargeFeeAudit.vue) | SearchForm | 页首 | 统计条 |
| [finance/DischargeSettlement.vue](/Users/yang/smartcare/admin-web/src/views/finance/DischargeSettlement.vue) | SearchForm | 页首 | 统计条 |
| [finance/DischargeStatusSync.vue](/Users/yang/smartcare/admin-web/src/views/finance/DischargeStatusSync.vue) | QueryBar | 工具条 | 统计条 |
| [finance/Reconcile.vue](/Users/yang/smartcare/admin-web/src/views/finance/Reconcile.vue) | QueryBar | 工具条 | 统计条 |
| [finance/ReconcileInvoice.vue](/Users/yang/smartcare/admin-web/src/views/finance/ReconcileInvoice.vue) | QueryBar | 工具条 | 统计条 |
| [finance/FinanceMonthClose.vue](/Users/yang/smartcare/admin-web/src/views/finance/FinanceMonthClose.vue) | QueryBar | 工具条 | 统计条 |
| [finance/Report.vue](/Users/yang/smartcare/admin-web/src/views/finance/Report.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [finance/FinanceMasterData.vue](/Users/yang/smartcare/admin-web/src/views/finance/FinanceMasterData.vue) | QueryBar | 工具条 | 统计条 |
| [finance/FeeSubjectDictionary.vue](/Users/yang/smartcare/admin-web/src/views/finance/FeeSubjectDictionary.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [finance/BillingRulesConfig.vue](/Users/yang/smartcare/admin-web/src/views/finance/BillingRulesConfig.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [finance/AllocationRules.vue](/Users/yang/smartcare/admin-web/src/views/finance/AllocationRules.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [finance/PaymentChannelConfig.vue](/Users/yang/smartcare/admin-web/src/views/finance/PaymentChannelConfig.vue) | QueryBar | 工具条 | 专用指标/无看板 |
| [finance/FinanceApprovalFlowConfig.vue](/Users/yang/smartcare/admin-web/src/views/finance/FinanceApprovalFlowConfig.vue) | QueryBar | 工具条 | 统计条 |
| [finance/FinanceConfigChangeLog.vue](/Users/yang/smartcare/admin-web/src/views/finance/FinanceConfigChangeLog.vue) | QueryBar | 工具条 | 统计条 |
| [hr/HrCenter.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrCenter.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |
| [hr/HrRecruitmentNeeds.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrRecruitmentNeeds.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrOnboardingWizard.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrOnboardingWizard.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |
| [hr/Staff.vue](/Users/yang/smartcare/admin-web/src/views/hr/Staff.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrProfileContracts.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrProfileContracts.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [Staff.vue](/Users/yang/smartcare/admin-web/src/views/Staff.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrProfileTemplates.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrProfileTemplates.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrContractPrint.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrContractPrint.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrProfileCertificates.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrProfileCertificates.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrProfileAttachments.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrProfileAttachments.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrContractReminder.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrContractReminder.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrSocialSecurityReminder.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrSocialSecurityReminder.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrAttendanceSchemes.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrAttendanceSchemes.vue) | 继承/专用 | 页首 | 专用指标/无看板 |
| [care/CaregiverGroups.vue](/Users/yang/smartcare/admin-web/src/views/care/CaregiverGroups.vue) | QueryBar | 继承/专用 | 专用指标/无看板 |
| [care/ShiftCalendar.vue](/Users/yang/smartcare/admin-web/src/views/care/ShiftCalendar.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrAttendanceApprovalPage.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrAttendanceApprovalPage.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrAttendanceRecords.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrAttendanceRecords.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrAttendanceAbnormal.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrAttendanceAbnormal.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrAccessControl.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrAccessControl.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrCardSync.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrCardSync.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrPolicies.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrPolicies.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrPolicyAlerts.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrPolicyAlerts.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrExpenseScenePage.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrExpenseScenePage.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrDormitoryManagement.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrDormitoryManagement.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrDormitoryMap.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrDormitoryMap.vue) | SearchForm | 页首 | 统计条 |
| [hr/HrExpenseMealFee.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrExpenseMealFee.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrExpenseElectricityFee.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrExpenseElectricityFee.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrExpenseApprovalFlow.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrExpenseApprovalFlow.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/Training.vue](/Users/yang/smartcare/admin-web/src/views/hr/Training.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/HrCertificateReminders.vue](/Users/yang/smartcare/admin-web/src/views/hr/HrCertificateReminders.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/PointsRule.vue](/Users/yang/smartcare/admin-web/src/views/hr/PointsRule.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/Performance.vue](/Users/yang/smartcare/admin-web/src/views/hr/Performance.vue) | SearchForm | 页首 | 统计条 |
| [oa/RewardPunishment.vue](/Users/yang/smartcare/admin-web/src/views/oa/RewardPunishment.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [hr/Points.vue](/Users/yang/smartcare/admin-web/src/views/hr/Points.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [base-config/Index.vue](/Users/yang/smartcare/admin-web/src/views/base-config/Index.vue) | QueryBar | 页首 | 专用指标/无看板 |
| [System/SiteConfigCenter.vue](/Users/yang/smartcare/admin-web/src/views/System/SiteConfigCenter.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |
| [System/OrgInfo.vue](/Users/yang/smartcare/admin-web/src/views/System/OrgInfo.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [System/RoleManage.vue](/Users/yang/smartcare/admin-web/src/views/System/RoleManage.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [System/DepartmentManage.vue](/Users/yang/smartcare/admin-web/src/views/System/DepartmentManage.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [marketing/components/LeadPipelineTable.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/LeadPipelineTable.vue) | SearchForm | 继承/专用 | 专用指标/无看板 |
| [marketing/components/MarketingListToolbar.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingListToolbar.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |
| [marketing/components/MarketingTraceTimelineCard.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingTraceTimelineCard.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |
| [marketing/components/MarketingQuickNav.vue](/Users/yang/smartcare/admin-web/src/views/marketing/components/MarketingQuickNav.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |
| [bed/Map.vue](/Users/yang/smartcare/admin-web/src/views/bed/Map.vue) | QueryBar | 工具条 | 统计条 |
| [bed/components/FloorPlanBoard.vue](/Users/yang/smartcare/admin-web/src/views/bed/components/FloorPlanBoard.vue) | 继承/专用 | 工具条 | 统计条 |
| [finance/components/ReminderPanel.vue](/Users/yang/smartcare/admin-web/src/views/finance/components/ReminderPanel.vue) | QueryBar | 工具条 | 统计条 |
| [finance/components/CollectionFollowUpPanel.vue](/Users/yang/smartcare/admin-web/src/views/finance/components/CollectionFollowUpPanel.vue) | QueryBar | 工具条 | 统计条 |
| [finance/components/ExcelReportPanel.vue](/Users/yang/smartcare/admin-web/src/views/finance/components/ExcelReportPanel.vue) | QueryBar | 继承/专用 | 专用指标/无看板 |
| [Portal.vue](/Users/yang/smartcare/admin-web/src/views/Portal.vue) | 继承/专用 | 页首 | 专用指标/无看板 |
| [System/SystemAdminHome.vue](/Users/yang/smartcare/admin-web/src/views/System/SystemAdminHome.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |
| [compliance/SensitiveAccessAudit.vue](/Users/yang/smartcare/admin-web/src/views/compliance/SensitiveAccessAudit.vue) | QueryBar | 页首 | 统计条 |
| [compliance/ExportAudit.vue](/Users/yang/smartcare/admin-web/src/views/compliance/ExportAudit.vue) | QueryBar | 页首 | 专用指标/无看板 |
| [compliance/SecurityPolicyConfig.vue](/Users/yang/smartcare/admin-web/src/views/compliance/SecurityPolicyConfig.vue) | 继承/专用 | 页首 | 专用指标/无看板 |
| [oa/Document.vue](/Users/yang/smartcare/admin-web/src/views/oa/Document.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [card/Account.vue](/Users/yang/smartcare/admin-web/src/views/card/Account.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [card/Recharge.vue](/Users/yang/smartcare/admin-web/src/views/card/Recharge.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [card/Consume.vue](/Users/yang/smartcare/admin-web/src/views/card/Consume.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [oa/Approval.vue](/Users/yang/smartcare/admin-web/src/views/oa/Approval.vue) | SearchForm | 页首 | 统计条 |
| [oa/WorkReport.vue](/Users/yang/smartcare/admin-web/src/views/oa/WorkReport.vue) | SearchForm | 页首 | 专用指标/无看板 |
| [oa/DailyReport.vue](/Users/yang/smartcare/admin-web/src/views/oa/DailyReport.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |
| [oa/WeeklyReport.vue](/Users/yang/smartcare/admin-web/src/views/oa/WeeklyReport.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |
| [oa/MonthlyReport.vue](/Users/yang/smartcare/admin-web/src/views/oa/MonthlyReport.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |
| [oa/YearlyReport.vue](/Users/yang/smartcare/admin-web/src/views/oa/YearlyReport.vue) | 继承/专用 | 继承/专用 | 专用指标/无看板 |

## 功能和权限验证

| 检查 | 结果 |
|---|---|
| 全量单元测试 `npm --prefix admin-web run test:unit -- --reporter=dot` | 43个文件，359项通过 |
| TypeScript `npm --prefix admin-web run typecheck` | 通过 |
| ESLint `npm --prefix admin-web run lint` | 通过 |
| 生产构建 `npm --prefix admin-web run build` | 通过；现有依赖分包仍有超过500kB的提示 |
| 导航 `npm --prefix admin-web run check:navigation` | 637条路由、366个链接，0个悬空链接 |
| 补丁格式 `git diff --check` | 通过 |
| Playwright浏览器 | 39个场景，无捕获的页面JavaScript异常，无根视口横向溢出 |

新增测试覆盖：筛选展开不改条件、恢复初始值、查询只触发一次、表单提交及长者自动搜索事件、次要按钮原事件、禁用状态响应更新、权限指令移入更多操作后仍生效、取消选择通过原回调执行且不直接修改父级数据。既有测试继续覆盖业务结算、旧入口兼容、不同视图原路由身份、岗位菜单、权限边界及窄授权。

浏览器检查覆盖四类岗位首页、账单与常用列切换、客户筛选、合同、组织权限、基础数据导入入口、Excel分支、自动扣费、财务异常、月结、交班、规则、营销方案、入职、护理班组、员工费用、宿舍、安全策略及审计、文档、卡务、审批、工作总结、官网配置；另检查同页切换、4种390px布局，以及HR仅获社保授权时可进入社保、伪造账号视图返回403。营销方案回车只查询一次，不打开新增弹窗；独立查询可展开/收起；员工电费的同步财务、打印、导出入口仍可找到。

[完整浏览器结果](/Users/yang/smartcare/output/business-layout-2026-11-15/browser-results.json) · [可复跑的隔离检查脚本](/Users/yang/smartcare/output/business-layout-2026-11-15/browser-check.cjs)

| 界面预览（隔离模拟数据） | 链接 |
|---|---|
| 财务异常与手机查询 | [桌面](/Users/yang/smartcare/output/business-layout-2026-11-15/finance-issue.png) / [390px](/Users/yang/smartcare/output/business-layout-2026-11-15/finance-query-mobile.png) |
| 员工电费与手机查询 | [桌面](/Users/yang/smartcare/output/business-layout-2026-11-15/hr-electricity.png) / [390px](/Users/yang/smartcare/output/business-layout-2026-11-15/hr-query-mobile.png) |
| 市场转化分析 | [预览](/Users/yang/smartcare/output/business-layout-2026-11-15/market-conversion.png) |
| 营销方案 | [预览](/Users/yang/smartcare/output/business-layout-2026-11-15/market-plan.png) |
| 组织与权限 | [预览](/Users/yang/smartcare/output/business-layout-2026-11-15/organization.png) |
| 审批办理 | [预览](/Users/yang/smartcare/output/business-layout-2026-11-15/approvals.png) |

## 验证边界

浏览器使用本地Vite、独立账号上下文和拦截的模拟API数据。前端回归证明已检查路径的显示、事件接线、旧链接兼容和前端权限符合预期；没有执行真实收退款、关账、审批、合同签署和生产接口对账，不能代替真实账号的业务验收。本轮布局修改未变更后端接口、数据库和服务端权限策略。
