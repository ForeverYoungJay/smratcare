<template>
  <PageContainer title="来访登记" subTitle="访客预约核销与到访记录">
    <a-card class="card-elevated" :bordered="false">
      <a-space>
        <a-button type="primary" :loading="loading" @click="fetchData">刷新今日来访</a-button>
      </a-space>
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
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { guardCheckin, guardTodayVisits } from '../../api/visit'
import type { VisitBookingItem } from '../../types'

const loading = ref(false)
const rows = ref<VisitBookingItem[]>([])

const columns = [
  { title: '老人ID', dataIndex: 'elderId', key: 'elderId', width: 100 },
  { title: '来访时间', dataIndex: 'visitTime', key: 'visitTime', width: 180 },
  { title: '访客人数', dataIndex: 'visitorCount', key: 'visitorCount', width: 100 },
  { title: '车牌', dataIndex: 'carPlate', key: 'carPlate', width: 120 },
  { title: '核销码', dataIndex: 'visitCode', key: 'visitCode', width: 160 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 120 }
]

async function fetchData() {
  loading.value = true
  try {
    rows.value = await guardTodayVisits()
  } finally {
    loading.value = false
  }
}

async function checkin(record: VisitBookingItem) {
  await guardCheckin({ bookingId: record.id })
  message.success('来访登记成功')
  fetchData()
}

onMounted(fetchData)
</script>
