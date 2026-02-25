<template>
  <PageContainer title="老人管理" subTitle="档案维护与床位绑定">
    <SearchForm :model="query" @search="onSearch" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="姓名/编号" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 160px">
          <a-select-option :value="1">在院</a-select-option>
          <a-select-option :value="2">请假</a-select-option>
          <a-select-option :value="3">离院</a-select-option>
        </a-select>
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openDrawer()">新增老人</a-button>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="list"
      :loading="loading"
      :pagination="pagination"
      @change="onTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : record.status === 2 ? 'gold' : 'red'">
            {{ statusText(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a @click="openDrawer(record)">编辑</a>
            <a @click="openDetail(record)">详情</a>
            <a @click="openAssign(record)">分配床位</a>
            <a @click="openUnbind(record)">退住/解绑</a>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="formDrawerOpen" :title="drawerTitle" width="520" destroy-on-close>
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="姓名" name="fullName">
          <a-input v-model:value="form.fullName" />
        </a-form-item>
        <a-form-item label="老人编号" name="elderCode">
          <a-input v-model:value="form.elderCode" />
        </a-form-item>
        <a-form-item label="护理等级">
          <a-input v-model:value="form.careLevel" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="formDrawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="submit">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-drawer v-model:open="detailDrawerOpen" title="老人详情" width="480">
      <a-descriptions bordered :column="1" size="small">
        <a-descriptions-item label="姓名">{{ current?.fullName }}</a-descriptions-item>
        <a-descriptions-item label="编号">{{ current?.elderCode }}</a-descriptions-item>
        <a-descriptions-item label="护理等级">{{ current?.careLevel || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ statusText(current?.status) }}</a-descriptions-item>
      </a-descriptions>
      <div style="margin-top: 16px;">
        <a-button type="primary" @click="openAssign">分配床位</a-button>
      </div>
    </a-drawer>

    <a-modal v-model:open="assignOpen" title="分配床位" @ok="submitAssign">
      <a-form layout="vertical">
        <a-form-item label="床位" required>
          <a-select v-model:value="assignForm.bedId" :options="bedOptions" />
        </a-form-item>
        <a-form-item label="开始日期" required>
          <a-date-picker v-model:value="assignForm.startDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="unbindOpen" title="退住/解绑" @ok="submitUnbind">
      <a-form layout="vertical">
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="unbindForm.endDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="原因">
          <a-select v-model:value="unbindForm.reason" allow-clear placeholder="请选择退住费用设置">
            <a-select-option v-for="item in dischargeFeeConfigOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../components/PageContainer.vue'
import SearchForm from '../components/SearchForm.vue'
import DataTable from '../components/DataTable.vue'
import { getBaseConfigItemList } from '../api/baseConfig'
import { getElderPage, createElder, updateElder, assignBed, unbindBed } from '../api/elder'
import { getBedList } from '../api/bed'
import type { BaseConfigItem, ElderItem, PageResult, BedItem } from '../types/api'

const query = reactive({ keyword: undefined as string | undefined, status: undefined as number | undefined, pageNo: 1, pageSize: 10 })
const list = ref<ElderItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const sorterState = reactive<{ field?: string; order?: string }>({})

const columns = [
  { title: '姓名', dataIndex: 'fullName', key: 'fullName', sorter: true, width: 140 },
  { title: '编号', dataIndex: 'elderCode', key: 'elderCode', width: 140 },
  { title: '护理等级', dataIndex: 'careLevel', key: 'careLevel', width: 120 },
  { title: '床位', dataIndex: 'bedId', key: 'bedId', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 240 }
]

const formDrawerOpen = ref(false)
const detailDrawerOpen = ref(false)
const assignOpen = ref(false)
const unbindOpen = ref(false)
const saving = ref(false)
const form = reactive<Partial<ElderItem>>({})
const current = ref<ElderItem | null>(null)
const beds = ref<BedItem[]>([])
const assignForm = reactive<{ elderId?: number; bedId?: number; startDate?: string }>({})
const unbindForm = reactive<{ elderId?: number; endDate?: string; reason?: string }>({})
const dischargeFeeConfigOptions = ref<Array<{ label: string; value: string }>>([])

const rules = {
  fullName: [{ required: true, message: '请输入姓名' }],
  elderCode: [{ required: true, message: '请输入编号' }],
  status: [{ required: true, message: '请选择状态' }]
}

const statusOptions = [
  { label: '在院', value: 1 },
  { label: '请假', value: 2 },
  { label: '离院', value: 3 }
]

const bedOptions = computed(() =>
  beds.value
    .filter((b) => (b.status === 1 || b.status === undefined) && !b.elderId)
    .map((b) => ({ label: `床位 ${b.bedNo} (房间 ${b.roomId})`, value: b.id }))
)

const drawerTitle = computed(() => (form.id ? '编辑老人' : '新增老人'))

async function loadDischargeFeeConfigOptions() {
  try {
    const options = await getBaseConfigItemList({ configGroup: 'DISCHARGE_FEE_CONFIG', status: 1 })
    dischargeFeeConfigOptions.value = (options || []).map((item: BaseConfigItem) => ({
      label: item.itemName,
      value: item.itemName
    }))
  } catch {
    dischargeFeeConfigOptions.value = []
  }
}

async function load() {
  loading.value = true
  try {
    const res: PageResult<ElderItem> = await getElderPage(query)
    {
      let records = res.list || []
      if (sorterState.field && sorterState.order) {
        const dir = sorterState.order === 'ascend' ? 1 : -1
        records = [...records].sort((a: any, b: any) => {
          const va = a[sorterState.field as keyof ElderItem]
          const vb = b[sorterState.field as keyof ElderItem]
          return (String(va || '').localeCompare(String(vb || ''))) * dir
        })
      }
      list.value = records
      pagination.total = res.total || records.length
      pagination.current = query.pageNo
      pagination.pageSize = query.pageSize
    }
    const bedRes: BedItem[] = await getBedList()
    beds.value = bedRes
  } catch {
    list.value = [
      { id: 1, fullName: '张三', elderCode: 'E1001', careLevel: 'A', status: 1 },
      { id: 2, fullName: '李四', elderCode: 'E1002', careLevel: 'B', status: 2 }
    ]
    pagination.total = list.value.length
    beds.value = [
      { id: 100, bedNo: '01', roomId: 10, status: 1 },
      { id: 101, bedNo: '02', roomId: 11, status: 1 }
    ]
  } finally {
    loading.value = false
  }
}

function onTableChange(pag: any, _filters: any, sorter: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  sorterState.field = sorter.field
  sorterState.order = sorter.order
  load()
}

function onSearch() {
  query.pageNo = 1
  pagination.current = 1
  load()
}

function onReset() {
  query.pageNo = 1
  pagination.current = 1
  load()
}

function openDrawer(record?: ElderItem) {
  Object.assign(form, record || { fullName: '', elderCode: '', careLevel: '', status: 1 })
  formDrawerOpen.value = true
}

function openDetail(record: ElderItem) {
  current.value = record
  detailDrawerOpen.value = true
}

function openAssign(record?: ElderItem) {
  const target = record || current.value
  if (!target) return
  assignForm.elderId = target.id
  assignForm.bedId = undefined
  assignForm.startDate = dayjs().format('YYYY-MM-DD')
  assignOpen.value = true
}

function openUnbind(record: ElderItem) {
  unbindForm.elderId = record.id
  unbindForm.endDate = dayjs().format('YYYY-MM-DD')
  unbindForm.reason = ''
  unbindOpen.value = true
}

async function submit() {
  saving.value = true
  try {
    if (form.id) {
      await updateElder(form.id, form)
    } else {
      await createElder(form)
    }
    message.success('保存成功')
    formDrawerOpen.value = false
    load()
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function submitAssign() {
  if (!assignForm.elderId || !assignForm.bedId || !assignForm.startDate) {
    message.warning('请选择床位和日期')
    return
  }
  try {
    await assignBed(assignForm.elderId, assignForm.bedId, assignForm.startDate)
    message.success('已分配床位')
    assignOpen.value = false
    load()
  } catch (err: any) {
    message.error(err?.response?.data?.message || '分配失败')
  }
}

async function submitUnbind() {
  if (!unbindForm.elderId) return
  try {
    await unbindBed(unbindForm.elderId, unbindForm.endDate, unbindForm.reason)
    message.success('已退住/解绑')
    unbindOpen.value = false
    load()
  } catch (err: any) {
    message.error(err?.response?.data?.message || '解绑失败')
  }
}

function statusText(status?: number) {
  if (status === 1) return '在院'
  if (status === 2) return '请假'
  if (status === 3) return '离院'
  return '-'
}

loadDischargeFeeConfigOptions()
load()
</script>
