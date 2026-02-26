<template>
  <PageContainer title="商品大类" subTitle="维护机构商城商品大类">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" class="search-form" :model="query">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" placeholder="分类名称/编码" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 120px">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
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
          <a-button type="primary" @click="openCreate">新增大类</a-button>
          <a-button @click="exportCsvData">导出CSV</a-button>
        </a-space>
      </div>

      <vxe-toolbar custom export></vxe-toolbar>
      <vxe-table border stripe show-overflow height="520" :loading="loading" :data="rows" :column-config="{ resizable: true }">
        <vxe-column field="categoryCode" title="分类编码" width="180" />
        <vxe-column field="categoryName" title="分类名称" min-width="180" />
        <vxe-column field="remark" title="备注" min-width="200" />
        <vxe-column field="status" title="状态" width="100">
          <template #default="{ row }">
            <a-tag :color="row.status === 1 ? 'green' : 'default'">{{ row.status === 1 ? '启用' : '停用' }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column title="操作" width="220" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a @click="openEdit(row)">编辑</a>
              <a @click="toggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</a>
              <a style="color: #ff4d4f" @click="remove(row)">删除</a>
            </a-space>
          </template>
        </vxe-column>
      </vxe-table>
    </a-card>

    <a-drawer v-model:open="editorOpen" title="商品大类信息" width="460">
      <a-form layout="vertical" :model="form" :rules="rules" ref="formRef">
        <a-form-item label="分类名称" name="categoryName" required>
          <a-input v-model:value="form.categoryName" />
        </a-form-item>
        <a-form-item label="分类编码" name="categoryCode" required>
          <a-input v-model:value="form.categoryCode" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="form.status" checked-children="启用" un-checked-children="停用" />
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
import { onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { getProductCategoryList, createProductCategory, updateProductCategory, deleteProductCategory } from '../../api/store'
import type { ProductCategoryItem } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<ProductCategoryItem[]>([])
const editorOpen = ref(false)
const formRef = ref()

const query = reactive({
  keyword: '',
  status: undefined as number | undefined
})
const form = reactive<any>({
  id: undefined,
  categoryCode: '',
  categoryName: '',
  remark: '',
  status: true
})

const rules = {
  categoryName: [{ required: true, message: '请输入分类名称' }],
  categoryCode: [
    { required: true, message: '请输入分类编码' },
    { pattern: /^[A-Za-z0-9_-]+$/, message: '分类编码仅允许字母/数字/下划线/中划线' }
  ]
}

async function fetchData() {
  loading.value = true
  try {
    rows.value = await getProductCategoryList({
      keyword: query.keyword || undefined,
      status: query.status
    })
  } finally {
    loading.value = false
  }
}

function reset() {
  query.keyword = ''
  query.status = undefined
  fetchData()
}

function exportCsvData() {
  exportCsv(
    rows.value.map((t) => ({
      编码: t.categoryCode,
      名称: t.categoryName,
      备注: t.remark,
      状态: t.status === 1 ? '启用' : '停用'
    })),
    '商品大类'
  )
}

function openCreate() {
  Object.assign(form, { id: undefined, categoryCode: '', categoryName: '', remark: '', status: true })
  editorOpen.value = true
}

function openEdit(row: ProductCategoryItem) {
  Object.assign(form, {
    id: row.id,
    categoryCode: row.categoryCode,
    categoryName: row.categoryName,
    remark: row.remark,
    status: row.status === 1
  })
  editorOpen.value = true
}

async function submit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = {
      categoryCode: form.categoryCode,
      categoryName: form.categoryName,
      remark: form.remark,
      status: form.status ? 1 : 0
    }
    if (form.id) {
      await updateProductCategory(form.id, payload)
      message.success('保存成功')
    } else {
      await createProductCategory(payload)
      message.success('创建成功')
    }
    editorOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

function toggleStatus(row: ProductCategoryItem) {
  Modal.confirm({
    title: row.status === 1 ? '确认停用该分类？' : '确认启用该分类？',
    onOk: async () => {
      await updateProductCategory(row.id, {
        categoryCode: row.categoryCode,
        categoryName: row.categoryName,
        remark: row.remark,
        status: row.status === 1 ? 0 : 1
      })
      message.success('操作成功')
      fetchData()
    }
  })
}

function remove(row: ProductCategoryItem) {
  Modal.confirm({
    title: `确认删除分类「${row.categoryName}」？`,
    onOk: async () => {
      await deleteProductCategory(row.id)
      message.success('删除成功')
      fetchData()
    }
  })
}

onMounted(fetchData)
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
