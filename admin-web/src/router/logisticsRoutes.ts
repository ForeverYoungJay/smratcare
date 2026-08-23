import type { RouteRecordRaw } from 'vue-router'

export const logisticsRoutes: RouteRecordRaw[] = [
  {
        path: 'logistics',
        name: 'Logistics',
        meta: { title: '后勤保障', icon: 'ToolOutlined', navSection: 'support', navOrder: 80, navPinned: true, roles: ['LOGISTICS_EMPLOYEE', 'LOGISTICS_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'] },
        children: [
          {
            path: '',
            name: 'LogisticsHub',
            component: () => import('../views/ModuleHub.vue'),
            meta: { title: '后勤导航', hidden: true }
          },
          {
            path: 'workbench',
            name: 'LogisticsWorkbench',
            component: () => import('../views/logistics/Workbench.vue'),
            meta: { title: '后勤工作台' }
          },
          {
            path: 'task-center',
            name: 'LogisticsTaskCenter',
            component: () => import('../views/logistics/TaskCenter.vue'),
            meta: { title: '后勤任务中心' }
          },
          {
            path: 'assets',
            name: 'LogisticsAssets',
            meta: { title: '资产与床态管理' },
            redirect: '/logistics/assets/room-state-map',
            children: [
              {
                path: 'building-management',
                name: 'LogisticsBuildingManagement',
                redirect: '/logistics/assets/bed-management',
                meta: { title: '楼栋管理', hidden: true }
              },
              {
                path: 'floor-management',
                name: 'LogisticsFloorManagement',
                redirect: '/logistics/assets/bed-management',
                meta: { title: '楼层管理', hidden: true }
              },
              {
                path: 'room-management',
                name: 'LogisticsRoomManagement',
                redirect: '/logistics/assets/bed-management',
                meta: { title: '房间管理', hidden: true }
              },
              {
                path: 'room-state-map',
                name: 'LogisticsRoomStateMap',
                component: () => import('../views/bed/Map.vue'),
                meta: { title: '房态图' }
              },
              {
                // 平面图已并入房态图，作为其「楼层平面图」布局；旧入口保留为重定向，不破坏书签与既有跳转
                path: 'floor-plan',
                name: 'LogisticsFloorPlan',
                redirect: { name: 'LogisticsRoomStateMap', query: { view: 'plan' } },
                meta: { title: '楼栋平面图（已并入房态图）', hidden: true }
              },
              {
                path: 'room-detail',
                name: 'LogisticsRoomDetail',
                component: () => import('../views/bed/RoomDetail.vue'),
                meta: { title: '房间详情', hidden: true }
              },
              {
                path: 'bed-management',
                name: 'LogisticsBedManagement',
                component: () => import('../views/bed/Manage.vue'),
                props: { initialTab: 'beds' },
                meta: { title: '床位管理' }
              },
              {
                path: 'bed-type-config',
                name: 'LogisticsBedTypeConfig',
                component: () => import('../views/base-config/Index.vue'),
                props: { title: '床位类型', groupCode: 'ADMISSION_BED_TYPE' },
                meta: { title: '床位类型', roles: ['ADMIN'] }
              },
              {
                path: 'room-type-config',
                name: 'LogisticsRoomTypeConfig',
                component: () => import('../views/base-config/Index.vue'),
                props: { title: '房间类型', groupCode: 'ADMISSION_ROOM_TYPE' },
                meta: { title: '房间类型', roles: ['ADMIN'] }
              },
              {
                path: 'area-config',
                name: 'LogisticsAreaConfig',
                component: () => import('../views/base-config/Index.vue'),
                props: { title: '区域设置', groupCode: 'ADMISSION_AREA' },
                meta: { title: '区域设置', roles: ['ADMIN'] }
              },
              {
                path: 'bed-panorama',
                name: 'LogisticsBedPanorama',
                redirect: '/elder/bed-panorama',
                meta: { title: '床态全景', hidden: true }
              },
              {
                path: 'bed-status-record',
                name: 'LogisticsBedStatusRecord',
                component: () => import('../views/elder/ChangeLog.vue'),
                meta: { title: '床位状态记录' }
              },
              {
                path: 'cleaning-record',
                name: 'LogisticsCleaningRecord',
                component: () => import('../views/life/RoomCleaning.vue'),
                meta: { title: '清洁消杀记录' }
              },
              {
                path: 'maintenance-record',
                name: 'LogisticsMaintenanceRecord',
                component: () => import('../views/life/Maintenance.vue'),
                meta: { title: '维修记录' }
              }
            ]
          },
          {
            path: 'storage',
            name: 'LogisticsStorage',
            meta: { title: '物资仓储管理' },
            children: [
              { path: 'warehouse', name: 'LogisticsWarehouse', component: () => import('../views/material/Warehouse.vue'), meta: { title: '仓库设置' } },
              { path: 'supplier', name: 'LogisticsSupplier', component: () => import('../views/material/Supplier.vue'), meta: { title: '供应商管理' } },
              { path: 'purchase', name: 'LogisticsPurchase', component: () => import('../views/material/Purchase.vue'), meta: { title: '采购单' } },
              { path: 'inbound', name: 'LogisticsInbound', component: () => import('../views/inventory/Inbound.vue'), meta: { title: '入库管理' } },
              { path: 'outbound', name: 'LogisticsOutbound', component: () => import('../views/inventory/Outbound.vue'), meta: { title: '出库管理' } },
              { path: 'transfer', name: 'LogisticsTransfer', component: () => import('../views/material/Transfer.vue'), meta: { title: '物资调拨' } },
              { path: 'stock-query', name: 'LogisticsStockQuery', component: () => import('../views/inventory/Overview.vue'), meta: { title: '库存查询' } },
              { path: 'alerts', name: 'LogisticsAlerts', component: () => import('../views/inventory/Alerts.vue'), meta: { title: '库存预警' } },
              { path: 'stock-check', name: 'LogisticsStockCheck', component: () => import('../views/inventory/Adjustments.vue'), meta: { title: '库存盘点' } },
              { path: 'stock-amount', name: 'LogisticsStockAmount', component: () => import('../views/material/StockAmount.vue'), meta: { title: '库存金额' } },
              {
                path: 'item-master',
                name: 'LogisticsItemMaster',
                component: () => import('../views/store/Product.vue'),
                props: {
                  mode: 'storage',
                  title: '物品信息（主数据）',
                  subTitle: '商品主数据与商城共享；仓储侧只读查看并联动库存、出入库流程'
                },
                meta: { title: '物品信息（主数据）' }
              }
            ]
          },
          {
            path: 'dining',
            name: 'LogisticsDining',
            meta: { title: '餐饮管理' },
            children: [
              { path: 'dish', name: 'LogisticsDiningDish', component: () => import('../views/dining/Dish.vue'), meta: { title: '菜品管理' } },
              { path: 'recipe', name: 'LogisticsDiningRecipe', component: () => import('../views/dining/Recipe.vue'), meta: { title: '食谱管理' } },
              { path: 'order', name: 'LogisticsDiningOrder', component: () => import('../views/dining/Order.vue'), meta: { title: '点餐管理（个性化）' } },
              { path: 'stats', name: 'LogisticsDiningStats', component: () => import('../views/dining/Stats.vue'), meta: { title: '订餐统计' } },
              { path: 'procurement-plan', name: 'LogisticsDiningProcurementPlan', component: () => import('../views/dining/ProcurementPlan.vue'), meta: { title: '采购计划单' } },
              { path: 'prep-zone', name: 'LogisticsDiningPrepZone', component: () => import('../views/dining/PrepZone.vue'), meta: { title: '分区备餐' } },
              { path: 'delivery-area', name: 'LogisticsDiningDeliveryArea', component: () => import('../views/dining/DeliveryArea.vue'), meta: { title: '送餐区域' } },
              {
                path: 'delivery-plan',
                name: 'LogisticsDiningDeliveryPlan',
                component: () => import('../views/logistics/DeliveryPlan.vue'),
                meta: { title: '送餐计划' }
              },
              { path: 'cost-stats', name: 'LogisticsDiningCostStats', redirect: '/logistics/dining/stats?metric=cost', meta: { title: '餐饮成本统计' } }
            ]
          },
          {
            path: 'maintenance',
            name: 'LogisticsMaintenance',
            meta: { title: '维修与报障' },
            children: [
              { path: 'report', name: 'LogisticsMaintenanceReport', redirect: '/logistics/assets/maintenance-record', meta: { title: '报修登记' } },
              { path: 'dispatch', name: 'LogisticsMaintenanceDispatch', redirect: '/logistics/assets/maintenance-record?status=OPEN', meta: { title: '维修派单' } },
              { path: 'progress', name: 'LogisticsMaintenanceProgress', redirect: '/logistics/assets/maintenance-record?status=PROCESSING', meta: { title: '维修进度' } },
              {
                path: 'cost',
                name: 'LogisticsMaintenanceCost',
                component: () => import('../views/logistics/MaintenanceCost.vue'),
                meta: { title: '维修成本记录' }
              },
              {
                path: 'assets',
                name: 'LogisticsMaintenanceAssets',
                component: () => import('../views/logistics/EquipmentArchive.vue'),
                meta: { title: '设备档案' }
              }
            ]
          },
          {
            path: 'reports',
            name: 'LogisticsReports',
            meta: { title: '后勤报表中心' },
            children: [
              { path: 'bed-usage', name: 'LogisticsReportBedUsage', redirect: '/stats/org/bed-usage', meta: { title: '床位使用率' } },
              { path: 'stock-amount', name: 'LogisticsReportStockAmount', redirect: '/logistics/storage/stock-amount', meta: { title: '库存金额统计' } },
              { path: 'purchase', name: 'LogisticsReportPurchase', redirect: '/logistics/storage/purchase', meta: { title: '采购统计' } },
              { path: 'consume', name: 'LogisticsReportConsume', redirect: '/logistics/storage/outbound', meta: { title: '物资消耗统计' } },
              { path: 'dining-cost', name: 'LogisticsReportDiningCost', redirect: '/logistics/dining/stats?metric=cost', meta: { title: '餐饮成本统计' } },
              {
                path: 'maintenance-todo-log',
                name: 'LogisticsReportMaintenanceTodoLog',
                component: () => import('../views/logistics/MaintenanceTodoJobLog.vue'),
                meta: { title: '维保待办日志' }
              }
            ]
          },
          {
            path: 'commerce',
            name: 'LogisticsCommerce',
            meta: { title: '商城与商品' },
            children: [
              { path: 'category', name: 'LogisticsCommerceCategory', component: () => import('../views/store/Category.vue'), meta: { title: '商品大类' } },
              { path: 'tag', name: 'LogisticsCommerceTag', component: () => import('../views/store/Tag.vue'), meta: { title: '商品标签' } },
              { path: 'risk', name: 'LogisticsCommerceRisk', component: () => import('../views/store/Risk.vue'), meta: { title: '禁忌规则' } },
              { path: 'product', name: 'LogisticsCommerceProduct', component: () => import('../views/store/Product.vue'), meta: { title: '商品管理' } },
              { path: 'order', name: 'LogisticsCommerceOrder', component: () => import('../views/store/Order.vue'), meta: { title: '订单管理' } },
              { path: 'points', name: 'LogisticsCommercePoints', component: () => import('../views/store/Points.vue'), meta: { title: '积分账户' } }
            ]
          }
        ]
      }
]
