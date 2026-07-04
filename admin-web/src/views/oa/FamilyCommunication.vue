<template>
  <PageContainer title="家属反馈与沟通" subTitle="处理家属端在线沟通、语音留言、满意度评价、投诉建议与探视预约">
    <template #meta>
      <span class="soft-pill">待处理 {{ pendingCount }}</span>
      <span class="soft-pill">处理中 {{ processingCount }}</span>
      <span class="soft-pill">已处理 {{ doneCount }}</span>
    </template>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="搜索内容/家属/电话" allow-clear style="width: 240px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 160px">
          <a-select-option value="PENDING">待处理</a-select-option>
          <a-select-option value="PROCESSING">处理中</a-select-option>
          <a-select-option value="DONE">已处理</a-select-option>
          <a-select-option value="CLOSED">已关闭</a-select-option>
        </a-select>
      </a-form-item>
      <template #extra>
        <a-space wrap>
          <a-button type="primary" @click="fetchData">刷新</a-button>
          <a-button :disabled="!selectedRecord" @click="markSelected('PROCESSING')">标记处理中</a-button>
          <a-button :disabled="!selectedRecord" type="primary" @click="openHandle(selectedRecord, 'DONE')">处理反馈</a-button>
          <a-button :disabled="!selectedRecord" danger @click="openHandle(selectedRecord, 'CLOSED')">关闭</a-button>
        </a-space>
      </template>
    </SearchForm>

    <a-alert
      type="info"
      show-icon
      style="margin-bottom: 12px"
      message="家属端消息会同步生成处理单；在这里更新状态后，家属小程序聊天气泡会显示对应进度。"
      description="投诉建议、满意度评价和在线沟通统一在这里受理，处理反馈会同步回家属端历史记录。"
    />

    <StatefulBlock :loading="loading" :error="error" :empty="!loading && !error && rows.length === 0" empty-text="暂无家属反馈与沟通记录" @retry="fetchData">
      <a-table
        row-key="id"
        :columns="columns"
        :data-source="rows"
        :pagination="pagination"
        :row-selection="{ selectedRowKeys, type: 'radio', onChange: onSelectChange }"
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'content'">
            <div class="message-content">
              <div class="message-title">{{ parseMessage(record.content).title }}</div>
              <div class="message-body">{{ parseMessage(record.content).body }}</div>
              <div v-if="parseMessage(record.content).mediaUrl" class="message-media">
                附件：{{ parseMessage(record.content).mediaUrl }}
              </div>
              <div v-if="parseMessage(record.content).reply" class="message-reply">
                处理反馈：{{ parseMessage(record.content).reply }}
              </div>
            </div>
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="row-action-links">
              <a-button type="link" size="small" :disabled="record.status === 'PROCESSING'" @click="mark(record, 'PROCESSING')">处理中</a-button>
              <a-button type="link" size="small" :disabled="isDone(record.status)" @click="openHandle(record, 'DONE')">反馈</a-button>
              <a-button type="link" size="small" danger :disabled="record.status === 'CLOSED'" @click="openHandle(record, 'CLOSED')">关闭</a-button>
            </div>
          </template>
        </template>
      </a-table>
    </StatefulBlock>

    <a-modal v-model:open="handleOpen" title="处理家属反馈" :confirm-loading="handleSubmitting" @ok="submitHandle">
      <a-form :model="handleForm" layout="vertical">
        <a-form-item label="处理状态">
          <a-select v-model:value="handleForm.status">
            <a-select-option value="PROCESSING">处理中</a-select-option>
            <a-select-option value="DONE">已处理</a-select-option>
            <a-select-option value="CLOSED">已关闭</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="给家属的处理反馈">
          <a-textarea
            v-model:value="handleForm.reply"
            :rows="4"
            :maxlength="500"
            show-count
            placeholder="例如：已安排护理主管复盘并在明日探视前电话回访。"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getSuggestionPage, handleSuggestion, updateSuggestionStatus } from '../../api/oa'
import type { Id, OaSuggestion } from '../../types'

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: undefined as string | undefined,
  status: undefined as string | undefined,
  familyCommunicationOnly: false
})

const rows = ref<OaSuggestion[]>([])
const total = ref(0)
const loading = ref(false)
const error = ref('')
const selectedRowKeys = ref<Id[]>([])
const handleOpen = ref(false)
const handleSubmitting = ref(false)
const handlingRecord = ref<OaSuggestion | null>(null)
const handleForm = reactive({
  status: 'DONE',
  reply: ''
})

const columns = [
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '家属', dataIndex: 'proposerName', key: 'proposerName', width: 120 },
  { title: '联系方式', dataIndex: 'contact', key: 'contact', width: 140 },
  { title: '沟通内容', dataIndex: 'content', key: 'content', ellipsis: true },
  { title: '提交时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 220, fixed: 'right' as const }
]

const pagination = computed(() => ({
  current: query.pageNo,
  pageSize: query.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (value: number) => `共 ${value} 条`
}))

const selectedRecord = computed(() => rows.value.find((item) => selectedRowKeys.value.includes(String(item.id))))
const pendingCount = computed(() => rows.value.filter((item) => item.status === 'PENDING').length)
const processingCount = computed(() => rows.value.filter((item) => item.status === 'PROCESSING').length)
const doneCount = computed(() => rows.value.filter((item) => isDone(item.status)).length)

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  error.value = ''
  try {
    const page = await getSuggestionPage(query)
    rows.value = page.list || []
    total.value = page.total || 0
    selectedRowKeys.value = selectedRowKeys.value.filter((id) => rows.value.some((item) => String(item.id) === String(id)))
  } catch (err: any) {
    error.value = err?.message || '家属反馈与沟通记录加载失败'
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.pageNo = 1
  query.keyword = undefined
  query.status = undefined
  fetchData()
}

function onTableChange(page: any) {
  query.pageNo = Number(page.current || 1)
  query.pageSize = Number(page.pageSize || 10)
  fetchData()
}

function onSelectChange(keys: Id[]) {
  selectedRowKeys.value = keys.map((item) => String(item))
}

async function markSelected(status: string) {
  if (!selectedRecord.value) return
  await mark(selectedRecord.value, status)
}

async function mark(record: OaSuggestion, status: string) {
  await updateSuggestionStatus(record.id, status)
  message.success(`已标记为${statusText(status)}`)
  await fetchData()
}

function openHandle(record?: OaSuggestion | null, status = 'DONE') {
  if (!record) return
  const parsed = parseMessage(record.content)
  handlingRecord.value = record
  handleForm.status = status
  handleForm.reply = parsed.reply || defaultReply(status)
  handleOpen.value = true
}

async function submitHandle() {
  if (!handlingRecord.value) return
  handleSubmitting.value = true
  try {
    await handleSuggestion(handlingRecord.value.id, {
      status: handleForm.status,
      reply: handleForm.reply
    })
    message.success(`已${statusText(handleForm.status)}`)
    handleOpen.value = false
    await fetchData()
  } finally {
    handleSubmitting.value = false
  }
}

function parseMessage(content?: string) {
  const text = String(content || '')
  const { bodyText, reply } = splitReply(text)
  const titleMatch = text.match(/^\[家属沟通]\[([^\]]+)]\[([^\]]+)]/)
  const feedbackMatch = text.match(/^\[(COMPLAINT|EVALUATION)]\[([^\]]+)]/)
  const mediaMatch = bodyText.match(/mediaUrl=([^|]+)/)
  return {
    title: titleMatch
      ? `${titleMatch[1]} · ${messageTypeText(titleMatch[2])}`
      : feedbackMatch
        ? `${feedbackMatch[1] === 'COMPLAINT' ? '投诉建议' : '服务评价'} · ${feedbackMatch[2]}`
        : '家属反馈',
    body: bodyText
      .replace(/^\[家属沟通]\[[^\]]+]\[[^\]]+]\s*/, '')
      .replace(/^\[(COMPLAINT|EVALUATION)]\[[^\]]+]\s*评分:[^\n]*\n?/, '')
      .split('|')[0]
      .trim() || '-',
    mediaUrl: mediaMatch ? mediaMatch[1].trim() : '',
    reply
  }
}

function splitReply(content: string) {
  const marker = '\n\n[处理反馈] '
  const index = content.indexOf(marker)
  if (index < 0) return { bodyText: content, reply: '' }
  return {
    bodyText: content.slice(0, index),
    reply: content.slice(index + marker.length).trim()
  }
}

function defaultReply(status: string) {
  if (status === 'CLOSED') return '已与家属沟通说明，本次反馈关闭归档。'
  if (status === 'PROCESSING') return '机构已受理，正在协调责任人跟进。'
  return '机构已完成处理，感谢您的反馈。'
}

function messageTypeText(type?: string) {
  if (type === 'voice') return '语音留言'
  if (type === 'video') return '探视预约'
  if (type === 'service') return '服务咨询'
  return '文字消息'
}

function statusText(status?: string) {
  if (status === 'PROCESSING') return '处理中'
  if (status === 'DONE' || status === 'ADOPTED' || status === 'RESOLVED') return '已处理'
  if (status === 'CLOSED' || status === 'REJECTED') return '已关闭'
  return '待处理'
}

function statusColor(status?: string) {
  if (status === 'PROCESSING') return 'blue'
  if (isDone(status)) return 'green'
  if (status === 'CLOSED' || status === 'REJECTED') return 'default'
  return 'gold'
}

function isDone(status?: string) {
  return status === 'DONE' || status === 'ADOPTED' || status === 'RESOLVED'
}
</script>

<style scoped>
.message-content {
  max-width: 640px;
}

.message-title {
  font-weight: 700;
  color: var(--ink);
}

.message-body {
  margin-top: 4px;
  color: var(--muted);
  white-space: pre-wrap;
}

.message-media {
  margin-top: 6px;
  color: var(--info);
  font-size: 12px;
  word-break: break-all;
}

.message-reply {
  margin-top: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  background: rgba(var(--success-rgb), 0.1);
  color: var(--success);
  font-size: 12px;
  line-height: 1.6;
}
</style>
