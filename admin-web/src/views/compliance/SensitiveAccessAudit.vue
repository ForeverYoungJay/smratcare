<template>
  <PageContainer title="敏感数据访问审计" subTitle="个人信息保护 · 谁在何时查看/导出了哪位长者的敏感数据">
    <template #extra>
      <a-space wrap>
        <a-select v-model:value="summaryDays" style="width: 120px" @change="loadSummary">
          <a-select-option :value="7">近 7 天</a-select-option>
          <a-select-option :value="30">近 30 天</a-select-option>
          <a-select-option :value="90">近 90 天</a-select-option>
        </a-select>
        <a-button type="primary" :loading="loading" @click="loadAll">刷新</a-button>
      </a-space>
    </template>

    <a-row :gutter="16" style="margin-bottom: 16px;">
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="访问总次数" :value="summary.totalAccess || 0" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="导出次数" :value="summary.exportCount || 0" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="被拒绝访问" :value="summary.deniedCount || 0" :value-style="{ color: '#cf1322' }" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="涉及操作人" :value="summary.distinctActors || 0" />
        </a-card>
      </a-col>
      <a-col :span="4">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="涉及对象" :value="summary.distinctTargets || 0" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 16px;">
      <a-form layout="inline">
        <a-form-item label="数据类别">
          <a-select v-model:value="query.dataCategory" allow-clear style="width: 180px" placeholder="全部">
            <a-select-option value="ELDER_HEALTH">长者健康</a-select-option>
            <a-select-option value="ELDER_IDCARD">长者身份证</a-select-option>
            <a-select-option value="FAMILY_PRIVACY">家属隐私</a-select-option>
            <a-select-option value="BILL">账单</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="动作">
          <a-select v-model:value="query.action" allow-clear style="width: 140px" placeholder="全部">
            <a-select-option value="VIEW">查看</a-select-option>
            <a-select-option value="EXPORT">导出</a-select-option>
            <a-select-option value="DECRYPT">明文查看</a-select-option>
            <a-select-option value="PRINT">打印</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="操作人ID">
          <a-input v-model:value="query.actorId" allow-clear placeholder="可选" style="width: 140px" />
        </a-form-item>
        <a-form-item label="日期">
          <a-range-picker v-model:value="dateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadPage">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false">
      <a-table
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        size="small"
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-tag :color="actionColor(record.action)">{{ actionLabel(record.action) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'result'">
            <a-tag :color="record.result === 'DENIED' ? 'red' : 'green'">
              {{ record.result === 'DENIED' ? '拒绝' : '成功' }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import {
  getSensitiveAccessPage,
  getSensitiveAccessSummary
} from '../../api/compliance'
import type { SensitiveAccessLog, SensitiveAccessSummary } from '../../api/compliance'

const loading = ref(false)
const summaryDays = ref(30)
const summary = reactive<Partial<SensitiveAccessSummary>>({})
const rows = ref<SensitiveAccessLog[]>([])
const dateRange = ref<string[]>([])

const query = reactive<{ dataCategory?: string; action?: string; actorId?: string }>({})
const pagination = reactive({ current: 1, pageSize: 20, total: 0 })

const columns = [
  { title: '时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作人', dataIndex: 'actorName', key: 'actorName' },
  { title: '角色', dataIndex: 'actorRole', key: 'actorRole' },
  { title: '动作', dataIndex: 'action', key: 'action' },
  { title: '数据类别', dataIndex: 'dataCategory', key: 'dataCategory' },
  { title: '对象', dataIndex: 'targetName', key: 'targetName' },
  { title: '字段', dataIndex: 'fields', key: 'fields' },
  { title: '用途', dataIndex: 'purpose', key: 'purpose' },
  { title: '结果', dataIndex: 'result', key: 'result' },
  { title: 'IP', dataIndex: 'ip', key: 'ip' }
]

function actionLabel(a: string) {
  return { VIEW: '查看', EXPORT: '导出', DECRYPT: '明文查看', PRINT: '打印' }[a] || a
}
function actionColor(a: string) {
  return { VIEW: 'blue', EXPORT: 'orange', DECRYPT: 'red', PRINT: 'purple' }[a] || 'default'
}

async function loadSummary() {
  try {
    const data = await getSensitiveAccessSummary({ days: summaryDays.value })
    Object.assign(summary, data)
  } catch (e) {
    message.error('加载概览失败')
  }
}

async function loadPage() {
  loading.value = true
  try {
    const data = await getSensitiveAccessPage({
      dataCategory: query.dataCategory,
      action: query.action,
      actorId: query.actorId || undefined,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
      pageNo: pagination.current,
      pageSize: pagination.pageSize
    })
    rows.value = data.records || []
    pagination.total = data.total || 0
  } catch (e) {
    message.error('加载审计记录失败')
  } finally {
    loading.value = false
  }
}

function onTableChange(pg: any) {
  pagination.current = pg.current
  pagination.pageSize = pg.pageSize
  loadPage()
}

function reset() {
  query.dataCategory = undefined
  query.action = undefined
  query.actorId = undefined
  dateRange.value = []
  pagination.current = 1
  loadPage()
}

function loadAll() {
  loadSummary()
  loadPage()
}

onMounted(loadAll)
</script>
