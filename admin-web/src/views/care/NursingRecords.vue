<template>
  <PageContainer title="护理记录" subTitle="服务预约、执行日志、结果留痕">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="老人">
        <a-select
          v-model:value="query.elderId"
          style="width: 220px"
          allow-clear
          show-search
          placeholder="输入姓名搜索"
          :filter-option="false"
          :options="elderOptions"
          @search="searchElders"
          @focus="() => !elderOptions.length && searchElders('')"
        />
      </a-form-item>
      <a-form-item label="员工">
        <a-select
          v-model:value="query.staffId"
          style="width: 220px"
          allow-clear
          show-search
          placeholder="输入员工姓名/拼音首字母"
          :filter-option="false"
          :options="staffOptions"
          @search="searchStaff"
          @focus="() => !staffOptions.length && searchStaff('')"
        />
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
import { onMounted, reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { getNursingRecordPage } from '../../api/nursing'
import { normalizeResidentId } from '../../utils/id'
import type { Id, NursingRecordItem, PageResult } from '../../types'
import { resolveCareError } from './careError'

const loading = ref(false)
const rows = ref<NursingRecordItem[]>([])
const route = useRoute()
const routeResidentId = normalizeResidentId(route.query as Record<string, unknown>)
const { elderOptions, searchElders } = useElderOptions({ pageSize: 50 })
const { staffOptions, searchStaff } = useStaffOptions({ pageSize: 220, preloadSize: 500 })
const query = reactive({
  elderId: routeResidentId as Id | undefined,
  staffId: undefined as string | undefined,
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
      staffId: query.staffId ? Number(query.staffId) : undefined,
      keyword: query.keyword || undefined,
      timeFrom: query.range?.[0] ? dayjs(query.range[0]).format('YYYY-MM-DDTHH:mm:ss') : undefined,
      timeTo: query.range?.[1] ? dayjs(query.range[1]).format('YYYY-MM-DDTHH:mm:ss') : undefined
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } catch (error) {
    message.error(resolveCareError(error, '加载护理记录失败'))
    rows.value = []
    pagination.total = 0
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
  query.elderId = routeResidentId
  query.staffId = undefined
  query.keyword = ''
  query.range = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

onMounted(async () => {
  await Promise.all([searchElders(''), searchStaff('')])
  fetchData()
})
</script>
