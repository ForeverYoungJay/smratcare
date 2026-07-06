<template>
  <PageContainer title="设备健康监控" subTitle="心跳在线率 / 电量 / 信号巡检（心跳超时自动离线并生成设备离线告警）">
    <a-row :gutter="16" class="stat-row">
      <a-col :xs="12" :md="4">
        <a-card size="small"><a-statistic title="设备总数" :value="summary.totalCount" /></a-card>
      </a-col>
      <a-col :xs="12" :md="4">
        <a-card size="small"><a-statistic title="在线" :value="summary.onlineCount" :value-style="{ color: '#52c41a' }" /></a-card>
      </a-col>
      <a-col :xs="12" :md="4">
        <a-card size="small"><a-statistic title="离线" :value="summary.offlineCount" :value-style="{ color: '#ff4d4f' }" /></a-card>
      </a-col>
      <a-col :xs="12" :md="4">
        <a-card size="small">
          <a-statistic :title="`低电量(≤${summary.lowBatteryThreshold ?? 20}%)`" :value="summary.lowBatteryCount" :value-style="{ color: '#fa8c16' }" />
        </a-card>
      </a-col>
      <a-col :xs="12" :md="4">
        <a-card size="small">
          <a-statistic :title="`弱信号(≤${summary.weakSignalThreshold ?? 30})`" :value="summary.weakSignalCount" :value-style="{ color: '#fa8c16' }" />
        </a-card>
      </a-col>
      <a-col :xs="12" :md="4">
        <a-card size="small">
          <a-statistic :title="`心跳超时(>${summary.offlineMinutes ?? 10}分钟)`" :value="summary.staleHeartbeatCount" :value-style="{ color: '#faad14' }" />
        </a-card>
      </a-col>
    </a-row>

    <SearchForm :model="query" @search="onSearch" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" allow-clear style="width: 200px" placeholder="设备名/编码/位置" />
      </a-form-item>
      <a-form-item label="在线状态">
        <a-select v-model:value="query.onlineStatus" allow-clear style="width: 130px" placeholder="全部">
          <a-select-option value="ONLINE">在线</a-select-option>
          <a-select-option value="OFFLINE">离线</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="仅低电量">
        <a-switch v-model:checked="query.lowBatteryOnly" />
      </a-form-item>
      <a-form-item label="仅弱信号">
        <a-switch v-model:checked="query.weakSignalOnly" />
      </a-form-item>
      <template #extra>
        <a-button @click="fetchAll">刷新</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'onlineStatus'">
          <a-badge :status="record.onlineStatus === 'ONLINE' ? 'success' : 'error'" :text="record.onlineStatus === 'ONLINE' ? '在线' : '离线'" />
        </template>
        <template v-else-if="column.key === 'battery'">
          <a-progress
            v-if="record.batteryLevel != null"
            :percent="record.batteryLevel"
            :size="'small'"
            :status="record.batteryLevel <= (summary.lowBatteryThreshold ?? 20) ? 'exception' : 'normal'"
            style="width: 110px"
          />
          <span v-else>-</span>
        </template>
        <template v-else-if="column.key === 'signal'">
          <a-tag v-if="record.signalStrength != null" :color="record.signalStrength <= (summary.weakSignalThreshold ?? 30) ? 'orange' : 'green'">
            {{ record.signalStrength }}
          </a-tag>
          <span v-else>-</span>
        </template>
        <template v-else-if="column.key === 'lastHeartbeatAt'">{{ record.lastHeartbeatAt || record.lastEventAt || '-' }}</template>
        <template v-else-if="column.key === 'firmwareVersion'">{{ record.firmwareVersion || '-' }}</template>
        <template v-else-if="column.key === 'enabled'">
          <a-tag :color="record.enabled === 1 ? 'blue' : 'default'">{{ record.enabled === 1 ? '启用' : '停用' }}</a-tag>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getSmartDeviceHealthPage, getSmartDeviceHealthSummary } from '../../api/smartCare'
import type { PageResult, SmartDevice, SmartDeviceHealthSummary } from '../../types'

const loading = ref(false)
const rows = ref<SmartDevice[]>([])
const summary = reactive<SmartDeviceHealthSummary>({
  totalCount: 0,
  onlineCount: 0,
  offlineCount: 0,
  lowBatteryCount: 0,
  weakSignalCount: 0,
  staleHeartbeatCount: 0
})
const query = reactive({
  keyword: undefined as string | undefined,
  onlineStatus: undefined as string | undefined,
  lowBatteryOnly: false,
  weakSignalOnly: false,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '设备编码', dataIndex: 'deviceCode', key: 'deviceCode', width: 140 },
  { title: '设备名称', dataIndex: 'deviceName', key: 'deviceName', width: 150 },
  { title: '类型', dataIndex: 'deviceType', key: 'deviceType', width: 110 },
  { title: '位置', dataIndex: 'location', key: 'location', width: 130 },
  { title: '在线状态', key: 'onlineStatus', width: 100 },
  { title: '电量', key: 'battery', width: 130 },
  { title: '信号', key: 'signal', width: 80 },
  { title: '最近心跳', key: 'lastHeartbeatAt', width: 160 },
  { title: '固件', key: 'firmwareVersion', width: 100 },
  { title: '启停', key: 'enabled', width: 80 }
]

async function fetchSummary() {
  const res = await getSmartDeviceHealthSummary()
  Object.assign(summary, res)
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<SmartDevice> = await getSmartDeviceHealthPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      onlineStatus: query.onlineStatus,
      lowBatteryOnly: query.lowBatteryOnly || undefined,
      weakSignalOnly: query.weakSignalOnly || undefined
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

function fetchAll() {
  fetchSummary()
  fetchData()
}

function onSearch() {
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function onReset() {
  query.keyword = undefined
  query.onlineStatus = undefined
  query.lowBatteryOnly = false
  query.weakSignalOnly = false
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

fetchAll()
</script>

<style scoped>
.stat-row {
  margin-bottom: 16px;
}
</style>
