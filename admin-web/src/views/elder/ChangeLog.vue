<template>
  <PageContainer title="变更记录" subTitle="床位/状态/护理等级变更追踪">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="老人姓名">
          <a-select v-model:value="query.elderId" allow-clear style="width: 180px" placeholder="请选择老人姓名">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="变更类型">
          <a-select v-model:value="query.changeType" allow-clear style="width: 160px" placeholder="请选择变更类型">
            <a-select-option value="ADMISSION">入住办理</a-select-option>
            <a-select-option value="DISCHARGE">退住办理</a-select-option>
            <a-select-option value="BED_CHANGE">床位变更</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="操作说明">
          <a-input v-model:value="query.reason" allow-clear placeholder="请输入操作说明" style="width: 220px" />
        </a-form-item>
        <a-form-item label="变更时间">
          <a-range-picker
            v-model:value="query.timeRange"
            value-format="YYYY-MM-DDTHH:mm:ss"
            show-time
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">搜索</a-button>
            <a-button @click="reset">清空</a-button>
            <a-button @click="exportRows">导出</a-button>
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
import { getElderPage } from '../../api/elder'
import { exportChangeLogs, getChangeLogs } from '../../api/elderLifecycle'
import type { ChangeLogItem, ElderItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<ChangeLogItem[]>([])
const total = ref(0)
const elders = ref<ElderItem[]>([])

const query = reactive({
  elderId: undefined as number | undefined,
  changeType: undefined as string | undefined,
  reason: undefined as string | undefined,
  timeRange: undefined as [string, string] | undefined,
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '变更类型', dataIndex: 'changeType', key: 'changeType', width: 160 },
  { title: '变更前', dataIndex: 'beforeValue', key: 'beforeValue', width: 160 },
  { title: '变更后', dataIndex: 'afterValue', key: 'afterValue', width: 160 },
  { title: '操作说明', dataIndex: 'reason', key: 'reason' },
  { title: '变更时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
]

async function fetchData() {
  loading.value = true
  try {
    const [startTime, endTime] = query.timeRange || []
    const res: PageResult<ChangeLogItem> = await getChangeLogs({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderId: query.elderId,
      changeType: query.changeType,
      reason: query.reason,
      startTime,
      endTime
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function loadElders() {
  const res: PageResult<ElderItem> = await getElderPage({ pageNo: 1, pageSize: 200 })
  elders.value = res.list
}

function reset() {
  query.elderId = undefined
  query.changeType = undefined
  query.reason = undefined
  query.timeRange = undefined
  query.pageNo = 1
  fetchData()
}

async function exportRows() {
  const [startTime, endTime] = query.timeRange || []
  await exportChangeLogs({
    elderId: query.elderId,
    changeType: query.changeType,
    reason: query.reason,
    startTime,
    endTime
  })
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

onMounted(async () => {
  await loadElders()
  await fetchData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
