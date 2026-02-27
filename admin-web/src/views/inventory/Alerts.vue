<template>
  <PageContainer title="库存预警" subTitle="低库存与临期预警中心">
    <a-card class="card-elevated" :bordered="false">
      <a-space>
        <a-button @click="exportSuggestions">导出采购建议</a-button>
        <a-button @click="fetchAll">刷新</a-button>
      </a-space>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-tabs v-model:activeKey="activeKey">
        <a-tab-pane key="low" tab="低库存预警">
          <a-table
            :columns="lowColumns"
            :data-source="lowRows"
            row-key="productId"
            :loading="loading"
            :pagination="false"
          />
        </a-tab-pane>
        <a-tab-pane key="expiry" tab="临期预警">
          <a-table
            :columns="expiryColumns"
            :data-source="expiryRows"
            row-key="batchId"
            :loading="loading"
            :pagination="false"
          />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { getInventoryAlerts, getInventoryExpiryAlerts } from '../../api/materialCenter'
import type { InventoryAlertItem, InventoryExpiryAlertItem } from '../../types'

const activeKey = ref('low')
const loading = ref(false)
const lowRows = ref<InventoryAlertItem[]>([])
const expiryRows = ref<InventoryExpiryAlertItem[]>([])

const lowColumns = [
  { title: '商品名称', dataIndex: 'productName' },
  { title: '商品ID', dataIndex: 'productId', width: 120 },
  { title: '安全库存', dataIndex: 'safetyStock', width: 120 },
  { title: '当前库存', dataIndex: 'currentStock', width: 120 }
]

const expiryColumns = [
  { title: '商品名称', dataIndex: 'productName' },
  { title: '批次号', dataIndex: 'batchNo', width: 160 },
  { title: '库存数量', dataIndex: 'quantity', width: 120 },
  { title: '有效期', dataIndex: 'expireDate', width: 140 },
  { title: '距离到期(天)', dataIndex: 'daysToExpire', width: 140 }
]

async function fetchAll() {
  loading.value = true
  try {
    lowRows.value = await getInventoryAlerts()
    expiryRows.value = await getInventoryExpiryAlerts()
  } finally {
    loading.value = false
  }
}

function exportSuggestions() {
  const data = [
    ...lowRows.value.map((r) => ({
      类型: '低库存',
      商品名称: r.productName,
      商品ID: r.productId,
      安全库存: r.safetyStock,
      当前库存: r.currentStock
    })),
    ...expiryRows.value.map((r) => ({
      类型: '临期',
      商品名称: r.productName,
      批次号: r.batchNo,
      库存数量: r.quantity,
      有效期: r.expireDate,
      距离到期: r.daysToExpire
    }))
  ]
  exportCsv(data, '库存预警清单')
}

onMounted(fetchAll)
</script>
