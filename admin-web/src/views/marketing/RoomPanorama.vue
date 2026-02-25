<template>
  <PageContainer title="房态全景" sub-title="实时掌握空床率、在住率与楼层房态">
    <a-row :gutter="16">
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="总床位" :value="stats.total" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="入住床位" :value="stats.occupied" />
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="空床率" :value="stats.freeRate" suffix="%" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="房间号">
          <a-input v-model:value="query.roomNo" placeholder="房间号" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 140px">
            <a-select-option :value="1">空床</a-select-option>
            <a-select-option :value="2">入住</a-select-option>
            <a-select-option :value="3">维修</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="applySearch">筛选</a-button>
        </a-form-item>
      </a-form>
      <a-table
        :columns="columns"
        :data-source="filteredRows"
        :loading="loading"
        row-key="id"
        :pagination="{ pageSize: 12 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record)">{{ statusText(record) }}</a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { getBedMap } from '../../api/bed'
import type { BedItem } from '../../types'

const loading = ref(false)
const rows = ref<BedItem[]>([])
const query = reactive({
  roomNo: '',
  status: undefined as number | undefined
})

const columns = [
  { title: '房间号', dataIndex: 'roomNo', key: 'roomNo', width: 120 },
  { title: '床位号', dataIndex: 'bedNo', key: 'bedNo', width: 120 },
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '护理等级', dataIndex: 'careLevel', key: 'careLevel', width: 120 },
  { title: '状态', key: 'status', width: 100 }
]

const filteredRows = computed(() => rows.value.filter((item) => {
  if (query.roomNo && !(item.roomNo || '').includes(query.roomNo)) return false
  if (query.status && item.status !== query.status) return false
  return true
}))

const stats = computed(() => {
  const total = rows.value.length
  const occupied = rows.value.filter((item) => item.status === 2 || !!item.elderId).length
  const freeRate = total === 0 ? 0 : Number((((total - occupied) / total) * 100).toFixed(1))
  return { total, occupied, freeRate }
})

function statusText(item: BedItem) {
  if (item.status === 3) return '维修'
  if (item.status === 2 || item.elderId) return '入住'
  return '空床'
}

function statusColor(item: BedItem) {
  if (item.status === 3) return 'red'
  if (item.status === 2 || item.elderId) return 'blue'
  return 'green'
}

function applySearch() {
  // retained for user interaction consistency
}

async function fetchData() {
  loading.value = true
  try {
    rows.value = await getBedMap()
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
