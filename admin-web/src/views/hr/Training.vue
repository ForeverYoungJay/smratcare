<template>
  <PageContainer :title="pageTitle" :subTitle="pageSubTitle">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="员工">
        <a-select
          v-model:value="query.staffId"
          allow-clear
          show-search
          :filter-option="false"
          :options="staffOptions"
          :loading="staffLoading"
          placeholder="选择员工"
          style="width: 200px"
          @search="searchStaff"
          @focus="() => !staffOptions.length && searchStaff('')"
        />
      </a-form-item>
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="培训名称/机构" allow-clear />
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openDrawer()">新增培训</a-button>
        <a-button :disabled="!selectedSingleRecord" @click="editSelected">编辑</a-button>
        <a-button :disabled="!selectedSingleRecord" danger @click="removeSelected">删除</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" danger @click="batchRemove">批量删除</a-button>
        <span class="selection-tip">已勾选 {{ selectedRowKeys.length }} 条</span>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :row-selection="rowSelection"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'default'">
            {{ record.status === 1 ? '完成' : '未完成' }}
          </a-tag>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="drawerOpen" :title="drawerTitle" width="560">
      <a-form :model="form" layout="vertical">
        <a-form-item label="员工" required>
          <a-select
            v-model:value="form.staffId"
            allow-clear
            show-search
            :filter-option="false"
            :options="staffOptions"
            :loading="staffLoading"
            placeholder="选择员工"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
          />
        </a-form-item>
        <a-form-item label="培训名称" required>
          <a-input v-model:value="form.trainingName" />
        </a-form-item>
        <a-form-item label="培训类型">
          <a-input v-model:value="form.trainingType" />
        </a-form-item>
        <a-form-item label="培训机构/讲师">
          <a-input v-model:value="form.provider" />
        </a-form-item>
        <a-form-item label="培训日期">
          <a-range-picker v-model:value="formRange" />
        </a-form-item>
        <a-form-item label="培训时长(小时)">
          <a-input-number v-model:value="form.hours" style="width: 100%" :min="0" :step="0.5" />
        </a-form-item>
        <a-form-item label="培训成绩">
          <a-input-number v-model:value="form.score" style="width: 100%" :min="0" :max="100" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="证书编号">
          <a-input v-model:value="form.certificateNo" />
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
import { computed, reactive, ref, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getHrTrainingPage, createHrTraining, updateHrTraining, deleteHrTraining } from '../../api/hr'
import { useStaffOptions } from '../../composables/useStaffOptions'
import type { StaffTrainingRecord, PageResult } from '../../types'

const route = useRoute()

const trainingScene = computed<'plans' | 'enrollments' | 'signin' | 'records'>(() => {
  const queryScene = String(route.query.scene || '').trim()
  if (queryScene === 'plans') return 'plans'
  if (queryScene === 'enrollments') return 'enrollments'
  if (queryScene === 'signin') return 'signin'
  if (queryScene === 'records') return 'records'
  if (route.name === 'HrDevelopmentPlans') return 'plans'
  if (route.name === 'HrDevelopmentEnrollments') return 'enrollments'
  if (route.name === 'HrDevelopmentSignin') return 'signin'
  return 'records'
})

const pageTitle = computed(() => {
  if (trainingScene.value === 'plans') return '培训计划'
  if (trainingScene.value === 'enrollments') return '培训报名'
  if (trainingScene.value === 'signin') return '培训签到'
  return '培训记录'
})

const pageSubTitle = computed(() => {
  if (trainingScene.value === 'plans') return '培训与发展 / 课程排期与计划执行'
  if (trainingScene.value === 'enrollments') return '培训与发展 / 报名与参训名单管理'
  if (trainingScene.value === 'signin') return '培训与发展 / 培训签到与出勤追踪'
  return '培训与发展 / 培训记录与结果归档'
})

const query = reactive({
  staffId: undefined as string | number | undefined,
  keyword: undefined as string | undefined,
  range: undefined as [Dayjs, Dayjs] | undefined,
  pageNo: 1,
  pageSize: 10
})
const rows = ref<StaffTrainingRecord[]>([])
const loading = ref(false)
const saving = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<string[]>([])

const columns = [
  { title: '员工', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '培训名称', dataIndex: 'trainingName', key: 'trainingName', width: 160 },
  { title: '培训类型', dataIndex: 'trainingType', key: 'trainingType', width: 120 },
  { title: '机构/讲师', dataIndex: 'provider', key: 'provider', width: 140 },
  { title: '开始日期', dataIndex: 'startDate', key: 'startDate', width: 120 },
  { title: '结束日期', dataIndex: 'endDate', key: 'endDate', width: 120 },
  { title: '时长', dataIndex: 'hours', key: 'hours', width: 100 },
  { title: '成绩', dataIndex: 'score', key: 'score', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 }
]

const drawerOpen = ref(false)
const form = reactive<Partial<StaffTrainingRecord>>({ status: 1 })
const formRange = ref<[Dayjs, Dayjs] | undefined>()
const { staffOptions, staffLoading, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 120 })
const statusOptions = [
  { label: '完成', value: 1 },
  { label: '未完成', value: 0 }
]
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))

const drawerTitle = computed(() => (form.id ? '编辑培训' : '新增培训'))

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      staffId: query.staffId,
      keyword: query.keyword
    }
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    const res: PageResult<StaffTrainingRecord> = await getHrTrainingPage(params)
    rows.value = (res.list || []).map((item) => ({
      ...item,
      id: String(item.id),
      staffId: item.staffId == null ? item.staffId : String(item.staffId)
    }))
    pagination.total = res.total || res.list.length
    selectedRowKeys.value = []
  } catch {
    rows.value = []
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
  query.staffId = undefined
  query.keyword = undefined
  query.range = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openDrawer(record?: StaffTrainingRecord) {
  Object.assign(form, record || { status: 1 })
  if (record?.staffId && record?.staffName) {
    ensureSelectedStaff(record.staffId, record.staffName)
  }
  if (record?.startDate && record?.endDate) {
    formRange.value = [dayjs(record.startDate as string), dayjs(record.endDate as string)]
  } else {
    formRange.value = undefined
  }
  drawerOpen.value = true
}

async function submit() {
  if (!form.staffId) {
    message.error('请选择员工')
    return
  }
  saving.value = true
  try {
    if (formRange.value) {
      form.startDate = formRange.value[0].format('YYYY-MM-DD')
      form.endDate = formRange.value[1].format('YYYY-MM-DD')
    }
    if (form.id) {
      await updateHrTraining(form.id, form)
    } else {
      await createHrTraining(form)
    }
    message.success('保存成功')
    drawerOpen.value = false
    fetchData()
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(record: StaffTrainingRecord) {
  if (!record.id) return
  try {
    await deleteHrTraining(record.id)
    message.success('删除成功')
    fetchData()
  } catch {
    message.error('删除失败')
  }
}

function requireSingleSelection(action: string) {
  if (!selectedSingleRecord.value) {
    message.info(`请先勾选 1 条培训记录后再${action}`)
    return null
  }
  return selectedSingleRecord.value
}

function editSelected() {
  const record = requireSingleSelection('编辑')
  if (!record) return
  openDrawer(record)
}

async function removeSelected() {
  const record = requireSingleSelection('删除')
  if (!record) return
  Modal.confirm({
    title: '确认删除培训记录？',
    content: `将删除「${record.trainingName || '未命名培训'}」记录，删除后不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      await remove(record)
    }
  })
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  Modal.confirm({
    title: '确认批量删除培训记录？',
    content: `将删除 ${selectedRecords.value.length} 条培训记录，删除后不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await Promise.all(selectedRecords.value.map((record) => deleteHrTraining(String(record.id))))
        message.success(`批量删除成功，共处理 ${selectedRecords.value.length} 条`)
        fetchData()
      } catch {
        message.error('批量删除失败')
      }
    }
  })
}

searchStaff('')
fetchData()

watch(
  () => route.fullPath,
  () => {
    if (loading.value || saving.value) return
    fetchData()
  }
)
</script>

<style scoped>
.selection-tip {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
