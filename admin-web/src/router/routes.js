export const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: { title: '登录', hidden: true }
    },
    {
        path: '/403',
        name: 'Forbidden',
        component: () => import('../views/Forbidden.vue'),
        meta: { title: '无权限', hidden: true }
    },
    {
        path: '/',
        name: 'Root',
        component: () => import('../layouts/BasicLayout.vue'),
        redirect: '/portal',
        children: [
            {
                path: 'portal',
                name: 'Portal',
                component: () => import('../views/Portal.vue'),
                meta: { title: '首页', icon: 'HomeOutlined' }
            },
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('../views/Dashboard.vue'),
                meta: { title: 'Dashboard', icon: 'DashboardOutlined', hidden: true }
            },
            {
                path: 'elder',
                name: 'Elder',
                meta: { title: '长者管理 Resident 360', icon: 'TeamOutlined' },
                redirect: '/elder/resident-360',
                children: [
                    {
                        path: 'resident-360',
                        name: 'ElderResident360',
                        component: () => import('../views/elder/resident360/Resident360.vue'),
                        meta: { title: 'Resident 360', hidden: true }
                    },
                    {
                        path: 'bed-panorama',
                        name: 'ElderBedPanorama',
                        component: () => import('../views/elder/resident360/BedPanorama.vue'),
                        meta: { title: '床态全景' }
                    },
                    {
                        path: 'list',
                        name: 'ElderList',
                        component: () => import('../views/elder/List.vue'),
                        meta: { title: '长者列表' }
                    },
                    {
                        path: 'admission-assessment',
                        name: 'ElderAdmissionAssessment',
                        component: () => import('../views/elder/resident360/AdmissionAssessment.vue'),
                        meta: { title: '入住评估' }
                    },
                    {
                        path: 'contracts-invoices',
                        name: 'ElderContractsInvoices',
                        component: () => import('../views/elder/resident360/ContractsInvoices.vue'),
                        meta: { title: '合同与票据' }
                    },
                    {
                        path: 'admission-processing',
                        name: 'ElderAdmissionProcessing',
                        component: () => import('../views/elder/Admission.vue'),
                        meta: { title: '入住办理' }
                    },
                    {
                        path: 'in-hospital-overview',
                        name: 'ElderInHospitalOverview',
                        component: () => import('../views/elder/resident360/InHospitalOverview.vue'),
                        meta: { title: '在院服务总览' }
                    },
                    {
                        path: 'status-change',
                        name: 'ElderStatusChangeCenter',
                        component: () => import('../views/elder/resident360/StatusChangeCenter.vue'),
                        meta: { title: '入住状态变更' }
                    },
                    {
                        path: 'discharge-settlement',
                        name: 'ElderDischargeSettlement',
                        component: () => import('../views/elder/resident360/DischargeSettlement.vue'),
                        meta: { title: '退院结算' }
                    },
                    {
                        path: 'create',
                        name: 'ElderCreate',
                        component: () => import('../views/elder/Create.vue'),
                        meta: { title: '新增老人', hidden: true }
                    },
                    {
                        path: 'edit/:id',
                        name: 'ElderEdit',
                        component: () => import('../views/elder/Edit.vue'),
                        meta: { title: '编辑老人', hidden: true }
                    },
                    {
                        path: 'detail/:id',
                        name: 'ElderDetail',
                        component: () => import('../views/elder/Detail.vue'),
                        meta: { title: '老人详情', hidden: true }
                    },
                    {
                        path: 'crm',
                        name: 'ElderCrm',
                        component: () => import('../views/elder/Crm.vue'),
                        meta: { title: 'CRM线索(旧)', hidden: true }
                    },
                    {
                        path: 'admission',
                        name: 'ElderAdmission',
                        redirect: '/elder/admission-processing',
                        meta: { hidden: true }
                    },
                    {
                        path: 'outing',
                        name: 'ElderOuting',
                        component: () => import('../views/elder/Outing.vue'),
                        meta: { title: '外出登记' }
                    },
                    {
                        path: 'visit-register',
                        name: 'ElderVisitRegister',
                        component: () => import('../views/elder/VisitRegister.vue'),
                        meta: { title: '来访登记' }
                    },
                    {
                        path: 'incident',
                        name: 'ElderIncident',
                        component: () => import('../views/life/Incident.vue'),
                        meta: { title: '事故登记', hidden: true }
                    },
                    {
                        path: 'discharge-apply',
                        name: 'ElderDischargeApply',
                        component: () => import('../views/elder/DischargeApply.vue'),
                        meta: { title: '退住申请' }
                    },
                    {
                        path: 'discharge',
                        name: 'ElderDischarge',
                        component: () => import('../views/elder/Discharge.vue'),
                        meta: { title: '退住登记', hidden: true }
                    },
                    {
                        path: 'trial-stay',
                        name: 'ElderTrialStay',
                        component: () => import('../views/elder/TrialStay.vue'),
                        meta: { title: '试住登记' }
                    },
                    {
                        path: 'medical-outing',
                        name: 'ElderMedicalOutingRegister',
                        component: () => import('../views/elder/MedicalOuting.vue'),
                        meta: { title: '外出就医登记', roles: ['ADMIN', 'STAFF'] }
                    },
                    {
                        path: 'death-register',
                        name: 'ElderDeathRegister',
                        component: () => import('../views/elder/DeathRegister.vue'),
                        meta: { title: '死亡登记', roles: ['ADMIN', 'STAFF'] }
                    },
                    {
                        path: 'change-log',
                        name: 'ElderChangeLog',
                        component: () => import('../views/elder/ChangeLog.vue'),
                        meta: { title: '变更记录', hidden: true }
                    }
                ]
            },
            {
                path: 'marketing',
                name: 'Marketing',
                meta: { title: '营销管理', icon: 'FundProjectionScreenOutlined' },
                redirect: '/marketing/sales/consultation',
                children: [
                    {
                        path: 'sales',
                        name: 'MarketingSales',
                        meta: { title: '销售跟进' },
                        redirect: '/marketing/sales/consultation',
                        children: [
                            {
                                path: 'consultation',
                                name: 'MarketingSalesConsultation',
                                component: () => import('../views/marketing/SalesConsultation.vue'),
                                meta: { title: '咨询管理' }
                            },
                            {
                                path: 'intent',
                                name: 'MarketingSalesIntent',
                                component: () => import('../views/marketing/SalesIntent.vue'),
                                meta: { title: '意向客户' }
                            },
                            {
                                path: 'reservation',
                                name: 'MarketingSalesReservation',
                                component: () => import('../views/marketing/SalesReservation.vue'),
                                meta: { title: '预订管理' }
                            },
                            {
                                path: 'invalid',
                                name: 'MarketingSalesInvalid',
                                component: () => import('../views/marketing/SalesInvalid.vue'),
                                meta: { title: '失效用户' }
                            },
                            {
                                path: 'callback',
                                name: 'MarketingSalesCallback',
                                component: () => import('../views/marketing/SalesCallback.vue'),
                                meta: { title: '待回访提醒' }
                            }
                        ]
                    },
                    {
                        path: 'room-panorama',
                        name: 'MarketingRoomPanorama',
                        component: () => import('../views/marketing/RoomPanorama.vue'),
                        meta: { title: '房态全景' }
                    },
                    {
                        path: 'contract-signing',
                        name: 'MarketingContractSigning',
                        component: () => import('../views/marketing/ContractSigning.vue'),
                        meta: { title: '合同签约' }
                    },
                    {
                        path: 'contract-management',
                        name: 'MarketingContractManagement',
                        component: () => import('../views/marketing/ContractManagement.vue'),
                        meta: { title: '合同管理' }
                    },
                    {
                        path: 'plan',
                        name: 'MarketingPlan',
                        component: () => import('../views/marketing/MarketingPlan.vue'),
                        meta: { title: '营销方案' }
                    },
                    {
                        path: 'reports',
                        name: 'MarketingReports',
                        meta: { title: '销售报表' },
                        redirect: '/marketing/reports/conversion',
                        children: [
                            {
                                path: 'conversion',
                                name: 'MarketingReportConversion',
                                component: () => import('../views/marketing/report/Conversion.vue'),
                                meta: { title: '转换率统计' }
                            },
                            {
                                path: 'consultation',
                                name: 'MarketingReportConsultation',
                                component: () => import('../views/marketing/report/Consultation.vue'),
                                meta: { title: '咨询统计' }
                            },
                            {
                                path: 'channel',
                                name: 'MarketingReportChannel',
                                component: () => import('../views/marketing/report/Channel.vue'),
                                meta: { title: '来源渠道统计' }
                            },
                            {
                                path: 'followup',
                                name: 'MarketingReportFollowup',
                                component: () => import('../views/marketing/report/Followup.vue'),
                                meta: { title: '营销跟进统计' }
                            },
                            {
                                path: 'callback',
                                name: 'MarketingReportCallback',
                                component: () => import('../views/marketing/report/Callback.vue'),
                                meta: { title: '回访统计' }
                            }
                        ]
                    },
                ]
            },
            {
                path: 'bed',
                name: 'Bed',
                meta: { title: '床位房态', icon: 'HomeOutlined', hidden: true },
                redirect: '/bed/map',
                children: [
                    {
                        path: 'map',
                        name: 'BedMap',
                        component: () => import('../views/bed/Map.vue'),
                        meta: { title: '房态图' }
                    },
                    {
                        path: 'manage',
                        name: 'BedManage',
                        component: () => import('../views/bed/Manage.vue'),
                        meta: { title: '床位管理' }
                    }
                ]
            },
            {
                path: 'care',
                name: 'Care',
                meta: { title: '照护管理', icon: 'ScheduleOutlined', hidden: true },
                redirect: '/care/staff/caregiver-info',
                children: [
                    {
                        path: 'staff',
                        name: 'CareStaff',
                        meta: { title: '人员管理' },
                        redirect: '/care/staff/caregiver-info',
                        children: [
                            {
                                path: 'caregiver-info',
                                name: 'CaregiverInfo',
                                component: () => import('../views/hr/Staff.vue'),
                                props: { title: '护工信息', subTitle: '照护团队成员档案与用工信息' },
                                meta: { title: '护工信息' }
                            },
                            {
                                path: 'caregiver-groups',
                                name: 'CaregiverGroups',
                                component: () => import('../views/care/CaregiverGroups.vue'),
                                meta: { title: '护工小组' }
                            }
                        ]
                    },
                    {
                        path: 'service',
                        name: 'CareService',
                        meta: { title: '服务管理' },
                        redirect: '/care/service/service-items',
                        children: [
                            {
                                path: 'service-items',
                                name: 'CareServiceItems',
                                component: () => import('../views/care/ServiceItems.vue'),
                                meta: { title: '服务项目' }
                            },
                            {
                                path: 'care-levels',
                                name: 'CareLevels',
                                component: () => import('../views/care/CareLevels.vue'),
                                meta: { title: '护理等级' }
                            },
                            {
                                path: 'service-plans',
                                name: 'ServicePlans',
                                component: () => import('../views/care/ServicePlans.vue'),
                                meta: { title: '服务计划' }
                            },
                            {
                                path: 'nursing-reports',
                                name: 'CareNursingReports',
                                component: () => import('../views/care/NursingReports.vue'),
                                meta: { title: '护理报表' }
                            },
                            {
                                path: 'nursing-records',
                                name: 'CareNursingRecords',
                                component: () => import('../views/care/NursingRecords.vue'),
                                meta: { title: '护理记录' }
                            },
                            {
                                path: 'service-bookings',
                                name: 'ServiceBookings',
                                component: () => import('../views/care/ServiceBookings.vue'),
                                meta: { title: '服务预定' }
                            }
                        ]
                    },
                    {
                        path: 'scheduling',
                        name: 'CareScheduling',
                        meta: { title: '排班管理' },
                        redirect: '/care/scheduling/shift-templates',
                        children: [
                            {
                                path: 'shift-templates',
                                name: 'ShiftTemplates',
                                component: () => import('../views/care/ShiftTemplates.vue'),
                                meta: { title: '排班方案' }
                            },
                            {
                                path: 'shift-calendar',
                                name: 'CareShiftCalendar',
                                component: () => import('../views/care/ShiftCalendar.vue'),
                                meta: { title: '排班' }
                            },
                            {
                                path: 'handovers',
                                name: 'ShiftHandovers',
                                redirect: (to) => ({ path: '/medical-care/handovers', query: to.query }),
                                meta: { title: '交接班' }
                            }
                        ]
                    },
                    {
                        path: 'dashboard',
                        name: 'CareDashboard',
                        component: () => import('../views/care/Dashboard.vue'),
                        meta: { title: '护理看板', hidden: true }
                    },
                    {
                        path: 'today',
                        name: 'CareToday',
                        component: () => import('../views/care/Today.vue'),
                        meta: { title: '今日任务', hidden: true }
                    },
                    {
                        path: 'template',
                        name: 'CareTemplate',
                        component: () => import('../views/care/Template.vue'),
                        meta: { title: '护理模板', hidden: true }
                    },
                    {
                        path: 'care-packages',
                        name: 'CarePackages',
                        component: () => import('../views/care/CarePackages.vue'),
                        meta: { title: '护理套餐', hidden: true }
                    },
                    {
                        path: 'package-items',
                        name: 'CarePackageItems',
                        component: () => import('../views/care/PackageItems.vue'),
                        meta: { title: '套餐明细', hidden: true }
                    },
                    {
                        path: 'elder-packages',
                        name: 'CareElderPackages',
                        component: () => import('../views/care/ElderPackages.vue'),
                        meta: { title: '老人套餐', hidden: true }
                    },
                    {
                        path: 'task-generate',
                        name: 'CareTaskGenerate',
                        component: () => import('../views/care/TaskGenerate.vue'),
                        meta: { title: '套餐任务生成', hidden: true }
                    },
                    {
                        path: 'exception',
                        name: 'CareException',
                        component: () => import('../views/care/Exception.vue'),
                        meta: { title: '异常任务', hidden: true }
                    },
                    {
                        path: 'audit',
                        name: 'CareAudit',
                        component: () => import('../views/care/Audit.vue'),
                        meta: { title: '防作弊审计', hidden: true }
                    },
                    {
                        path: 'workbench/task-board',
                        name: 'CareWorkbenchTaskBoard',
                        redirect: (to) => ({ path: '/medical-care/care-task-board', query: to.query }),
                        meta: { title: '照护任务看板', hidden: true }
                    },
                    {
                        path: 'workbench/plan',
                        name: 'CareWorkbenchPlan',
                        component: () => import('../views/care/workbench/CarePlan.vue'),
                        meta: { title: '照护计划', hidden: true }
                    },
                    {
                        path: 'workbench/qr',
                        name: 'CareWorkbenchQr',
                        component: () => import('../views/care/workbench/ScanExecute.vue'),
                        meta: { title: '护工扫码执行', hidden: true }
                    },
                    {
                        path: 'workbench/task',
                        name: 'CareWorkbenchTask',
                        component: () => import('../views/care/workbench/ServiceBooking.vue'),
                        meta: { title: '服务预约', hidden: true }
                    }
                ]
            },
            {
                path: 'material',
                name: 'Material',
                meta: { title: '物资中心', icon: 'DatabaseOutlined' },
                children: [
                    {
                        path: 'info',
                        name: 'MaterialInfo',
                        redirect: '/store/product',
                        meta: { title: '商品档案(商城)' }
                    },
                    {
                        path: 'warehouse',
                        name: 'MaterialWarehouse',
                        component: () => import('../views/material/Warehouse.vue'),
                        meta: { title: '仓库设置' }
                    },
                    {
                        path: 'supplier',
                        name: 'MaterialSupplier',
                        component: () => import('../views/material/Supplier.vue'),
                        meta: { title: '供应商管理' }
                    },
                    {
                        path: 'inbound',
                        name: 'MaterialInbound',
                        component: () => import('../views/inventory/Inbound.vue'),
                        meta: { title: '入库管理' }
                    },
                    {
                        path: 'outbound',
                        name: 'MaterialOutbound',
                        component: () => import('../views/inventory/Outbound.vue'),
                        meta: { title: '出库管理' }
                    },
                    {
                        path: 'stock-query',
                        name: 'MaterialStockQuery',
                        component: () => import('../views/inventory/Overview.vue'),
                        meta: { title: '库存查询' }
                    },
                    {
                        path: 'alerts',
                        name: 'MaterialAlerts',
                        component: () => import('../views/inventory/Alerts.vue'),
                        meta: { title: '库存预警' }
                    },
                    {
                        path: 'transfer',
                        name: 'MaterialTransfer',
                        component: () => import('../views/material/Transfer.vue'),
                        meta: { title: '物资调拨' }
                    },
                    {
                        path: 'stock-amount',
                        name: 'MaterialStockAmount',
                        component: () => import('../views/material/StockAmount.vue'),
                        meta: { title: '库存金额' }
                    },
                    {
                        path: 'stock-check',
                        name: 'MaterialStockCheck',
                        component: () => import('../views/inventory/Adjustments.vue'),
                        meta: { title: '库存盘点' }
                    },
                    {
                        path: 'purchase',
                        name: 'MaterialPurchase',
                        component: () => import('../views/material/Purchase.vue'),
                        meta: { title: '采购单' }
                    }
                ]
            },
            {
                path: 'store',
                name: 'Store',
                meta: { title: '商城', icon: 'ShopOutlined' },
                children: [
                    {
                        path: 'category',
                        name: 'StoreCategory',
                        component: () => import('../views/store/Category.vue'),
                        meta: { title: '商品大类' }
                    },
                    {
                        path: 'product',
                        name: 'StoreProduct',
                        component: () => import('../views/store/Product.vue'),
                        meta: { title: '商品管理' }
                    },
                    {
                        path: 'tag',
                        name: 'StoreTag',
                        component: () => import('../views/store/Tag.vue'),
                        meta: { title: '商品标签' }
                    },
                    {
                        path: 'risk',
                        name: 'StoreRisk',
                        component: () => import('../views/store/Risk.vue'),
                        meta: { title: '禁忌规则' }
                    },
                    {
                        path: 'order',
                        name: 'StoreOrder',
                        component: () => import('../views/store/Order.vue'),
                        meta: { title: '订单管理' }
                    },
                    {
                        path: 'points',
                        name: 'StorePoints',
                        component: () => import('../views/store/Points.vue'),
                        meta: { title: '积分账户' }
                    }
                ]
            },
            {
                path: 'inventory',
                name: 'Inventory',
                meta: { title: '库存管理', icon: 'AlertOutlined', hidden: true },
                children: [
                    {
                        path: '',
                        redirect: '/material/stock-query',
                        meta: { hidden: true }
                    },
                    {
                        path: 'overview',
                        redirect: '/material/stock-query',
                        meta: { title: '库存总览' }
                    },
                    {
                        path: 'inbound',
                        redirect: '/material/inbound',
                        meta: { title: '入库管理' }
                    },
                    {
                        path: 'outbound',
                        redirect: '/material/outbound',
                        meta: { title: '出库记录' }
                    },
                    {
                        path: 'alerts',
                        redirect: '/material/alerts',
                        meta: { title: '预警中心' }
                    },
                    {
                        path: 'adjustments',
                        redirect: '/material/stock-check',
                        meta: { title: '盘点记录' }
                    }
                ]
            },
            {
                path: 'finance',
                name: 'Finance',
                meta: { title: '费用管理', icon: 'AccountBookOutlined' },
                redirect: '/finance/deposit-management',
                children: [
                    {
                        path: 'deposit-management',
                        name: 'FinanceDepositManagement',
                        component: () => import('../views/finance/Account.vue'),
                        meta: { title: '押金管理' }
                    },
                    {
                        path: 'admission-fee-audit',
                        name: 'FinanceAdmissionFeeAudit',
                        component: () => import('../views/finance/AdmissionFeeAudit.vue'),
                        meta: { title: '入住费用审核' }
                    },
                    {
                        path: 'admission-bill-payment',
                        name: 'FinanceAdmissionBillPayment',
                        component: () => import('../views/finance/AdmissionBillPayment.vue'),
                        meta: { title: '入住账单缴费' }
                    },
                    {
                        path: 'resident-bill-payment',
                        name: 'FinanceResidentBillPayment',
                        component: () => import('../views/finance/ResidentBillPayment.vue'),
                        meta: { title: '在住账单缴费' }
                    },
                    {
                        path: 'monthly-allocation',
                        name: 'FinanceMonthlyAllocation',
                        component: () => import('../views/finance/MonthlyAllocation.vue'),
                        meta: { title: '月分摊费' }
                    },
                    {
                        path: 'prepaid-recharge',
                        name: 'FinancePrepaidRecharge',
                        component: () => import('../views/finance/PrepaidRecharge.vue'),
                        meta: { title: '预存充值' }
                    },
                    {
                        path: 'discharge-fee-audit',
                        name: 'FinanceDischargeFeeAudit',
                        component: () => import('../views/finance/DischargeFeeAudit.vue'),
                        meta: { title: '退住费用审核' }
                    },
                    {
                        path: 'finance-settlement',
                        name: 'FinanceSettlement',
                        component: () => import('../views/finance/Reconcile.vue'),
                        meta: { title: '财务结算' }
                    },
                    {
                        path: 'discharge-settlement',
                        name: 'FinanceDischargeSettlement',
                        component: () => import('../views/finance/DischargeSettlement.vue'),
                        meta: { title: '退住结算' }
                    },
                    {
                        path: 'medical-care-ledger',
                        name: 'FinanceMedicalCareLedger',
                        component: () => import('../views/finance/AccountLog.vue'),
                        meta: { title: '医护费用流水' }
                    },
                    {
                        path: 'consumption-register',
                        name: 'FinanceConsumptionRegister',
                        component: () => import('../views/finance/ConsumptionRegister.vue'),
                        meta: { title: '消费登记' }
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
                        component: () => import('../views/finance/Report.vue'),
                        meta: { title: '财务报表' }
                    },
                    {
                        path: 'account',
                        name: 'FinanceAccount',
                        component: () => import('../views/finance/Account.vue'),
                        meta: { title: '老人账户' }
                    },
                    {
                        path: 'account-log',
                        name: 'FinanceAccountLog',
                        component: () => import('../views/finance/AccountLog.vue'),
                        meta: { title: '账户流水' }
                    },
                    {
                        path: 'bill',
                        redirect: '/finance/resident-bill-payment',
                        meta: { hidden: true }
                    },
                    {
                        path: 'reconcile',
                        redirect: '/finance/finance-settlement',
                        meta: { hidden: true }
                    }
                ]
            },
            {
                path: 'health',
                name: 'Health',
                meta: { title: '健康服务', icon: 'HeartOutlined', hidden: true },
                redirect: '/health/medication/drug-dictionary',
                children: [
                    {
                        path: 'medication',
                        name: 'HealthMedication',
                        meta: { title: '用药服务' },
                        redirect: '/health/medication/drug-dictionary',
                        children: [
                            {
                                path: 'drug-dictionary',
                                name: 'HealthDrugDictionary',
                                component: () => import('../views/health/DrugDictionary.vue'),
                                meta: { title: '药品字典' }
                            },
                            {
                                path: 'drug-deposit',
                                name: 'HealthDrugDeposit',
                                component: () => import('../views/health/DrugDeposit.vue'),
                                meta: { title: '药品缴存' }
                            },
                            {
                                path: 'medication-setting',
                                name: 'HealthMedicationSetting',
                                component: () => import('../views/health/MedicationSetting.vue'),
                                meta: { title: '用药设置' }
                            },
                            {
                                path: 'medication-registration',
                                name: 'HealthMedicationRegistration',
                                redirect: (to) => ({ path: '/medical-care/medication-registration', query: to.query }),
                                meta: { title: '用药登记' }
                            },
                            {
                                path: 'medication-remaining',
                                name: 'HealthMedicationRemaining',
                                component: () => import('../views/health/MedicationRemaining.vue'),
                                meta: { title: '剩余用药' }
                            }
                        ]
                    },
                    {
                        path: 'management',
                        name: 'HealthManagement',
                        meta: { title: '健康管理' },
                        redirect: '/health/management/archive',
                        children: [
                            {
                                path: 'archive',
                                name: 'HealthArchive',
                                component: () => import('../views/health/HealthArchive.vue'),
                                meta: { title: '健康档案' }
                            },
                            {
                                path: 'data',
                                name: 'HealthData',
                                component: () => import('../views/health/HealthData.vue'),
                                meta: { title: '健康数据' }
                            }
                        ]
                    },
                    {
                        path: 'inspection',
                        name: 'HealthInspection',
                        redirect: (to) => ({ path: '/medical-care/inspection', query: to.query }),
                        meta: { title: '健康巡检' }
                    },
                    {
                        path: 'nursing-log',
                        name: 'HealthNursingLog',
                        redirect: (to) => ({ path: '/medical-care/nursing-log', query: to.query }),
                        meta: { title: '护理日志' }
                    },
                    {
                        path: 'drug-dictionary',
                        redirect: '/health/medication/drug-dictionary',
                        meta: { hidden: true }
                    },
                    {
                        path: 'drug-deposit',
                        redirect: '/health/medication/drug-deposit',
                        meta: { hidden: true }
                    },
                    {
                        path: 'medication-setting',
                        redirect: '/health/medication/medication-setting',
                        meta: { hidden: true }
                    },
                    {
                        path: 'medication-registration',
                        redirect: '/health/medication/medication-registration',
                        meta: { hidden: true }
                    },
                    {
                        path: 'medication-remaining',
                        redirect: '/health/medication/medication-remaining',
                        meta: { hidden: true }
                    },
                    {
                        path: 'archive',
                        redirect: '/health/management/archive',
                        meta: { hidden: true }
                    },
                    {
                        path: 'data',
                        redirect: '/health/management/data',
                        meta: { hidden: true }
                    }
                ]
            },
            {
                path: 'medical-care',
                name: 'MedicalCare',
                meta: { title: '医护健康服务', icon: 'MedicineBoxOutlined' },
                redirect: '/medical-care/center',
                children: [
                    {
                        path: 'center',
                        name: 'MedicalCareCenter',
                        component: () => import('../views/medical/MedicalHealthCenter.vue'),
                        meta: { title: '服务中心' }
                    },
                    {
                        path: 'workbench',
                        name: 'MedicalCareWorkbench',
                        component: () => import('../views/medical/Workbench.vue'),
                        meta: { title: '医护照护工作台', hidden: true }
                    },
                    {
                        path: 'care-task-board',
                        name: 'MedicalCareTaskBoard',
                        component: () => import('../views/care/workbench/TaskBoard.vue'),
                        meta: { title: '护理任务看板' }
                    },
                    {
                        path: 'inspection',
                        name: 'MedicalCareInspection',
                        component: () => import('../views/health/Inspection.vue'),
                        meta: { title: '健康巡检' }
                    },
                    {
                        path: 'medication-registration',
                        name: 'MedicalCareMedicationRegistration',
                        component: () => import('../views/health/MedicationRegistration.vue'),
                        meta: { title: '用药登记' }
                    },
                    {
                        path: 'nursing-log',
                        name: 'MedicalCareNursingLog',
                        component: () => import('../views/health/NursingLog.vue'),
                        meta: { title: '护理日志' }
                    },
                    {
                        path: 'handovers',
                        name: 'MedicalCareHandovers',
                        component: () => import('../views/care/Handovers.vue'),
                        meta: { title: '交接班' }
                    },
                    {
                        path: 'assessment/tcm',
                        name: 'MedicalCareTcmAssessment',
                        component: () => import('../views/medical/TcmAssessment.vue'),
                        meta: { title: '中医体质评估' }
                    },
                    {
                        path: 'assessment/cvd',
                        name: 'MedicalCareCvdAssessment',
                        component: () => import('../views/medical/CvdRiskAssessment.vue'),
                        meta: { title: '心血管风险评估' }
                    },
                    {
                        path: 'integrated-account',
                        name: 'MedicalCareIntegratedAccount',
                        component: () => import('../views/medical/IntegratedHealthAccount.vue'),
                        meta: { title: '健康服务与医护账户一体化' }
                    }
                ]
            },
            {
                path: 'fire',
                name: 'FireSafety',
                meta: { title: '消防安全管理', icon: 'SafetyOutlined' },
                redirect: '/fire/facility-management',
                children: [
                    {
                        path: 'facility-management',
                        name: 'FireFacilityManagement',
                        component: () => import('../views/fire/FacilityManagement.vue'),
                        meta: { title: '消防设施管理' }
                    },
                    {
                        path: 'control-room-duty',
                        name: 'FireControlRoomDuty',
                        component: () => import('../views/fire/ControlRoomDuty.vue'),
                        meta: { title: '控制室值班记录' }
                    },
                    {
                        path: 'monthly-check',
                        name: 'FireMonthlyCheck',
                        component: () => import('../views/fire/MonthlyCheck.vue'),
                        meta: { title: '每月防火检查' }
                    },
                    {
                        path: 'day-patrol',
                        name: 'FireDayPatrol',
                        component: () => import('../views/fire/DayPatrol.vue'),
                        meta: { title: '日间防火巡查' }
                    },
                    {
                        path: 'night-patrol',
                        name: 'FireNightPatrol',
                        component: () => import('../views/fire/NightPatrol.vue'),
                        meta: { title: '夜间防火巡查' }
                    },
                    {
                        path: 'maintenance-report',
                        name: 'FireMaintenanceReport',
                        component: () => import('../views/fire/MaintenanceReport.vue'),
                        meta: { title: '消防设施维护保养报告' }
                    },
                    {
                        path: 'fault-maintenance',
                        name: 'FireFaultMaintenance',
                        component: () => import('../views/fire/FaultMaintenance.vue'),
                        meta: { title: '建筑消防设施故障维护' }
                    },
                    {
                        path: 'data-stats',
                        name: 'FireDataStats',
                        component: () => import('../views/fire/DataStats.vue'),
                        meta: { title: '数据报表统计' }
                    }
                ]
            },
            {
                path: 'stats',
                name: 'Stats',
                meta: { title: '统计分析', icon: 'BarChartOutlined' },
                redirect: '/stats/check-in',
                children: [
                    {
                        path: 'check-in',
                        name: 'StatsCheckIn',
                        component: () => import('../views/stats/CheckInStats.vue'),
                        meta: { title: '入住统计' }
                    },
                    {
                        path: 'consumption',
                        name: 'StatsConsumption',
                        component: () => import('../views/stats/ConsumptionStats.vue'),
                        meta: { title: '消费统计' }
                    },
                    {
                        path: 'elder-info',
                        name: 'StatsElderInfo',
                        component: () => import('../views/stats/ElderInfoStats.vue'),
                        meta: { title: '老人信息统计' }
                    },
                    {
                        path: 'org',
                        name: 'StatsOrg',
                        meta: { title: '机构统计' },
                        redirect: '/stats/org/monthly-operation',
                        children: [
                            {
                                path: 'monthly-operation',
                                name: 'StatsOrgMonthlyOperation',
                                component: () => import('../views/stats/OrgMonthlyOperation.vue'),
                                meta: { title: '机构月运营详情' }
                            },
                            {
                                path: 'elder-flow',
                                name: 'StatsOrgElderFlow',
                                component: () => import('../views/stats/OrgElderFlow.vue'),
                                meta: { title: '老人出入统计' }
                            },
                            {
                                path: 'bed-usage',
                                name: 'StatsOrgBedUsage',
                                component: () => import('../views/stats/OrgBedUsage.vue'),
                                meta: { title: '床位使用统计' }
                            }
                        ]
                    },
                    {
                        path: 'monthly-revenue',
                        name: 'StatsMonthlyRevenue',
                        component: () => import('../views/stats/MonthlyRevenueStats.vue'),
                        meta: { title: '月运营收入统计' }
                    },
                    {
                        path: 'elder-flow-report',
                        name: 'StatsElderFlowReport',
                        component: () => import('../views/stats/ElderFlowReport.vue'),
                        meta: { title: '老人出入报表' }
                    }
                ]
            },
            {
                path: 'card',
                name: 'Card',
                meta: { title: '一卡通管理', icon: 'CreditCardOutlined', hidden: true },
                redirect: '/card/account',
                children: [
                    {
                        path: 'account',
                        name: 'CardAccount',
                        component: () => import('../views/card/Account.vue'),
                        meta: { title: '卡务管理' }
                    },
                    {
                        path: 'recharge',
                        name: 'CardRecharge',
                        component: () => import('../views/card/Recharge.vue'),
                        meta: { title: '充值记录' }
                    },
                    {
                        path: 'consume',
                        name: 'CardConsume',
                        component: () => import('../views/card/Consume.vue'),
                        meta: { title: '消费记录' }
                    }
                ]
            },
            {
                path: 'life',
                name: 'Life',
                meta: { title: '生活与健康', icon: 'CoffeeOutlined', hidden: true },
                redirect: '/life/meal-plan',
                children: [
                    {
                        path: 'birthday',
                        name: 'LifeBirthday',
                        component: () => import('../views/life/Birthday.vue'),
                        meta: { title: '会员生日' }
                    },
                    {
                        path: 'meal-plan',
                        name: 'LifeMealPlan',
                        component: () => import('../views/life/MealPlan.vue'),
                        meta: { title: '膳食计划' }
                    },
                    {
                        path: 'activity',
                        name: 'LifeActivity',
                        component: () => import('../views/life/Activity.vue'),
                        meta: { title: '活动管理' }
                    },
                    {
                        path: 'incident',
                        name: 'LifeIncident',
                        component: () => import('../views/life/Incident.vue'),
                        meta: { title: '事故登记' }
                    },
                    {
                        path: 'health-basic',
                        name: 'LifeHealthBasic',
                        component: () => import('../views/life/HealthBasic.vue'),
                        meta: { title: '基础健康记录' }
                    },
                    {
                        path: 'room-cleaning',
                        name: 'LifeRoomCleaning',
                        component: () => import('../views/life/RoomCleaning.vue'),
                        meta: { title: '房间打扫' }
                    },
                    {
                        path: 'maintenance',
                        name: 'LifeMaintenance',
                        component: () => import('../views/life/Maintenance.vue'),
                        meta: { title: '维修管理' }
                    }
                ]
            },
            {
                path: 'dining',
                name: 'Dining',
                meta: { title: '餐饮管理', icon: 'CoffeeOutlined' },
                redirect: '/dining/dish',
                children: [
                    {
                        path: 'dish',
                        name: 'DiningDish',
                        component: () => import('../views/dining/Dish.vue'),
                        meta: { title: '菜品管理' }
                    },
                    {
                        path: 'order',
                        name: 'DiningOrder',
                        component: () => import('../views/dining/Order.vue'),
                        meta: { title: '点餐' }
                    },
                    {
                        path: 'stats',
                        name: 'DiningStats',
                        component: () => import('../views/dining/Stats.vue'),
                        meta: { title: '订餐统计' }
                    },
                    {
                        path: 'recipe',
                        name: 'DiningRecipe',
                        component: () => import('../views/dining/Recipe.vue'),
                        meta: { title: '食谱管理' }
                    },
                    {
                        path: 'prep-zone',
                        name: 'DiningPrepZone',
                        component: () => import('../views/dining/PrepZone.vue'),
                        meta: { title: '分区备餐' }
                    },
                    {
                        path: 'delivery-area',
                        name: 'DiningDeliveryArea',
                        component: () => import('../views/dining/DeliveryArea.vue'),
                        meta: { title: '送餐区域' }
                    },
                    {
                        path: 'delivery-record',
                        name: 'DiningDeliveryRecord',
                        component: () => import('../views/dining/DeliveryRecord.vue'),
                        meta: { title: '送餐记录' }
                    }
                ]
            },
            {
                path: 'assessment',
                name: 'Assessment',
                meta: { title: '评估管理', icon: 'FileSearchOutlined', roles: ['ADMIN', 'STAFF'] },
                redirect: '/assessment/ability/admission',
                children: [
                    {
                        path: 'ability',
                        name: 'AssessmentAbility',
                        meta: { title: '能力评估管理' },
                        redirect: '/assessment/ability/admission',
                        children: [
                            {
                                path: 'admission',
                                name: 'AssessmentAdmission',
                                component: () => import('../views/assessment/Admission.vue'),
                                meta: { title: '入住评估' }
                            },
                            {
                                path: 'registration',
                                name: 'AssessmentRegistration',
                                component: () => import('../views/assessment/Registration.vue'),
                                meta: { title: '评估登记' }
                            },
                            {
                                path: 'continuous',
                                name: 'AssessmentContinuous',
                                component: () => import('../views/assessment/Continuous.vue'),
                                meta: { title: '持续评估' }
                            },
                            {
                                path: 'archive',
                                name: 'AssessmentArchive',
                                component: () => import('../views/assessment/Archive.vue'),
                                meta: { title: '评估档案' }
                            }
                        ]
                    },
                    {
                        path: 'other-scale',
                        name: 'AssessmentOtherScale',
                        component: () => import('../views/assessment/OtherScale.vue'),
                        meta: { title: '其他量表评估' }
                    },
                    {
                        path: 'template',
                        name: 'AssessmentTemplate',
                        component: () => import('../views/assessment/Template.vue'),
                        meta: { title: '量表模板', roles: ['ADMIN'], hidden: true }
                    },
                    {
                        path: 'cognitive',
                        name: 'AssessmentCognitive',
                        component: () => import('../views/assessment/Cognitive.vue'),
                        meta: { title: '认知能力评估' }
                    },
                    {
                        path: 'self-care',
                        name: 'AssessmentSelfCare',
                        component: () => import('../views/assessment/SelfCare.vue'),
                        meta: { title: '自理能力评估' }
                    },
                    {
                        path: 'admission',
                        redirect: '/assessment/ability/admission',
                        meta: { hidden: true }
                    },
                    {
                        path: 'registration',
                        redirect: '/assessment/ability/registration',
                        meta: { hidden: true }
                    },
                    {
                        path: 'continuous',
                        redirect: '/assessment/ability/continuous',
                        meta: { hidden: true }
                    },
                    {
                        path: 'archive',
                        redirect: '/assessment/ability/archive',
                        meta: { hidden: true }
                    }
                ]
            },
            {
                path: 'survey',
                name: 'Survey',
                meta: { title: '问卷与持续改进', icon: 'FormOutlined', hidden: true },
                redirect: '/survey/question-bank',
                children: [
                    {
                        path: 'question-bank',
                        name: 'SurveyQuestionBank',
                        component: () => import('../views/survey/QuestionBank.vue'),
                        meta: { title: '题库管理' }
                    },
                    {
                        path: 'template',
                        name: 'SurveyTemplate',
                        component: () => import('../views/survey/Template.vue'),
                        meta: { title: '模板管理' }
                    },
                    {
                        path: 'submit',
                        name: 'SurveySubmit',
                        component: () => import('../views/survey/Submit.vue'),
                        meta: { title: '问卷填写' }
                    },
                    {
                        path: 'stats',
                        name: 'SurveyStats',
                        component: () => import('../views/survey/Stats.vue'),
                        meta: { title: '问卷统计' }
                    },
                    {
                        path: 'performance',
                        name: 'SurveyPerformance',
                        component: () => import('../views/survey/Performance.vue'),
                        meta: { title: '绩效榜' }
                    }
                ]
            },
            {
                path: 'oa',
                name: 'OA',
                meta: { title: '行政管理', icon: 'ApartmentOutlined' },
                redirect: '/oa/work-execution/task',
                children: [
                    {
                        path: 'work-execution',
                        name: 'OaWorkExecution',
                        meta: { title: '工作执行管理' },
                        redirect: '/oa/work-execution/task',
                        children: [
                            {
                                path: 'task',
                                name: 'OaTask',
                                component: () => import('../views/oa/Task.vue'),
                                meta: { title: '任务管理' }
                            },
                            {
                                path: 'calendar',
                                name: 'OaCalendar',
                                component: () => import('../views/oa/Calendar.vue'),
                                meta: { title: '协同日历' }
                            },
                            {
                                path: 'daily-report',
                                name: 'OaDailyReport',
                                component: () => import('../views/oa/DailyReport.vue'),
                                meta: { title: '日总结' }
                            },
                            {
                                path: 'weekly-report',
                                name: 'OaWeeklyReport',
                                component: () => import('../views/oa/WeeklyReport.vue'),
                                meta: { title: '周总结' }
                            },
                            {
                                path: 'monthly-report',
                                name: 'OaMonthlyReport',
                                component: () => import('../views/oa/MonthlyReport.vue'),
                                meta: { title: '月总结' }
                            },
                            {
                                path: 'yearly-report',
                                name: 'OaYearlyReport',
                                component: () => import('../views/oa/YearlyReport.vue'),
                                meta: { title: '年总结' }
                            },
                            {
                                path: 'attendance-leave',
                                name: 'OaWorkExecutionAttendanceLeave',
                                redirect: '/oa/attendance-leave',
                                meta: { title: '考勤与请假' }
                            }
                        ]
                    },
                    {
                        path: 'life',
                        name: 'OaLife',
                        meta: { title: '生活管理' },
                        redirect: '/oa/life/birthday',
                        children: [
                            {
                                path: 'birthday',
                                name: 'OaLifeBirthday',
                                component: () => import('../views/life/Birthday.vue'),
                                meta: { title: '会员生日' }
                            },
                            {
                                path: 'room-cleaning',
                                name: 'OaLifeRoomCleaning',
                                component: () => import('../views/life/RoomCleaning.vue'),
                                meta: { title: '房间打扫' }
                            },
                            {
                                path: 'maintenance',
                                name: 'OaLifeMaintenance',
                                component: () => import('../views/life/Maintenance.vue'),
                                meta: { title: '维修管理' }
                            }
                        ]
                    },
                    {
                        path: 'staff',
                        name: 'OaStaff',
                        component: () => import('../views/hr/Staff.vue'),
                        props: { title: '员工管理', subTitle: '行政员工档案与在离职状态管理' },
                        meta: { title: '员工管理' }
                    },
                    {
                        path: 'album',
                        name: 'OaAlbum',
                        component: () => import('../views/oa/Album.vue'),
                        meta: { title: '相册管理' }
                    },
                    {
                        path: 'card',
                        name: 'OaCard',
                        meta: { title: '一卡通管理' },
                        redirect: '/oa/card/account',
                        children: [
                            {
                                path: 'account',
                                name: 'OaCardAccount',
                                component: () => import('../views/card/Account.vue'),
                                meta: { title: '卡务管理' }
                            },
                            {
                                path: 'recharge',
                                name: 'OaCardRecharge',
                                component: () => import('../views/card/Recharge.vue'),
                                meta: { title: '充值记录' }
                            },
                            {
                                path: 'consume',
                                name: 'OaCardConsume',
                                component: () => import('../views/card/Consume.vue'),
                                meta: { title: '消费记录' }
                            }
                        ]
                    },
                    {
                        path: 'survey',
                        name: 'OaSurvey',
                        meta: { title: '满意度问卷' },
                        redirect: '/oa/survey/manage',
                        children: [
                            {
                                path: 'manage',
                                name: 'OaSurveyManage',
                                component: () => import('../views/survey/Template.vue'),
                                meta: { title: '问卷管理' }
                            },
                            {
                                path: 'stats',
                                name: 'OaSurveyStats',
                                component: () => import('../views/survey/Stats.vue'),
                                meta: { title: '问卷统计' }
                            }
                        ]
                    },
                    {
                        path: 'knowledge',
                        name: 'OaKnowledge',
                        component: () => import('../views/oa/Knowledge.vue'),
                        meta: { title: '知识库管理' }
                    },
                    {
                        path: 'group-settings',
                        name: 'OaGroupSettings',
                        component: () => import('../views/oa/GroupSettings.vue'),
                        meta: { title: '分组设置' }
                    },
                    {
                        path: 'notice',
                        name: 'OaNotice',
                        component: () => import('../views/oa/Notice.vue'),
                        meta: { title: '通知公告' }
                    },
                    {
                        path: 'activity',
                        name: 'OaActivity',
                        component: () => import('../views/life/Activity.vue'),
                        meta: { title: '活动管理' }
                    },
                    {
                        path: 'activity-plan',
                        name: 'OaActivityPlan',
                        component: () => import('../views/oa/ActivityPlan.vue'),
                        meta: { title: '活动计划' }
                    },
                    {
                        path: 'reward-punishment',
                        name: 'OaRewardPunishment',
                        component: () => import('../views/oa/RewardPunishment.vue'),
                        meta: { title: '奖惩管理' }
                    },
                    {
                        path: 'training',
                        name: 'OaTraining',
                        component: () => import('../views/hr/Training.vue'),
                        meta: { title: '培训管理' }
                    },
                    {
                        path: 'portal',
                        name: 'OaPortal',
                        component: () => import('../views/oa/Portal.vue'),
                        meta: { title: '门户与待办' }
                    },
                    {
                        path: 'todo',
                        name: 'OaTodo',
                        component: () => import('../views/oa/Todo.vue'),
                        meta: { title: '待办事项' }
                    },
                    {
                        path: 'approval',
                        name: 'OaApproval',
                        component: () => import('../views/oa/Approval.vue'),
                        meta: { title: '固定审批' }
                    },
                    {
                        path: 'attendance-leave',
                        name: 'OaAttendanceLeave',
                        component: () => import('../views/oa/AttendanceLeave.vue'),
                        meta: { title: '考勤与请假' }
                    },
                    {
                        path: 'document',
                        name: 'OaDocument',
                        component: () => import('../views/oa/Document.vue'),
                        meta: { title: '文档管理' }
                    },
                    {
                        path: 'work-report',
                        name: 'OaWorkReport',
                        component: () => import('../views/oa/WorkReport.vue'),
                        meta: { title: '工作总结' }
                    }
                ]
            },
            {
                path: 'hr',
                name: 'Hr',
                meta: { title: '人力与绩效', icon: 'SolutionOutlined', roles: ['ADMIN'], hidden: true },
                redirect: '/hr/staff',
                children: [
                    {
                        path: 'staff',
                        name: 'HrStaff',
                        component: () => import('../views/hr/Staff.vue'),
                        props: { title: '员工档案', subTitle: '人力资源档案、岗位与资质信息' },
                        meta: { title: '员工档案', roles: ['ADMIN'] }
                    },
                    {
                        path: 'training',
                        name: 'HrTraining',
                        component: () => import('../views/hr/Training.vue'),
                        meta: { title: '培训管理', roles: ['ADMIN'] }
                    },
                    {
                        path: 'points',
                        name: 'HrPoints',
                        component: () => import('../views/hr/Points.vue'),
                        meta: { title: '积分管理', roles: ['ADMIN'] }
                    },
                    {
                        path: 'points-rule',
                        name: 'HrPointsRule',
                        component: () => import('../views/hr/PointsRule.vue'),
                        meta: { title: '积分规则', roles: ['ADMIN'] }
                    },
                    {
                        path: 'performance',
                        name: 'HrPerformance',
                        component: () => import('../views/hr/Performance.vue'),
                        meta: { title: '绩效看板', roles: ['ADMIN'] }
                    }
                ]
            },
            {
                path: 'base-config',
                name: 'BaseConfig',
                meta: { title: '基础数据配置', icon: 'DatabaseOutlined', roles: ['ADMIN'] },
                redirect: '/base-config/elder-type',
                children: [
                    {
                        path: 'elder-type',
                        name: 'BaseConfigElderType',
                        component: () => import('../views/base-config/Index.vue'),
                        props: { title: '老人类别', groupCode: 'ELDER_CATEGORY' },
                        meta: { title: '老人类别', roles: ['ADMIN'] }
                    },
                    {
                        path: 'marketing',
                        name: 'BaseConfigMarketing',
                        meta: { title: '营销', roles: ['ADMIN'] },
                        redirect: '/base-config/marketing/customer-tag',
                        children: [
                            {
                                path: 'customer-tag',
                                name: 'BaseConfigMarketingCustomerTag',
                                component: () => import('../views/base-config/Index.vue'),
                                props: { title: '客户标签', groupCode: 'MARKETING_CUSTOMER_TAG' },
                                meta: { title: '客户标签', roles: ['ADMIN'] }
                            },
                            {
                                path: 'source-channel',
                                name: 'BaseConfigMarketingSourceChannel',
                                component: () => import('../views/base-config/Index.vue'),
                                props: { title: '来源渠道', groupCode: 'MARKETING_SOURCE_CHANNEL' },
                                meta: { title: '来源渠道', roles: ['ADMIN'] }
                            }
                        ]
                    },
                    {
                        path: 'assessment',
                        name: 'BaseConfigAssessment',
                        component: () => import('../views/base-config/Index.vue'),
                        props: { title: '评估', groupCode: 'ASSESSMENT_TYPE' },
                        meta: { title: '评估', roles: ['ADMIN'] }
                    },
                    {
                        path: 'residence',
                        name: 'BaseConfigResidence',
                        meta: { title: '入住', roles: ['ADMIN'] },
                        redirect: '/base-config/residence/bed-type',
                        children: [
                            {
                                path: 'bed-type',
                                name: 'BaseConfigResidenceBedType',
                                component: () => import('../views/base-config/Index.vue'),
                                props: { title: '床位类型', groupCode: 'ADMISSION_BED_TYPE' },
                                meta: { title: '床位类型', roles: ['ADMIN'] }
                            },
                            {
                                path: 'room-type',
                                name: 'BaseConfigResidenceRoomType',
                                component: () => import('../views/base-config/Index.vue'),
                                props: { title: '房间类型', groupCode: 'ADMISSION_ROOM_TYPE' },
                                meta: { title: '房间类型', roles: ['ADMIN'] }
                            },
                            {
                                path: 'area-settings',
                                name: 'BaseConfigResidenceAreaSettings',
                                component: () => import('../views/base-config/Index.vue'),
                                props: { title: '区域设置', groupCode: 'ADMISSION_AREA' },
                                meta: { title: '区域设置', roles: ['ADMIN'] }
                            },
                            {
                                path: 'building-management',
                                name: 'BaseConfigResidenceBuildingManagement',
                                component: () => import('../views/base-config/Index.vue'),
                                props: { title: '楼栋管理', groupCode: 'ADMISSION_BUILDING' },
                                meta: { title: '楼栋管理', roles: ['ADMIN'] }
                            }
                        ]
                    },
                    {
                        path: 'activity',
                        name: 'BaseConfigActivity',
                        component: () => import('../views/base-config/Index.vue'),
                        props: { title: '活动', groupCode: 'ACTIVITY_TYPE' },
                        meta: { title: '活动', roles: ['ADMIN'] }
                    },
                    {
                        path: 'community',
                        name: 'BaseConfigCommunity',
                        meta: { title: '社区', roles: ['ADMIN'] },
                        redirect: '/base-config/community/maintenance-category',
                        children: [
                            {
                                path: 'maintenance-category',
                                name: 'BaseConfigCommunityMaintenanceCategory',
                                component: () => import('../views/base-config/Index.vue'),
                                props: { title: '维修分类', groupCode: 'COMMUNITY_REPAIR_CATEGORY' },
                                meta: { title: '维修分类', roles: ['ADMIN'] }
                            },
                            {
                                path: 'task-type-settings',
                                name: 'BaseConfigCommunityTaskTypeSettings',
                                component: () => import('../views/base-config/Index.vue'),
                                props: { title: '任务类型设置', groupCode: 'COMMUNITY_TASK_TYPE' },
                                meta: { title: '任务类型设置', roles: ['ADMIN'] }
                            }
                        ]
                    },
                    {
                        path: 'discharge-fee-settings',
                        name: 'BaseConfigDischargeFeeSettings',
                        component: () => import('../views/base-config/Index.vue'),
                        props: { title: '退住费用设置', groupCode: 'DISCHARGE_FEE_CONFIG' },
                        meta: { title: '退住费用设置', roles: ['ADMIN'] }
                    },
                    {
                        path: 'fee',
                        name: 'BaseConfigFee',
                        component: () => import('../views/base-config/Index.vue'),
                        props: { title: '费用', groupCode: 'FEE_TYPE' },
                        meta: { title: '费用', roles: ['ADMIN'] }
                    },
                    {
                        path: 'refund-reason',
                        name: 'BaseConfigRefundReason',
                        component: () => import('../views/base-config/Index.vue'),
                        props: { title: '退款原因', groupCode: 'REFUND_REASON' },
                        meta: { title: '退款原因', roles: ['ADMIN'] }
                    },
                    {
                        path: 'trial-package',
                        name: 'BaseConfigTrialPackage',
                        component: () => import('../views/base-config/Index.vue'),
                        props: { title: '试住套餐', groupCode: 'TRIAL_STAY_PACKAGE' },
                        meta: { title: '试住套餐', roles: ['ADMIN'] }
                    },
                    {
                        path: 'index',
                        redirect: '/base-config/elder-type',
                        meta: { hidden: true }
                    }
                ]
            },
            {
                path: 'system',
                name: 'System',
                meta: { title: '系统管理', icon: 'SettingOutlined', roles: ['ADMIN'] },
                redirect: '/system/org-info',
                children: [
                    {
                        path: 'org-info',
                        name: 'SystemOrgInfo',
                        component: () => import('../views/System/OrgInfo.vue'),
                        meta: { title: '机构信息', roles: ['ADMIN'] }
                    },
                    {
                        path: 'org-manage',
                        name: 'SystemOrgManage',
                        meta: { title: '机构管理', roles: ['ADMIN'] },
                        redirect: '/system/org-manage/intro',
                        children: [
                            {
                                path: 'intro',
                                name: 'SystemOrgIntro',
                                component: () => import('../views/base-config/Index.vue'),
                                props: { title: '机构介绍', groupCode: 'SYSTEM_ORG_INTRO' },
                                meta: { title: '机构介绍', roles: ['ADMIN'] }
                            },
                            {
                                path: 'news',
                                name: 'SystemOrgNews',
                                component: () => import('../views/base-config/Index.vue'),
                                props: { title: '机构动态', groupCode: 'SYSTEM_ORG_NEWS' },
                                meta: { title: '机构动态', roles: ['ADMIN'] }
                            },
                            {
                                path: 'life',
                                name: 'SystemLifeEntertainment',
                                component: () => import('../views/base-config/Index.vue'),
                                props: { title: '生活娱乐', groupCode: 'SYSTEM_LIFE_ENTERTAINMENT' },
                                meta: { title: '生活娱乐', roles: ['ADMIN'] }
                            }
                        ]
                    },
                    {
                        path: 'role',
                        name: 'SystemRoleManage',
                        component: () => import('../views/System/RoleManage.vue'),
                        meta: { title: '角色管理', roles: ['ADMIN'] }
                    },
                    {
                        path: 'department',
                        name: 'SystemDepartmentManage',
                        component: () => import('../views/System/DepartmentManage.vue'),
                        meta: { title: '部门管理', roles: ['ADMIN'] }
                    },
                    {
                        path: 'app-version',
                        name: 'SystemAppVersion',
                        component: () => import('../views/base-config/Index.vue'),
                        props: { title: 'APP版本管理', groupCode: 'SYSTEM_APP_VERSION' },
                        meta: { title: 'APP版本管理', roles: ['ADMIN'] }
                    },
                    {
                        path: 'dict',
                        name: 'SystemDictionary',
                        component: () => import('../views/base-config/Index.vue'),
                        props: { title: '系统字典', groupCode: 'SYSTEM_DICTIONARY' },
                        meta: { title: '系统字典', roles: ['ADMIN'] }
                    },
                    {
                        path: 'message',
                        name: 'SystemMessageManage',
                        component: () => import('../views/base-config/Index.vue'),
                        props: { title: '留言管理', groupCode: 'SYSTEM_MESSAGE' },
                        meta: { title: '留言管理', roles: ['ADMIN'] }
                    },
                    {
                        path: 'staff',
                        name: 'SystemStaff',
                        component: () => import('../views/Staff.vue'),
                        meta: { title: '员工管理', roles: ['ADMIN'] }
                    },
                    {
                        path: 'menu-preview',
                        name: 'SystemMenuPreview',
                        component: () => import('../views/System/MenuPreview.vue'),
                        meta: { title: '菜单预览', roles: ['ADMIN'] }
                    }
                ]
            },
            {
                path: 'demo',
                name: 'Demo',
                meta: { title: '能力演示', icon: 'ExperimentOutlined', hidden: true },
                children: [
                    { path: 'vxe-table', name: 'DemoVxe', component: () => import('../views/demo/VxeTableDemo.vue'), meta: { title: '高级表格' } },
                    { path: 'calendar', name: 'DemoCalendar', component: () => import('../views/demo/CalendarDemo.vue'), meta: { title: '排班日历' } },
                    { path: 'charts', name: 'DemoCharts', component: () => import('../views/demo/ChartsDemo.vue'), meta: { title: '报表图表' } },
                    { path: 'editor', name: 'DemoEditor', component: () => import('../views/demo/EditorDemo.vue'), meta: { title: '富文本编辑' } },
                    { path: 'flow', name: 'DemoFlow', component: () => import('../views/demo/FlowDemo.vue'), meta: { title: '流程图' } },
                    { path: 'antdx-chat', name: 'DemoAntdx', component: () => import('../views/demo/AntdxChatDemo.vue'), meta: { title: '智能助手' } }
                ]
            }
        ]
    },
    { path: '/', redirect: '/portal' },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('../views/NotFound.vue'),
        meta: { title: '未找到', hidden: true }
    }
];
