<template>
  <PageContainer title="商品管理" subTitle="商品与库存信息维护">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="商品名称/编码" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openDrawer()">新增商品</a-button>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'default'">
            {{ record.status === 1 ? '上架' : '下架' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a @click="openDrawer(record)">编辑</a>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="drawerOpen" :title="drawerTitle" width="480">
      <a-form :model="form" layout="vertical">
        <a-form-item label="商品编码" required>
          <a-input v-model:value="form.productCode" />
        </a-form-item>
        <a-form-item label="商品名称" required>
          <a-input v-model:value="form.productName" />
        </a-form-item>
        <a-form-item label="积分价格" required>
          <a-input-number v-model:value="form.pointsPrice" style="width: 100%" />
        </a-form-item>
        <a-form-item label="安全库存">
          <a-input-number v-model:value="form.safetyStock" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="submit">保存</a-button>
        </a-space>
      </template>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../components/PageContainer.vue'
import SearchForm from '../components/SearchForm.vue'
import DataTable from '../components/DataTable.vue'
import { getProductPage, createProduct, updateProduct } from '../api/store'
import type { ProductItem, PageResult } from '../types/api'

const query = reactive({ keyword: undefined as string | undefined, pageNo: 1, pageSize: 10 })
const rows = ref<ProductItem[]>([])
const loading = ref(false)
const saving = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '编码', dataIndex: 'productCode', key: 'productCode', width: 120 },
  { title: '名称', dataIndex: 'productName', key: 'productName' },
  { title: '积分价', dataIndex: 'pointsPrice', key: 'pointsPrice', width: 100 },
  { title: '安全库存', dataIndex: 'safetyStock', key: 'safetyStock', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 120 }
]

const drawerOpen = ref(false)
const form = reactive<Partial<ProductItem>>({})
const statusOptions = [
  { label: '上架', value: 1 },
  { label: '下架', value: 0 }
]

const drawerTitle = computed(() => (form.id ? '编辑商品' : '新增商品'))

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<ProductItem> = await getProductPage(query)
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } catch {
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
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openDrawer(record?: ProductItem) {
  Object.assign(form, record || { status: 1 })
  drawerOpen.value = true
}

async function submit() {
  saving.value = true
  try {
    if (form.id) {
      await updateProduct(form.id, form)
    } else {
      await createProduct(form)
    }
    message.success('保存成功')
    drawerOpen.value = false
    fetchData()
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

fetchData()
</script>
