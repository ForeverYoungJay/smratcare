import type { RouteRecordRaw } from 'vue-router'

export const financeRoutes: RouteRecordRaw[] = [
  {
        path: 'finance',
        name: 'Finance',
        meta: { title: '财务运营中心', icon: 'AccountBookOutlined', navSection: 'operations', navOrder: 60, navPinned: true, roles: ['FINANCE_EMPLOYEE', 'FINANCE_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
        children: [
          {
            path: '',
            name: 'FinanceHub',
            component: () => import('../views/ModuleHub.vue'),
            meta: { title: '财务导航', hidden: true }
          },
          {
            path: 'workbench',
            name: 'FinanceWorkbench',
            component: () => import('../views/finance/FinanceWorkbench.vue'),
            meta: { title: '今日工作台' }
          },
          {
            path: 'search',
            name: 'FinanceSearch',
            component: () => import('../views/finance/FinanceSearch.vue'),
            meta: { title: '财务搜索', hidden: true }
          },
          {
            path: 'payments',
            name: 'FinancePayments',
            redirect: '/finance/payments/cashier-desk',
            meta: { title: '收费与收款' },
            children: [
              {
                path: 'cashier-desk',
                name: 'FinancePaymentsCashierDesk',
                component: () => import('../views/finance/CashierDesk.vue'),
                meta: { title: '收银台' }
              },
              {
                path: 'register',
                name: 'FinancePaymentsRegister',
                component: () => import('../views/finance/ResidentBillPayment.vue'),
                meta: { title: '收费登记' }
              },
              {
                path: 'records',
                name: 'FinancePaymentsRecords',
                component: () => import('../views/finance/PaymentRecords.vue'),
                meta: { title: '收款流水' }
              },
              {
                path: '/finance/fees/payment-and-invoice',
                name: 'FinancePaymentsInvoice',
                component: () => import('../views/finance/InvoiceReceiptManagement.vue'),
                meta: { title: '发票与收据' }
              },
              {
                path: '/finance/payments/refund-reversal',
                name: 'FinancePaymentsRefundReversal',
                component: () => import('../views/finance/PaymentRefundReversal.vue'),
                meta: { title: '退款与冲正' }
              },
              {
                path: '/finance/payments/shift-close',
                name: 'FinancePaymentsShiftClose',
                component: () => import('../views/finance/ShiftClose.vue'),
                meta: { title: '日结与交班' }
              }
            ]
          },
          {
            path: 'billing',
            name: 'FinanceBilling',
            redirect: '/finance/bills/in-resident',
            meta: { title: '账单与账户' },
            children: [
              {
                path: '/finance/bills/in-admission',
                name: 'FinanceBillsInAdmission',
                component: () => import('../views/finance/AdmissionBillPayment.vue'),
                meta: { title: '入住账单' }
              },
              {
                path: '/finance/bills/in-resident',
                name: 'FinanceBillsInResident',
                component: () => import('../views/finance/ResidentBillPayment.vue'),
                meta: { title: '在住周期账单' }
              },
              {
                path: '/finance/bills/detail-query',
                name: 'FinanceBillsDetailQuery',
                component: () => import('../views/finance/BillCenter.vue'),
                meta: { title: '账单查询' },
                props: {
                  title: '账单详情查询',
                  subTitle: '按老人、月份检索账单后进入详情页',
                  defaultCurrentMonth: true
                }
              },
              {
                path: '/finance/accounts/list',
                name: 'FinanceAccountsList',
                component: () => import('../views/finance/Account.vue'),
                meta: { title: '长者账户列表' }
              },
              {
                path: '/finance/accounts/ledger',
                name: 'FinanceAccountsLedger',
                component: () => import('../views/finance/AccountLog.vue'),
                meta: { title: '账户流水' }
              },
              {
                path: '/finance/prepaid-recharge',
                name: 'FinancePrepaidRecharge',
                component: () => import('../views/finance/PrepaidRecharge.vue'),
                meta: { title: '预存充值' }
              },
              {
                path: '/finance/consumer-voucher',
                name: 'FinanceConsumerVoucher',
                component: () => import('../views/finance/ConsumerVoucher.vue'),
                meta: { title: '消费券管理' }
              },
              {
                path: '/finance/electricity-fee',
                name: 'FinanceElectricityFee',
                component: () => import('../views/finance/ElectricityFee.vue'),
                meta: { title: '电费管理' }
              },
              {
                path: '/finance/deposit-management',
                name: 'FinanceDepositManagement',
                component: () => import('../views/finance/DepositManagement.vue'),
                meta: { title: '押金管理' }
              },
              {
                // 提醒中心已与催缴跟进合并为「欠费与提醒中心」，旧入口保留为重定向
                path: '/finance/reminder-center',
                name: 'FinanceReminderCenter',
                redirect: { path: '/finance/bills/follow-up', query: { tab: 'reminder' } },
                meta: { title: '财务提醒中心（已并入欠费与提醒中心）', hidden: true }
              },
              {
                // Excel 报表已并入经营分析的标签导航，旧入口保留为重定向
                path: '/finance/excel-reports',
                name: 'FinanceExcelReports',
                redirect: '/finance/reports/excel',
                meta: { title: 'Excel 报表（已并入经营分析）', hidden: true }
              },
              {
                path: '/finance/accounts/warning-rules',
                name: 'FinanceAccountsWarningRules',
                component: () => import('../views/finance/BalanceWarningRules.vue'),
                meta: { title: '余额预警规则' }
              },
              {
                path: '/finance/admission-fee-audit',
                name: 'FinanceAdmissionFeeAudit',
                component: () => import('../views/finance/AdmissionFeeAudit.vue'),
                meta: { title: '入住费用审核' }
              }
            ]
          },
          {
            path: 'sources',
            name: 'FinanceSources',
            redirect: '/finance/flows/consumption',
            meta: { title: '消费与分摊' },
            children: [
              {
                path: '/finance/flows/consumption',
                name: 'FinanceFlowsConsumption',
                component: () => import('../views/finance/ConsumptionRegister.vue'),
                meta: { title: '消费登记' }
              },
              {
                path: '/finance/flows/medical',
                name: 'FinanceFlowsMedical',
                component: () => import('../views/finance/FlowCenter.vue'),
                meta: { title: '医护费用流水' },
                props: {
                  moduleKey: 'MEDICAL_FLOW',
                  moduleName: '医护费用流水',
                  description: '按医嘱、用药、治疗与检查动作汇总收费流水',
                  category: 'MEDICINE',
                  links: [
                    { label: '查看医护费用异常', to: '/finance/flows/medical-errors' },
                    { label: '查看费用调整单', to: '/finance/flows/adjustments' }
                  ]
                }
              },
              {
                path: '/finance/flows/medical-errors',
                name: 'FinanceFlowsMedicalErrors',
                component: () => import('../views/finance/FlowCenter.vue'),
                meta: { title: '医护费用异常' },
                props: {
                  moduleKey: 'MEDICAL_ERRORS',
                  moduleName: '医护费用异常',
                  description: '重复计费、缺少医嘱关联、异常修正与重算',
                  category: 'MEDICINE',
                  links: [
                    { label: '查看医护费用流水', to: '/finance/flows/medical' },
                    { label: '查看费用调整单', to: '/finance/flows/adjustments' }
                  ]
                }
              },
              {
                path: '/finance/flows/dining',
                name: 'FinanceFlowsDining',
                component: () => import('../views/finance/FlowCenter.vue'),
                meta: { title: '餐饮扣费流水' },
                props: {
                  moduleKey: 'DINING_FLOW',
                  moduleName: '餐饮扣费流水',
                  description: '积分点餐扣费、餐饮补扣与异常追踪',
                  category: 'DINING',
                  links: [
                    { label: '查看消费流水', to: '/finance/flows/consumption' },
                    { label: '查看在住账单', to: '/finance/bills/in-resident' }
                  ]
                }
              },
              {
                path: '/finance/flows/logistics',
                name: 'FinanceFlowsLogistics',
                component: () => import('../views/finance/FlowCenter.vue'),
                meta: { title: '物资/后勤收费流水' },
                props: {
                  moduleKey: 'LOGISTICS_FLOW',
                  moduleName: '物资/后勤收费流水',
                  description: '物资与后勤额外收费项目的自动入账视图',
                  category: 'OTHER',
                  links: [
                    { label: '查看消费流水', to: '/finance/flows/consumption' },
                    { label: '查看经营报表', to: '/finance/reports/overall' }
                  ]
                }
              },
              {
                path: '/finance/flows/adjustments',
                name: 'FinanceFlowsAdjustments',
                component: () => import('../views/finance/FlowCenter.vue'),
                meta: { title: '费用调整单' },
                props: {
                  moduleKey: 'ADJUSTMENTS',
                  moduleName: '费用调整单',
                  description: '减免、补录、改价调整申请与审批跟踪',
                  links: [
                    { label: '查看审批中心', to: '/oa/approval?module=finance&status=pending' },
                    { label: '查看退住审核', to: '/finance/discharge/review?status=pending' }
                  ]
                }
              },
              {
                path: '/finance/allocation/public-cost',
                name: 'FinanceAllocationPublicCost',
                component: () => import('../views/finance/MonthlyAllocation.vue'),
                meta: { title: '公共费用计算' }
              },
              {
                path: '/finance/allocation/tasks',
                name: 'FinanceAllocationTasks',
                component: () => import('../views/finance/MonthlyAllocation.vue'),
                meta: { title: '分摊入账任务' }
              }
            ]
          },
          {
            path: 'risk',
            name: 'FinanceRisk',
            redirect: '/finance/bills/follow-up',
            meta: { title: '欠费与异常' },
            children: [
              {
                path: '/finance/bills/follow-up',
                name: 'FinanceBillsFollowUp',
                component: () => import('../views/finance/CollectionCenter.vue'),
                meta: { title: '欠费与提醒中心' }
              },
              {
                path: '/finance/bills/auto-deduct',
                name: 'FinanceBillsAutoDeduct',
                component: () => import('../views/finance/AutoDebitManagement.vue'),
                meta: { title: '自动扣费与催缴' }
              },
              {
                path: '/finance/bills/auto-deduct-errors',
                name: 'FinanceBillsAutoDeductErrors',
                component: () => import('../views/finance/AutoDebitManagement.vue'),
                meta: { title: '自动扣费异常' }
              },
              {
                path: '/finance/reconcile/exception',
                name: 'FinanceReconcileException',
                component: () => import('../views/finance/ReconcileException.vue'),
                meta: { title: '对账异常处理' }
              },
              {
                path: '/finance/reconcile/ledger-health',
                name: 'FinanceReconcileLedgerHealth',
                component: () => import('../views/finance/LedgerHealth.vue'),
                meta: { title: '财务一致性巡检' }
              },
              {
                path: '/finance/reconcile/issue-center',
                name: 'FinanceReconcileIssueCenter',
                component: () => import('../views/finance/FinanceIssueCenter.vue'),
                meta: { title: '异常修复中心' }
              }
            ]
          },
          {
            path: 'discharge',
            name: 'FinanceDischarge',
            redirect: '/finance/discharge/review',
            meta: { title: '结算与月结' },
            children: [
              {
                path: 'review',
                name: 'FinanceDischargeReview',
                component: () => import('../views/finance/DischargeFeeAudit.vue'),
                meta: { title: '退住审核' }
              },
              {
                path: 'settlement',
                name: 'FinanceDischargeSettlementCenter',
                component: () => import('../views/finance/DischargeSettlement.vue'),
                meta: { title: '退住结算' }
              },
              {
                path: 'print-sign',
                name: 'FinanceDischargePrintSign',
                component: () => import('../views/finance/DischargeSettlement.vue'),
                meta: { title: '结算单打印' }
              },
              {
                path: 'status-sync',
                name: 'FinanceDischargeStatusSync',
                component: () => import('../views/finance/DischargeStatusSync.vue'),
                meta: { title: '结算状态回写' }
              },
              {
                path: '/finance/reconcile/center',
                name: 'FinanceReconcileCenter',
                component: () => import('../views/finance/Reconcile.vue'),
                meta: { title: '对账中心' }
              },
              {
                path: '/finance/reconcile/invoice',
                name: 'FinanceReconcileInvoice',
                component: () => import('../views/finance/ReconcileInvoice.vue'),
                meta: { title: '发票对账' }
              },
              {
                path: '/finance/reconcile/month-close',
                name: 'FinanceReconcileMonthClose',
                component: () => import('../views/finance/FinanceMonthClose.vue'),
                meta: { title: '月结进度' }
              }
            ]
          },
          {
            path: 'reports',
            name: 'FinanceReports',
            redirect: '/finance/reports/overall',
            meta: { title: '经营分析' },
            children: [
              {
                path: 'overall',
                name: 'FinanceReportsOverall',
                component: () => import('../views/finance/Report.vue'),
                meta: { title: '总收支' }
              },
              {
                path: 'revenue-structure',
                name: 'FinanceReportsRevenueStructure',
                component: () => import('../views/finance/Report.vue'),
                meta: { title: '营收结构' }
              },
              {
                path: 'floor-room-profit',
                name: 'FinanceReportsFloorRoom',
                component: () => import('../views/finance/Report.vue'),
                meta: { title: '楼层房间收支' }
              },
              {
                path: 'occupancy-consumption',
                name: 'FinanceReportsOccupancyConsumption',
                component: () => import('../views/finance/Report.vue'),
                meta: { title: '入住与消费' }
              },
              {
                path: 'excel',
                name: 'FinanceReportsExcel',
                component: () => import('../views/finance/Report.vue'),
                meta: { title: 'Excel 报表' }
              },
              {
                path: 'monthly-ops',
                name: 'FinanceReportsMonthlyOps',
                component: () => import('../views/finance/Report.vue'),
                meta: { title: '机构月运营' }
              }
            ]
          },
          {
            path: 'config',
            name: 'FinanceConfig',
            redirect: '/finance/config/master-data',
            meta: { title: '规则与配置' },
            children: [
              {
                path: 'master-data',
                name: 'FinanceConfigMasterData',
                component: () => import('../views/finance/FinanceMasterData.vue'),
                meta: { title: '主数据配置中心' }
              },
              {
                path: '/finance/allocation/subjects',
                name: 'FinanceConfigFeeSubjects',
                component: () => import('../views/finance/FeeSubjectDictionary.vue'),
                meta: { title: '费用科目' }
              },
              {
                path: '/finance/bills/rules',
                name: 'FinanceBillsRules',
                component: () => import('../views/finance/BillingRulesConfig.vue'),
                meta: { title: '计费规则' }
              },
              {
                path: '/finance/allocation/rules',
                name: 'FinanceAllocationRules',
                component: () => import('../views/finance/AllocationRules.vue'),
                meta: { title: '分摊规则' }
              },
              {
                path: 'payment-channels',
                name: 'FinanceConfigPaymentChannels',
                component: () => import('../views/finance/PaymentChannelConfig.vue'),
                meta: { title: '支付渠道' }
              },
              {
                path: 'approval-flow',
                name: 'FinanceConfigApprovalFlow',
                component: () => import('../views/finance/FinanceApprovalFlowConfig.vue'),
                meta: { title: '审批与权限' }
              },
              {
                path: 'change-log',
                name: 'FinanceConfigChangeLog',
                component: () => import('../views/finance/FinanceConfigChangeLog.vue'),
                meta: { title: '配置变更记录' }
              }
            ]
          },
          {
            path: 'accounts',
            name: 'FinanceAccountsCompat',
            redirect: '/finance/accounts/list',
            meta: { title: '账户与预存（兼容）', hidden: true }
          },
          {
            path: 'bills',
            name: 'FinanceBillsCompat',
            redirect: '/finance/bills/in-resident',
            meta: { title: '账单与账户（兼容）', hidden: true }
          },
          {
            path: 'flows',
            name: 'FinanceFlowsCompat',
            redirect: '/finance/flows/consumption',
            meta: { title: '消费与分摊（兼容）', hidden: true }
          },
          {
            path: 'fees',
            name: 'FinanceFeesCompat',
            redirect: '/finance/fees/payment-and-invoice',
            meta: { title: '收费与票据（兼容）', hidden: true }
          },
          {
            path: 'allocation',
            name: 'FinanceAllocationCompat',
            redirect: '/finance/allocation/public-cost',
            meta: { title: '公共费用分摊（兼容）', hidden: true }
          },
          {
            path: 'reconcile',
            name: 'FinanceReconcileCompat',
            redirect: '/finance/reconcile/center',
            meta: { title: '欠费与异常（兼容）', hidden: true }
          },
          {
            path: 'admission-bill-payment',
            name: 'FinanceAdmissionBillPayment',
            redirect: '/finance/bills/in-admission',
            meta: { title: '入住账单缴费（兼容）', hidden: true }
          },
          {
            path: 'resident-bill-payment',
            name: 'FinanceResidentBillPayment',
            redirect: '/finance/bills/in-resident',
            meta: { title: '在住账单缴费（兼容）', hidden: true }
          },
          {
            path: 'monthly-allocation',
            name: 'FinanceMonthlyAllocation',
            redirect: '/finance/allocation/public-cost',
            meta: { title: '月分摊费（兼容）', hidden: true }
          },
          {
            path: 'discharge-fee-audit',
            name: 'FinanceDischargeFeeAudit',
            redirect: '/finance/discharge/review',
            meta: { title: '退住费用审核（兼容）', hidden: true }
          },
          {
            path: 'finance-settlement',
            name: 'FinanceSettlement',
            redirect: '/finance/reconcile/center',
            meta: { title: '财务结算（兼容）', hidden: true }
          },
          {
            path: 'discharge-settlement',
            name: 'FinanceDischargeSettlement',
            redirect: '/finance/discharge/settlement',
            meta: { title: '退住结算（兼容）', hidden: true }
          },
          {
            path: 'medical-care-ledger',
            name: 'FinanceMedicalCareLedger',
            redirect: '/finance/flows/medical',
            meta: { title: '医护费用流水（兼容）', hidden: true }
          },
          {
            path: 'consumption-register',
            name: 'FinanceConsumptionRegister',
            redirect: '/finance/flows/consumption',
            meta: { title: '消费登记（兼容）', hidden: true }
          },
          {
            path: 'bill/:billId',
            name: 'FinanceBillDetail',
            component: () => import('../views/finance/BillDetail.vue'),
            meta: { title: '账单详情', hidden: true }
          },
          {
            path: 'report',
            name: 'FinanceReport',
            redirect: '/finance/reports/overall',
            meta: { title: '财务报表（兼容）', hidden: true }
          },
          {
            path: 'account',
            name: 'FinanceAccount',
            redirect: '/finance/accounts/list',
            meta: { title: '老人账户（兼容）', hidden: true }
          },
          {
            path: 'account-log',
            name: 'FinanceAccountLog',
            redirect: '/finance/accounts/ledger',
            meta: { title: '账户流水（兼容）', hidden: true }
          },
          {
            path: 'bill',
            redirect: '/finance/bills/in-resident',
            meta: { hidden: true }
          },
          {
            path: 'reconcile-center',
            redirect: '/finance/reconcile/center',
            meta: { hidden: true }
          },
          {
            path: 'reconcile-invoice',
            redirect: '/finance/reconcile/invoice',
            meta: { hidden: true }
          },
          {
            path: 'reconcile-exception',
            redirect: '/finance/reconcile/exception',
            meta: { hidden: true }
          }
        ]
      }
]
