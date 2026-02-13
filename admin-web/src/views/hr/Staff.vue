<template>
  <PageContainer title="人力资源" subTitle="护理员档案与岗位信息">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="姓名/工号/手机号" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openDrawer()">新增档案</a-button>
      </template>
    </SearchForm>

    <DataTable
      rowKey="staffId"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'default'">
            {{ record.status === 1 ? '在职' : '离职' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a @click="openDrawer(record)">编辑档案</a>
            <a @click="viewProfile(record)">查看</a>
            <a v-if="record.status === 1" @click="openTerminate(record)">离职</a>
            <a v-else @click="submitReinstate(record)">复职</a>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="drawerOpen" :title="drawerTitle" width="560">
      <a-form :model="form" layout="vertical">
        <a-form-item label="员工" required>
          <a-select
            v-if="!form.staffId"
            v-model:value="form.staffId"
            allow-clear
            show-search
            :filter-option="false"
            placeholder="选择员工"
            @search="searchStaff"
            @change="onStaffChange"
          >
            <a-select-option v-for="item in staffOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
          <a-input v-else v-model:value="form.realName" disabled />
        </a-form-item>
        <a-form-item label="岗位/职称">
          <a-input v-model:value="form.jobTitle" />
        </a-form-item>
        <a-form-item label="用工类型">
          <a-select v-model:value="form.employmentType" :options="employmentOptions" />
        </a-form-item>
        <a-form-item label="入职日期">
          <a-date-picker v-model:value="form.hireDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="资质等级">
          <a-input v-model:value="form.qualificationLevel" />
        </a-form-item>
        <a-form-item label="证书编号">
          <a-input v-model:value="form.certificateNo" />
        </a-form-item>
        <a-form-item label="紧急联系人">
          <a-input v-model:value="form.emergencyContactName" />
        </a-form-item>
        <a-form-item label="紧急联系电话">
          <a-input v-model:value="form.emergencyContactPhone" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="离职日期">
          <a-date-picker v-model:value="form.leaveDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="离职原因">
          <a-input v-model:value="form.leaveReason" />
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

    <a-modal v-model:open="terminateOpen" title="员工离职" @ok="submitTerminate" :confirm-loading="terminating">
      <a-form layout="vertical">
        <a-form-item label="员工ID" required>
          <a-input v-model:value="terminateForm.staffId" disabled />
        </a-form-item>
        <a-form-item label="离职日期">
          <a-date-picker v-model:value="terminateForm.leaveDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="离职原因">
          <a-textarea v-model:value="terminateForm.leaveReason" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getHrStaffPage, getHrProfile, upsertHrProfile, terminateStaff, reinstateStaff } from '../../api/hr'
import { getStaffPage } from '../../api/rbac'
import type { HrStaffProfile, PageResult, StaffItem } from '../../types'

const query = reactive({ keyword: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const rows = ref<HrStaffProfile[]>([])
const loading = ref(false)
const saving = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const staffOptions = ref<Array<{ label: string; value: number }>>([])

const columns = [
  { title: '工号', dataIndex: 'staffNo', key: 'staffNo', width: 120 },
  { title: '姓名', dataIndex: 'realName', key: 'realName', width: 120 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '岗位', dataIndex: 'jobTitle', key: 'jobTitle', width: 140 },
  { title: '用工类型', dataIndex: 'employmentType', key: 'employmentType', width: 120 },
  { title: '离职日期', dataIndex: 'leaveDate', key: 'leaveDate', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 160 }
]

const drawerOpen = ref(false)
const form = reactive<Partial<HrStaffProfile>>({})
const statusOptions = [
  { label: '在职', value: 1 },
  { label: '离职', value: 0 }
]
const employmentOptions = [
  { label: '全职', value: 'FULLTIME' },
  { label: '兼职', value: 'PARTTIME' },
  { label: '外包', value: 'OUTSOURCE' }
]

const drawerTitle = computed(() => (form.staffId ? '编辑档案' : '新增档案'))

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrStaffProfile> = await getHrStaffPage(query)
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
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

async function loadStaffOptions(keyword?: string) {
  const res: PageResult<StaffItem> = await getStaffPage({ pageNo: 1, pageSize: 50, keyword })
  staffOptions.value = res.list.map((item) => ({ label: item.realName || item.username, value: item.id }))
}

async function searchStaff(val: string) {
  await loadStaffOptions(val)
}

function onStaffChange(val: number) {
  const selected = staffOptions.value.find((item) => item.value === val)
  if (selected) {
    form.realName = selected.label
  }
}

async function openDrawer(record?: HrStaffProfile) {
  Object.keys(form).forEach((key) => {
    ;(form as any)[key] = undefined
  })
  Object.assign(form, record || { status: 1 })
  if (!record) {
    await loadStaffOptions()
  }
  if (record?.staffId && record?.realName) {
    if (!staffOptions.value.some((item) => item.value === record.staffId)) {
      staffOptions.value = [{ label: record.realName, value: record.staffId }, ...staffOptions.value]
    }
  }
  if (record?.staffId) {
    try {
      const profile = await getHrProfile(record.staffId)
      Object.assign(form, profile)
    } catch {
      // ignore
    }
  }
  if (form.hireDate && typeof form.hireDate === 'string') {
    form.hireDate = dayjs(form.hireDate)
  }
  if (form.leaveDate && typeof form.leaveDate === 'string') {
    form.leaveDate = dayjs(form.leaveDate)
  }
  drawerOpen.value = true
}

async function viewProfile(record: HrStaffProfile) {
  await openDrawer(record)
}

async function submit() {
  if (!form.staffId) {
    message.error('请选择员工')
    return
  }
  saving.value = true
  try {
    const payload = { ...form } as any
    if (payload.hireDate && typeof payload.hireDate === 'object' && payload.hireDate.format) {
      payload.hireDate = payload.hireDate.format('YYYY-MM-DD')
    }
    if (payload.leaveDate && typeof payload.leaveDate === 'object' && payload.leaveDate.format) {
      payload.leaveDate = payload.leaveDate.format('YYYY-MM-DD')
    }
    await upsertHrProfile(payload)
    message.success('保存成功')
    drawerOpen.value = false
    fetchData()
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

const terminateOpen = ref(false)
const terminating = ref(false)
const terminateForm = reactive<{ staffId?: number; leaveDate?: any; leaveReason?: string }>({})

function openTerminate(record: HrStaffProfile) {
  terminateForm.staffId = record.staffId
  terminateForm.leaveDate = record.leaveDate ? dayjs(record.leaveDate) : undefined
  terminateForm.leaveReason = record.leaveReason || ''
  terminateOpen.value = true
}

async function submitTerminate() {
  if (!terminateForm.staffId) return
  terminating.value = true
  try {
    const params: any = { staffId: terminateForm.staffId }
    if (terminateForm.leaveDate && terminateForm.leaveDate.format) {
      params.leaveDate = terminateForm.leaveDate.format('YYYY-MM-DD')
    }
    if (terminateForm.leaveReason) {
      params.leaveReason = terminateForm.leaveReason
    }
    await terminateStaff(params)
    message.success('已设置离职')
    terminateOpen.value = false
    fetchData()
  } catch {
    message.error('离职操作失败')
  } finally {
    terminating.value = false
  }
}

async function submitReinstate(record: HrStaffProfile) {
  if (!record.staffId) return
  try {
    await reinstateStaff({ staffId: record.staffId })
    message.success('已复职')
    fetchData()
  } catch {
    message.error('复职操作失败')
  }
}

loadStaffOptions()
fetchData()
</script>
