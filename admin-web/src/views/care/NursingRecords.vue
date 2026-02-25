<template>
  <PageContainer title="护理记录" subTitle="服务预约、执行日志、结果留痕">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="老人ID">
        <a-input-number v-model:value="query.elderId" :min="1" style="width: 140px" />
      </a-form-item>
      <a-form-item label="员工ID">
        <a-input-number v-model:value="query.staffId" :min="1" style="width: 140px" />
      </a-form-item>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="服务名/计划名/备注" allow-clear />
      </a-form-item>
      <a-form-item label="时间范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
    </SearchForm>

    <DataTable
      rowKey="sourceId"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'successFlag'">
          <a-tag :color="record.successFlag === 1 ? 'green' : 'red'">
            {{ record.successFlag === 1 ? '成功' : '失败' }}
          </a-tag>
        </template>
      </template>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getNursingRecordPage } from '../../api/nursing'
import type { NursingRecordItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<NursingRecordItem[]>([])
const query = reactive({
  elderId: undefined as number | undefined,
  staffId: undefined as number | undefined,
  keyword: '',
  range: undefined as [Dayjs, Dayjs] | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '记录类型', dataIndex: 'recordType', key: 'recordType', width: 110 },
  { title: '记录时间', dataIndex: 'recordTime', key: 'recordTime', width: 170 },
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '员工', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '服务名称', dataIndex: 'serviceName', key: 'serviceName', width: 150 },
  { title: '计划名称', dataIndex: 'planName', key: 'planName', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 110 },
  { title: '结果', dataIndex: 'successFlag', key: 'successFlag', width: 90 },
  { title: '备注', dataIndex: 'remark', key: 'remark' }
]

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<NursingRecordItem> = await getNursingRecordPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderId: query.elderId,
      staffId: query.staffId,
      keyword: query.keyword || undefined,
      timeFrom: query.range?.[0] ? dayjs(query.range[0]).format('YYYY-MM-DD HH:mm:ss') : undefined,
      timeTo: query.range?.[1] ? dayjs(query.range[1]).format('YYYY-MM-DD HH:mm:ss') : undefined
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.elderId = undefined
  query.staffId = undefined
  query.keyword = ''
  query.range = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

fetchData()
</script>
