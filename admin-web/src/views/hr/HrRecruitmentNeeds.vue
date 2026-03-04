<template>
  <PageContainer :title="pageTitle" subTitle="招聘管理 / 岗位需求申请、审批流转与到岗追踪">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="标题/申请人/备注" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 180px" :options="statusOptions" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button @click="onExportCsv">导出 CSV</a-button>
          <a-button @click="onExportExcel">导出 Excel</a-button>
          <a-button type="primary" @click="openDrawer">新增需求</a-button>
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
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ record.status || '-' }}</a-tag>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="drawerOpen" title="新增招聘需求" width="520">
      <a-form :model="form" layout="vertical">
        <a-form-item label="需求标题" required>
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-form-item label="岗位名称" required>
          <a-input v-model:value="form.positionName" />
        </a-form-item>
        <a-form-item label="需求部门" required>
          <a-input v-model:value="form.departmentName" />
        </a-form-item>
        <a-form-item label="需求人数">
          <a-input-number v-model:value="form.headcount" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="期望到岗日期">
          <a-date-picker v-model:value="requiredDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { createHrRecruitmentNeed, getHrRecruitmentNeedPage } from '../../api/hr'
import type { HrRecruitmentNeedItem, PageResult } from '../../types'
import { resolveHrError } from './hrError'
import { exportCsv, exportExcel } from '../../utils/export'
import { mapByDict } from './hrExportFields'

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

const columns = [
  { title: '需求标题', dataIndex: 'title', key: 'title', width: 180 },
  { title: '岗位', dataIndex: 'positionName', key: 'positionName', width: 120 },
  { title: '部门', dataIndex: 'departmentName', key: 'departmentName', width: 120 },
  { title: '人数', dataIndex: 'headcount', key: 'headcount', width: 80 },
  { title: '期望到岗', dataIndex: 'requiredDate', key: 'requiredDate', width: 120 },
  { title: '申请人', dataIndex: 'applicantName', key: 'applicantName', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
]
const rows = ref<HrRecruitmentNeedItem[]>([])
const loading = ref(false)
const saving = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const drawerOpen = ref(false)
const requiredDate = ref<Dayjs>()
const form = reactive<Partial<HrRecruitmentNeedItem>>({
  status: 'PENDING',
  headcount: 1
})
const route = useRoute()

const pageTitle = computed(() => {
  if (route.name === 'HrRecruitmentJobPosting') return '岗位发布'
  if (route.name === 'HrRecruitmentCandidates') return '候选人库'
  if (route.name === 'HrRecruitmentInterviews') return '面试管理'
  if (route.name === 'HrRecruitmentOnboarding') return '入职办理'
  if (route.name === 'HrRecruitmentMaterials') return '入职资料收集'
  return '招聘需求申请'
})
const routeScene = computed(() => {
  if (route.name === 'HrRecruitmentJobPosting') return 'job-posting'
  if (route.name === 'HrRecruitmentCandidates') return 'candidates'
  if (route.name === 'HrRecruitmentInterviews') return 'interviews'
  if (route.name === 'HrRecruitmentOnboarding') return 'onboarding'
  if (route.name === 'HrRecruitmentMaterials') return 'materials'
  return undefined
})

function statusColor(status?: string) {
  if (status === 'APPROVED') return 'green'
  if (status === 'REJECTED') return 'red'
  return 'blue'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrRecruitmentNeedItem> = await getHrRecruitmentNeedPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      status: query.status,
      scene: routeScene.value
    })
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
  form.positionName = undefined
  form.departmentName = undefined
  form.headcount = 1
  form.status = 'PENDING'
  form.remark = undefined
  requiredDate.value = undefined
  drawerOpen.value = true
}

async function submit() {
  if (!form.title || !form.positionName || !form.departmentName) {
    message.warning('请填写标题、岗位、部门')
    return
  }
  saving.value = true
  try {
    await createHrRecruitmentNeed({
      ...form,
      scene: routeScene.value,
      requiredDate: requiredDate.value?.format('YYYY-MM-DD')
    })
    message.success('需求已提交')
    drawerOpen.value = false
    fetchData()
  } catch (error) {
    message.error(resolveHrError(error, '提交失败'))
  } finally {
    saving.value = false
  }
}

function rowClassName(record: HrRecruitmentNeedItem) {
  if (record.status === 'REJECTED') return 'hr-row-danger'
  if (record.status === 'PENDING') return 'hr-row-warning'
  return ''
}

const recruitmentExportFields = [
  { key: 'title', label: '需求标题' },
  { key: 'positionName', label: '岗位' },
  { key: 'departmentName', label: '部门' },
  { key: 'headcount', label: '人数' },
  { key: 'requiredDate', label: '期望到岗' },
  { key: 'scene', label: '场景' },
  { key: 'applicantName', label: '申请人' },
  { key: 'status', label: '状态' },
  { key: 'createTime', label: '创建时间' }
]

function onExportCsv() {
  exportCsv(mapByDict(rows.value as Record<string, any>[], recruitmentExportFields), `${pageTitle.value}-当前筛选`)
}

function onExportExcel() {
  exportExcel(mapByDict(rows.value as Record<string, any>[], recruitmentExportFields), `${pageTitle.value}-当前筛选`)
}

onMounted(fetchData)

watch(
  () => route.name,
  () => {
    onReset()
  }
)
</script>

<style scoped>
:deep(.hr-row-danger) {
  background: #fff1f0 !important;
}

:deep(.hr-row-warning) {
  background: #fffbe6 !important;
}
</style>
