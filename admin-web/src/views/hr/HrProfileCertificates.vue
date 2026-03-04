<template>
  <PageContainer title="证书上传" subTitle="员工档案中心 / 证书上传">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="证书名称/证书编号/颁发机构" allow-clear />
      </a-form-item>
      <a-form-item label="有效期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
          <a-button type="primary" @click="openDrawer">新增证书</a-button>
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
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'remainingDays'">
          <a-tag v-if="(record.remainingDays || 0) < 0" color="red">已过期 {{ Math.abs(record.remainingDays || 0) }} 天</a-tag>
          <a-tag v-else-if="(record.remainingDays || 0) <= 30" color="orange">{{ record.remainingDays }} 天</a-tag>
          <span v-else>{{ record.remainingDays ?? '-' }} 天</span>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="drawerOpen" title="新增证书" width="560">
      <a-form :model="form" layout="vertical">
        <a-form-item label="员工" required>
          <a-select
            v-model:value="form.staffId"
            allow-clear
            show-search
            :filter-option="false"
            placeholder="选择员工"
            @search="searchStaff"
          >
            <a-select-option v-for="item in staffOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="证书名称" required>
          <a-input v-model:value="form.certificateName" />
        </a-form-item>
        <a-form-item label="证书编号">
          <a-input v-model:value="form.certificateNo" />
        </a-form-item>
        <a-form-item label="颁发机构">
          <a-input v-model:value="form.issuingAuthority" />
        </a-form-item>
        <a-form-item label="颁发日期">
          <a-date-picker v-model:value="issueDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="到期日期">
          <a-date-picker v-model:value="expiryDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="submit">保存</a-button>
        </a-space>
      </template>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { createHrProfileCertificate, getHrProfileCertificatePage } from '../../api/hr'
import { getStaffPage } from '../../api/rbac'
import type { HrStaffCertificateItem, PageResult, StaffItem } from '../../types'
import { resolveHrError } from './hrError'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapByDict } from './hrExportFields'

const query = reactive({
  keyword: undefined as string | undefined,
  range: undefined as [Dayjs, Dayjs] | undefined,
  pageNo: 1,
  pageSize: 10
})
const columns = [
  { title: '员工', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '证书名称', dataIndex: 'certificateName', key: 'certificateName', width: 160 },
  { title: '证书编号', dataIndex: 'certificateNo', key: 'certificateNo', width: 160 },
  { title: '颁发机构', dataIndex: 'issuingAuthority', key: 'issuingAuthority', width: 160 },
  { title: '颁发日期', dataIndex: 'issueDate', key: 'issueDate', width: 120 },
  { title: '到期日期', dataIndex: 'expiryDate', key: 'expiryDate', width: 120 },
  { title: '剩余天数', dataIndex: 'remainingDays', key: 'remainingDays', width: 120 }
]
const rows = ref<HrStaffCertificateItem[]>([])
const loading = ref(false)
const saving = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const drawerOpen = ref(false)
const issueDate = ref<Dayjs>()
const expiryDate = ref<Dayjs>()
const form = reactive<Partial<HrStaffCertificateItem>>({})
const staffOptions = ref<Array<{ label: string; value: string | number }>>([])

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword
    }
    if (query.range) {
      params.expiryFrom = query.range[0].format('YYYY-MM-DD')
      params.expiryTo = query.range[1].format('YYYY-MM-DD')
    }
    const res: PageResult<HrStaffCertificateItem> = await getHrProfileCertificatePage(params)
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
  query.range = undefined
  query.pageNo = 1
  query.pageSize = 10
  pagination.current = 1
  pagination.pageSize = 10
  fetchData()
}

async function loadStaffOptions(keyword?: string) {
  const res: PageResult<StaffItem> = await getStaffPage({ pageNo: 1, pageSize: 50, keyword })
  staffOptions.value = res.list.map((item) => ({ label: item.realName || item.username, value: String(item.id) }))
}

async function searchStaff(val: string) {
  await loadStaffOptions(val)
}

function openDrawer() {
  form.staffId = undefined
  form.certificateName = undefined
  form.certificateNo = undefined
  form.issuingAuthority = undefined
  form.remark = undefined
  issueDate.value = undefined
  expiryDate.value = undefined
  drawerOpen.value = true
}

async function submit() {
  if (!form.staffId || !form.certificateName) {
    message.warning('请填写员工和证书名称')
    return
  }
  saving.value = true
  try {
    await createHrProfileCertificate({
      ...form,
      issueDate: issueDate.value?.format('YYYY-MM-DD'),
      expiryDate: expiryDate.value?.format('YYYY-MM-DD')
    })
    message.success('证书已新增')
    drawerOpen.value = false
    fetchData()
  } catch (error) {
    message.error(resolveHrError(error, '保存失败'))
  } finally {
    saving.value = false
  }
}

function rowClassName(record: HrStaffCertificateItem) {
  const days = Number(record.remainingDays ?? 0)
  if (days < 0) return 'hr-row-danger'
  if (days <= 30) return 'hr-row-warning'
  return ''
}

const exportFields = [
  { key: 'staffName', label: '员工' },
  { key: 'certificateName', label: '证书名称' },
  { key: 'certificateNo', label: '证书编号' },
  { key: 'issuingAuthority', label: '颁发机构' },
  { key: 'issueDate', label: '颁发日期' },
  { key: 'expiryDate', label: '到期日期' },
  { key: 'remainingDays', label: '剩余天数' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], exportFields), '证书上传-当前筛选')
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], exportFields), '证书上传-当前筛选')
}

onMounted(() => {
  fetchData()
  loadStaffOptions()
})
</script>

<style scoped>
:deep(.hr-row-danger) {
  background: #fff1f0 !important;
}

:deep(.hr-row-warning) {
  background: #fffbe6 !important;
}
</style>
