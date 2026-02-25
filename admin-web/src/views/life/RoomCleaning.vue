<template>
  <PageContainer title="房间打扫" subTitle="保洁计划与完成追踪">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="房间号/保洁员" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增打扫任务</a-button>
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
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 'DONE' ? 'green' : 'orange'">
            {{ record.status === 'DONE' ? '已完成' : '待处理' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" @click="done(record)">完成</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="form.id ? '编辑打扫任务' : '新增打扫任务'" @ok="submit" :confirm-loading="saving" width="620px">
      <a-form layout="vertical">
        <a-form-item label="房间" required>
          <a-select
            v-model:value="form.roomId"
            show-search
            :options="roomOptions"
            placeholder="请选择房间"
            option-filter-prop="label"
          />
        </a-form-item>
        <a-form-item label="计划日期" required>
          <a-date-picker v-model:value="form.planDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="保洁员">
          <a-input v-model:value="form.cleanerName" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs from 'dayjs'
import type { Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getRoomList } from '../../api/bed'
import { getRoomCleaningPage, createRoomCleaning, updateRoomCleaning, completeRoomCleaning, deleteRoomCleaning } from '../../api/life'
import type { PageResult, RoomCleaningTask } from '../../types'

const loading = ref(false)
const rows = ref<RoomCleaningTask[]>([])
const roomOptions = ref<any[]>([])
const query = reactive({ status: undefined as string | undefined, keyword: '', pageNo: 1, pageSize: 10 })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const columns = [
  { title: '房间号', dataIndex: 'roomNo', key: 'roomNo', width: 140 },
  { title: '保洁员', dataIndex: 'cleanerName', key: 'cleanerName', width: 120 },
  { title: '计划日期', dataIndex: 'planDate', key: 'planDate', width: 120 },
  { title: '完成时间', dataIndex: 'cleanedAt', key: 'cleanedAt', width: 180 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

const statusOptions = [
  { label: '待处理', value: 'PENDING' },
  { label: '已完成', value: 'DONE' }
]

const editOpen = ref(false)
const saving = ref(false)
const form = reactive({
  id: undefined as number | undefined,
  roomId: undefined as number | undefined,
  planDate: dayjs() as Dayjs,
  cleanerName: '',
  status: 'PENDING',
  remark: ''
})

async function loadRooms() {
  const rooms = await getRoomList()
  roomOptions.value = rooms.map((r: any) => ({ label: `${r.roomNo}`, value: r.id }))
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<RoomCleaningTask> = await getRoomCleaningPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      keyword: query.keyword || undefined
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
  query.status = undefined
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  form.id = undefined
  form.roomId = undefined
  form.planDate = dayjs()
  form.cleanerName = ''
  form.status = 'PENDING'
  form.remark = ''
  editOpen.value = true
}

function openEdit(record: RoomCleaningTask) {
  form.id = record.id
  form.roomId = record.roomId
  form.planDate = dayjs(record.planDate)
  form.cleanerName = record.cleanerName || ''
  form.status = record.status || 'PENDING'
  form.remark = record.remark || ''
  editOpen.value = true
}

async function submit() {
  const payload = {
    roomId: form.roomId,
    planDate: dayjs(form.planDate).format('YYYY-MM-DD'),
    cleanerName: form.cleanerName,
    status: form.status,
    remark: form.remark
  }
  saving.value = true
  try {
    if (form.id) {
      await updateRoomCleaning(form.id, payload)
    } else {
      await createRoomCleaning(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function done(record: RoomCleaningTask) {
  await completeRoomCleaning(record.id)
  fetchData()
}

async function remove(record: RoomCleaningTask) {
  await deleteRoomCleaning(record.id)
  fetchData()
}

loadRooms()
fetchData()
</script>
