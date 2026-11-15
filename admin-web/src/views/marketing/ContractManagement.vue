<template>
  <PageContainer title="合同到期管理" sub-title="合同到期提醒、短信触达与统一维护">
    <MarketingQuickNav parent-path="/marketing/contracts" />
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="合同编号">
          <a-input v-model:value="query.contractNo" placeholder="请输入 合同编号" allow-clear />
        </a-form-item>
        <a-form-item label="姓名">
          <a-input v-model:value="query.elderName" placeholder="请输入 姓名" allow-clear />
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model:value="query.elderPhone" placeholder="请输入 联系电话" allow-clear />
        </a-form-item>
        <a-form-item label="负责人">
          <a-input v-model:value="query.marketerName" placeholder="请输入 负责人/营销人员" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">搜 索</a-button>
            <a-button @click="reset">清 空</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <MarketingListToolbar
        :selected-count="selectedCount"
        :tip="canDeleteContract ? '支持批量短信、批量删除' : '支持单条或批量发送续签提醒短信'"
      >
        <a-space>
          <a-button @click="exportList">导出当前页</a-button>
          <a-button v-if="canDeleteContract" @click="reminderConfigOpen = true">提醒规则设置</a-button>
          <a-button :disabled="selectedCount !== 1" @click="sendSmsSelected">短信</a-button>
          <a-button :disabled="selectedCount === 0" @click="batchSms">批量发短信</a-button>
          <a-button v-if="canDeleteContract" :disabled="selectedCount !== 1" danger @click="removeSelected">删除</a-button>
          <a-button v-if="canDeleteContract" :disabled="selectedCount === 0" danger @click="batchDelete">批量删除</a-button>
          <a-button @click="showSmsTasks">短信任务记录</a-button>
        </a-space>
      </MarketingListToolbar>
      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :row-selection="rowSelection"
        :scroll="{ x: 1800 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'countdownDays'">
            <span>{{ calcCountdown(record.contractExpiryDate) }}</span>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal v-model:open="smsTaskOpen" title="短信任务记录" :footer="null" width="760px">
      <a-table :data-source="smsTasks" :columns="smsTaskColumns" :pagination="false" row-key="id" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'operation'">
            <a-button v-if="record.status !== 'SENT'" type="link" @click="sendTask(record.id)">立即发送</a-button>
            <span v-else>-</span>
          </template>
        </template>
      </a-table>
    </a-modal>

    <a-modal
      v-model:open="reminderConfigOpen"
      title="合同到期提醒规则"
      ok-text="保存"
      :confirm-loading="reminderConfigSaving"
      @ok="saveReminderConfig"
    >
      <a-form layout="vertical">
        <a-form-item label="启用到期提醒">
          <a-switch v-model:checked="reminderConfig.enabled" />
        </a-form-item>
        <a-form-item label="提前提醒天数" required>
          <a-input-number v-model:value="reminderConfig.advanceDays" :min="1" :max="365" style="width: 100%" />
        </a-form-item>
        <a-form-item label="计划发送时间" required>
          <a-input v-model:value="reminderConfig.sendTime" type="time" />
        </a-form-item>
        <a-form-item label="短信内容" required>
          <a-textarea v-model:value="reminderConfig.templateContent" :rows="4" :maxlength="255" show-count />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import MarketingQuickNav from './components/MarketingQuickNav.vue'
import MarketingListToolbar from './components/MarketingListToolbar.vue'
import { useUserStore } from '../../stores/user'
import { exportCsv } from '../../utils/export'
import { hasAnyRole } from '../../utils/roleAccess'
import {
  batchDeleteContracts,
  createContractSmsTasks,
  deleteCrmContract,
  getContractPage,
  getContractReminderConfig,
  getContractSmsTasks,
  saveContractReminderConfig as persistReminderConfig,
  sendContractSmsTask
} from '../../api/marketing'
import type { CrmContractItem, CrmContractReminderConfig, PageResult, SmsTaskItem } from '../../types'

const loading = ref(false)
const userStore = useUserStore()
const canDeleteContract = computed(() => hasAnyRole(userStore.roles || [], [
  'MARKETING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'
]))
const rows = ref<CrmContractItem[]>([])
const total = ref(0)
const selectedRowKeys = ref<string[]>([])
const selectedContracts = ref<CrmContractItem[]>([])

const query = reactive({
  contractNo: '',
  elderName: '',
  elderPhone: '',
  marketerName: '',
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo', width: 170 },
  { title: '签约房号', dataIndex: 'reservationRoomNo', key: 'reservationRoomNo', width: 150 },
  { title: '姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '性别', dataIndex: 'gender', key: 'gender', width: 80 },
  { title: '年龄', dataIndex: 'age', key: 'age', width: 80 },
  { title: '联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 140 },
  { title: '负责人', dataIndex: 'marketerName', key: 'marketerName', width: 120 },
  { title: '签约时间', dataIndex: 'contractSignedAt', key: 'contractSignedAt', width: 170 },
  { title: '合同有效期', dataIndex: 'contractExpiryDate', key: 'contractExpiryDate', width: 140 },
  { title: '域状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '发送次数', dataIndex: 'smsSendCount', key: 'smsSendCount', width: 100 },
  { title: '倒计时', key: 'countdownDays', width: 100 },
  { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 }
]

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: Array<string | number>, selectedRows: CrmContractItem[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
    selectedContracts.value = selectedRows
  }
}))
const selectedCount = computed(() => selectedRowKeys.value.length)

function requireSingleSelection(actionLabel: string) {
  if (selectedContracts.value.length !== 1) {
    message.info(`请先勾选 1 条合同后再${actionLabel}`)
    return null
  }
  return selectedContracts.value[0]
}

const reminderConfig = reactive<CrmContractReminderConfig>({
  enabled: true,
  advanceDays: 30,
  sendTime: '09:00',
  templateContent: '您好，您的合同即将到期，请及时联系机构续签。'
})
const reminderConfigOpen = ref(false)
const reminderConfigSaving = ref(false)

const smsTaskOpen = ref(false)
const smsTasks = ref<SmsTaskItem[]>([])
const smsTaskColumns = [
  { title: '任务ID', dataIndex: 'id', key: 'id', width: 100 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 120 },
  { title: '模板', dataIndex: 'templateName', key: 'templateName', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '计划发送时间', dataIndex: 'planSendTime', key: 'planSendTime', width: 170 },
  { title: '发送时间', dataIndex: 'sendTime', key: 'sendTime', width: 170 },
  { title: '结果', dataIndex: 'resultMessage', key: 'resultMessage', width: 140 },
  { title: '操作', key: 'operation', width: 100 }
]

function statusText(status?: CrmContractItem['status']) {
  if (status === 'DRAFT') return '草稿'
  if (status === 'PENDING_APPROVAL') return '待审批'
  if (status === 'APPROVED') return '已审批'
  if (status === 'REJECTED') return '已驳回'
  if (status === 'SIGNED') return '已签署'
  if (status === 'EFFECTIVE') return '已生效'
  if (status === 'VOID') return '已作废'
  return status || '-'
}

function statusColor(status?: CrmContractItem['status']) {
  if (status === 'SIGNED' || status === 'EFFECTIVE') return 'green'
  if (status === 'APPROVED') return 'blue'
  if (status === 'PENDING_APPROVAL') return 'gold'
  if (status === 'REJECTED') return 'volcano'
  if (status === 'VOID') return 'default'
  return 'default'
}

async function fetchData() {
  loading.value = true
  try {
    const page: PageResult<CrmContractItem> = await getContractPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      contractNo: query.contractNo || undefined,
      elderName: query.elderName || undefined,
      elderPhone: query.elderPhone || undefined,
      marketerName: query.marketerName || undefined,
      // 续签提醒仅针对已签约合同
      flowStage: 'SIGNED'
    })
    rows.value = page.list || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

function calcCountdown(expiry?: string) {
  if (!expiry) return 0
  const days = dayjs(expiry).diff(dayjs(), 'day')
  return days > 0 ? days : 0
}

function reset() {
  query.contractNo = ''
  query.elderName = ''
  query.elderPhone = ''
  query.marketerName = ''
  query.pageNo = 1
  selectedRowKeys.value = []
  selectedContracts.value = []
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  selectedContracts.value = []
  fetchData()
}

async function sendSms(record: CrmContractItem) {
  if (record.status === 'VOID') {
    message.warning('作废合同不可发送短信')
    return
  }
  const tasks = await createContractSmsTasks({
    contractIds: [record.id],
    templateName: '合同到期提醒',
    content: reminderConfig.templateContent,
    planSendTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
  })
  if (tasks?.[0]?.id) {
    await sendContractSmsTask(tasks[0].id)
  }
  message.success('短信发送成功')
  fetchData()
}

async function batchSms() {
  if (!selectedRowKeys.value.length) {
    message.info('请先勾选合同')
    return
  }
  const targetIds = selectedContracts.value
    .filter((item) => item.status !== 'VOID')
    .map((item) => item.id)
  if (!targetIds.length) {
    message.warning('选中合同均为作废状态，无法发送短信')
    return
  }
  const tasks = await createContractSmsTasks({
    contractIds: targetIds,
    templateName: '合同到期提醒',
    content: reminderConfig.templateContent,
    planSendTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
  })
  await Promise.all((tasks || []).map((item) => sendContractSmsTask(item.id)))
  message.success(`已批量发送 ${tasks.length} 条短信`)
  fetchData()
}

async function sendSmsSelected() {
  const contract = requireSingleSelection('发送短信')
  if (!contract) return
  await sendSms(contract)
}

function removeRow(record: CrmContractItem) {
  Modal.confirm({
    title: `确认删除合同 ${record.contractNo || '-'} 吗？`,
    onOk: async () => {
      if (record.contractNo) {
        await batchDeleteContracts({ contractNos: [record.contractNo] })
      } else {
        await deleteCrmContract(record.id)
      }
      selectedRowKeys.value = selectedRowKeys.value.filter((id) => String(id) !== String(record.id))
      selectedContracts.value = selectedContracts.value.filter((item) => item.contractNo !== record.contractNo)
      message.success('合同已删除')
      fetchData()
    }
  })
}

function removeSelected() {
  const contract = requireSingleSelection('删除')
  if (!contract) return
  removeRow(contract)
}

function batchDelete() {
  const contractNos = selectedContracts.value
    .map((item) => item.contractNo)
    .filter((item): item is string => Boolean(item))
  if (!contractNos.length && !selectedRowKeys.value.length) {
    message.info('请先勾选合同')
    return
  }
  Modal.confirm({
    title: `确认删除选中的 ${selectedRowKeys.value.length} 条合同吗？`,
    onOk: async () => {
      const affected = await batchDeleteContracts(
        contractNos.length ? { contractNos } : { ids: selectedRowKeys.value }
      )
      selectedRowKeys.value = []
      selectedContracts.value = []
      if (!affected) {
        message.warning('未删除任何合同，请刷新后重试')
        return
      }
      message.success(`批量删除成功（${affected} 条）`)
      fetchData()
    }
  })
}

function exportList() {
  exportCsv(rows.value.map((item) => ({
    合同编号: item.contractNo,
    长者姓名: item.elderName,
    联系电话: item.elderPhone,
    负责人: item.marketerName,
    签约房号: item.reservationRoomNo,
    签约时间: item.contractSignedAt,
    合同有效期: item.contractExpiryDate,
    合同状态: statusText(item.status),
    短信发送次数: item.smsSendCount,
    到期倒计时天数: calcCountdown(item.contractExpiryDate)
  })), `marketing-contract-renewal-${Date.now()}.csv`)
}

async function showSmsTasks() {
  smsTasks.value = await getContractSmsTasks()
  smsTaskOpen.value = true
}

async function sendTask(taskId: number) {
  await sendContractSmsTask(taskId)
  smsTasks.value = await getContractSmsTasks()
  fetchData()
}

async function loadReminderConfig() {
  Object.assign(reminderConfig, await getContractReminderConfig())
}

async function saveReminderConfig() {
  if (!reminderConfig.templateContent.trim()) {
    message.warning('请填写短信内容')
    return
  }
  reminderConfigSaving.value = true
  try {
    Object.assign(reminderConfig, await persistReminderConfig({ ...reminderConfig }))
    reminderConfigOpen.value = false
    message.success('提醒规则已保存')
  } finally {
    reminderConfigSaving.value = false
  }
}

onMounted(() => Promise.all([fetchData(), loadReminderConfig()]))
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
