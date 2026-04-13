<template>
  <PageContainer title="护工小组" subTitle="编组管理、成员速编与快捷打印一页完成">
    <div class="group-shell">
      <section class="group-hero">
        <div>
          <p class="eyebrow">Care Team Ledger</p>
          <h2>弋阳龟峰颐养中心护工小组明细表</h2>
          <p class="hero-text">为班组长准备的现场工具页。这里既能管理编组，也能临时维护护工优点、特长，并按需要生成带照片或电话的打印版台账。</p>
        </div>
        <div class="hero-actions">
          <a-button type="primary" @click="openModal()">新增小组</a-button>
          <a-button @click="openPrintDrawer(rows[0])" :disabled="rows.length === 0">快捷打印</a-button>
        </div>
      </section>

      <a-card :bordered="false" class="group-query-card">
        <a-form layout="inline" @submit.prevent>
          <a-form-item label="关键字">
            <a-input v-model:value="query.keyword" placeholder="小组名称" allow-clear />
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model:value="query.enabled" :options="enabledOptions" allow-clear style="width: 140px" />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="search">查询</a-button>
              <a-button @click="reset">重置</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>

      <div class="group-grid">
        <a-card
          v-for="record in rows"
          :key="record.id"
          :bordered="false"
          class="group-card"
        >
          <div class="group-card-head">
            <div>
              <div class="group-name">{{ record.name }}</div>
              <div class="group-meta">
                <span>组长：{{ record.leaderStaffName || resolveMemberName(record.leaderStaffId) || '-' }}</span>
                <span>成员：{{ buildMemberRows(record).length }} 人</span>
              </div>
            </div>
            <a-tag :color="record.enabled === 1 ? 'green' : 'default'">{{ record.enabled === 1 ? '启用' : '停用' }}</a-tag>
          </div>

          <div class="member-strip">
            <div v-for="member in buildMemberRows(record).slice(0, 4)" :key="member.staffId" class="member-pill">
              <div class="member-avatar" :style="{ background: avatarColor(member.name) }">{{ avatarText(member.name) }}</div>
              <div class="member-copy">
                <div class="member-name">{{ member.name }}</div>
                <div class="member-sub">{{ member.phone || '未维护电话' }}</div>
              </div>
            </div>
            <div v-if="buildMemberRows(record).length > 4" class="member-pill member-pill-more">
              +{{ buildMemberRows(record).length - 4 }} 人
            </div>
          </div>

          <div class="group-remark">{{ record.remark || '暂无班组备注，建议补充排班习惯或服务侧重点。' }}</div>

          <div class="group-actions">
            <a-space wrap>
              <a @click="openModal(record)">编辑小组</a>
              <a @click="openProfileDrawer(record)">一键编辑优点/特长</a>
              <a @click="openPrintDrawer(record)">快捷打印</a>
              <a class="danger-text" @click="remove(record.id)">删除</a>
            </a-space>
          </div>
        </a-card>
      </div>

      <div class="group-pagination">
        <a-pagination
          :current="pagination.current"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          :show-size-changer="true"
          @change="(page, pageSize) => onTableChange({ current: page, pageSize })"
          @showSizeChange="(page, pageSize) => onTableChange({ current: page, pageSize })"
        />
      </div>
    </div>

    <a-modal v-model:open="modalOpen" title="护工小组" :confirm-loading="submitting" @ok="submit">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-form-item label="小组名称" name="name" required>
          <a-input v-model:value="form.name" />
        </a-form-item>
        <a-form-item label="组长" name="leaderStaffId">
          <a-select
            v-model:value="form.leaderStaffId"
            show-search
            allow-clear
            :filter-option="false"
            placeholder="输入姓名搜索"
            :options="staffOptions"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
          />
        </a-form-item>
        <a-form-item label="成员" name="memberStaffIds">
          <a-select
            v-model:value="form.memberStaffIds"
            mode="multiple"
            show-search
            :filter-option="false"
            placeholder="选择小组成员"
            :options="staffOptions"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
          />
        </a-form-item>
        <a-form-item label="启用状态" name="enabled">
          <a-switch v-model:checked="form.enabled" :checked-value="1" :un-checked-value="0" />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer v-model:open="profileDrawerOpen" :title="profileDrawerTitle" width="720">
      <div class="batch-toolbar">
        <span>支持一键维护整组护工的优点与特长。</span>
        <a-button type="primary" :loading="profileSaving" @click="saveProfiles">保存本组信息</a-button>
      </div>
      <div class="profile-grid">
        <div v-for="member in profileRows" :key="member.staffId" class="profile-card">
          <div class="profile-card-head">
            <div class="member-avatar large" :style="{ background: avatarColor(member.name) }">{{ avatarText(member.name) }}</div>
            <div>
              <div class="member-name">{{ member.name }}</div>
              <div class="member-sub">{{ member.phone || '未维护电话' }}</div>
            </div>
          </div>
          <a-form layout="vertical">
            <a-form-item label="优点">
              <a-textarea v-model:value="member.advantage" :rows="2" placeholder="例如：细心耐心、团队协作强" />
            </a-form-item>
            <a-form-item label="特长">
              <a-textarea v-model:value="member.specialty" :rows="2" placeholder="例如：康复按摩、基础护理、心理陪伴" />
            </a-form-item>
          </a-form>
        </div>
      </div>
    </a-drawer>

    <a-drawer v-model:open="printDrawerOpen" title="快捷打印 | 护工小组明细" width="920">
      <div class="print-toolbar">
        <div class="print-options">
          <span class="print-title">选择打印内容：</span>
          <a-checkbox v-model:checked="printOptions.photo">照片</a-checkbox>
          <a-checkbox v-model:checked="printOptions.phone">电话</a-checkbox>
          <a-checkbox v-model:checked="printOptions.advantage">优点</a-checkbox>
          <a-checkbox v-model:checked="printOptions.specialty">特长</a-checkbox>
        </div>
        <a-button type="primary" @click="printCurrentGroup">生成打印表</a-button>
      </div>

      <div class="print-preview">
        <div class="preview-paper">
          <div class="paper-header">
            <div class="paper-mark">弋阳龟峰颐养中心护工小组明细表</div>
            <h3>{{ printGroup?.name || '护工小组明细表' }}</h3>
          </div>

          <table class="preview-table">
            <thead>
              <tr>
                <th v-if="printOptions.photo">照片</th>
                <th>姓名</th>
                <th v-if="printOptions.phone">电话</th>
                <th v-if="printOptions.advantage">优点</th>
                <th v-if="printOptions.specialty">特长</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="member in previewMembers" :key="member.staffId">
                <td v-if="printOptions.photo">
                  <div class="preview-avatar" :style="{ background: avatarColor(member.name) }">{{ avatarText(member.name) }}</div>
                </td>
                <td>{{ member.name }}</td>
                <td v-if="printOptions.phone">{{ member.phone || '-' }}</td>
                <td v-if="printOptions.advantage">{{ member.advantage || '-' }}</td>
                <td v-if="printOptions.specialty">{{ member.specialty || '-' }}</td>
              </tr>
            </tbody>
          </table>

          <div class="paper-footer">
            <div>小组人数：{{ previewMembers.length }} 人</div>
            <div>负责人：{{ printGroup?.leaderStaffName || resolveMemberName(printGroup?.leaderStaffId) || '-' }}</div>
          </div>
        </div>
      </div>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getStaffOptionPage } from '../../api/rbac'
import { createCaregiverGroup, deleteCaregiverGroup, getCaregiverGroupPage, updateCaregiverGroup } from '../../api/nursing'
import type { CaregiverGroupItem, PageResult, StaffItem } from '../../types'
import { resolveCareError } from './careError'

type MemberProfileMeta = {
  advantage?: string
  specialty?: string
}

type GroupMemberRow = {
  staffId: number
  name: string
  phone?: string
  advantage?: string
  specialty?: string
}

const PROFILE_STORAGE_KEY = 'caregiver-group-member-profiles-v1'

const rows = ref<CaregiverGroupItem[]>([])
const loading = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const staffDirectory = ref<StaffItem[]>([])
const profileStore = ref<Record<string, MemberProfileMeta>>(loadProfileStore())

const profileDrawerOpen = ref(false)
const profileSaving = ref(false)
const profileRows = ref<GroupMemberRow[]>([])
const profileGroup = ref<CaregiverGroupItem | null>(null)

const printDrawerOpen = ref(false)
const printGroup = ref<CaregiverGroupItem | null>(null)
const printOptions = reactive({
  photo: true,
  phone: true,
  advantage: true,
  specialty: true
})

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  keyword: undefined as string | undefined,
  enabled: undefined as number | undefined
})

const form = reactive<Partial<CaregiverGroupItem>>({
  name: '',
  leaderStaffId: undefined,
  memberStaffIds: [],
  enabled: 1,
  remark: ''
})

const enabledOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const rules = {
  name: [{ required: true, message: '请输入小组名称', trigger: 'blur' }]
}

const staffOptions = computed(() =>
  staffDirectory.value.map((item) => ({
    label: `${item.realName || item.username || '未识别员工'}${item.staffNo ? ` (${item.staffNo})` : ''}`,
    value: Number(item.id)
  }))
)

const pagination = computed(() => ({
  current: query.pageNo,
  pageSize: query.pageSize,
  total: query.total,
  showSizeChanger: true
}))

const profileDrawerTitle = computed(() => profileGroup.value ? `一键编辑优点/特长 | ${profileGroup.value.name}` : '一键编辑优点/特长')
const previewMembers = computed(() => (printGroup.value ? buildMemberRows(printGroup.value) : []))

function loadProfileStore() {
  try {
    const raw = localStorage.getItem(PROFILE_STORAGE_KEY)
    return raw ? JSON.parse(raw) : {}
  } catch {
    return {}
  }
}

function saveProfileStore() {
  localStorage.setItem(PROFILE_STORAGE_KEY, JSON.stringify(profileStore.value))
}

function avatarText(name?: string) {
  return String(name || '?').slice(0, 1)
}

function avatarColor(name?: string) {
  const palette = ['#c96c50', '#5a7fd8', '#3b8a5e', '#a96fd6', '#cf8b2e']
  const seed = Array.from(String(name || '')).reduce((sum, char) => sum + char.charCodeAt(0), 0)
  return `linear-gradient(135deg, ${palette[seed % palette.length]}, #f4efe8)`
}

function resetForm() {
  form.id = undefined
  form.name = ''
  form.leaderStaffId = undefined
  form.memberStaffIds = []
  form.enabled = 1
  form.remark = ''
}

function resolveStaff(staffId?: number) {
  return staffDirectory.value.find((item) => Number(item.id) === Number(staffId))
}

function resolveMemberName(staffId?: number) {
  return resolveStaff(staffId)?.realName || ''
}

function buildMemberRows(group?: CaregiverGroupItem | null) {
  return (group?.memberStaffIds || []).map((staffId) => {
    const staff = resolveStaff(staffId)
    const meta = profileStore.value[String(staffId)] || {}
    return {
      staffId,
      name: staff?.realName || staff?.username || '未识别员工',
      phone: staff?.phone,
      advantage: meta.advantage,
      specialty: meta.specialty
    } as GroupMemberRow
  })
}

function openModal(record?: CaregiverGroupItem) {
  resetForm()
  if (record) {
    Object.assign(form, {
      id: record.id,
      name: record.name,
      leaderStaffId: record.leaderStaffId,
      memberStaffIds: record.memberStaffIds || [],
      enabled: record.enabled ?? 1,
      remark: record.remark || ''
    })
  }
  modalOpen.value = true
}

function openProfileDrawer(group: CaregiverGroupItem) {
  profileGroup.value = group
  profileRows.value = buildMemberRows(group)
  profileDrawerOpen.value = true
}

function openPrintDrawer(group?: CaregiverGroupItem) {
  if (!group) {
    message.info('当前没有可打印的小组')
    return
  }
  printGroup.value = group
  printDrawerOpen.value = true
}

async function saveProfiles() {
  profileSaving.value = true
  try {
    profileRows.value.forEach((item) => {
      profileStore.value[String(item.staffId)] = {
        advantage: item.advantage,
        specialty: item.specialty
      }
    })
    saveProfileStore()
    message.success('护工优点与特长已保存')
    profileDrawerOpen.value = false
  } finally {
    profileSaving.value = false
  }
}

function printCurrentGroup() {
  if (!printGroup.value) return
  const popup = window.open('', '_blank')
  if (!popup) {
    message.error('请允许弹窗后重试打印')
    return
  }
  const members = buildMemberRows(printGroup.value)
  const includePhoto = printOptions.photo
  const includePhone = printOptions.phone
  const includeAdvantage = printOptions.advantage
  const includeSpecialty = printOptions.specialty
  popup.document.write(`
    <html>
      <head>
        <title>弋阳龟峰颐养中心护工小组明细表</title>
        <style>
          body { font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif; background: #f3efe8; padding: 24px; color: #2b2117; }
          .paper { max-width: 900px; margin: 0 auto; background: #fffdf9; border-radius: 18px; padding: 28px; box-shadow: 0 20px 60px rgba(111, 87, 58, 0.15); }
          .mark { text-align: center; font-size: 15px; color: #6d5b49; margin-bottom: 8px; }
          h1 { text-align: center; font-size: 28px; margin: 0 0 18px; }
          table { width: 100%; border-collapse: collapse; }
          th, td { border: 1px solid #dccfbd; padding: 12px 10px; font-size: 13px; vertical-align: middle; }
          th { background: #ede5d9; }
          .avatar { width: 54px; height: 68px; border-radius: 12px; display: inline-flex; align-items: center; justify-content: center; color: white; font-size: 28px; font-weight: 700; }
          .footer { margin-top: 20px; padding-top: 16px; border-top: 1px solid #e6dbcd; display: flex; justify-content: space-between; font-size: 13px; }
        </style>
      </head>
      <body>
        <div class="paper">
          <div class="mark">弋阳龟峰颐养中心护工小组明细表</div>
          <h1>${escapeHtml(printGroup.value.name)}</h1>
          <table>
            <thead>
              <tr>
                ${includePhoto ? '<th>照片</th>' : ''}
                <th>姓名</th>
                ${includePhone ? '<th>电话</th>' : ''}
                ${includeAdvantage ? '<th>优点</th>' : ''}
                ${includeSpecialty ? '<th>特长</th>' : ''}
              </tr>
            </thead>
            <tbody>
              ${members.map((member) => `
                <tr>
                  ${includePhoto ? `<td><div class="avatar" style="background:${escapeHtml(avatarColor(member.name))}">${escapeHtml(avatarText(member.name))}</div></td>` : ''}
                  <td>${escapeHtml(member.name)}</td>
                  ${includePhone ? `<td>${escapeHtml(member.phone || '-')}</td>` : ''}
                  ${includeAdvantage ? `<td>${escapeHtml(member.advantage || '-')}</td>` : ''}
                  ${includeSpecialty ? `<td>${escapeHtml(member.specialty || '-')}</td>` : ''}
                </tr>
              `).join('')}
            </tbody>
          </table>
          <div class="footer">
            <div>小组人数：${members.length} 人</div>
            <div>负责人：${escapeHtml(printGroup.value.leaderStaffName || resolveMemberName(printGroup.value.leaderStaffId) || '-')}</div>
          </div>
        </div>
      </body>
    </html>
  `)
  popup.document.close()
  popup.focus()
  popup.print()
}

function escapeHtml(value: string) {
  return String(value || '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}

async function submit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    const payload: Partial<CaregiverGroupItem> = {
      name: form.name,
      leaderStaffId: form.leaderStaffId,
      memberStaffIds: form.memberStaffIds || [],
      enabled: form.enabled ?? 1,
      remark: form.remark
    }
    if (form.id) {
      await updateCaregiverGroup(form.id, payload)
    } else {
      await createCaregiverGroup(payload)
    }
    message.success('保存成功')
    modalOpen.value = false
    await load()
  } catch (error) {
    message.error(resolveCareError(error, '保存失败'))
  } finally {
    submitting.value = false
  }
}

async function remove(id: number) {
  Modal.confirm({
    title: '确认删除该护工小组？',
    onOk: async () => {
      try {
        await deleteCaregiverGroup(id)
        message.success('删除成功')
        await load()
      } catch (error) {
        message.error(resolveCareError(error, '删除失败'))
      }
    }
  })
}

function onTableChange(pag: { current?: number; pageSize?: number }) {
  query.pageNo = pag.current || 1
  query.pageSize = pag.pageSize || 10
  load()
}

function search() {
  query.pageNo = 1
  load()
}

function reset() {
  query.keyword = undefined
  query.enabled = undefined
  query.pageNo = 1
  load()
}

async function searchStaff(keyword = '') {
  try {
    const res: PageResult<StaffItem> = await getStaffOptionPage({
      pageNo: 1,
      pageSize: 300,
      keyword,
      activeOnly: true
    })
    staffDirectory.value = res.list || []
  } catch {
    staffDirectory.value = []
  }
}

async function load() {
  loading.value = true
  try {
    const [groupRes] = await Promise.all([
      getCaregiverGroupPage({
        pageNo: query.pageNo,
        pageSize: query.pageSize,
        keyword: query.keyword,
        enabled: query.enabled
      }),
      searchStaff('')
    ])
    rows.value = groupRes.list
    query.total = groupRes.total
  } catch (error) {
    message.error(resolveCareError(error, '加载护工小组失败'))
    rows.value = []
    query.total = 0
  } finally {
    loading.value = false
  }
}

load()
</script>

<style scoped>
.group-shell {
  --care-cream: #f5efe7;
  --care-ink: #33261b;
  --care-muted: #7f6a53;
  --care-line: #e6d7c7;
  --care-accent: #3f7ad8;
  --care-accent-soft: rgba(63, 122, 216, 0.12);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.group-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 24px 28px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.9), transparent 42%),
    linear-gradient(135deg, #f6efe5, #f1e4d5 60%, #e7d4bf);
  border: 1px solid rgba(154, 121, 84, 0.18);
}

.eyebrow {
  margin: 0 0 8px;
  color: #8b6b49;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.group-hero h2 {
  margin: 0;
  color: var(--care-ink);
  font-size: 30px;
  line-height: 1.2;
}

.hero-text {
  max-width: 760px;
  margin: 10px 0 0;
  color: var(--care-muted);
}

.hero-actions {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.group-query-card,
.group-card {
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(255,255,255,0.98), rgba(251,248,243,0.96));
  box-shadow: 0 18px 40px rgba(80, 55, 22, 0.08);
}

.group-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
}

.group-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.group-card-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.group-name {
  font-size: 22px;
  font-weight: 700;
  color: var(--care-ink);
}

.group-meta {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: var(--care-muted);
  font-size: 13px;
}

.member-strip {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(132px, 1fr));
  gap: 10px;
  margin-top: 18px;
}

.member-pill {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 18px;
  background: #faf4ec;
  border: 1px solid var(--care-line);
}

.member-pill-more {
  justify-content: center;
  color: var(--care-muted);
  font-weight: 600;
}

.member-avatar {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: 700;
  box-shadow: inset 0 0 0 1px rgba(255,255,255,0.2);
}

.member-avatar.large {
  width: 54px;
  height: 54px;
  font-size: 22px;
}

.member-copy {
  min-width: 0;
}

.member-name {
  font-weight: 700;
  color: var(--care-ink);
}

.member-sub {
  color: var(--care-muted);
  font-size: 12px;
}

.group-remark {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed var(--care-line);
  color: var(--care-muted);
  min-height: 58px;
}

.group-actions {
  margin-top: 16px;
}

.danger-text {
  color: #d84f4f;
}

.batch-toolbar,
.print-toolbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
  padding: 14px 16px;
  border-radius: 18px;
  background: #faf3ea;
}

.print-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.print-title {
  color: var(--care-ink);
  font-weight: 700;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.profile-card {
  padding: 18px;
  border-radius: 20px;
  background: linear-gradient(180deg, #fffdf9, #f8f1e9);
  border: 1px solid var(--care-line);
}

.profile-card-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.print-preview {
  padding: 18px;
  border-radius: 24px;
  background: linear-gradient(180deg, #f2eee7, #ece4d8);
}

.preview-paper {
  max-width: 760px;
  margin: 0 auto;
  background: #fffdfa;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 20px 50px rgba(83, 54, 20, 0.1);
}

.paper-header {
  text-align: center;
}

.paper-mark {
  color: #8b6b49;
  font-size: 13px;
  margin-bottom: 6px;
}

.paper-header h3 {
  margin: 0 0 16px;
  color: var(--care-ink);
  font-size: 26px;
}

.preview-table {
  width: 100%;
  border-collapse: collapse;
}

.preview-table th,
.preview-table td {
  border: 1px solid #daccbb;
  padding: 12px 10px;
  text-align: left;
}

.preview-table th {
  background: #ebe1d4;
}

.preview-avatar {
  width: 52px;
  height: 66px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  font-weight: 700;
}

.paper-footer {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid #e6dbcf;
  color: var(--care-ink);
  font-weight: 600;
}

@media (max-width: 900px) {
  .group-hero,
  .batch-toolbar,
  .print-toolbar,
  .paper-footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
