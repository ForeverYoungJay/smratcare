<template>
  <PageContainer :title="title" :sub-title="subTitle">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" placeholder="姓名/手机号" allow-clear />
        </a-form-item>
        <a-form-item label="渠道来源">
          <a-select v-model:value="query.source" allow-clear style="width: 180px" placeholder="全部">
            <a-select-option v-for="item in sourceOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="客户标签">
          <a-select v-model:value="query.customerTag" allow-clear style="width: 180px" placeholder="全部">
            <a-select-option v-for="item in customerTagOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="mode !== 'callback'" label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 140px">
            <a-select-option :value="0">咨询管理</a-select-option>
            <a-select-option :value="1">意向客户</a-select-option>
            <a-select-option :value="2">预订客户</a-select-option>
            <a-select-option :value="3">失效用户</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="openForm()">新增线索</a-button>
          <a-button v-if="mode === 'callback'" @click="goContract">去合同签约</a-button>
        </a-space>
      </div>
      <a-table
        :data-source="displayRows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusTag(record.status)">{{ statusLabel(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'nextFollowDate'">
            <a-tag v-if="isOverdue(record)" color="red">逾期</a-tag>
            <span>{{ record.nextFollowDate || '-' }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space wrap>
              <a-button type="link" @click="openForm(record)">编辑</a-button>
              <a-button type="link" @click="quickMove(record, 1)">转意向</a-button>
              <a-button type="link" @click="quickMove(record, 2)">转预订</a-button>
              <a-button type="link" danger @click="quickMove(record, 3)">设失效</a-button>
              <a-button type="link" danger @click="remove(record.id)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="displayTotal"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal v-model:open="open" :title="modalTitle" width="460px" :confirm-loading="submitting" @ok="submit">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="姓名" name="name">
          <a-input v-model:value="form.name" />
        </a-form-item>
        <a-form-item label="电话" name="phone">
          <a-input v-model:value="form.phone" />
        </a-form-item>
        <a-form-item label="来源渠道" name="source">
          <a-select v-model:value="form.source" allow-clear placeholder="请选择来源渠道">
            <a-select-option v-for="item in sourceOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="客户标签" name="customerTag">
          <a-select v-model:value="form.customerTag" allow-clear placeholder="请选择客户标签">
            <a-select-option v-for="item in customerTagOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="form.status">
            <a-select-option :value="0">咨询管理</a-select-option>
            <a-select-option :value="1">意向客户</a-select-option>
            <a-select-option :value="2">预订客户</a-select-option>
            <a-select-option :value="3">失效用户</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="待回访日期" name="nextFollowDate">
          <a-date-picker v-model:value="form.nextFollowDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../../components/PageContainer.vue'
import { getBaseConfigItemList } from '../../../api/baseConfig'
import { createCrmLead, deleteCrmLead, getLeadPage, updateCrmLead } from '../../../api/marketing'
import type { BaseConfigItem, CrmLeadItem, PageResult } from '../../../types'

const props = withDefaults(defineProps<{
  mode: 'consultation' | 'intent' | 'reservation' | 'invalid' | 'callback'
  title: string
  subTitle: string
}>(), {
  mode: 'consultation'
})

const router = useRouter()
const loading = ref(false)
const rows = ref<CrmLeadItem[]>([])
const total = ref(0)

const query = reactive({
  keyword: '',
  source: '',
  customerTag: '',
  status: defaultStatus(props.mode),
  pageNo: 1,
  pageSize: 10
})

const open = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<CrmLeadItem>>({ status: defaultStatus(props.mode) })
const submitting = ref(false)

const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名' }],
  phone: [{ required: true, message: '请输入电话' }]
}

const columns = [
  { title: '姓名', dataIndex: 'name', key: 'name', width: 110 },
  { title: '电话', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '来源渠道', dataIndex: 'source', key: 'source', width: 140 },
  { title: '客户标签', dataIndex: 'customerTag', key: 'customerTag', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '待回访', dataIndex: 'nextFollowDate', key: 'nextFollowDate', width: 150 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 320 }
]

const callbackRows = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  return rows.value.filter((item) => {
    if (!item.nextFollowDate) return false
    if (item.status === 2 || item.status === 3) return false
    return item.nextFollowDate <= today
  })
})

const displayRows = computed(() => props.mode === 'callback' ? callbackRows.value : rows.value)
const displayTotal = computed(() => props.mode === 'callback' ? callbackRows.value.length : total.value)

const modalTitle = computed(() => form.id ? '编辑线索' : '新增线索')
const sourceOptions = ref<{ label: string; value: string }[]>([])
const customerTagOptions = ref<{ label: string; value: string }[]>([])

async function loadDictOptions() {
  const [sourceItems, tagItems] = await Promise.all([
    getBaseConfigItemList({ configGroup: 'MARKETING_SOURCE_CHANNEL', status: 1 }),
    getBaseConfigItemList({ configGroup: 'MARKETING_CUSTOMER_TAG', status: 1 })
  ])
  sourceOptions.value = (sourceItems || []).map((item: BaseConfigItem) => ({ label: item.itemName, value: item.itemName }))
  customerTagOptions.value = (tagItems || []).map((item: BaseConfigItem) => ({ label: item.itemName, value: item.itemName }))
}

function defaultStatus(mode: string) {
  if (mode === 'consultation') return 0
  if (mode === 'intent') return 1
  if (mode === 'reservation') return 2
  if (mode === 'invalid') return 3
  return undefined
}

function statusLabel(status?: number) {
  if (status === 0) return '咨询管理'
  if (status === 1) return '意向客户'
  if (status === 2) return '预订客户'
  if (status === 3) return '失效用户'
  return '-'
}

function statusTag(status?: number) {
  if (status === 0) return 'blue'
  if (status === 1) return 'orange'
  if (status === 2) return 'green'
  if (status === 3) return 'red'
  return 'default'
}

function isOverdue(record: CrmLeadItem) {
  return !!record.nextFollowDate && record.nextFollowDate < dayjs().format('YYYY-MM-DD')
}

function buildQueryParams() {
  return {
    pageNo: query.pageNo,
    pageSize: query.pageSize,
    keyword: query.keyword || undefined,
    source: query.source || undefined,
    customerTag: query.customerTag || undefined,
    status: props.mode === 'callback' ? undefined : query.status
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<CrmLeadItem> = await getLeadPage(buildQueryParams())
    rows.value = res.list || []
    total.value = res.total || rows.value.length
  } finally {
    loading.value = false
  }
}

function reset() {
  query.keyword = ''
  query.source = ''
  query.customerTag = ''
  query.status = defaultStatus(props.mode)
  query.pageNo = 1
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

function openForm(row?: CrmLeadItem) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, {
      id: undefined,
      name: '',
      phone: '',
      source: '',
      customerTag: '',
      status: defaultStatus(props.mode) ?? 0,
      nextFollowDate: '',
      remark: ''
    })
  }
  open.value = true
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    if (form.id) {
      await updateCrmLead(form.id, form)
    } else {
      await createCrmLead(form)
    }
    message.success('保存成功')
    open.value = false
    fetchData()
  } catch {
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

function quickMove(row: CrmLeadItem, targetStatus: number) {
  updateCrmLead(row.id, { ...row, status: targetStatus })
    .then(() => {
      message.success('已更新状态')
      fetchData()
    })
    .catch(() => message.error('更新失败'))
}

function remove(id: number) {
  Modal.confirm({
    title: '确认删除该线索吗？',
    onOk: async () => {
      await deleteCrmLead(id)
      message.success('已删除')
      fetchData()
    }
  })
}

function goContract() {
  router.push('/marketing/contract-signing')
}

onMounted(async () => {
  await loadDictOptions()
  await fetchData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}

.table-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
</style>
