<template>
  <PageContainer title="奖惩管理" subTitle="员工奖惩记录与执行台账">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="员工">
        <a-select
          v-model:value="query.staffId"
          allow-clear
          show-search
          :filter-option="false"
          placeholder="选择员工"
          style="width: 200px"
          @search="searchStaff"
        >
          <a-select-option v-for="item in staffOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="类型">
        <a-select v-model:value="query.type" allow-clear style="width: 140px" :options="typeOptions" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 140px" :options="statusOptions" />
      </a-form-item>
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="标题/原因" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openEdit()">新增记录</a-button>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'type'">
          <a-tag :color="record.type === 'REWARD' ? 'green' : 'red'">{{ record.type === 'REWARD' ? '奖励' : '惩罚' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="record.status === 'EFFECTIVE' ? 'blue' : 'default'">
            {{ record.status === 'EFFECTIVE' ? '生效' : '已作废' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a @click="openEdit(record)">编辑</a>
            <a @click="remove(record)" style="color: #ff4d4f">删除</a>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑奖惩' : '新增奖惩'" @ok="submit" :confirm-loading="saving" width="700px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="员工" required>
              <a-select
                v-model:value="form.staffId"
                allow-clear
                show-search
                :filter-option="false"
                placeholder="选择员工"
                @search="searchStaff"
                @change="onStaffChange"
              >
                <a-select-option v-for="item in staffOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="类型" required>
              <a-select v-model:value="form.type" :options="typeOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="级别">
              <a-input v-model:value="form.level" placeholder="如：A级/轻微" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :options="statusOptions" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-form-item label="原因">
          <a-textarea v-model:value="form.reason" :rows="3" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="金额">
              <a-input-number v-model:value="form.amount" :min="0" :step="10" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="积分">
              <a-input-number v-model:value="form.points" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="发生日期" required>
              <a-date-picker v-model:value="occurredDate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { createRewardPunishment, deleteRewardPunishment, getRewardPunishmentPage, updateRewardPunishment } from '../../api/hr'
import { getStaffPage } from '../../api/rbac'
import type { PageResult, StaffItem, StaffRewardPunishment } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<StaffRewardPunishment[]>([])
const query = reactive({
  staffId: undefined as number | undefined,
  type: undefined as string | undefined,
  status: undefined as string | undefined,
  keyword: '',
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const staffOptions = ref<Array<{ label: string; value: number }>>([])

const columns = [
  { title: '员工', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '类型', dataIndex: 'type', key: 'type', width: 90 },
  { title: '级别', dataIndex: 'level', key: 'level', width: 100 },
  { title: '标题', dataIndex: 'title', key: 'title', width: 180 },
  { title: '发生日期', dataIndex: 'occurredDate', key: 'occurredDate', width: 120 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 100 },
  { title: '积分', dataIndex: 'points', key: 'points', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 140 }
]

const typeOptions = [
  { label: '奖励', value: 'REWARD' },
  { label: '惩罚', value: 'PUNISH' }
]
const statusOptions = [
  { label: '生效', value: 'EFFECTIVE' },
  { label: '已作废', value: 'CANCELLED' }
]

const editOpen = ref(false)
const occurredDate = ref<Dayjs | undefined>()
const form = reactive<Partial<StaffRewardPunishment>>({ type: 'REWARD', status: 'EFFECTIVE' })

async function loadStaffOptions(keyword?: string) {
  const res: PageResult<StaffItem> = await getStaffPage({ pageNo: 1, pageSize: 50, keyword })
  staffOptions.value = res.list.map((item) => ({ label: item.realName || item.username, value: item.id }))
}

async function searchStaff(val: string) {
  await loadStaffOptions(val)
}

function onStaffChange(staffId: number) {
  const staff = staffOptions.value.find((item) => item.value === staffId)
  if (staff) {
    form.staffName = staff.label
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<StaffRewardPunishment> = await getRewardPunishmentPage(query)
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.staffId = undefined
  query.type = undefined
  query.status = undefined
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openEdit(record?: StaffRewardPunishment) {
  Object.assign(form, record || { type: 'REWARD', status: 'EFFECTIVE', title: '', reason: '', level: '' })
  if (record?.staffId && record?.staffName && !staffOptions.value.some((item) => item.value === record.staffId)) {
    staffOptions.value.unshift({ label: record.staffName, value: record.staffId })
  }
  occurredDate.value = form.occurredDate ? dayjs(form.occurredDate) : undefined
  editOpen.value = true
}

async function submit() {
  if (!form.staffId) {
    message.error('请选择员工')
    return
  }
  if (!form.title) {
    message.error('请填写标题')
    return
  }
  if (!occurredDate.value) {
    message.error('请选择发生日期')
    return
  }
  saving.value = true
  try {
    const payload = {
      ...form,
      occurredDate: occurredDate.value.format('YYYY-MM-DD')
    }
    if (form.id) {
      await updateRewardPunishment(form.id, payload)
    } else {
      await createRewardPunishment(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: StaffRewardPunishment) {
  if (!record.id) return
  await deleteRewardPunishment(record.id)
  fetchData()
}

loadStaffOptions()
fetchData()
</script>
