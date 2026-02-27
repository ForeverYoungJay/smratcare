<template>
  <PageContainer title="死亡登记" subTitle="老人死亡信息登记与追溯">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="老人">
          <a-select v-model:value="query.elderId" allow-clear style="width: 180px" placeholder="请选择老人姓名">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 140px" placeholder="请选择状态">
            <a-select-option value="REGISTERED">已登记</a-select-option>
            <a-select-option value="CANCELLED">已取消</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="原因/证明编号/上报人" style="width: 240px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">搜索</a-button>
            <a-button @click="reset">清空</a-button>
            <a-button v-if="canManage" @click="exportRows">导出</a-button>
            <a-button v-if="canManage" type="primary" ghost @click="openCreate">新增死亡登记</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-table :data-source="rows" :columns="columns" :loading="loading" :pagination="false" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'CANCELLED' ? 'default' : 'red'">
              {{ record.status === 'CANCELLED' ? '已取消' : '已登记' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space v-if="canManage">
              <a-button type="link" :disabled="record.status === 'CANCELLED'" @click="openEdit(record)">更正</a-button>
              <a-button
                v-if="canCancel"
                type="link"
                danger
                :disabled="record.status === 'CANCELLED'"
                @click="cancelRecord(record)"
              >
                作废
              </a-button>
            </a-space>
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

    <a-modal v-model:open="createOpen" :title="editingId ? '更正死亡登记' : '新增死亡登记'" @ok="submitCreate" :confirm-loading="submitting" width="560px">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="老人" name="elderId">
          <a-select v-model:value="form.elderId" placeholder="请选择老人">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="死亡日期" name="deathDate">
          <a-date-picker v-model:value="form.deathDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="死亡时间">
          <a-date-picker v-model:value="form.deathTime" show-time value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </a-form-item>
        <a-form-item label="地点">
          <a-input v-model:value="form.place" />
        </a-form-item>
        <a-form-item label="死亡原因">
          <a-input v-model:value="form.cause" />
        </a-form-item>
        <a-form-item label="死亡证明编号">
          <a-input v-model:value="form.certificateNo" />
        </a-form-item>
        <a-form-item label="上报人">
          <a-input v-model:value="form.reportedBy" />
        </a-form-item>
        <a-form-item label="上报时间">
          <a-date-picker
            v-model:value="form.reportedTime"
            show-time
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getElderPage } from '../../api/elder'
import { cancelDeathRegister, createDeathRegister, exportDeathRegister, getDeathRegisterPage, updateDeathRegister } from '../../api/elderResidence'
import { getRoles } from '../../utils/auth'
import type { DeathRegisterCreateRequest, DeathRegisterItem, ElderItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<DeathRegisterItem[]>([])
const total = ref(0)
const elders = ref<ElderItem[]>([])
const createOpen = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | undefined>(undefined)
const roles = getRoles()
const canManage = roles.includes('ADMIN') || roles.includes('STAFF')
const canCancel = roles.includes('ADMIN')

const query = reactive({
  elderId: undefined as number | undefined,
  status: undefined as 'REGISTERED' | 'CANCELLED' | undefined,
  keyword: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const form = reactive<DeathRegisterCreateRequest>({
  elderId: 0,
  deathDate: '',
  deathTime: undefined,
  place: '',
  cause: '',
  certificateNo: '',
  reportedBy: '',
  reportedTime: undefined,
  remark: ''
})

const rules: FormRules = {
  elderId: [{ required: true, message: '请选择老人' }],
  deathDate: [{ required: true, message: '请选择死亡日期' }]
}

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '死亡日期', dataIndex: 'deathDate', key: 'deathDate', width: 120 },
  { title: '死亡时间', dataIndex: 'deathTime', key: 'deathTime', width: 180 },
  { title: '地点', dataIndex: 'place', key: 'place', width: 140 },
  { title: '死亡原因', dataIndex: 'cause', key: 'cause', width: 180 },
  { title: '证明编号', dataIndex: 'certificateNo', key: 'certificateNo', width: 160 },
  { title: '上报人', dataIndex: 'reportedBy', key: 'reportedBy', width: 120 },
  { title: '上报时间', dataIndex: 'reportedTime', key: 'reportedTime', width: 180 },
  { title: '作废时间', dataIndex: 'cancelledTime', key: 'cancelledTime', width: 180 },
  { title: '状态', key: 'status', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 140 }
]

async function loadElders() {
  const res: PageResult<ElderItem> = await getElderPage({ pageNo: 1, pageSize: 200 })
  elders.value = res.list
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<DeathRegisterItem> = await getDeathRegisterPage(query)
    rows.value = res.list || []
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.elderId = undefined
  query.status = undefined
  query.keyword = undefined
  query.pageNo = 1
  fetchData()
}

function openCreate() {
  createOpen.value = true
  editingId.value = undefined
  form.elderId = 0
  form.deathDate = ''
  form.deathTime = undefined
  form.place = ''
  form.cause = ''
  form.certificateNo = ''
  form.reportedBy = ''
  form.reportedTime = undefined
  form.remark = ''
}

function openEdit(record: DeathRegisterItem) {
  createOpen.value = true
  editingId.value = record.id
  form.elderId = record.elderId
  form.deathDate = record.deathDate || ''
  form.deathTime = record.deathTime
  form.place = record.place || ''
  form.cause = record.cause || ''
  form.certificateNo = record.certificateNo || ''
  form.reportedBy = record.reportedBy || ''
  form.reportedTime = record.reportedTime
  form.remark = record.remark || ''
}

async function submitCreate() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await updateDeathRegister(editingId.value, {
        deathDate: form.deathDate,
        deathTime: form.deathTime,
        place: form.place,
        cause: form.cause,
        certificateNo: form.certificateNo,
        reportedBy: form.reportedBy,
        reportedTime: form.reportedTime,
        remark: form.remark
      })
      message.success('死亡登记已更正')
    } else {
      await createDeathRegister(form)
      message.success('死亡登记成功')
    }
    createOpen.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function cancelRecord(record: DeathRegisterItem) {
  await cancelDeathRegister(record.id, { remark: '前台作废操作' })
  message.success('死亡登记已作废')
  fetchData()
}

async function exportRows() {
  await exportDeathRegister({
    elderId: query.elderId,
    status: query.status,
    keyword: query.keyword
  })
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

onMounted(async () => {
  await loadElders()
  await fetchData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
