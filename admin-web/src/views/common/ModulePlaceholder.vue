<template>
  <PageContainer :title="title" :subTitle="description">
    <a-card :bordered="false" class="card-elevated">
      <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadSummary">
        <a-row :gutter="[12, 12]">
          <a-col :xs="12" :lg="6">
            <a-statistic title="待办总量" :value="summary.openTodoCount || 0" />
          </a-col>
          <a-col :xs="12" :lg="6">
            <a-statistic title="流程待审批" :value="summary.pendingApprovalCount || 0" />
          </a-col>
          <a-col :xs="12" :lg="6">
            <a-statistic title="库存预警" :value="summary.inventoryLowStockCount || 0" />
          </a-col>
          <a-col :xs="12" :lg="6">
            <a-statistic title="异常事件" :value="summary.incidentOpenCount || 0" />
          </a-col>
        </a-row>
        <a-divider />
        <a-space wrap>
          <a-button type="primary" @click="go('/workbench')">返回工作台</a-button>
          <a-button @click="go('/workbench/todo')">查看待办</a-button>
          <a-button @click="go('/oa/approval')">查看审批</a-button>
          <a-button @click="go('/dashboard')">查看看板</a-button>
        </a-space>
      </StatefulBlock>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { getPortalSummary } from '../../api/oa'

withDefaults(
  defineProps<{
    title: string
    description?: string
  }>(),
  {
    description: '该功能已纳入规划，等待业务规则与接口联调。'
  }
)

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const summary = reactive<Record<string, number>>({
  openTodoCount: 0,
  pendingApprovalCount: 0,
  inventoryLowStockCount: 0,
  incidentOpenCount: 0
})

function go(path: string) {
  router.push(path)
}

async function loadSummary() {
  loading.value = true
  errorMessage.value = ''
  try {
    const data = await getPortalSummary({ silent403: true })
    Object.assign(summary, data || {})
  } catch (error: any) {
    const status = Number(error?.response?.status || 0)
    errorMessage.value = status === 403 ? '当前账号暂无模块概览权限' : (error?.message || '加载模块概览失败')
    if (status !== 403) {
      message.error(errorMessage.value)
    }
  } finally {
    loading.value = false
  }
}

onMounted(loadSummary)
</script>
