<template>
  <PageContainer title="智能排班工作台" subTitle="AI 生成排班方案 → 人工调整 → 一键采纳写回排班表">
    <a-card :bordered="false" class="card-elevated" title="生成参数">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :xs="24" :md="8">
            <a-form-item label="方案名称">
              <a-input v-model:value="form.title" placeholder="如：护理部7月第2周排班" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="排班周期" required>
              <a-range-picker v-model:value="form.dateRange" style="width: 100%" value-format="YYYY-MM-DD" />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="班次（不选=全部启用班次）">
              <a-select
                v-model:value="form.shiftCodes"
                mode="multiple"
                allow-clear
                placeholder="选择参与排班的班次"
                :options="shiftOptions"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :xs="24" :md="16">
            <a-form-item label="参与员工（不选=全机构在职员工）">
              <a-select
                v-model:value="form.staffIds"
                mode="multiple"
                allow-clear
                show-search
                placeholder="选择参与排班的员工"
                :options="staffOptions"
                :filter-option="filterOption"
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="8">
            <a-form-item label="备注">
              <a-input v-model:value="form.remark" placeholder="可选" allow-clear />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
      <a-space wrap>
        <a-button type="primary" :loading="generating" @click="handleGenerate">生成排班方案</a-button>
        <a-button @click="constraintVisible = true">排班约束配置</a-button>
        <a-button @click="loadProposals">刷新方案列表</a-button>
      </a-space>
    </a-card>

    <a-card :bordered="false" class="card-elevated" style="margin-top: 12px" title="方案列表">
      <a-table
        :data-source="proposals"
        :columns="proposalColumns"
        :loading="loadingProposals"
        :pagination="{ current: pageNo, pageSize, total, onChange: onPageChange }"
        row-key="id"
        size="middle"
        :scroll="{ x: 900 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'unfilled'">
            <a-tag :color="(record.unfilledCount || 0) > 0 ? 'orange' : 'green'">
              {{ record.unfilledCount || 0 }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a @click="openDetail(record.id)">查看/调整</a>
              <a-popconfirm
                v-if="record.status !== 'ADOPTED'"
                title="采纳后将写回正式排班表，确认？"
                @confirm="handleAdopt(record.id)"
              >
                <a>一键采纳</a>
              </a-popconfirm>
              <a-popconfirm title="确认删除该方案？" @confirm="handleDelete(record.id)">
                <a style="color: var(--error, #ff4d4f)">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-card
      v-if="detail"
      :bordered="false"
      class="card-elevated"
      style="margin-top: 12px"
      :title="`方案预览：${detail.title}（${detail.dateFrom} ~ ${detail.dateTo}）`"
    >
      <a-space wrap style="margin-bottom: 12px">
        <a-tag :color="statusColor(detail.status)">{{ statusText(detail.status) }}</a-tag>
        <a-tag color="blue">共 {{ (detail.items || []).length }} 条班次</a-tag>
        <a-tag :color="(detail.unfilledCount || 0) > 0 ? 'orange' : 'green'">缺口 {{ detail.unfilledCount || 0 }}</a-tag>
        <a-popconfirm
          v-if="detail.status !== 'ADOPTED'"
          title="采纳后将写回正式排班表，确认？"
          @confirm="handleAdopt(detail.id)"
        >
          <a-button type="primary" size="small">一键采纳</a-button>
        </a-popconfirm>
      </a-space>
      <a-alert
        v-if="(detail.unfilledSlots || []).length"
        type="warning"
        show-icon
        style="margin-bottom: 12px"
        :message="`存在排班缺口：${(detail.unfilledSlots || []).join('；')}`"
      />
      <div style="overflow-x: auto">
        <table class="grid-table">
          <thead>
            <tr>
              <th class="grid-th sticky-col">员工 \ 日期</th>
              <th v-for="day in gridDays" :key="day" class="grid-th">{{ day.slice(5) }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in gridRows" :key="String(row.staffId)">
              <td class="grid-td sticky-col">{{ row.staffName }}</td>
              <td v-for="day in gridDays" :key="day" class="grid-td">
                <a-tag
                  v-for="item in row.cells[day] || []"
                  :key="String(item.id)"
                  :color="item.nightShift === 1 ? 'purple' : item.manualAdjusted === 1 ? 'gold' : 'blue'"
                  style="cursor: pointer; margin: 2px"
                  @click="openItemEdit(item)"
                >
                  {{ item.shiftCode }}{{ item.violationNote ? ' ⚠' : '' }}
                </a-tag>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div style="margin-top: 8px; color: var(--muted, #999)">
        紫色=夜班，金色=人工调整过，⚠=存在冲突提示；点击班次标签可调整或移除。
      </div>
    </a-card>

    <a-modal
      v-model:open="itemEditVisible"
      title="调整班次"
      :confirm-loading="itemSaving"
      @ok="handleItemSave"
    >
      <a-form layout="vertical" v-if="editingItem">
        <a-form-item label="日期">
          <a-input :value="editingItem.dutyDate" disabled />
        </a-form-item>
        <a-form-item label="员工">
          <a-select
            v-model:value="itemForm.staffId"
            show-search
            :options="staffOptions"
            :filter-option="filterOption"
          />
        </a-form-item>
        <a-form-item label="班次">
          <a-select v-model:value="itemForm.shiftCode" :options="shiftOptions" />
        </a-form-item>
        <a-form-item v-if="editingItem.violationNote">
          <a-alert type="warning" show-icon :message="editingItem.violationNote" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-button danger :loading="itemSaving" @click="handleItemRemove">移除该班次</a-button>
        <a-button @click="itemEditVisible = false">取消</a-button>
        <a-button type="primary" :loading="itemSaving" @click="handleItemSave">保存调整</a-button>
      </template>
    </a-modal>

    <a-modal
      v-model:open="constraintVisible"
      title="排班约束配置"
      :confirm-loading="constraintSaving"
      @ok="handleConstraintSave"
    >
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="每人每周最大工时">
              <a-input-number v-model:value="constraint.maxWeeklyHours" :min="8" :max="80" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="连续上班天数上限">
              <a-input-number v-model:value="constraint.maxConsecutiveDays" :min="1" :max="14" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="夜班后强制休一天">
              <a-switch :checked="constraint.nightRestEnabled === 1" @change="(v: boolean) => (constraint.nightRestEnabled = v ? 1 : 0)" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="避开已批准请假">
              <a-switch :checked="constraint.respectLeave === 1" @change="(v: boolean) => (constraint.respectLeave = v ? 1 : 0)" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="工时均衡权重">
              <a-input-number v-model:value="constraint.workloadBalanceWeight" :min="0" :max="10" :step="0.5" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="夜班公平权重">
              <a-input-number v-model:value="constraint.nightFairnessWeight" :min="0" :max="10" :step="0.5" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="班次资质要求 JSON（{&quot;班次编码&quot;:[&quot;角色编码&quot;]}）">
          <a-textarea v-model:value="constraint.qualificationJson" :rows="3" placeholder='如 {"NIGHT":["NURSING_EMPLOYEE"]}' />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import {
  adoptAiScheduleProposal,
  deleteAiScheduleProposal,
  deleteAiScheduleProposalItem,
  generateAiScheduleProposal,
  getAiScheduleConstraint,
  getAiScheduleProposal,
  getAiScheduleProposalPage,
  saveAiScheduleConstraint,
  updateAiScheduleProposalItem,
  type AiScheduleConstraint,
  type AiScheduleProposal,
  type AiScheduleProposalItem
} from '../../api/ai'
import { getShiftTemplateList } from '../../api/nursing'
import { getStaffPage } from '../../api/staff'
import type { Id } from '../../types/common'

const form = reactive<{
  title: string
  dateRange: [string, string] | null
  shiftCodes: string[]
  staffIds: Id[]
  remark: string
}>({ title: '', dateRange: null, shiftCodes: [], staffIds: [], remark: '' })

const shiftOptions = ref<Array<{ label: string; value: string }>>([])
const staffOptions = ref<Array<{ label: string; value: Id }>>([])

const proposals = ref<AiScheduleProposal[]>([])
const loadingProposals = ref(false)
const generating = ref(false)
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detail = ref<AiScheduleProposal | null>(null)

const itemEditVisible = ref(false)
const itemSaving = ref(false)
const editingItem = ref<AiScheduleProposalItem | null>(null)
const itemForm = reactive<{ staffId?: Id; shiftCode?: string }>({})

const constraintVisible = ref(false)
const constraintSaving = ref(false)
const constraint = reactive<AiScheduleConstraint>({
  maxWeeklyHours: 40,
  maxConsecutiveDays: 5,
  nightRestEnabled: 1,
  respectLeave: 1,
  workloadBalanceWeight: 1,
  nightFairnessWeight: 1,
  qualificationJson: ''
})

const proposalColumns = [
  { title: '方案名称', dataIndex: 'title', key: 'title', width: 220 },
  { title: '周期', key: 'range', width: 200, customRender: ({ record }: any) => `${record.dateFrom} ~ ${record.dateTo}` },
  { title: '状态', key: 'status', width: 100 },
  { title: '缺口', key: 'unfilled', width: 80 },
  { title: '生成时间', dataIndex: 'generatedAt', key: 'generatedAt', width: 170 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' as const }
]

const gridDays = computed(() => {
  if (!detail.value) return []
  const days: string[] = []
  let cursor = new Date(detail.value.dateFrom + 'T00:00:00')
  const end = new Date(detail.value.dateTo + 'T00:00:00')
  while (cursor <= end) {
    days.push(cursor.toISOString().slice(0, 10))
    cursor = new Date(cursor.getTime() + 24 * 3600 * 1000)
  }
  return days
})

const gridRows = computed(() => {
  const rows = new Map<string, { staffId: Id; staffName: string; cells: Record<string, AiScheduleProposalItem[]> }>()
  for (const item of detail.value?.items || []) {
    const key = String(item.staffId)
    if (!rows.has(key)) {
      rows.set(key, { staffId: item.staffId, staffName: item.staffName || key, cells: {} })
    }
    const row = rows.get(key)!
    if (!row.cells[item.dutyDate]) row.cells[item.dutyDate] = []
    row.cells[item.dutyDate].push(item)
  }
  return Array.from(rows.values()).sort((a, b) => a.staffName.localeCompare(b.staffName, 'zh-CN'))
})

function filterOption(input: string, option: any) {
  return String(option?.label || '').toLowerCase().includes(input.toLowerCase())
}

function statusText(status?: string) {
  if (status === 'ADOPTED') return '已采纳'
  if (status === 'GENERATED') return '已生成'
  return '草稿'
}

function statusColor(status?: string) {
  if (status === 'ADOPTED') return 'green'
  if (status === 'GENERATED') return 'blue'
  return 'default'
}

async function loadOptions() {
  try {
    const templates = await getShiftTemplateList()
    shiftOptions.value = (templates || [])
      .filter((t: any) => t.shiftCode)
      .map((t: any) => ({ label: `${t.name || t.shiftCode}（${t.shiftCode}）`, value: t.shiftCode }))
  } catch {
    shiftOptions.value = []
  }
  try {
    const staffPage = await getStaffPage({ pageNo: 1, pageSize: 500 })
    staffOptions.value = (staffPage.list || []).map((s: any) => ({
      label: `${s.realName || s.username}（${s.staffNo || s.id}）`,
      value: s.id
    }))
  } catch {
    staffOptions.value = []
  }
}

async function loadProposals() {
  loadingProposals.value = true
  try {
    const res = await getAiScheduleProposalPage({ pageNo: pageNo.value, pageSize: pageSize.value })
    proposals.value = res.list || []
    total.value = res.total || 0
  } catch (error: any) {
    message.error(error?.message || '加载方案列表失败')
  } finally {
    loadingProposals.value = false
  }
}

function onPageChange(no: number, size: number) {
  pageNo.value = no
  pageSize.value = size
  loadProposals()
}

async function handleGenerate() {
  if (!form.dateRange || !form.dateRange[0] || !form.dateRange[1]) {
    message.warning('请选择排班周期')
    return
  }
  generating.value = true
  try {
    const proposal = await generateAiScheduleProposal({
      title: form.title || undefined,
      dateFrom: form.dateRange[0],
      dateTo: form.dateRange[1],
      staffIds: form.staffIds.length ? form.staffIds : undefined,
      shiftCodes: form.shiftCodes.length ? form.shiftCodes : undefined,
      remark: form.remark || undefined
    })
    message.success('方案已生成，可在下方预览并调整')
    detail.value = proposal
    await loadProposals()
  } catch (error: any) {
    message.error(error?.message || '生成失败')
  } finally {
    generating.value = false
  }
}

async function openDetail(id: Id) {
  try {
    detail.value = await getAiScheduleProposal(id)
  } catch (error: any) {
    message.error(error?.message || '加载方案详情失败')
  }
}

function openItemEdit(item: AiScheduleProposalItem) {
  if (detail.value?.status === 'ADOPTED') {
    message.info('方案已采纳，不能再调整')
    return
  }
  editingItem.value = item
  itemForm.staffId = item.staffId
  itemForm.shiftCode = item.shiftCode
  itemEditVisible.value = true
}

async function handleItemSave() {
  if (!detail.value || !editingItem.value) return
  itemSaving.value = true
  try {
    await updateAiScheduleProposalItem(detail.value.id, editingItem.value.id, {
      staffId: itemForm.staffId,
      shiftCode: itemForm.shiftCode
    })
    message.success('已调整')
    itemEditVisible.value = false
    await openDetail(detail.value.id)
  } catch (error: any) {
    message.error(error?.message || '调整失败')
  } finally {
    itemSaving.value = false
  }
}

async function handleItemRemove() {
  if (!detail.value || !editingItem.value) return
  itemSaving.value = true
  try {
    await deleteAiScheduleProposalItem(detail.value.id, editingItem.value.id)
    message.success('已移除该班次')
    itemEditVisible.value = false
    await openDetail(detail.value.id)
  } catch (error: any) {
    message.error(error?.message || '移除失败')
  } finally {
    itemSaving.value = false
  }
}

async function handleAdopt(id: Id) {
  try {
    const res = await adoptAiScheduleProposal(id)
    message.success(`已采纳：写回 ${res.createdCount} 条排班，跳过 ${res.skippedCount} 条重复`)
    await loadProposals()
    if (detail.value?.id === id) {
      await openDetail(id)
    }
  } catch (error: any) {
    message.error(error?.message || '采纳失败')
  }
}

async function handleDelete(id: Id) {
  try {
    await deleteAiScheduleProposal(id)
    message.success('已删除')
    if (detail.value?.id === id) {
      detail.value = null
    }
    await loadProposals()
  } catch (error: any) {
    message.error(error?.message || '删除失败')
  }
}

async function loadConstraint() {
  try {
    const res = await getAiScheduleConstraint()
    Object.assign(constraint, res || {})
  } catch {
    // 使用默认值
  }
}

async function handleConstraintSave() {
  constraintSaving.value = true
  try {
    const saved = await saveAiScheduleConstraint({ ...constraint })
    Object.assign(constraint, saved || {})
    message.success('约束配置已保存')
    constraintVisible.value = false
  } catch (error: any) {
    message.error(error?.message || '保存失败')
  } finally {
    constraintSaving.value = false
  }
}

onMounted(() => {
  loadOptions()
  loadProposals()
  loadConstraint()
})
</script>

<style scoped>
.grid-table {
  border-collapse: collapse;
  min-width: 100%;
}
.grid-th,
.grid-td {
  border: 1px solid var(--border-color, #f0f0f0);
  padding: 6px 8px;
  text-align: center;
  white-space: nowrap;
  font-size: 13px;
}
.grid-th {
  background: var(--table-header-bg, #fafafa);
  font-weight: 600;
}
.sticky-col {
  position: sticky;
  left: 0;
  background: var(--component-background, #fff);
  z-index: 1;
}
</style>
