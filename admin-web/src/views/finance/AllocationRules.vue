<template>
  <PageContainer title="分摊规则" subTitle="按床位/房间/人数/天数/面积与公共费用规则管理">
    <a-card class="card-elevated" :bordered="false">
      <a-space wrap>
        <a-date-picker v-model:value="query.month" picker="month" style="width: 150px" />
        <a-button type="primary" @click="loadData">查询</a-button>
        <a-button :loading="initLoading" @click="initTemplate">初始化默认电费模板</a-button>
        <a-button @click="go('/finance/allocation/public-cost?period=this_month')">分摊与公共费用</a-button>
      </a-space>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <vxe-table border stripe show-overflow :loading="loading" :data="rows" height="560">
        <vxe-column field="ruleTypeLabel" title="规则类型" width="150" />
        <vxe-column field="configKey" title="规则键" min-width="220" />
        <vxe-column field="configValue" title="规则值" width="150" />
        <vxe-column field="effectiveMonth" title="生效月份" width="120" />
        <vxe-column field="status" title="状态" width="110">
          <template #default="{ row }">
            <a-tag :color="Number(row.status) === 1 ? 'green' : 'default'">{{ Number(row.status) === 1 ? '启用' : '停用' }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="remark" title="备注" min-width="220" />
      </vxe-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getFinanceAllocationRules, initFinanceAllocationRuleTemplate } from '../../api/finance'
import type { FinanceAllocationRuleItem } from '../../types'

const router = useRouter()
const loading = ref(false)
const initLoading = ref(false)
const query = ref({
  month: dayjs()
})
const rows = ref<FinanceAllocationRuleItem[]>([])

function go(path: string) {
  router.push(path)
}

async function loadData() {
  loading.value = true
  try {
    rows.value = await getFinanceAllocationRules({
      month: dayjs(query.value.month).format('YYYY-MM')
    })
  } finally {
    loading.value = false
  }
}

async function initTemplate() {
  initLoading.value = true
  try {
    const month = dayjs(query.value.month).format('YYYY-MM')
    const res = await initFinanceAllocationRuleTemplate({ month })
    message.success(`模板已同步：新增${res.createdCount}条，更新${res.updatedCount}条`)
    await loadData()
  } finally {
    initLoading.value = false
  }
}

onMounted(loadData)
</script>
