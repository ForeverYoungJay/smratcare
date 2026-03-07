<template>
  <PageContainer title="月分摊费" subTitle="按月份建立分摊项目（自动确认）">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="月份">
        <a-date-picker v-model:value="query.month" picker="month" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 180px" :options="statusOptions" />
      </a-form-item>
      <a-form-item label="老人">
        <a-select
          v-model:value="query.elderId"
          show-search
          allow-clear
          :filter-option="false"
          :options="elderOptions"
          :loading="elderLoading"
          placeholder="按老人筛选分摊记录"
          style="width: 220px"
          @search="searchElders"
        />
      </a-form-item>
      <a-form-item label="打印备注">
        <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：月度电费分摊核对" style="width: 220px" />
      </a-form-item>
      <template #extra>
        <a-button @click="exportCurrent">导出当前</a-button>
        <a-button @click="printCurrent">打印当前</a-button>
        <a-button @click="openMeterValidate">抄表校验</a-button>
        <a-button type="primary" @click="openCreate">新建分摊</a-button>
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
        <template v-if="column.key === 'avgAmount'">
          {{ resolveAvgAmount(record) }}
        </template>
        <template v-else-if="column.key === 'elderIds'">
          {{ formatElderLabels(record.elderIds, record.elderSnapshotJson) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-popconfirm
            v-if="record.status === 'CONFIRMED'"
            title="确认回滚该分摊？将删除该次自动入账流水"
            @confirm="rollbackAllocation(record)"
          >
            <a-button type="link" danger>回滚</a-button>
          </a-popconfirm>
          <a-tag v-else>{{ record.status || '-' }}</a-tag>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="createOpen" title="新建月分摊" :confirm-loading="creating" @ok="submitCreate">
      <a-form layout="vertical">
        <a-form-item label="分摊月份" required>
          <a-date-picker v-model:value="createForm.allocationMonth" picker="month" style="width: 100%" />
        </a-form-item>
        <a-form-item label="分摊项目" required>
          <a-input v-model:value="createForm.allocationName" />
        </a-form-item>
        <a-form-item label="分摊总金额" required>
          <a-input-number v-model:value="createForm.totalAmount" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="房间定位选人">
          <a-row :gutter="8">
            <a-col :span="8">
              <a-select
                v-model:value="createForm.building"
                allow-clear
                show-search
                :options="buildingOptions"
                placeholder="选择楼栋（如情感楼）"
                @change="onBuildingChange"
              />
            </a-col>
            <a-col :span="8">
              <a-select
                v-model:value="createForm.floorNo"
                allow-clear
                show-search
                :options="floorOptions"
                placeholder="选择楼层"
                @change="onFloorChange"
              />
            </a-col>
            <a-col :span="8">
              <a-select
                v-model:value="createForm.roomNo"
                allow-clear
                show-search
                :options="roomOptions"
                placeholder="选择房间"
                @change="applyRoomSelection"
              />
            </a-col>
          </a-row>
          <div class="form-tip">选择楼栋/楼层/房间后，自动带出该房间在住老人用于分摊</div>
        </a-form-item>
        <a-form-item label="分摊老人" required>
          <a-select
            v-model:value="createForm.elderIds"
            mode="multiple"
            show-search
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            placeholder="请选择参与分摊的老人（可多选）"
            style="width: 100%"
            @search="searchElders"
          />
          <div class="form-tip">{{ selectedEldersText }}</div>
        </a-form-item>
        <a-form-item label="自动核算人数">
          <a-input :value="selectedCount" readonly />
        </a-form-item>
        <a-form-item label="自动核算人均金额">
          <a-input :value="createPreviewAvg" readonly />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="createForm.remark" placeholder="可填写分摊说明，会自动写入每位老人月消费明细" />
        </a-form-item>
        <a-form-item label="分摊预览">
          <a-space>
            <a-button :loading="previewLoading" @click="openPreview">预览分摊明细</a-button>
            <span class="form-tip">建议先预览后确认创建</span>
          </a-space>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="previewOpen" title="分摊预览" :footer="null" width="860">
      <a-descriptions :column="4" size="small" bordered>
        <a-descriptions-item label="分摊月份">{{ previewData?.allocationMonth || '-' }}</a-descriptions-item>
        <a-descriptions-item label="分摊项目">{{ previewData?.allocationName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="总金额">{{ Number(previewData?.totalAmount || 0).toFixed(2) }}</a-descriptions-item>
        <a-descriptions-item label="人均">{{ Number(previewData?.avgAmount || 0).toFixed(2) }}</a-descriptions-item>
      </a-descriptions>
      <vxe-table border stripe show-overflow :data="previewData?.rows || []" height="360" style="margin-top: 12px;">
        <vxe-column field="elderId" title="老人ID" width="140" />
        <vxe-column field="elderName" title="老人姓名" min-width="220" />
        <vxe-column field="amount" title="分摊金额" width="160" />
      </vxe-table>
    </a-modal>

    <a-modal
      v-model:open="meterOpen"
      title="月抄表数据校验"
      :confirm-loading="meterLoading"
      width="920"
      @ok="submitMeterValidate"
    >
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="月份">
              <a-date-picker v-model:value="meterForm.month" picker="month" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="异常波动阈值（度）">
              <a-input-number v-model:value="meterForm.abnormalThreshold" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="抄表数据（每行：房间号,上期读数,本期读数,楼栋,楼层,备注）">
          <a-textarea
            v-model:value="meterForm.rawText"
            :rows="7"
            placeholder="示例：A101,100,130,1号楼,1F,夜班抄表"
          />
          <div class="form-tip">支持英文逗号、中文逗号、Tab 分隔；至少填写房间号/上期/本期。</div>
        </a-form-item>
      </a-form>
      <a-alert
        v-if="meterResult"
        style="margin-bottom: 12px"
        type="info"
        show-icon
        :message="`总行数 ${meterResult.totalRows}，有效 ${meterResult.validRows}，异常 ${meterResult.invalidRows}，预警 ${meterResult.warningRows}`"
      />
      <DataTable
        v-if="meterResult"
        rowKey="rowNo"
        size="small"
        :pagination="false"
        :data-source="meterResult.rows"
        :columns="meterColumns"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'level'">
            <a-tag :color="record.level === 'OK' ? 'green' : record.level === 'WARN' ? 'orange' : 'red'">
              {{ record.level }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'usage'">
            {{ record.usage ?? '--' }}
          </template>
        </template>
      </DataTable>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import {
  createMonthlyAllocation,
  getMonthlyAllocationPage,
  previewMonthlyAllocation,
  rollbackMonthlyAllocation
} from '../../api/financeFee'
import { validateFinanceAllocationMeter } from '../../api/finance'
import { getRoomList } from '../../api/bed'
import { getElderPage } from '../../api/elder'
import type { ElderItem, MonthlyAllocationItem, PageResult } from '../../types'
import { exportCsv } from '../../utils/export'
import { printTableReport } from '../../utils/print'
import { useElderOptions } from '../../composables/useElderOptions'

const loading = ref(false)
const rows = ref<MonthlyAllocationItem[]>([])
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  month: undefined as any,
  status: undefined as string | undefined,
  elderId: undefined as number | undefined,
  printRemark: ''
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const statusOptions = [
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已取消', value: 'CANCELLED' }
]

const columns = [
  { title: '月份', dataIndex: 'allocationMonth', key: 'allocationMonth', width: 120 },
  { title: '分摊项目', dataIndex: 'allocationName', key: 'allocationName', width: 160 },
  { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120 },
  { title: '分摊老人', key: 'elderIds', width: 220 },
  { title: '目标人数', dataIndex: 'targetCount', key: 'targetCount', width: 100 },
  { title: '人均金额', key: 'avgAmount', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 110, fixed: 'right' }
]

const createOpen = ref(false)
const creating = ref(false)
const previewOpen = ref(false)
const previewLoading = ref(false)
const previewData = ref<any>(null)
const createForm = reactive({
  allocationMonth: undefined as any,
  allocationName: '',
  totalAmount: 0,
  building: undefined as string | undefined,
  floorNo: undefined as string | undefined,
  roomNo: undefined as string | undefined,
  elderIds: [] as number[],
  remark: ''
})
const residentPool = ref<ElderItem[]>([])
const roomPool = ref<Array<{ roomNo: string; building?: string; floorNo?: string }>>([])
const meterOpen = ref(false)
const meterLoading = ref(false)
const meterForm = reactive({
  month: dayjs().startOf('month') as any,
  abnormalThreshold: 500,
  rawText: ''
})
const meterResult = ref<any>(null)
const meterColumns = [
  { title: '#', dataIndex: 'rowNo', key: 'rowNo', width: 56 },
  { title: '楼栋', dataIndex: 'building', key: 'building', width: 90 },
  { title: '楼层', dataIndex: 'floorNo', key: 'floorNo', width: 90 },
  { title: '房间号', dataIndex: 'roomNo', key: 'roomNo', width: 100 },
  { title: '上期', dataIndex: 'previousReading', key: 'previousReading', width: 90 },
  { title: '本期', dataIndex: 'currentReading', key: 'currentReading', width: 90 },
  { title: '用量', key: 'usage', width: 90 },
  { title: '级别', key: 'level', width: 90 },
  { title: '原因', dataIndex: 'message', key: 'message' }
]

const { elderOptions, elderLoading, searchElders, findElderName } = useElderOptions({ pageSize: 80 })
const buildingOptions = computed(() => {
  const set = new Set<string>()
  roomPool.value.forEach(item => {
    const value = String(item.building || '').trim()
    if (value) set.add(value)
  })
  return Array.from(set)
    .sort((a, b) => a.localeCompare(b, 'zh-CN'))
    .map(value => ({ label: value, value }))
})
const floorOptions = computed(() => {
  const set = new Set<string>()
  roomPool.value
    .filter(item => !createForm.building || item.building === createForm.building)
    .forEach(item => {
      const value = String(item.floorNo || '').trim()
      if (value) set.add(value)
    })
  return Array.from(set)
    .sort((a, b) => a.localeCompare(b, 'zh-CN'))
    .map(value => ({ label: value, value }))
})
const roomOptions = computed(() => {
  return roomPool.value
    .filter(item => !createForm.building || item.building === createForm.building)
    .filter(item => !createForm.floorNo || item.floorNo === createForm.floorNo)
    .map(item => String(item.roomNo || '').trim())
    .filter(Boolean)
    .sort((a, b) => a.localeCompare(b, 'zh-CN'))
    .map(value => ({ label: value, value }))
})
const selectedEldersText = computed(() => {
  if (!createForm.elderIds.length) return '请至少选择一位老人'
  const idSet = new Set(createForm.elderIds.map(id => Number(id)))
  const labels = residentPool.value
    .filter(item => idSet.has(Number(item.id)))
    .map(item => {
      const roomNo = String(item.roomNo || '').trim()
      return roomNo ? `${item.fullName}（${roomNo}）` : item.fullName
    })
  if (!labels.length) {
    return `已选 ${createForm.elderIds.length} 位老人`
  }
  return `已选：${labels.join('、')}`
})

const selectedCount = computed(() => createForm.elderIds.length)
const createPreviewAvg = computed(() => {
  if (!selectedCount.value) {
    return '--'
  }
  return (Number(createForm.totalAmount || 0) / selectedCount.value).toFixed(2)
})

function parseElderIds(raw?: string) {
  if (!raw) return [] as number[]
  return String(raw)
    .split(',')
    .map(part => Number(part.trim()))
    .filter(id => Number.isFinite(id) && id > 0)
}

function formatElderLabels(raw?: string, snapshotJson?: string) {
  if (snapshotJson) {
    try {
      const rows = JSON.parse(snapshotJson)
      if (Array.isArray(rows) && rows.length) {
        const names = rows
          .map(item => String(item?.elderName || '').trim())
          .filter(Boolean)
        if (names.length) {
          return names.join('、')
        }
      }
    } catch (_) {}
  }
  const ids = parseElderIds(raw)
  if (!ids.length) {
    return '--'
  }
  return ids.map(id => findElderName(id) || `#${id}`).join('、')
}

function resolveAvgAmount(record: MonthlyAllocationItem) {
  if (typeof record.avgAmount === 'number' && Number.isFinite(record.avgAmount)) {
    return Number(record.avgAmount).toFixed(2)
  }
  if (!record.targetCount || record.targetCount <= 0) {
    return '--'
  }
  return (Number(record.totalAmount || 0) / record.targetCount).toFixed(2)
}

async function ensureResidentPool() {
  if (residentPool.value.length) return
  const page = await getElderPage({
    pageNo: 1,
    pageSize: 500,
    status: 1,
    elderStatus: 1
  })
  residentPool.value = page.list || []
}

async function ensureRoomPool() {
  if (roomPool.value.length) return
  const rows = await getRoomList()
  roomPool.value = (rows || []).map(item => ({
    roomNo: String(item.roomNo || '').trim(),
    building: String(item.building || '').trim() || undefined,
    floorNo: String(item.floorNo || '').trim() || undefined
  }))
}

async function onBuildingChange() {
  createForm.floorNo = undefined
  createForm.roomNo = undefined
  createForm.elderIds = []
}

async function onFloorChange() {
  createForm.roomNo = undefined
  createForm.elderIds = []
}

async function applyRoomSelection() {
  await ensureResidentPool()
  if (!createForm.roomNo) return
  const ids = residentPool.value
    .filter(item => String(item.roomNo || '').trim() === String(createForm.roomNo || '').trim())
    .map(item => Number(item.id))
    .filter(id => Number.isFinite(id) && id > 0)
  createForm.elderIds = Array.from(new Set(ids))
  if (!createForm.elderIds.length) {
    message.warning('该房间暂无可分摊老人，请重新选择')
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<MonthlyAllocationItem> = await getMonthlyAllocationPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      month: query.month ? dayjs(query.month).format('YYYY-MM') : undefined,
      status: query.status,
      elderId: query.elderId
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.pageNo = 1
  query.month = undefined
  query.status = undefined
  query.elderId = undefined
  query.printRemark = ''
  pagination.current = 1
  fetchData()
}

function handleTableChange(pag: any) {
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

function openCreate() {
  createForm.allocationMonth = dayjs().startOf('month')
  createForm.allocationName = ''
  createForm.totalAmount = 0
  createForm.building = undefined
  createForm.floorNo = undefined
  createForm.roomNo = undefined
  createForm.elderIds = []
  createForm.remark = ''
  previewData.value = null
  createOpen.value = true
  ensureResidentPool().catch(() => {})
  ensureRoomPool().catch(() => {})
  searchElders('').catch(() => {})
}

async function openPreview() {
  if (createForm.totalAmount < 0) {
    message.error('金额不能小于0')
    return
  }
  if (!createForm.elderIds.length) {
    message.error('请至少选择一位分摊老人')
    return
  }
  previewLoading.value = true
  try {
    previewData.value = await previewMonthlyAllocation({
      allocationMonth: createForm.allocationMonth ? dayjs(createForm.allocationMonth).format('YYYY-MM') : undefined,
      allocationName: createForm.allocationName || undefined,
      totalAmount: Number(createForm.totalAmount || 0),
      elderIds: [...createForm.elderIds],
      remark: createForm.remark || undefined
    })
    previewOpen.value = true
  } finally {
    previewLoading.value = false
  }
}

async function submitCreate() {
  if (!createForm.allocationMonth) {
    message.error('请选择月份')
    return
  }
  if (!createForm.allocationName) {
    message.error('请输入分摊项目')
    return
  }
  if (createForm.totalAmount < 0) {
    message.error('金额不能小于0')
    return
  }
  if (!createForm.elderIds.length) {
    message.error('请至少选择一位分摊老人')
    return
  }
  creating.value = true
  try {
    await createMonthlyAllocation({
      allocationMonth: dayjs(createForm.allocationMonth).format('YYYY-MM'),
      allocationName: createForm.allocationName,
      totalAmount: createForm.totalAmount,
      targetCount: createForm.elderIds.length,
      elderIds: [...createForm.elderIds],
      remark: createForm.remark || undefined
    })
    message.success('创建成功，已自动写入老人月消费明细')
    createOpen.value = false
    fetchData()
  } finally {
    creating.value = false
  }
}

async function rollbackAllocation(record: MonthlyAllocationItem) {
  await rollbackMonthlyAllocation(Number(record.id), '运营手工回滚')
  message.success('已回滚该分摊记录')
  fetchData()
}

function openMeterValidate() {
  meterForm.month = dayjs().startOf('month')
  meterForm.abnormalThreshold = 500
  meterForm.rawText = ''
  meterResult.value = null
  meterOpen.value = true
}

function parseMeterText(rawText: string) {
  return (rawText || '')
    .split('\n')
    .map(line => line.trim())
    .filter(Boolean)
    .map(line => {
      const parts = line
        .replace(/，/g, ',')
        .split(/[\t,]/)
        .map(part => part.trim())
      const previousText = parts[1] || ''
      const currentText = parts[2] || ''
      return {
        roomNo: parts[0] || '',
        previousReading: previousText === '' ? (undefined as any) : Number(previousText),
        currentReading: currentText === '' ? (undefined as any) : Number(currentText),
        building: parts[3] || undefined,
        floorNo: parts[4] || undefined,
        remark: parts[5] || undefined
      }
    })
}

async function submitMeterValidate() {
  const rows = parseMeterText(meterForm.rawText)
  if (!rows.length) {
    message.error('请先粘贴抄表数据')
    return
  }
  if (rows.some(item => !item.roomNo)) {
    message.error('房间号不能为空')
    return
  }
  meterLoading.value = true
  try {
    meterResult.value = await validateFinanceAllocationMeter({
      month: meterForm.month ? dayjs(meterForm.month).format('YYYY-MM') : undefined,
      abnormalThreshold: Number(meterForm.abnormalThreshold || 0),
      rows
    })
    if (Number(meterResult.value?.invalidRows || 0) > 0) {
      message.warning('校验完成，存在错误行')
    } else if (Number(meterResult.value?.warningRows || 0) > 0) {
      message.warning('校验完成，存在预警行')
    } else {
      message.success('校验通过')
    }
  } finally {
    meterLoading.value = false
  }
}

function exportCurrent() {
  exportCsv(
    rows.value.map(item => ({
      月份: item.allocationMonth || '',
      分摊项目: item.allocationName || '',
      总金额: Number(item.totalAmount || 0).toFixed(2),
      分摊老人: formatElderLabels(item.elderIds, item.elderSnapshotJson),
      目标人数: item.targetCount || 0,
      人均金额: resolveAvgAmount(item),
      状态: item.status || '',
      备注: item.remark || '',
      创建时间: item.createTime || ''
    })),
    `月分摊费-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  )
}

function printCurrent() {
  try {
    printTableReport({
      title: '月分摊费',
      subtitle: `月份：${query.month ? dayjs(query.month).format('YYYY-MM') : '全部'}；状态：${query.status || '全部'}；老人：${query.elderId ? (findElderName(query.elderId) || `#${query.elderId}`) : '全部'}；备注：${query.printRemark || '-'}`,
      columns: [
        { key: 'allocationMonth', title: '月份' },
        { key: 'allocationName', title: '分摊项目' },
        { key: 'totalAmount', title: '总金额' },
        { key: 'elderIds', title: '分摊老人' },
        { key: 'targetCount', title: '目标人数' },
        { key: 'avgAmount', title: '人均金额' },
        { key: 'status', title: '状态' },
        { key: 'remark', title: '备注' },
        { key: 'createTime', title: '创建时间' }
      ],
      rows: rows.value.map(item => ({
        allocationMonth: item.allocationMonth || '-',
        allocationName: item.allocationName || '-',
        totalAmount: Number(item.totalAmount || 0).toFixed(2),
        elderIds: formatElderLabels(item.elderIds, item.elderSnapshotJson),
        targetCount: item.targetCount || 0,
        avgAmount: resolveAvgAmount(item),
        status: item.status || '-',
        remark: item.remark || '-',
        createTime: item.createTime || '-'
      })),
      signatures: ['制表', '复核', '审批']
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

fetchData()
searchElders('').catch(() => {})
</script>

<style scoped>
.form-tip {
  margin-top: 6px;
  color: #8c8c8c;
  font-size: 12px;
  line-height: 1.5;
}
</style>
