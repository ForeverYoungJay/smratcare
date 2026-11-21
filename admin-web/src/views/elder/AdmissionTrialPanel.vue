<template>
  <div>
    <div class="trial-toolbar">
      <span class="rm-note">试住周期与意向跟进；确定入住后点「转正式入住」进入六步办理。</span>
      <a-space wrap>
        <a-button @click="exportRows">导出</a-button>
        <a-button type="primary" @click="openCreate">新增试住</a-button>
      </a-space>
    </div>
    <a-card class="card-elevated" :bordered="false">
      <QueryBar :model="query" layout="inline" class="search-bar" @finish="fetchData">
        <a-form-item label="老人姓名">
          <a-select
            v-model:value="query.elderId"
            allow-clear
            show-search
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            style="width: 220px"
            placeholder="请输入老人姓名/拼音首字母"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
          />
        </a-form-item>
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="请输入老人姓名/护理等级/来源渠道" style="width: 280px" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 180px" placeholder="请选择状态" :options="TRIAL_STATUS_OPTIONS" />
        </a-form-item>
        <a-form-item label="试住开始日期">
          <a-range-picker v-model:value="query.trialStartDateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
        <template #actions>
          <ActionToolbar>
            <a-space>
              <a-button type="primary" @click="search">搜索</a-button>
              <a-button @click="reset">清空</a-button>
            </a-space>
          </ActionToolbar>
        </template>
      </QueryBar>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-space style="margin-bottom: 12px" wrap>
        <a-tag color="blue">已选 {{ selectedRowKeys.length }} 条</a-tag>
        <a-button :disabled="selectedRowKeys.length !== 1" @click="openEditSelected">编辑</a-button>
        <a-button danger :disabled="selectedRowKeys.length === 0" @click="deleteSelected">删除</a-button>
      </a-space>
      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :row-selection="rowSelection"
        :scroll="{ x: 'max-content' }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'intentLevel'">
            {{ intentLevelText(record.intentLevel) }}
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="trialStatusColor(record.status)">{{ trialStatusText(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button v-if="canConvertTrial(record.status)" type="link" size="small" @click="convert(record)">转正式入住</a-button>
            <span v-else>--</span>
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

    <a-modal v-model:open="editOpen" :mask-closable="false" :title="editId ? '编辑试住登记' : '新增试住登记'" :confirm-loading="submitting" width="560px" @ok="submit">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="老人" name="elderId">
          <a-select
            v-model:value="form.elderId"
            show-search
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            placeholder="请输入老人姓名/拼音首字母"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
          />
        </a-form-item>
        <a-form-item label="开始日期" name="trialStartDate">
          <a-date-picker v-model:value="form.trialStartDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="结束日期" name="trialEndDate">
          <a-date-picker v-model:value="form.trialEndDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="来源渠道">
          <a-select v-model:value="form.channel" allow-clear placeholder="请选择来源渠道" :options="channelOptions" />
        </a-form-item>
        <a-form-item label="试住套餐">
          <a-select v-model:value="form.trialPackage" allow-clear placeholder="请选择试住套餐" :options="trialPackageOptions" />
        </a-form-item>
        <a-form-item label="意向等级">
          <a-select v-model:value="form.intentLevel" allow-clear>
            <a-select-option value="HIGH">高</a-select-option>
            <a-select-option value="MEDIUM">中</a-select-option>
            <a-select-option value="LOW">低</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" allow-clear :options="TRIAL_STATUS_OPTIONS" />
        </a-form-item>
        <a-form-item label="护理等级">
          <a-input v-model:value="form.careLevel" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { message, Modal } from 'ant-design-vue'
import ActionToolbar from '../../components/ActionToolbar.vue'
import QueryBar from '../../components/QueryBar.vue'
import { getBaseConfigItemList } from '../../api/baseConfig'
import { useElderOptions } from '../../composables/useElderOptions'
import { createTrialStay, deleteTrialStay, exportTrialStay, getTrialStayPage, updateTrialStay } from '../../api/elderResidence'
import type { BaseConfigItem, Id, PageResult, TrialStayItem, TrialStayRequest, TrialStayStatus } from '../../types'
import { TRIAL_STATUS_OPTIONS, canConvertTrial, intentLevelText, toConvertedTrialRequest, trialStatusColor, trialStatusText } from './admissionTrial'

// 试住登记（原 TrialStay.vue 并入入住办理中心）：elderId 预选，autoCreate 打开新增
const props = defineProps<{ elderId?: string; autoCreate?: boolean }>()
const emit = defineEmits<{ convert: [elderId: Id, elderName?: string] }>()

const loading = ref(false)
const rows = ref<TrialStayItem[]>([])
const total = ref(0)
const editOpen = ref(false)
const editId = ref<Id | undefined>(undefined)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const selectedRowKeys = ref<Id[]>([])
const { elderOptions, elderLoading, searchElders, ensureSelectedElder } = useElderOptions({ pageSize: 80 })
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((key) => String(key)) as Id[]
  }
}))
const channelOptions = ref<{ label: string; value: string }[]>([])
const trialPackageOptions = ref<{ label: string; value: string }[]>([])

const query = reactive({
  elderId: undefined as Id | undefined,
  keyword: undefined as string | undefined,
  status: undefined as TrialStayStatus | undefined,
  trialStartDateRange: undefined as [string, string] | undefined,
  pageNo: 1,
  pageSize: 10
})

const form = reactive<TrialStayRequest>({
  elderId: '' as Id,
  trialStartDate: '',
  trialEndDate: '',
  channel: '',
  trialPackage: '',
  intentLevel: 'MEDIUM',
  status: 'REGISTERED',
  careLevel: '',
  remark: ''
})

const rules: Record<string, Rule[]> = {
  elderId: [{ required: true, message: '请选择老人' }],
  trialStartDate: [{ required: true, message: '请选择开始日期' }],
  trialEndDate: [{ required: true, message: '请选择结束日期' }]
}

const columns = [
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '试住开始日期', dataIndex: 'trialStartDate', key: 'trialStartDate', width: 120 },
  { title: '试住结束日期', dataIndex: 'trialEndDate', key: 'trialEndDate', width: 120 },
  { title: '来源渠道', dataIndex: 'channel', key: 'channel', width: 140 },
  { title: '试住套餐', dataIndex: 'trialPackage', key: 'trialPackage', width: 140 },
  { title: '意向等级', dataIndex: 'intentLevel', key: 'intentLevel', width: 100 },
  { title: '状态', key: 'status', width: 120 },
  { title: '护理等级', dataIndex: 'careLevel', key: 'careLevel', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
]

function filterParams() {
  const [trialStartDateFrom, trialStartDateTo] = query.trialStartDateRange || []
  return { elderId: query.elderId, keyword: query.keyword, status: query.status, trialStartDateFrom, trialStartDateTo }
}

async function exportRows() {
  await exportTrialStay(filterParams())
}

async function loadDictOptions() {
  const [channelItems, packageItems] = await Promise.all([
    getBaseConfigItemList({ configGroup: 'MARKETING_SOURCE_CHANNEL', status: 1 }),
    getBaseConfigItemList({ configGroup: 'TRIAL_STAY_PACKAGE', status: 1 })
  ])
  channelOptions.value = (channelItems || []).map((item: BaseConfigItem) => ({ label: item.itemName, value: item.itemName }))
  trialPackageOptions.value = (packageItems || []).map((item: BaseConfigItem) => ({ label: item.itemName, value: item.itemName }))
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<TrialStayItem> = await getTrialStayPage({ pageNo: query.pageNo, pageSize: query.pageSize, ...filterParams() })
    rows.value = res.list
    total.value = res.total
    selectedRowKeys.value = selectedRowKeys.value.filter((id) => rows.value.some((item) => item.id === id))
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNo = 1
  fetchData()
}

function reset() {
  query.elderId = undefined
  query.keyword = undefined
  query.status = undefined
  query.trialStartDateRange = undefined
  query.pageNo = 1
  fetchData()
}

function openCreate() {
  editOpen.value = true
  editId.value = undefined
  form.elderId = (query.elderId || '') as Id
  form.trialStartDate = ''
  form.trialEndDate = ''
  form.channel = ''
  form.trialPackage = ''
  form.intentLevel = 'MEDIUM'
  form.status = 'REGISTERED'
  form.careLevel = ''
  form.remark = ''
}

function openEdit(record: TrialStayItem) {
  ensureSelectedElder(record.elderId, record.elderName)
  editOpen.value = true
  editId.value = record.id
  form.elderId = record.elderId
  form.trialStartDate = record.trialStartDate
  form.trialEndDate = record.trialEndDate
  form.channel = record.channel || ''
  form.trialPackage = record.trialPackage || ''
  form.intentLevel = record.intentLevel || 'MEDIUM'
  form.status = record.status || 'REGISTERED'
  form.careLevel = record.careLevel || ''
  form.remark = record.remark || ''
}

function openEditSelected() {
  if (selectedRowKeys.value.length !== 1) return
  const record = rows.value.find((item) => item.id === selectedRowKeys.value[0])
  if (record) openEdit(record)
}

function deleteSelected() {
  if (!selectedRowKeys.value.length) return
  Modal.confirm({
    title: '确认删除试住登记？',
    content: `将删除 ${selectedRowKeys.value.length} 条试住记录`,
    okText: '确认删除',
    okType: 'danger',
    async onOk() {
      await Promise.all(selectedRowKeys.value.map((id) => deleteTrialStay(id)))
      message.success('删除成功')
      selectedRowKeys.value = []
      await fetchData()
    }
  })
}

function convert(record: TrialStayItem) {
  Modal.confirm({
    title: '转正式入住？',
    content: `将把 ${record.elderName || '该长者'} 的试住记录标记为「已转签约」，并进入入住办理六步流程。`,
    okText: '转正式入住',
    async onOk() {
      await updateTrialStay(record.id, toConvertedTrialRequest(record))
      message.success('已转正式入住，请继续办理入住')
      emit('convert', record.elderId, record.elderName)
    }
  })
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
  if (submitting.value) return
  submitting.value = true
  try {
    if (editId.value) {
      await updateTrialStay(editId.value, form)
      message.success('试住登记已更新')
    } else {
      await createTrialStay(form)
      message.success('试住登记成功')
    }
    editOpen.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
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

onMounted(async () => {
  await loadDictOptions().catch(() => {})
  await searchElders('')
  if (props.elderId) {
    ensureSelectedElder(props.elderId)
    query.elderId = props.elderId as Id
  }
  if (props.autoCreate) openCreate()
  await fetchData()
})
</script>

<style scoped>
.trial-toolbar { display: flex; flex-wrap: wrap; justify-content: space-between; align-items: center; gap: 12px; margin-bottom: 12px; }
.search-bar { margin-bottom: 12px; }
</style>
