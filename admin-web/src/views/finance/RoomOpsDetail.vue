<template>
  <PageContainer title="房间经营详情" subTitle="楼层/房间维度的收入、净额与床态联动">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query">
        <a-form-item label="期间">
          <a-date-picker v-model:value="query.period" picker="month" style="width: 150px" />
        </a-form-item>
        <a-form-item label="楼栋">
          <a-input v-model:value="query.building" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item label="房间">
          <a-input v-model:value="query.room" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="[16, 16]" style="margin-top: 16px;">
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="总收入" :value="detail?.totalIncome || 0" :precision="2" suffix="元" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="总净额" :value="detail?.totalNetAmount || 0" :precision="2" suffix="元" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="在住床位" :value="detail?.totalOccupiedBeds || 0" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="空床床位" :value="detail?.totalEmptyBeds || 0" /></a-card></a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <vxe-table border stripe show-overflow :loading="loading" :data="detail?.rows || []" height="560">
        <vxe-column field="building" title="楼栋" width="120" />
        <vxe-column field="floorNo" title="楼层" width="120" />
        <vxe-column field="roomNo" title="房间" width="120" />
        <vxe-column field="income" title="收入" width="130" />
        <vxe-column field="cost" title="成本" width="130" />
        <vxe-column field="netAmount" title="净额" width="130" />
        <vxe-column field="occupiedBeds" title="在住床位" width="100" />
        <vxe-column field="emptyBeds" title="空床床位" width="100" />
      </vxe-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getFinanceRoomOpsDetail } from '../../api/finance'
import type { FinanceRoomOpsDetailResponse } from '../../types'

const route = useRoute()
const loading = ref(false)
const detail = ref<FinanceRoomOpsDetailResponse | null>(null)
const query = ref({
  period: dayjs(),
  building: String(route.query.building || ''),
  room: String(route.query.room || '')
})

async function loadData() {
  loading.value = true
  try {
    detail.value = await getFinanceRoomOpsDetail({
      period: dayjs(query.value.period).format('YYYY-MM'),
      building: query.value.building || undefined,
      room: query.value.room || undefined
    })
  } finally {
    loading.value = false
  }
}

function reset() {
  query.value.period = dayjs()
  query.value.building = ''
  query.value.room = ''
  loadData()
}

onMounted(loadData)
</script>
