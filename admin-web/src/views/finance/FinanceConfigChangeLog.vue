<template>
  <PageContainer title="配置变更记录" subTitle="查看财务配置变更轨迹（谁在何时改了什么）">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query">
        <a-form-item label="开始日期">
          <a-date-picker v-model:value="query.from" style="width: 150px" />
        </a-form-item>
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="query.to" style="width: 150px" />
        </a-form-item>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" allow-clear placeholder="操作人/动作/详情" style="width: 220px" />
        </a-form-item>
        <a-form-item label="动作类型">
          <a-select v-model:value="query.actionType" allow-clear style="width: 180px">
            <a-select-option value="FIN_BILLING_CONFIG_CREATE">新增</a-select-option>
            <a-select-option value="FIN_BILLING_CONFIG_UPDATE">更新</a-select-option>
            <a-select-option value="FIN_BILLING_CONFIG_ROLLBACK">回滚</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="风险等级">
          <a-select v-model:value="query.riskLevel" allow-clear style="width: 140px">
            <a-select-option value="HIGH">高风险</a-select-option>
            <a-select-option value="MEDIUM">中风险</a-select-option>
            <a-select-option value="LOW">低风险</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：配置审计版" style="width: 180px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button @click="exportData">导出</a-button>
            <a-button @click="printCurrent">打印当前</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="[12, 12]" style="margin-top: 16px;">
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="变更记录数" :value="filteredRows.length" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="可回滚数" :value="rollbackableCount" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="高风险变更" :value="highRiskCount" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="最近7天变更" :value="recent7dCount" /></a-card></a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <vxe-table border stripe show-overflow :loading="loading" :data="filteredRows" height="560">
        <vxe-column field="createTime" title="时间" width="180" />
        <vxe-column field="actorName" title="操作人" width="140">
          <template #default="{ row }">{{ row.actorName || '-' }}</template>
        </vxe-column>
        <vxe-column field="actionType" title="动作" width="220">
          <template #default="{ row }">
            <a-tag color="blue">{{ row.actionType }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column title="风险等级" width="100">
          <template #default="{ row }">
            <a-tag :color="riskColor(row)">{{ riskText(row) }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="entityType" title="实体类型" width="140" />
        <vxe-column field="entityId" title="实体ID" width="140" />
        <vxe-column field="detail" title="变更详情" min-width="300" />
        <vxe-column title="操作" width="160" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a-button type="link" @click="rollback(row)">回滚</a-button>
            </a-space>
          </template>
        </vxe-column>
      </vxe-table>

      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getFinanceConfigChangeLogPage, rollbackFinanceBillingConfig } from '../../api/finance'
import type { FinanceConfigChangeLogItem, PageResult } from '../../types'
import { exportCsv } from '../../utils/export'
import { printTableReport } from '../../utils/print'
import { confirmAction } from '../../utils/actionConfirm'

const loading = ref(false)
const rows = ref<FinanceConfigChangeLogItem[]>([])
const total = ref(0)

const query = reactive({
  from: dayjs().subtract(30, 'day'),
  to: dayjs(),
  keyword: '',
  actionType: undefined as string | undefined,
  riskLevel: undefined as 'HIGH' | 'MEDIUM' | 'LOW' | undefined,
  printRemark: '',
  pageNo: 1,
  pageSize: 20
})
const filteredRows = computed(() => (rows.value || [])
  .filter(item => !query.actionType || String(item.actionType || '') === query.actionType)
  .filter(item => !query.riskLevel || riskText(item) === (query.riskLevel === 'HIGH' ? '高' : query.riskLevel === 'MEDIUM' ? '中' : '低')))
const rollbackableCount = computed(() => filteredRows.value.filter(item => String(item.actionType || '').startsWith('FIN_BILLING_CONFIG_')).length)
const highRiskCount = computed(() => filteredRows.value.filter(item => riskText(item) === '高').length)
const recent7dCount = computed(() => filteredRows.value.filter(item => {
  const time = item.createTime ? dayjs(item.createTime) : null
  return time ? time.isAfter(dayjs().subtract(7, 'day')) : false
}).length)

async function loadData() {
  loading.value = true
  try {
    const result: PageResult<FinanceConfigChangeLogItem> = await getFinanceConfigChangeLogPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      from: query.from ? dayjs(query.from).format('YYYY-MM-DD') : undefined,
      to: query.to ? dayjs(query.to).format('YYYY-MM-DD') : undefined,
      keyword: query.keyword || undefined
    })
    rows.value = result.list || []
    total.value = Number(result.total || 0)
  } finally {
    loading.value = false
  }
}

function reset() {
  query.from = dayjs().subtract(30, 'day')
  query.to = dayjs()
  query.keyword = ''
  query.actionType = undefined
  query.riskLevel = undefined
  query.printRemark = ''
  query.pageNo = 1
  loadData()
}

function onPageChange(page: number) {
  query.pageNo = page
  loadData()
}

function onPageSizeChange(_: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  loadData()
}

onMounted(loadData)

async function rollback(row: FinanceConfigChangeLogItem) {
  if (!row?.id) return
  try {
    const confirmed = await confirmAction({
      title: '确认回滚该配置变更？',
      content: `${row.actionType || '-'} / ${row.detail || '-'}`,
      impactItems: ['将按变更记录反向写入配置', '可能影响计费和分摊口径'],
      okText: '确认回滚',
      danger: true
    })
    if (!confirmed) return
    await rollbackFinanceBillingConfig({ logId: Number(row.id) })
    message.success('回滚成功')
    loadData()
  } catch (error: any) {
    message.error(error?.message || '回滚失败')
  }
}

function riskText(item: FinanceConfigChangeLogItem) {
  const action = String(item.actionType || '').toUpperCase()
  const detail = String(item.detail || '')
  if (action.includes('ROLLBACK')) return '高'
  if (detail.includes('->') && /[0-9]/.test(detail)) return '中'
  return '低'
}

function riskColor(item: FinanceConfigChangeLogItem) {
  const level = riskText(item)
  if (level === '高') return 'red'
  if (level === '中') return 'orange'
  return 'green'
}

function exportData() {
  exportCsv(
    filteredRows.value.map(item => ({
      时间: item.createTime || '',
      操作人: item.actorName || '',
      动作: item.actionType || '',
      风险等级: riskText(item),
      实体类型: item.entityType || '',
      实体ID: item.entityId || '',
      详情: item.detail || ''
    })),
    `财务配置变更记录-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  )
}

function printCurrent() {
  try {
    printTableReport({
      title: '财务配置变更记录',
      subtitle: `${dayjs(query.from).format('YYYY-MM-DD')} ~ ${dayjs(query.to).format('YYYY-MM-DD')}；关键字：${query.keyword || '-'}；备注：${query.printRemark || '-'}`,
      columns: [
        { key: 'createTime', title: '时间' },
        { key: 'actorName', title: '操作人' },
        { key: 'actionType', title: '动作' },
        { key: 'riskLevel', title: '风险等级' },
        { key: 'entityType', title: '实体类型' },
        { key: 'entityId', title: '实体ID' },
        { key: 'detail', title: '变更详情' }
      ],
      rows: filteredRows.value.map(item => ({
        createTime: item.createTime || '-',
        actorName: item.actorName || '-',
        actionType: item.actionType || '-',
        riskLevel: riskText(item),
        entityType: item.entityType || '-',
        entityId: item.entityId || '-',
        detail: item.detail || '-'
      }))
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}
</script>
