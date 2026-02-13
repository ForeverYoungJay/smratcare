<template>
  <PageContainer title="对账中心" subTitle="按日核对收款与账单变动">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="日期">
          <a-date-picker v-model:value="query.date" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="reconcile">生成对账</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="对账结果">
      <a-row :gutter="16">
        <a-col :span="8">
          <a-statistic title="日期" :value="result?.date || '-'" />
        </a-col>
        <a-col :span="8">
          <a-statistic title="当日收款" :value="result?.totalReceived ?? 0" />
        </a-col>
        <a-col :span="8">
          <a-statistic title="是否异常">
            <template #value>
              <a-tag :color="result?.mismatchFlag ? 'red' : 'green'">{{ result?.mismatchFlag ? '存在差异' : '正常' }}</a-tag>
            </template>
          </a-statistic>
        </a-col>
      </a-row>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="对账历史">
      <a-form layout="inline" :model="historyQuery" class="search-form">
        <a-form-item label="开始日期">
          <a-date-picker v-model:value="historyQuery.from" />
        </a-form-item>
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="historyQuery.to" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchHistory">查询</a-button>
            <a-button @click="resetHistory">重置</a-button>
            <a-button @click="exportHistory">导出</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <vxe-table border stripe show-overflow :data="history" height="320" :loading="historyLoading">
        <vxe-column field="reconcileDate" title="对账日期" width="140">
          <template #default="{ row }">
            <span>{{ row.reconcileDate || row.date }}</span>
          </template>
        </vxe-column>
        <vxe-column field="totalReceived" title="收款总额" width="140" />
        <vxe-column field="mismatchFlag" title="是否异常" width="120">
          <template #default="{ row }">
            <a-tag :color="row.mismatchFlag ? 'red' : 'green'">{{ row.mismatchFlag ? '异常' : '正常' }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="remark" title="备注" min-width="200" />
        <vxe-column field="createTime" title="生成时间" width="180" />
      </vxe-table>

      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="historyQuery.pageNo"
        :page-size="historyQuery.pageSize"
        :total="historyTotal"
        show-size-changer
        @change="onHistoryPageChange"
        @showSizeChange="onHistoryPageSizeChange"
      />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { reconcileDaily, getReconcilePage } from '../../api/finance'
import type { PageResult, ReconcileDailyItem } from '../../types'
import { exportCsv } from '../../utils/export'

const query = ref({
  date: dayjs()
})

const result = ref<ReconcileDailyItem | null>(null)

const historyQuery = ref({
  from: dayjs().subtract(14, 'day'),
  to: dayjs(),
  pageNo: 1,
  pageSize: 10
})
const historyLoading = ref(false)
const history = ref<ReconcileDailyItem[]>([])
const historyTotal = ref(0)

async function reconcile() {
  result.value = await reconcileDaily({
    date: dayjs(query.value.date).format('YYYY-MM-DD')
  })
}

function reset() {
  query.value.date = dayjs()
  result.value = null
}

async function fetchHistory() {
  historyLoading.value = true
  try {
    const res: PageResult<ReconcileDailyItem> = await getReconcilePage({
      pageNo: historyQuery.value.pageNo,
      pageSize: historyQuery.value.pageSize,
      from: dayjs(historyQuery.value.from).format('YYYY-MM-DD'),
      to: dayjs(historyQuery.value.to).format('YYYY-MM-DD')
    })
    history.value = res.list
    historyTotal.value = res.total
  } finally {
    historyLoading.value = false
  }
}

function resetHistory() {
  historyQuery.value.from = dayjs().subtract(14, 'day')
  historyQuery.value.to = dayjs()
  historyQuery.value.pageNo = 1
  fetchHistory()
}

function exportHistory() {
  exportCsv(history.value, `reconcile-${dayjs().format('YYYYMMDD')}.csv`)
}

function onHistoryPageChange(page: number) {
  historyQuery.value.pageNo = page
  fetchHistory()
}

function onHistoryPageSizeChange(current: number, size: number) {
  historyQuery.value.pageNo = 1
  historyQuery.value.pageSize = size
  fetchHistory()
}

onMounted(fetchHistory)
</script>
