<template>
  <PageContainer :title="pageTitle" :subTitle="pageSubTitle">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="姓名/账号" />
      </a-form-item>
      <a-form-item label="部门">
        <a-select
          v-model:value="query.departmentId"
          allow-clear
          show-search
          :filter-option="false"
          :options="departmentSelectOptions"
          style="width: 200px"
          placeholder="选择部门"
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
          style="width: 220px"
          placeholder="选择角色"
        />
      </a-form-item>
      <template #extra>
        <a-button type="primary" v-permission="accountManagerRoles" @click="openDrawer()">新增员工</a-button>
        <a-button :type="showSupervisorAnomaliesOnly ? 'primary' : 'default'" @click="toggleSupervisorAnomalyView">
          监管异常 {{ supervisorAnomalies.length }}
        </a-button>
        <a-button @click="exportSupervisorAnomalies">导出异常</a-button>
      </template>
    </SearchForm>

    <a-alert
      v-if="showSupervisorAnomaliesOnly"
      type="warning"
      show-icon
      style="margin-bottom: 10px;"
      :message="`当前仅显示监管链异常员工（${displayRows.length}）`"
      description="规则：员工→本部门部长；部长→院长/SYS_ADMIN；间接领导→院长/SYS_ADMIN"
    />
    <a-alert
      v-if="String(route.query.autoOpen || '') === '1' && route.query.staffId"
      type="info"
      show-icon
      style="margin-bottom: 10px;"
      message="当前来自新增人员流程"
      description="这里继续补充分配账号、领导链和角色。保存后可返回档案中心继续处理。"
    />

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="displayRows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'default'">
            {{ record.status === 1 ? '启用' : '已锁定' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'departmentId'">
          <span>{{ departmentNameMap.get(String(record.departmentId || '')) || '-' }}</span>
        </template>
        <template v-else-if="column.key === 'directLeaderId'">
          <span>{{ staffNameMap.get(String(record.directLeaderId || '')) || '-' }}</span>
        </template>
        <template v-else-if="column.key === 'indirectLeaderId'">
          <span>{{ staffNameMap.get(String(record.indirectLeaderId || '')) || '-' }}</span>
        </template>
        <template v-else-if="column.key === 'supervisorIssue'">
          <a-tag v-if="supervisorAnomalyMap.get(String(record.id))" color="red">
            {{ supervisorAnomalyMap.get(String(record.id)) }}
          </a-tag>
          <a-tag v-else color="green">正常</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a v-permission="accountManagerRoles" @click="openDrawer(record)">编辑</a>
            <a
              v-if="supervisorAnomalyMap.get(String(record.id))"
              v-permission="accountManagerRoles"
              @click="openDrawerWithRecommendation(record)"
            >
              推荐修复
            </a>
            <a v-permission="credentialViewerRoles" @click="openCredential(record.id)">查看密码</a>
            <a v-permission="credentialViewerRoles" @click="openCredential(record.id, true)">打印账号密码</a>
            <a v-if="record.status === 1" v-permission="accountManagerRoles" @click="toggleStaffLock(record, true)">一键锁定</a>
            <a v-else v-permission="accountManagerRoles" @click="toggleStaffLock(record, false)">解除锁定</a>
            <a v-permission="accountManagerRoles" @click="openRole(record)">分配角色</a>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="drawerOpen" :title="drawerTitle" width="520">
      <a-form :model="form" layout="vertical">
        <div v-if="form.id" class="identity-shell">
          <div class="identity-head">
            <div>
              <div class="identity-title">人员主档</div>
              <div class="form-rule-tip">姓名、工号、部门、手机号统一在“员工基本信息”维护，账号页只处理账号、密码、角色与监管链。</div>
            </div>
            <a-button size="small" @click="goToProfileBasic">去基本信息</a-button>
          </div>
          <a-descriptions :column="2" size="small" bordered>
            <a-descriptions-item label="姓名">{{ form.realName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="职工号">{{ form.staffNo || '-' }}</a-descriptions-item>
            <a-descriptions-item label="部门">{{ departmentNameMap.get(String(form.departmentId || '')) || '-' }}</a-descriptions-item>
            <a-descriptions-item label="手机号">{{ form.phone || '-' }}</a-descriptions-item>
          </a-descriptions>
        </div>
        <template v-else>
          <a-form-item label="账号" required>
            <a-input v-model:value="form.username" :disabled="!form.id" placeholder="默认与职工号一致" />
            <div class="form-rule-tip">新建时默认使用自动生成的职工号作为登录账号。</div>
          </a-form-item>
          <a-form-item label="职工号（登录ID）" required>
            <a-input v-model:value="form.staffNo" :disabled="!!form.id" placeholder="保存前可自动生成" />
            <a-space style="margin-top: 8px">
              <a-button size="small" @click="regenerateStaffCredentials">重新生成职工号</a-button>
              <span class="form-rule-tip">默认初始密码：123456</span>
            </a-space>
          </a-form-item>
          <a-form-item label="姓名" required>
            <a-input v-model:value="form.realName" />
          </a-form-item>
        </template>
        <a-form-item :label="form.id ? '重置密码（可选）' : '初始密码'" :required="!form.id">
          <a-input-password v-model:value="form.password" :placeholder="form.id ? '留空则不修改密码' : '请输入初始密码'" />
          <div v-if="!form.id" class="form-rule-tip">新账号默认密码为 123456，可保存前修改。</div>
        </a-form-item>
        <a-form-item v-if="!form.id" label="部门" required>
          <a-select
            v-model:value="form.departmentId"
            allow-clear
            show-search
            :filter-option="false"
            :options="departmentSelectOptions"
            placeholder="输入部门名称/拼音首字母"
            @search="searchDepartments"
            @focus="() => !departmentOptions.length && searchDepartments('')"
          />
        </a-form-item>
        <a-form-item label="直接领导（一级监管）">
          <div class="form-rule-tip">{{ directLeaderRuleHint }}</div>
          <a-select
            v-model:value="form.directLeaderId"
            allow-clear
            show-search
            :filter-option="false"
            :options="directLeaderSelectOptions"
            :loading="staffLoading"
            placeholder="输入姓名/工号/拼音首字母"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
          />
        </a-form-item>
        <a-form-item label="间接领导（二级监管）">
          <div class="form-rule-tip">{{ indirectLeaderRuleHint }}</div>
          <a-select
            v-model:value="form.indirectLeaderId"
            allow-clear
            show-search
            :filter-option="false"
            :options="indirectLeaderSelectOptions"
            :loading="staffLoading"
            placeholder="输入姓名/工号/拼音首字母"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
          />
        </a-form-item>
        <a-form-item label="快速修复">
          <a-space wrap>
            <a-button size="small" @click="applySupervisorRecommendation">一键推荐领导</a-button>
            <span class="form-rule-tip">优先按角色的上级领导角色推荐；未配置时，自动回退为“员工→本部门部长，部长→院长层级”。</span>
          </a-space>
        </a-form-item>
        <a-alert
          type="info"
          show-icon
          style="margin-bottom: 12px"
          :message="leaderStrategyTitle"
          :description="leaderStrategyDescription"
        />
        <a-form-item v-if="!form.id" label="手机号">
          <a-input v-model:value="form.phone" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button v-if="!form.id" :loading="saving" @click="submit(true)">保存并打印账号密码</a-button>
          <a-button type="primary" :loading="saving" @click="submit">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-modal v-model:open="roleOpen" title="分配角色" @ok="submitRole">
      <a-form layout="vertical">
        <a-form-item label="角色" required>
          <a-select v-model:value="roleForm.roleIds" mode="multiple" :options="roleFilterOptions" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="credentialOpen" title="账号密码" width="560px" :footer="null">
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="姓名">{{ credentialForm.realName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="职工号">{{ credentialForm.staffNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="登录账号">{{ credentialForm.username || '-' }}</a-descriptions-item>
        <a-descriptions-item label="当前密码">
          <span v-if="credentialForm.passwordPlaintextSnapshot">{{ credentialForm.passwordPlaintextSnapshot }}</span>
          <span v-else class="credential-empty">暂无可查看密码快照，请先重置密码后再查看。</span>
        </a-descriptions-item>
        <a-descriptions-item label="最近更新时间">
          {{ credentialForm.passwordSnapshotUpdatedAt ? dayjs(credentialForm.passwordSnapshotUpdatedAt).format('YYYY-MM-DD HH:mm:ss') : '-' }}
        </a-descriptions-item>
      </a-descriptions>
      <div class="credential-actions">
        <a-space>
          <a-button @click="credentialOpen = false">关闭</a-button>
          <a-button type="primary" :disabled="!credentialForm.passwordPlaintextSnapshot" @click="printCredentialCard">打印账号密码</a-button>
        </a-space>
      </div>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../components/PageContainer.vue'
import SearchForm from '../components/SearchForm.vue'
import DataTable from '../components/DataTable.vue'
import { getStaff, getStaffCredentials, getStaffPage, createStaff, updateStaff, updateStaffRoles, getStaffSupervisorAnomalies, lockStaff, unlockStaff } from '../api/staff'
import { getRolePage } from '../api/role'
import { getStaffRoleAssignments } from '../api/rbac'
import { useDepartmentOptions } from '../composables/useDepartmentOptions'
import { useStaffOptions } from '../composables/useStaffOptions'
import { useLiveSyncRefresh } from '../composables/useLiveSyncRefresh'
import { openPrintTableReport } from '../utils/print'
import { canBeDirectLeader, canBeIndirectLeader, ensureSupervisorOrder, mergeRoleCodes } from '../utils/supervisor'
import type { Id, StaffCredentialItem, StaffItem, RoleItem, PageResult } from '../types'

const props = withDefaults(defineProps<{ title?: string; subTitle?: string }>(), {
  title: '员工管理',
  subTitle: '账号与角色配置'
})

const accountManagerRoles = ['HR_EMPLOYEE', 'HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']
const credentialViewerRoles = ['HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN']

const query = reactive({
  keyword: undefined as string | undefined,
  departmentId: undefined as number | undefined,
  roleId: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})
const rows = ref<StaffItem[]>([])
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const showSupervisorAnomaliesOnly = ref(false)
const supervisorAnomalies = ref<Array<{
  staffId: Id
  staffNo?: string
  staffName?: string
  departmentId?: number
  directLeaderId?: Id
  indirectLeaderId?: Id
  issue: string
}>>([])

const columns = [
  { title: '职工号', dataIndex: 'staffNo', key: 'staffNo', width: 130 },
  { title: '账号', dataIndex: 'username', key: 'username', width: 120 },
  { title: '姓名', dataIndex: 'realName', key: 'realName', width: 120 },
  { title: '部门', dataIndex: 'departmentId', key: 'departmentId', width: 140 },
  { title: '直接领导', dataIndex: 'directLeaderId', key: 'directLeaderId', width: 120 },
  { title: '间接领导', dataIndex: 'indirectLeaderId', key: 'indirectLeaderId', width: 120 },
  { title: '监管状态', key: 'supervisorIssue', width: 200 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 260 }
]

const drawerOpen = ref(false)
const form = reactive<Partial<StaffItem>>({})
const DEFAULT_INITIAL_PASSWORD = '123456'
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '已锁定', value: 0 }
]

const roleOpen = ref(false)
const roleForm = reactive<{ staffId?: Id; roleIds: number[] }>({ roleIds: [] })
const credentialOpen = ref(false)
const credentialForm = reactive<Partial<StaffCredentialItem>>({})
const { departmentOptions, searchDepartments, ensureSelectedDepartment } = useDepartmentOptions({ pageSize: 260, preloadSize: 600 })
const { staffOptions, staffLoading, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 220, preloadSize: 600 })

const roles = ref<RoleItem[]>([])
const pageTitle = computed(() => props.title || '员工管理')
const pageSubTitle = computed(() => props.subTitle || '账号与角色配置')

const roleFilterOptions = computed(() => roles.value.map((r) => ({ label: r.roleName, value: r.id })))
const departmentNameMap = computed(() => new Map(departmentOptions.value.map((item) => [String(item.value), item.name])))
const staffNameMap = computed(() => {
  const map = new Map<string, string>()
  staffOptions.value.forEach((item) => {
    map.set(String(item.value), item.name)
  })
  rows.value.forEach((item) => {
    if (item.id != null && item.realName) {
      map.set(String(item.id), String(item.realName))
    }
  })
  return map
})
const staffRoleCodeMap = computed(() => {
  const map = new Map<string, string[]>()
  staffOptions.value.forEach((item) => {
    map.set(String(item.value), (item.roleCodes || []).map((code) => String(code || '').toUpperCase()))
  })
  rows.value.forEach((item) => {
    if (item.id == null) return
    const key = String(item.id)
    map.set(key, mergeRoleCodes({ roleCodes: map.get(key) }, item))
  })
  return map
})
const departmentSelectOptions = computed(() =>
  departmentOptions.value
    .map((item) => ({
      label: item.label,
      value: Number(item.value)
    }))
    .filter((item) => Number.isFinite(item.value))
)
const staffSelectOptions = computed(() =>
  staffOptions.value
    .filter((item) => String(item.value) !== String(form.id || ''))
    .map((item) => ({
      label: item.label,
      value: String(item.value),
      departmentId: item.departmentId
    }))
)
const formRoleCodes = computed(() => {
  if (Array.isArray(form.roleCodes) && form.roleCodes.length) {
    return form.roleCodes.map((code) => String(code || '').toUpperCase()).filter(Boolean)
  }
  if (form.id != null) {
    return staffRoleCodeMap.value.get(String(form.id)) || []
  }
  return []
})
const roleByCodeMap = computed(() => {
  const map = new Map<string, RoleItem>()
  roles.value.forEach((item) => {
    const code = String(item.roleCode || '').trim().toUpperCase()
    if (code) map.set(code, item)
  })
  return map
})
const roleByIdMap = computed(() => {
  const map = new Map<string, RoleItem>()
  roles.value.forEach((item) => {
    if (item.id != null) map.set(String(item.id), item)
  })
  return map
})
const formRoleRecords = computed(() =>
  formRoleCodes.value
    .map((code) => roleByCodeMap.value.get(String(code || '').trim().toUpperCase()))
    .filter((item): item is RoleItem => Boolean(item))
)
function resolveSuperiorRoleCodes(level = 1) {
  const result = new Set<string>()
  formRoleRecords.value.forEach((role) => {
    let current = role
    let depth = 0
    while (current?.superiorRoleId != null && depth < level) {
      const next = roleByIdMap.value.get(String(current.superiorRoleId))
      if (!next) break
      current = next
      depth += 1
    }
    if (depth === level) {
      const code = String(current.roleCode || '').trim().toUpperCase()
      if (code) result.add(code)
    }
  })
  return Array.from(result)
}
const expectedDirectLeaderRoleCodes = computed(() => resolveSuperiorRoleCodes(1))
const expectedIndirectLeaderRoleCodes = computed(() => resolveSuperiorRoleCodes(2))
const expectedDirectLeaderRoleNames = computed(() =>
  expectedDirectLeaderRoleCodes.value
    .map((code) => roleByCodeMap.value.get(code)?.roleName)
    .filter(Boolean)
)
const expectedIndirectLeaderRoleNames = computed(() =>
  expectedIndirectLeaderRoleCodes.value
    .map((code) => roleByCodeMap.value.get(code)?.roleName)
    .filter(Boolean)
)
function candidateMatchScore(candidateId: string | number, expectedRoleCodes: string[]) {
  if (!expectedRoleCodes.length) return 0
  const codes = new Set((staffRoleCodeMap.value.get(String(candidateId)) || []).map((item) => String(item || '').toUpperCase()))
  return expectedRoleCodes.some((code) => codes.has(String(code || '').toUpperCase())) ? 1 : 0
}
function sortLeaderOptions<T extends { value: string | number; label: string }>(options: T[], expectedRoleCodes: string[]) {
  return [...options].sort((a, b) => {
    const scoreB = candidateMatchScore(b.value, expectedRoleCodes)
    const scoreA = candidateMatchScore(a.value, expectedRoleCodes)
    if (scoreB !== scoreA) return scoreB - scoreA
    return String(a.label || '').localeCompare(String(b.label || ''), 'zh-CN')
  })
}
const directLeaderSelectOptions = computed(() => {
  const target = { id: form.id, departmentId: form.departmentId, roleCodes: formRoleCodes.value }
  const available = staffSelectOptions.value.filter((candidate) => {
    const roleCodes = staffRoleCodeMap.value.get(String(candidate.value)) || []
    return canBeDirectLeader(target, { id: candidate.value, departmentId: candidate.departmentId, roleCodes })
  })
  return sortLeaderOptions(available, expectedDirectLeaderRoleCodes.value)
})
const indirectLeaderSelectOptions = computed(() => {
  const target = { id: form.id, departmentId: form.departmentId, roleCodes: formRoleCodes.value }
  const available = staffSelectOptions.value.filter((candidate) => {
    const roleCodes = staffRoleCodeMap.value.get(String(candidate.value)) || []
    return canBeIndirectLeader(target, { id: candidate.value, departmentId: candidate.departmentId, roleCodes })
  })
  return sortLeaderOptions(available, expectedIndirectLeaderRoleCodes.value)
})
const recommendedDirectLeaderId = computed(() => {
  if (!directLeaderSelectOptions.value.length) return undefined
  const preferred = directLeaderSelectOptions.value.find((item) => candidateMatchScore(item.value, expectedDirectLeaderRoleCodes.value) > 0)
  return String((preferred || directLeaderSelectOptions.value[0]).value)
})
const recommendedIndirectLeaderId = computed(() => {
  if (!indirectLeaderSelectOptions.value.length) return undefined
  const direct = String(form.directLeaderId || recommendedDirectLeaderId.value || '')
  const preferred = indirectLeaderSelectOptions.value.find((item) =>
    String(item.value) !== direct && candidateMatchScore(item.value, expectedIndirectLeaderRoleCodes.value) > 0
  )
    || indirectLeaderSelectOptions.value.find((item) => String(item.value) !== direct)
  return String((preferred || indirectLeaderSelectOptions.value[0]).value)
})
const directLeaderRuleHint = computed(() => {
  if (expectedDirectLeaderRoleNames.value.length) {
    return `规则：优先由角色上级自动推荐，当前推荐角色：${expectedDirectLeaderRoleNames.value.join(' / ')}`
  }
  if (formRoleCodes.value.some((code) => String(code).endsWith('_EMPLOYEE'))) {
    return '规则：员工 → 直接领导仅可选本部门部长'
  }
  if (formRoleCodes.value.some((code) => String(code).endsWith('_MINISTER'))) {
    return '规则：部长 → 直接领导仅可选院长/DIRECTOR/SYS_ADMIN'
  }
  return '规则：未知层级时可选同部门部长或院长层级'
})
const indirectLeaderRuleHint = computed(() => {
  if (expectedIndirectLeaderRoleNames.value.length) {
    return `规则：优先由角色上级链自动推荐，当前推荐角色：${expectedIndirectLeaderRoleNames.value.join(' / ')}`
  }
  return '规则：间接领导仅可选院长或院长层级管理者'
})
const leaderStrategyTitle = computed(() => {
  if (expectedDirectLeaderRoleNames.value.length || expectedIndirectLeaderRoleNames.value.length) {
    return '当前账号已匹配角色领导关系'
  }
  return '当前账号未配置完整角色领导关系'
})
const leaderStrategyDescription = computed(() => {
  const parts: string[] = []
  if (expectedDirectLeaderRoleNames.value.length) {
    parts.push(`直接领导建议：${expectedDirectLeaderRoleNames.value.join(' / ')}`)
  }
  if (expectedIndirectLeaderRoleNames.value.length) {
    parts.push(`间接领导建议：${expectedIndirectLeaderRoleNames.value.join(' / ')}`)
  }
  if (parts.length) {
    return `${parts.join('；')}。若员工有特殊汇报关系，再手动微调个人直接领导和间接领导。`
  }
  return '建议做法：先在角色管理里给角色设置“上级领导角色”，再回到这里一键推荐。员工个人层面的直接领导、间接领导只用于落地到具体负责人。'
})
const drawerTitle = computed(() => (form.id ? '编辑员工' : '新增员工'))
const supervisorAnomalyMap = computed(() => {
  const map = new Map<string, string>()
  supervisorAnomalies.value.forEach((item) => {
    map.set(String(item.staffId), item.issue)
  })
  return map
})
const displayRows = computed(() => {
  if (!showSupervisorAnomaliesOnly.value) return rows.value
  return rows.value.filter((item) => supervisorAnomalyMap.value.has(String(item.id)))
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<StaffItem> = await getStaffPage(query)
    rows.value = res.list
    pagination.total = res.total || res.list.length
    const roleRes: PageResult<RoleItem> = await getRolePage({ pageNo: 1, pageSize: 200 })
    roles.value = roleRes.list
    if (!departmentOptions.value.length) await searchDepartments('')
    if (!staffOptions.value.length) await searchStaff('')
    rows.value.forEach((item) => {
      if (item.directLeaderId != null) ensureSelectedStaff(item.directLeaderId)
      if (item.indirectLeaderId != null) ensureSelectedStaff(item.indirectLeaderId)
      if (item.departmentId != null) ensureSelectedDepartment(item.departmentId)
    })
    supervisorAnomalies.value = await getStaffSupervisorAnomalies()
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

async function ensureRolesLoaded() {
  if (roles.value.length > 0) {
    return
  }
  const roleRes: PageResult<RoleItem> = await getRolePage({ pageNo: 1, pageSize: 200 })
  roles.value = roleRes.list || []
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

function toggleSupervisorAnomalyView() {
  showSupervisorAnomaliesOnly.value = !showSupervisorAnomaliesOnly.value
  router.replace({
    query: {
      ...route.query,
      view: showSupervisorAnomaliesOnly.value ? 'supervisor-anomalies' : undefined
    }
  })
}

function exportSupervisorAnomalies() {
  if (!supervisorAnomalies.value.length) {
    message.info('当前无监管异常可导出')
    return
  }
  const header = ['员工ID', '职工号', '姓名', '部门ID', '直接领导ID', '间接领导ID', '异常原因']
  const rows = supervisorAnomalies.value.map((item) => [
    String(item.staffId || ''),
    String(item.staffNo || ''),
    String(item.staffName || ''),
    String(item.departmentId || ''),
    String(item.directLeaderId || ''),
    String(item.indirectLeaderId || ''),
    String(item.issue || '')
  ])
  const csv = [header, ...rows]
    .map((cols) => cols.map((cell) => `"${String(cell || '').replace(/"/g, '""')}"`).join(','))
    .join('\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '监管链异常清单.csv'
  a.click()
  URL.revokeObjectURL(url)
}

function openDrawer(record?: StaffItem) {
  Object.keys(form).forEach((key) => {
    ;(form as any)[key] = undefined
  })
  Object.assign(form, record || { status: 1 })
  if (!record) {
    regenerateStaffCredentials()
    form.password = DEFAULT_INITIAL_PASSWORD
  }
  ensureSelectedDepartment(form.departmentId)
  if (form.directLeaderId != null) ensureSelectedStaff(form.directLeaderId)
  if (form.indirectLeaderId != null) ensureSelectedStaff(form.indirectLeaderId)
  if (!staffOptions.value.length) {
    searchStaff('').catch(() => {})
  }
  drawerOpen.value = true
}

function goToProfileBasic() {
  if (!form.id) return
  router.push({
    path: '/hr/profile/basic',
    query: {
      staffId: String(form.id),
      autoOpen: '1'
    }
  })
}

function generateStaffNo() {
  return `YG${dayjs().format('YYMMDDHHmmssSSS')}`
}

function regenerateStaffCredentials() {
  if (form.id) return
  form.staffNo = generateStaffNo()
  form.username = form.staffNo
  if (!String(form.password || '').trim()) {
    form.password = DEFAULT_INITIAL_PASSWORD
  }
}

function consumeAutoOpenQuery() {
  if (String(route.query.autoOpen || '') !== '1') return
  router.replace({
    query: {
      ...route.query,
      autoOpen: undefined,
      from: undefined
    }
  })
}

function applySupervisorRecommendation() {
  if (!form.id && !form.departmentId) {
    message.info('请先选择部门后再推荐领导')
    return
  }
  if (!form.directLeaderId && recommendedDirectLeaderId.value) {
    form.directLeaderId = recommendedDirectLeaderId.value
  }
  if (!form.indirectLeaderId && recommendedIndirectLeaderId.value) {
    form.indirectLeaderId = recommendedIndirectLeaderId.value
  }
  if (!form.directLeaderId && !form.indirectLeaderId) {
    message.info('当前规则下暂无可推荐领导，请先确认角色与部门')
    return
  }
  message.success('已按规则填充推荐领导，可直接保存')
}

function openDrawerWithRecommendation(record: StaffItem) {
  openDrawer(record)
  applySupervisorRecommendation()
}

async function submit(printAfterSave = false) {
  const username = String(form.username || '').trim()
  const realName = String(form.realName || '').trim()
  const staffNo = String(form.staffNo || '').trim()
  if (!username || !realName || !staffNo) {
    message.warning('账号、姓名、职工号为必填项')
    return
  }
  if (!form.departmentId) {
    message.warning('部门为必填项，请先选择部门')
    return
  }
  if (!form.id && String(form.password || '').trim().length < 6) {
    message.warning('新增员工需设置至少6位初始密码')
    return
  }
  if (!ensureSupervisorOrder(form.directLeaderId, form.indirectLeaderId)) {
    message.warning('直接领导与间接领导不能为同一人')
    return
  }
  if (form.id && String(form.directLeaderId || '') === String(form.id)) {
    message.warning('直接领导不能设置为本人')
    return
  }
  if (form.id && String(form.indirectLeaderId || '') === String(form.id)) {
    message.warning('间接领导不能设置为本人')
    return
  }
  if (form.directLeaderId) {
    const directRoleCodes = staffRoleCodeMap.value.get(String(form.directLeaderId)) || []
    if (!canBeDirectLeader(
      { id: form.id, departmentId: form.departmentId, roleCodes: formRoleCodes.value },
      { id: form.directLeaderId, departmentId: staffOptions.value.find((item) => String(item.value) === String(form.directLeaderId))?.departmentId, roleCodes: directRoleCodes }
    )) {
      message.warning('当前组织规则下，直接领导应为本部门部长（员工）或院长/SYS_ADMIN（部长）')
      return
    }
  }
  if (form.indirectLeaderId) {
    const indirectRoleCodes = staffRoleCodeMap.value.get(String(form.indirectLeaderId)) || []
    if (!canBeIndirectLeader(
      { id: form.id, departmentId: form.departmentId, roleCodes: formRoleCodes.value },
      { id: form.indirectLeaderId, departmentId: staffOptions.value.find((item) => String(item.value) === String(form.indirectLeaderId))?.departmentId, roleCodes: indirectRoleCodes }
    )) {
      message.warning('当前组织规则下，间接领导需为院长或SYS_ADMIN')
      return
    }
  }
  saving.value = true
  try {
    const payload: Record<string, any> = {
      ...form,
      username,
      realName,
      staffNo,
      directLeaderId: form.directLeaderId ? String(form.directLeaderId) : undefined,
      indirectLeaderId: form.indirectLeaderId ? String(form.indirectLeaderId) : undefined
    }
    if (!payload.password) delete payload.password
    let saved: StaffItem
    if (form.id) {
      saved = await updateStaff(form.id, payload)
    } else {
      saved = await createStaff(payload)
    }
    message.success('保存成功')
    drawerOpen.value = false
    await fetchData()
    if (printAfterSave && saved?.id) {
      await openCredential(saved.id, true)
    }
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleStaffLock(record: StaffItem, lock: boolean) {
  try {
    if (lock) {
      await lockStaff(record.id)
      message.success('账号已锁定')
    } else {
      await unlockStaff(record.id)
      message.success('账号已解除锁定')
    }
    fetchData()
  } catch {
    message.error(lock ? '锁定失败' : '解锁失败')
  }
}

async function openCredential(staffId?: Id, autoPrint = false) {
  if (!staffId) return
  try {
    const detail = await getStaffCredentials(staffId)
    Object.keys(credentialForm).forEach((key) => {
      ;(credentialForm as any)[key] = undefined
    })
    Object.assign(credentialForm, detail || {})
    credentialOpen.value = true
    if (autoPrint) {
      printCredentialCard()
    }
  } catch {
    message.error('获取账号密码失败')
  }
}

function printCredentialCard() {
  if (!credentialForm.username || !credentialForm.passwordPlaintextSnapshot) {
    message.warning('当前没有可打印的账号密码')
    return
  }
  openPrintTableReport({
    title: '员工账号密码单',
    subtitle: `打印时间：${dayjs().format('YYYY-MM-DD HH:mm:ss')}`,
    columns: [
      { key: 'realName', title: '姓名' },
      { key: 'staffNo', title: '职工号' },
      { key: 'username', title: '登录账号' },
      { key: 'password', title: '当前密码' },
      { key: 'updatedAt', title: '密码更新时间' }
    ],
    rows: [{
      realName: credentialForm.realName || '-',
      staffNo: credentialForm.staffNo || '-',
      username: credentialForm.username || '-',
      password: credentialForm.passwordPlaintextSnapshot || '-',
      updatedAt: credentialForm.passwordSnapshotUpdatedAt ? dayjs(credentialForm.passwordSnapshotUpdatedAt).format('YYYY-MM-DD HH:mm:ss') : '-'
    }]
  })
}

async function maybeAutoOpenFromRoute() {
  const staffId = String(route.query.staffId || '').trim()
  if (String(route.query.autoOpen || '') !== '1' || !staffId) return
  try {
    const detail = await getStaff(staffId)
    openDrawer(detail)
  } catch {
    // ignore
  } finally {
    consumeAutoOpenQuery()
  }
}

async function openRole(record: StaffItem) {
  await ensureRolesLoaded()
  roleForm.staffId = record.id
  roleForm.roleIds = []
  roleOpen.value = true
  try {
    const rows = await getStaffRoleAssignments(record.id)
    roleForm.roleIds = (rows || []).map((item) => Number(item.roleId)).filter((item) => Number.isFinite(item))
  } catch {
    roleForm.roleIds = []
  }
}

async function submitRole() {
  if (!roleForm.staffId) return
  try {
    await updateStaffRoles(roleForm.staffId, roleForm.roleIds)
    message.success('角色已更新')
    roleOpen.value = false
  } catch {
    message.error('更新失败')
  }
}

if (String(route.query.view || '') === 'supervisor-anomalies') {
  showSupervisorAnomaliesOnly.value = true
}

function syncQueryFromRoute() {
  const nextDepartmentId = route.query.departmentId == null || route.query.departmentId === ''
    ? undefined
    : Number(route.query.departmentId)
  const nextRoleId = route.query.roleId == null || route.query.roleId === ''
    ? undefined
    : Number(route.query.roleId)
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

watch(
  () => [route.query.staffId, route.query.autoOpen],
  () => {
    maybeAutoOpenFromRoute().catch(() => {})
  }
)

syncQueryFromRoute()
fetchData()
maybeAutoOpenFromRoute().catch(() => {})

useLiveSyncRefresh({
  topics: ['system', 'hr', 'oa'],
  refresh: () => {
    if (loading.value || saving.value) return
    fetchData().catch(() => {})
  },
  debounceMs: 900
})
</script>

<style scoped>
.form-rule-tip {
  margin-bottom: 6px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.identity-shell {
  margin-bottom: 16px;
  padding: 12px;
  border-radius: 12px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
}

.identity-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.identity-title {
  font-size: 14px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.88);
}

.credential-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.credential-empty {
  color: rgba(0, 0, 0, 0.45);
}
</style>
