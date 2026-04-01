<template>
  <PageContainer :title="pageTitle" :subTitle="pageSubTitle">
    <template #stats>
      <div class="product-overview-grid">
        <div class="overview-card">
          <span>当前商品数</span>
          <strong>{{ total }}</strong>
          <small>按当前筛选结果实时统计</small>
        </div>
        <div class="overview-card">
          <span>已上架</span>
          <strong>{{ activeCount }}</strong>
          <small>可继续用于日常销售与内部领用</small>
        </div>
        <div class="overview-card">
          <span>商城可售</span>
          <strong>{{ mallEnabledCount }}</strong>
          <small>适合与商城同步上架</small>
        </div>
        <div class="overview-card">
          <span>库存风险</span>
          <strong>{{ lowStockCount }}</strong>
          <small>当前页低于安全库存的商品</small>
        </div>
      </div>
    </template>

    <a-card v-if="isStorageMode" class="card-elevated" :bordered="false" style="margin-bottom: 16px;">
      <a-alert
        type="info"
        show-icon
        message="当前为仓储视角：商品主数据与商城共享，商品维护统一在「商城与商品 > 商品管理」完成。"
      />
      <div class="link-actions">
        <a-space wrap>
          <a-button type="primary" @click="goToCommerceProduct">去商城维护商品</a-button>
          <a-button @click="goToCategory">商品大类</a-button>
          <a-button @click="goToTag">商品标签</a-button>
          <a-button @click="goToStockQuery">库存查询</a-button>
          <a-button @click="goToInbound">入库管理</a-button>
          <a-button @click="goToOutbound">出库管理</a-button>
        </a-space>
      </div>
    </a-card>

    <SearchForm :model="query" @search="handleSearch" @reset="reset">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="商品名/编码" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 120px">
          <a-select-option :value="1">上架</a-select-option>
          <a-select-option :value="0">下架</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="商品大类">
        <a-select v-model:value="query.category" allow-clear style="width: 180px" :options="categoryOptions" />
      </a-form-item>
      <a-form-item label="业务域">
        <a-select v-model:value="query.businessDomain" allow-clear style="width: 160px" :options="businessDomainOptions" />
      </a-form-item>
      <a-form-item label="物资类型">
        <a-select v-model:value="query.itemType" allow-clear style="width: 160px" :options="itemTypeOptions" />
      </a-form-item>
      <a-form-item label="商城可售">
        <a-select v-model:value="query.mallEnabled" allow-clear style="width: 120px">
          <a-select-option :value="1">是</a-select-option>
          <a-select-option :value="0">否</a-select-option>
        </a-select>
      </a-form-item>
    </SearchForm>

    <section class="surface-toolbar">
      <div class="surface-toolbar-title">
        <strong>{{ isStorageMode ? '仓储商品联动视角' : '商城商品工作台' }}</strong>
        <span>{{ isStorageMode ? '仓储端以查看库存和联动维护为主，商品主数据统一由商城侧维护。' : '集中处理商品上架、标签、定价、安全库存与商城同步状态。' }}</span>
      </div>
      <a-space wrap>
        <a-tag color="processing">{{ isStorageMode ? '仓储视角' : '商城视角' }}</a-tag>
        <a-tag color="blue">业务域 {{ query.businessDomain ? domainLabel(query.businessDomain) : '全部' }}</a-tag>
        <a-tag color="gold">库存风险 {{ lowStockCount }}</a-tag>
        <a-tag color="green">商城可售 {{ mallEnabledCount }}</a-tag>
      </a-space>
    </section>

    <section class="card-elevated product-workspace">
      <div class="table-actions">
        <div class="table-head">
          <div>
            <strong>商品列表</strong>
            <span>统一查看商品主数据、库存状态、商城可售性和标签归类，降低跨模块跳转成本。</span>
          </div>
          <a-space wrap>
            <a-tag color="default">共 {{ total }} 条</a-tag>
            <a-tag color="purple">低库存 {{ lowStockCount }} 条</a-tag>
          </a-space>
        </div>
        <div class="toolbar-actions">
          <a-button v-if="!isStorageMode" type="primary" @click="openCreate">新增商品</a-button>
          <a-button v-else type="primary" @click="goToCommerceProduct">去商城维护</a-button>
          <a-button @click="exportCsvData">导出CSV</a-button>
        </div>
      </div>

      <div class="table-frame">
        <vxe-toolbar class="product-vxe-toolbar" custom export></vxe-toolbar>
        <vxe-table
          class="product-vxe-table"
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
          <vxe-column field="productName" title="商品名称" min-width="220">
            <template #default="{ row }">
              <div class="product-name-cell">
                <strong>{{ row.productName }}</strong>
                <span>{{ row.category || '未分类' }} / {{ itemTypeLabel(row.itemType) }}</span>
              </div>
            </template>
          </vxe-column>
          <vxe-column field="category" title="商品大类" width="140" />
          <vxe-column field="businessDomain" title="业务域" width="130">
            <template #default="{ row }">
              <a-tag :color="domainColor(row.businessDomain)">{{ domainLabel(row.businessDomain) }}</a-tag>
            </template>
          </vxe-column>
          <vxe-column field="itemType" title="物资类型" width="130">
            <template #default="{ row }">
              <a-tag>{{ itemTypeLabel(row.itemType) }}</a-tag>
            </template>
          </vxe-column>
          <vxe-column field="mallEnabled" title="商城可售" width="110">
            <template #default="{ row }">
              <a-tag :color="row.mallEnabled === 1 ? 'green' : 'default'">{{ row.mallEnabled === 1 ? '是' : '否' }}</a-tag>
            </template>
          </vxe-column>
          <vxe-column field="price" title="售价" width="120" />
          <vxe-column field="pointsPrice" title="积分价" width="120" />
          <vxe-column field="safetyStock" title="安全库存" width="120" />
          <vxe-column field="currentStock" title="当前库存" width="120">
            <template #default="{ row }">
              <a-tag :color="Number(row.currentStock || 0) < Number(row.safetyStock || 0) ? 'red' : 'blue'">
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
          <vxe-column :title="isStorageMode ? '联动操作' : '操作'" :width="isStorageMode ? 280 : 220" fixed="right">
            <template #default="{ row }">
              <a-space>
                <a v-if="!isStorageMode" @click="openEdit(row)">编辑</a>
                <a v-if="!isStorageMode" @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</a>
                <a v-if="isStorageMode" @click="goToStockQuery">查看库存</a>
                <a v-if="isStorageMode" @click="goToCommerceProduct">商品维护</a>
              </a-space>
            </template>
          </vxe-column>
        </vxe-table>
      </div>

      <div class="pager-row">
        <a-pagination
          :current="query.pageNo"
          :page-size="query.pageSize"
          :total="total"
          show-size-changer
          @change="onPageChange"
          @showSizeChange="onPageSizeChange"
        />
      </div>
    </section>

    <a-drawer v-if="!isStorageMode" v-model:open="editorOpen" title="商品信息" width="520" @close="resetEditor">
      <a-form layout="vertical" :model="form" :rules="rules" ref="formRef">
        <a-form-item label="商品名称" name="productName" required>
          <a-input v-model:value="form.productName" />
        </a-form-item>
        <a-form-item label="商品编码">
          <a-input v-model:value="form.productCode" placeholder="留空自动生成" />
        </a-form-item>
        <a-form-item label="售价">
          <a-input-number v-model:value="form.price" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="积分价" name="pointsPrice" required>
          <a-input-number v-model:value="form.pointsPrice" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="商品大类">
          <a-select v-model:value="form.category" :options="categoryOptions" allow-clear />
        </a-form-item>
        <a-form-item label="安全库存">
          <a-input-number v-model:value="form.safetyStock" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="商品标签">
          <a-select v-model:value="form.tagIds" mode="multiple" :options="tagOptions" allow-clear />
        </a-form-item>
        <a-form-item label="业务域">
          <a-select v-model:value="form.businessDomain" :options="businessDomainOptions" />
        </a-form-item>
        <a-form-item label="物资类型">
          <a-select v-model:value="form.itemType" :options="itemTypeOptions" />
        </a-form-item>
        <a-form-item label="商城可售">
          <a-switch v-model:checked="form.mallEnabled" checked-children="是" un-checked-children="否" />
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
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import { exportCsv } from '../../utils/export'
import { getProductPage, createProduct, updateProduct, getProductTagList, getProductCategoryList } from '../../api/store'
import type { PageResult, ProductItem, ProductTagItem, ProductCategoryItem } from '../../types'

const props = withDefaults(defineProps<{
  title?: string
  subTitle?: string
  mode?: 'storage' | 'commerce'
}>(), {
  title: '商品管理',
  subTitle: '维护商品信息、标签与安全库存',
  mode: 'commerce'
})

const router = useRouter()
const isStorageMode = computed(() => props.mode === 'storage')
const pageTitle = props.title
const pageSubTitle = props.subTitle

const loading = ref(false)
const saving = ref(false)
const rows = ref<ProductItem[]>([])
const total = ref(0)
const tags = ref<ProductTagItem[]>([])
const categories = ref<ProductCategoryItem[]>([])
const activeCount = computed(() => rows.value.filter((item) => item.status === 1).length)
const mallEnabledCount = computed(() => rows.value.filter((item) => item.mallEnabled === 1).length)
const lowStockCount = computed(() => rows.value.filter((item) => Number(item.currentStock || 0) < Number(item.safetyStock || 0)).length)

const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  category: undefined as string | undefined,
  businessDomain: undefined as string | undefined,
  itemType: undefined as string | undefined,
  mallEnabled: (props.mode === 'commerce' ? 1 : undefined) as number | undefined,
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
  category: undefined as string | undefined,
  safetyStock: 0,
  tagIds: [] as number[],
  businessDomain: 'BOTH',
  itemType: 'CONSUMABLE',
  mallEnabled: true,
  status: true
})

const rules = {
  productName: [{ required: true, message: '请输入商品名称' }],
  pointsPrice: [{ required: true, message: '请输入积分价' }]
}

const businessDomainOptions = [
  { label: '企业内部', value: 'INTERNAL' },
  { label: '商城', value: 'MALL' },
  { label: '双用途', value: 'BOTH' }
]

const itemTypeOptions = [
  { label: '固定资产', value: 'ASSET' },
  { label: '耗材', value: 'CONSUMABLE' },
  { label: '食材', value: 'FOOD' },
  { label: '服务', value: 'SERVICE' }
]

const tagOptions = computed(() => tags.value.map((t) => ({ label: t.tagName, value: t.id })))
const categoryOptions = computed(() =>
  categories.value
    .filter((c) => c.status === 1)
    .map((c) => ({ label: c.categoryName, value: c.categoryName }))
)

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

async function fetchCategories() {
  categories.value = await getProductCategoryList()
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<ProductItem> = await getProductPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      status: query.status,
      category: query.category,
      businessDomain: query.businessDomain,
      itemType: query.itemType,
      mallEnabled: query.mallEnabled
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
  query.category = undefined
  query.businessDomain = undefined
  query.itemType = undefined
  query.mallEnabled = isStorageMode.value ? undefined : 1
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
    rows.value.map((p) => ({
      编码: p.productCode,
      名称: p.productName,
      大类: p.category,
      业务域: domainLabel(p.businessDomain),
      物资类型: itemTypeLabel(p.itemType),
      商城可售: p.mallEnabled === 1 ? '是' : '否',
      售价: p.price,
      积分价: p.pointsPrice,
      安全库存: p.safetyStock,
      状态: p.status === 1 ? '上架' : '下架'
    })),
    '商品列表'
  )
}

function openCreate() {
  if (isStorageMode.value) {
    goToCommerceProduct()
    return
  }
  Object.assign(form, {
    id: undefined,
    productName: '',
    productCode: '',
    price: 0,
    pointsPrice: 0,
    category: undefined,
    safetyStock: 0,
    tagIds: [],
    businessDomain: 'BOTH',
    itemType: 'CONSUMABLE',
    mallEnabled: true,
    status: true
  })
  editorOpen.value = true
}

function openEdit(row: ProductItem) {
  if (isStorageMode.value) {
    goToCommerceProduct()
    return
  }
  Object.assign(form, {
    id: row.id,
    productName: row.productName,
    productCode: row.productCode,
    price: row.price ?? 0,
    pointsPrice: row.pointsPrice ?? 0,
    category: row.category,
    safetyStock: row.safetyStock ?? 0,
    tagIds: String((row as any).tagIds || '')
      .split(',')
      .map((v) => Number(v))
      .filter(Boolean),
    businessDomain: row.businessDomain || 'BOTH',
    itemType: row.itemType || 'CONSUMABLE',
    mallEnabled: row.mallEnabled !== 0,
    status: row.status === 1
  })
  editorOpen.value = true
}

function resetEditor() {
  formRef.value?.resetFields?.()
}

async function submit() {
  if (isStorageMode.value) {
    goToCommerceProduct()
    return
  }
  if (!form.mallEnabled) {
    message.error('商城商品必须设置为“商城可售=是”')
    return
  }
  if (form.businessDomain === 'INTERNAL') {
    message.error('商城商品业务域不能为“企业内部”，请改为“商城”或“双用途”')
    return
  }
  try {
    await formRef.value?.validate()
    saving.value = true
    const payload = {
      productName: form.productName,
      productCode: form.productCode || undefined,
      price: form.price,
      pointsPrice: form.pointsPrice,
      category: form.category || undefined,
      safetyStock: form.safetyStock,
      businessDomain: form.businessDomain || 'BOTH',
      itemType: form.itemType || 'CONSUMABLE',
      mallEnabled: form.mallEnabled ? 1 : 0,
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
  if (isStorageMode.value) {
    goToCommerceProduct()
    return
  }
  Modal.confirm({
    title: row.status === 1 ? '确认下架该商品？' : '确认上架该商品？',
    onOk: async () => {
      await updateProduct(row.id, { status: row.status === 1 ? 0 : 1 })
      message.success('操作成功')
      fetchData()
    }
  })
}

function goToCommerceProduct() {
  router.push('/logistics/commerce/product')
}

function goToCategory() {
  router.push('/logistics/commerce/category')
}

function goToTag() {
  router.push('/logistics/commerce/tag')
}

function goToStockQuery() {
  router.push('/logistics/storage/stock-query')
}

function goToInbound() {
  router.push('/logistics/storage/inbound')
}

function goToOutbound() {
  router.push('/logistics/storage/outbound')
}

function domainLabel(value?: string) {
  if (value === 'INTERNAL') return '企业内部'
  if (value === 'MALL') return '商城'
  if (value === 'BOTH') return '双用途'
  return '未设置'
}

function domainColor(value?: string) {
  if (value === 'INTERNAL') return 'default'
  if (value === 'MALL') return 'blue'
  if (value === 'BOTH') return 'purple'
  return 'default'
}

function itemTypeLabel(value?: string) {
  if (value === 'ASSET') return '固定资产'
  if (value === 'FOOD') return '食材'
  if (value === 'SERVICE') return '服务'
  if (value === 'CONSUMABLE') return '耗材'
  return '未设置'
}

onMounted(() => {
  fetchTags()
  fetchCategories()
  fetchData()
})
</script>

<style scoped>
.product-overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.overview-card {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid #dce9f2;
  background: rgba(255, 255, 255, 0.86);
}

.overview-card span,
.table-head span,
.product-name-cell span {
  color: #6d8aa3;
  font-size: 12px;
}

.overview-card strong {
  color: #173854;
  font-size: 24px;
}

.overview-card small {
  color: #7a97b0;
}

.product-workspace {
  padding: 16px;
}

.table-actions,
.toolbar-actions,
.table-head,
.product-name-cell,
.pager-row {
  display: flex;
  align-items: center;
}

.table-actions {
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.toolbar-actions {
  gap: 8px;
  flex-wrap: wrap;
}

.table-head {
  justify-content: space-between;
  gap: 12px;
  flex: 1;
  flex-wrap: wrap;
}

.table-head strong,
.product-name-cell strong {
  color: #173854;
}

.table-head strong {
  display: block;
  font-size: 16px;
}

.table-frame {
  border: 1px solid #e4edf4;
  border-radius: 18px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.95);
}

.product-name-cell {
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.pager-row {
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 1200px) {
  .product-overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .product-overview-grid {
    grid-template-columns: 1fr;
  }

  .table-actions,
  .table-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
