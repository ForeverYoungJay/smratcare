<template>
  <PageContainer title="对账异常处理" subTitle="短款/多款/重复/跨期等异常明细与处理建议">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query">
        <a-form-item label="日期">
          <a-date-picker v-model:value="query.date" style="width: 150px" />
        </a-form-item>
        <a-form-item label="异常类型">
          <a-select v-model:value="query.type" allow-clear style="width: 180px">
            <a-select-option value="BILL_PAID_UNMATCHED">账单已收未核销</a-select-option>
            <a-select-option value="DUPLICATED_PAYMENT">收款重复/冲正未完成</a-select-option>
            <a-select-option value="INVOICE_UNLINKED">发票未关联账单</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">查询</a-button>
            <a-button @click="go('/finance/reconcile/center')">返回对账中心</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <vxe-table border stripe show-overflow :loading="loading" :data="rows" height="560">
        <vxe-column field="occurredAt" title="发生时间" width="180" />
        <vxe-column field="exceptionTypeLabel" title="异常类型" width="180">
          <template #default="{ row }">
            <a-tag color="volcano">{{ row.exceptionTypeLabel }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="elderName" title="长者" width="140">
          <template #default="{ row }">{{ row.elderName || '-' }}</template>
        </vxe-column>
        <vxe-column field="amount" title="涉及金额" width="120" />
        <vxe-column field="detail" title="异常描述" min-width="240" />
        <vxe-column field="suggestion" title="处理建议" min-width="220" />
        <vxe-column title="操作" width="180" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a-button v-if="row.billId" type="link" @click="go(`/finance/bill/${row.billId}`)">查看账单</a-button>
              <a-button type="link" @click="go('/finance/payments/register?from=reconcile_exception')">去处理</a-button>
            </a-space>
          </template>
        </vxe-column>
      </vxe-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getFinanceReconcileExceptions } from '../../api/finance'
import type { FinanceReconcileExceptionItem } from '../../types'

const router = useRouter()
const loading = ref(false)
const rows = ref<FinanceReconcileExceptionItem[]>([])
const query = ref({
  date: dayjs(),
  type: undefined as string | undefined
})

function go(path: string) {
  router.push(path)
}

async function loadData() {
  loading.value = true
  try {
    rows.value = await getFinanceReconcileExceptions({
      date: dayjs(query.value.date).format('YYYY-MM-DD'),
      type: query.value.type
    })
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
