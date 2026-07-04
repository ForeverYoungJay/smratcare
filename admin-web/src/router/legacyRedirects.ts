import type { RouteRecordRaw } from 'vue-router'

/**
 * 历史兼容路由（纯重定向）
 *
 * 收纳两类旧 URL 的自动跳转，避免收藏/外链失效：
 *   1. 已合并进 logistics/* 的旧一级模块（bed / material / store / inventory）
 *   2. 长者模块内被迁移到新页面的旧路径（elder/admission 等）
 * 全部 hidden，不出现在菜单里。新功能请勿加到这里——加到 routes.ts 的正式模块。
 *
 * 使用：在 routes.ts 的根布局 children 中 `...legacyModuleRedirects` 展开。
 */
export const legacyModuleRedirects: RouteRecordRaw[] = [
  {
    path: 'bed',
    name: 'Bed',
    meta: { title: '床位管理', icon: 'HomeOutlined', hidden: true },
    redirect: '/logistics/assets/room-state-map',
    children: [
      {
        path: 'map',
        name: 'BedMap',
        redirect: '/logistics/assets/room-state-map',
        meta: { title: '房态图', hidden: true }
      },
      {
        path: 'manage',
        name: 'BedManage',
        redirect: '/logistics/assets/bed-management',
        meta: { title: '床位管理', hidden: true }
      }
    ]
  },
  {
    path: 'material',
    name: 'Material',
    meta: { title: '后勤物资（兼容）', icon: 'DatabaseOutlined', hidden: true },
    children: [
      {
        path: 'info',
        name: 'MaterialInfo',
        redirect: '/logistics/storage/item-master',
        meta: { title: '物资主数据（兼容）', hidden: true }
      },
      {
        path: 'warehouse',
        name: 'MaterialWarehouse',
        redirect: '/logistics/storage/warehouse',
        meta: { title: '仓库设置（兼容）', hidden: true }
      },
      {
        path: 'supplier',
        name: 'MaterialSupplier',
        redirect: '/logistics/storage/supplier',
        meta: { title: '供应商管理（兼容）', hidden: true }
      },
      {
        path: 'inbound',
        name: 'MaterialInbound',
        redirect: '/logistics/storage/inbound',
        meta: { title: '入库管理（兼容）', hidden: true }
      },
      {
        path: 'outbound',
        name: 'MaterialOutbound',
        redirect: '/logistics/storage/outbound',
        meta: { title: '出库管理（兼容）', hidden: true }
      },
      {
        path: 'stock-query',
        name: 'MaterialStockQuery',
        redirect: '/logistics/storage/stock-query',
        meta: { title: '库存查询（兼容）', hidden: true }
      },
      {
        path: 'alerts',
        name: 'MaterialAlerts',
        redirect: '/logistics/storage/alerts',
        meta: { title: '库存预警（兼容）', hidden: true }
      },
      {
        path: 'transfer',
        name: 'MaterialTransfer',
        redirect: '/logistics/storage/transfer',
        meta: { title: '物资调拨（兼容）', hidden: true }
      },
      {
        path: 'stock-amount',
        name: 'MaterialStockAmount',
        redirect: '/logistics/storage/stock-amount',
        meta: { title: '库存金额（兼容）', hidden: true }
      },
      {
        path: 'stock-check',
        name: 'MaterialStockCheck',
        redirect: '/logistics/storage/stock-check',
        meta: { title: '库存盘点（兼容）', hidden: true }
      },
      {
        path: 'purchase',
        name: 'MaterialPurchase',
        redirect: '/logistics/storage/purchase',
        meta: { title: '采购单（兼容）', hidden: true }
      }
    ]
  },
  {
    path: 'store',
    name: 'Store',
    meta: { title: '后勤商城（兼容）', icon: 'ShopOutlined', hidden: true },
    children: [
      {
        path: 'category',
        name: 'StoreCategory',
        redirect: '/logistics/commerce/category',
        meta: { title: '商品大类（兼容）', hidden: true }
      },
      {
        path: 'product',
        name: 'StoreProduct',
        redirect: '/logistics/commerce/product',
        meta: { title: '商品管理（兼容）', hidden: true }
      },
      {
        path: 'tag',
        name: 'StoreTag',
        redirect: '/logistics/commerce/tag',
        meta: { title: '商品标签（兼容）', hidden: true }
      },
      {
        path: 'order',
        name: 'StoreOrder',
        redirect: '/logistics/commerce/order',
        meta: { title: '订单管理（兼容）', hidden: true }
      },
      {
        path: 'points',
        name: 'StorePoints',
        redirect: '/logistics/commerce/points',
        meta: { title: '积分账户（兼容）', hidden: true }
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
        redirect: '/logistics/storage/stock-query',
        meta: { hidden: true }
      },
      {
        path: 'overview',
        redirect: '/logistics/storage/stock-query',
        meta: { title: '库存总览' }
      },
      {
        path: 'inbound',
        redirect: '/logistics/storage/inbound',
        meta: { title: '入库管理' }
      },
      {
        path: 'outbound',
        redirect: '/logistics/storage/outbound',
        meta: { title: '出库记录' }
      },
      {
        path: 'alerts',
        redirect: '/logistics/storage/alerts',
        meta: { title: '预警中心' }
      },
      {
        path: 'adjustments',
        redirect: '/logistics/storage/stock-check',
        meta: { title: '盘点记录' }
      }
    ]
  },
  // 长者模块历史兼容跳转（旧路径 → 新页面）
  {
    path: 'elder/admission-assessment',
    name: 'ElderAdmissionAssessment',
    redirect: '/elder/assessment/ability/admission',
    meta: { title: '入住评估', hidden: true }
  },
  {
    path: 'elder/discharge-settlement',
    name: 'ElderDischargeSettlement',
    redirect: '/elder/status-change/discharge-settlement',
    meta: { hidden: true }
  },
  {
    path: 'elder/admission',
    name: 'ElderAdmission',
    redirect: '/elder/admission-processing',
    meta: { hidden: true }
  },
  {
    path: 'elder/outing',
    name: 'ElderOuting',
    redirect: '/elder/status-change/outing',
    meta: { hidden: true }
  },
  {
    path: 'elder/visit-register',
    name: 'ElderVisitRegister',
    redirect: '/elder/status-change/visit-register',
    meta: { hidden: true }
  },
  {
    path: 'elder/discharge-apply',
    name: 'ElderDischargeApply',
    redirect: '/elder/status-change/discharge-apply',
    meta: { hidden: true }
  },
  {
    path: 'elder/trial-stay',
    name: 'ElderTrialStay',
    redirect: '/elder/status-change/trial-stay',
    meta: { hidden: true }
  },
  {
    path: 'elder/medical-outing',
    name: 'ElderMedicalOutingRegister',
    redirect: '/elder/status-change/medical-outing',
    meta: { hidden: true }
  },
  {
    path: 'elder/death-register',
    name: 'ElderDeathRegister',
    redirect: '/elder/status-change/death-register',
    meta: { hidden: true }
  }
]
