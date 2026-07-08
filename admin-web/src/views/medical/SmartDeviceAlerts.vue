<template>
  <PageContainer title="智慧设备告警" subTitle="设备接入、事件上报、实时风险确认与处置闭环">
    <SearchForm :model="alertQuery" @search="fetchAll" @reset="onReset">
      <a-form-item label="告警状态">
        <a-select v-model:value="alertQuery.status" allow-clear style="width: 150px">
          <a-select-option value="OPEN">待确认</a-select-option>
          <a-select-option value="ACKNOWLEDGED">已确认</a-select-option>
          <a-select-option value="RESOLVED">已处置</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="等级">
        <a-select v-model:value="alertQuery.level" allow-clear style="width: 150px">
          <a-select-option value="CRITICAL">紧急</a-select-option>
          <a-select-option value="HIGH">高</a-select-option>
          <a-select-option value="WARNING">预警</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="关键词">
        <a-input v-model:value="alertQuery.keyword" allow-clear placeholder="标题/位置/内容" style="width: 220px" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="openDeviceModal()">新增设备</a-button>
          <a-button @click="openEventModal()">上报事件</a-button>
          <a-button :loading="refreshingDerived" @click="refreshDerivedAlerts">刷新趋势预警</a-button>
          <a-button type="primary" :loading="loading" @click="fetchAll">刷新</a-button>
        </a-space>
      </template>
    </SearchForm>

    <StatefulBlock :loading="loading" :error="errorMessage" @retry="fetchAll">
      <a-row :gutter="[12, 12]" style="margin-bottom: 16px">
        <a-col :xs="12" :lg="4"><a-card><a-statistic title="设备总数" :value="summary.totalDeviceCount" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card><a-statistic title="在线设备" :value="summary.onlineDeviceCount" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card><a-statistic title="离线设备" :value="summary.offlineDeviceCount" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card><a-statistic title="未闭环告警" :value="summary.openAlertCount" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card><a-statistic title="紧急告警" :value="summary.criticalAlertCount" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card><a-statistic title="趋势预警" :value="summary.derivedHealthAlertCount" /></a-card></a-col>
        <a-col :xs="12" :lg="4"><a-card><a-statistic title="今日事件" :value="summary.todayEventCount" /></a-card></a-col>
      </a-row>

      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :xl="15">
          <a-card title="告警闭环">
            <a-table
              :columns="alertColumns"
              :data-source="alerts"
              :pagination="alertPagination"
              row-key="id"
              size="small"
              :scroll="{ x: 1180 }"
              @change="handleAlertTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'level'">
                  <a-tag :color="levelColor(record.level)">{{ levelText(record.level) }}</a-tag>
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <div class="row-action-links">
                    <a-button type="link" size="small" :disabled="record.status !== 'OPEN'" @click="ackAlert(record)">确认</a-button>
                    <a-button type="link" size="small" :disabled="record.status === 'RESOLVED'" @click="openResolveModal(record)">处置</a-button>
                  </div>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="9">
          <a-card title="设备档案">
            <a-table
              :columns="deviceColumns"
              :data-source="devices"
              :pagination="devicePagination"
              row-key="id"
              size="small"
              @change="handleDeviceTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'onlineStatus'">
                  <a-tag :color="record.onlineStatus === 'ONLINE' ? 'green' : 'default'">{{ record.onlineStatus === 'ONLINE' ? '在线' : '离线' }}</a-tag>
                </template>
                <template v-else-if="column.key === 'enabled'">
                  <a-switch :checked="Number(record.enabled) === 1" size="small" @change="(checked) => toggleDevice(record, checked as boolean)" />
                </template>
                <template v-else-if="column.key === 'action'">
                  <div class="row-action-links">
                    <a-button type="link" size="small" @click="openDeviceModal(record)">编辑</a-button>
                  </div>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>
    </StatefulBlock>

    <a-modal v-model:open="deviceModalOpen" :title="deviceForm.id ? '编辑设备' : '新增设备'" @ok="submitDevice" :confirm-loading="saving" width="720px">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="设备编码" required><a-input v-model:value="deviceForm.deviceCode" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="设备名称" required><a-input v-model:value="deviceForm.deviceName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="设备类型" required><a-select v-model:value="deviceForm.deviceType" :options="deviceTypeOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="绑定长者ID"><a-input v-model:value="deviceForm.elderId" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="厂商"><a-input v-model:value="deviceForm.vendor" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="型号"><a-input v-model:value="deviceForm.model" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="协议"><a-select v-model:value="deviceForm.protocol" allow-clear :options="protocolOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="位置"><a-input v-model:value="deviceForm.location" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-textarea v-model:value="deviceForm.remark" :rows="2" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-model:open="eventModalOpen" title="设备事件上报" @ok="submitEvent" :confirm-loading="saving" width="720px">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="设备编码" required><a-input v-model:value="eventForm.deviceCode" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="事件类型" required><a-select v-model:value="eventForm.eventType" :options="eventTypeOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="事件等级"><a-select v-model:value="eventForm.eventLevel" :options="eventLevelOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="位置"><a-input v-model:value="eventForm.location" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="载荷 JSON"><a-textarea v-model:value="eventPayloadText" :rows="4" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-model:open="resolveModalOpen" title="处置告警" @ok="submitResolve" :confirm-loading="saving">
      <a-form layout="vertical">
        <a-form-item label="处置说明">
          <a-textarea v-model:value="resolveForm.resolutionNote" :rows="4" placeholder="填写到场情况、处理结果、后续观察要求" />
        </a-form-item>
        <a-checkbox v-model:checked="resolveForm.notifyFamily">同步给家属端</a-checkbox>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import {
  acknowledgeSmartAlert,
  createSmartDevice,
  getSmartAlertPage,
  getSmartAlertSummary,
  getSmartDevicePage,
  reportSmartDeviceEvent,
  refreshDerivedSmartAlerts,
  resolveSmartAlert,
  setSmartDeviceEnabled,
  updateSmartDevice
} from '../../api/smartCare'
import type { PageResult, SmartAlert, SmartAlertSummary, SmartDevice } from '../../types'

const loading = ref(false)
const saving = ref(false)
const refreshingDerived = ref(false)
const errorMessage = ref('')
const alerts = ref<SmartAlert[]>([])
const devices = ref<SmartDevice[]>([])
const summary = reactive<SmartAlertSummary>({
  totalDeviceCount: 0,
  onlineDeviceCount: 0,
  offlineDeviceCount: 0,
  openAlertCount: 0,
  criticalAlertCount: 0,
  derivedHealthAlertCount: 0,
  derivedHealthGeneratedCount: 0,
  todayEventCount: 0,
  levelStats: []
})

const alertQuery = reactive({ pageNo: 1, pageSize: 10, status: 'OPEN', level: undefined as string | undefined, keyword: '' })
const deviceQuery = reactive({ pageNo: 1, pageSize: 8 })
const alertPagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const devicePagination = reactive({ current: 1, pageSize: 8, total: 0, showSizeChanger: true })

const deviceModalOpen = ref(false)
const eventModalOpen = ref(false)
const resolveModalOpen = ref(false)
const selectedAlert = ref<SmartAlert | null>(null)
const eventPayloadText = ref('{"value":"test","source":"manual"}')

const deviceForm = reactive<Partial<SmartDevice>>({})
const eventForm = reactive({ deviceCode: '', eventType: 'SOS', eventLevel: 'CRITICAL', location: '' })
const resolveForm = reactive({ resolutionNote: '', notifyFamily: false })

const deviceTypeOptions = [
  { label: '紧急呼叫器', value: 'SOS_BUTTON' },
  { label: '定位手环', value: 'WRISTBAND' },
  { label: '智能床垫', value: 'BED_MATTRESS' },
  { label: '生命体征设备', value: 'VITAL_DEVICE' },
  { label: '门禁/电子围栏', value: 'ACCESS_CONTROL' }
]
const protocolOptions = [
  { label: 'HTTP', value: 'HTTP' },
  { label: 'MQTT', value: 'MQTT' },
  { label: '蓝牙网关', value: 'BLE_GATEWAY' }
]
const eventTypeOptions = [
  { label: '紧急呼叫', value: 'SOS' },
  { label: '跌倒', value: 'FALL' },
  { label: '生命体征异常', value: 'VITAL_ABNORMAL' },
  { label: '电子围栏', value: 'GEOFENCE' },
  { label: '心跳', value: 'HEARTBEAT' }
]
const eventLevelOptions = [
  { label: '信息', value: 'INFO' },
  { label: '预警', value: 'WARNING' },
  { label: '高', value: 'HIGH' },
  { label: '紧急', value: 'CRITICAL' }
]

const alertColumns = [
  { title: '等级', key: 'level', width: 90 },
  { title: '标题', dataIndex: 'title', width: 220 },
  { title: '位置', dataIndex: 'location', width: 160 },
  { title: '状态', key: 'status', width: 100 },
  { title: '触发时间', dataIndex: 'latestTriggeredAt', width: 180 },
  { title: '处置说明', dataIndex: 'resolutionNote', width: 220 },
  { title: '操作', key: 'action', fixed: 'right', width: 150 }
]
const deviceColumns = [
  { title: '设备', dataIndex: 'deviceName' },
  { title: '类型', dataIndex: 'deviceType', width: 130 },
  { title: '在线', key: 'onlineStatus', width: 80 },
  { title: '启用', key: 'enabled', width: 70 },
  { title: '操作', key: 'action', width: 70 }
]

async function fetchAll() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [summaryData, alertPage, devicePage] = await Promise.all([
      getSmartAlertSummary(),
      getSmartAlertPage(alertQuery),
      getSmartDevicePage(deviceQuery)
    ])
    Object.assign(summary, summaryData || {})
    applyAlertPage(alertPage)
    applyDevicePage(devicePage)
  } catch (error: any) {
    errorMessage.value = error?.message || '加载智慧设备告警失败'
  } finally {
    loading.value = false
  }
}

function applyAlertPage(page: PageResult<SmartAlert>) {
  alerts.value = page.list || []
  alertPagination.current = page.pageNo
  alertPagination.pageSize = page.pageSize
  alertPagination.total = page.total
}

function applyDevicePage(page: PageResult<SmartDevice>) {
  devices.value = page.list || []
  devicePagination.current = page.pageNo
  devicePagination.pageSize = page.pageSize
  devicePagination.total = page.total
}

function onReset() {
  alertQuery.pageNo = 1
  alertQuery.pageSize = 10
  alertQuery.status = 'OPEN'
  alertQuery.level = undefined
  alertQuery.keyword = ''
  fetchAll()
}

function handleAlertTableChange(pagination: any) {
  alertQuery.pageNo = pagination.current
  alertQuery.pageSize = pagination.pageSize
  fetchAll()
}

function handleDeviceTableChange(pagination: any) {
  deviceQuery.pageNo = pagination.current
  deviceQuery.pageSize = pagination.pageSize
  fetchAll()
}

function openDeviceModal(record?: SmartDevice) {
  Object.keys(deviceForm).forEach((key) => delete (deviceForm as any)[key])
  Object.assign(deviceForm, record || { deviceType: 'SOS_BUTTON', protocol: 'HTTP', enabled: 1 })
  deviceModalOpen.value = true
}

function openEventModal() {
  eventModalOpen.value = true
}

function openResolveModal(record: SmartAlert) {
  selectedAlert.value = record
  resolveForm.resolutionNote = record.resolutionNote || ''
  resolveForm.notifyFamily = Number(record.notifyFamily) === 1
  resolveModalOpen.value = true
}

async function submitDevice() {
  if (!deviceForm.deviceCode || !deviceForm.deviceName || !deviceForm.deviceType) {
    message.warning('请填写设备编码、名称和类型')
    return
  }
  if (saving.value) return
  saving.value = true
  try {
    if (deviceForm.id) {
      await updateSmartDevice(deviceForm.id, deviceForm)
    } else {
      await createSmartDevice(deviceForm)
    }
    message.success('设备已保存')
    deviceModalOpen.value = false
    fetchAll()
  } finally {
    saving.value = false
  }
}

async function submitEvent() {
  if (saving.value) return
  saving.value = true
  try {
    let payload: Record<string, any> | undefined
    if (eventPayloadText.value.trim()) {
      payload = JSON.parse(eventPayloadText.value)
    }
    await reportSmartDeviceEvent({ ...eventForm, payload })
    message.success('事件已上报')
    eventModalOpen.value = false
    fetchAll()
  } catch (error: any) {
    message.error(error instanceof SyntaxError ? '载荷 JSON 格式不正确' : (error?.message || '事件上报失败'))
  } finally {
    saving.value = false
  }
}

async function ackAlert(record: SmartAlert) {
  await acknowledgeSmartAlert(record.id)
  message.success('告警已确认')
  fetchAll()
}

async function refreshDerivedAlerts() {
  refreshingDerived.value = true
  try {
    const data = await refreshDerivedSmartAlerts()
    Object.assign(summary, data || {})
    message.success(`趋势预警已刷新，本次更新 ${data?.derivedHealthGeneratedCount || 0} 条`)
    fetchAll()
  } finally {
    refreshingDerived.value = false
  }
}

async function submitResolve() {
  if (!selectedAlert.value) return
  if (saving.value) return
  saving.value = true
  try {
    await resolveSmartAlert(selectedAlert.value.id, resolveForm)
    message.success('告警已处置')
    resolveModalOpen.value = false
    fetchAll()
  } finally {
    saving.value = false
  }
}

async function toggleDevice(record: SmartDevice, checked: boolean) {
  await setSmartDeviceEnabled(record.id, checked)
  message.success(checked ? '设备已启用' : '设备已停用')
  fetchAll()
}

function levelText(level: string) {
  return ({ CRITICAL: '紧急', HIGH: '高', WARNING: '预警', INFO: '信息' } as Record<string, string>)[level] || level
}

function levelColor(level: string) {
  return ({ CRITICAL: 'red', HIGH: 'orange', WARNING: 'gold', INFO: 'blue' } as Record<string, string>)[level] || 'default'
}

function statusText(status: string) {
  return ({ OPEN: '待确认', ACKNOWLEDGED: '已确认', RESOLVED: '已处置' } as Record<string, string>)[status] || status
}

function statusColor(status: string) {
  return ({ OPEN: 'red', ACKNOWLEDGED: 'blue', RESOLVED: 'green' } as Record<string, string>)[status] || 'default'
}

onMounted(fetchAll)
</script>
