<template>
  <PageContainer title="变更记录" subTitle="床位/状态/护理等级变更追踪">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="老人">
          <a-select v-model:value="query.elderId" allow-clear style="width: 180px">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
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
import { getChangeLogs } from '../../api/elderLifecycle'
import type { ChangeLogItem, ElderItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<ChangeLogItem[]>([])
const total = ref(0)
const elders = ref<ElderItem[]>([])

const query = reactive({
  elderId: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '变更类型', dataIndex: 'changeType', key: 'changeType', width: 160 },
  { title: '变更前', dataIndex: 'beforeValue', key: 'beforeValue', width: 160 },
  { title: '变更后', dataIndex: 'afterValue', key: 'afterValue', width: 160 },
  { title: '原因', dataIndex: 'reason', key: 'reason' },
  { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<ChangeLogItem> = await getChangeLogs({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderId: query.elderId
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
  query.pageNo = 1
  fetchData()
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
