<template>
  <PageContainer title="来访登记" subTitle="访客预约与到访登记">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
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
        <a-button :disabled="selectedRowKeys.length !== 1" @click="printSelectedTicket">打印词条</a-button>
        <a-button :disabled="selectedRowKeys.length !== 1" @click="openEditSelected">编辑</a-button>
        <a-button danger :disabled="selectedRowKeys.length === 0" @click="deleteSelected">删除</a-button>
      </a-space>
      <StatefulBlock :loading="loading" :error="errorMessage" :empty="!rows.length" empty-text="暂无来访记录" @retry="fetchData">
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
      </StatefulBlock>
    </a-card>

    <a-modal v-model:open="editOpen" :title="editingId ? '编辑来访预约' : '新增来访预约'" @ok="submitEdit" :confirm-loading="submitting" width="520px">
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
            @change="onFormElderChange"
          />
        </a-form-item>
        <a-form-item label="已绑定家属">
          <a-select
            v-model:value="form.familyUserId"
            allow-clear
            show-search
            :filter-option="filterFamilyOption"
            :options="familyOptions"
            :loading="familyLoading"
            placeholder="可选，选择后自动带出来访人信息"
            @change="onFamilyRelationChange"
          />
        </a-form-item>
        <a-form-item label="来访人姓名" name="visitorName">
          <a-input v-model:value="form.visitorName" placeholder="请输入来访人姓名" />
        </a-form-item>
        <a-form-item label="联系电话">
          <a-input v-model:value="form.visitorPhone" placeholder="请输入联系电话" />
        </a-form-item>
        <a-form-item label="与长者关系">
          <a-input v-model:value="form.visitorRelation" placeholder="如：女儿、儿子、朋友" />
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
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getFamilyRelations } from '../../api/family'
import { useElderOptions } from '../../composables/useElderOptions'
import { guardBookVisit, guardCheckin, guardDeleteVisit, guardGetPrintTicket, guardTodayVisits, guardUpdateVisit } from '../../api/visit'
import type { FamilyRelationItem, Id, VisitBookRequest, VisitBookingItem } from '../../types'
import { normalizeResidentId } from '../../utils/id'

const loading = ref(false)
const rows = ref<VisitBookingItem[]>([])
const selectedRowKeys = ref<Id[]>([])
const route = useRoute()
const editOpen = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const editingId = ref<Id | undefined>(undefined)
const formRef = ref<FormInstance>()
const familyLoading = ref(false)
const familyRelations = ref<FamilyRelationItem[]>([])
const { elderOptions, elderLoading, searchElders, findElderName, ensureSelectedElder } = useElderOptions({ pageSize: 80 })
const familyOptions = computed(() =>
  familyRelations.value.map((item) => ({
    label: `${item.realName || '未命名家属'}${item.relation ? ` · ${item.relation}` : ''}${item.phone ? ` · ${item.phone}` : ''}`,
    value: item.familyUserId
  }))
)
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((key) => String(key)) as Id[]
  }
}))
const query = reactive({
  elderId: undefined as Id | undefined,
  status: undefined as number | undefined
})
const form = reactive<VisitBookRequest>({
  elderId: '' as Id,
  familyUserId: undefined,
  visitorName: '',
  visitorPhone: '',
  visitorRelation: '',
  visitTime: '',
  visitTimeSlot: '',
  visitorCount: 1,
  carPlate: '',
  remark: ''
})
const rules: FormRules = {
  elderId: [{ required: true, message: '请选择老人' }],
  visitorName: [{ required: true, message: '请输入来访人姓名' }],
  visitTime: [{ required: true, message: '请选择来访时间' }]
}

const columns = [
  { title: '来访人', dataIndex: 'visitorName', key: 'visitorName', width: 120 },
  { title: '关系', dataIndex: 'visitorRelation', key: 'visitorRelation', width: 120 },
  { title: '联系电话', dataIndex: 'visitorPhone', key: 'visitorPhone', width: 140 },
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '居住楼层', dataIndex: 'floorNo', key: 'floorNo', width: 120 },
  { title: '房号', dataIndex: 'roomNo', key: 'roomNo', width: 120 },
  { title: '来访时间', dataIndex: 'visitTime', key: 'visitTime', width: 180 },
  { title: '访客人数', dataIndex: 'visitorCount', key: 'visitorCount', width: 100 },
  { title: '车牌', dataIndex: 'carPlate', key: 'carPlate', width: 120 },
  { title: '状态', key: 'status', width: 100 }
]

function resolveElderName(elderId?: Id) {
  return findElderName(elderId) || '未命名长者'
}

function filterFamilyOption(input: string, option: { label?: string }) {
  const keyword = String(input || '').trim().toLowerCase()
  if (!keyword) return true
  return String(option?.label || '').toLowerCase().includes(keyword)
}

async function loadFamilyRelations(elderId?: Id) {
  const targetId = String(elderId || '').trim()
  familyRelations.value = []
  if (!targetId) return
  familyLoading.value = true
  try {
    familyRelations.value = await getFamilyRelations(targetId)
  } catch {
    familyRelations.value = []
  } finally {
    familyLoading.value = false
  }
}

function fillVisitorFromRelation(relation?: FamilyRelationItem) {
  if (!relation) return
  form.familyUserId = relation.familyUserId
  form.visitorName = relation.realName || ''
  form.visitorPhone = relation.phone || ''
  form.visitorRelation = relation.relation || ''
}

async function onFormElderChange(elderId?: Id) {
  form.familyUserId = undefined
  form.visitorName = ''
  form.visitorPhone = ''
  form.visitorRelation = ''
  await loadFamilyRelations(elderId)
  const primaryRelation = familyRelations.value.find((item) => Number(item.isPrimary || 0) === 1) || familyRelations.value[0]
  if (primaryRelation) {
    fillVisitorFromRelation(primaryRelation)
  }
}

function onFamilyRelationChange(familyUserId?: Id) {
  const targetId = String(familyUserId || '').trim()
  if (!targetId) return
  const matched = familyRelations.value.find((item) => String(item.familyUserId || '') === targetId)
  fillVisitorFromRelation(matched)
}

async function fetchData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const source = await guardTodayVisits()
    rows.value = source
      .filter((item) => (query.elderId ? item.elderId === query.elderId : true))
      .filter((item) => (query.status !== undefined ? item.status === query.status : true))
      .map((item) => ({
        ...item,
        elderName: item.elderName || resolveElderName(item.elderId),
        visitorName: item.visitorName || item.familyName || '未填写',
        visitorPhone: item.visitorPhone || '',
        visitorRelation: item.visitorRelation || ''
      }))
    selectedRowKeys.value = selectedRowKeys.value.filter((id) => rows.value.some((item) => item.id === id))
  } catch (error: any) {
    errorMessage.value = error?.message || '加载来访记录失败'
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

function buildPrintableTicket(record: {
  ticketNo?: string
  elderName?: string
  familyName?: string
  visitorName?: string
  visitorPhone?: string
  visitorRelation?: string
  visitTime?: string
  visitorCount?: number
  floorNo?: string
  roomNo?: string
  carPlate?: string
  statusText?: string
  generatedAt?: string
}) {
  const lines = [
    `词条编号：${record.ticketNo || '-'}`,
    `老人：${record.elderName || '-'}`,
    `来访人：${record.visitorName || record.familyName || '-'}`,
    `关系：${record.visitorRelation || '-'}`,
    `联系电话：${record.visitorPhone || '-'}`,
    `来访时间：${record.visitTime || '-'}`,
    `访客人数：${record.visitorCount || 1}`,
    `楼层/房间：${record.floorNo || '-'} / ${record.roomNo || '-'}`,
    `车牌：${record.carPlate || '-'}`,
    `状态：${record.statusText || '-'}`,
    `生成时间：${record.generatedAt || '-'}`
  ]
  return lines.map((line) => `<div>${line}</div>`).join('')
}

function printVisitTicket(record: {
  ticketNo?: string
  elderName?: string
  familyName?: string
  visitorName?: string
  visitorPhone?: string
  visitorRelation?: string
  visitTime?: string
  visitorCount?: number
  floorNo?: string
  roomNo?: string
  carPlate?: string
  statusText?: string
  generatedAt?: string
}) {
  const popup = window.open('', '_blank', 'width=420,height=520')
  if (!popup) {
    message.warning('浏览器拦截了打印窗口，请允许弹窗后重试')
    return
  }
  popup.document.write(`
    <html>
      <head>
        <title>来访登记词条</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif; padding: 20px; }
          .ticket { border: 1px dashed #333; padding: 16px; line-height: 1.8; font-size: 14px; }
          .title { font-weight: 700; margin-bottom: 8px; font-size: 16px; }
        </style>
      </head>
      <body>
        <div class="ticket">
          <div class="title">来访登记词条</div>
          ${buildPrintableTicket(record)}
        </div>
      </body>
    </html>
  `)
  popup.document.close()
  popup.focus()
  popup.print()
}

async function printSelectedTicket() {
  if (selectedRowKeys.value.length !== 1) return
  const record = rows.value.find((item) => item.id === selectedRowKeys.value[0])
  if (!record) return
  try {
    const ticket = await guardGetPrintTicket(record.id)
    printVisitTicket({
      ticketNo: ticket.ticketNo,
      elderName: ticket.elderName,
      familyName: ticket.familyName,
      visitorName: ticket.visitorName,
      visitorPhone: ticket.visitorPhone,
      visitorRelation: ticket.visitorRelation,
      visitTime: ticket.visitTime,
      visitorCount: ticket.visitorCount,
      floorNo: ticket.floorNo,
      roomNo: ticket.roomNo,
      carPlate: ticket.carPlate,
      statusText: ticket.statusText,
      generatedAt: ticket.generatedAt
    })
  } catch (error: any) {
    message.error(error?.message || '生成打印词条失败')
  }
}

function openCreate() {
  editingId.value = undefined
  form.elderId = '' as Id
  form.familyUserId = undefined
  form.visitorName = ''
  form.visitorPhone = ''
  form.visitorRelation = ''
  form.visitTime = ''
  form.visitTimeSlot = ''
  form.visitorCount = 1
  form.carPlate = ''
  form.remark = ''
  familyRelations.value = []
  editOpen.value = true
}

async function openEditSelected() {
  if (selectedRowKeys.value.length !== 1) return
  const record = rows.value.find((item) => item.id === selectedRowKeys.value[0])
  if (!record) return
  if (record.status === 1) {
    message.warning('已登记到访记录不可编辑')
    return
  }
  ensureSelectedElder(record.elderId, record.elderName)
  await loadFamilyRelations(record.elderId)
  editingId.value = record.id
  form.elderId = record.elderId
  form.familyUserId = record.familyUserId
  form.visitorName = record.visitorName || record.familyName || ''
  form.visitorPhone = record.visitorPhone || ''
  form.visitorRelation = record.visitorRelation || ''
  form.visitTime = record.visitTime
  form.visitTimeSlot = ''
  form.visitorCount = record.visitorCount || 1
  form.carPlate = record.carPlate || ''
  form.remark = record.remark || ''
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
      familyUserId: form.familyUserId || undefined,
      visitorName: form.visitorName || undefined,
      visitorPhone: form.visitorPhone || undefined,
      visitorRelation: form.visitorRelation || undefined,
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
  await searchElders('')
  const elderId = normalizeResidentId(route.query as Record<string, unknown>)
  if (elderId) {
    ensureSelectedElder(elderId)
    query.elderId = elderId
    await loadFamilyRelations(elderId)
  }
  await fetchData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
