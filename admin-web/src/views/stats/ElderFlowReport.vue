<template>
  <PageContainer title="老人出入报表" subTitle="老人入院/离院明细查询">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline">
        <a-form-item label="开始日期">
          <a-date-picker v-model:value="query.fromDate" style="width: 150px" />
        </a-form-item>
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="query.toDate" style="width: 150px" />
        </a-form-item>
        <a-form-item label="事件类型">
          <a-select v-model:value="query.eventType" allow-clear style="width: 140px">
            <a-select-option value="ADMISSION">入院</a-select-option>
            <a-select-option value="DISCHARGE">离院</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="机构ID">
          <a-input-number v-model:value="query.orgId" :min="1" placeholder="默认当前机构" style="width: 160px" />
        </a-form-item>
        <a-form-item label="打印老人(院内)">
          <a-select
            v-model:value="printElderId"
            show-search
            allow-clear
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            placeholder="请选择院内老人"
            style="width: 180px"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
            @change="onPrintElderChange"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="search">查询</a-button>
            <a-button @click="exportCsvReport">导出报表</a-button>
            <a-button @click="openColumnSetting">列设置</a-button>
            <a-button :disabled="!printRows.length" @click="printCurrent">打印当前列</a-button>
            <a-button :disabled="!canPrintSpecific" @click="printSpecificElder">打印指定老人</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <vxe-table border stripe show-overflow :data="rows" height="420">
        <vxe-column field="eventDate" title="日期" width="140" />
        <vxe-column field="eventType" title="事件类型" width="120">
          <template #default="{ row }">
            <a-tag :color="row.eventType === 'ADMISSION' ? 'blue' : 'orange'">
              {{ row.eventType === 'ADMISSION' ? '入院' : '离院' }}
            </a-tag>
          </template>
        </vxe-column>
        <vxe-column field="elderName" title="老人姓名" min-width="160">
          <template #default="{ row }">
            {{ row.elderName || '未知老人' }}
          </template>
        </vxe-column>
        <vxe-column field="remark" title="备注" min-width="260" />
      </vxe-table>
      <div style="margin-top: 12px; text-align: right;">
        <a-pagination
          v-model:current="query.pageNo"
          v-model:page-size="query.pageSize"
          :total="total"
          show-size-changer
          :page-size-options="['10', '20', '50', '100']"
          @change="loadData"
          @show-size-change="onPageSizeChange"
        />
      </div>
    </a-card>

    <a-modal v-model:open="columnSettingOpen" title="打印列设置" @ok="columnSettingOpen = false" cancel-text="关闭" ok-text="确定">
      <a-checkbox-group v-model:value="selectedPrintColumns" :options="printColumnOptions" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { exportElderFlowReportCsv, getElderFlowReport } from '../../api/stats'
import type { FlowReportItem } from '../../types'
import { message, Modal } from 'ant-design-vue'
import { printTableReport } from '../../utils/print'
import { useElderOptions } from '../../composables/useElderOptions'

const query = ref({
  fromDate: dayjs().subtract(29, 'day') as Dayjs,
  toDate: dayjs() as Dayjs,
  eventType: undefined as 'ADMISSION' | 'DISCHARGE' | undefined,
  orgId: undefined as number | undefined,
  pageNo: 1,
  pageSize: 20
})

const rows = ref<FlowReportItem[]>([])
const total = ref(0)
const printElderId = ref<number | undefined>(undefined)
const columnSettingOpen = ref(false)
const printColumnOptions = [
  { label: '日期', value: 'eventDate' },
  { label: '事件类型', value: 'eventTypeLabel' },
  { label: '老人姓名', value: 'elderName' },
  { label: '备注', value: 'remark' }
]
const selectedPrintColumns = ref<string[]>(['eventDate', 'eventTypeLabel', 'elderName', 'remark'])
const { elderOptions, elderLoading, searchElders } = useElderOptions({ pageSize: 100, inHospitalOnly: true })
const LARGE_PRINT_ROW_THRESHOLD = 200
const printRows = computed(() => rows.value.map(item => ({
  elderId: item.elderId,
  eventDate: item.eventDate || '',
  eventTypeLabel: item.eventType === 'ADMISSION' ? '入院' : '离院',
  elderName: item.elderName || '未知老人',
  remark: item.remark || ''
})))
const selectedPrintElder = computed(() => elderOptions.value.find(item => Number(item.value) === Number(printElderId.value)))
const canPrintSpecific = computed(() => !!printRows.value.length && !!printElderId.value)

async function loadData() {
  if (query.value.fromDate.isAfter(query.value.toDate, 'day')) {
    message.warning('开始日期不能晚于结束日期')
    return
  }
  try {
    const res = await getElderFlowReport({
      fromDate: dayjs(query.value.fromDate).format('YYYY-MM-DD'),
      toDate: dayjs(query.value.toDate).format('YYYY-MM-DD'),
      eventType: query.value.eventType,
      pageNo: query.value.pageNo,
      pageSize: query.value.pageSize,
      orgId: query.value.orgId
    })
    rows.value = res.list || []
    total.value = Number(res.total || 0)
  } catch (error: any) {
    message.error(error?.message || '加载老人出入报表失败')
  }
}

function search() {
  query.value.pageNo = 1
  loadData()
}

function reset() {
  query.value.fromDate = dayjs().subtract(29, 'day')
  query.value.toDate = dayjs()
  query.value.eventType = undefined
  query.value.orgId = undefined
  query.value.pageNo = 1
  query.value.pageSize = 20
  printElderId.value = undefined
  loadData()
}

function onPageSizeChange(_: number, pageSize: number) {
  query.value.pageNo = 1
  query.value.pageSize = pageSize
  loadData()
}

function openColumnSetting() {
  columnSettingOpen.value = true
}

async function exportCsvReport() {
  if (query.value.fromDate.isAfter(query.value.toDate, 'day')) {
    message.warning('开始日期不能晚于结束日期')
    return
  }
  try {
    await exportElderFlowReportCsv({
      fromDate: dayjs(query.value.fromDate).format('YYYY-MM-DD'),
      toDate: dayjs(query.value.toDate).format('YYYY-MM-DD'),
      eventType: query.value.eventType,
      orgId: query.value.orgId
    })
    message.success('报表导出成功')
  } catch (error: any) {
    message.error(error?.message || '报表导出失败')
  }
}

function printCurrent() {
  renderPrint('老人出入报表（当前结果）', printRows.value, '当前查询结果')
}

function printSpecificElder() {
  let filtered = printRows.value
  let title = '老人出入报表（指定老人）'
  let printScope = '当前查询结果'

  if (printElderId.value) {
    filtered = filtered.filter(item => Number(item.elderId) === Number(printElderId.value))
    const selectedName = selectedPrintElder.value?.name || `老人ID:${printElderId.value}`
    title = `老人出入报表（${selectedName} / ID:${printElderId.value}）`
    printScope = `指定老人：${selectedName} / ID:${printElderId.value}`
  } else {
    message.warning('请选择院内老人')
    return
  }

  if (!filtered.length) {
    message.warning('未找到匹配老人记录')
    return
  }
  renderPrint(title, filtered, printScope)
}

function onPrintElderChange(value?: number) {
  printElderId.value = value
}

async function renderPrint(title: string, data: Array<Record<string, any>>, printScope: string) {
  if (!selectedPrintColumns.value.length) {
    message.warning('请至少选择一列打印')
    return
  }
  if (data.length > LARGE_PRINT_ROW_THRESHOLD) {
    const confirmed = await confirmLargePrint(data.length)
    if (!confirmed) {
      return
    }
  }
  const headers = printColumnOptions.filter(item => selectedPrintColumns.value.includes(item.value))
  try {
    printTableReport({
      title,
      subtitle: `${dayjs(query.value.fromDate).format('YYYY-MM-DD')} ~ ${dayjs(query.value.toDate).format('YYYY-MM-DD')}；打印范围：${printScope}；记录数：${data.length}`,
      columns: headers.map(item => ({ key: item.value, title: item.label })),
      rows: data
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

function confirmLargePrint(rowCount: number) {
  return new Promise<boolean>((resolve) => {
    Modal.confirm({
      title: '打印确认',
      content: `本次将打印 ${rowCount} 条记录，可能耗时较长，是否继续？`,
      okText: '继续打印',
      cancelText: '取消',
      onOk: () => resolve(true),
      onCancel: () => resolve(false)
    })
  })
}

onMounted(loadData)
</script>
