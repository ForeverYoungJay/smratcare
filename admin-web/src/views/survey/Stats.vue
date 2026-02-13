<template>
  <PageContainer title="问卷统计" subTitle="提交与评分概览">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="模板">
          <a-select v-model:value="query.templateId" allow-clear show-search style="width: 220px" @change="fetchAll">
            <a-select-option v-for="tpl in templates" :key="tpl.id" :value="tpl.id">{{ tpl.templateName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="日期">
          <a-range-picker v-model:value="query.dateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchAll">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <div class="summary-cards">
      <a-card class="summary" :bordered="false">
        <div class="label">提交总数</div>
        <div class="value">{{ summary.totalSubmissions }}</div>
      </a-card>
      <a-card class="summary" :bordered="false">
        <div class="label">评分总分</div>
        <div class="value">{{ summary.scoreTotal }}</div>
      </a-card>
      <a-card class="summary" :bordered="false">
        <div class="label">平均分</div>
        <div class="value">{{ summary.averageScore.toFixed(2) }}</div>
      </a-card>
    </div>

    <a-card class="card-elevated" :bordered="false">
      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :scroll="{ y: 520 }"
      />
      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { getSurveyTemplatePage, getSurveyStatsSummary, getSurveySubmissionPage } from '../../api/survey'
import type { SurveyTemplate, SurveyStatsSummary, PageResult } from '../../types'

const templates = ref<SurveyTemplate[]>([])
const summary = reactive<SurveyStatsSummary>({ totalSubmissions: 0, averageScore: 0, scoreTotal: 0 })

const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)

const query = reactive({
  templateId: undefined as number | undefined,
  dateRange: [] as string[],
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '提交ID', dataIndex: 'id', key: 'id', width: 120 },
  { title: '模板ID', dataIndex: 'templateId', key: 'templateId', width: 120 },
  { title: '对象类型', dataIndex: 'targetType', key: 'targetType', width: 120 },
  { title: '对象ID', dataIndex: 'targetId', key: 'targetId', width: 120 },
  { title: '提交人', dataIndex: 'submitterName', key: 'submitterName', width: 140 },
  { title: '得分', dataIndex: 'scoreTotal', key: 'scoreTotal', width: 100 },
  { title: '提交时间', dataIndex: 'createTime', key: 'createTime' }
]

async function loadTemplates() {
  const res: PageResult<SurveyTemplate> = await getSurveyTemplatePage({ pageNo: 1, pageSize: 200 })
  templates.value = res.list
}

function getDateParams() {
  const [from, to] = query.dateRange || []
  return { dateFrom: from, dateTo: to }
}

async function fetchSummary() {
  const params = { templateId: query.templateId, ...getDateParams() }
  const res = await getSurveyStatsSummary(params)
  summary.totalSubmissions = res.totalSubmissions
  summary.averageScore = res.averageScore
  summary.scoreTotal = res.scoreTotal
}

async function fetchPage() {
  loading.value = true
  try {
    const res = await getSurveySubmissionPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      templateId: query.templateId
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function fetchAll() {
  await Promise.all([fetchSummary(), fetchPage()])
}

function reset() {
  query.templateId = undefined
  query.dateRange = []
  query.pageNo = 1
  fetchAll()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchPage()
}

function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchPage()
}

onMounted(async () => {
  await loadTemplates()
  await fetchAll()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
.summary-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin: 16px 0;
}
.summary {
  text-align: left;
}
.summary .label {
  color: rgba(0, 0, 0, 0.6);
  margin-bottom: 6px;
}
.summary .value {
  font-size: 24px;
  font-weight: 600;
}
</style>
