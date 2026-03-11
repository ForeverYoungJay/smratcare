<template>
  <PageContainer :title="pageTitle" :subTitle="pageSubTitle">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="姓名/工号/手机号" allow-clear />
      </a-form-item>
      <a-form-item label="部门">
        <a-select
          v-model:value="query.departmentId"
          allow-clear
          show-search
          :filter-option="false"
          :options="departmentFilterOptions"
          placeholder="选择部门"
          style="width: 200px"
          @search="searchDepartments"
          @focus="() => !departmentOptions.length && searchDepartments('')"
        />
      </a-form-item>
      <a-form-item label="角色">
        <a-select
          v-model:value="query.roleId"
          allow-clear
          show-search
          :options="roleFilterOptions"
          placeholder="选择角色"
          style="width: 220px"
        />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openDrawer()">新增档案</a-button>
        <a-button :disabled="!selectedSingleRecord" @click="editSelected">编辑档案</a-button>
        <a-button :disabled="!selectedSingleRecord" @click="viewSelected">查看</a-button>
        <a-button :disabled="!canTerminateSingle" @click="terminateSelected">离职</a-button>
        <a-button :disabled="!canReinstateSingle" @click="reinstateSelected">复职</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" @click="batchTerminate">批量离职</a-button>
        <a-button :disabled="selectedRowKeys.length === 0" @click="batchReinstate">批量复职</a-button>
        <a-button @click="printBirthdayLedger">打印生日台账</a-button>
        <span class="selection-tip">已勾选 {{ selectedRowKeys.length }} 条</span>
      </template>
    </SearchForm>

    <DataTable
      rowKey="staffId"
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
            {{ record.status === 1 ? '在职' : '离职' }}
          </a-tag>
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
            :options="staffOptions"
            :loading="staffLoading"
            placeholder="选择员工"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
            @change="onStaffChange"
          />
          <a-input v-else v-model:value="form.realName" disabled />
        </a-form-item>
        <a-form-item label="岗位/职称">
          <a-input v-model:value="form.jobTitle" />
        </a-form-item>
        <a-form-item label="用工类型">
          <a-select v-model:value="form.employmentType" :options="employmentOptions" />
        </a-form-item>
        <a-form-item label="合同编号">
          <a-input v-model:value="form.contractNo" />
        </a-form-item>
        <a-form-item label="合同类型">
          <a-select v-model:value="form.contractType" :options="contractTypeOptions" />
        </a-form-item>
        <a-form-item label="合同状态">
          <a-select v-model:value="form.contractStatus" :options="contractStatusOptions" />
        </a-form-item>
        <a-form-item label="合同开始">
          <a-date-picker v-model:value="form.contractStartDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="合同结束">
          <a-date-picker v-model:value="form.contractEndDate" style="width: 100%" />
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
        <a-form-item label="生日">
          <a-date-picker v-model:value="form.birthday" style="width: 100%" />
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
import { computed, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getHrStaffPage, getHrProfile, upsertHrProfile, terminateStaff, reinstateStaff } from '../../api/hr'
import { getRolePage } from '../../api/rbac'
import { openPrintTableReport } from '../../utils/print'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { useDepartmentOptions } from '../../composables/useDepartmentOptions'
import type { HrStaffProfile, PageResult, RoleItem } from '../../types'

const props = withDefaults(defineProps<{
  title?: string
  subTitle?: string
}>(), {
  title: '人力资源',
  subTitle: '护理员档案与岗位信息'
})

const pageTitle = props.title
const pageSubTitle = props.subTitle

const route = useRoute()
const router = useRouter()
const query = reactive({
  keyword: undefined as string | undefined,
  departmentId: undefined as number | undefined,
  roleId: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})
const rows = ref<HrStaffProfile[]>([])
const loading = ref(false)
const saving = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const { staffOptions, staffLoading, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 120 })
const { departmentOptions, searchDepartments } = useDepartmentOptions({ pageSize: 200, preloadSize: 500 })
const roles = ref<RoleItem[]>([])
const selectedRowKeys = ref<string[]>([])

const columns = [
  { title: '工号', dataIndex: 'staffNo', key: 'staffNo', width: 120 },
  { title: '姓名', dataIndex: 'realName', key: 'realName', width: 120 },
  { title: '生日', dataIndex: 'birthday', key: 'birthday', width: 120 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '岗位', dataIndex: 'jobTitle', key: 'jobTitle', width: 140 },
  { title: '用工类型', dataIndex: 'employmentType', key: 'employmentType', width: 120 },
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo', width: 160 },
  { title: '合同到期', dataIndex: 'contractEndDate', key: 'contractEndDate', width: 120 },
  { title: '离职日期', dataIndex: 'leaveDate', key: 'leaveDate', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 }
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
const contractTypeOptions = [
  { label: '固定期限', value: 'FIXED_TERM' },
  { label: '无固定期限', value: 'OPEN_ENDED' },
  { label: '实习协议', value: 'INTERNSHIP' },
  { label: '劳务协议', value: 'SERVICE' }
]
const contractStatusOptions = [
  { label: '待签署', value: 'PENDING' },
  { label: '已生效', value: 'ACTIVE' },
  { label: '续签处理中', value: 'RENEWAL_PENDING' },
  { label: '已到期', value: 'EXPIRED' },
  { label: '已终止', value: 'TERMINATED' }
]
const departmentFilterOptions = computed(() =>
  departmentOptions.value.map((item) => ({ label: item.label, value: Number(item.value) })).filter((item) => Number.isFinite(item.value))
)
const roleFilterOptions = computed(() => roles.value.map((item) => ({ label: `${item.roleName} (${item.roleCode})`, value: item.id })))

const drawerTitle = computed(() => (form.staffId ? '编辑档案' : '新增档案'))
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.staffId))))
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))
const canTerminateSingle = computed(() => !!selectedSingleRecord.value && Number(selectedSingleRecord.value.status) === 1)
const canReinstateSingle = computed(() => !!selectedSingleRecord.value && Number(selectedSingleRecord.value.status) !== 1)

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<HrStaffProfile> = await getHrStaffPage(query)
    rows.value = (res.list || []).map((item) => ({
      ...item,
      staffId: String(item.staffId)
    }))
    pagination.total = res.total || res.list.length
    selectedRowKeys.value = []
    if (!roles.value.length) {
      const roleRes: PageResult<RoleItem> = await getRolePage({ pageNo: 1, pageSize: 200 })
      roles.value = roleRes.list || []
    }
    if (!departmentOptions.value.length) {
      await searchDepartments('')
    }
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
  query.keyword = undefined
  query.departmentId = undefined
  query.roleId = undefined
  query.pageNo = 1
  pagination.current = 1
  router.replace({
    query: {
      ...route.query,
      departmentId: undefined,
      roleId: undefined
    }
  })
  fetchData()
}

function onStaffChange(val: string | number) {
  const selected = staffOptions.value.find((item) => String(item.value) === String(val))
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
    await searchStaff('')
  }
  if (record?.staffId && record?.realName) {
    ensureSelectedStaff(record.staffId, record.realName)
  }
  if (record?.staffId) {
    try {
      const profile = await getHrProfile(String(record.staffId))
      Object.assign(form, profile)
    } catch {
      // ignore
    }
  }
  if (form.hireDate && typeof form.hireDate === 'string') {
    form.hireDate = dayjs(form.hireDate)
  }
  if (form.contractStartDate && typeof form.contractStartDate === 'string') {
    form.contractStartDate = dayjs(form.contractStartDate)
  }
  if (form.contractEndDate && typeof form.contractEndDate === 'string') {
    form.contractEndDate = dayjs(form.contractEndDate)
  }
  if (form.leaveDate && typeof form.leaveDate === 'string') {
    form.leaveDate = dayjs(form.leaveDate)
  }
  if (form.birthday && typeof form.birthday === 'string') {
    form.birthday = dayjs(form.birthday)
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
    payload.hireDate = payload.hireDate && typeof payload.hireDate === 'object' && payload.hireDate.format
      ? payload.hireDate.format('YYYY-MM-DD')
      : null
    payload.contractStartDate = payload.contractStartDate && typeof payload.contractStartDate === 'object' && payload.contractStartDate.format
      ? payload.contractStartDate.format('YYYY-MM-DD')
      : null
    payload.contractEndDate = payload.contractEndDate && typeof payload.contractEndDate === 'object' && payload.contractEndDate.format
      ? payload.contractEndDate.format('YYYY-MM-DD')
      : null
    payload.leaveDate = payload.leaveDate && typeof payload.leaveDate === 'object' && payload.leaveDate.format
      ? payload.leaveDate.format('YYYY-MM-DD')
      : null
    payload.birthday = payload.birthday && typeof payload.birthday === 'object' && payload.birthday.format
      ? payload.birthday.format('YYYY-MM-DD')
      : null
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
const terminateForm = reactive<{ staffId?: string | number; leaveDate?: any; leaveReason?: string }>({})

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
    await terminateStaff({ ...params, staffId: String(params.staffId) })
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
    await reinstateStaff({ staffId: String(record.staffId) })
    message.success('已复职')
    fetchData()
  } catch {
    message.error('复职操作失败')
  }
}

function requireSingleSelection(action: string) {
  if (!selectedSingleRecord.value) {
    message.info(`请先勾选 1 条员工档案后再${action}`)
    return null
  }
  return selectedSingleRecord.value
}

async function editSelected() {
  const record = requireSingleSelection('编辑')
  if (!record) return
  await openDrawer(record)
}

async function viewSelected() {
  const record = requireSingleSelection('查看')
  if (!record) return
  await viewProfile(record)
}

function terminateSelected() {
  const record = requireSingleSelection('离职')
  if (!record) return
  if (Number(record.status) !== 1) {
    message.info('所选员工已是离职状态')
    return
  }
  openTerminate(record)
}

async function reinstateSelected() {
  const record = requireSingleSelection('复职')
  if (!record) return
  if (Number(record.status) === 1) {
    message.info('所选员工已是在职状态')
    return
  }
  await submitReinstate(record)
}

async function batchTerminate() {
  if (selectedRowKeys.value.length === 0) return
  const validRecords = selectedRecords.value.filter((item) => Number(item.status) === 1)
  if (validRecords.length === 0) {
    message.info('勾选项中没有在职员工，无需批量离职')
    return
  }
  try {
    await Promise.all(validRecords.map((item) => terminateStaff({ staffId: String(item.staffId) })))
    message.success(`批量离职完成，共处理 ${validRecords.length} 条`)
    fetchData()
  } catch {
    message.error('批量离职失败')
  }
}

async function batchReinstate() {
  if (selectedRowKeys.value.length === 0) return
  const validRecords = selectedRecords.value.filter((item) => Number(item.status) !== 1)
  if (validRecords.length === 0) {
    message.info('勾选项中没有离职员工，无需批量复职')
    return
  }
  try {
    await Promise.all(validRecords.map((item) => reinstateStaff({ staffId: String(item.staffId) })))
    message.success(`批量复职完成，共处理 ${validRecords.length} 条`)
    fetchData()
  } catch {
    message.error('批量复职失败')
  }
}

function printBirthdayLedger() {
  const candidates = rows.value
    .filter((item) => item.birthday)
    .sort((a, b) => String(a.birthday || '').localeCompare(String(b.birthday || '')))
  if (!candidates.length) {
    message.info('当前列表暂无生日数据可打印')
    return
  }
  openPrintTableReport({
    title: '职工生日台账',
    subtitle: `打印时间：${dayjs().format('YYYY-MM-DD HH:mm:ss')}`,
    columns: [
      { key: 'staffNo', title: '工号' },
      { key: 'realName', title: '姓名' },
      { key: 'birthday', title: '生日' },
      { key: 'phone', title: '手机号' },
      { key: 'jobTitle', title: '岗位' },
      { key: 'statusText', title: '状态' }
    ],
    rows: candidates.map((item) => ({
      staffNo: item.staffNo || '-',
      realName: item.realName || '-',
      birthday: item.birthday ? dayjs(item.birthday).format('YYYY-MM-DD') : '-',
      phone: item.phone || '-',
      jobTitle: item.jobTitle || '-',
      statusText: Number(item.status) === 1 ? '在职' : '离职'
    }))
  })
}

searchStaff('')
function syncQueryFromRoute() {
  const nextDepartmentId = route.query.departmentId == null || route.query.departmentId === '' ? undefined : Number(route.query.departmentId)
  const nextRoleId = route.query.roleId == null || route.query.roleId === '' ? undefined : Number(route.query.roleId)
  query.departmentId = Number.isFinite(nextDepartmentId as number) ? Number(nextDepartmentId) : undefined
  query.roleId = Number.isFinite(nextRoleId as number) ? Number(nextRoleId) : undefined
}

watch(
  () => [route.query.departmentId, route.query.roleId],
  () => {
    syncQueryFromRoute()
    query.pageNo = 1
    pagination.current = 1
    fetchData()
  }
)

syncQueryFromRoute()
fetchData()
</script>

<style scoped>
.selection-tip {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
