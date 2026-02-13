<template>
  <PageContainer title="绩效榜" subTitle="问卷评分关联绩效">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="模板">
          <a-select v-model:value="query.templateId" allow-clear show-search style="width: 220px" @change="fetchData">
            <a-select-option v-for="tpl in templates" :key="tpl.id" :value="tpl.id">{{ tpl.templateName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="日期">
          <a-range-picker v-model:value="query.dateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="staffId"
        :scroll="{ y: 520 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'averageScore'">
            {{ record.averageScore.toFixed(2) }}
          </template>
        </template>
      </a-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { getSurveyPerformance, getSurveyTemplatePage } from '../../api/survey'
import type { SurveyPerformanceItem, SurveyTemplate, PageResult } from '../../types'

const templates = ref<SurveyTemplate[]>([])
const rows = ref<SurveyPerformanceItem[]>([])
const loading = ref(false)

const query = reactive({
  templateId: undefined as number | undefined,
  dateRange: [] as string[]
})

const columns = [
  { title: '员工', dataIndex: 'staffName', key: 'staffName', width: 140 },
  { title: '提交数', dataIndex: 'submissions', key: 'submissions', width: 120 },
  { title: '平均分', dataIndex: 'averageScore', key: 'averageScore', width: 120 }
]

async function loadTemplates() {
  const res: PageResult<SurveyTemplate> = await getSurveyTemplatePage({ pageNo: 1, pageSize: 200 })
  templates.value = res.list
}

function getDateParams() {
  const [from, to] = query.dateRange || []
  return { dateFrom: from, dateTo: to }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getSurveyPerformance({ templateId: query.templateId, ...getDateParams() })
    rows.value = res
  } finally {
    loading.value = false
  }
}

function reset() {
  query.templateId = undefined
  query.dateRange = []
  fetchData()
}

onMounted(async () => {
  await loadTemplates()
  await fetchData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
