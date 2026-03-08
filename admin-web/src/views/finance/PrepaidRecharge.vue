<template>
  <PageContainer title="预存充值" subTitle="按老人账户登记充值并写入账户流水">
    <a-row :gutter="[12, 12]" style="margin-bottom: 12px;">
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="今日充值总额" :value="todayRechargeAmount" suffix="元" :precision="2" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="低余额人数" :value="lowBalanceCount" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="欠费人数" :value="overdueCount" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="欠费金额" :value="overdueAmount" suffix="元" :precision="2" /></a-card></a-col>
      <a-col :span="24">
        <a-alert
          :type="summary.warningMessage ? 'warning' : 'info'"
          show-icon
          :message="summary.warningMessage || `建议优先处理低余额与欠费长者充值`"
          :description="`今日收款 ${Number(summary.todayAmount || 0).toFixed(2)} 元；欠费 ${Number(summary.monthAmount || 0).toFixed(2)} 元`"
        />
      </a-col>
      <a-col :span="24">
        <a-space wrap>
          <a-tag v-for="method in paymentMethods" :key="method.method">{{ method.methodLabel }}: {{ Number(method.amount || 0).toFixed(2) }}</a-tag>
          <a-button @click="goLowBalance">低余额账户</a-button>
          <a-button @click="goOverdueBills">欠费账单</a-button>
        </a-space>
      </a-col>
    </a-row>

    <a-card :bordered="false" class="card-elevated">
      <a-form layout="vertical">
        <a-form-item label="老人" required>
          <a-select
            v-model:value="form.elderId"
            show-search
            placeholder="输入姓名搜索"
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
          />
        </a-form-item>
        <a-form-item label="充值金额(元)" required>
          <a-input-number v-model:value="form.amount" :min="0.01" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="充值方式" required>
          <a-select v-model:value="form.rechargeMethod">
            <a-select-option value="ALIPAY">支付宝</a-select-option>
            <a-select-option value="WECHAT">微信</a-select-option>
            <a-select-option value="CASH">现金</a-select-option>
            <a-select-option value="QR_CODE">扫码</a-select-option>
            <a-select-option value="BANK">转账</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="充值时间" required>
          <a-date-picker v-model:value="form.rechargeTime" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
        <a-space>
          <a-button type="primary" :loading="submitting" @click="submit">确认充值</a-button>
          <a-button @click="goLogs">查看账户流水</a-button>
        </a-space>
      </a-form>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { adjustElderAccount, getFinanceModuleEntrySummary, getFinanceWorkbenchOverview } from '../../api/finance'
import type { FinanceModuleEntrySummary, FinancePaymentMethodAmount } from '../../types'
import { confirmAction } from '../../utils/actionConfirm'

const router = useRouter()

const form = reactive({
  elderId: undefined as number | undefined,
  amount: 0,
  rechargeMethod: 'ALIPAY',
  rechargeTime: dayjs(),
  remark: '预存充值'
})
const { elderOptions, elderLoading, searchElders: searchElderOptions, findElderName } = useElderOptions({ pageSize: 20 })
const submitting = ref(false)
const summary = ref<FinanceModuleEntrySummary>({
  moduleKey: 'BALANCE_WARN',
  bizDate: '',
  todayAmount: 0,
  monthAmount: 0,
  totalCount: 0,
  pendingCount: 0,
  exceptionCount: 0,
  warningMessage: '',
  topItems: []
})
const paymentMethods = ref<FinancePaymentMethodAmount[]>([])
const todayRechargeAmount = computed(() => paymentMethods.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))
const lowBalanceCount = computed(() => Number(summary.value.pendingCount || 0))
const overdueCount = computed(() => Number(summary.value.totalCount || 0))
const overdueAmount = computed(() => Number(summary.value.monthAmount || 0))

async function searchElders(keyword: string) {
  await searchElderOptions(keyword)
}

async function submit() {
  if (!form.elderId) {
    message.error('请选择老人')
    return
  }
  if (!form.amount || form.amount <= 0) {
    message.error('请输入有效金额')
    return
  }
  if (!form.rechargeMethod) {
    message.error('请选择充值方式')
    return
  }
  if (!form.rechargeTime) {
    message.error('请选择充值时间')
    return
  }
  if (Number(form.amount || 0) >= 5000) {
    const confirmed = await confirmAction({
      title: '确认大额充值？',
      content: `本次充值金额 ${Number(form.amount || 0).toFixed(2)} 元`,
      impactItems: ['将直接写入账户余额', '会影响自动扣费与欠费统计'],
      okText: '确认充值'
    })
    if (!confirmed) return
  }
  submitting.value = true
  try {
    const rechargeTimeText = dayjs(form.rechargeTime).format('YYYY-MM-DD HH:mm:ss')
    await adjustElderAccount({
      elderId: form.elderId,
      elderName: findElderName(form.elderId),
      amount: form.amount,
      direction: 'CREDIT',
      remark: `${form.remark || '预存充值'} | 充值方式:${form.rechargeMethod} | 充值时间:${rechargeTimeText}`
    })
    message.success('充值成功')
    router.push({ name: 'FinanceDepositManagement' })
  } finally {
    submitting.value = false
  }
}

function goLogs() {
  router.push({ name: 'FinanceAccountLog', query: { elderId: form.elderId } })
}

function goLowBalance() {
  router.push('/finance/accounts/list?filter=low_balance')
}

function goOverdueBills() {
  router.push('/finance/bills/in-resident?filter=overdue')
}

onMounted(() => {
  searchElders('').catch(() => {})
  Promise.all([
    getFinanceModuleEntrySummary({ moduleKey: 'BALANCE_WARN' }).then((data) => {
      summary.value = data
    }),
    getFinanceWorkbenchOverview().then((data) => {
      paymentMethods.value = data?.cashier?.paymentMethods || []
    })
  ]).catch(() => {})
})
</script>
