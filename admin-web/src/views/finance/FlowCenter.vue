<template>
  <PageContainer :title="props.moduleName" :subTitle="props.description || '业务动作自动入账，实时查看金额与异常'">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query">
        <a-form-item label="日期范围">
          <a-range-picker v-model:value="query.range" style="width: 260px" />
        </a-form-item>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" allow-clear placeholder="老人/来源/备注" style="width: 200px" />
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear style="width: 180px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" :loading="loading" @click="loadData">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button @click="exportCurrent">导出CSV</a-button>
            <a-button @click="printCurrent">打印当前</a-button>
            <a-button v-for="item in props.links" :key="item.to" @click="go(item.to)">{{ item.label }}</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <StatefulBlock style="margin-top: 16px;" :loading="loading" :error="errorMessage" @retry="loadData">
      <a-row :gutter="[12, 12]" style="margin-bottom: 12px;">
        <a-col :xs="12" :lg="6"><a-card size="small"><a-statistic title="今日金额" :value="summary.todayAmount || 0" :precision="2" suffix="元" /></a-card></a-col>
        <a-col :xs="12" :lg="6"><a-card size="small"><a-statistic title="本月金额" :value="summary.monthAmount || 0" :precision="2" suffix="元" /></a-card></a-col>
        <a-col :xs="12" :lg="6"><a-card size="small"><a-statistic title="待处理" :value="summary.pendingCount || 0" /></a-card></a-col>
        <a-col :xs="12" :lg="6"><a-card size="small"><a-statistic title="异常数" :value="summary.exceptionCount || 0" /></a-card></a-col>
      </a-row>
      <a-alert
        v-if="summary.warningMessage"
        type="warning"
        show-icon
        style="margin-bottom: 12px;"
        :message="summary.warningMessage"
      />
      <a-card class="card-elevated" :bordered="false">
        <vxe-table border stripe show-overflow :loading="loading" :data="rows" height="560">
          <vxe-column field="consumeDate" title="消费日期" width="140" />
          <vxe-column field="elderName" title="长者" min-width="140">
            <template #default="{ row }">{{ row.elderName || '-' }}</template>
          </vxe-column>
          <vxe-column field="amount" title="金额" width="120" />
          <vxe-column field="category" title="分类" width="140" />
          <vxe-column field="sourceType" title="来源类型" width="160" />
          <vxe-column field="sourceId" title="来源ID" width="120" />
          <vxe-column field="remark" title="备注" min-width="220" />
          <vxe-column title="操作" width="180" fixed="right">
            <template #default="{ row }">
              <a-space>
                <a-button v-if="row.elderId" type="link" @click="go(`/finance/flows/consumption?elderId=${row.elderId}`)">消费明细</a-button>
                <a-button type="link" @click="go('/finance/reconcile/exception')">异常处理</a-button>
              </a-space>
            </template>
          </vxe-column>
        </vxe-table>
      </a-card>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref, withDefaults } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getFinanceModuleEntrySummary } from '../../api/finance'
import { getConsumptionPage } from '../../api/financeFee'
import type { ConsumptionRecordItem, FinanceModuleEntrySummary, PageResult } from '../../types'
import { exportCsv } from '../../utils/export'
import { printTableReport } from '../../utils/print'

type QuickLink = { label: string; to: string }

const props = withDefaults(
  defineProps<{
    moduleName: string
    moduleKey?: string
    description?: string
    links?: QuickLink[]
    category?: string
  }>(),
  {
    moduleKey: 'GENERAL',
    description: '',
    links: () => [],
    category: undefined
  }
)

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const summary = ref<FinanceModuleEntrySummary>({
  moduleKey: props.moduleKey || 'GENERAL',
  bizDate: '',
  todayAmount: 0,
  monthAmount: 0,
  totalCount: 0,
  pendingCount: 0,
  exceptionCount: 0,
  warningMessage: '',
  topItems: []
})
const rows = ref<ConsumptionRecordItem[]>([])
const query = ref({
  range: [dayjs().startOf('month'), dayjs()] as any,
  keyword: '',
  printRemark: ''
})

function go(path: string) {
  router.push(path)
}

function reset() {
  query.value.range = [dayjs().startOf('month'), dayjs()]
  query.value.keyword = ''
  query.value.printRemark = ''
  loadData()
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [summaryRes, pageRes] = await Promise.all([
      getFinanceModuleEntrySummary({ moduleKey: props.moduleKey || 'GENERAL' }),
      getConsumptionPage({
        pageNo: 1,
        pageSize: 200,
        from: dayjs(query.value.range?.[0]).format('YYYY-MM-DD'),
        to: dayjs(query.value.range?.[1]).format('YYYY-MM-DD'),
        category: props.category,
        keyword: query.value.keyword || undefined
      }) as Promise<PageResult<ConsumptionRecordItem>>
    ])
    summary.value = summaryRes
    rows.value = pageRes.list || []
  } catch (error: any) {
    errorMessage.value = error?.message || '加载流水中心失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

function exportCurrent() {
  exportCsv(
    rows.value.map(item => ({
      消费日期: item.consumeDate || '',
      长者: item.elderName || '',
      金额: Number(item.amount || 0).toFixed(2),
      分类: item.category || '',
      来源类型: item.sourceType || '',
      来源ID: item.sourceId || '',
      备注: item.remark || ''
    })),
    `${props.moduleName}-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  )
}

function printCurrent() {
  try {
    printTableReport({
      title: props.moduleName,
      subtitle: `日期：${dayjs(query.value.range?.[0]).format('YYYY-MM-DD')} ~ ${dayjs(query.value.range?.[1]).format('YYYY-MM-DD')}；关键字：${query.value.keyword || '-'}；备注：${query.value.printRemark || '-'}`,
      columns: [
        { key: 'consumeDate', title: '消费日期' },
        { key: 'elderName', title: '长者' },
        { key: 'amount', title: '金额' },
        { key: 'category', title: '分类' },
        { key: 'sourceType', title: '来源类型' },
        { key: 'sourceId', title: '来源ID' },
        { key: 'remark', title: '备注' }
      ],
      rows: rows.value.map(item => ({
        consumeDate: item.consumeDate || '-',
        elderName: item.elderName || '-',
        amount: Number(item.amount || 0).toFixed(2),
        category: item.category || '-',
        sourceType: item.sourceType || '-',
        sourceId: item.sourceId || '-',
        remark: item.remark || '-'
      }))
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

onMounted(loadData)
</script>
