<template>
  <PageContainer title="来访登记" subTitle="访客预约核销与到访记录">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="老人姓名">
          <a-select v-model:value="query.elderId" allow-clear style="width: 180px" placeholder="请选择老人姓名">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="来访状态">
          <a-select v-model:value="query.status" allow-clear style="width: 160px" placeholder="请选择来访状态">
            <a-select-option :value="0">待登记</a-select-option>
            <a-select-option :value="1">已登记</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" :loading="loading" @click="fetchData">搜索</a-button>
            <a-button @click="reset">清空</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-table :data-source="rows" :columns="columns" :loading="loading" row-key="id" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'orange'">
              {{ record.status === 1 ? '已登记' : '待登记' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" :disabled="record.status === 1" @click="checkin(record)">登记到访</a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getElderPage } from '../../api/elder'
import { guardCheckin, guardTodayVisits } from '../../api/visit'
import type { ElderItem, PageResult, VisitBookingItem } from '../../types'

const loading = ref(false)
const rows = ref<VisitBookingItem[]>([])
const elders = ref<ElderItem[]>([])
const query = reactive({
  elderId: undefined as number | undefined,
  status: undefined as number | undefined
})

const columns = [
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '来访时间', dataIndex: 'visitTime', key: 'visitTime', width: 180 },
  { title: '访客人数', dataIndex: 'visitorCount', key: 'visitorCount', width: 100 },
  { title: '车牌', dataIndex: 'carPlate', key: 'carPlate', width: 120 },
  { title: '核销码', dataIndex: 'visitCode', key: 'visitCode', width: 160 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 120 }
]

async function loadElders() {
  const res: PageResult<ElderItem> = await getElderPage({ pageNo: 1, pageSize: 200 })
  elders.value = res.list
}

function resolveElderName(elderId?: number) {
  const elder = elders.value.find((item) => item.id === elderId)
  return elder?.fullName || `#${elderId || '-'}`
}

async function fetchData() {
  loading.value = true
  try {
    const source = await guardTodayVisits()
    rows.value = source
      .filter((item) => (query.elderId ? item.elderId === query.elderId : true))
      .filter((item) => (query.status !== undefined ? item.status === query.status : true))
      .map((item) => ({
        ...item,
        elderName: resolveElderName(item.elderId)
      }))
  } finally {
    loading.value = false
  }
}

function reset() {
  query.elderId = undefined
  query.status = undefined
  fetchData()
}

async function checkin(record: VisitBookingItem) {
  await guardCheckin({ bookingId: record.id })
  message.success('来访登记成功')
  fetchData()
}

onMounted(async () => {
  await loadElders()
  await fetchData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
