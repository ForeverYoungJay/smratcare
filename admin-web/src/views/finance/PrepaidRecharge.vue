<template>
  <PageContainer title="预存充值" subTitle="按老人账户登记充值并写入账户流水">
    <a-card :bordered="false" class="card-elevated">
      <a-form layout="vertical">
        <a-form-item label="老人" required>
          <a-select
            v-model:value="form.elderId"
            show-search
            placeholder="输入姓名搜索"
            :filter-option="false"
            :options="elderOptions"
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
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { adjustElderAccount } from '../../api/finance'

const router = useRouter()

const form = reactive({
  elderId: undefined as number | undefined,
  amount: 0,
  rechargeMethod: 'ALIPAY',
  rechargeTime: dayjs(),
  remark: '预存充值'
})
const { elderOptions, searchElders: searchElderOptions, findElderName } = useElderOptions({ pageSize: 20 })
const submitting = ref(false)

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
  router.push({ name: 'FinanceMedicalCareLedger' })
}
</script>
