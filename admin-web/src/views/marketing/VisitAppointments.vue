<template>
  <PageContainer title="客户预约参观" sub-title="安排潜客到访、登记签到、记录参观结果并自动生成后续跟进">
    <a-card :bordered="false" class="card-elevated">
      <a-form layout="inline" :model="query">
        <a-form-item label="客户/电话">
          <a-input v-model:value="query.keyword" allow-clear placeholder="客户、联系人或电话" @press-enter="search" />
        </a-form-item>
        <a-form-item label="预约状态">
          <a-select v-model:value="query.status" allow-clear style="width: 150px" placeholder="全部状态">
            <a-select-option v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="预约日期">
          <a-range-picker v-model:value="query.dateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="search">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <div class="appointment-summary">
        <a-tag color="blue">今日待到访 {{ todayScheduledCount }}</a-tag>
        <a-tag color="green">已到访 {{ arrivedCount }}</a-tag>
        <a-tag color="orange">待填写结果 {{ arrivedCount }}</a-tag>
      </div>

      <div class="appointment-actions">
        <a-space wrap>
          <a-button type="primary" @click="openForm()">新增预约</a-button>
          <a-button :disabled="selectedRows.length !== 1 || selectedRows[0]?.status !== 'SCHEDULED'" @click="openForm(selectedRows[0])">编辑</a-button>
          <a-button :disabled="selectedRows.length !== 1 || selectedRows[0]?.status !== 'SCHEDULED'" @click="arriveSelected">登记到访</a-button>
          <a-button :disabled="selectedRows.length !== 1 || selectedRows[0]?.status !== 'ARRIVED'" type="primary" @click="openComplete">填写参观结果</a-button>
          <a-button :disabled="selectedRows.length !== 1 || !canCancel(selectedRows[0])" danger @click="openCancel">取消预约</a-button>
        </a-space>
      </div>

      <StatefulBlock :loading="loading" :error="errorText" :empty="!rows.length" @retry="fetchData">
        <a-table
          row-key="id"
          :columns="columns"
          :data-source="rows"
          :pagination="pagination"
          :row-selection="rowSelection"
          :scroll="{ x: 1320 }"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'customer'">
              <div><strong>{{ record.elderName || record.leadName || '-' }}</strong></div>
              <div class="secondary-text">{{ record.visitorName }} · {{ record.visitorPhone || '-' }}</div>
            </template>
            <template v-else-if="column.key === 'appointmentTime'">
              <span :class="{ 'overdue-time': isOverdue(record) }">{{ formatDateTime(record.appointmentTime) }}</span>
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
            </template>
            <template v-else-if="column.key === 'result'">
              <span>{{ record.visitResult || record.cancelReason || '-' }}</span>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button v-if="record.status === 'SCHEDULED'" type="link" size="small" @click="arrive(record)">到访</a-button>
                <a-button v-if="record.status === 'ARRIVED'" type="link" size="small" @click="openComplete(record)">填结果</a-button>
                <a-button v-if="record.status === 'SCHEDULED'" type="link" size="small" @click="openForm(record)">编辑</a-button>
                <a-button v-if="canCancel(record)" type="link" size="small" danger @click="openCancel(record)">取消</a-button>
              </a-space>
            </template>
          </template>
        </a-table>
      </StatefulBlock>
    </a-card>

    <a-modal v-model:open="formOpen" :title="form.id ? '编辑参观预约' : '新增参观预约'" :mask-closable="false" :confirm-loading="saving" @ok="submitForm">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="formRules">
        <a-form-item label="客户线索" name="leadId">
          <a-select
            v-model:value="form.leadId"
            show-search
            :filter-option="filterLeadOption"
            placeholder="输入姓名或电话选择客户"
            @change="fillFromLead"
          >
            <a-select-option v-for="lead in leadOptions" :key="String(lead.id)" :value="String(lead.id)">
              {{ lead.elderName || lead.name || lead.consultantName }} · {{ lead.elderPhone || lead.phone || lead.consultantPhone || '无电话' }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="预约参观时间" name="appointmentTime">
          <a-date-picker v-model:value="form.appointmentTime" show-time format="YYYY-MM-DD HH:mm" style="width: 100%" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="来访联系人" name="visitorName"><a-input v-model:value="form.visitorName" /></a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="联系电话"><a-input v-model:value="form.visitorPhone" /></a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="来访人数"><a-input-number v-model:value="form.partySize" :min="1" :max="20" style="width: 100%" /></a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="接待负责人"><a-input v-model:value="form.ownerStaffName" placeholder="默认取客户负责人" /></a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="预约备注"><a-textarea v-model:value="form.remark" :rows="3" placeholder="例如行动不便、需要无障碍路线、重点关注房型" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="completeOpen" title="填写参观结果" :mask-closable="false" :confirm-loading="saving" @ok="submitComplete">
      <a-form layout="vertical" :model="completeForm">
        <a-form-item label="参观结果" required>
          <a-textarea v-model:value="completeForm.visitResult" :rows="4" placeholder="记录客户关注点、意向变化和需要协助的事项" />
        </a-form-item>
        <a-form-item label="下次跟进日期">
          <a-date-picker v-model:value="completeForm.nextFollowDate" format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="补充备注"><a-textarea v-model:value="completeForm.remark" :rows="2" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="cancelOpen" title="取消参观预约" :mask-closable="false" :confirm-loading="saving" @ok="submitCancel">
      <a-form layout="vertical">
        <a-form-item label="取消原因" required>
          <a-textarea v-model:value="cancelReason" :rows="3" placeholder="请填写客户取消或改期原因" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import {
  arriveVisitAppointment,
  cancelVisitAppointment,
  completeVisitAppointment,
  createVisitAppointment,
  getLeadPage,
  getVisitAppointmentPage,
  updateVisitAppointment
} from '../../api/marketing'
import type { CrmLeadItem, CrmVisitAppointmentItem, PageResult } from '../../types'

const statusOptions = [
  { value: 'SCHEDULED', label: '待到访' },
  { value: 'ARRIVED', label: '已到访' },
  { value: 'COMPLETED', label: '参观完成' },
  { value: 'CANCELLED', label: '已取消' },
  { value: 'NO_SHOW', label: '未到访' }
]
const columns = [
  { title: '客户/联系人', key: 'customer', width: 210 },
  { title: '预约时间', key: 'appointmentTime', width: 170 },
  { title: '人数', dataIndex: 'partySize', key: 'partySize', width: 70 },
  { title: '接待负责人', dataIndex: 'ownerStaffName', key: 'ownerStaffName', width: 120 },
  { title: '状态', key: 'status', width: 100 },
  { title: '参观结果/取消原因', key: 'result', width: 260 },
  { title: '下次跟进', dataIndex: 'nextFollowDate', key: 'nextFollowDate', width: 120 },
  { title: '操作', key: 'action', width: 190, fixed: 'right' as const }
]

const loading = ref(false)
const saving = ref(false)
const errorText = ref('')
const rows = ref<CrmVisitAppointmentItem[]>([])
const total = ref(0)
const selectedRows = ref<CrmVisitAppointmentItem[]>([])
const leadOptions = ref<CrmLeadItem[]>([])
const query = reactive({ keyword: '', status: undefined as string | undefined, dateRange: [] as string[] , pageNo: 1, pageSize: 10 })
const formOpen = ref(false)
const completeOpen = ref(false)
const cancelOpen = ref(false)
const formRef = ref<FormInstance>()
const activeRecord = ref<CrmVisitAppointmentItem>()
const form = reactive({
  id: undefined as string | number | undefined,
  leadId: undefined as string | undefined,
  appointmentTime: null as Dayjs | null,
  visitorName: '',
  visitorPhone: '',
  partySize: 1,
  ownerStaffName: '',
  remark: ''
})
const completeForm = reactive({ visitResult: '', nextFollowDate: null as Dayjs | null, remark: '' })
const cancelReason = ref('')
const formRules: FormRules = {
  leadId: [{ required: true, message: '请选择客户线索' }],
  appointmentTime: [{ required: true, message: '请选择预约参观时间' }],
  visitorName: [{ required: true, message: '请填写来访联系人' }]
}

const pagination = computed(() => ({ current: query.pageNo, pageSize: query.pageSize, total: total.value, showSizeChanger: true, showTotal: (value: number) => `共 ${value} 条` }))
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRows.value.map(item => String(item.id)),
  onChange: (_keys: Array<string | number>, records: CrmVisitAppointmentItem[]) => { selectedRows.value = records }
}))
const todayScheduledCount = computed(() => rows.value.filter(item => item.status === 'SCHEDULED' && dayjs(item.appointmentTime).isSame(dayjs(), 'day')).length)
const arrivedCount = computed(() => rows.value.filter(item => item.status === 'ARRIVED').length)

onMounted(async () => {
  await Promise.all([fetchData(), loadLeads()])
})

async function fetchData() {
  loading.value = true
  errorText.value = ''
  try {
    const page: PageResult<CrmVisitAppointmentItem> = await getVisitAppointmentPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      status: query.status,
      dateFrom: query.dateRange?.[0],
      dateTo: query.dateRange?.[1]
    })
    rows.value = page.list || []
    total.value = Number(page.total || 0)
    selectedRows.value = []
  } catch (error: any) {
    errorText.value = error?.message || '预约参观数据加载失败'
  } finally {
    loading.value = false
  }
}

async function loadLeads() {
  const page: PageResult<CrmLeadItem> = await getLeadPage({ pageNo: 1, pageSize: 100 })
  leadOptions.value = page.list || []
}

function search() { query.pageNo = 1; fetchData() }
function reset() { query.keyword = ''; query.status = undefined; query.dateRange = []; query.pageNo = 1; fetchData() }
function handleTableChange(page: any) { query.pageNo = page.current; query.pageSize = page.pageSize; fetchData() }

function openForm(record?: CrmVisitAppointmentItem) {
  formRef.value?.clearValidate()
  Object.assign(form, record ? {
    id: record.id,
    leadId: String(record.leadId),
    appointmentTime: dayjs(record.appointmentTime),
    visitorName: record.visitorName,
    visitorPhone: record.visitorPhone || '',
    partySize: record.partySize || 1,
    ownerStaffName: record.ownerStaffName || '',
    remark: record.remark || ''
  } : {
    id: undefined,
    leadId: undefined,
    appointmentTime: dayjs().add(1, 'day').hour(9).minute(0).second(0),
    visitorName: '',
    visitorPhone: '',
    partySize: 1,
    ownerStaffName: '',
    remark: ''
  })
  formOpen.value = true
}

function filterLeadOption(input: string, option: any) {
  return String(option?.children?.default?.() || option?.label || '').toLowerCase().includes(input.toLowerCase())
}

function fillFromLead(value: string) {
  const lead = leadOptions.value.find(item => String(item.id) === String(value))
  if (!lead) return
  form.visitorName = lead.consultantName || lead.elderName || lead.name || ''
  form.visitorPhone = lead.consultantPhone || lead.elderPhone || lead.phone || ''
  form.ownerStaffName = lead.ownerStaffName || lead.marketerName || ''
}

async function submitForm() {
  await formRef.value?.validate()
  if (!form.leadId || !form.appointmentTime) return
  saving.value = true
  try {
    const payload = {
      leadId: form.leadId,
      appointmentTime: form.appointmentTime.format('YYYY-MM-DD HH:mm:ss'),
      visitorName: form.visitorName,
      visitorPhone: form.visitorPhone || undefined,
      partySize: form.partySize,
      ownerStaffName: form.ownerStaffName || undefined,
      remark: form.remark || undefined
    }
    if (form.id) await updateVisitAppointment(form.id, payload)
    else await createVisitAppointment(payload)
    message.success(form.id ? '预约已更新' : '预约已创建，并已同步客户下次跟进日期')
    formOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

function arriveSelected() { if (selectedRows.value[0]) arrive(selectedRows.value[0]) }
function arrive(record: CrmVisitAppointmentItem) {
  Modal.confirm({
    title: `确认 ${record.visitorName} 已到访？`,
    content: '登记后即可填写参观结果。',
    onOk: async () => { await arriveVisitAppointment(record.id); message.success('已登记到访'); fetchData() }
  })
}

function openComplete(record?: CrmVisitAppointmentItem) {
  activeRecord.value = record || selectedRows.value[0]
  if (!activeRecord.value) return
  completeForm.visitResult = activeRecord.value.visitResult || ''
  completeForm.nextFollowDate = dayjs().add(1, 'day')
  completeForm.remark = ''
  completeOpen.value = true
}

async function submitComplete() {
  if (!activeRecord.value || !completeForm.visitResult.trim()) {
    message.warning('请填写参观结果')
    return
  }
  saving.value = true
  try {
    await completeVisitAppointment(activeRecord.value.id, {
      visitResult: completeForm.visitResult.trim(),
      nextFollowDate: completeForm.nextFollowDate?.format('YYYY-MM-DD'),
      remark: completeForm.remark || undefined
    })
    message.success('参观结果已保存，下次跟进已同步到客户')
    completeOpen.value = false
    fetchData()
  } finally { saving.value = false }
}

function canCancel(record?: CrmVisitAppointmentItem) { return !!record && (record.status === 'SCHEDULED' || record.status === 'ARRIVED') }
function openCancel(record?: CrmVisitAppointmentItem) { activeRecord.value = record || selectedRows.value[0]; cancelReason.value = ''; cancelOpen.value = !!activeRecord.value }
async function submitCancel() {
  if (!activeRecord.value || !cancelReason.value.trim()) { message.warning('请填写取消原因'); return }
  saving.value = true
  try {
    await cancelVisitAppointment(activeRecord.value.id, cancelReason.value.trim())
    message.success('预约已取消并保留原因')
    cancelOpen.value = false
    fetchData()
  } finally { saving.value = false }
}

function statusLabel(status: string) { return statusOptions.find(item => item.value === status)?.label || status }
function statusColor(status: string) { return ({ SCHEDULED: 'blue', ARRIVED: 'orange', COMPLETED: 'green', CANCELLED: 'default', NO_SHOW: 'red' } as Record<string, string>)[status] || 'default' }
function formatDateTime(value?: string) { return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '-' }
function isOverdue(record: CrmVisitAppointmentItem) { return record.status === 'SCHEDULED' && dayjs(record.appointmentTime).isBefore(dayjs()) }
</script>

<style scoped>
.appointment-summary { margin: 16px 0 8px; }
.appointment-actions { margin: 12px 0; }
.secondary-text { color: rgba(0, 0, 0, 0.45); font-size: 12px; margin-top: 2px; }
.overdue-time { color: #cf1322; font-weight: 600; }
</style>
