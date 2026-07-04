<template>
  <PageContainer title="员工建议中心" subTitle="集中处理员工小程序提交的流程优化、物资补给、护理协同和现场改进建议">
    <template #meta>
      <span class="soft-pill">待处理 {{ pendingCount }}</span>
      <span class="soft-pill">处理中 {{ processingCount }}</span>
      <span class="soft-pill">已处理 {{ doneCount }}</span>
    </template>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="搜索建议内容/提交人/联系方式" allow-clear style="width: 260px" />
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
          <a-button @click="openPath('/oa/staff-collaboration')">员工协同</a-button>
          <a-button :disabled="!selectedRecord" @click="markSelected('PROCESSING')">标记处理中</a-button>
          <a-button :disabled="!selectedRecord" type="primary" @click="openHandle(selectedRecord, 'DONE')">采纳反馈</a-button>
          <a-button :disabled="!selectedRecord" danger @click="openHandle(selectedRecord, 'CLOSED')">关闭归档</a-button>
        </a-space>
      </template>
    </SearchForm>

    <a-alert
      type="info"
      show-icon
      style="margin-bottom: 12px"
      message="员工端建议会保留原始内容、分类和处理反馈；更新状态后，后续可继续扩成员工端可回看结果。"
      description="这里优先处理流程优化、设备物资、培训排班和现场协同建议，涉及具体执行时可跳转审批、待办、维修或库存模块继续流转。"
    />

    <StatefulBlock :loading="loading" :error="error" :empty="!loading && !error && rows.length === 0" empty-text="暂无员工建议记录" @retry="fetchData">
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
              <div class="message-title">{{ parseSuggestion(record.content).title }}</div>
              <div class="message-body">{{ parseSuggestion(record.content).body }}</div>
              <div v-if="parseSuggestion(record.content).reply" class="message-reply">
                处理反馈：{{ parseSuggestion(record.content).reply }}
              </div>
            </div>
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="row-action-links">
              <a-button type="link" size="small" :disabled="record.status === 'PROCESSING'" @click="mark(record, 'PROCESSING')">处理中</a-button>
              <a-button type="link" size="small" @click="openSuggestedRoute(record)">关联业务</a-button>
              <a-button type="link" size="small" :disabled="isDone(record.status)" @click="openHandle(record, 'DONE')">采纳</a-button>
              <a-button type="link" size="small" danger :disabled="record.status === 'CLOSED'" @click="openHandle(record, 'CLOSED')">关闭</a-button>
            </div>
          </template>
        </template>
      </a-table>
    </StatefulBlock>

    <a-modal v-model:open="handleOpen" title="处理员工建议" :confirm-loading="handleSubmitting" @ok="submitHandle">
      <a-form :model="handleForm" layout="vertical">
        <a-form-item label="处理状态">
          <a-select v-model:value="handleForm.status">
            <a-select-option value="PROCESSING">处理中</a-select-option>
            <a-select-option value="DONE">已处理</a-select-option>
            <a-select-option value="CLOSED">已关闭</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="处理反馈">
          <a-textarea
            v-model:value="handleForm.reply"
            :rows="4"
            :maxlength="500"
            show-count
            placeholder="例如：已安排仓库与护理主管核实现存库存，并在本周补齐申领流程。"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getSuggestionPage, handleSuggestion, updateSuggestionStatus } from '../../api/oa'
import type { Id, OaSuggestion } from '../../types'

const router = useRouter()
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: undefined as string | undefined,
  status: undefined as string | undefined,
  employeeSuggestionOnly: true
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
  { title: '提交人', dataIndex: 'proposerName', key: 'proposerName', width: 130 },
  { title: '联系方式', dataIndex: 'contact', key: 'contact', width: 140 },
  { title: '建议内容', dataIndex: 'content', key: 'content', ellipsis: true },
  { title: '提交时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 260, fixed: 'right' as const }
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
    error.value = err?.message || '员工建议记录加载失败'
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

function openPath(path?: string) {
  if (!path) return
  router.push(path)
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
  const parsed = parseSuggestion(record.content)
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

function parseSuggestion(content?: string) {
  const text = String(content || '')
  const { bodyText, reply } = splitReply(text)
  const matched = text.match(/^【员工建议｜(.+?)】/)
  return {
    title: matched ? `员工建议 · ${matched[1]}` : '员工建议',
    body: bodyText.replace(/^【员工建议｜.+?】/, '').trim() || '-',
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
  if (status === 'CLOSED') return '已评估当前建议，暂不纳入本轮执行，先关闭归档。'
  if (status === 'PROCESSING') return '已受理，正在协调相关负责人评估和跟进。'
  return '建议已采纳或完成处理，感谢你的反馈。'
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

function openSuggestedRoute(record: OaSuggestion) {
  const text = String(record.content || '')
  if (text.includes('物资') || text.includes('耗材') || text.includes('库存')) {
    router.push('/oa/approval?type=MATERIAL_APPLY')
    return
  }
  if (text.includes('排班') || text.includes('请假') || text.includes('调班') || text.includes('考勤')) {
    router.push('/oa/attendance-leave')
    return
  }
  if (text.includes('维修') || text.includes('设备')) {
    router.push('/life/maintenance')
    return
  }
  router.push('/oa/staff-collaboration')
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
