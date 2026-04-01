<template>
  <PageContainer title="官网配置中心" subTitle="原系统管理中的机构介绍、机构动态、生活娱乐、留言记录已统一下沉到这里">
    <a-alert
      type="info"
      show-icon
      style="margin-bottom: 12px"
      message="建议只保留一个运营配置入口"
      description="企业首页、官网公示、社区动态、居民活动、预约留言记录都在这里集中维护。旧系统管理入口会自动跳转到本页。"
    />

    <a-row :gutter="[12, 12]" style="margin-bottom: 12px">
      <a-col :xs="24" :md="8">
        <a-card size="small">
          <div class="summary-label">最近更新</div>
          <div class="summary-value">{{ profile.publishMeta?.lastUpdated || '-' }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card size="small">
          <div class="summary-label">维护人</div>
          <div class="summary-value">{{ pageMaintainer }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card size="small">
          <div class="summary-label">预约留言数</div>
          <div class="summary-value">{{ consultRecords.length }}</div>
        </a-card>
      </a-col>
    </a-row>

    <a-card :bordered="false" class="card-elevated">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="profile" tab="官网内容配置">
          <a-space wrap style="margin-bottom: 12px">
            <a-tag color="blue">机构介绍</a-tag>
            <a-tag color="cyan">机构动态</a-tag>
            <a-tag color="green">生活娱乐</a-tag>
            <a-tag color="purple">资质公示</a-tag>
            <a-tag color="gold">联系方式</a-tag>
          </a-space>
          <a-textarea v-model:value="profileConfigDraft" :rows="24" />
          <div class="config-actions">
            <a-button size="small" @click="formatProfileConfigDraft">JSON格式化</a-button>
            <a-button size="small" @click="reloadProfileConfigDraft">从当前配置重载</a-button>
            <a-button size="small" @click="importProfileConfigFromFile">导入JSON文件</a-button>
            <a-button size="small" @click="exportProfileConfig">导出JSON</a-button>
            <a-button size="small" danger @click="resetProfileConfig">恢复默认配置</a-button>
            <a-button type="primary" :loading="profileConfigSaving" @click="saveProfileConfig">保存并生效</a-button>
          </div>
        </a-tab-pane>

        <a-tab-pane key="consult" tab="预约留言记录">
          <div class="consult-actions">
            <a-tag color="blue">总记录 {{ consultRecords.length }}</a-tag>
            <a-tag color="green">今日新增 {{ consultTodayCount }}</a-tag>
            <a-tag color="purple">热门部门 {{ consultTopDepartment }}</a-tag>
            <a-button size="small" @click="exportConsultRecords">导出JSON</a-button>
            <a-button size="small" @click="exportConsultRecordsCsv">导出CSV</a-button>
            <a-button size="small" danger @click="clearConsultRecords">清空全部</a-button>
          </div>
          <a-table :data-source="consultRecords" :pagination="{ pageSize: 8 }" row-key="createdAt" size="small">
            <a-table-column title="提交时间" data-index="createdAt" key="createdAt" width="170" />
            <a-table-column title="联系人" data-index="name" key="name" width="120" />
            <a-table-column title="联系电话" data-index="phone" key="phone" width="140" />
            <a-table-column title="对接部门" data-index="department" key="department" width="140" />
            <a-table-column title="意向日期" data-index="visitDate" key="visitDate" width="130" />
            <a-table-column title="需求" data-index="need" key="need" />
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { enterpriseProfile, type EnterpriseProfile } from '../../constants/enterpriseProfile'
import { useUserStore } from '../../stores/user'

const PROFILE_OVERRIDE_STORAGE_KEY = 'enterprise_profile_override_v1'
const CONSULT_RECORD_STORAGE_KEY = 'enterprise_consult_records'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('profile')
const profileConfigSaving = ref(false)
const profileConfigDraft = ref('')
const profile = reactive<EnterpriseProfile>(buildRuntimeProfile())
const consultVersion = ref(0)

function deepClone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value))
}

function isPlainObject(value: unknown): value is Record<string, any> {
  return Object.prototype.toString.call(value) === '[object Object]'
}

function mergeProfile(base: any, patch: any): any {
  if (!isPlainObject(base) || !isPlainObject(patch)) {
    return patch == null ? base : patch
  }
  const merged: Record<string, any> = { ...base }
  Object.keys(patch).forEach((key) => {
    const baseValue = merged[key]
    const patchValue = patch[key]
    if (Array.isArray(patchValue)) {
      merged[key] = patchValue
      return
    }
    if (isPlainObject(baseValue) && isPlainObject(patchValue)) {
      merged[key] = mergeProfile(baseValue, patchValue)
      return
    }
    merged[key] = patchValue
  })
  return merged
}

function loadProfileOverride(): Partial<EnterpriseProfile> {
  const raw = localStorage.getItem(PROFILE_OVERRIDE_STORAGE_KEY)
  if (!raw) return {}
  try {
    const parsed = JSON.parse(raw)
    return isPlainObject(parsed) ? parsed : {}
  } catch {
    return {}
  }
}

function buildRuntimeProfile(): EnterpriseProfile {
  return mergeProfile(deepClone(enterpriseProfile), loadProfileOverride())
}

function replaceProfile(nextProfile: EnterpriseProfile) {
  Object.keys(profile).forEach((key) => {
    delete (profile as Record<string, unknown>)[key]
  })
  Object.assign(profile, nextProfile)
}

function buildEditablePayload(source: EnterpriseProfile) {
  return deepClone(source)
}

const pageMaintainer = computed(() => {
  const realName = String(userStore.staffInfo?.realName || '').trim()
  if (realName) return realName
  const username = String(userStore.staffInfo?.username || '').trim()
  if (username) return username
  return String(profile.publishMeta?.maintainer || '运营管理员')
})

const consultRecords = computed(() => {
  consultVersion.value
  const raw = localStorage.getItem(CONSULT_RECORD_STORAGE_KEY)
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
})

const consultTodayCount = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  return consultRecords.value.filter((item) => String(item.createdAt || '').startsWith(today)).length
})

const consultTopDepartment = computed(() => {
  const counter = new Map<string, number>()
  consultRecords.value.forEach((item) => {
    const key = String(item.department || '未指定')
    counter.set(key, (counter.get(key) || 0) + 1)
  })
  const top = Array.from(counter.entries()).sort((a, b) => b[1] - a[1])[0]
  return top ? `${top[0]}（${top[1]}）` : '暂无'
})

function reloadProfileConfigDraft() {
  profileConfigDraft.value = JSON.stringify(buildEditablePayload(profile), null, 2)
}

function formatProfileConfigDraft() {
  try {
    profileConfigDraft.value = JSON.stringify(JSON.parse(profileConfigDraft.value || '{}'), null, 2)
  } catch {
    message.warning('JSON 格式不正确，无法格式化')
  }
}

function saveProfileConfig() {
  profileConfigSaving.value = true
  try {
    const parsed = JSON.parse(profileConfigDraft.value || '{}')
    if (!isPlainObject(parsed)) {
      message.warning('配置内容必须是 JSON 对象')
      return
    }
    const publishMetaPatch = isPlainObject(parsed.publishMeta) ? parsed.publishMeta : {}
    publishMetaPatch.maintainer = pageMaintainer.value
    parsed.publishMeta = publishMetaPatch
    localStorage.setItem(PROFILE_OVERRIDE_STORAGE_KEY, JSON.stringify(parsed))
    replaceProfile(mergeProfile(deepClone(enterpriseProfile), parsed) as EnterpriseProfile)
    reloadProfileConfigDraft()
    message.success('官网配置已保存并生效')
  } catch {
    message.error('保存失败，请检查 JSON 结构')
  } finally {
    profileConfigSaving.value = false
  }
}

function exportProfileConfig() {
  const blob = new Blob([JSON.stringify(buildEditablePayload(profile), null, 2)], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `enterprise-site-config-${dayjs().format('YYYYMMDD-HHmmss')}.json`
  a.click()
  URL.revokeObjectURL(url)
}

function importProfileConfigFromFile() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'application/json,.json'
  input.onchange = () => {
    const file = input.files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = () => {
      try {
        profileConfigDraft.value = JSON.stringify(JSON.parse(String(reader.result || '{}')), null, 2)
        message.success('配置文件已导入，请确认后保存')
      } catch {
        message.error('导入失败：文件不是有效 JSON')
      }
    }
    reader.readAsText(file, 'utf-8')
  }
  input.click()
}

function resetProfileConfig() {
  localStorage.removeItem(PROFILE_OVERRIDE_STORAGE_KEY)
  replaceProfile(deepClone(enterpriseProfile))
  reloadProfileConfigDraft()
  message.success('已恢复默认官网配置')
}

function exportConsultRecords() {
  const blob = new Blob([JSON.stringify(consultRecords.value, null, 2)], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `enterprise-consult-records-${dayjs().format('YYYYMMDD-HHmmss')}.json`
  a.click()
  URL.revokeObjectURL(url)
}

function exportConsultRecordsCsv() {
  const header = ['提交时间', '联系人', '联系电话', '对接部门', '意向日期', '需求']
  const rows = consultRecords.value.map((item) => [
    item.createdAt || '',
    item.name || '',
    item.phone || '',
    item.department || '',
    item.visitDate || '',
    item.need || ''
  ])
  const csv = [header, ...rows]
    .map((cols) => cols.map((cell) => `"${String(cell || '').replace(/"/g, '""')}"`).join(','))
    .join('\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `enterprise-consult-records-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

function clearConsultRecords() {
  localStorage.removeItem(CONSULT_RECORD_STORAGE_KEY)
  consultVersion.value += 1
  message.success('预约留言记录已清空')
}

function syncTabFromRoute() {
  const tab = String(route.query.tab || 'profile')
  activeTab.value = tab === 'consult' ? 'consult' : 'profile'
}

watch(activeTab, (value) => {
  if (String(route.query.tab || 'profile') === value) return
  router.replace({ query: { ...route.query, tab: value } })
})

watch(() => route.query.tab, syncTabFromRoute)

onMounted(() => {
  syncTabFromRoute()
  reloadProfileConfigDraft()
})
</script>

<style scoped>
.summary-label {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.summary-value {
  margin-top: 6px;
  font-size: 22px;
  font-weight: 600;
}

.config-actions,
.consult-actions {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
