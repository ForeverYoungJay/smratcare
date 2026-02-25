<template>
  <PageContainer title="合同管理" sub-title="合同到期提醒、短信触达与统一维护">
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
        <a-form-item label="营销人员">
          <a-input v-model:value="query.marketerName" placeholder="请选择营销人员" allow-clear />
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
      <div class="table-actions">
        <a-space>
          <a-button @click="exportList">导出</a-button>
          <a-button @click="batchSms">批量发短信</a-button>
          <a-button @click="settingReminder">到期提醒设置</a-button>
        </a-space>
      </div>
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
          <template v-else-if="column.key === 'operation'">
            <a-button type="link" @click="sendSms(record)">短信</a-button>
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
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { createSmsTasks, getLeadPage, getSmsTasks, sendSmsTask } from '../../api/marketing'
import type { CrmLeadItem, PageResult, SmsTaskItem } from '../../types'

const loading = ref(false)
const rows = ref<CrmLeadItem[]>([])
const total = ref(0)
const selectedRowKeys = ref<number[]>([])

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
  { title: '营销人员', dataIndex: 'marketerName', key: 'marketerName', width: 120 },
  { title: '签约时间', dataIndex: 'contractSignedAt', key: 'contractSignedAt', width: 170 },
  { title: '合同有效期', dataIndex: 'contractExpiryDate', key: 'contractExpiryDate', width: 140 },
  { title: '发送次数', dataIndex: 'smsSendCount', key: 'smsSendCount', width: 100 },
  { title: '倒计时', key: 'countdownDays', width: 100 },
  { title: '所属机构', dataIndex: 'orgName', key: 'orgName', width: 120 },
  { title: '操作', key: 'operation', fixed: 'right', width: 100 }
]

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (number | string)[]) => {
    selectedRowKeys.value = keys.map((item) => Number(item))
  }
}))

const reminderConfig = reactive({
  beforeDays: 30,
  templateName: '合同到期提醒',
  content: '您好，您的合同即将到期，请及时联系机构续签。'
})

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

async function fetchData() {
  loading.value = true
  try {
    const page: PageResult<CrmLeadItem> = await getLeadPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: 2,
      contractNo: query.contractNo || undefined,
      elderName: query.elderName || undefined,
      elderPhone: query.elderPhone || undefined,
      marketerName: query.marketerName || undefined
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
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

async function sendSms(record: CrmLeadItem) {
  const tasks = await createSmsTasks({
    leadIds: [record.id],
    templateName: reminderConfig.templateName,
    content: reminderConfig.content,
    planSendTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
  })
  if (tasks?.[0]?.id) {
    await sendSmsTask(tasks[0].id)
  }
  message.success('短信发送成功')
  fetchData()
}

async function batchSms() {
  if (!selectedRowKeys.value.length) {
    message.info('请先勾选合同')
    return
  }
  const tasks = await createSmsTasks({
    leadIds: selectedRowKeys.value,
    templateName: reminderConfig.templateName,
    content: reminderConfig.content,
    planSendTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
  })
  await Promise.all((tasks || []).map((item) => sendSmsTask(item.id)))
  message.success(`已批量发送 ${tasks.length} 条短信`)
  fetchData()
}

function exportList() {
  message.success('导出任务已提交')
}

async function settingReminder() {
  smsTasks.value = await getSmsTasks()
  smsTaskOpen.value = true
}

async function sendTask(taskId: number) {
  await sendSmsTask(taskId)
  smsTasks.value = await getSmsTasks()
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}

.table-actions {
  margin-bottom: 12px;
}
</style>
