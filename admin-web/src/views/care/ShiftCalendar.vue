<template>
  <PageContainer title="排班" subTitle="护理人员班次排期管理">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="员工ID">
        <a-input-number v-model:value="query.staffId" :min="1" style="width: 140px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 140px">
          <a-select-option :value="1">排班中</a-select-option>
          <a-select-option :value="2">已确认</a-select-option>
          <a-select-option :value="0">停用</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openEdit()">新增排班</a-button>
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
        <template v-if="column.key === 'action'">
          <a-space>
            <a @click="openEdit(record)">编辑</a>
            <a @click="remove(record)" style="color: #ff4d4f">删除</a>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑排班' : '新增排班'" @ok="submit" :confirm-loading="saving" width="620px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="员工ID" required>
              <a-input-number v-model:value="form.staffId" :min="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="员工姓名">
              <a-input v-model:value="form.staffName" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="值班日期" required>
              <a-date-picker v-model:value="dutyDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="班次编码">
              <a-input v-model:value="form.shiftCode" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始时间">
              <a-input v-model:value="form.startTime" placeholder="08:00:00" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束时间">
              <a-input v-model:value="form.endTime" placeholder="17:00:00" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="状态">
          <a-select v-model:value="form.status">
            <a-select-option :value="1">排班中</a-select-option>
            <a-select-option :value="2">已确认</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { createSchedule, deleteSchedule, getSchedulePage, updateSchedule } from '../../api/schedule'
import type { PageResult, ScheduleItem } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<ScheduleItem[]>([])
const query = reactive({
  staffId: undefined as number | undefined,
  status: undefined as number | undefined,
  range: undefined as [Dayjs, Dayjs] | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '员工ID', dataIndex: 'staffId', key: 'staffId', width: 100 },
  { title: '员工姓名', dataIndex: 'staffName', key: 'staffName', width: 140 },
  { title: '值班日期', dataIndex: 'dutyDate', key: 'dutyDate', width: 120 },
  { title: '班次', dataIndex: 'shiftCode', key: 'shiftCode', width: 100 },
  { title: '开始', dataIndex: 'startTime', key: 'startTime', width: 100 },
  { title: '结束', dataIndex: 'endTime', key: 'endTime', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 140 }
]

const editOpen = ref(false)
const dutyDate = ref<Dayjs | undefined>()
const form = reactive<Partial<ScheduleItem>>({ status: 1 })

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<ScheduleItem> = await getSchedulePage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      staffId: query.staffId,
      status: query.status,
      dateFrom: query.range?.[0] ? dayjs(query.range[0]).format('YYYY-MM-DD') : undefined,
      dateTo: query.range?.[1] ? dayjs(query.range[1]).format('YYYY-MM-DD') : undefined
    })
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
  query.status = undefined
  query.range = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openEdit(record?: ScheduleItem) {
  Object.assign(form, record || { status: 1, staffId: undefined, staffName: '', shiftCode: '', startTime: '', endTime: '' })
  dutyDate.value = form.dutyDate ? dayjs(form.dutyDate) : undefined
  editOpen.value = true
}

async function submit() {
  if (!form.staffId || !dutyDate.value) return
  const payload = {
    ...form,
    dutyDate: dutyDate.value.format('YYYY-MM-DD')
  }
  saving.value = true
  try {
    if (form.id) {
      await updateSchedule(form.id, payload)
    } else {
      await createSchedule(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: ScheduleItem) {
  await deleteSchedule(record.id)
  fetchData()
}

fetchData()
</script>
