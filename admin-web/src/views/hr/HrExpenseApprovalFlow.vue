<template>
  <PageContainer title="报销审批流" subTitle="薪酬与费用 / 审批流">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="标题/申请人/备注" allow-clear />
      </a-form-item>
      <a-form-item label="审批状态">
        <a-select v-model:value="query.status" allow-clear style="width: 180px" :options="statusOptions" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
          <a-button type="primary" @click="openDrawer">新建报销申请</a-button>
        </a-space>
      </template>
    </SearchForm>
    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      :row-class-name="rowClassName"
      @change="handleTableChange"
    />

    <a-drawer v-model:open="drawerOpen" title="新建报销审批" width="520">
      <a-form :model="form" layout="vertical">
        <a-form-item label="费用类型" required>
          <a-select v-model:value="form.expenseType" :options="expenseTypeOptions" />
        </a-form-item>
        <a-form-item label="场景">
          <a-select v-model:value="form.scene" :options="sceneOptions" allow-clear />
        </a-form-item>
        <a-form-item label="标题">
          <a-input v-model:value="form.title" placeholder="可留空自动生成" />
        </a-form-item>
        <a-form-item label="金额" required>
          <a-input-number v-model:value="form.amount" style="width: 100%" :min="0" :precision="2" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="申请区间">
          <a-range-picker v-model:value="applyRange" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="submit">提交</a-button>
        </a-space>
      </template>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { createHrExpenseApprovalFlow, getHrExpenseApprovalFlowPage } from '../../api/hr'
import { exportCsv, exportExcel } from '../../utils/export'
import type { HrExpenseApprovalRequest, HrExpenseItem, PageResult } from '../../types'
import { HR_EXPENSE_EXPORT_FIELDS, mapByDict } from './hrExportFields'
import { resolveHrError } from './hrError'

const query = reactive({
  keyword: undefined as string | undefined,
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const statusOptions = [
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]

const expenseTypeOptions = [
  { label: '培训费用', value: '培训费用' },
  { label: '补贴', value: '补贴' },
  { label: '工资补贴', value: '工资补贴' },
  { label: '餐费', value: '餐费' },
  { label: '电费', value: '电费' }
]

const sceneOptions = [
  { label: 'training-reimburse', value: 'training-reimburse' },
  { label: 'subsidy', value: 'subsidy' },
  { label: 'salary-subsidy', value: 'salary-subsidy' }
]

const columns = [
  { title: '费用类型', dataIndex: 'expenseType', key: 'expenseType', width: 120 },
  { title: '标题', dataIndex: 'title', key: 'title', width: 220 },
  { title: '申请人', dataIndex: 'applicantName', key: 'applicantName', width: 120 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '申请开始', dataIndex: 'applyStartTime', key: 'applyStartTime', width: 180 },
  { title: '申请结束', dataIndex: 'applyEndTime', key: 'applyEndTime', width: 180 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 220 }
]

const rows = ref<HrExpenseItem[]>([])
const loading = ref(false)
const saving = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const drawerOpen = ref(false)
const applyRange = ref<[Dayjs, Dayjs]>()
const form = reactive<HrExpenseApprovalRequest>({
  expenseType: '培训费用',
  status: 'PENDING'
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrExpenseItem> = await getHrExpenseApprovalFlowPage({ ...query })
    rows.value = res.list || []
    pagination.total = res.total || rows.value.length
  } catch {
    rows.value = []
    pagination.total = 0
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
  query.keyword = undefined
  query.status = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

function openDrawer() {
  form.title = undefined
  form.expenseType = '培训费用'
  form.scene = undefined
  form.amount = undefined
  form.status = 'PENDING'
  form.remark = undefined
  applyRange.value = undefined
  drawerOpen.value = true
}

async function submit() {
  if (!form.expenseType) {
    message.warning('请选择费用类型')
    return
  }
  if (!form.amount || form.amount <= 0) {
    message.warning('金额需大于0')
    return
  }
  saving.value = true
  try {
    await createHrExpenseApprovalFlow({
      ...form,
      applyStartTime: applyRange.value?.[0]?.format('YYYY-MM-DDTHH:mm:ss'),
      applyEndTime: applyRange.value?.[1]?.format('YYYY-MM-DDTHH:mm:ss')
    })
    message.success('提交成功')
    drawerOpen.value = false
    fetchData()
  } catch (error) {
    message.error(resolveHrError(error, '提交失败'))
  } finally {
    saving.value = false
  }
}

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], HR_EXPENSE_EXPORT_FIELDS), '报销审批流-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], HR_EXPENSE_EXPORT_FIELDS), '报销审批流-当前筛选')
}

function rowClassName(record: HrExpenseItem) {
  if (record.status === 'REJECTED') return 'hr-row-danger'
  if (record.status === 'PENDING') return 'hr-row-warning'
  return ''
}

onMounted(fetchData)
</script>

<style scoped>
:deep(.hr-row-danger) {
  background: #fff1f0 !important;
}

:deep(.hr-row-warning) {
  background: #fffbe6 !important;
}
</style>
