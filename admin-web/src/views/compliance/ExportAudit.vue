<template>
  <PageContainer title="导出审计" subTitle="导出二次确认留痕 · 谁在何时导出了哪个模块、什么范围、多少行">
    <template #extra>
      <a-button type="primary" :loading="loading" @click="loadPage">刷新</a-button>
    </template>

    <a-card class="card-elevated" :bordered="false" style="margin-bottom: 16px;">
      <a-form layout="inline">
        <a-form-item label="模块">
          <a-input v-model:value="query.module" allow-clear placeholder="如 ELDER_LIST" style="width: 180px" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 140px" placeholder="全部">
            <a-select-option value="USED">已导出</a-select-option>
            <a-select-option value="PENDING">待导出</a-select-option>
            <a-select-option value="EXPIRED">已过期</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="日期">
          <a-range-picker v-model:value="dateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="search">查询</a-button>
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
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'rowCount'">
            {{ record.rowCount == null ? '-' : record.rowCount }}
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
import { getExportLogPage } from '../../api/complianceSecurity'
import type { ExportLogItem } from '../../api/complianceSecurity'

const loading = ref(false)
const rows = ref<ExportLogItem[]>([])
const dateRange = ref<string[]>([])
const query = reactive<{ module?: string; status?: string }>({})
const pagination = reactive({ current: 1, pageSize: 20, total: 0 })

const columns = [
  { title: '时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作人', dataIndex: 'actorName', key: 'actorName' },
  { title: '角色', dataIndex: 'actorRole', key: 'actorRole' },
  { title: '模块', dataIndex: 'module', key: 'module' },
  { title: '范围', dataIndex: 'scope', key: 'scope', ellipsis: true },
  { title: '用途', dataIndex: 'purpose', key: 'purpose', ellipsis: true },
  { title: '行数', dataIndex: 'rowCount', key: 'rowCount' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '导出时间', dataIndex: 'usedAt', key: 'usedAt' },
  { title: 'IP', dataIndex: 'ip', key: 'ip' }
]

function statusLabel(status: string) {
  return { USED: '已导出', PENDING: '待导出', EXPIRED: '已过期' }[status] || status
}
function statusColor(status: string) {
  return { USED: 'green', PENDING: 'orange', EXPIRED: 'default' }[status] || 'default'
}

async function loadPage() {
  loading.value = true
  try {
    const data = await getExportLogPage({
      module: query.module || undefined,
      status: query.status,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
      pageNo: pagination.current,
      pageSize: pagination.pageSize
    })
    rows.value = data.records || []
    pagination.total = data.total || 0
  } catch (e) {
    message.error('加载导出审计失败')
  } finally {
    loading.value = false
  }
}

function search() {
  pagination.current = 1
  loadPage()
}

function onTableChange(pg: any) {
  pagination.current = pg.current
  pagination.pageSize = pg.pageSize
  loadPage()
}

function reset() {
  query.module = undefined
  query.status = undefined
  dateRange.value = []
  pagination.current = 1
  loadPage()
}

onMounted(loadPage)
</script>
