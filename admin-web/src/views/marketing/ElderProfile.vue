<template>
  <PageContainer title="长者档案" sub-title="营销转化后的长者信息沉淀与跟踪">
    <MarketingQuickNav />
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="姓名">
          <ElderNameAutocomplete v-model:value="query.keyword" placeholder="姓名(编号)" width="220px" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 130px">
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

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <MarketingListToolbar :selected-count="selectedRowKeys.length" tip="支持批量删除与导出">
        <a-space>
          <a-button :disabled="!selectedSingleRecord" @click="viewSelected">查看档案</a-button>
        </a-space>
      </MarketingListToolbar>
      <StatefulBlock :loading="loading" :error="errorMessage" :empty="!rows.length" empty-text="暂无长者档案" @retry="fetchData">
        <a-table
          :data-source="rows"
          :columns="columns"
          :loading="loading"
          row-key="id"
          :row-selection="rowSelection"
          :pagination="false"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
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
      </StatefulBlock>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import MarketingQuickNav from './components/MarketingQuickNav.vue'
import MarketingListToolbar from './components/MarketingListToolbar.vue'
import { getElderPage } from '../../api/elder'
import type { ElderItem, PageResult } from '../../types'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const rows = ref<ElderItem[]>([])
const total = ref(0)
const selectedRowKeys = ref<string[]>([])

const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '姓名', dataIndex: 'fullName', key: 'fullName', width: 140 },
  { title: '电话', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '入住日期', dataIndex: 'admissionDate', key: 'admissionDate', width: 140 },
  { title: '床位号', dataIndex: 'bedNo', key: 'bedNo', width: 120 },
  { title: '护理等级', dataIndex: 'careLevel', key: 'careLevel', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 }
]
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))

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
  errorMessage.value = ''
  try {
    const page: PageResult<ElderItem> = await getElderPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      status: query.status
    })
    rows.value = (page.list || []).map((item) => ({ ...item, id: String(item.id) }))
    total.value = page.total || 0
    selectedRowKeys.value = []
  } catch (error: any) {
    errorMessage.value = error?.message || '加载长者档案失败'
  } finally {
    loading.value = false
  }
}

function reset() {
  query.keyword = ''
  query.status = undefined
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

function goDetail(id: string | number) {
  router.push(`/elder/detail/${id}`)
}

function viewSelected() {
  if (!selectedSingleRecord.value) {
    message.info('请先勾选 1 条长者记录')
    return
  }
  goDetail(String(selectedSingleRecord.value.id))
}

onMounted(fetchData)
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
