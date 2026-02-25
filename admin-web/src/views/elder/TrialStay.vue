<template>
  <PageContainer title="试住登记" subTitle="试住周期与意向跟进">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="老人">
          <a-select v-model:value="query.elderId" allow-clear style="width: 180px">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 180px">
            <a-select-option value="REGISTERED">已登记</a-select-option>
            <a-select-option value="FINISHED">已结束</a-select-option>
            <a-select-option value="CONVERTED">已转签约</a-select-option>
            <a-select-option value="CANCELLED">已取消</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" ghost @click="openCreate">新增试住</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-table :data-source="rows" :columns="columns" :loading="loading" :pagination="false" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag>{{ record.status || '-' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
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

    <a-modal v-model:open="editOpen" :title="editId ? '编辑试住登记' : '新增试住登记'" @ok="submit" :confirm-loading="submitting" width="560px">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="老人" name="elderId">
          <a-select v-model:value="form.elderId" placeholder="请选择老人">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="开始日期" name="trialStartDate">
          <a-date-picker v-model:value="form.trialStartDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="结束日期" name="trialEndDate">
          <a-date-picker v-model:value="form.trialEndDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="来源渠道">
          <a-select v-model:value="form.channel" allow-clear placeholder="请选择来源渠道">
            <a-select-option v-for="item in channelOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="试住套餐">
          <a-select v-model:value="form.trialPackage" allow-clear placeholder="请选择试住套餐">
            <a-select-option v-for="item in trialPackageOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="意向等级">
          <a-select v-model:value="form.intentLevel" allow-clear>
            <a-select-option value="HIGH">高</a-select-option>
            <a-select-option value="MEDIUM">中</a-select-option>
            <a-select-option value="LOW">低</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" allow-clear>
            <a-select-option value="REGISTERED">已登记</a-select-option>
            <a-select-option value="FINISHED">已结束</a-select-option>
            <a-select-option value="CONVERTED">已转签约</a-select-option>
            <a-select-option value="CANCELLED">已取消</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="护理等级">
          <a-input v-model:value="form.careLevel" />
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
import { getBaseConfigItemList } from '../../api/baseConfig'
import { getElderPage } from '../../api/elder'
import { createTrialStay, getTrialStayPage, updateTrialStay } from '../../api/elderResidence'
import type { BaseConfigItem, ElderItem, PageResult, TrialStayItem, TrialStayRequest } from '../../types'

const loading = ref(false)
const rows = ref<TrialStayItem[]>([])
const total = ref(0)
const elders = ref<ElderItem[]>([])
const editOpen = ref(false)
const editId = ref<number | undefined>(undefined)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const channelOptions = ref<{ label: string; value: string }[]>([])
const trialPackageOptions = ref<{ label: string; value: string }[]>([])

const query = reactive({
  elderId: undefined as number | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const form = reactive<TrialStayRequest>({
  elderId: 0,
  trialStartDate: '',
  trialEndDate: '',
  channel: '',
  trialPackage: '',
  intentLevel: 'MEDIUM',
  status: 'REGISTERED',
  careLevel: '',
  remark: ''
})

const rules: FormRules = {
  elderId: [{ required: true, message: '请选择老人' }],
  trialStartDate: [{ required: true, message: '请选择开始日期' }],
  trialEndDate: [{ required: true, message: '请选择结束日期' }]
}

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '开始日期', dataIndex: 'trialStartDate', key: 'trialStartDate', width: 120 },
  { title: '结束日期', dataIndex: 'trialEndDate', key: 'trialEndDate', width: 120 },
  { title: '来源渠道', dataIndex: 'channel', key: 'channel', width: 140 },
  { title: '试住套餐', dataIndex: 'trialPackage', key: 'trialPackage', width: 140 },
  { title: '意向等级', dataIndex: 'intentLevel', key: 'intentLevel', width: 100 },
  { title: '状态', key: 'status', width: 120 },
  { title: '护理等级', dataIndex: 'careLevel', key: 'careLevel', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 100 }
]

async function loadElders() {
  const res: PageResult<ElderItem> = await getElderPage({ pageNo: 1, pageSize: 200 })
  elders.value = res.list
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
    const res: PageResult<TrialStayItem> = await getTrialStayPage(query)
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.elderId = undefined
  query.status = undefined
  query.pageNo = 1
  fetchData()
}

function openCreate() {
  editOpen.value = true
  editId.value = undefined
  form.elderId = 0
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

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
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

function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

onMounted(async () => {
  await loadDictOptions()
  await loadElders()
  await fetchData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
