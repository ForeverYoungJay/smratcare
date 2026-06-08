<template>
  <PageContainer title="家属沟通" subTitle="处理家属端在线沟通、语音留言与探视预约">
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
          <a-button :disabled="!selectedRecord" @click="markSelected('DONE')">完成处理</a-button>
          <a-button :disabled="!selectedRecord" danger @click="markSelected('CLOSED')">关闭</a-button>
        </a-space>
      </template>
    </SearchForm>

    <a-alert
      type="info"
      show-icon
      style="margin-bottom: 12px"
      message="家属端消息会同步生成处理单；在这里更新状态后，家属小程序聊天气泡会显示对应进度。"
    />

    <StatefulBlock :loading="loading" :error="error" :empty="!loading && !error && rows.length === 0" empty-text="暂无家属沟通记录" @retry="fetchData">
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
            </div>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" :disabled="record.status === 'PROCESSING'" @click="mark(record, 'PROCESSING')">处理中</a-button>
              <a-button size="small" type="primary" :disabled="isDone(record.status)" @click="mark(record, 'DONE')">完成</a-button>
              <a-button size="small" danger :disabled="record.status === 'CLOSED'" @click="mark(record, 'CLOSED')">关闭</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getSuggestionPage, updateSuggestionStatus } from '../../api/oa'
import type { Id, OaSuggestion } from '../../types'

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: undefined as string | undefined,
  status: undefined as string | undefined,
  familyCommunicationOnly: true
})

const rows = ref<OaSuggestion[]>([])
const total = ref(0)
const loading = ref(false)
const error = ref('')
const selectedRowKeys = ref<Id[]>([])

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
    error.value = err?.message || '家属沟通记录加载失败'
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

function parseMessage(content?: string) {
  const text = String(content || '')
  const titleMatch = text.match(/^\[家属沟通]\[([^\]]+)]\[([^\]]+)]/)
  const mediaMatch = text.match(/mediaUrl=([^|]+)/)
  return {
    title: titleMatch ? `${titleMatch[1]} · ${messageTypeText(titleMatch[2])}` : '家属沟通',
    body: text.replace(/^\[家属沟通]\[[^\]]+]\[[^\]]+]\s*/, '').split('|')[0].trim() || '-',
    mediaUrl: mediaMatch ? mediaMatch[1].trim() : ''
  }
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
  color: #1f2937;
}

.message-body {
  margin-top: 4px;
  color: #4b5563;
  white-space: pre-wrap;
}

.message-media {
  margin-top: 6px;
  color: #0f766e;
  font-size: 12px;
  word-break: break-all;
}
</style>
