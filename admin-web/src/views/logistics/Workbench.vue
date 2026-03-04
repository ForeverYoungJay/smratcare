<template>
  <PageContainer title="后勤工作台" subTitle="资产、物资、餐饮、维修一体化后勤保障总览">
    <template #extra>
      <a-space>
        <a-tag :color="dutyMode ? 'gold' : 'default'">{{ dutyMode ? '值班高密度模式' : '标准模式' }}</a-tag>
        <a-switch v-model:checked="dutyMode" checked-children="值班" un-checked-children="标准" />
      </a-space>
    </template>
    <StatefulBlock :loading="loading" :error="errorMessage" :empty="!summary" empty-text="暂无后勤数据" @retry="loadSummary">
      <a-row :gutter="dutyMode ? 8 : 12">
        <a-col
          :xs="24"
          :md="dutyMode ? 8 : 12"
          :xl="dutyMode ? 6 : 8"
          v-for="card in cards"
          :key="card.key"
          :style="{ marginBottom: dutyMode ? '8px' : '12px' }"
        >
          <a-card class="card-elevated" :class="{ 'card-duty': dutyMode }" :bordered="false" :title="card.title">
            <template #extra>
              <a-tag :color="card.color">{{ card.tag }}</a-tag>
            </template>
            <div class="line-item" v-for="line in visibleLines(card.lines)" :key="line">{{ line }}</div>
            <a-space wrap :style="{ marginTop: dutyMode ? '8px' : '12px' }">
              <a-button
                v-for="action in visibleActions(card.actions)"
                :key="action.label"
                :type="action.primary ? 'primary' : 'default'"
                :disabled="action.disabled"
                :size="dutyMode ? 'small' : 'middle'"
                @click="go(action.path)"
              >
                {{ action.label }}
              </a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import {
  generateEquipmentMaintenanceTodos,
  getLogisticsWorkbenchSummary,
  rerunLatestFailedMaintenanceTodoJobLog
} from '../../api/logistics'
import type { LogisticsWorkbenchSummary } from '../../types'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const errorMessage = ref('')
const summary = ref<LogisticsWorkbenchSummary>()
const dutyMode = ref(false)

const cards = computed(() => {
  const data = summary.value
  if (!data) return []
  const weekTop = data.weekTopConsumption?.slice(0, 5).map((item) => `${item.name}(${item.quantity})`).join('、') || '-'
  const monthTop = data.monthTopConsumption?.slice(0, 10).map((item) => `${item.name}(${item.quantity})`).join('、') || '-'
  return [
    {
      key: 'inventory-alert',
      title: '卡片1：库存预警中心',
      tag: '核心',
      color: 'red',
      lines: [
        `低库存物品：${data.lowStockCount}，临期物品：${data.expiringCount}`,
        `今日出库数量：${data.todayOutboundQty}，库存总金额：${amount(data.inventoryTotalAmount)}`,
        `本周消耗Top5：${weekTop}`
      ],
      actions: [
        { label: '查看低库存', path: '/logistics/storage/alerts?lowStockOnly=true', primary: true },
        { label: '查看临期', path: '/logistics/storage/alerts?expiryDays=30' },
        { label: '发起采购', path: '/logistics/storage/purchase?from=low_stock' },
        { label: '查看库存报表', path: '/logistics/reports/stock-amount' }
      ]
    },
    {
      key: 'purchase-progress',
      title: '卡片2：采购执行进度',
      tag: '采购',
      color: 'blue',
      lines: [
        `待审批：${data.purchasePendingApprovalCount}，待到货：${data.purchaseApprovedNotArrivedCount}`,
        `已入库未对账：${data.purchaseReceivedNotSettledCount}`,
        `本月采购金额：${amount(data.monthPurchaseAmount)}`
      ],
      actions: [
        { label: '待审批采购单', path: '/logistics/storage/purchase?status=DRAFT', primary: true },
        { label: '待到货采购单', path: '/logistics/storage/purchase?status=APPROVED' },
        { label: '已入库未对账', path: '/logistics/storage/purchase?status=COMPLETED' }
      ]
    },
    {
      key: 'bed-asset',
      title: '卡片3：床态与资产运行状态',
      tag: '资产',
      color: 'geekblue',
      lines: [
        `在住床位：${data.occupiedBeds}，空闲床位：${data.freeBeds}`,
        `清洁中床位：${data.cleaningBeds}，维修中床位：${data.maintenanceBeds}`,
        `今日退住：${data.todayDischargeCount}，今日新入住：${data.todayAdmissionCount}`
      ],
      actions: [
        { label: '床态全景', path: '/elder/bed-panorama?filter=occupied', primary: true },
        { label: '清洁中床位', path: '/life/room-cleaning?status=PENDING' },
        { label: '维修中床位', path: '/logistics/assets/maintenance-record?status=PROCESSING' }
      ]
    },
    {
      key: 'maintenance',
      title: '卡片4：维修与报障中心',
      tag: '维修',
      color: 'orange',
      lines: [
        `待维修：${data.maintenancePendingCount}，处理中：${data.maintenanceProcessingCount}`,
        `超时维修：${data.maintenanceOverdueCount}，设备维保中：${data.equipmentMaintainingCount}`,
        `本月维修成本：${amount(data.monthMaintenanceCost)}`
      ],
      actions: [
        { label: '待维修', path: '/logistics/assets/maintenance-record?status=OPEN', primary: true },
        { label: '超时维修', path: '/logistics/assets/maintenance-record?filter=overdue' },
        { label: '维修成本', path: '/logistics/maintenance/cost' },
        { label: '设备档案', path: '/logistics/maintenance/assets' },
        { label: '生成维保待办', path: 'action:generate-maintenance-todos' },
        { label: '失败重跑', path: 'action:rerun-latest-failed-maintenance-todos', disabled: data.maintenanceTodoFailedCount7d <= 0 },
        { label: '执行日志', path: '/logistics/reports/maintenance-todo-log' }
      ]
    },
    {
      key: 'dining-overview',
      title: '卡片5：餐饮执行总览',
      tag: '餐饮',
      color: 'green',
      lines: [
        `今日订餐人数：${data.todayMealOrderCount}，个性化餐：${data.personalizedMealCount}`,
        `送餐完成率：${percent(data.deliveryCompletionRate)}，未送达：${data.undeliveredCount}`,
        `今日餐饮成本估算：${amount(data.todayDiningCost)}`
      ],
      actions: [
        { label: '订餐详情', path: '/logistics/dining/stats?date=today', primary: true },
        { label: '未送达', path: '/logistics/dining/delivery-plan?filter=undelivered' },
        { label: '餐饮成本', path: '/logistics/reports/dining-cost' }
      ]
    },
    {
      key: 'dining-risk',
      title: '卡片6：个性化点餐与禁忌预警',
      tag: '膳食风险',
      color: 'volcano',
      lines: [
        `糖尿病餐：${data.diabetesMealCount}，低盐餐：${data.lowSaltMealCount}`,
        `过敏禁忌提醒：${data.allergyAlertCount}`,
        `特殊营养餐：${data.specialNutritionMealCount}`
      ],
      actions: [
        { label: '食谱管理', path: '/logistics/dining/recipe', primary: true },
        { label: '禁忌设置', path: '/logistics/commerce/risk' }
      ]
    },
    {
      key: 'consume-trend',
      title: '卡片7：物资消耗趋势',
      tag: '消耗',
      color: 'purple',
      lines: [
        `本月消耗金额：${amount(data.monthConsumeAmount)}`,
        `Top10消耗物资：${monthTop}`,
        `护理消耗 vs 餐饮消耗：${amount(data.nursingConsumeAmount)} / ${amount(data.diningConsumeAmount)}`
      ],
      actions: [{ label: '物资消耗报表', path: '/logistics/reports/consume', primary: true }]
    },
    {
      key: 'supply-chain',
      title: '卡片8：仓储四链路聚合',
      tag: '聚合',
      color: 'magenta',
      lines: [
        `库存(资/耗/食/服)：${data.inventoryAssetQty}/${data.inventoryConsumableQty}/${data.inventoryFoodQty}/${data.inventoryServiceQty}`,
        `低库存预警(资/耗/食/服)：${data.lowStockAssetCount}/${data.lowStockConsumableCount}/${data.lowStockFoodCount}/${data.lowStockServiceCount}`,
        `本月采购金额(资/耗/食/服)：${amount(data.monthPurchaseAssetAmount)} / ${amount(data.monthPurchaseConsumableAmount)} / ${amount(data.monthPurchaseFoodAmount)} / ${amount(data.monthPurchaseServiceAmount)}`,
        `今日出库(资/耗/食/服)：${data.todayOutboundAssetQty}/${data.todayOutboundConsumableQty}/${data.todayOutboundFoodQty}/${data.todayOutboundServiceQty}`
      ],
      actions: [
        { label: '库存总览', path: '/logistics/storage/stock-query?businessDomain=INTERNAL&itemType=CONSUMABLE', primary: true },
        { label: '库存预警', path: '/logistics/storage/alerts?focus=low&businessDomain=INTERNAL&itemType=CONSUMABLE' },
        { label: '采购单', path: '/logistics/storage/purchase?status=DRAFT&source=inventory_alert_batch' },
        { label: '出库记录', path: '/logistics/storage/outbound?outType=CONSUME&businessDomain=INTERNAL&itemType=CONSUMABLE' }
      ]
    },
    {
      key: 'task-load',
      title: '卡片9：后勤任务负载',
      tag: '任务',
      color: 'cyan',
      lines: [
        `今日清洁任务：${data.todayCleaningTaskCount}，今日维修任务：${data.todayMaintenanceTaskCount}`,
        `今日送餐任务：${data.todayDeliveryTaskCount}，今日库存盘点任务：${data.todayInventoryCheckTaskCount}`,
        `设备总数：${data.equipmentTotalCount}，30天内待维保：${data.equipmentDueSoonCount}`,
        `最近维保任务：${lastMaintenanceTodoSummary(data)}`
      ],
      actions: [
        { label: '任务中心', path: '/logistics/task-center?tab=cleaning', primary: true },
        { label: '维修任务', path: '/logistics/task-center?tab=maintenance' },
        { label: '送餐任务', path: '/logistics/task-center?tab=delivery' },
        { label: '盘点任务', path: '/logistics/task-center?tab=inventory' },
        { label: '维保设备', path: '/logistics/maintenance/assets?status=MAINTENANCE' },
        { label: '执行日志', path: '/logistics/reports/maintenance-todo-log' }
      ]
    }
  ]
})

function amount(value?: number) {
  const num = Number(value || 0)
  return `${num.toFixed(2)} 元`
}

function percent(value?: number) {
  return `${Number(value || 0).toFixed(2)}%`
}

function go(path: string) {
  if (path === 'action:generate-maintenance-todos') {
    triggerMaintenanceTodos()
    return
  }
  if (path === 'action:rerun-latest-failed-maintenance-todos') {
    rerunLatestFailed()
    return
  }
  router.push(path)
}

function visibleLines(lines: string[]) {
  if (!dutyMode.value) return lines
  return lines.slice(0, 2)
}

function visibleActions(actions: Array<{ label: string; path: string; primary?: boolean; disabled?: boolean }>) {
  if (!dutyMode.value) return actions
  return actions.slice(0, 3)
}

async function triggerMaintenanceTodos() {
  try {
    const res: any = await generateEquipmentMaintenanceTodos(30)
    const created = Number(res?.createdCount || 0)
    const skipped = Number(res?.skippedCount || 0)
    message.success(`已生成${created}条维保待办，跳过${skipped}条重复提醒`)
    loadSummary()
  } catch (error: any) {
    message.error(error?.message || '生成维保待办失败')
  }
}

function lastMaintenanceTodoSummary(data: LogisticsWorkbenchSummary) {
  if (!data.maintenanceTodoLastExecutedAt) {
    return '暂无执行记录'
  }
  const status = data.maintenanceTodoLastStatus === 'SUCCESS' ? '成功' : '失败'
  const triggerTypeLabelMap: Record<string, string> = {
    MANUAL: '手动',
    SCHEDULED: '自动',
    RETRY: '重跑'
  }
  const trigger = triggerTypeLabelMap[data.maintenanceTodoLastTriggerType || ''] || '未知'
  const counts = `新建${Number(data.maintenanceTodoLastCreatedCount || 0)} / 跳过${Number(data.maintenanceTodoLastSkippedCount || 0)}`
  const failedTip = data.maintenanceTodoFailedCount7d > 0 ? `，近7天失败${data.maintenanceTodoFailedCount7d}次` : ''
  const error = data.maintenanceTodoLastStatus === 'FAILED' && data.maintenanceTodoLastErrorMessage
    ? `，错误：${data.maintenanceTodoLastErrorMessage}`
    : ''
  return `${status}（${trigger}）${counts}${failedTip}${error}`
}

async function rerunLatestFailed() {
  try {
    const res: any = await rerunLatestFailedMaintenanceTodoJobLog()
    if (Number(res?.blockedByWindow || 0)) {
      message.warning('仅支持重跑30天内的失败记录')
      return
    }
    if (Number(res?.blockedByCooldown || 0)) {
      message.warning('重跑过于频繁，请2分钟后再试')
      return
    }
    if (!Number(res?.rerunTriggered || 0)) {
      message.info('暂无失败执行记录，无需重跑')
      return
    }
    message.success(`失败重跑完成：新建${Number(res?.createdCount || 0)}，跳过${Number(res?.skippedCount || 0)}`)
    loadSummary()
  } catch (error: any) {
    message.error(error?.message || '失败重跑触发失败')
  }
}

async function loadSummary() {
  loading.value = true
  errorMessage.value = ''
  try {
    summary.value = await getLogisticsWorkbenchSummary()
  } catch (error: any) {
    errorMessage.value = error?.message || '加载后勤工作台失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  dutyMode.value = String(Array.isArray(route.query.mode) ? route.query.mode[0] : route.query.mode || '').toLowerCase() === 'duty'
  loadSummary()
})
</script>

<style scoped>
.line-item {
  margin-top: 6px;
  color: #434343;
  font-size: 13px;
  line-height: 1.65;
}

.card-duty :deep(.ant-card-head) {
  min-height: 44px;
}

.card-duty :deep(.ant-card-head-title) {
  font-size: 14px;
}

.card-duty :deep(.ant-card-body) {
  padding: 12px;
}

.card-duty .line-item {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.45;
}
</style>
