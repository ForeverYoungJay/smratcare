<template>
  <PageContainer title="服务预约" subTitle="按长者发起预约类服务并查看执行状态">
    <template #extra>
      <a-space>
        <a-select v-model:value="statusFilter" style="width: 140px" allow-clear placeholder="预约状态" @change="loadBookings">
          <a-select-option value="PENDING">待执行</a-select-option>
          <a-select-option value="DONE">已完成</a-select-option>
          <a-select-option value="CANCELLED">已取消</a-select-option>
        </a-select>
        <a-button type="primary" @click="go('/care/service/service-bookings')">进入服务预定</a-button>
      </a-space>
    </template>

    <a-card class="card-elevated" :bordered="false">
      <StatefulBlock :loading="loading" :error="errorMessage" :empty="!rows.length" empty-text="暂无服务预约" @retry="loadBookings">
        <a-table :columns="columns" :data-source="rows" :pagination="false" row-key="id">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="statusColor(record.status)">{{ record.status || '-' }}</a-tag>
            </template>
          </template>
        </a-table>
      </StatefulBlock>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import { getServiceBookingPage } from '../../../api/nursing'
import type { ServiceBookingItem } from '../../../types'

const route = useRoute()
const router = useRouter()
const residentId = computed(() => Number(route.query.residentId || 0) || undefined)
const mode = computed(() => String(route.query.mode || ''))

const statusFilter = ref<string | undefined>(mode.value === 'booking' ? 'PENDING' : undefined)
const loading = ref(false)
const errorMessage = ref('')
const rows = ref<ServiceBookingItem[]>([])

const columns = [
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 100 },
  { title: '服务项目', dataIndex: 'serviceItemName', key: 'serviceItemName', width: 160 },
  { title: '计划', dataIndex: 'planName', key: 'planName' },
  { title: '预约时间', dataIndex: 'bookingTime', key: 'bookingTime', width: 170 },
  { title: '执行人', dataIndex: 'assignedStaffName', key: 'assignedStaffName', width: 100 },
  { title: '状态', key: 'status', width: 100 }
]

function go(path: string) {
  router.push(path)
}

function statusColor(status?: string) {
  if (status === 'DONE') return 'green'
  if (status === 'CANCELLED') return 'red'
  if (status === 'PENDING') return 'orange'
  return 'default'
}

async function loadBookings() {
  loading.value = true
  errorMessage.value = ''
  try {
    const page = await getServiceBookingPage({
      pageNo: 1,
      pageSize: 50,
      elderId: residentId.value,
      status: statusFilter.value,
      timeFrom: dayjs().subtract(30, 'day').format('YYYY-MM-DD 00:00:00'),
      timeTo: dayjs().add(30, 'day').format('YYYY-MM-DD 23:59:59')
    })
    rows.value = page.list || []
  } catch (error: any) {
    errorMessage.value = error?.message || '加载服务预约失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadBookings)
</script>
