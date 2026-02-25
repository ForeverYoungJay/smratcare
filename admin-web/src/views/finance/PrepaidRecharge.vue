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
          />
        </a-form-item>
        <a-form-item label="充值金额(元)" required>
          <a-input-number v-model:value="form.amount" :min="0.01" :precision="2" style="width: 100%" />
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
import PageContainer from '../../components/PageContainer.vue'
import { getElderPage } from '../../api/elder'
import { adjustElderAccount } from '../../api/finance'

const router = useRouter()

const form = reactive({
  elderId: undefined as number | undefined,
  amount: 0,
  remark: '预存充值'
})
const elderOptions = ref<{ label: string; value: number }[]>([])
const submitting = ref(false)

async function searchElders(keyword: string) {
  if (!keyword) {
    elderOptions.value = []
    return
  }
  const res = await getElderPage({ pageNo: 1, pageSize: 20, keyword })
  elderOptions.value = res.list.map((item: any) => ({
    label: item.fullName || item.elderName || item.name || '未知老人',
    value: item.id
  }))
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
  submitting.value = true
  try {
    const selected = elderOptions.value.find((item) => item.value === form.elderId)
    await adjustElderAccount({
      elderId: form.elderId,
      elderName: selected?.label,
      amount: form.amount,
      direction: 'CREDIT',
      remark: form.remark
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
