<template>
  <PageContainer title="外出就医登记" subTitle="老人外出就医与返院登记">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="老人">
          <a-select v-model:value="query.elderId" allow-clear style="width: 180px" placeholder="请选择老人姓名">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 140px" placeholder="请选择状态">
            <a-select-option value="OUT">外出中</a-select-option>
            <a-select-option value="RETURNED">已返院</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="医院/科室/诊断/原因" style="width: 220px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">搜索</a-button>
            <a-button @click="reset">清空</a-button>
            <a-button v-if="canManage" @click="exportRows">导出</a-button>
            <a-button v-if="canManage" type="primary" ghost @click="openCreate">新增外出就医</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-table :data-source="rows" :columns="columns" :loading="loading" :pagination="false" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'RETURNED' ? 'green' : 'orange'">
              {{ record.status === 'RETURNED' ? '已返院' : '外出中' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button v-if="canManage" type="link" :disabled="record.status === 'RETURNED'" @click="markReturn(record)">返院登记</a-button>
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

    <a-modal v-model:open="createOpen" title="新增外出就医登记" @ok="submitCreate" :confirm-loading="submitting" width="560px">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="老人" name="elderId">
          <a-select v-model:value="form.elderId" placeholder="请选择老人">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="外出就医日期" name="outingDate">
          <a-date-picker v-model:value="form.outingDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="预计返院时间">
          <a-date-picker
            v-model:value="form.expectedReturnTime"
            show-time
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="就医医院">
          <a-input v-model:value="form.hospitalName" />
        </a-form-item>
        <a-form-item label="就诊科室">
          <a-input v-model:value="form.department" />
        </a-form-item>
        <a-form-item label="初步诊断">
          <a-input v-model:value="form.diagnosis" />
        </a-form-item>
        <a-form-item label="陪同人">
          <a-input v-model:value="form.companion" />
        </a-form-item>
        <a-form-item label="就医原因">
          <a-input v-model:value="form.reason" />
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
import { createMedicalOuting, exportMedicalOuting, getMedicalOutingPage, returnMedicalOuting } from '../../api/elderResidence'
import { getRoles } from '../../utils/auth'
import type { ElderItem, MedicalOutingCreateRequest, MedicalOutingItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<MedicalOutingItem[]>([])
const total = ref(0)
const elders = ref<ElderItem[]>([])
const createOpen = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const roles = getRoles()
const canManage = roles.includes('ADMIN') || roles.includes('STAFF')

const query = reactive({
  elderId: undefined as number | undefined,
  status: undefined as 'OUT' | 'RETURNED' | undefined,
  keyword: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const form = reactive<MedicalOutingCreateRequest>({
  elderId: 0,
  outingDate: '',
  expectedReturnTime: undefined,
  hospitalName: '',
  department: '',
  diagnosis: '',
  companion: '',
  reason: '',
  remark: ''
})

const rules: FormRules = {
  elderId: [{ required: true, message: '请选择老人' }],
  outingDate: [{ required: true, message: '请选择外出就医日期' }]
}

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '外出日期', dataIndex: 'outingDate', key: 'outingDate', width: 120 },
  { title: '预计返院', dataIndex: 'expectedReturnTime', key: 'expectedReturnTime', width: 180 },
  { title: '实际返院', dataIndex: 'actualReturnTime', key: 'actualReturnTime', width: 180 },
  { title: '医院', dataIndex: 'hospitalName', key: 'hospitalName', width: 160 },
  { title: '科室', dataIndex: 'department', key: 'department', width: 120 },
  { title: '初步诊断', dataIndex: 'diagnosis', key: 'diagnosis', width: 180 },
  { title: '就医原因', dataIndex: 'reason', key: 'reason' },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 120 }
]

async function loadElders() {
  const res: PageResult<ElderItem> = await getElderPage({ pageNo: 1, pageSize: 200 })
  elders.value = res.list
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<MedicalOutingItem> = await getMedicalOutingPage(query)
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
  form.elderId = 0
  form.outingDate = ''
  form.expectedReturnTime = undefined
  form.hospitalName = ''
  form.department = ''
  form.diagnosis = ''
  form.companion = ''
  form.reason = ''
  form.remark = ''
}

async function submitCreate() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    await createMedicalOuting(form)
    message.success('外出就医登记成功')
    createOpen.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function markReturn(record: MedicalOutingItem) {
  await returnMedicalOuting(record.id, {})
  message.success('外出就医返院登记成功')
  fetchData()
}

async function exportRows() {
  await exportMedicalOuting({
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
