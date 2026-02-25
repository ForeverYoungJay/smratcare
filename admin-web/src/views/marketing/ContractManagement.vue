<template>
  <PageContainer title="合同管理" sub-title="合同查询、签约明细与在院状态">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="老人姓名">
          <a-input v-model:value="query.keyword" placeholder="姓名" allow-clear />
        </a-form-item>
        <a-form-item label="合同号">
          <a-input v-model:value="query.contractNo" placeholder="合同号" allow-clear />
        </a-form-item>
        <a-form-item label="在院状态">
          <a-select v-model:value="query.elderStatus" allow-clear style="width: 140px">
            <a-select-option :value="1">在院</a-select-option>
            <a-select-option :value="2">请假</a-select-option>
            <a-select-option :value="3">离院</a-select-option>
          </a-select>
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
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'elderStatus'">
            <a-tag :color="statusColor(record.elderStatus)">{{ statusLabel(record.elderStatus) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'depositAmount'">
            ¥{{ Number(record.depositAmount || 0).toFixed(2) }}
          </template>
        </template>
      </a-table>
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
import { getAdmissionRecords } from '../../api/elderLifecycle'
import type { AdmissionRecordItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<AdmissionRecordItem[]>([])
const total = ref(0)

const query = reactive({
  keyword: '',
  contractNo: '',
  elderStatus: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '合同号', dataIndex: 'contractNo', key: 'contractNo', width: 180 },
  { title: '长者', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '签约日期', dataIndex: 'admissionDate', key: 'admissionDate', width: 140 },
  { title: '押金', dataIndex: 'depositAmount', key: 'depositAmount', width: 120 },
  { title: '在院状态', dataIndex: 'elderStatus', key: 'elderStatus', width: 120 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '登记时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
]

function statusLabel(status?: number) {
  if (status === 1) return '在院'
  if (status === 2) return '请假'
  if (status === 3) return '离院'
  return '-'
}

function statusColor(status?: number) {
  if (status === 1) return 'green'
  if (status === 2) return 'orange'
  if (status === 3) return 'red'
  return 'default'
}

async function fetchData() {
  loading.value = true
  try {
    const page: PageResult<AdmissionRecordItem> = await getAdmissionRecords({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      contractNo: query.contractNo || undefined,
      elderStatus: query.elderStatus
    })
    rows.value = page.list || []
    total.value = page.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  query.keyword = ''
  query.contractNo = ''
  query.elderStatus = undefined
  query.pageNo = 1
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
