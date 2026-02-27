<template>
  <PageContainer title="入库管理" subTitle="新增入库单与入库记录">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="商品">
          <a-select
            v-model:value="query.productId"
            :options="productOptions"
            allow-clear
            show-search
            option-filter-prop="label"
            style="width: 220px"
          />
        </a-form-item>
        <a-form-item label="日期">
          <a-range-picker v-model:value="query.range" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="openInbound">新增入库单</a-button>
          <a-button @click="exportTemplate">导出入库模板</a-button>
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
      >
        <vxe-column field="createTime" title="入库时间" width="180" />
        <vxe-column field="productName" title="商品名称" min-width="160" />
        <vxe-column field="productId" title="商品ID" width="120" />
        <vxe-column field="batchNo" title="批次号" min-width="160" />
        <vxe-column field="changeQty" title="入库数量" width="120" />
        <vxe-column field="remark" title="备注" min-width="160" />
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

    <a-drawer v-model:open="editorOpen" title="新增入库" width="520">
      <a-form layout="vertical" :model="form" :rules="rules" ref="formRef">
        <a-form-item label="商品" name="productId">
          <a-select v-model:value="form.productId" :options="productOptions" show-search allow-clear />
        </a-form-item>
        <a-form-item label="批次号">
          <a-input v-model:value="form.batchNo" placeholder="留空自动生成" />
        </a-form-item>
        <a-form-item label="数量" name="quantity">
          <a-input-number v-model:value="form.quantity" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="成本价">
          <a-input-number v-model:value="form.costPrice" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="有效期">
          <a-date-picker v-model:value="form.expireDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="库位">
          <a-input v-model:value="form.warehouseLocation" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
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
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { createInbound, getInventoryInboundPage } from '../../api/materialCenter'
import { getProductPage } from '../../api/store'
import type { InventoryInboundRequest, InventoryLogItem, PageResult, ProductItem } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<InventoryLogItem[]>([])
const total = ref(0)
const products = ref<ProductItem[]>([])

const query = reactive({
  productId: undefined as number | undefined,
  range: undefined as any,
  pageNo: 1,
  pageSize: 10
})

const editorOpen = ref(false)
const formRef = ref()
const form = reactive<InventoryInboundRequest>({
  productId: '',
  batchNo: '',
  quantity: 1,
  costPrice: 0,
  expireDate: undefined,
  warehouseLocation: '',
  remark: ''
})

const rules = {
  productId: [{ required: true, message: '请选择商品' }],
  quantity: [{ required: true, message: '请输入数量' }]
}

const productOptions = computed(() =>
  products.value.map((p) => ({
    label: `${p.productName} (ID:${p.idStr || p.id})`,
    value: p.id
  }))
)

async function fetchProducts() {
  const res: PageResult<ProductItem> = await getProductPage({ pageNo: 1, pageSize: 200 })
  products.value = res.list
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<InventoryLogItem> = await getInventoryInboundPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      productId: query.productId,
      dateFrom: query.range?.[0]?.format?.('YYYY-MM-DD'),
      dateTo: query.range?.[1]?.format?.('YYYY-MM-DD')
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.productId = undefined
  query.range = undefined
  query.pageNo = 1
  fetchData()
}

function handleSearch() {
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
    rows.value.map((r) => ({
      入库时间: r.createTime,
      商品名称: r.productName,
      商品ID: r.productId,
      批次号: r.batchNo,
      入库数量: r.changeQty,
      备注: r.remark
    })),
    '入库记录'
  )
}

function exportTemplate() {
  exportCsv(
    [
      {
        商品ID: '',
        批次号: '',
        数量: '',
        成本价: '',
        有效期: '',
        库位: '',
        备注: ''
      }
    ],
    '入库模板'
  )
}

function openInbound() {
  Object.assign(form, {
    productId: '',
    batchNo: '',
    quantity: 1,
    costPrice: 0,
    expireDate: undefined,
    warehouseLocation: '',
    remark: ''
  })
  editorOpen.value = true
}

async function submit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    await createInbound({
      ...form,
      productId: String(form.productId),
      expireDate: form.expireDate?.format?.('YYYY-MM-DD')
    })
    message.success('入库成功')
    editorOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchProducts()
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
