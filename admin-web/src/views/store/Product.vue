<template>
  <PageContainer title="商品管理" subTitle="维护商品信息、标签与安全库存">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" placeholder="商品名/编码" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 120px">
            <a-select-option :value="1">上架</a-select-option>
            <a-select-option :value="0">下架</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">搜索</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="openCreate">新增商品</a-button>
          <a-button @click="exportCsvData">导出CSV</a-button>
        </a-space>
      </div>

      <vxe-toolbar custom export></vxe-toolbar>
      <vxe-table
        border
        stripe
        show-overflow
        height="520"
        :loading="loading"
        :data="rows"
        :column-config="{ resizable: true }"
        :custom-config="{ storage: true, checkMethod: () => true }"
      >
        <vxe-column field="productCode" title="编码" width="140" />
        <vxe-column field="productName" title="商品名称" min-width="180" />
        <vxe-column field="price" title="售价" width="120" />
        <vxe-column field="pointsPrice" title="积分价" width="120" />
        <vxe-column field="safetyStock" title="安全库存" width="120" />
        <vxe-column field="currentStock" title="当前库存" width="120">
          <template #default="{ row }">
            <a-tag :color="row.currentStock < (row.safetyStock || 0) ? 'red' : 'blue'">
              {{ row.currentStock ?? 0 }}
            </a-tag>
          </template>
        </vxe-column>
        <vxe-column field="status" title="状态" width="100">
          <template #default="{ row }">
            <a-tag :color="row.status === 1 ? 'green' : 'default'">{{ row.status === 1 ? '上架' : '下架' }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="tags" title="标签" min-width="200">
          <template #default="{ row }">
            <a-space wrap>
              <a-tag v-for="tag in resolveTags(row)" :key="tag.id" color="blue">{{ tag.tagName }}</a-tag>
            </a-space>
          </template>
        </vxe-column>
        <vxe-column title="操作" width="220" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a @click="openEdit(row)">编辑</a>
              <a @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</a>
            </a-space>
          </template>
        </vxe-column>
      </vxe-table>

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

    <a-drawer v-model:open="editorOpen" title="商品信息" width="520" @close="resetEditor">
      <a-form layout="vertical" :model="form" :rules="rules" ref="formRef">
        <a-form-item label="商品名称" name="productName" required>
          <a-input v-model:value="form.productName" />
        </a-form-item>
        <a-form-item label="商品编码">
          <a-input v-model:value="form.productCode" placeholder="留空自动生成" />
        </a-form-item>
        <a-form-item label="售价">
          <a-input-number v-model:value="form.price" style="width: 100%" />
        </a-form-item>
        <a-form-item label="积分价" name="pointsPrice" required>
          <a-input-number v-model:value="form.pointsPrice" style="width: 100%" />
        </a-form-item>
        <a-form-item label="安全库存">
          <a-input-number v-model:value="form.safetyStock" style="width: 100%" />
        </a-form-item>
        <a-form-item label="商品标签">
          <a-select v-model:value="form.tagIds" mode="multiple" :options="tagOptions" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="form.status" checked-children="上架" un-checked-children="下架" />
        </a-form-item>
      </a-form>

      <div class="drawer-footer">
        <a-space>
          <a-button @click="editorOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="submit">保存</a-button>
        </a-space>
      </div>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { getProductPage, createProduct, updateProduct, getProductTagList } from '../../api/store'
import type { PageResult, ProductItem, ProductTagItem } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<ProductItem[]>([])
const total = ref(0)
const tags = ref<ProductTagItem[]>([])

const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})

const editorOpen = ref(false)
const formRef = ref()
const form = reactive<any>({
  id: undefined,
  productName: '',
  productCode: '',
  price: 0,
  pointsPrice: 0,
  safetyStock: 0,
  tagIds: [] as number[],
  status: true
})

const rules = {
  productName: [{ required: true, message: '请输入商品名称' }],
  pointsPrice: [{ required: true, message: '请输入积分价' }]
}

const tagOptions = computed(() => tags.value.map((t) => ({ label: t.tagName, value: t.id })))

function resolveTags(row: ProductItem) {
  const ids = String((row as any).tagIds || '')
    .split(',')
    .map((v) => Number(v))
    .filter(Boolean)
  return tags.value.filter((t) => ids.includes(t.id))
}

async function fetchTags() {
  tags.value = await getProductTagList()
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<ProductItem> = await getProductPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      status: query.status
    })
    rows.value = res.list
    total.value = res.total
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

function onPageSizeChange(_page: number, size: number) {
  query.pageSize = size
  query.pageNo = 1
  fetchData()
}

function exportCsvData() {
  exportCsv(
    rows.value.map((p) => ({
      编码: p.productCode,
      名称: p.productName,
      售价: p.price,
      积分价: p.pointsPrice,
      安全库存: p.safetyStock,
      状态: p.status === 1 ? '上架' : '下架'
    })),
    '商品列表'
  )
}

function openCreate() {
  Object.assign(form, {
    id: undefined,
    productName: '',
    productCode: '',
    price: 0,
    pointsPrice: 0,
    safetyStock: 0,
    tagIds: [],
    status: true
  })
  editorOpen.value = true
}

function openEdit(row: ProductItem) {
  Object.assign(form, {
    id: row.id,
    productName: row.productName,
    productCode: row.productCode,
    price: row.price ?? 0,
    pointsPrice: row.pointsPrice ?? 0,
    safetyStock: row.safetyStock ?? 0,
    tagIds: String((row as any).tagIds || '')
      .split(',')
      .map((v) => Number(v))
      .filter(Boolean),
    status: row.status === 1
  })
  editorOpen.value = true
}

function resetEditor() {
  formRef.value?.resetFields?.()
}

async function submit() {
  try {
    await formRef.value?.validate()
    saving.value = true
    const payload = {
      productName: form.productName,
      productCode: form.productCode || undefined,
      price: form.price,
      pointsPrice: form.pointsPrice,
      safetyStock: form.safetyStock,
      status: form.status ? 1 : 0,
      tagIds: (form.tagIds || []).join(',')
    }
    if (form.id) {
      await updateProduct(form.id, payload)
      message.success('保存成功')
    } else {
      await createProduct(payload)
      message.success('创建成功')
    }
    editorOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

function toggleStatus(row: ProductItem) {
  Modal.confirm({
    title: row.status === 1 ? '确认下架该商品？' : '确认上架该商品？',
    onOk: async () => {
      await updateProduct(row.id, { status: row.status === 1 ? 0 : 1 })
      message.success('操作成功')
      fetchData()
    }
  })
}

onMounted(() => {
  fetchTags()
  fetchData()
})
</script>

<style scoped>
.search-form {
  row-gap: 8px;
}
.table-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.drawer-footer {
  margin-top: 16px;
  text-align: right;
}
</style>
