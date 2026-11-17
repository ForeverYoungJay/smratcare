<template>
  <div>
    <div class="rb-bar">
      <a-radio-group v-model:value="status" button-style="solid" @change="search">
        <a-radio-button value="">全部</a-radio-button>
        <a-radio-button v-for="(meta, key) in STATUS" :key="key" :value="key">{{ meta.label }}</a-radio-button>
      </a-radio-group>
      <a-checkbox v-model:checked="overdue" @change="search">只看逾期</a-checkbox>
      <a-input-search v-model:value="keyword" allow-clear placeholder="问题或责任人" style="width: 200px" @search="search" />
      <span class="rb-spacer" />
      <a-button v-if="manager" type="primary" @click="openForm()">新增整改项</a-button>
    </div>
    <a-table row-key="id" size="middle" :loading="loading" :data-source="rows" :columns="columns"
      :pagination="{ current: page.pageNo, pageSize: page.pageSize, total: page.total, onChange: (no: number) => { page.pageNo = no; load() } }">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <span class="rm-pill" :class="`rm-pill--${STATUS[record.status]?.tone}`">{{ STATUS[record.status]?.label || record.status }}</span>
          <span v-if="isOverdue(record)" class="rm-pill rm-pill--danger">逾期</span>
        </template>
        <template v-else-if="column.key === 'evidence'"><span class="rb-clip">{{ record.evidence || '--' }}</span></template>
        <template v-else-if="column.key === 'review'"><span class="rb-clip">{{ record.reviewResult ? `${record.reviewerName || ''}：${record.reviewResult}` : '--' }}</span></template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a v-if="record.status !== 'DONE' && (manager || isOwner(record))" class="rm-link" @click="openForm(record)">{{ manager ? '编辑' : '提交证据' }}</a>
            <a v-if="manager && record.status === 'PENDING_REVIEW'" class="rm-link" @click="openReview(record)">复核</a>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="formOpen" :title="form.id ? '整改项' : '新增整改项'" :confirm-loading="saving" @ok="submitForm">
      <a-form layout="vertical">
        <a-form-item label="问题" required><a-input v-model:value="form.title" :disabled="!manager" :maxlength="120" /></a-form-item>
        <a-form-item label="来源"><a-input v-model:value="form.source" :disabled="!manager" placeholder="例如：质控检查、异常事件复盘" /></a-form-item>
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="责任人" required>
            <a-select v-model:value="form.ownerId" :disabled="!manager" show-search :filter-option="false" :options="staffOptions" @search="searchStaff" />
          </a-form-item></a-col>
          <a-col :span="12"><a-form-item label="截止日期" required><a-date-picker v-model:value="form.dueDate" :disabled="!manager" value-format="YYYY-MM-DD" style="width: 100%" /></a-form-item></a-col>
        </a-row>
        <a-form-item v-if="form.id" label="整改证据"><a-textarea v-model:value="form.evidence" :rows="3" :maxlength="500" placeholder="做了什么、记录在哪里" /></a-form-item>
        <a-form-item v-if="form.id" label="状态">
          <a-select v-model:value="form.status" :options="editableStatus" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="reviewOpen" title="整改复核" :confirm-loading="saving" ok-text="提交" @ok="submitReview">
      <a-radio-group v-model:value="reviewPassed">
        <a-radio :value="true">通过，结案</a-radio>
        <a-radio :value="false">不通过，退回继续整改</a-radio>
      </a-radio-group>
      <a-textarea v-model:value="reviewResult" class="rb-gap" :rows="3" :maxlength="300" placeholder="复核结果" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import { createRectification, getRectificationPage, reviewRectification, updateRectification } from '../../../api/nursing'
import { useStaffOptions } from '../../../composables/useStaffOptions'
import { useUserStore } from '../../../stores/user'
import type { NursingRectificationItem } from '../../../types'
import { resolveCareError } from '../careError'
import '../../elder/residentUi.css'

const STATUS: Record<string, { label: string; tone: string }> = {
  OPEN: { label: '待整改', tone: 'warning' }, IN_PROGRESS: { label: '整改中', tone: 'info' },
  PENDING_REVIEW: { label: '待复核', tone: 'warning' }, DONE: { label: '已整改', tone: 'success' }
}
const editableStatus = ['OPEN', 'IN_PROGRESS', 'PENDING_REVIEW'].map((value) => ({ value, label: STATUS[value].label }))

const route = useRoute()
const userStore = useUserStore()
const manager = computed(() => (userStore.roles || []).some((r) => ['NURSING_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'].includes(String(r || '').toUpperCase())))
const { staffOptions, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 120 })
const loading = ref(false)
const saving = ref(false)
const rows = ref<NursingRectificationItem[]>([])
const page = reactive({ pageNo: 1, pageSize: 10, total: 0 })
const status = ref('')
const overdue = ref(route.query.overdue === 'true')
const keyword = ref('')
const columns = [
  { title: '问题', dataIndex: 'title', width: 220 },
  { title: '责任人', dataIndex: 'ownerName', width: 100 },
  { title: '截止', dataIndex: 'dueDate', width: 110 },
  { title: '状态', key: 'status', width: 150 },
  { title: '整改证据', key: 'evidence' },
  { title: '复核结果', key: 'review' },
  { title: '操作', key: 'action', width: 120 }
]

const formOpen = ref(false)
const blank = () => ({ id: undefined as number | undefined, title: '', source: '', ownerId: undefined as string | undefined, dueDate: '', evidence: '', status: 'OPEN' })
const form = reactive(blank())
const reviewOpen = ref(false)
const reviewTarget = ref<NursingRectificationItem | null>(null)
const reviewPassed = ref(true)
const reviewResult = ref('')

function isOverdue(row: NursingRectificationItem) { return row.status !== 'DONE' && row.dueDate < dayjs().format('YYYY-MM-DD') }
function isOwner(row: NursingRectificationItem) { return String(row.ownerId ?? '') === String(userStore.staffInfo?.id ?? '-') }

async function load() {
  loading.value = true
  try {
    const res = await getRectificationPage({ ...page, status: status.value || undefined, keyword: keyword.value || undefined, overdue: overdue.value || undefined })
    rows.value = res.list || []
    page.total = res.total
  } catch (error) {
    message.error(resolveCareError(error, '整改台账加载失败'))
  } finally {
    loading.value = false
  }
}

function search() { page.pageNo = 1; load() }

function openForm(row?: NursingRectificationItem) {
  Object.assign(form, blank(), row ? { id: row.id, title: row.title, source: row.source || '', ownerId: row.ownerId == null ? undefined : String(row.ownerId), dueDate: row.dueDate, evidence: row.evidence || '', status: row.status } : {})
  if (row?.ownerId) ensureSelectedStaff(row.ownerId, row.ownerName)
  formOpen.value = true
}

async function submitForm() {
  if (!form.title.trim() || !form.ownerId || !form.dueDate) return message.warning('请填写问题、责任人和截止日期')
  saving.value = true
  try {
    const ownerName = staffOptions.value.find((o) => o.value === form.ownerId)?.name
    const { id, ...data } = form
    const payload = { ...data, ownerId: Number(form.ownerId), ownerName }
    if (id) await updateRectification(id, payload)
    else await createRectification(payload)
    formOpen.value = false
    message.success('已保存')
    await load()
  } catch (error) {
    message.error(resolveCareError(error, '保存失败'))
  } finally {
    saving.value = false
  }
}

function openReview(row: NursingRectificationItem) {
  reviewTarget.value = row
  reviewPassed.value = true
  reviewResult.value = ''
  reviewOpen.value = true
}

async function submitReview() {
  if (!reviewResult.value.trim()) return message.warning('请填写复核结果')
  saving.value = true
  try {
    await reviewRectification(reviewTarget.value!.id, reviewPassed.value, reviewResult.value.trim())
    reviewOpen.value = false
    message.success(reviewPassed.value ? '已结案' : '已退回继续整改')
    await load()
  } catch (error) {
    message.error(resolveCareError(error, '复核失败'))
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.rb-bar { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; margin-bottom: 12px; }
.rb-spacer { flex: 1; }
.rb-clip { display: -webkit-box; overflow: hidden; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.rb-gap { margin-top: 12px; }
</style>
