<template>
  <PageContainer title="排班方案" subTitle="按方案名称打包保存多条班次规则，可重复实施到员工排班">
    <a-card :bordered="false" class="card-elevated">
      <a-form layout="inline" @submit.prevent>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" placeholder="方案名称/班次编码/执行人" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.enabled" :options="enabledOptions" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="load">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" @click="openModal()">新增方案组</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        row-key="name"
        :columns="columns"
        :data-source="pagedRows"
        :loading="loading"
        :pagination="pagination"
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'enabled'">
            <a-tag :color="record.enabled === 1 ? 'green' : 'default'">{{ record.enabled === 1 ? '启用' : '停用' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'rules'">
            <a-space wrap>
              <a-tag v-for="item in record.items" :key="`${record.name}-${item.id}`" color="blue">
                {{ item.shiftCode }} / {{ item.executeStaffName || '未分配' }}
              </a-tag>
            </a-space>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a @click="openApply(record)">实施</a>
              <a @click="openModal(record)">编辑</a>
              <a class="danger-text" @click="removeScheme(record)">删除</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="modalOpen" title="排班方案组" :confirm-loading="submitting" @ok="submit" width="980px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="方案名称" required>
              <a-input v-model:value="form.name" placeholder="例如：护理A组白夜轮值" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="方案状态">
              <a-switch v-model:checked="form.enabled" :checked-value="1" :un-checked-value="0" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="方案备注">
          <a-textarea v-model:value="form.remark" :rows="2" placeholder="同一个方案下可保存多条班次规则，实施时整组一起下发。" />
        </a-form-item>
        <a-divider orientation="left">班次规则</a-divider>
        <a-space style="margin-bottom: 12px">
          <a-button @click="addRule">新增规则</a-button>
          <a-tag color="blue">当前共 {{ form.items.length }} 条规则</a-tag>
        </a-space>
        <a-card v-for="(item, index) in form.items" :key="item.localKey" size="small" style="margin-bottom: 12px">
          <template #title>规则 {{ index + 1 }}</template>
          <template #extra>
            <a-button danger type="link" @click="removeRule(index)">删除</a-button>
          </template>
          <a-row :gutter="12">
            <a-col :span="8">
              <a-form-item label="班次编码" required>
                <a-input v-model:value="item.shiftCode" placeholder="DAY / NIGHT / A1" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="开始时间" required>
                <a-time-picker v-model:value="item.startTime" value-format="HH:mm:ss" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="结束时间" required>
                <a-time-picker v-model:value="item.endTime" value-format="HH:mm:ss" style="width: 100%" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="12">
            <a-col :span="6">
              <a-form-item label="跨天班次">
                <a-switch v-model:checked="item.crossDay" :checked-value="1" :un-checked-value="0" />
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item label="建议人数">
                <a-input-number v-model:value="item.requiredStaffCount" :min="1" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item label="执行周期">
                <a-select v-model:value="item.recurrenceType">
                  <a-select-option value="WEEKLY_ONCE">一周一次</a-select-option>
                  <a-select-option value="WEEKLY_TWICE">一周两次</a-select-option>
                  <a-select-option value="DAILY_ONCE">每天一次</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item label="联动打卡">
                <a-switch v-model:checked="item.attendanceLinked" :checked-value="1" :un-checked-value="0" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="12">
            <a-col :span="12">
              <a-form-item label="执行人">
                <a-select
                  v-model:value="item.executeStaffId"
                  show-search
                  :filter-option="false"
                  placeholder="输入姓名搜索"
                  :options="staffOptions"
                  @search="searchStaff"
                  @focus="() => !staffOptions.length && searchStaff('')"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="规则备注">
                <a-input v-model:value="item.remark" placeholder="可填写适用场景、夜班补充说明等" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-card>
      </a-form>
    </a-modal>

    <a-modal v-model:open="applyOpen" title="实施排班方案组" :confirm-loading="applying" @ok="submitApply">
      <a-form layout="vertical">
        <a-form-item label="方案名称">
          <a-input :value="applyTarget?.name" disabled />
        </a-form-item>
        <a-form-item label="规则数量">
          <a-input :value="String(applyTarget?.items?.length || 0)" disabled />
        </a-form-item>
        <a-form-item label="开始日期" required>
          <a-date-picker v-model:value="applyForm.startDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="结束日期" required>
          <a-date-picker v-model:value="applyForm.endDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { applyShiftTemplate, deleteShiftTemplate, getShiftTemplateList, saveShiftTemplateBatch } from '../../api/nursing'
import type { ShiftTemplateBatchSaveItem, ShiftTemplateItem } from '../../types'
import { resolveCareError } from './careError'

type SchemeRow = {
  id: number
  name: string
  enabled: number
  remark?: string
  items: ShiftTemplateItem[]
}

type EditableRule = ShiftTemplateBatchSaveItem & { localKey: string }

const loading = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const applying = ref(false)
const applyOpen = ref(false)
const rawRows = ref<ShiftTemplateItem[]>([])
const applyTarget = ref<SchemeRow>()
const { staffOptions, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 120 })

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  keyword: undefined as string | undefined,
  enabled: undefined as number | undefined
})

const form = reactive<{
  name: string
  enabled: number
  remark: string
  items: EditableRule[]
}>({
  name: '',
  enabled: 1,
  remark: '',
  items: []
})

const applyForm = reactive({
  startDate: '',
  endDate: ''
})

const enabledOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const columns = [
  { title: '方案名称', dataIndex: 'name', key: 'name' },
  { title: '规则条数', key: 'ruleCount', width: 100, customRender: ({ record }: { record: SchemeRow }) => record.items.length },
  { title: '班次规则', key: 'rules' },
  { title: '状态', key: 'enabled', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 220 },
  { title: '操作', key: 'actions', width: 180 }
]

const groupedRows = computed<SchemeRow[]>(() => {
  const groups = new Map<string, SchemeRow>()
  for (const item of rawRows.value) {
    const key = String(item.name || '').trim()
    if (!key) continue
    const existed = groups.get(key)
    if (existed) {
      existed.items.push(item)
      existed.enabled = existed.enabled === 1 && item.enabled === 1 ? 1 : 0
      if (!existed.remark && item.remark) existed.remark = item.remark
      continue
    }
    groups.set(key, {
      id: item.id,
      name: key,
      enabled: item.enabled === 1 ? 1 : 0,
      remark: item.remark,
      items: [item]
    })
  }
  let rows = Array.from(groups.values()).map((item) => ({
    ...item,
    items: [...item.items].sort((a, b) => Number(a.ruleSort || 0) - Number(b.ruleSort || 0))
  }))
  if (query.enabled != null) {
    rows = rows.filter((item) => item.enabled === query.enabled)
  }
  if (query.keyword) {
    const keyword = query.keyword.trim()
    rows = rows.filter((item) =>
      item.name.includes(keyword)
      || item.items.some((rule) =>
        String(rule.shiftCode || '').includes(keyword) || String(rule.executeStaffName || '').includes(keyword)
      )
    )
  }
  query.total = rows.length
  return rows.sort((a, b) => a.name.localeCompare(b.name, 'zh-CN'))
})

const pagedRows = computed(() => {
  const start = (query.pageNo - 1) * query.pageSize
  return groupedRows.value.slice(start, start + query.pageSize)
})

const pagination = computed(() => ({
  current: query.pageNo,
  pageSize: query.pageSize,
  total: query.total,
  showSizeChanger: true
}))

function makeRule(seed?: Partial<ShiftTemplateItem>): EditableRule {
  if (seed?.executeStaffId && seed.executeStaffName) {
    ensureSelectedStaff(seed.executeStaffId, seed.executeStaffName)
  }
  return {
    localKey: `${Date.now()}-${Math.random()}`,
    id: seed?.id,
    shiftCode: seed?.shiftCode || '',
    startTime: seed?.startTime || '08:00:00',
    endTime: seed?.endTime || '16:00:00',
    crossDay: seed?.crossDay ?? 0,
    requiredStaffCount: seed?.requiredStaffCount ?? 1,
    recurrenceType: seed?.recurrenceType || 'WEEKLY_ONCE',
    executeStaffId: seed?.executeStaffId,
    executeStaffName: seed?.executeStaffName,
    attendanceLinked: seed?.attendanceLinked ?? 1,
    enabled: seed?.enabled ?? 1,
    remark: seed?.remark || ''
  }
}

function resetForm() {
  form.name = ''
  form.enabled = 1
  form.remark = ''
  form.items = [makeRule()]
}

function addRule() {
  form.items.push(makeRule())
}

function removeRule(index: number) {
  form.items.splice(index, 1)
  if (form.items.length === 0) {
    form.items.push(makeRule())
  }
}

function openModal(record?: SchemeRow) {
  resetForm()
  if (record) {
    form.name = record.name
    form.enabled = record.enabled
    form.remark = record.remark || ''
    form.items = record.items.map((item) => makeRule(item))
  }
  modalOpen.value = true
}

async function submit() {
  if (!form.name.trim()) {
    message.warning('请填写方案名称')
    return
  }
  if (!form.items.length) {
    message.warning('请至少维护 1 条班次规则')
    return
  }
  for (const item of form.items) {
    if (!item.shiftCode || !item.startTime || !item.endTime) {
      message.warning('请补全每条规则的班次编码和起止时间')
      return
    }
  }
  submitting.value = true
  try {
    await saveShiftTemplateBatch({
      name: form.name.trim(),
      enabled: form.enabled,
      remark: form.remark,
      replaceExisting: 1,
      items: form.items.map(({ localKey, executeStaffName, ...item }) => item)
    })
    message.success('方案组已保存')
    modalOpen.value = false
    await load()
  } catch (error) {
    message.error(resolveCareError(error, '保存失败'))
  } finally {
    submitting.value = false
  }
}

function openApply(record: SchemeRow) {
  applyTarget.value = record
  applyForm.startDate = ''
  applyForm.endDate = ''
  applyOpen.value = true
}

async function submitApply() {
  if (!applyTarget.value?.id || !applyForm.startDate || !applyForm.endDate) {
    message.warning('请选择完整日期范围')
    return
  }
  applying.value = true
  try {
    await applyShiftTemplate(applyTarget.value.id, {
      startDate: applyForm.startDate,
      endDate: applyForm.endDate
    })
    message.success('方案组已实施，排班将同步到员工日程并生成值班提醒')
    applyOpen.value = false
  } catch (error) {
    message.error(resolveCareError(error, '实施失败'))
  } finally {
    applying.value = false
  }
}

async function removeScheme(record: SchemeRow) {
  Modal.confirm({
    title: `确认删除方案“${record.name}”？`,
    content: '该方案名下的全部班次规则都会删除。',
    onOk: async () => {
      try {
        await Promise.all(record.items.map((item) => deleteShiftTemplate(item.id)))
        message.success('方案组已删除')
        await load()
      } catch (error) {
        message.error(resolveCareError(error, '删除失败'))
      }
    }
  })
}

function onTableChange(pag: any) {
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
}

function reset() {
  query.pageNo = 1
  query.pageSize = 10
  query.keyword = undefined
  query.enabled = undefined
}

async function load() {
  loading.value = true
  try {
    rawRows.value = await getShiftTemplateList()
  } catch (error) {
    rawRows.value = []
    message.error(resolveCareError(error, '加载失败'))
  } finally {
    loading.value = false
  }
}

searchStaff('')
load()
</script>
