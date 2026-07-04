<template>
  <PageContainer title="家属充值台账" subTitle="集中查看家属端充值、待支付、异常关闭与到账情况，方便运营和财务协同处理">
    <template #extra>
      <a-space wrap>
        <a-button :loading="loading" @click="loadData">刷新</a-button>
        <a-button @click="go('/oa/family-users')">家属账号</a-button>
        <a-button @click="go('/oa/family-service-health')">运行健康</a-button>
        <a-button @click="go('/oa/work-execution/family-communication')">沟通处理</a-button>
      </a-space>
    </template>

    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-form">
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 150px">
            <a-select-option value="INIT">待创建</a-select-option>
            <a-select-option value="PREPAY_CREATED">待支付</a-select-option>
            <a-select-option value="PAID">已支付</a-select-option>
            <a-select-option value="FAILED">支付失败</a-select-option>
            <a-select-option value="CLOSED">已关闭</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="渠道">
          <a-select v-model:value="query.channel" allow-clear style="width: 160px">
            <a-select-option value="WECHAT_JSAPI">微信支付</a-select-option>
            <a-select-option value="MANUAL">人工登记</a-select-option>
            <a-select-option value="WECHAT">微信</a-select-option>
            <a-select-option value="ALIPAY">支付宝</a-select-option>
            <a-select-option value="BANK">转账</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" allow-clear placeholder="家属/老人/订单号/备注" style="width: 260px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="applySearch">搜索</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <section class="summary-grid">
      <div class="summary-card">
        <span>订单总数</span>
        <strong>{{ data.summary.totalCount }}</strong>
        <small>当前筛选条件下的充值订单总量</small>
      </div>
      <div class="summary-card">
        <span>待支付</span>
        <strong>{{ data.summary.pendingCount }}</strong>
        <small>含待创建和预支付已创建订单</small>
      </div>
      <div class="summary-card">
        <span>异常订单</span>
        <strong>{{ data.summary.abnormalCount }}</strong>
        <small>失败或超时关闭，需优先排查</small>
      </div>
      <div class="summary-card">
        <span>到账金额</span>
        <strong>{{ money(data.summary.paidAmount) }}</strong>
        <small>总额 {{ money(data.summary.totalAmount) }}</small>
      </div>
    </section>

    <a-alert
      class="ledger-alert"
      :type="data.summary.abnormalCount > 0 ? 'warning' : 'success'"
      show-icon
      :message="data.summary.abnormalCount > 0 ? `当前有 ${data.summary.abnormalCount} 笔异常订单待处理` : '当前筛选范围内没有异常充值订单'"
      :description="`已支付 ${data.summary.paidCount} 笔，自动扣费相关备注 ${data.summary.autoPayEnabledCount} 笔。`"
    />

    <a-card class="card-elevated" :bordered="false">
      <a-table
        :columns="columns"
        :data-source="data.list"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :scroll="{ x: 1480 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ record.statusText || record.status }}</a-tag>
          </template>
          <template v-else-if="column.key === 'amount'">
            {{ money(record.amount) }}
          </template>
          <template v-else-if="column.key === 'family'">
            <div class="cell-stack">
              <strong>{{ record.familyName || '-' }}</strong>
              <span>{{ record.familyPhone || '-' }}</span>
            </div>
          </template>
          <template v-else-if="column.key === 'elder'">
            <div class="cell-stack">
              <strong>{{ record.elderName || '-' }}</strong>
              <span>ID {{ record.elderId || '-' }}</span>
            </div>
          </template>
          <template v-else-if="column.key === 'remark'">
            <div class="remark-cell">{{ record.remark || '-' }}</div>
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="row-action-links">
              <a-button v-if="record.familyUserId" type="link" size="small" @click="go('/oa/family-users')">家属账号</a-button>
              <a-button v-if="record.elderId" type="link" size="small" @click="go(`/elder/detail/${record.elderId}?tab=family`)">老人详情</a-button>
              <a-button type="link" size="small" @click="go('/oa/work-execution/family-communication')">沟通跟进</a-button>
            </div>
          </template>
        </template>
      </a-table>

      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="data.total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import {
  getOperationsFamilyRechargeLedger,
  type OperationsFamilyRechargeLedger
} from '../../api/operations'

const router = useRouter()
const loading = ref(false)
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  status: undefined as string | undefined,
  channel: undefined as string | undefined,
  keyword: ''
})
const data = ref<OperationsFamilyRechargeLedger>({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  summary: {
    totalCount: 0,
    pendingCount: 0,
    paidCount: 0,
    abnormalCount: 0,
    autoPayEnabledCount: 0,
    totalAmount: 0,
    paidAmount: 0,
    abnormalAmount: 0
  },
  list: []
})

const columns = [
  { title: '订单号', dataIndex: 'outTradeNo', key: 'outTradeNo', width: 180 },
  { title: '家属', key: 'family', width: 180 },
  { title: '老人', key: 'elder', width: 180 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '渠道', dataIndex: 'channel', key: 'channel', width: 130 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: '支付时间', dataIndex: 'paidTime', key: 'paidTime', width: 170 },
  { title: '微信流水', dataIndex: 'wxTransactionId', key: 'wxTransactionId', width: 180 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 260 },
  { title: '操作', key: 'action', width: 220, fixed: 'right' as const }
]

function money(value?: number) {
  return `¥${Number(value || 0).toFixed(2)}`
}

function statusColor(status?: string) {
  if (status === 'PAID') return 'green'
  if (status === 'PREPAY_CREATED' || status === 'INIT') return 'gold'
  if (status === 'FAILED' || status === 'CLOSED') return 'red'
  return 'default'
}

function go(path?: string) {
  if (!path) return
  router.push(path)
}

async function loadData() {
  loading.value = true
  try {
    data.value = await getOperationsFamilyRechargeLedger({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      channel: query.channel,
      keyword: query.keyword || undefined
    })
  } catch (error: any) {
    message.error(error?.message || '家属充值台账加载失败')
  } finally {
    loading.value = false
  }
}

function applySearch() {
  query.pageNo = 1
  loadData()
}

function reset() {
  query.pageNo = 1
  query.pageSize = 10
  query.status = undefined
  query.channel = undefined
  query.keyword = ''
  loadData()
}

function onPageChange(page: number) {
  query.pageNo = page
  loadData()
}

function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.search-form {
  row-gap: 12px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin: 16px 0;
}

.summary-card {
  display: grid;
  gap: 10px;
  min-height: 128px;
  padding: 18px;
  border: 1px solid var(--border-soft);
  border-radius: 18px;
  background: var(--surface-2);
}

.summary-card span,
.summary-card small,
.cell-stack span,
.remark-cell {
  color: var(--muted);
  font-size: 12px;
  line-height: 1.7;
}

.summary-card strong {
  color: var(--ink);
  font-size: 30px;
  line-height: 1.1;
}

.ledger-alert {
  margin-bottom: 16px;
}

.cell-stack {
  display: grid;
}

.cell-stack strong {
  color: var(--ink);
}

.remark-cell {
  white-space: pre-wrap;
}

@media (max-width: 1280px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
