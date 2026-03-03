<template>
  <PageContainer title="来访登记" subTitle="访客预约核销与到访记录">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="老人姓名">
          <a-select v-model:value="query.elderId" allow-clear style="width: 180px" placeholder="请选择老人姓名">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="来访状态">
          <a-select v-model:value="query.status" allow-clear style="width: 160px" placeholder="请选择来访状态">
            <a-select-option :value="0">待登记</a-select-option>
            <a-select-option :value="1">已登记</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" :loading="loading" @click="fetchData">搜索</a-button>
            <a-button @click="reset">清空</a-button>
            <a-button type="primary" @click="openCreate">新增来访</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-space style="margin-bottom: 12px" wrap>
        <a-tag color="blue">已选 {{ selectedRowKeys.length }} 条</a-tag>
        <a-button :disabled="selectedRowKeys.length !== 1" @click="checkinSelected">登记到访</a-button>
        <a-button :disabled="selectedRowKeys.length !== 1" @click="openEditSelected">编辑</a-button>
        <a-button danger :disabled="selectedRowKeys.length === 0" @click="deleteSelected">删除</a-button>
      </a-space>
      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        row-key="id"
        :pagination="false"
        :row-selection="rowSelection"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'orange'">
              {{ record.status === 1 ? '已登记' : '待登记' }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="editOpen" :title="editingId ? '编辑来访预约' : '新增来访预约'" @ok="submitEdit" :confirm-loading="submitting" width="520px">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="老人" name="elderId">
          <a-select v-model:value="form.elderId" placeholder="请选择老人">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="来访时间" name="visitTime">
          <a-date-picker v-model:value="form.visitTime" show-time value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </a-form-item>
        <a-form-item label="访客人数">
          <a-input-number v-model:value="form.visitorCount" :min="1" :max="20" style="width: 100%" />
        </a-form-item>
        <a-form-item label="车牌号">
          <a-input v-model:value="form.carPlate" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getElderPage } from '../../api/elder'
import { guardBookVisit, guardCheckin, guardDeleteVisit, guardTodayVisits, guardUpdateVisit } from '../../api/visit'
import type { ElderItem, PageResult, VisitBookRequest, VisitBookingItem } from '../../types'

const loading = ref(false)
const rows = ref<VisitBookingItem[]>([])
const elders = ref<ElderItem[]>([])
const selectedRowKeys = ref<number[]>([])
const route = useRoute()
const editOpen = ref(false)
const submitting = ref(false)
const editingId = ref<number | undefined>(undefined)
const formRef = ref<FormInstance>()
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys as number[]
  }
}))
const query = reactive({
  elderId: undefined as number | undefined,
  status: undefined as number | undefined
})
const form = reactive<VisitBookRequest>({
  elderId: 0,
  visitTime: '',
  visitTimeSlot: '',
  visitorCount: 1,
  carPlate: '',
  remark: ''
})
const rules: FormRules = {
  elderId: [{ required: true, message: '请选择老人' }],
  visitTime: [{ required: true, message: '请选择来访时间' }]
}

const columns = [
  { title: '家属姓名', dataIndex: 'familyName', key: 'familyName', width: 120 },
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '居住楼层', dataIndex: 'floorNo', key: 'floorNo', width: 120 },
  { title: '房号', dataIndex: 'roomNo', key: 'roomNo', width: 120 },
  { title: '来访时间', dataIndex: 'visitTime', key: 'visitTime', width: 180 },
  { title: '访客人数', dataIndex: 'visitorCount', key: 'visitorCount', width: 100 },
  { title: '车牌', dataIndex: 'carPlate', key: 'carPlate', width: 120 },
  { title: '核销码', dataIndex: 'visitCode', key: 'visitCode', width: 160 },
  { title: '状态', key: 'status', width: 100 }
]

async function loadElders() {
  const res: PageResult<ElderItem> = await getElderPage({ pageNo: 1, pageSize: 200 })
  elders.value = res.list
}

function resolveElderName(elderId?: number) {
  const elder = elders.value.find((item) => item.id === elderId)
  return elder?.fullName || `#${elderId || '-'}`
}

async function fetchData() {
  loading.value = true
  try {
    const source = await guardTodayVisits()
    rows.value = source
      .filter((item) => (query.elderId ? item.elderId === query.elderId : true))
      .filter((item) => (query.status !== undefined ? item.status === query.status : true))
      .map((item) => ({
        ...item,
        elderName: item.elderName || resolveElderName(item.elderId)
      }))
    selectedRowKeys.value = selectedRowKeys.value.filter((id) => rows.value.some((item) => item.id === id))
  } finally {
    loading.value = false
  }
}

function reset() {
  query.elderId = undefined
  query.status = undefined
  fetchData()
}

async function checkin(record: VisitBookingItem) {
  await guardCheckin({ bookingId: record.id })
  message.success('来访登记成功')
  fetchData()
}

async function checkinSelected() {
  if (selectedRowKeys.value.length !== 1) return
  const record = rows.value.find((item) => item.id === selectedRowKeys.value[0])
  if (!record) return
  if (record.status === 1) {
    message.warning('该记录已登记')
    return
  }
  await checkin(record)
}

function openCreate() {
  editingId.value = undefined
  form.elderId = 0
  form.visitTime = ''
  form.visitTimeSlot = ''
  form.visitorCount = 1
  form.carPlate = ''
  form.remark = ''
  editOpen.value = true
}

function openEditSelected() {
  if (selectedRowKeys.value.length !== 1) return
  const record = rows.value.find((item) => item.id === selectedRowKeys.value[0])
  if (!record) return
  if (record.status === 1) {
    message.warning('已登记到访记录不可编辑')
    return
  }
  editingId.value = record.id
  form.elderId = record.elderId
  form.visitTime = record.visitTime
  form.visitTimeSlot = ''
  form.visitorCount = record.visitorCount || 1
  form.carPlate = record.carPlate || ''
  form.remark = ''
  editOpen.value = true
}

function buildTimeSlot(visitTime: string) {
  const start = dayjs(visitTime)
  if (!start.isValid()) return '09:00-10:00'
  const end = start.add(1, 'hour')
  return `${start.format('HH:mm')}-${end.format('HH:mm')}`
}

async function submitEdit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload: VisitBookRequest = {
      elderId: form.elderId,
      familyUserId: undefined,
      visitTime: form.visitTime,
      visitTimeSlot: buildTimeSlot(form.visitTime),
      visitorCount: form.visitorCount || 1,
      carPlate: form.carPlate || undefined,
      remark: form.remark || undefined
    }
    if (editingId.value) {
      await guardUpdateVisit(editingId.value, payload)
      message.success('来访预约已更新')
    } else {
      await guardBookVisit(payload)
      message.success('来访预约已新增')
    }
    editOpen.value = false
    await fetchData()
  } finally {
    submitting.value = false
  }
}

function deleteSelected() {
  if (!selectedRowKeys.value.length) return
  Modal.confirm({
    title: '确认删除来访预约？',
    content: `将删除 ${selectedRowKeys.value.length} 条预约（已登记记录不可删除）`,
    okText: '确认删除',
    okType: 'danger',
    async onOk() {
      await Promise.all(selectedRowKeys.value.map((id) => guardDeleteVisit(id)))
      message.success('删除成功')
      selectedRowKeys.value = []
      await fetchData()
    }
  })
}

onMounted(async () => {
  await loadElders()
  const elderId = Number(route.query.elderId || 0)
  if (elderId > 0) {
    query.elderId = elderId
  }
  await fetchData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
