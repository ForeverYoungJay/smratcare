<template>
  <PageContainer title="外出就医登记" subTitle="老人外出就医与返院登记">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="老人">
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
            <a-button v-if="canManage" type="primary" @click="openCreate">新增外出就医</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-space style="margin-bottom: 12px" wrap>
        <a-tag color="blue">已选 {{ selectedRowKeys.length }} 条</a-tag>
        <a-button v-if="canManage" :disabled="selectedRowKeys.length !== 1" @click="markReturnSelected">返院登记</a-button>
        <a-button v-if="canManage" danger :disabled="selectedRowKeys.length === 0" @click="deleteSelected">删除</a-button>
      </a-space>
      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :row-selection="rowSelection"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'RETURNED' ? 'green' : 'orange'">
              {{ record.status === 'RETURNED' ? '已返院' : '外出中' }}
            </a-tag>
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
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import {
  createMedicalOuting,
  deleteMedicalOuting,
  exportMedicalOuting,
  getMedicalOutingPage,
  returnMedicalOuting
} from '../../api/elderResidence'
import { getRoles } from '../../utils/auth'
import { hasStaffOrHigher } from '../../utils/roleAccess'
import type { MedicalOutingCreateRequest, MedicalOutingItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<MedicalOutingItem[]>([])
const total = ref(0)
const createOpen = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const selectedRowKeys = ref<number[]>([])
const route = useRoute()
const { elderOptions, elderLoading, searchElders, ensureSelectedElder } = useElderOptions({ pageSize: 80 })
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys as number[]
  }
}))
const roles = getRoles()
const canManage = hasStaffOrHigher(roles)

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
  { title: '状态', key: 'status', width: 100 }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<MedicalOutingItem> = await getMedicalOutingPage(query)
    rows.value = res.list || []
    total.value = res.total
    selectedRowKeys.value = selectedRowKeys.value.filter((id) => rows.value.some((item) => item.id === id))
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

function getSelectedRecord() {
  if (selectedRowKeys.value.length !== 1) return undefined
  return rows.value.find((item) => item.id === selectedRowKeys.value[0])
}

async function markReturnSelected() {
  const record = getSelectedRecord()
  if (!record) return
  if (record.status === 'RETURNED') {
    message.warning('该记录已返院，无需重复登记')
    return
  }
  await markReturn(record)
}

function deleteSelected() {
  if (!selectedRowKeys.value.length) return
  Modal.confirm({
    title: '确认删除外出就医记录？',
    content: `将删除 ${selectedRowKeys.value.length} 条记录`,
    okText: '确认删除',
    okType: 'danger',
    async onOk() {
      await Promise.all(selectedRowKeys.value.map((id) => deleteMedicalOuting(id)))
      message.success('删除成功')
      selectedRowKeys.value = []
      await fetchData()
    }
  })
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
  await searchElders('')
  const elderId = Number(route.query.elderId || 0)
  if (elderId > 0) {
    ensureSelectedElder(elderId)
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
