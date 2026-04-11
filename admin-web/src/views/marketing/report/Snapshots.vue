<template>
  <PageContainer title="经营快照" sub-title="查看销售工作台与报表快照，便于复盘和口径回放">
    <MarketingQuickNav parent-path="/marketing/reports" />
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline">
        <a-form-item label="快照类型">
          <a-select v-model:value="query.snapshotType" style="width: 220px" allow-clear>
            <a-select-option
              v-for="item in MARKETING_SNAPSHOT_TYPE_OPTIONS"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="最近条数">
          <a-input-number v-model:value="query.limit" :min="1" :max="50" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <a-table :loading="loading" :columns="columns" :data-source="rows" row-key="id" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'snapshotType'">
            {{ snapshotTypeLabel(record.snapshotType) }}
          </template>
          <template v-else-if="column.key === 'operation'">
            <a-button type="link" size="small" @click="openSnapshot(record)">查看明细</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-drawer v-model:open="detailOpen" title="快照明细" width="720">
      <a-descriptions v-if="currentSnapshot" :column="1" bordered size="small">
        <a-descriptions-item label="类型">{{ snapshotTypeLabel(currentSnapshot.snapshotType) }}</a-descriptions-item>
        <a-descriptions-item label="统计窗口">{{ currentSnapshot.windowFrom || '-' }} ~ {{ currentSnapshot.windowTo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="生成时间">{{ currentSnapshot.generatedAt || '-' }}</a-descriptions-item>
        <a-descriptions-item label="生成人">{{ currentSnapshot.generatedByName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="快照键">{{ currentSnapshot.snapshotKey || '-' }}</a-descriptions-item>
        <a-descriptions-item label="指标JSON">
          <pre class="snapshot-json">{{ prettyJson(currentSnapshot.metricsJson) }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '../../../components/PageContainer.vue'
import MarketingQuickNav from '../components/MarketingQuickNav.vue'
import { getMarketingReportSnapshots } from '../../../api/marketing'
import { MARKETING_SNAPSHOT_TYPE_LABELS, MARKETING_SNAPSHOT_TYPE_OPTIONS } from '../../../utils/marketingEnums'
import type { CrmSalesReportSnapshotItem } from '../../../types'

const loading = ref(false)
const rows = ref<CrmSalesReportSnapshotItem[]>([])
const detailOpen = ref(false)
const currentSnapshot = ref<CrmSalesReportSnapshotItem>()
const query = reactive({
  snapshotType: 'MARKETING_WORKBENCH',
  limit: 10
})

const columns = [
  { title: '快照类型', dataIndex: 'snapshotType', key: 'snapshotType', width: 180 },
  { title: '快照日期', dataIndex: 'snapshotDate', key: 'snapshotDate', width: 140 },
  { title: '窗口开始', dataIndex: 'windowFrom', key: 'windowFrom', width: 140 },
  { title: '窗口结束', dataIndex: 'windowTo', key: 'windowTo', width: 140 },
  { title: '生成时间', dataIndex: 'generatedAt', key: 'generatedAt', width: 180 },
  { title: '生成人', dataIndex: 'generatedByName', key: 'generatedByName', width: 120 },
  { title: '操作', key: 'operation', width: 100, fixed: 'right' as const }
]

async function loadData() {
  loading.value = true
  try {
    rows.value = await getMarketingReportSnapshots({
      snapshotType: query.snapshotType || undefined,
      limit: query.limit || 10
    })
  } finally {
    loading.value = false
  }
}

function reset() {
  query.snapshotType = 'MARKETING_WORKBENCH'
  query.limit = 10
  loadData()
}

function snapshotTypeLabel(type?: string) {
  return MARKETING_SNAPSHOT_TYPE_LABELS[String(type || '').trim()] || type || '-'
}

function prettyJson(value?: string) {
  if (!value) return '{}'
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

function openSnapshot(record: CrmSalesReportSnapshotItem) {
  currentSnapshot.value = record
  detailOpen.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.snapshot-json {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  color: #173854;
  font-size: 12px;
  line-height: 1.6;
}
</style>
