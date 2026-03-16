<template>
  <PageContainer title="财务搜索" subTitle="统一检索长者、账单、收款流水与合同">
    <a-card class="search-hero card-elevated" :bordered="false">
      <div>
        <div class="search-hero__eyebrow">Finance Search</div>
        <h2>先搜，再决定去哪个业务页。</h2>
        <p>适合按长者、账单号、收据号、外部流水号或合同号快速定位财务对象。</p>
      </div>
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="长者 / 账单号 / 外部流水号 / 合同号" style="width: 320px" @pressEnter="loadData" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">搜索</a-button>
            <a-button :disabled="!query.keyword.trim()" @click="exportData">导出结果</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <StatefulBlock style="margin-top: 16px;" :loading="loading" :error="errorMessage" @retry="loadData">
      <a-row :gutter="[12, 12]" style="margin-bottom: 16px;">
        <a-col :xs="24" :md="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="长者命中" :value="result?.elders?.length || 0" /></a-card></a-col>
        <a-col :xs="24" :md="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="账单命中" :value="result?.bills?.length || 0" /></a-card></a-col>
        <a-col :xs="24" :md="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="收款命中" :value="result?.payments?.length || 0" /></a-card></a-col>
        <a-col :xs="24" :md="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="合同命中" :value="result?.contracts?.length || 0" /></a-card></a-col>
      </a-row>
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :xl="12">
          <a-card class="card-elevated" :bordered="false" title="长者">
            <a-list :data-source="result?.elders || []">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta :title="item.title" :description="`${item.subtitle || ''} ${item.extra || ''}`" />
                  <template #actions>
                    <a-button type="link" @click="go(item.actionPath || '/elder/list')">打开</a-button>
                  </template>
                </a-list-item>
              </template>
            </a-list>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="12">
          <a-card class="card-elevated" :bordered="false" title="账单">
            <a-list :data-source="result?.bills || []">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta :title="item.title" :description="`${item.subtitle || ''} ${item.extra || ''}`" />
                  <template #actions>
                    <a-button type="link" @click="go(item.actionPath || '/finance/bills/in-resident')">打开</a-button>
                  </template>
                </a-list-item>
              </template>
            </a-list>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="12">
          <a-card class="card-elevated" :bordered="false" title="收款流水">
            <a-list :data-source="result?.payments || []">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta :title="item.title" :description="`${item.subtitle || ''} ${item.extra || ''}`" />
                  <template #actions>
                    <a-button type="link" @click="go(item.actionPath || '/finance/payments/records')">打开</a-button>
                  </template>
                </a-list-item>
              </template>
            </a-list>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="12">
          <a-card class="card-elevated" :bordered="false" title="合同">
            <a-list :data-source="result?.contracts || []">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta :title="item.title" :description="`${item.subtitle || ''} ${item.extra || ''}`" />
                  <template #actions>
                    <a-button type="link" @click="go(item.actionPath || '/elder/contracts-invoices')">打开</a-button>
                  </template>
                </a-list-item>
              </template>
            </a-list>
          </a-card>
        </a-col>
      </a-row>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { exportFinanceSearchCsv, financeSearch } from '../../api/finance'
import type { FinanceSearchResponse } from '../../types'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const result = ref<FinanceSearchResponse | null>(null)
const query = reactive({
  keyword: typeof route.query.keyword === 'string' ? route.query.keyword : ''
})

function go(path: string) {
  router.push(path)
}

async function loadData() {
  if (!query.keyword.trim()) {
    message.warning('请输入搜索关键词')
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    result.value = await financeSearch({ keyword: query.keyword.trim(), limit: 8 })
  } catch (error: any) {
    errorMessage.value = error?.message || '财务搜索失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

async function exportData() {
  if (!query.keyword.trim()) {
    message.warning('请输入搜索关键词')
    return
  }
  try {
    await exportFinanceSearchCsv({ keyword: query.keyword.trim(), limit: 20 })
    message.success('财务搜索结果已导出')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

function reset() {
  query.keyword = ''
  result.value = null
}

onMounted(() => {
  if (query.keyword.trim()) {
    loadData()
  }
})
</script>

<style scoped>
.search-hero {
  display: grid;
  gap: 18px;
  grid-template-columns: 1fr 1fr;
  background:
    radial-gradient(circle at top left, rgba(14, 165, 233, 0.22), transparent 34%),
    linear-gradient(135deg, #0f172a 0%, #1e3a8a 46%, #0f766e 100%);
  color: #fff;
}

.search-hero__eyebrow {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.72);
}

.search-hero h2 {
  margin: 10px 0;
  font-size: 28px;
  line-height: 1.18;
}

.search-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.76);
}

@media (max-width: 1100px) {
  .search-hero {
    grid-template-columns: 1fr;
  }
}
</style>
