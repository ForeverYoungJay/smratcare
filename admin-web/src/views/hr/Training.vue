<template>
  <PageContainer title="培训管理" subTitle="护理员培训与考核记录">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="员工">
        <a-select
          v-model:value="query.staffId"
          allow-clear
          show-search
          :filter-option="false"
          placeholder="选择员工"
          style="width: 200px"
          @search="searchStaff"
        >
          <a-select-option v-for="item in staffOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="培训名称/机构" allow-clear />
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openDrawer()">新增培训</a-button>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
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
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a @click="openDrawer(record)">编辑</a>
            <a @click="remove(record)">删除</a>
          </a-space>
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
            placeholder="选择员工"
            @search="searchStaff"
          >
            <a-select-option v-for="item in staffOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
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
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getHrTrainingPage, createHrTraining, updateHrTraining, deleteHrTraining } from '../../api/hr'
import { getStaffPage } from '../../api/rbac'
import type { StaffTrainingRecord, PageResult, StaffItem } from '../../types'

const query = reactive({
  staffId: undefined as number | undefined,
  keyword: undefined as string | undefined,
  range: undefined as [Dayjs, Dayjs] | undefined,
  pageNo: 1,
  pageSize: 10
})
const rows = ref<StaffTrainingRecord[]>([])
const loading = ref(false)
const saving = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '员工', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '培训名称', dataIndex: 'trainingName', key: 'trainingName', width: 160 },
  { title: '培训类型', dataIndex: 'trainingType', key: 'trainingType', width: 120 },
  { title: '机构/讲师', dataIndex: 'provider', key: 'provider', width: 140 },
  { title: '开始日期', dataIndex: 'startDate', key: 'startDate', width: 120 },
  { title: '结束日期', dataIndex: 'endDate', key: 'endDate', width: 120 },
  { title: '时长', dataIndex: 'hours', key: 'hours', width: 100 },
  { title: '成绩', dataIndex: 'score', key: 'score', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 140 }
]

const drawerOpen = ref(false)
const form = reactive<Partial<StaffTrainingRecord>>({ status: 1 })
const formRange = ref<[Dayjs, Dayjs] | undefined>()
const staffOptions = ref<Array<{ label: string; value: number }>>([])
const statusOptions = [
  { label: '完成', value: 1 },
  { label: '未完成', value: 0 }
]

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
    rows.value = res.list
    pagination.total = res.total || res.list.length
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
    ensureStaffOption(record.staffId, record.staffName)
  }
  if (record?.startDate && record?.endDate) {
    formRange.value = [dayjs(record.startDate as string), dayjs(record.endDate as string)]
  } else {
    formRange.value = undefined
  }
  drawerOpen.value = true
}

async function loadStaffOptions(keyword?: string) {
  const res: PageResult<StaffItem> = await getStaffPage({ pageNo: 1, pageSize: 50, keyword })
  staffOptions.value = res.list.map((item) => ({ label: item.realName || item.username, value: item.id }))
}

function ensureStaffOption(id: number, name: string) {
  if (!staffOptions.value.some((item) => item.value === id)) {
    staffOptions.value = [{ label: name, value: id }, ...staffOptions.value]
  }
}

async function searchStaff(val: string) {
  await loadStaffOptions(val)
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

loadStaffOptions()
fetchData()
</script>
