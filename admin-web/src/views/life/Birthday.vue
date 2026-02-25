<template>
  <PageContainer title="会员生日" subTitle="生日提醒与关怀安排">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="老人姓名" allow-clear />
      </a-form-item>
      <a-form-item label="月份">
        <a-select v-model:value="query.month" :options="monthOptions" allow-clear style="width: 140px" />
      </a-form-item>
      <a-form-item label="未来天数">
        <a-input-number v-model:value="query.daysAhead" :min="1" :max="365" style="width: 160px" />
      </a-form-item>
    </SearchForm>

    <DataTable
      rowKey="elderId"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getBirthdayPage } from '../../api/life'
import type { BirthdayReminder, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<BirthdayReminder[]>([])
const query = reactive({
  keyword: '',
  month: undefined as number | undefined,
  daysAhead: 30 as number | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const columns = [
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 120 },
  { title: '出生日期', dataIndex: 'birthDate', key: 'birthDate', width: 120 },
  { title: '下次生日', dataIndex: 'nextBirthday', key: 'nextBirthday', width: 120 },
  { title: '剩余天数', dataIndex: 'daysUntil', key: 'daysUntil', width: 100 },
  { title: '届时年龄', dataIndex: 'ageOnNextBirthday', key: 'ageOnNextBirthday', width: 100 },
  { title: '房间号', dataIndex: 'roomNo', key: 'roomNo', width: 120 },
  { title: '床位号', dataIndex: 'bedNo', key: 'bedNo', width: 120 }
]
const monthOptions = Array.from({ length: 12 }).map((_, i) => ({ label: `${i + 1}月`, value: i + 1 }))

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<BirthdayReminder> = await getBirthdayPage({
      keyword: query.keyword || undefined,
      month: query.month,
      daysAhead: query.daysAhead,
      pageNo: query.pageNo,
      pageSize: query.pageSize
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
  query.keyword = ''
  query.month = undefined
  query.daysAhead = 30
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

fetchData()
</script>
